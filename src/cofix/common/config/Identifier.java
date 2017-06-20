/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */

package cofix.common.config;

import java.util.HashMap;
import java.util.Map;

/**
 * Used for label each method with a unique id
 * @author Jiajun
 *
 */
public class Identifier {
	private static Map<Integer, String> identifiers = new HashMap<>();
	private static Map<String, Integer> inverseIdentifier = new HashMap<>();
	private static Integer counter = 0;

	public static void resetAll() {
		identifiers = new HashMap<>();
		inverseIdentifier = new HashMap<>();
		counter = 0;
	}

	/**
	 * get exclusive method id based on the given method string information,
	 * 
	 * @param message
	 *            : string representation for method, e.g.,
	 *            "fullClasspath#returnType#methodName#arguments"
	 * @return an exclusive id for the given method
	 */
	public static Integer getIdentifier(String message) {
		Integer value = inverseIdentifier.get(message);
		if (value != null) {
			return value;
		} else {
			identifiers.put(counter, message);
			inverseIdentifier.put(message, counter);
			counter++;
			return counter - 1;
		}
	}

	/**
	 * get method string representation for the given method id
	 * 
	 * @param id
	 *            : method id
	 * @return a string of a method, e.g.,
	 *         "fullClasspath#returnType#methodName#arguments"
	 */
	public static String getMessage(int id) {
		String message = identifiers.get(Integer.valueOf(id));
		if (message == null) {
			message = "ERROR";
		}
		return message;
	}

}
