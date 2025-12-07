package com.rdovgan.advent.day4;

import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

public class EscalatorUtil {
	public static int countAccessibleRolls(List<String> data) {
		if (CollectionUtils.isEmpty(data)) {
			return 0;
		}
		int size = data.size();
		int[] dx = { -1, -1, -1, 0, 0, 1, 1, 1 }, dy = { -1, 0, 1, -1, 1, -1, 0, 1 };
		int accessible = 0;
		for (int r = 0; r < size; r++) {
			String row = data.get(r);
			if (row == null) {
				continue;
			}
			for (int c = 0, L = row.length(); c < L; c++) {
				if (row.charAt(c) != '@') {
					continue;
				}
				int n = 0;
				for (int k = 0; k < 8; k++) {
					int nr = r + dx[k], nc = c + dy[k];
					if (nr < 0 || nr >= size) {
						continue;
					}
					String rr = data.get(nr);
					if (rr == null || nc < 0 || nc >= rr.length()) {
						continue;
					}
					if (rr.charAt(nc) == '@' && ++n >= 4) {
						break;
					}
				}
				if (n < 4) {
					accessible++;
				}
			}
		}
		return accessible;
	}

}
