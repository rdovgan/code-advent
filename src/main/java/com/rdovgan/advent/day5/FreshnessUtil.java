package com.rdovgan.advent.day5;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class FreshnessUtil {

	record Range(long start, long end) {
		boolean contains(long id) {
			return id >= start && id < end;
		}
	}

	public static Long defineFreshCount(List<String> data) {
		List<Range> ranges = defineFreshRanges(data);
		List<Long> idsToCheck = defineIdsToCheck(data);
		return idsToCheck.stream().filter(id -> ranges.stream().anyMatch(range -> range.contains(id))).count();
	}

	private static List<Long> defineIdsToCheck(List<String> data) {
		return data.stream().filter(record -> !StringUtils.contains(record, '-')).map(Long::parseLong).toList();
	}

	private static List<Range> defineFreshRanges(List<String> data) {
		return data.stream().filter(record -> StringUtils.contains(record, '-')).map(range -> {
			int idx = range.indexOf('-');
			return new Range(Long.parseLong(range.substring(0, idx)), Long.parseLong(range.substring(idx + 1)));
		}).toList();
	}

}
