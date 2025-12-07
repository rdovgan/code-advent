package com.rdovgan.advent.day4;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

public class EscalatorUtil {
	public static int countAccessibleRolls(List<String> data) {
		if (CollectionUtils.isEmpty(data)) {
			return 0;
		}

		int rows = data.size();
		// convert to modifiable char arrays (handle null / uneven row lengths)
		List<char[]> grid = new ArrayList<>(rows);
		for (String row : data) {
			grid.add(row == null ? new char[0] : row.toCharArray());
		}

		int totalRemoved = 0;
		int[] dx = { -1, -1, -1, 0, 0, 1, 1, 1 };
		int[] dy = { -1, 0, 1, -1, 1, -1, 0, 1 };

		for (;;) {
			List<int[]> toRemove = new ArrayList<>();

			for (int r = 0; r < rows; r++) {
				char[] row = grid.get(r);
				if (row == null || row.length == 0) continue;
				for (int c = 0; c < row.length; c++) {
					if (row[c] != '@') continue;

					int neighbors = 0;
					for (int k = 0; k < 8; k++) {
						int nr = r + dx[k], nc = c + dy[k];
						if (nr < 0 || nr >= rows) continue;
						char[] nrRow = grid.get(nr);
						if (nrRow == null || nc < 0 || nc >= nrRow.length) continue;
						if (nrRow[nc] == '@' && ++neighbors >= 4) break;
					}
					if (neighbors < 4) toRemove.add(new int[] { r, c });
				}
			}

			if (toRemove.isEmpty()) break;

			// remove all found rolls simultaneously
			for (int[] p : toRemove) {
				int r = p[0], c = p[1];
				grid.get(r)[c] = '.';
			}
			totalRemoved += toRemove.size();
		}

		return totalRemoved;
	}
}
