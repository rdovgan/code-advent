package com.rdovgan.advent.day8;

import org.apache.commons.lang3.math.NumberUtils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpaceUtil {

	private static final Comparator<Pair> PAIR_COMPARATOR = Comparator.comparingLong((Pair p) -> p.dist).thenComparingInt(p -> p.a).thenComparingInt(p -> p.b);

	public static long distanceSq(Point one, Point two) {
		long dx = (long) one.x() - two.x();
		long dy = (long) one.y() - two.y();
		long dz = (long) one.z() - two.z();
		return dx * dx + dy * dy + dz * dz;
	}

	public static List<Point> definePoints(List<String> lines) {
		if (lines == null) {
			return Collections.emptyList();
		}
		List<Point> pts = new ArrayList<>(lines.size());
		for (String line : lines) {
			if (line == null) {
				continue;
			}
			String[] parts = line.trim().split(",");
			if (parts.length < 3) {
				continue;
			}
			Integer x = NumberUtils.createInteger(parts[0].trim());
			Integer y = NumberUtils.createInteger(parts[1].trim());
			Integer z = NumberUtils.createInteger(parts[2].trim());
			if (x == null || y == null || z == null) {
				continue;
			}
			pts.add(new Point(x, y, z));
		}
		return pts;
	}

	public static List<Pair> findClosestPairs(List<Point> points, int count) {
		if (points == null || points.size() < 2 || count <= 0) {
			return Collections.emptyList();
		}

		List<Pair> edges = buildAndSortAllEdges(points);
		int limit = Math.min(count, edges.size());
		return new ArrayList<>(edges.subList(0, limit));
	}

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

	public static BigInteger multiplyTop3ByConnectingKPairs(List<Point> points, int count) {
		if (points == null || points.isEmpty()) {
			return BigInteger.ZERO;
		}
		List<Pair> pairs = findClosestPairs(points, count);
		int n = points.size();
		DSU dsu = new DSU(n);
		for (Pair p : pairs) {
			dsu.union(p.a, p.b);
		}
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
	}

	private static final class DSU {
		private final int[] parent;
		private final int[] rank;

		DSU(int n) {
			parent = new int[n];
			rank = new int[n];
			for (int i = 0; i < n; i++)
				parent[i] = i;
		}

		int find(int point) {
			while (parent[point] != point) {
				parent[point] = parent[parent[point]];
				point = parent[point];
			}
			return point;
		}

		void union(int x, int y) {
			int rootX = find(x);
			int rootY = find(y);
			if (rootX == rootY)
				return;
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

	public static BigInteger productOfXCoordinatesOfLastConnection(List<Point> points) {
		if (points == null || points.size() < 2) {
			return BigInteger.ZERO;
		}
		List<Pair> edges = buildAndSortAllEdges(points);
		DSU dsu = new DSU(points.size());
		int components = points.size();

		for (Pair e : edges) {
			int a = e.a;
			int b = e.b;
			int ra = dsu.find(a);
			int rb = dsu.find(b);
			if (ra == rb) {
				continue;
			}
			dsu.union(ra, rb);
			components--;
			if (components == 1) {
				long xa = points.get(a).x();
				long xb = points.get(b).x();
				return BigInteger.valueOf(xa).multiply(BigInteger.valueOf(xb));
			}
		}
		return BigInteger.ZERO;
	}

}
