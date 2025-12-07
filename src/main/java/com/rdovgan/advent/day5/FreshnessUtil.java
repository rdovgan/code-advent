package com.rdovgan.advent.day5;

import org.apache.commons.lang3.StringUtils;

import java.util.Comparator;
import java.util.List;

public class FreshnessUtil {

	record Range(long start, long end) {
		boolean contains(long id) {
			return id >= start && id < end;
		}

		Long defineTotal() {
			return end - start + 1;
		}
	}

	public static Long defineTotalFreshCount(List<String> data) {
		List<Range> ranges = defineFreshRanges(data);
		ranges = defineIntersections(ranges);
		return ranges.stream().mapToLong(Range::defineTotal).sum();
	}

	private static List<Range> defineIntersections(List<Range> ranges) {
		if (ranges.isEmpty()) {
			return ranges;
		}

		var sorted = ranges.stream().sorted(Comparator.comparingLong(a -> a.start)).toList();
		var merged = new java.util.ArrayList<Range>();

		Range current = sorted.getFirst();

		for (int i = 1; i < sorted.size(); i++) {
			Range next = sorted.get(i);
			if (next.start <= current.end) {
				current = new Range(current.start, Math.max(current.end, next.end));
			} else {
				merged.add(current);
				current = next;
			}
		}
		merged.add(current);

		return merged;
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
