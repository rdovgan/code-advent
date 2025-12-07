package com.rdovgan.advent.day6;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

public class MathUtil {

	public static long defineProblems(List<String> data) {
		var problemActions = StringUtils.split(data.getLast(), ' ');
		long[][] matrix = data.stream().limit(data.size() - 1).map(line -> Arrays.stream(StringUtils.split(line, ' ')).mapToLong(Long::parseLong).toArray())
				.toArray(long[][]::new);
		long[][] numbers = IntStream.range(0, matrix[0].length).mapToObj(col -> Arrays.stream(matrix).mapToLong(longs -> longs[col]).toArray())
				.toArray(long[][]::new);
		long result = 0;
		for (int i = 0; i < problemActions.length; i++) {
			if (Objects.equals(problemActions[i], "+")) {
				result += Arrays.stream(numbers[i]).sum();
			}
			if (Objects.equals(problemActions[i], "*")) {
				result += Arrays.stream(numbers[i]).reduce(1L, (a, b) -> a * b);
			}
		}
		return result;
	}

}
