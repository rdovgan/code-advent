package com.rdovgan.advent.day9;

import com.rdovgan.advent.util.ResourceData;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class MovieTheater {

	public static void main(String[] args) {
		List<String> lines = new ResourceData().loadFromResource("data9.csv");
		System.out.println(largestRectangleArea(lines));
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
		if (lines == null || lines.isEmpty())
			return 0L;

		Set<Point> redTiles = new HashSet<>(lines.size());
		for (String line : lines) {
			if (line == null) {
				continue;
			}
			String s = line.trim();
			if (s.isEmpty()) {
				continue;
			}
			String[] parts = s.split(",");
			if (parts.length < 2)
				continue;
			try {
				int x = Integer.parseInt(parts[0].trim());
				int y = Integer.parseInt(parts[1].trim());
				redTiles.add(new Point(x, y));
			} catch (NumberFormatException ignored) {
			}
		}

		Point[] pts = redTiles.toArray(new Point[0]);
		int n = pts.length;
		if (n < 2) {
			return 0L;
		}

		long maxArea = 0L;
		// Check all pairs of red tiles as opposite corners
		for (int i = 0; i < n; i++) {
			Point p1 = pts[i];
			for (int j = i + 1; j < n; j++) {
				Point p2 = pts[j];

				// Calculate rectangle dimensions (inclusive)
				// From index 0 to 3 = 4 cells (0,1,2,3)
				long width = Math.abs((long) p1.x - p2.x) + 1;
				long height = Math.abs((long) p1.y - p2.y) + 1;

				// Skip if they're on the same row or column (no rectangle formed)
				if (width == 1L || height == 1L) {
					continue;
				}

				long area = width * height;
				maxArea = Math.max(maxArea, area);
			}
		}
		return maxArea;
	}
}
