/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.common.node.expr;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Type;

import cofix.common.node.Node;
import cofix.common.node.metric.Literal;
import cofix.common.node.metric.MethodCall;
import cofix.common.node.metric.Operator;
import cofix.common.node.metric.Variable;
import cofix.common.node.modify.Modification;

/**
 * @author Jiajun
 * @datae Jun 23, 2017
 */
public class ArrayInitial extends Expr {

	private List<Expr> _expressions = null;
	
	/**
	 * ArrayInitializer:
     *           { [ Expression { , Expression} [ , ]] }
	 */
	public ArrayInitial(int startLine, int endLine, ASTNode node) {
		super(startLine, endLine, node);
	}
	
	public void setExpressions(List<Expr> expressions){
		_expressions = expressions;
	}

	@Override
	public boolean match(Node node, Map<String, Type> allUsableVariables, List<Modification> modifications) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean adapt(Modification modification) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean restore(Modification modification) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean backup(Modification modification) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<Literal> getLiterals() {
		List<Literal> list = new LinkedList<>();
		if(_expressions != null){
			for(Expr expr : _expressions){
				list.addAll(expr.getLiterals());
			}
		}
		return list;
	}

	@Override
	public List<Variable> getVariables() {
		List<Variable> list = new LinkedList<>();
		if(_expressions != null){
			for(Expr expr : _expressions){
				list.addAll(expr.getVariables());
			}
		}
		return list;
	}
	
	@Override
	public List<MethodCall> getMethodCalls() {
		List<MethodCall> list = new LinkedList<>();
		if(_expressions != null){
			for(Expr expr : _expressions){
				list.addAll(expr.getMethodCalls());
			}
		}
		return list;
	}
	
	@Override
	public List<Operator> getOperators() {
		List<Operator> list = new LinkedList<>();
		if(_expressions != null){
			for(Expr expr : _expressions){
				list.addAll(expr.getOperators());
			}
		}
		return list;
	}

}
