package com.rdovgan.advent.day6;

import com.rdovgan.advent.util.ResourceData;

import java.util.List;

public class TrashCompactor {
	public static void main(String[] args) {
		List<String> data = new ResourceData().loadFromResource("data6.csv");
		System.out.println(MathUtil.defineProblems(data));
	}
}
