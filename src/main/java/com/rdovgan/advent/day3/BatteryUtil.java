package com.rdovgan.advent.day3;

import java.util.List;
import java.util.stream.IntStream;

public class BatteryUtil {

	public static Integer defineMaxJoltage(String battery) {
		if (battery == null || battery.length() < 2)
			return null;

		return IntStream.range(0, battery.length() - 1)
				.flatMap(i -> IntStream.range(i + 1, battery.length()).map(j -> (battery.charAt(i) - '0') * 10 + (battery.charAt(j) - '0')))
				.max().stream().boxed().findFirst().orElse(null);
	}

	public static Long defineSumOfAllJoltages(List<String> batteryData) {
		return batteryData.stream().mapToLong(BatteryUtil::defineMaxJoltage).sum();
	}

}
