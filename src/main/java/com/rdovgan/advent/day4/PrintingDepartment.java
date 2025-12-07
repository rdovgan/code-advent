package com.rdovgan.advent.day4;

import com.rdovgan.advent.util.ResourceData;

import java.util.List;

public class PrintingDepartment {

	public static void main(String[] args) {
		List<String> data = new ResourceData().loadFromResource("data4.csv");
		System.out.println(EscalatorUtil.countAccessibleRolls(data));
	}

}
