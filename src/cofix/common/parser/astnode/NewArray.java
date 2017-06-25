/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.common.parser.astnode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Type;

import cofix.common.parser.astnode.expr.Variable;
import cofix.core.adapt.Modification;

/**
 * @author Jiajun
 * @datae May 31, 2017
 */
public class NewArray extends MethodCall{

	private List<Expr> _dimension = null;
	private List<Expr> _initializers = null;
	
	public NewArray(ASTNode node, Type retType, Expr expr, String name) {
		this(node, retType, expr, name, new ArrayList<Expr>());
	}
	
	public NewArray(ASTNode node, Type retType, Expr expr, String name, List<Expr> dimensions){
		this(node, retType, expr, name, dimensions, null);
	}
	
	public NewArray(ASTNode node, Type retType, Expr expr, String name, List<Expr> dimensions, List<Expr> initializers){
		super(node, retType, expr, name);
		_dimension = dimensions;
		_initializers = initializers;
	}
	
	public void setDimension(List<Expr> dimension){
		_dimension = dimension;
	}
	
	public void setInitializers(List<Expr> initializers){
		_initializers = initializers;
	}
	
	@Override
	public String toString() {
		StringBuffer stringBuffer = new StringBuffer();
		if(_expr != null){
			stringBuffer.append(_expr);
			stringBuffer.append(".");
		}
		stringBuffer.append("new ");
		stringBuffer.append(_name);
		for(int i = 0; i < _dimension.size(); i++){
			stringBuffer.append("[");
			stringBuffer.append(_dimension.get(i));
			stringBuffer.append("]");
		}
		if(_initializers != null){
			stringBuffer.append("{");
			stringBuffer.append(_initializers.get(0));
			for(int i = 1; i < _initializers.size(); i++){
				stringBuffer.append(",");
				stringBuffer.append(_initializers.get(i));
			}
			stringBuffer.append("}");
		}
		return stringBuffer.toString();
	}
	
	@Override
	public boolean matchType(Expr expr, Map<String, Type> allUsableVariables, List<Modification> modifications) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Expr adapt(Expr tar, Modification modify, Map<String, Type> allUsableVarMap) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public List<Variable> getVariables() {
		List<Variable> variables = super.getVariables();
		for(Expr expr : _dimension){
			variables.addAll(expr.getVariables());
		}
		for(Expr expr : _initializers){
			variables.addAll(expr.getVariables());
		}
		return variables;
	}
	
}
