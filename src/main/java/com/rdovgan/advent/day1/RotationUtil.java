package com.rdovgan.advent.day1;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

public class RotationUtil {

	public static int defineRotation(String rotation) {
		if (StringUtils.isEmpty(rotation)) {
			return 0;
		}
		try {
			int rotationValue = NumberUtils.createInteger(rotation.substring(1));
			return StringUtils.substring(rotation, 0, 1).equals("L") ? -rotationValue : rotationValue;
		} catch (Exception _) {
		}
		return 0;
	}

	public static void rotate(Password password, String rotation) {
		int rotationValue = defineRotation(rotation);
		if (rotationValue == 0) {
			return;
		}
		int oldValue = password.getPosition();
		int absValue = Math.abs(rotationValue);
		int clicks = absValue / 100;
		int rem = absValue % 100;
		if (rem != 0 && (rotationValue > 0 ? oldValue + rem >= 100 : oldValue - rem < 0)) {
			clicks++;
		}
		password.setPosition(Math.floorMod(oldValue + rotationValue, 100));
		password.setPassword(password.getPassword() + clicks);
	}

}
