package com.rdovgan.advent.day9;

import com.rdovgan.advent.util.ResourceData;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;

public final class MovieTheater {

	public static void main(String[] args) {
		List<String> lines = new ResourceData().loadFromResource("data9.csv");
		System.out.println("Part1: " + largestRectangleArea(lines));
		System.out.println("Part2: " + largestRectangleAreaPart2(lines));
	}

	private record Point(int x, int y) {
		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof Point(int x1, int y1))) {
				return false;
			}
			return x == x1 && y == y1;
		}
	}

	public static long largestRectangleArea(List<String> lines) {
		if (lines == null || lines.isEmpty()) {
			return 0L;
		}

		// 2. Use mapMulti for efficient "parse or ignore" logic in a single pass
		Point[] points = lines.stream().filter(Objects::nonNull).map(String::trim).filter(Predicate.not(String::isEmpty)).<Point>mapMulti((line, consumer) -> {
					// Logic: Attempt to parse, push to consumer if valid, ignore if not
					try {
						var parts = line.split(",");
						if (parts.length >= 2) {
							int x = Integer.parseInt(parts[0].trim());
							int y = Integer.parseInt(parts[1].trim());
							consumer.accept(new Point(x, y));
						}
					} catch (NumberFormatException ignored) {
						// In Java 21 (with --enable-preview), this variable could be '_'
					}
				}).distinct() // Deduplicate points automatically based on Record equality
				.toArray(Point[]::new);

		// Optimization: Early exit if we don't have enough points for a pair
		if (points.length < 2) {
			return 0L;
		}

		return calculateMaxArea(points);
	}

	private static long calculateMaxArea(Point[] points) {
		long maxArea = 0L;
		int n = points.length;

		// O(N^2) iteration over all unique pairs
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				// Access record components directly
				maxArea = Math.max(maxArea, areaBetween(points[i], points[j]));
			}
		}
		return maxArea;
	}

	private static long areaBetween(Point p1, Point p2) {
		// Cast to long is critical to prevent Integer Overflow during subtraction
		long width = Math.abs((long) p1.x() - p2.x()) + 1;
		long height = Math.abs((long) p1.y() - p2.y()) + 1;

		// Ignore "thin" lines (1 pixel wide/tall) as per original logic
		if (width <= 1 || height <= 1) {
			return 0L;
		}

		return width * height;
	}

	public static long largestRectangleAreaPart2(List<String> lines) {
		if (lines == null || lines.isEmpty()) {
			return 0L;
		}

		// 1. Parse Inputs
		List<Point> orderedPoints = parsePoints(lines);
		Set<Point> redPoints = new LinkedHashSet<>(orderedPoints); // Preserve insertion order

		if (redPoints.size() < 2) {
			return 0L;
		}

		// 2. Extract the polygon loop
		List<Point> loop = extractLoop(orderedPoints, redPoints);
		if (loop.size() < 2) {
			return 0L;
		}

		// 3. Initialize Coordinate Compression Grid
		//    This maps the large integer coordinates into a compressed grid indices.
		var grid = new RegionGrid(redPoints, loop);

		// 4. Mark boundaries (edges of the loop and specific points)
		grid.markBoundaries(loop, redPoints);

		// 5. Flood fill from outside to determine the "interior"
		grid.fillExteriorAndInvert();

		// 6. Build 2D Prefix Sums to allow O(1) area queries
		grid.buildPrefixSums();

		// 7. Find Max Area
		//    Iterate all pairs of points. If the rectangle formed by them is
		//    fully contained within the allowed region, calculate area.
		return findMaxContainedArea(redPoints, grid);
	}

	private static long findMaxContainedArea(Set<Point> redPoints, RegionGrid grid) {
		Point[] pts = redPoints.toArray(Point[]::new);
		long maxArea = 0L;

		for (int i = 0; i < pts.length; i++) {
			Point p1 = pts[i];
			for (int j = i + 1; j < pts.length; j++) {
				Point p2 = pts[j];

				long width = Math.abs((long) p1.x() - p2.x()) + 1;
				long height = Math.abs((long) p1.y() - p2.y()) + 1;

				if (width <= 1 || height <= 1) {
					continue;
				}

				long geometricArea = width * height;
				long allowedArea = grid.calculateAllowedArea(p1, p2);

				// If the area calculated from the grid (which only counts "true" cells)
				// matches the geometric area, the rectangle is valid (fully enclosed).
				if (allowedArea == geometricArea) {
					maxArea = Math.max(maxArea, geometricArea);
				}
			}
		}
		return maxArea;
	}

	private static List<Point> parsePoints(List<String> lines) {
		var points = new ArrayList<Point>();
		for (String line : lines) {
			if (line == null)
				continue;
			String s = line.trim();
			if (s.isEmpty())
				continue;

			String[] parts = s.split(",");
			if (parts.length >= 2) {
				try {
					int x = Integer.parseInt(parts[0].trim());
					int y = Integer.parseInt(parts[1].trim());
					points.add(new Point(x, y));
				} catch (NumberFormatException ignored) {
				}
			}
		}
		return points;
	}

	private static List<Point> extractLoop(List<Point> ordered, Set<Point> redSet) {
		var loop = new ArrayList<Point>();
		for (Point p : ordered) {
			// Logic from original: Filter points that exist in redSet
			if (redSet.contains(p)) {
				// Deduplicate consecutive points
				if (loop.isEmpty() || !loop.getLast().equals(p)) {
					loop.add(p);
				}
			}
		}
		// Remove closing point if it duplicates the start (form a clean open chain)
		if (!loop.isEmpty() && loop.getFirst().equals(loop.getLast())) {
			loop.removeLast();
		}
		return loop;
	}

	/**
	 * Handles Coordinate Compression, Grid Painting, Flood Fill, and Prefix Sums.
	 */
	private static class RegionGrid {
		private final int[] xs; // Sorted unique X coordinates
		private final int[] ys; // Sorted unique Y coordinates
		private final boolean[][] allowed; // The grid: true = inside/boundary, false = outside
		private final long[][] prefixSums; // 2D prefix sums of real-world areas
		private final int width;
		private final int height;

		public RegionGrid(Set<Point> points, List<Point> loop) {
			// Collect all interesting coordinates for compression
			TreeSet<Integer> xSet = new TreeSet<>();
			TreeSet<Integer> ySet = new TreeSet<>();

			// Add points and their neighbors to handle line thickness and boundaries
			for (Point p : points) {
				addPadding(xSet, p.x());
				addPadding(ySet, p.y());
			}

			int m = loop.size();
			for (int i = 0; i < m; i++) {
				Point a = loop.get(i);
				Point b = loop.get((i + 1) % m);

				// Add boundaries defined by loop segments
				if (a.x() == b.x()) {
					addPadding(xSet, a.x());
					ySet.add(Math.min(a.y(), b.y()));
					ySet.add(Math.max(a.y(), b.y()) + 1);
				} else {
					addPadding(ySet, a.y());
					xSet.add(Math.min(a.x(), b.x()));
					xSet.add(Math.max(a.x(), b.x()) + 1);
				}
			}

			// Add outer boundary buffer
			xSet.add(xSet.getFirst() - 1);
			xSet.add(xSet.getLast() + 1);
			ySet.add(ySet.getFirst() - 1);
			ySet.add(ySet.getLast() + 1);

			this.xs = xSet.stream().mapToInt(Integer::intValue).toArray();
			this.ys = ySet.stream().mapToInt(Integer::intValue).toArray();
			this.width = xs.length - 1;
			this.height = ys.length - 1;
			this.allowed = new boolean[height][width];
			this.prefixSums = new long[height + 1][width + 1];
		}

		private void addPadding(TreeSet<Integer> set, int val) {
			set.add(val);
			set.add(val + 1);
		}

		public void markBoundaries(List<Point> loop, Set<Point> redPts) {
			// Mark individual points
			for (Point p : redPts) {
				int cx = findIndex(xs, p.x());
				int cy = findIndex(ys, p.y());
				if (isValidIndex(cx, cy)) {
					allowed[cy][cx] = true;
				}
			}

			// Mark loop segments
			int m = loop.size();
			for (int i = 0; i < m; i++) {
				Point a = loop.get(i);
				Point b = loop.get((i + 1) % m);

				if (a.x() == b.x()) { // Vertical
					int y1 = Math.min(a.y(), b.y());
					int y2 = Math.max(a.y(), b.y());
					markRectInGrid(a.x(), a.x(), y1, y2);
				} else if (a.y() == b.y()) { // Horizontal
					int x1 = Math.min(a.x(), b.x());
					int x2 = Math.max(a.x(), b.x());
					markRectInGrid(x1, x2, a.y(), a.y());
				} else { // Diagonal (treat as two points)
					markRectInGrid(a.x(), a.x(), a.y(), a.y());
					markRectInGrid(b.x(), b.x(), b.y(), b.y());
				}
			}
		}

		/**
		 * Marks a rectangular area in the compressed grid as allowed.
		 */
		private void markRectInGrid(int x1, int x2, int y1, int y2) {
			int cxStart = findIndex(xs, x1);
			int cxEnd = findIndex(xs, x2 + 1) - 1;
			int cyStart = findIndex(ys, y1);
			int cyEnd = findIndex(ys, y2 + 1) - 1;

			if (cxStart < 0)
				cxStart = 0;
			if (cyStart < 0)
				cyStart = 0;
			if (cxEnd >= width)
				cxEnd = width - 1;
			if (cyEnd >= height)
				cyEnd = height - 1;

			for (int cy = cyStart; cy <= cyEnd; cy++) {
				// Optimization: Check row bounds before inner loop
				if (ys[cy + 1] - 1 < y1 || ys[cy] > y2)
					continue;

				for (int cx = cxStart; cx <= cxEnd; cx++) {
					if (xs[cx + 1] - 1 < x1 || xs[cx] > x2)
						continue;
					allowed[cy][cx] = true;
				}
			}
		}

		public void fillExteriorAndInvert() {
			boolean[][] visited = new boolean[height][width];
			Deque<int[]> queue = new ArrayDeque<>();

			// Initialize queue with all border cells that aren't already allowed (walls)
			for (int x = 0; x < width; x++) {
				addIfOpen(x, 0, visited, queue);
				addIfOpen(x, height - 1, visited, queue);
			}
			for (int y = 0; y < height; y++) {
				addIfOpen(0, y, visited, queue);
				addIfOpen(width - 1, y, visited, queue);
			}

			// BFS
			int[] dx = { 1, -1, 0, 0 };
			int[] dy = { 0, 0, 1, -1 };

			while (!queue.isEmpty()) {
				int[] cell = queue.poll();
				int cx = cell[0];
				int cy = cell[1];

				for (int k = 0; k < 4; k++) {
					int nx = cx + dx[k];
					int ny = cy + dy[k];

					if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
						if (!visited[ny][nx] && !allowed[ny][nx]) {
							visited[ny][nx] = true;
							queue.add(new int[] { nx, ny });
						}
					}
				}
			}

			// Invert logic: If not visited by flood fill, it's inside (or wall).
			// allowed becomes true for Interior + Walls.
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					if (!visited[y][x]) {
						allowed[y][x] = true;
					}
				}
			}
		}

		private void addIfOpen(int x, int y, boolean[][] visited, Deque<int[]> q) {
			if (!allowed[y][x] && !visited[y][x]) {
				visited[y][x] = true;
				q.add(new int[] { x, y });
			}
		}

		public void buildPrefixSums() {
			for (int y = 0; y < height; y++) {
				long rowSum = 0L;
				for (int x = 0; x < width; x++) {
					if (allowed[y][x]) {
						long w = (long) xs[x + 1] - xs[x];
						long h = (long) ys[y + 1] - ys[y];
						rowSum += w * h;
					}
					prefixSums[y + 1][x + 1] = prefixSums[y][x + 1] + rowSum;
				}
			}
		}

		public long calculateAllowedArea(Point p1, Point p2) {
			int x1 = Math.min(p1.x(), p2.x());
			int x2 = Math.max(p1.x(), p2.x());
			int y1 = Math.min(p1.y(), p2.y());
			int y2 = Math.max(p1.y(), p2.y());

			// Convert real coords to grid indices
			int col1 = findIndex(xs, x1);
			int col2 = findIndex(xs, x2 + 1) - 1;
			int row1 = findIndex(ys, y1);
			int row2 = findIndex(ys, y2 + 1) - 1;

			if (col1 > col2 || row1 > row2)
				return 0L;
			if (!isValidIndex(col1, row1) || !isValidIndex(col2, row2))
				return 0L;

			// standard 2D prefix sum inclusion-exclusion
			return prefixSums[row2 + 1][col2 + 1] - prefixSums[row1][col2 + 1] - prefixSums[row2 + 1][col1] + prefixSums[row1][col1];
		}

		private int findIndex(int[] arr, int value) {
			int idx = Arrays.binarySearch(arr, value);
			if (idx < 0) {
				idx = -idx - 1; // Insertion point
				idx--; // Move to the interval containing the value
			}
			// If value is exactly an edge, verify strictly inside logic
			// (The original logic allowed tight fitting, so this standard lower-bound is usually correct
			// combined with the +1 logic in range calculation).
			return idx;
		}

		private boolean isValidIndex(int x, int y) {
			return x >= 0 && x < width && y >= 0 && y < height;
		}
	}

}
