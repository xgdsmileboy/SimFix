/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */

package cofix.common.astnode;

public enum Structure{
	IF("if"),
	ELSE("else"),
	FOR("for"),
	WHILE("while"),
	BREAK("break"),
	CONTINUE("continue"),
	RETURN("return"),
	THRWO("throw");
	
	private String _value = null;
	private Structure(String value){
		_value = value;
	}
	
	@Override
	public String toString() {
		return _value;
	}
	
}
