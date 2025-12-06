package com.rdovgan.advent.day2;

import com.rdovgan.advent.util.ResourceData;

import java.util.List;

public class GiftShop {

	public static void main(String[] args) {
		List<String> dataList = new ResourceData().loadFromResource("data2.csv");
		List<Long> incorrectIds = IdRecognizer.fullDefineIncorrectIds(dataList.stream().findFirst().orElse(null));
		System.out.println(incorrectIds.stream().mapToLong(Long::longValue).sum());
	}

}
