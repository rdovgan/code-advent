package com.rdovgan.advent.day3;

import java.util.List;

public class BatteryUtil {

	public static Long defineMaxJoltage(String battery) {
		if (battery == null || battery.isEmpty()) {
			return null;
		}
		var result = new StringBuilder();
		int start = 0, take = Math.min(12, battery.length());

		for (int i = 0; i < take; i++) {
			int end = battery.length() - (take - i - 1);
			int maxIdx = start;

			for (int j = start + 1; j < end; j++) {
				if (battery.charAt(j) > battery.charAt(maxIdx)) {
					maxIdx = j;
				}
			}
			result.append(battery.charAt(maxIdx));
			start = maxIdx + 1;
		}

		return Long.parseLong(result.toString());
	}

	public static Long defineSumOfAllJoltages(List<String> batteryData) {
		return batteryData.stream().mapToLong(BatteryUtil::defineMaxJoltage).sum();
	}

}
