/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.common.node.stmt;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Type;

import cofix.common.node.Node;
import cofix.common.node.expr.Expr;
import cofix.common.node.metric.CondStruct;
import cofix.common.node.metric.Literal;
import cofix.common.node.metric.LoopStruct;
import cofix.common.node.metric.MethodCall;
import cofix.common.node.metric.Operator;
import cofix.common.node.metric.OtherStruct;
import cofix.common.node.metric.Variable;
import cofix.common.node.modify.Modification;

/**
 * @author Jiajun
 * @datae Jun 23, 2017
 */
public class WhileStmt extends Stmt {

	private Expr _expression = null;
	private Stmt _body = null;
	
	private Expr _expression_replace = null;
	private Stmt _body_replace = null;
	
	/**
	 * WhileStatement:
     *	while ( Expression ) Statement
	 */
	public WhileStmt(int startLine, int endLine, ASTNode node) {
		this(startLine, endLine, node, null);
	}

	public WhileStmt(int startLine, int endLine, ASTNode node, Node parent) {
		super(startLine, endLine, node, parent);
	}
	
	public void setExpression(Expr expression){
		_expression = expression;
	}
	
	public void setBody(Stmt body){
		_body = body;
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
	public StringBuffer toSrcString() {
		StringBuffer stringBuffer = new StringBuffer("while(");
		if(_expression_replace != null){
			stringBuffer.append(_expression_replace.toSrcString());
		} else {
			stringBuffer.append(_expression.toSrcString());
		}
		stringBuffer.append(")");
		if(_body_replace != null){
			stringBuffer.append(_body_replace.toSrcString());
		} else {
			stringBuffer.append(_body.toSrcString());
		}
		return stringBuffer;
	}

	@Override
	public List<Literal> getLiterals() {
		List<Literal> list = _expression.getLiterals();
		list.addAll(_body.getLiterals());
		return list;
	}

	@Override
	public List<Variable> getVariables() {
		List<Variable> list = _expression.getVariables();
		list.addAll(_body.getVariables());
		return list;
	}

	@Override
	public List<LoopStruct> getLoopStruct() {
		List<LoopStruct> list = new LinkedList<>();
		LoopStruct loopStruct = new LoopStruct(this, LoopStruct.KIND.WHILE);
		list.add(loopStruct);
		list.addAll(_body.getLoopStruct());
		return null;
	}
	
	@Override
	public List<CondStruct> getCondStruct() {
		List<CondStruct> list = new LinkedList<>();
		list.addAll(_body.getCondStruct());
		return list;
	}

	@Override
	public List<MethodCall> getMethodCalls() {
		List<MethodCall> list = _expression.getMethodCalls();
		list.addAll(_body.getMethodCalls());
		return list;
	}

	@Override
	public List<Operator> getOperators() {
		List<Operator> list = _expression.getOperators();
		list.addAll(_body.getOperators());
		return list;
	}

	@Override
	public List<OtherStruct> getOtherStruct() {
		List<OtherStruct> list = new LinkedList<>();
		list.addAll(_body.getOtherStruct());
		return list;
	}
}
