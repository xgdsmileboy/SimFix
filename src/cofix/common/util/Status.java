/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.common.util;

/**
 * @author Jiajun
 * @date Jun 21, 2017
 */
public enum Status {
	
	SUCCESS("Successfully repair!"),
	FAILED("Failed to repair!"),
	TIMEOUT("Timeout!");
	
	private String _msg = null;
	private Status(String message){
		_msg = message;
	}
	
	@Override
	public String toString() {
		return _msg;
	}
}
