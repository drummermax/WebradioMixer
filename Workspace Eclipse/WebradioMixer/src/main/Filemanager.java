package main; 

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class Filemanager {
	private static Filemanager instance;

	private String path;

	public Map<String, Number> variables;
	
	private final int numberOfVariables = 20;

	public static Filemanager getInstance() {
		if (instance == null) {
			instance = new Filemanager();
		}
		return instance;
	}

	private Filemanager() {
		variables = new HashMap<String, Number>();
		
		path = System.getProperty("user.dir") + "\\config.txt";
		
		if (path.contains("\\workspace\\WebradioMixer"))
			path = path.replace("\\workspace\\WebradioMixer", "");
		
		getConfig();
	}

	public void getConfig() {
		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
			String line1, line2;
			for (int i = 0; i < numberOfVariables; i++) {
				line1 = br.readLine();
				line2 = br.readLine();

				variables.put(line1, Integer.parseInt(line2));
			}
			
		} catch (FileNotFoundException e) {
			System.out.println("Config file not found!");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Config file parsing error!");
			e.printStackTrace();
		}
	}
	
	public void setConfig(int[] lineNumbers) {
		BufferedReader br = null;
		PrintWriter writer = null;
		try {
		    br = new BufferedReader(new FileReader(path));
			writer = new PrintWriter(new BufferedWriter(new FileWriter(path.replace("txt", "temp"))));
		} catch (IOException e) {
			System.out.println("Config file not opened!");
			e.printStackTrace();
		}
		
		String originalString = null;
		
		for (int i = 0; i < 2*numberOfVariables; i++) {
			try {
				originalString = br.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			if (i % 2 == 1) {
				writer.println(lineNumbers[(i-1)/2]);
			} else {
				writer.println(originalString);
			}
		}

		try {
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		writer.close();
		
		File realName = new File(path);
		realName.delete();
		new File(path.replace("txt", "temp")).renameTo(realName);
	}

}
