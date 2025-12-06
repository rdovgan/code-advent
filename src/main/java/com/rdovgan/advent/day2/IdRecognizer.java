package com.rdovgan.advent.day2;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.List;
import java.util.stream.LongStream;

public class IdRecognizer {

	public static List<Long> fullDefineIncorrectIds(String data) {
		List<String> ranges = defineRanges(data);
		return ranges.stream().flatMap(range -> defineIncorrectIdsFromRange(range).stream()).toList();
	}

	public static List<Long> defineIncorrectIdsFromRange(String range) {
		List<String> rangeInStrings = List.of(StringUtils.split(range, '-'));
		if (CollectionUtils.size(rangeInStrings) != 2) {
			return List.of();
		}
		Long firstId = NumberUtils.createLong(rangeInStrings.getFirst());
		Long lastId = NumberUtils.createLong(rangeInStrings.getLast());
		return defineIncorrectIds(firstId, lastId);
	}

	public static List<String> defineRanges(String data) {
		return List.of(StringUtils.split(data, ','));
	}

	public static List<Long> defineIncorrectIds(Long firstId, Long lastId) {
		if (firstId == null || lastId == null || firstId > lastId) {
			return List.of();
		}
		var result = LongStream.rangeClosed(firstId, lastId).boxed().filter(IdRecognizer::isIncorrectId).toList();
		System.out.println(result);
		return result;
	}

	public static boolean isIncorrectId(long id) {
		String s = Long.toString(id);
		if (s.length() < 2) {
			return false;
		}
		return (s + s).indexOf(s, 1) != s.length();
	}

}
