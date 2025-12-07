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

		int start = password.getPosition();
		int count = 0;

		if (rotationValue > 0) {
			// Right rotation: count how many times we land on 0
			// First time we hit 0 is after (100 - start) clicks, or 100 if start is 0
			int firstCrossing = start == 0 ? 100 : (100 - start);
			if (firstCrossing <= rotationValue) {
				// We cross at firstCrossing, firstCrossing + 100, firstCrossing + 200, ...
				count = 1 + (rotationValue - firstCrossing) / 100;
			}
		} else {
			// Left rotation: count how many times we land on 0
			int absRotation = -rotationValue;
			// First time we hit 0 is after start clicks, or 100 if start is 0
			int firstCrossing = start == 0 ? 100 : start;
			if (firstCrossing <= absRotation) {
				count = 1 + (absRotation - firstCrossing) / 100;
			}
		}

		int finalPos = Math.floorMod(start + rotationValue, 100);
		password.setPosition(finalPos);
		password.setPassword(password.getPassword() + count);
	}

}
