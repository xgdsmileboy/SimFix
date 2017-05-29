/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */

package cofix.common.astnode;

import org.eclipse.jdt.core.dom.Type;

public class Variable extends Expr {
	
	private Type _type = null; 
	private String _name = null;
	
	public Variable(Type type, String name) {
		_type = type;
		_name = name;
	}
	
	public Type getType(){
		return _type;
	}
	
	public String getName(){
		return _name;
	}
	
	@Override
	public String toString() {
		return _name;
	}
	
}
