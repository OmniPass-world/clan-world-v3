import { useEffect, useMemo, useState } from 'react';
import {
  useAccount,
  useConnect,
  useDisconnect,
  useChainId,
  useReadContract,
  useWriteContract,
  useWaitForTransactionReceipt,
} from 'wagmi';
import { baseSepolia } from 'wagmi/chains';
import {
  encodeFunctionData,
  decodeFunctionResult,
  type Abi,
  type Address,
  type AbiFunction,
  type AbiParameter,
  isAddress,
} from 'viem';

// ABIs are imported from @clan-world/contracts as JSON modules. Source of truth lives at
// packages/contracts/abi/*.json and is regenerated from forge artifacts by `pnpm codegen`.
// Do NOT re-vendor copies here — drift between this UI and the real diamond is a footgun.
import IClanWorldArtifact from '@clan-world/contracts/abi/IClanWorld.json' with { type: 'json' };
import IDiamondLoupeArtifact from '@clan-world/contracts/abi/IDiamondLoupe.json' with { type: 'json' };
import IDiamondCutArtifact from '@clan-world/contracts/abi/IDiamondCut.json' with { type: 'json' };
import OwnershipFacetArtifact from '@clan-world/contracts/abi/OwnershipFacet.json' with { type: 'json' };

// Cast through `Abi` from viem — JSON imports lose the literal-type narrowing that an
// `as const`-d TS source would give us, so wagmi/viem can't infer call signatures unless
// we re-tag with the structural Abi type. Same pattern as packages/shared/src/adapters/IChainClient.ts.
const IClanWorldAbi = IClanWorldArtifact.abi as Abi;
const IDiamondLoupeAbi = IDiamondLoupeArtifact.abi as Abi;
const IDiamondCutAbi = IDiamondCutArtifact.abi as Abi;
const OwnershipFacetAbi = OwnershipFacetArtifact.abi as Abi;
import './App.css';

const DEFAULT_DIAMOND =
  (import.meta.env.VITE_DEFAULT_DIAMOND as string | undefined) ??
  '0x14392f8276c6234064395e74f0741e26f1613c1e';

const COMBINED_ABI = [
  ...IClanWorldAbi,
  ...IDiamondLoupeAbi,
  ...IDiamondCutAbi,
  ...OwnershipFacetAbi,
] as const;

type FnAbi = AbiFunction & { stateMutability: string };

// Canonical Solidity signature for an ABI item — handles tuple expansion so
// overloads like `Foo((uint256))` vs `Foo((address))` get distinct dedup keys.
// (`i.type` alone collapses both to `Foo(tuple)`.) viem uses this same canonical
// form internally for selector / topic hashing.
type AbiParamMin = { type: string; components?: readonly AbiParamMin[] };
function canonicalParamType(p: AbiParamMin): string {
  if (p.type === 'tuple' || p.type.startsWith('tuple')) {
    const suffix = p.type === 'tuple' ? '' : p.type.slice('tuple'.length); // '' or '[]' or '[N]'
    const inner = (p.components ?? []).map(canonicalParamType).join(',');
    return `(${inner})${suffix}`;
  }
  return p.type;
}
function canonicalSignature(name: string, inputs: readonly AbiParamMin[] | undefined): string {
  return `${name}(${(inputs ?? []).map(canonicalParamType).join(',')})`;
}

function dedupAbi(abi: readonly unknown[]): FnAbi[] {
  const seen = new Set<string>();
  const out: FnAbi[] = [];
  for (const item of abi) {
    if (!item || typeof item !== 'object') continue;
    const obj = item as { type?: string };
    if (obj.type !== 'function') continue;
    const fn = item as FnAbi;
    const sig = canonicalSignature(fn.name, fn.inputs as readonly AbiParamMin[]);
    if (seen.has(sig)) continue;
    seen.add(sig);
    out.push(fn);
  }
  return out;
}

const ALL_FUNCTIONS = dedupAbi(COMBINED_ABI as unknown as readonly unknown[]);

// Pass-through dedup for errors + events. Same overlap risk as functions
// (DiamondNotOwner from OwnershipFacet, etc. could repeat across interfaces).
// Uses the canonical (tuple-expanded) signature so overloads with tuple inputs
// of different shapes do not collapse to the same key.
function dedupErrorsAndEvents(abi: readonly unknown[]): unknown[] {
  const seenErrors = new Set<string>();
  const seenEvents = new Set<string>();
  const out: unknown[] = [];
  for (const item of abi) {
    if (!item || typeof item !== 'object') continue;
    const obj = item as { type?: string; name?: string; inputs?: readonly AbiParamMin[] };
    if (obj.type !== 'error' && obj.type !== 'event') continue;
    const sig = canonicalSignature(obj.name ?? '', obj.inputs);
    const seen = obj.type === 'error' ? seenErrors : seenEvents;
    if (seen.has(sig)) continue;
    seen.add(sig);
    out.push(item);
  }
  return out;
}

const ALL_ERRORS_AND_EVENTS = dedupErrorsAndEvents(COMBINED_ABI as unknown as readonly unknown[]);

// Deduped ABI for encode/write paths. The raw COMBINED_ABI concatenates four source
// ABIs and can carry duplicate selectors (e.g. owner(), facets() across overlapping
// interface fragments). Passing the raw concatenation to viem's encodeFunctionData /
// writeContract risks AbiFunctionNameNotFoundError or non-deterministic selection on
// collisions. Reuse the already-deduped function list AND keep deduped errors+events
// so viem can decode custom Solidity errors (e.g. `DiamondNotOwner(address)`) on revert
// — without this, users see a generic "execution reverted" instead of the named error.
const DEDUPED_ABI = [...ALL_FUNCTIONS, ...ALL_ERRORS_AND_EVENTS] as unknown as Abi;

function classifyMutability(fn: FnAbi): 'read' | 'write' {
  if (fn.stateMutability === 'view' || fn.stateMutability === 'pure') return 'read';
  return 'write';
}

function ConnectButton() {
  const { address, isConnected } = useAccount();
  const { connectors, connect, isPending } = useConnect();
  const { disconnect } = useDisconnect();
  const chainId = useChainId();

  if (isConnected && address) {
    const wrongChain = chainId !== baseSepolia.id;
    return (
      <div className="connect-block">
        <div className="addr">
          <span className="dot" /> {address}
        </div>
        <div className={wrongChain ? 'chain-warn' : 'chain-ok'}>
          chain: {chainId}
          {wrongChain ? ` ⚠ expected ${baseSepolia.id} (Base Sepolia)` : ''}
        </div>
        <button onClick={() => disconnect()} className="btn-sm">
          disconnect
        </button>
      </div>
    );
  }

  return (
    <div className="connect-block">
      <p>not connected</p>
      <div className="connectors">
        {connectors.map((c) => (
          <button key={c.uid} onClick={() => connect({ connector: c })} disabled={isPending}>
            {c.name}
          </button>
        ))}
      </div>
    </div>
  );
}

// Hardcoded danger-list for destructive diamond operations. Clicking "send tx (write)"
// on any of these surfaces a native confirm() dialog before firing the wallet popup —
// the dev-ui's `DEFAULT_DIAMOND` pre-populates the address input, so an admin connected
// with the owner wallet could otherwise mis-click into an irreversible facet change.
// Source: GH #281 (super-swarm finding from PR #272). Keep this list close to the
// write handler so it stays visible during diamond changes.
//
// `/^init.*/` (broader than the literal `initialize`) covers ABI surfaces like
// `initTreasury(...)` — one-shot setup functions that are as destructive as
// `initialize` if re-fired. `seedPools` is the other current one-shot setup write.
// Confirmed against packages/contracts/abi/IClanWorld.json on 2026-05-14 after a
// codex tier-2 finding flagged both as unguarded.
const DANGEROUS_FN_NAMES: ReadonlySet<string> = new Set([
  'diamondCut',
  'transferOwnership',
  'seedPools',
]);
const DANGEROUS_FN_PATTERNS: readonly RegExp[] = [
  /^init.*/,
  /^set.*Address$/,
  /^upgrade.*/,
];
function isDangerousFn(name: string): boolean {
  if (DANGEROUS_FN_NAMES.has(name)) return true;
  return DANGEROUS_FN_PATTERNS.some((re) => re.test(name));
}

function defaultInputForType(t: string): string {
  if (t === 'address') return '';
  if (t === 'bool') return 'false';
  if (t === 'bytes' || t.startsWith('bytes')) return '0x';
  if (t === 'tuple') return '{}';
  if (t === 'tuple[]') return '[]';
  if (t.endsWith('[]')) return '[]';
  if (t === 'string') return '';
  return '0';
}

// JSON numeric literals are parsed as JS doubles, which silently round any uint/int
// value larger than Number.MAX_SAFE_INTEGER (2^53-1). For a dev-ui that admins a
// diamond — token amounts, time-locked deposits, fee bps mantissas — that's a
// real data-corruption hazard. Users must enter big integers as JSON strings:
//   {"amount": "123456789012345678901234567890"}
// This helper detects the failure mode at parse-time and throws a clear error.
function assertSafeNumericForType(
  component: AbiParameter,
  value: unknown,
  context: string,
): void {
  if (typeof value !== 'number') return;
  if (!(component.type.startsWith('uint') || component.type.startsWith('int'))) return;
  if (!Number.isFinite(value)) {
    throw new Error(`${context}: ${component.type} value is not finite`);
  }
  if (!Number.isInteger(value)) {
    throw new Error(
      `${context}: ${component.type} value ${value} is not an integer — wrap as a JSON string instead`,
    );
  }
  if (!Number.isSafeInteger(value)) {
    throw new Error(
      `${context}: ${component.type} value ${value} exceeds Number.MAX_SAFE_INTEGER; pass as a JSON string (e.g. "${value}") to preserve precision`,
    );
  }
}

// AbiParameter-driven recursive parser. `input` carries the canonical Solidity type plus
// (for tuples) the `components` schema. We MUST recurse through components for tuple
// and tuple[] — JSON.stringify on an arbitrary nested value is not enough because the
// previous code path collapsed objects to "[object Object]" via String(x). With this
// shape, viem receives the correctly-shaped object/array and encoding succeeds.
function parseInputValue(input: AbiParameter, raw: string): unknown {
  const t = input.type;

  if (t === 'tuple') {
    let parsed: unknown;
    try {
      parsed = JSON.parse(raw);
    } catch (e) {
      throw new Error(
        `tuple ${input.name || ''}: invalid JSON — ${e instanceof Error ? e.message : String(e)}`,
        { cause: e },
      );
    }
    const components = (input as { components?: readonly AbiParameter[] }).components ?? [];
    if (components.length === 0) {
      throw new Error(`tuple ${input.name || ''}: ABI is missing components metadata`);
    }
    // If any component lacks a name, viem can't read fields by name — the value
    // MUST be encoded positionally as an array. Otherwise the canonical shape is
    // an object keyed by component.name.
    const allNamed = components.every((c) => c.name && c.name.length > 0);

    if (!allNamed) {
      if (!Array.isArray(parsed)) {
        throw new Error(
          `tuple ${input.name || ''}: components are unnamed; expected JSON array of length ${components.length}, got ${typeof parsed}`,
        );
      }
      if (parsed.length !== components.length) {
        throw new Error(
          `tuple ${input.name || ''}: expected array of length ${components.length}, got ${parsed.length}`,
        );
      }
      return parsed.map((item, idx) => {
        const component = components[idx];
        assertSafeNumericForType(component, item, `tuple ${input.name || ''}[${idx}]`);
        const itemRaw = typeof item === 'string' ? item : JSON.stringify(item);
        return parseInputValue(component, itemRaw);
      });
    }

    if (typeof parsed !== 'object' || parsed === null || Array.isArray(parsed)) {
      const got =
        parsed === null ? 'null' : Array.isArray(parsed) ? 'array' : typeof parsed;
      throw new Error(`tuple ${input.name || ''}: expected object, got ${got}`);
    }
    const obj = parsed as Record<string, unknown>;
    const result: Record<string, unknown> = {};
    for (const component of components) {
      // Use Object.hasOwn to avoid prototype-chain lookups (e.g. a field named
      // `toString` would otherwise resolve to Object.prototype.toString).
      const fieldKey = component.name as string;
      if (!Object.hasOwn(obj, fieldKey)) {
        throw new Error(
          `tuple ${input.name || ''}: missing field "${fieldKey}" (expected ${component.type})`,
        );
      }
      // Re-serialize so the recursive call can JSON.parse the leaf value via its own
      // type-specific branch. For strings/addresses/bytes we route the raw string
      // through the primitive branches below.
      assertSafeNumericForType(component, obj[fieldKey], `tuple ${input.name || ''}.${fieldKey}`);
      const fieldRaw =
        typeof obj[fieldKey] === 'string'
          ? (obj[fieldKey] as string)
          : JSON.stringify(obj[fieldKey]);
      result[fieldKey] = parseInputValue(component, fieldRaw);
    }
    return result;
  }

  if (t === 'tuple[]') {
    let parsed: unknown;
    try {
      parsed = JSON.parse(raw);
    } catch (e) {
      throw new Error(
        `tuple[] ${input.name || ''}: invalid JSON — ${e instanceof Error ? e.message : String(e)}`,
        { cause: e },
      );
    }
    if (!Array.isArray(parsed)) {
      throw new Error(`tuple[] ${input.name || ''}: expected array, got ${typeof parsed}`);
    }
    // Recurse each element as a `tuple` with the same components.
    const tupleElem: AbiParameter = { ...input, type: 'tuple' } as AbiParameter;
    return parsed.map((item) => parseInputValue(tupleElem, JSON.stringify(item)));
  }

  const v = raw.trim();
  if (t === 'address') {
    if (!isAddress(v)) throw new Error(`invalid address: ${v}`);
    return v as Address;
  }
  if (t === 'bool') return v === 'true' || v === '1';
  if (t === 'string') return raw;
  if (t.startsWith('bytes')) return (v.startsWith('0x') ? v : `0x${v}`) as `0x${string}`;
  if (t.endsWith('[]')) {
    const inner = t.slice(0, -2);
    const parsed = JSON.parse(v);
    if (!Array.isArray(parsed)) throw new Error(`expected array for ${t}`);
    // Build a synthetic AbiParameter for the inner element type. Primitive arrays
    // never carry components so an unnamed inner param is sufficient.
    const innerParam: AbiParameter = { ...input, type: inner, name: '' } as AbiParameter;
    return parsed.map((x, idx) => {
      assertSafeNumericForType(innerParam, x, `${input.name || t}[${idx}]`);
      return parseInputValue(innerParam, typeof x === 'string' ? x : JSON.stringify(x));
    });
  }
  if (t.startsWith('uint') || t.startsWith('int')) {
    return BigInt(v);
  }
  return raw;
}

function FunctionCard({ fn, diamond }: { fn: FnAbi; diamond: Address }) {
  const mode = classifyMutability(fn);
  const [inputs, setInputs] = useState<string[]>(() =>
    fn.inputs.map((i) => defaultInputForType(i.type)),
  );
  const [readError, setReadError] = useState<string | null>(null);
  const [readResult, setReadResult] = useState<string | null>(null);

  const writeContract = useWriteContract();
  const waitReceipt = useWaitForTransactionReceipt({ hash: writeContract.data });

  const setInput = (idx: number, val: string) => {
    setInputs((prev) => prev.map((p, i) => (i === idx ? val : p)));
  };

  const onRead = async () => {
    try {
      setReadError(null);
      setReadResult(null);
      const args = fn.inputs.map((i, idx) => parseInputValue(i, inputs[idx]));
      const data = encodeFunctionData({
        abi: DEDUPED_ABI,
        functionName: fn.name,
        args,
      } as never);
      const { config } = await import('./wagmi');
      const client = config.getClient({ chainId: baseSepolia.id });
      const res = await client.request({
        method: 'eth_call',
        params: [{ to: diamond, data }, 'latest'],
      });
      const rawHex = typeof res === 'string' ? res : JSON.stringify(res);
      // Decode via viem so users see addresses / numbers / structs rather than zero-padded hex.
      // BigInt values aren't JSON-stringifiable by default, so coerce them with a replacer.
      try {
        const decoded = decodeFunctionResult({
          abi: DEDUPED_ABI,
          functionName: fn.name,
          data: rawHex as `0x${string}`,
        });
        const isScalar =
          decoded === null ||
          (typeof decoded !== 'object' && typeof decoded !== 'bigint');
        if (isScalar) {
          setReadResult(String(decoded));
        } else if (typeof decoded === 'bigint') {
          setReadResult(decoded.toString());
        } else {
          setReadResult(
            JSON.stringify(
              decoded,
              (_k, v) => (typeof v === 'bigint' ? v.toString() : v),
              2,
            ),
          );
        }
      } catch (decodeErr: unknown) {
        const dmsg = decodeErr instanceof Error ? decodeErr.message : String(decodeErr);
        setReadResult(`${rawHex}\n\n(decode failed: ${dmsg})`);
      }
    } catch (e: unknown) {
      const msg = e instanceof Error ? e.message : String(e);
      setReadError(msg);
    }
  };

  const onWrite = () => {
    try {
      setReadError(null);
      // Parse inputs FIRST so an invalid address/etc. surfaces as a normal error
      // instead of being suppressed by the confirm() dialog.
      // Uses the AbiParameter-driven parser (#276) so tuple / tuple[] decode correctly.
      const args = fn.inputs.map((i, idx) => parseInputValue(i, inputs[idx]));
      // Destructive-op guardrail: native confirm() before firing the wallet popup.
      // See DANGEROUS_FN_NAMES / DANGEROUS_FN_PATTERNS above. GH #281.
      if (isDangerousFn(fn.name)) {
        const msg =
          `This is a destructive operation that will modify the diamond.\n\n` +
          `Function: ${fn.name}\n` +
          `Diamond: ${diamond}\n\n` +
          `Are you sure?`;
        if (!window.confirm(msg)) {
          return;
        }
      }
      writeContract.writeContract({
        address: diamond,
        abi: DEDUPED_ABI,
        functionName: fn.name as never,
        args: args as never,
      });
    } catch (e: unknown) {
      const msg = e instanceof Error ? e.message : String(e);
      setReadError(msg);
    }
  };

  return (
    <div className={`fn-card ${mode}`}>
      <div className="fn-header">
        <span className={`badge ${mode}`}>{mode}</span>
        <code className="fn-name">{fn.name}</code>
        <span className="mut">({fn.stateMutability})</span>
      </div>
      {fn.inputs.length > 0 && (
        <div className="inputs">
          {fn.inputs.map((inp, idx) => (
            <div key={idx} className="input-row">
              <label>
                {inp.name || `arg${idx}`} <span className="type">{inp.type}</span>
              </label>
              <input
                value={inputs[idx]}
                onChange={(e) => setInput(idx, e.target.value)}
                placeholder={defaultInputForType(inp.type)}
                spellCheck={false}
              />
            </div>
          ))}
        </div>
      )}
      <div className="actions">
        {mode === 'read' ? (
          <button onClick={onRead} className="btn-read">
            call (read)
          </button>
        ) : (
          <button
            onClick={onWrite}
            className="btn-write"
            disabled={writeContract.isPending || waitReceipt.isLoading}
          >
            {writeContract.isPending
              ? 'awaiting wallet…'
              : waitReceipt.isLoading
              ? 'mining…'
              : 'send tx (write)'}
          </button>
        )}
      </div>
      {readError && <pre className="err">{readError}</pre>}
      {readResult && <pre className="res">{readResult}</pre>}
      {writeContract.data && (
        <div className="tx-info">
          tx:{' '}
          <a
            href={`https://sepolia.basescan.org/tx/${writeContract.data}`}
            target="_blank"
            rel="noreferrer"
          >
            {writeContract.data.slice(0, 18)}…
          </a>{' '}
          {waitReceipt.isLoading && '⏳ pending'}
          {waitReceipt.isSuccess && '✓ confirmed'}
          {waitReceipt.isError && '✗ failed'}
        </div>
      )}
      {writeContract.error && <pre className="err">{writeContract.error.message}</pre>}
    </div>
  );
}

function App() {
  const [diamond, setDiamond] = useState<Address>(DEFAULT_DIAMOND as Address);
  const [diamondInput, setDiamondInput] = useState<string>(DEFAULT_DIAMOND);
  const [filter, setFilter] = useState('');
  const [modeFilter, setModeFilter] = useState<'all' | 'read' | 'write'>('all');
  const [onlyDeployed, setOnlyDeployed] = useState(true);

  const facetsCall = useReadContract({
    chainId: baseSepolia.id,
    address: diamond,
    abi: IDiamondLoupeAbi,
    functionName: 'facets',
    query: { enabled: isAddress(diamond) },
  });

  const deployedSelectors = useMemo(() => {
    if (!facetsCall.data) return new Set<string>();
    const out = new Set<string>();
    for (const facet of facetsCall.data as readonly {
      facetAddress: Address;
      functionSelectors: readonly string[];
    }[]) {
      for (const sel of facet.functionSelectors) {
        out.add(sel.toLowerCase());
      }
    }
    return out;
  }, [facetsCall.data]);

  const [selectorMap, setSelectorMap] = useState<Map<string, string>>(new Map());
  useEffect(() => {
    (async () => {
      const { toFunctionSelector } = await import('viem');
      const map = new Map<string, string>();
      for (const fn of ALL_FUNCTIONS) {
        try {
          // Pass the AbiFunction object directly. viem expands tuple components into
          // their canonical inner form for selector hashing (e.g. diamondCut becomes
          // diamondCut((address,uint8,bytes4[])[],address,bytes) — NOT
          // diamondCut(tuple[],address,bytes), which was the previous buggy form).
          const sel = toFunctionSelector(fn);
          map.set(`${fn.name}/${fn.inputs.length}`, sel.toLowerCase());
        } catch {
          // Defensive: a malformed ABI entry shouldn't break the whole filter.
        }
      }
      setSelectorMap(map);
    })();
  }, []);

  const filtered = useMemo(() => {
    return ALL_FUNCTIONS.filter((fn) => {
      if (filter && !fn.name.toLowerCase().includes(filter.toLowerCase())) return false;
      if (modeFilter !== 'all' && classifyMutability(fn) !== modeFilter) return false;
      if (onlyDeployed && deployedSelectors.size > 0) {
        const sel = selectorMap.get(`${fn.name}/${fn.inputs.length}`);
        if (sel && !deployedSelectors.has(sel)) return false;
      }
      return true;
    });
  }, [filter, modeFilter, onlyDeployed, deployedSelectors, selectorMap]);

  const setDiamondFromInput = () => {
    const v = diamondInput.trim();
    if (isAddress(v)) setDiamond(v as Address);
  };

  return (
    <div className="app">
      <header>
        <h1>ClanWorld Diamond Dev UI</h1>
        <p className="sub">
          Generic write-and-read interface for any ClanWorld diamond on Base Sepolia. Bundles
          IClanWorld + IDiamondLoupe + IDiamondCut + OwnershipFacet ABIs, filters to deployed
          selectors via the diamond's facets() loupe.
        </p>
      </header>

      <section className="card">
        <h2>1. Wallet</h2>
        <ConnectButton />
      </section>

      <section className="card">
        <h2>2. Diamond</h2>
        <div className="diamond-row">
          <input
            value={diamondInput}
            onChange={(e) => setDiamondInput(e.target.value)}
            placeholder="0x…"
            spellCheck={false}
          />
          <button onClick={setDiamondFromInput}>set</button>
        </div>
        <p className="active-diamond">
          active: <code>{diamond}</code>
        </p>
        <p className="loupe-status">
          {facetsCall.isLoading && '⏳ loading facets…'}
          {facetsCall.error && `✗ loupe error: ${facetsCall.error.message}`}
          {Boolean(facetsCall.data) && (
            <>
              ✓ {(facetsCall.data as readonly unknown[]).length} facets,{' '}
              {deployedSelectors.size} selectors deployed
            </>
          )}
        </p>
      </section>

      <section className="card">
        <h2>3. Functions</h2>
        <div className="filter-row">
          <input
            value={filter}
            onChange={(e) => setFilter(e.target.value)}
            placeholder="filter by name…"
            spellCheck={false}
          />
          <select value={modeFilter} onChange={(e) => setModeFilter(e.target.value as never)}>
            <option value="all">all</option>
            <option value="read">read only</option>
            <option value="write">write only</option>
          </select>
          <label className="checkbox">
            <input
              type="checkbox"
              checked={onlyDeployed}
              onChange={(e) => setOnlyDeployed(e.target.checked)}
            />
            only deployed selectors
          </label>
        </div>
        <p className="counts">
          showing {filtered.length} of {ALL_FUNCTIONS.length} functions
        </p>

        <div className="fn-list">
          {filtered.map((fn) => (
            <FunctionCard key={`${fn.name}/${fn.inputs.length}`} fn={fn} diamond={diamond} />
          ))}
        </div>
      </section>

      <footer>
        <p>
          ClanWorld Diamond Dev UI — basescan:{' '}
          <a
            href={`https://sepolia.basescan.org/address/${diamond}`}
            target="_blank"
            rel="noreferrer"
          >
            {diamond.slice(0, 10)}…{diamond.slice(-8)}
          </a>
        </p>
      </footer>
    </div>
  );
}

export default App;
