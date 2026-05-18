// @generated — do not edit by hand. Run: pnpm --filter @clan-world/contract-types codegen
export const ownershipFacetAbi = [
  {
    "type": "function",
    "name": "owner",
    "inputs": [],
    "outputs": [
      {
        "name": "",
        "type": "address",
        "internalType": "address"
      }
    ],
    "stateMutability": "view"
  },
  {
    "type": "function",
    "name": "transferOwnership",
    "inputs": [
      {
        "name": "newOwner",
        "type": "address",
        "internalType": "address"
      }
    ],
    "outputs": [],
    "stateMutability": "nonpayable"
  },
  {
    "type": "event",
    "name": "OwnershipTransferred",
    "inputs": [
      {
        "name": "previousOwner",
        "type": "address",
        "indexed": true,
        "internalType": "address"
      },
      {
        "name": "newOwner",
        "type": "address",
        "indexed": true,
        "internalType": "address"
      }
    ],
    "anonymous": false
  },
  {
    "type": "error",
    "name": "DiamondNotOwner",
    "inputs": [
      {
        "name": "caller",
        "type": "address",
        "internalType": "address"
      }
    ]
  }
] as const;

export type OwnershipFacetAbiType = typeof ownershipFacetAbi;

// Derived event name union
export type OwnershipFacetAbiEventName = (typeof ownershipFacetAbi)[number] extends infer T
  ? T extends { type: 'event'; name: string }
    ? T['name']
    : never
  : never;

// Derived function name union
export type OwnershipFacetAbiFunctionName = (typeof ownershipFacetAbi)[number] extends infer T
  ? T extends { type: 'function'; name: string }
    ? T['name']
    : never
  : never;
