/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */

package cofix.common.astnode;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.Type;

public class MethodCall {
	
	private String _name = null;
	private Type _exprType = null;
	private List<Identifier> _parameters = null;
	
	public MethodCall(Type exprType, String name) {
		this(exprType, name, new ArrayList<>());
	}
	
	public MethodCall(Type exprType, String name, List<Identifier> parameters){
		_exprType = exprType;
		_name = name;
		_parameters = parameters;
	}
	
	public Type getExprType(){
		return _exprType;
	}
	
	public String getName(){
		return _name;
	}
	
	public List<Identifier> getParameters(){
		return _parameters;
	}
	
	
}
