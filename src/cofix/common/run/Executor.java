/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.common.run;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jiajun
 * @datae Jul 11, 2017
 */
public class Executor {
	private final static String __name__ = "@Executor ";
	
	public static List<String> executeCommand(String[] command) throws IOException, InterruptedException {
		final Process process = Runtime.getRuntime().exec(command);

		final List<String> message = new ArrayList<>();
		
		BufferedReader output = new BufferedReader(new InputStreamReader(process.getInputStream()));
		BufferedReader error = new BufferedReader(new InputStreamReader(process.getErrorStream()));
		String ligne = "";

		while ((ligne = output.readLine()) != null) {
		    System.out.println(ligne);
		    message.add(ligne);
		}

		while ((ligne = error.readLine()) != null) {
			System.out.println(ligne);
			message.add(ligne);
		}

		process.waitFor();
		return message;
	}
}
