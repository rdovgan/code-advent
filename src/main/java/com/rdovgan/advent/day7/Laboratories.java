package com.rdovgan.advent.day7;

import com.rdovgan.advent.util.ResourceData;

import java.util.List;

public class Laboratories {
	public static void main(String[] args) {
		List<String> data = new ResourceData().loadFromResource("data7.csv");
		System.out.println(TeleporterLab.countBeamSplits(data));
	}
}
