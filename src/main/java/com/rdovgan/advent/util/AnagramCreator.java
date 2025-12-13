package com.rdovgan.advent.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class AnagramCreator {

	private final ResourceData resourceData;
	private final Random random;

	public AnagramCreator() {
		this.resourceData = new ResourceData();
		this.random = new Random();
	}

	public AnagramCreator(long seed) {
		this.resourceData = new ResourceData();
		this.random = new Random(seed);
	}

	/**
	 * Shuffles a single word to create an anagram.
	 * @param word the original word
	 *
	 * @return shuffled version of the word
	 */
	public String shuffleWord(String word) {
		if (word == null || word.length() <= 1) {
			return word;
		}

		List<Character> chars = new ArrayList<>();
		for (char c : word.toCharArray()) {
			chars.add(c);
		}

		Collections.shuffle(chars, random);

		StringBuilder shuffled = new StringBuilder(chars.size());
		for (char c : chars) {
			shuffled.append(c);
		}

		return shuffled.toString();
	}

	/**
	 * Creates anagrams for all words from a resource file.
	 *
	 * @return list of shuffled words
	 */
	public List<String> createAnagrams() {
		List<String> lines = resourceData.loadFromResource("words.csv");
		List<String> anagrams = new ArrayList<>();

		for (String line : lines) {
			anagrams.add(Arrays.stream(line.split(" ")).map(this::shuffleWord).collect(Collectors.joining(" ")));
		}

		return anagrams;
	}

	/**
	 * Creates anagrams from a resource file and writes them to an output file.
	 */
	public void createAnagramsToFile() {
		List<String> anagrams = createAnagrams();
		resourceData.writeToResource("anagram.csv", anagrams);
	}

	/**
	 * Main method for quick testing and execution.
	 * Usage: java AnagramCreator <resourcePath> <outputPath>
	 */
	public static void main(String[] args) {
		AnagramCreator creator = new AnagramCreator();
		creator.createAnagramsToFile();
	}
}
