package com.rdovgan.advent.day3;

import com.rdovgan.advent.util.ResourceData;

import java.util.List;

public class Lobby {

	public static void main(String[] args) {
		List<String> data = new ResourceData().loadFromResource("data3.csv");
		System.out.println(BatteryUtil.defineSumOfAllJoltages(data));
	}

}
