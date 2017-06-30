/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.core.parser.node.stmt;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Type;

import cofix.core.metric.Literal;
import cofix.core.metric.MethodCall;
import cofix.core.metric.Operator;
import cofix.core.metric.OtherStruct;
import cofix.core.metric.Variable;
import cofix.core.modify.Modification;
import cofix.core.parser.node.Node;
import cofix.core.parser.node.expr.Expr;

/**
 * @author Jiajun
 * @datae Jun 23, 2017
 */
public class ThrowStmt extends Stmt {

	private Expr _expression = null;
	
	private Expr _expression_replace = null;
	
	/**
	 * ThrowStatement:
     *	throw Expression ;
	 */
	public ThrowStmt(int startLine, int endLine, ASTNode node) {
		this(startLine, endLine, node, null);
	}

	public ThrowStmt(int startLine, int endLine, ASTNode node, Node parent) {
		super(startLine, endLine, node, parent);
	}
	
	public void setExpression(Expr expression){
		_expression = expression;
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
		StringBuffer stringBuffer = new StringBuffer("throw ");
		if(_expression_replace != null){
			stringBuffer.append(_expression_replace.toSrcString());
		} else {
			stringBuffer.append(_expression.toSrcString());
		}
		stringBuffer.append(";");
		return stringBuffer;
	}

	@Override
	public List<Literal> getLiterals() {
		return _expression.getLiterals();
	}

	@Override
	public List<Variable> getVariables() {
		return _expression.getVariables();
	}

	@Override
	public List<MethodCall> getMethodCalls() {
		return _expression.getMethodCalls();
	}

	@Override
	public List<Operator> getOperators() {
		return _expression.getOperators();
	}

	@Override
	public List<OtherStruct> getOtherStruct() {
		List<OtherStruct> list = new LinkedList<>();
		OtherStruct otherStruct = new OtherStruct(this, OtherStruct.KIND.THROW);
		list.add(otherStruct);
		list.addAll(_expression.getOtherStruct());
		return list;
	}
}
