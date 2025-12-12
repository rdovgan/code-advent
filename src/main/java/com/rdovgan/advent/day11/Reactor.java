package com.rdovgan.advent.day11;

import com.rdovgan.advent.util.ResourceData;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Reactor {

	private Reactor() {}

	public static long countPaths(List<String> lines) {
		Map<String, List<String>> graph = new HashMap<>();
		for (String line : lines) {
			if (line == null || line.trim().isEmpty()) continue;
			String[] parts = line.split(":");
			if (parts.length < 2) continue;
			String src = parts[0].trim();
			String[] outs = parts[1].trim().split("\\s+");
			graph.put(src, Arrays.asList(outs));
		}
		Map<String, Long> memo = new HashMap<>();
		return dfs("you", graph, memo);
	}

	private static long dfs(String node, Map<String, List<String>> graph, Map<String, Long> memo) {
		if (node.equals("out")) return 1L;
		if (memo.containsKey(node)) return memo.get(node);
		long sum = 0L;
		List<String> next = graph.getOrDefault(node, Collections.emptyList());
		for (String n : next) {
			sum += dfs(n, graph, memo);
		}
		memo.put(node, sum);
		return sum;
	}

	private static final Pattern PAREN = Pattern.compile("\\((.*?)\\)");
	private static final Pattern CURLY = Pattern.compile("\\{(.*?)\\}");

	public static long totalMinButtonPresses(List<String> lines) {
		if (lines == null || lines.isEmpty()) return 0L;
		long sum = 0L;
		for (String line : lines) {
			if (line == null || line.trim().isEmpty()) continue;
			sum += solveMachineJoltage(line.trim());
		}
		return sum;
	}

	private static int solveMachineJoltage(String line) {
		// parse buttons
		List<int[]> buttons = new ArrayList<>();
		Matcher mp = PAREN.matcher(line);
		while (mp.find()) {
			String inside = mp.group(1).trim();
			if (inside.isEmpty()) {
				buttons.add(new int[0]);
				continue;
			}
			String[] parts = inside.split(",");
			int[] indices = new int[parts.length];
			for (int i = 0; i < parts.length; i++) {
				indices[i] = Integer.parseInt(parts[i].trim());
			}
			buttons.add(indices);
		}

		// parse target counters
		Matcher mc = CURLY.matcher(line);
		int[] target = null;
		if (mc.find()) {
			String[] parts = mc.group(1).split(",");
			target = new int[parts.length];
			for (int i = 0; i < parts.length; i++) target[i] = Integer.parseInt(parts[i].trim());
		}
		if (target == null) return 0;

		int L = target.length;

		Map<String, Integer> dist = new HashMap<>();
		Queue<int[]> queue = new ArrayDeque<>();
		int[] start = new int[L]; // all zeros
		queue.offer(start);
		dist.put(Arrays.toString(start), 0);

		while (!queue.isEmpty()) {
			int[] state = queue.poll();
			int d = dist.get(Arrays.toString(state));
			if (Arrays.equals(state, target)) return d;
			for (int[] btn : buttons) {
				int[] next = state.clone();
				for (int idx : btn) {
					next[idx]++;
				}
				String key = Arrays.toString(next);
				if (!dist.containsKey(key)) {
					dist.put(key, d + 1);
					queue.offer(next);
				}
			}
		}
		return -1;
	}

	public static void main(String[] args) {
		List<String> sample1 = new ResourceData().loadFromResource("data11.csv");
		System.out.println(countPaths(sample1));
	}
}
