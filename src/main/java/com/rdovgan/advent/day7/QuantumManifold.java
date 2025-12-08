package com.rdovgan.advent.day7;

import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

/**
 * Part Two (many-worlds) for Day 7.
 *
 * Public API:
 *   public static BigInteger countTimelines(List<String> grid)
 *
 * - Returns the number of distinct timelines produced by a single quantum tachyon particle.
 * - Uses BigInteger because the number of timelines can grow exponentially.
 * - Handles null rows and uneven row lengths (columns outside a row are treated as out-of-bounds / lost).
 */
public final class QuantumManifold {
	private QuantumManifold() {}

	public static BigInteger countTimelines(List<String> grid) {
		if (grid == null || grid.isEmpty()) return BigInteger.ZERO;

		int rows = grid.size();

		// find start S
		int startRow = -1, startCol = -1;
		for (int r = 0; r < rows; r++) {
			String row = grid.get(r);
			if (row == null) continue;
			int idx = row.indexOf('S');
			if (idx >= 0) {
				startRow = r;
				startCol = idx;
				break;
			}
		}
		if (startRow == -1) return BigInteger.ZERO;
		// if S is on the last row -> particle immediately exits (one timeline)
		if (startRow >= rows - 1) return BigInteger.ONE;

		int maxCols = 0;
		for (String s : grid) if (s != null && s.length() > maxCols) maxCols = s.length();
		if (maxCols == 0) return BigInteger.ZERO;

		// current ways arriving to row (indexed by column). Start at row = startRow + 1
		BigInteger[] curr = new BigInteger[maxCols];
		Arrays.fill(curr, BigInteger.ZERO);
		int firstRow = startRow + 1;
		String firstRowStr = grid.get(firstRow);
		if (firstRowStr != null && startCol >= 0 && startCol < firstRowStr.length()) {
			curr[startCol] = BigInteger.ONE;
		} else {
			// beam from S goes into out-of-bounds on first row -> lost => zero timelines
			return BigInteger.ZERO;
		}

		// process row by row
		for (int r = firstRow; r < rows; r++) {
			String rowStr = grid.get(r);
			int rowLen = rowStr == null ? 0 : rowStr.length();

			// localWays holds counts for this row and will be mutated by in-row splitting.
			BigInteger[] localWays = new BigInteger[maxCols];
			for (int c = 0; c < maxCols; c++) localWays[c] = curr[c];

			// queue of columns to process for cascaded splits on this row
			Deque<Integer> q = new ArrayDeque<>();
			boolean[] inQueue = new boolean[maxCols];
			for (int c = 0; c < maxCols; c++) {
				if (localWays[c].signum() != 0) {
					q.add(c);
					inQueue[c] = true;
				}
			}

			while (!q.isEmpty()) {
				int c = q.removeFirst();
				inQueue[c] = false;
				BigInteger ways = localWays[c];
				if (ways.signum() == 0) continue;
				// if this column is out of bounds on this row -> beam is lost here
				if (c < 0 || c >= rowLen) {
					localWays[c] = BigInteger.ZERO;
					continue;
				}
				if (rowStr.charAt(c) == '^') {
					// beam stops here and spawns left and right on the same row
					localWays[c] = BigInteger.ZERO;
					int left = c - 1, right = c + 1;
					if (left >= 0) {
						localWays[left] = localWays[left].add(ways);
						if (!inQueue[left]) { q.add(left); inQueue[left] = true; }
					}
					// right may be >= maxCols; check bounds
					if (right < maxCols) {
						localWays[right] = localWays[right].add(ways);
						if (!inQueue[right]) { q.add(right); inQueue[right] = true; }
					}
				}
				// else: beam continues downward from this column -> leave localWays[c] as is
			}

			// if this is the last row, all remaining localWays represent beams that will exit -> sum them
			if (r == rows - 1) {
				BigInteger total = BigInteger.ZERO;
				for (int c = 0; c < rowLen; c++) {
					if (localWays[c].signum() != 0) total = total.add(localWays[c]);
				}
				return total;
			}

			// prepare curr for next row: beams continue downward from same column,
			// but they are lost if next row is shorter at that column.
			String nextRowStr = grid.get(r + 1);
			int nextLen = nextRowStr == null ? 0 : nextRowStr.length();
			BigInteger[] next = new BigInteger[maxCols];
			for (int c = 0; c < maxCols; c++) next[c] = BigInteger.ZERO;
			for (int c = 0; c < nextLen; c++) {
				if (localWays[c].signum() != 0) next[c] = localWays[c];
			}
			curr = next;
		}

		// unreachable, but return zero for completeness
		return BigInteger.ZERO;
	}
}
