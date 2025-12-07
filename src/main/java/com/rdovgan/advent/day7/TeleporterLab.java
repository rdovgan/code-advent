package com.rdovgan.advent.day7;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public final class TeleporterLab {
	private TeleporterLab() {
	}

	public static int countBeamSplits(List<String> grid) {
		if (grid == null || grid.isEmpty()) {
			return 0;
		}

		StartPosition startPos = findStartPosition(grid);
		if (startPos == null) {
			return 0;
		}

		return simulateBeams(grid, startPos);
	}

	private static StartPosition findStartPosition(List<String> grid) {
		for (int row = 0; row < grid.size(); row++) {
			String rowStr = grid.get(row);
			if (rowStr == null) {
				continue;
			}
			int col = rowStr.indexOf('S');
			if (col >= 0) {
				return new StartPosition(row, col);
			}
		}
		return null;
	}

	private static int simulateBeams(List<String> grid, StartPosition start) {
		int totalSplits = 0;
		Set<Integer> activeColumns = new HashSet<>();
		activeColumns.add(start.col);

		for (int row = start.row + 1; row < grid.size(); row++) {
			if (activeColumns.isEmpty()) {
				break;
			}

			RowResult result = processRow(grid.get(row), activeColumns);
			totalSplits += result.splits;
			activeColumns = result.nextColumns;
		}

		return totalSplits;
	}

	private static RowResult processRow(String rowStr, Set<Integer> incomingColumns) {
		Queue<Integer> beamQueue = new ArrayDeque<>(incomingColumns);
		Set<Integer> processed = new HashSet<>();
		Set<Integer> continuingBeams = new HashSet<>();
		int splits = 0;

		while (!beamQueue.isEmpty()) {
			int col = beamQueue.poll();
			if (!processed.add(col)) {
				continue;
			}

			BeamAction action = determineBeamAction(rowStr, col);
			if (action == BeamAction.SPLIT) {
				splits++;
				enqueueAdjacentBeams(beamQueue, col);
			} else if (action == BeamAction.CONTINUE) {
				continuingBeams.add(col);
			}
		}

		return new RowResult(splits, continuingBeams);
	}

	private static BeamAction determineBeamAction(String rowStr, int col) {
		if (rowStr == null || col < 0 || col >= rowStr.length()) {
			return BeamAction.OUT_OF_BOUNDS;
		}
		return rowStr.charAt(col) == '^' ? BeamAction.SPLIT : BeamAction.CONTINUE;
	}

	private static void enqueueAdjacentBeams(Queue<Integer> queue, int col) {
		int left = col - 1;
		int right = col + 1;
		if (left >= 0) {
			queue.add(left);
		}
		queue.add(right);
	}

	private record StartPosition(int row, int col) {
	}

	private record RowResult(int splits, Set<Integer> nextColumns) {
	}

	private enum BeamAction {
		SPLIT, CONTINUE, OUT_OF_BOUNDS
	}

}
