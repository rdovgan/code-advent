package com.rdovgan.advent.day8;

import org.apache.commons.lang3.math.NumberUtils;

import java.math.BigInteger;
import java.util.*;

/**
 * Space utilities — deterministic selection of closest pairs and DSU-based multiplication of top-3 components.
 *
 * Changes vs previous:
 * - distanceSq: squared Euclidean distance (correct for "straight-line distance" ordering, avoids sqrt).
 * - findClosestPairs: builds full list of pairs, sorts by (dist, a, b), returns first `count`.
 * - Pair: small POJO (Java 8 friendly).
 */
public class SpaceUtil {

    private static final Comparator<Pair> PAIR_COMPARATOR = Comparator
            .comparingLong((Pair p) -> p.dist)
            .thenComparingInt(p -> p.a)
            .thenComparingInt(p -> p.b);

    public static long distanceSq(Point one, Point two) {
        long dx = (long) one.x() - two.x();
        long dy = (long) one.y() - two.y();
        long dz = (long) one.z() - two.z();
        return dx * dx + dy * dy + dz * dz;
    }

    public static List<Point> definePoints(List<String> lines) {
        if (lines == null) return Collections.emptyList();
        List<Point> pts = new ArrayList<>(lines.size());
        for (String line : lines) {
            if (line == null) continue;
            String[] parts = line.trim().split(",");
            if (parts.length < 3) continue;
            Integer x = NumberUtils.createInteger(parts[0].trim());
            Integer y = NumberUtils.createInteger(parts[1].trim());
            Integer z = NumberUtils.createInteger(parts[2].trim());
            if (x == null || y == null || z == null) continue;
            pts.add(new Point(x, y, z));
        }
        return pts;
    }

    /**
     * Generate all pairs, sort deterministically by (dist asc, a asc, b asc) and return up-to `count` pairs.
     * Complexity: O(N^2) time and memory.
     */
    public static List<Pair> findClosestPairs(List<Point> points, int count) {
        if (points == null || points.size() < 2 || count <= 0) return Collections.emptyList();

        List<Pair> edges = buildAndSortAllEdges(points);
        int limit = Math.min(count, edges.size());
        return new ArrayList<>(edges.subList(0, limit));
    }

    /**
     * Build all edges and sort them deterministically by distance, then indices.
     */
    private static List<Pair> buildAndSortAllEdges(List<Point> points) {
        int n = points.size();
        List<Pair> edges = new ArrayList<>(n * (n - 1) / 2);

        for (int i = 0; i < n; i++) {
            Point a = points.get(i);
            for (int j = i + 1; j < n; j++) {
                Point b = points.get(j);
                long d = distanceSq(a, b);
                edges.add(new Pair(i, j, d));
            }
        }

        edges.sort(PAIR_COMPARATOR);
        return edges;
    }

    /**
     * Connect given points by the `count` closest pairs and return product of three largest component sizes.
     */
    public static BigInteger multiplyTop3ByConnectingKPairs(List<Point> points, int count) {
        if (points == null || points.isEmpty()) return BigInteger.ZERO;

        List<Pair> pairs = findClosestPairs(points, count);
        int n = points.size();
        DSU dsu = new DSU(n);
        for (Pair p : pairs) dsu.union(p.a, p.b);

        Map<Integer, Integer> sizes = new HashMap<>();
        for (int i = 0; i < n; i++) {
            int root = dsu.find(i);
            sizes.put(root, sizes.getOrDefault(root, 0) + 1);
        }

        List<Integer> sz = new ArrayList<>(sizes.values());
        sz.sort(Collections.reverseOrder());

        BigInteger res = BigInteger.ONE;
        for (int i = 0; i < Math.min(3, sz.size()); i++) {
            res = res.multiply(BigInteger.valueOf(sz.get(i)));
        }
        return res;
    }

	public record Pair(int a, int b, long dist) {
		@Override
		public String toString() {
			return "Pair{" + a + "," + b + ",d=" + dist + '}';
		}
	}

    private static final class DSU {
        private final int[] parent;
        private final int[] rank;
        DSU(int n) {
            parent = new int[n];
            rank = new int[n];
            for (int i = 0; i < n; i++) parent[i] = i;
        }
        int find(int x) {
            while (parent[x] != x) {
                parent[x] = parent[parent[x]];
                x = parent[x];
            }
            return x;
        }
        void union(int x, int y) {
            int rootX = find(x);
            int rootY = find(y);
            if (rootX == rootY) return;
            if (rank[rootX] < rank[rootY]) {
                parent[rootX] = rootY;
            } else if (rank[rootY] < rank[rootX]) {
                parent[rootY] = rootX;
            } else {
                parent[rootY] = rootX;
                rank[rootX]++;
            }
        }
    }
	/**
	 * Part Two:
	 * Continue connecting closest unconnected pairs until all points form one circuit.
	 * Return product of the X coordinates of the last two junction boxes that were connected
	 * to make the entire set a single component. Returns BigInteger.ZERO for invalid input.
	 */
	public static BigInteger productOfXCoordinatesOfLastConnection(List<Point> points) {
		if (points == null || points.size() < 2) return BigInteger.ZERO;

		List<Pair> edges = buildAndSortAllEdges(points);
		DSU dsu = new DSU(points.size());
		int components = points.size();

		for (Pair e : edges) {
			int a = e.a;
			int b = e.b;
			int ra = dsu.find(a);
			int rb = dsu.find(b);
			if (ra == rb) continue;
			dsu.union(ra, rb);
			components--;
			if (components == 1) {
				long xa = points.get(a).x();
				long xb = points.get(b).x();
				return BigInteger.valueOf(xa).multiply(BigInteger.valueOf(xb));
			}
		}

		// if we never reached single component (shouldn't happen for connected complete graph), return zero
		return BigInteger.ZERO;
	}

}
