// @generated — do not edit by hand. Run: pnpm --filter @clan-world/contract-types codegen
export const iDiamondLoupeAbi = [
  {
    "type": "function",
    "name": "facetAddress",
    "inputs": [
      {
        "name": "selector",
        "type": "bytes4",
        "internalType": "bytes4"
      }
    ],
    "outputs": [
      {
        "name": "facet",
        "type": "address",
        "internalType": "address"
      }
    ],
    "stateMutability": "view"
  },
  {
    "type": "function",
    "name": "facetAddresses",
    "inputs": [],
    "outputs": [
      {
        "name": "facetAddresses_",
        "type": "address[]",
        "internalType": "address[]"
      }
    ],
    "stateMutability": "view"
  },
  {
    "type": "function",
    "name": "facetFunctionSelectors",
    "inputs": [
      {
        "name": "facet",
        "type": "address",
        "internalType": "address"
      }
    ],
    "outputs": [
      {
        "name": "selectors",
        "type": "bytes4[]",
        "internalType": "bytes4[]"
      }
    ],
    "stateMutability": "view"
  },
  {
    "type": "function",
    "name": "facets",
    "inputs": [],
    "outputs": [
      {
        "name": "facets_",
        "type": "tuple[]",
        "internalType": "struct IDiamondLoupe.Facet[]",
        "components": [
          {
            "name": "facetAddress",
            "type": "address",
            "internalType": "address"
          },
          {
            "name": "functionSelectors",
            "type": "bytes4[]",
            "internalType": "bytes4[]"
          }
        ]
      }
    ],
    "stateMutability": "view"
  }
] as const;

export type IDiamondLoupeAbiType = typeof iDiamondLoupeAbi;

// Derived event name union
export type IDiamondLoupeAbiEventName = (typeof iDiamondLoupeAbi)[number] extends infer T
  ? T extends { type: 'event'; name: string }
    ? T['name']
    : never
  : never;

// Derived function name union
export type IDiamondLoupeAbiFunctionName = (typeof iDiamondLoupeAbi)[number] extends infer T
  ? T extends { type: 'function'; name: string }
    ? T['name']
    : never
  : never;
