package com.rdovgan.advent.day6;

import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class CephalopodsUtil {

	public static Long calculate(List<String> lines) {
		long result = 0L;
		char[][] data = convertToCharArrays(lines);
		int lastColumnAction;
		int currentColumnAction = data[0].length;
		while (currentColumnAction > 0) {
			lastColumnAction = currentColumnAction;
			currentColumnAction = defineNextColumn(data, lastColumnAction);
			char[][] problem = defineProblem(data, currentColumnAction, lastColumnAction);
			List<Long> problemNumbers = defineProblemNumbers(problem);
			if (problem[problem.length - 1][0] == '+') {
				result += problemNumbers.stream().mapToLong(Long::longValue).sum();
			} else {
				result += problemNumbers.stream().reduce(1L, (a, b) -> a * b);
			}
		}
		return result;
	}

	public static char[][] convertToCharArrays(List<String> data) {
		return data.stream().map(String::toCharArray).toArray(char[][]::new);
	}

	public static int defineNextColumn(char[][] data, int previousColumn) {
		char[] lastRow = data[data.length - 1];
		int maxIndex = Math.min(previousColumn - 1, lastRow.length - 1);
		return IntStream.rangeClosed(0, maxIndex).map(i -> maxIndex - i).filter(raw -> lastRow[raw] != ' ').findFirst().orElse(0);
	}

	private static char[][] defineProblem(char[][] data, int firstPosition, int lastPosition) {
		return Arrays.stream(data).map(row -> Arrays.copyOfRange(row, firstPosition, lastPosition)).toArray(char[][]::new);
	}

	private static List<Long> defineProblemNumbers(char[][] problem) {
		List<Long> problemNumbers = new ArrayList<>();
		for (int col = problem[0].length - 1; col >= 0; col--) {
			StringBuilder number = new StringBuilder();
			for (int row = 0; row < problem.length - 1; row++) {
				number.append(problem[row][col]);
			}
			try {
				problemNumbers.add(NumberUtils.createLong(number.toString().trim()));
			} catch (Exception _) {
			}
		}
		return problemNumbers;
	}

}
