package com.rdovgan.advent.day1;

import com.rdovgan.advent.util.ResourceData;

import java.util.List;

public class SecretEntrance {

	public static void main(String[] args) {
		List<String> rotations = new ResourceData().loadFromResource("data1.csv");
		Password password = new Password();
		rotations.forEach(rotation -> RotationUtil.rotate(password, rotation));
		System.out.println(password.getPassword());
	}

}
