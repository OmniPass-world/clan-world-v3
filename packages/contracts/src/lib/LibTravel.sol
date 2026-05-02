// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

library LibTravel {
    function quoteTravel(uint8 srcRegion, uint8 dstRegion) internal pure returns (uint8 travelTicks, bytes8 path) {
        if (srcRegion > 8 || dstRegion > 8) {
            return (0, bytes8(0));
        }
        if (srcRegion == dstRegion || srcRegion == 0 || dstRegion == 0) {
            travelTicks = 0;
            path = bytes8(uint64(srcRegion) << 56);
            return (travelTicks, path);
        }
        travelTicks = travelTicksBetween(srcRegion, dstRegion);
        path = buildPath(srcRegion, dstRegion);
    }

    function travelTicksBetween(uint8 fromRegion, uint8 toRegion) internal pure returns (uint8) {
        if (fromRegion == 0 || toRegion == 0) return 0;
        if (fromRegion == toRegion) return 0;
        if (fromRegion > 8 || toRegion > 8) return 0;
        return distMatrix(fromRegion, toRegion);
    }

    function buildPath(uint8 src, uint8 dst) internal pure returns (bytes8) {
        if (src == dst) {
            return bytes8(uint64(src) << 56);
        }

        uint8[3][9] memory adj = [
            [0, 0, 0],
            [2, 4, 0],
            [1, 3, 0],
            [2, 4, 5],
            [1, 3, 6],
            [3, 7, 0],
            [4, 8, 0],
            [5, 8, 0],
            [6, 7, 0]
        ];

        uint8[9] memory parent;
        bool[9] memory visited;
        uint8[9] memory queue;
        uint256 head;
        uint256 tail;

        visited[src] = true;
        queue[tail++] = src;

        while (head < tail) {
            uint8 curr = queue[head++];
            if (curr == dst) break;
            for (uint256 ni = 0; ni < 3; ni++) {
                uint8 nb = adj[curr][ni];
                if (nb == 0) break;
                if (!visited[nb]) {
                    visited[nb] = true;
                    parent[nb] = curr;
                    queue[tail++] = nb;
                }
            }
        }

        uint8[8] memory path;
        uint256 pathLen;
        uint8 cur = dst;
        while (cur != src) {
            path[pathLen++] = cur;
            cur = parent[cur];
        }
        path[pathLen++] = src;

        bytes8 packed;
        uint64 byteShift = 56;
        for (uint256 i = pathLen; i > 0; i--) {
            packed = packed | bytes8(uint64(path[i - 1]) << byteShift);
            if (byteShift >= 8) byteShift -= 8;
        }
        return packed;
    }

    function distMatrix(uint8 src, uint8 dst) internal pure returns (uint8) {
        uint8[64] memory d = [
            uint8(0),
            1,
            2,
            1,
            3,
            2,
            4,
            3,
            1,
            0,
            1,
            2,
            2,
            3,
            3,
            4,
            2,
            1,
            0,
            1,
            1,
            2,
            2,
            3,
            1,
            2,
            1,
            0,
            2,
            1,
            3,
            2,
            3,
            2,
            1,
            2,
            0,
            3,
            1,
            2,
            2,
            3,
            2,
            1,
            3,
            0,
            2,
            1,
            4,
            3,
            2,
            3,
            1,
            2,
            0,
            1,
            3,
            4,
            3,
            2,
            2,
            1,
            1,
            0
        ];
        return d[uint8(src - 1) * 8 + uint8(dst - 1)];
    }
}
