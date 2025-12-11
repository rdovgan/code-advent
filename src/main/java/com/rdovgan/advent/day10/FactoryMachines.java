package com.rdovgan.advent.day10;

import com.rdovgan.advent.util.ResourceData;

import java.util.*;

public final class FactoryMachines {

	public static void main(String[] args) {
		var lines = new ResourceData().loadFromResource("data10.csv");
		System.out.println("Part1: " + solvePart1(lines));
	}

	private static long solvePart1(List<String> lines) {
		long totalPresses = 0;
		for (var line : lines) {
			if (line != null && !line.trim().isEmpty()) {
				totalPresses += solveMachine(line.trim());
			}
		}
		return totalPresses;
	}

	private static int solveMachine(String line) {
		var machineData = parseMachineLine(line);
		return solveLinearSystem(machineData.target, machineData.buttons);
	}

	private record MachineData(boolean[] target, List<int[]> buttons) {
	}

	private static MachineData parseMachineLine(String line) {
		int bracketStart = line.indexOf('[');
		int bracketEnd = line.indexOf(']');
		var targetStr = line.substring(bracketStart + 1, bracketEnd);
		var target = new boolean[targetStr.length()];
		for (int i = 0; i < targetStr.length(); i++) {
			target[i] = targetStr.charAt(i) == '#';
		}

		var buttons = new ArrayList<int[]>();
		int pos = bracketEnd + 1;
		while (pos < line.length()) {
			if (line.charAt(pos) == '(') {
				int closePos = line.indexOf(')', pos);
				var buttonStr = line.substring(pos + 1, closePos);
				var buttonIndices = parseButtonIndices(buttonStr);
				buttons.add(buttonIndices);
				pos = closePos + 1;
			} else if (line.charAt(pos) == '{') {
				break;
			} else {
				pos++;
			}
		}

		return new MachineData(target, buttons);
	}

	private static int[] parseButtonIndices(String s) {
		var parts = s.split(",");
		var result = new int[parts.length];
		for (int i = 0; i < parts.length; i++) {
			result[i] = Integer.parseInt(parts[i].trim());
		}
		return result;
	}

	private static int solveLinearSystem(boolean[] target, List<int[]> buttons) {
		int numLights = target.length;
		int numButtons = buttons.size();

		var matrix = new int[numLights][numButtons + 1];

		for (int light = 0; light < numLights; light++) {
			for (int button = 0; button < numButtons; button++) {
				var buttonLights = buttons.get(button);
				boolean togglesThisLight = false;
				for (int l : buttonLights) {
					if (l == light) {
						togglesThisLight = true;
						break;
					}
				}
				matrix[light][button] = togglesThisLight ? 1 : 0;
			}
			matrix[light][numButtons] = target[light] ? 1 : 0;
		}

		var pivotCols = gaussianEliminationGF2(matrix, numLights, numButtons);

		var freeVars = new ArrayList<Integer>();
		for (int col = 0; col < numButtons; col++) {
			if (!pivotCols.contains(col)) {
				freeVars.add(col);
			}
		}

		int minPresses = Integer.MAX_VALUE;
		int numFree = freeVars.size();
		int combinations = 1 << numFree;

		for (int mask = 0; mask < combinations; mask++) {
			var solution = new int[numButtons];

			for (int i = 0; i < numFree; i++) {
				solution[freeVars.get(i)] = (mask >> i) & 1;
			}

			boolean valid = backSubstituteWithFreeVars(matrix, numLights, numButtons, pivotCols, solution);

			if (valid) {
				int presses = 0;
				for (int press : solution) {
					presses += press;
				}
				minPresses = Math.min(minPresses, presses);
			}
		}

		return minPresses == Integer.MAX_VALUE ? 0 : minPresses;
	}

	private static Set<Integer> gaussianEliminationGF2(int[][] matrix, int rows, int cols) {
		var pivotCols = new LinkedHashSet<Integer>();
		int rank = 0;

		for (int col = 0; col < cols && rank < rows; col++) {
			int pivotRow = -1;
			for (int row = rank; row < rows; row++) {
				if (matrix[row][col] == 1) {
					pivotRow = row;
					break;
				}
			}

			if (pivotRow == -1) {
				continue;
			}

			var temp = matrix[rank];
			matrix[rank] = matrix[pivotRow];
			matrix[pivotRow] = temp;

			for (int row = 0; row < rows; row++) {
				if (row != rank && matrix[row][col] == 1) {
					for (int c = 0; c <= cols; c++) {
						matrix[row][c] ^= matrix[rank][c];
					}
				}
			}

			pivotCols.add(col);
			rank++;
		}

		return pivotCols;
	}

	private static boolean backSubstituteWithFreeVars(int[][] matrix, int rows, int cols,
													  Set<Integer> pivotCols, int[] solution) {
		for (int row = rows - 1; row >= 0; row--) {
			int pivotCol = -1;
			for (int col = 0; col < cols; col++) {
				if (matrix[row][col] == 1 && pivotCols.contains(col)) {
					pivotCol = col;
					break;
				}
			}

			if (pivotCol == -1) {
				int check = matrix[row][cols];
				for (int col = 0; col < cols; col++) {
					if (matrix[row][col] == 1) {
						check ^= solution[col];
					}
				}
				if (check != 0) {
					return false;
				}
				continue;
			}

			int val = matrix[row][cols];
			for (int col = pivotCol + 1; col < cols; col++) {
				if (matrix[row][col] == 1) {
					val ^= solution[col];
				}
			}
			solution[pivotCol] = val;
		}

		return true;
	}

}
