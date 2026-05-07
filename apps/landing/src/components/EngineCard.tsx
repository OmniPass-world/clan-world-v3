import { useState } from 'react'
import data from '../data/engineSizes.json'
import './EngineCard.css'

const LIMIT = data.limit

export default function EngineCard() {
  const [expanded, setExpanded] = useState(false)
  const totalBytes = data.stats.totalDeployedEngineBytes
  const closest = data.stats.closestToLimit // { name, size, distance }

  return (
    <section className="section section-engine">
      <div className="container">
        <div className={`engine-card ${expanded ? 'engine-card-open' : ''}`}>
          {/* Always-visible header */}
          <button
            className="engine-card-toggle"
            onClick={() => setExpanded(!expanded)}
            aria-expanded={expanded}
            aria-controls="engine-card-detail"
          >
            <div className="engine-card-eyebrow pixel">⌬ THE ENGINE ROOM ⌬</div>
            <h2 className="engine-card-title">
              Every byte squeezed under <span className="engine-limit">24,576</span>.
            </h2>
            <p className="engine-card-pitch">
              An EIP-2535 Diamond — {data.facets.length} facets sharing a single storage slot. The whole game engine had to fit
              under EIP-170's 24KB-per-contract ceiling, set in 2016 by the Spurious Dragon hard fork. We carved
              the logic into facets and libraries, optimized every byte, and one deployed unit —
              <strong> {closest.name}</strong> — clears the line by just <strong>{closest.distance} bytes</strong>.
            </p>
            <div className="engine-card-cta pixel">
              {expanded ? '▲ COLLAPSE' : '▼ EXPAND THE FACET TABLE'}
            </div>
          </button>

          {/* Expandable detail */}
          <div
            className={`engine-card-detail ${expanded ? 'engine-card-detail-open' : ''}`}
            id="engine-card-detail"
          >
            <div className="engine-card-detail-inner">
              <h3 className="engine-card-section-h">Facets — {data.facets.length} total</h3>
              <ul className="engine-facet-list">
                {[...data.facets]
                  .sort((a, b) => b.size - a.size)
                  .map((f) => (
                    <FacetRow key={f.name} name={f.name} size={f.size} />
                  ))}
              </ul>

              <h3 className="engine-card-section-h">Libraries — {data.libraries.length} total</h3>
              <p className="engine-lib-blurb">
                Internal-only libraries are inlined into facets at compile time (size shown as 0 because nothing
                stands alone). The deployable libraries below are linked by facets to share runtime code.
              </p>
              <ul className="engine-lib-list">
                {[...data.libraries]
                  .sort((a, b) => b.size - a.size)
                  .map((lib) => {
                    const internal = 'internal' in lib && lib.internal === true
                    return (
                      <li
                        key={lib.name}
                        className={`engine-lib-row ${internal ? 'engine-lib-internal' : ''}`}
                      >
                        <span className="engine-lib-name">{lib.name}</span>
                        <span className="engine-lib-size">
                          {internal ? 'inlined' : lib.size.toLocaleString() + ' bytes'}
                        </span>
                      </li>
                    )
                  })}
              </ul>

              <p className="engine-blurb">
                EIP-170 capped contract bytecode at 24,576 bytes when Spurious Dragon shipped on Nov 22, 2016.
                Nine years later we're still living inside it. Every byte in the table above was negotiated:
                inlined math, packed structs, stripped revert strings, function selectors moved from facet to
                library and back. {totalBytes.toLocaleString()} bytes of deployed engine logic across {data.facets.length} facets
                and {data.libraries.filter((lib) => !('internal' in lib && lib.internal === true)).length} linked libraries —
                each one a separate cliff-edge against the ceiling.
              </p>
            </div>
          </div>
        </div>
      </div>
    </section>
  )
}

function FacetRow({ name, size }: { name: string; size: number }) {
  const pct = Math.min(100, (size / LIMIT) * 100)
  // Color tier based on % of limit
  const tier = pct > 95 ? 'critical' : pct > 75 ? 'warn' : pct > 40 ? 'mid' : 'safe'
  return (
    <li className="engine-facet-row">
      <div className="engine-facet-row-head">
        <span className="engine-facet-name">{name}</span>
        <span className="engine-facet-size">
          {size.toLocaleString()} <span className="engine-facet-size-unit">B</span>
          <span className="engine-facet-pct">{pct.toFixed(1)}%</span>
        </span>
      </div>
      <div className={`engine-facet-bar engine-facet-bar-${tier}`}>
        <div className="engine-facet-bar-fill" style={{ width: `${pct}%` }} />
      </div>
    </li>
  )
}
