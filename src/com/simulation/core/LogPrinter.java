//Siyuan Zhou
package com.simulation.core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class LogPrinter {
	private static FileWriter fw;
	
	static {
		try {
			fw = new FileWriter(new File("MultiProcsSimulation.txt"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void out(String content) {
		System.out.println(content);
		try {
			fw.write(content);
			fw.write("\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
		;
	}
}
