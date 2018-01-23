/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.common.run;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Jiajun
 * @date Jul 11, 2017
 */
public class Executor {
	private final static String __name__ = "@Executor ";
	
	public static List<String> execute(String[] command) {
		Process process = null;
		final List<String> message = new ArrayList<String>();
		try {
			ProcessBuilder builder = new ProcessBuilder(command);
			builder.redirectErrorStream(true);
			process = builder.start();
			final InputStream inputStream = process.getInputStream();
			
			Thread processReader = new Thread(){
				public void run() {
					BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
					String line;
					try {
						while((line = reader.readLine()) != null) {
							message.add(line);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
					try {
						reader.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			};
			
			processReader.start();
			try {
				processReader.join();
				process.waitFor();
			} catch (InterruptedException e) {
				return new LinkedList<>();
			}
		} catch (IOException e) {
		} finally {
			if (process != null) {
				process.destroy();
			}
			process = null;
		}
		
		return message;
	}
	
//	public static List<String> executeCommand(String[] command) throws IOException, InterruptedException {
//		final Process process = Runtime.getRuntime().exec(command);
//
//		final List<String> message = new ArrayList<>();
//		
//		BufferedReader output = new BufferedReader(new InputStreamReader(process.getInputStream()));
//		BufferedReader error = new BufferedReader(new InputStreamReader(process.getErrorStream()));
//		String ligne = "";
//
//		while ((ligne = output.readLine()) != null) {
////		    System.out.println(ligne);
//		    message.add(ligne);
//		}
//
//		while ((ligne = error.readLine()) != null) {
////			System.out.println(ligne);
//			message.add(ligne);
//		}
//
//		process.waitFor();
//		return message;
//	}
}
