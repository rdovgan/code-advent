package com.rdovgan.advent.day5;

import com.rdovgan.advent.util.ResourceData;

import java.util.List;

public class Cafeteria {
	public static void main(String[] args) {
		List<String> data = new ResourceData().loadFromResource("data5.csv");
		System.out.println(FreshnessUtil.defineFreshCount(data));
	}
}
