// @generated — do not edit by hand. Run: pnpm --filter @clan-world/contract-types codegen
export const iDiamondCutAbi = [
  {
    "type": "function",
    "name": "diamondCut",
    "inputs": [
      {
        "name": "diamondCut_",
        "type": "tuple[]",
        "internalType": "struct IDiamondCut.FacetCut[]",
        "components": [
          {
            "name": "facetAddress",
            "type": "address",
            "internalType": "address"
          },
          {
            "name": "action",
            "type": "uint8",
            "internalType": "enum IDiamondCut.FacetCutAction"
          },
          {
            "name": "functionSelectors",
            "type": "bytes4[]",
            "internalType": "bytes4[]"
          }
        ]
      },
      {
        "name": "init",
        "type": "address",
        "internalType": "address"
      },
      {
        "name": "data",
        "type": "bytes",
        "internalType": "bytes"
      }
    ],
    "outputs": [],
    "stateMutability": "nonpayable"
  },
  {
    "type": "event",
    "name": "DiamondCut",
    "inputs": [
      {
        "name": "diamondCut",
        "type": "tuple[]",
        "indexed": false,
        "internalType": "struct IDiamondCut.FacetCut[]",
        "components": [
          {
            "name": "facetAddress",
            "type": "address",
            "internalType": "address"
          },
          {
            "name": "action",
            "type": "uint8",
            "internalType": "enum IDiamondCut.FacetCutAction"
          },
          {
            "name": "functionSelectors",
            "type": "bytes4[]",
            "internalType": "bytes4[]"
          }
        ]
      },
      {
        "name": "init",
        "type": "address",
        "indexed": false,
        "internalType": "address"
      },
      {
        "name": "data",
        "type": "bytes",
        "indexed": false,
        "internalType": "bytes"
      }
    ],
    "anonymous": false
  }
] as const;

export type IDiamondCutAbiType = typeof iDiamondCutAbi;

// Derived event name union
export type IDiamondCutAbiEventName = (typeof iDiamondCutAbi)[number] extends infer T
  ? T extends { type: 'event'; name: string }
    ? T['name']
    : never
  : never;

// Derived function name union
export type IDiamondCutAbiFunctionName = (typeof iDiamondCutAbi)[number] extends infer T
  ? T extends { type: 'function'; name: string }
    ? T['name']
    : never
  : never;
