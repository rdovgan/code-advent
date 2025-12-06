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
		password.setPosition((password.getPosition() + rotationValue) % 100);
		if (password.getPosition() == 0) {
			password.setPassword(password.getPassword() + 1);
		}
	}

}
