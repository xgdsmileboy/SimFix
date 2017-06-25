/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.common.parser.astnode.expr;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.Type;

import cofix.common.parser.astnode.Expr;
import cofix.core.adapt.Modification;

/**
 * @author Jiajun
 * @datae Jun 21, 2017
 */
public class VariableDef extends Expr{

	@Override
	public Expr adapt(Expr tar, Modification modify, Map<String, Type> allUsableVarMap) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type getType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void backup() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void restore() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<Variable> getVariables() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean matchType(Expr expr, Map<String, Type> allUsableVariables, List<Modification> modifications) {
		// TODO Auto-generated method stub
		return false;
	}

}
