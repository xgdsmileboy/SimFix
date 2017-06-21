/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */

package cofix.common.astnode;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Type;

import cofix.common.astnode.expr.Variable;
import cofix.core.adapt.Adapter;
import cofix.core.adapt.Delta;

public abstract class Expr implements Adapter {
	
	protected ASTNode _srcNode = null;
	protected Expr _backup = null;
	
	public abstract Type getType();
	
	public abstract void backup();
	
	public abstract void restore();
	
	public abstract List<Variable> getVariables();
	
	public abstract boolean matchType(Expr expr, Map<String, Type> allUsableVariables, List<Delta> modifications);
	
	protected boolean canReplace(Expr expr, Map<String, Type> allUsableVarMap){
		List<Variable> variables = expr.getVariables();
		boolean replacable = true;
		for(Variable variable : variables){
			String name = variable.getName();
			if(name.equals("THIS")){
				continue;
			}
			Type type = allUsableVarMap.get(name);
			if(type == null){
				// null match non-null
				if(variable.getType() != null){
					replacable = false;
					break;
				}
				//non-null match null
			} else if(variable.getType() == null) {
				replacable = false;
				break;
				//non-null match non-null
			} else if(!type.toString().equals(variable.getType().toString())){
				replacable = false;
				break;
			}
		}
		return replacable;
	}
	
	public ASTNode getOriginalASTnode(){
		return _srcNode;
	}

}
