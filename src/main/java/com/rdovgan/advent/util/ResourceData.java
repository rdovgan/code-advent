package com.rdovgan.advent.util;

import lombok.Getter;
import lombok.Setter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ResourceData {

	public List<String> loadFromResource(String resourcePath) {
		List<String> values = new ArrayList<>();
		try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
			if (inputStream == null) {
				throw new RuntimeException("Resource not found: " + resourcePath);
			}

			try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
				String line;
				while ((line = reader.readLine()) != null) {
					if (!line.trim().isEmpty()) {
						values.add(line.trim());
					}
				}
			}
		} catch (IOException e) {
			throw new RuntimeException("Failed to load rotation data from: " + resourcePath, e);
		}
		return values;
	}

	public void writeToResource(String resourcePath, List<String> values) {
		String filePath = "src/main/resources/" + resourcePath;
		try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath)))) {
			for (String value : values) {
				writer.write(value);
				writer.newLine();
			}
			System.out.println("Anagrams successfully written to: " + filePath);
		} catch (IOException e) {
			throw new RuntimeException("Failed to write data to: " + resourcePath, e);
		}
	}

}
