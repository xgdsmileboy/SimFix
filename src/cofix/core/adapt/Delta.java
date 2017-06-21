/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.core.adapt;

import java.util.Map;

import org.eclipse.jdt.core.dom.Type;

import cofix.common.astnode.Expr;

/**
 * @author Jiajun
 * @datae Jun 6, 2017
 */
public abstract class Delta{
	
	protected Expr _expr = null;
	protected int _modificationComplexity = 0;
	
	public Delta(Expr expr) {
		_expr = expr;
	}
	
	public abstract boolean apply(Map<String, Type> allUsableVarMap);
	
	public abstract boolean restore();
	
	public void setModificationComplexity(int complexity){
		_modificationComplexity = complexity;
	}
	
	public int getModificationComplexity(){
		return _modificationComplexity;
	}
}
