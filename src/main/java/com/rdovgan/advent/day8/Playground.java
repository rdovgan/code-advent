package com.rdovgan.advent.day8;

import com.rdovgan.advent.util.ResourceData;

import java.math.BigInteger;
import java.util.List;

public class Playground {
	public static void main(String[] args) {
		List<String> lines = new ResourceData().loadFromResource("data8.csv");
		List<Point> pts = SpaceUtil.definePoints(lines);
		BigInteger product = SpaceUtil.multiplyTop3ByConnectingKPairs(pts, 1000);
		System.out.println(product);
		BigInteger xProduct = SpaceUtil.productOfXCoordinatesOfLastConnection(pts);
		System.out.println(xProduct);
	}
}
