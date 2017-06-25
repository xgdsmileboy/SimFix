/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.common.node.stmt;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Type;

import cofix.common.node.Node;
import cofix.common.node.expr.Expr;
import cofix.common.node.metric.Literal;
import cofix.common.node.metric.MethodCall;
import cofix.common.node.metric.Operator;
import cofix.common.node.metric.Structure;
import cofix.common.node.metric.Variable;
import cofix.common.node.modify.Modification;

/**
 * @author Jiajun
 * @datae Jun 23, 2017
 */
public class IfStmt extends Stmt {

	private Expr _condition = null;
	private Stmt _then = null;
	private Stmt _else = null;
	
	/**
	 * IfStatement:
     *	if ( Expression ) Statement [ else Statement]
	 */
	public IfStmt(int startLine, int endLine, ASTNode node) {
		this(startLine, endLine, node, null);
	}
	
	public IfStmt(int startLine, int endLine, ASTNode node, Node parent) {
		super(startLine, endLine, node, parent);
	}
	
	public void setCondition(Expr condition){
		_condition = condition;
	}
	
	public void setThen(Stmt then){
		_then = then;
	}
	
	public void setElse(Stmt els){
		_else = els;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Variable> getVariables() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Structure> getStructures() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<MethodCall> getMethodCalls() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Operator> getOperators() {
		// TODO Auto-generated method stub
		return null;
	}
}
