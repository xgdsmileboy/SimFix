/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.core.parser.node.stmt;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Type;

import cofix.core.metric.CondStruct;
import cofix.core.metric.Literal;
import cofix.core.metric.LoopStruct;
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
public class DoStmt extends Stmt {

	private Stmt _stmt = null;
	private Expr _expression = null;
	
	private Stmt _stmt_replace = null;
	private Expr _expression_replace = null;
	
	/**
	 * DoStatement:
     *	do Statement while ( Expression ) ;
	 */
	public DoStmt(int startLine, int endLine, ASTNode node) {
		this(startLine, endLine, node, null);
	}

	public DoStmt(int startLine, int endLine, ASTNode node, Node parent) {
		super(startLine, endLine, node, parent);
	}
	
	public void setBody(Stmt stmt){
		_stmt = stmt;
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
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("do ");
		if(_stmt_replace != null){
			stringBuffer.append(_stmt_replace.toSrcString());
		} else {
			stringBuffer.append(_stmt.toSrcString());
		}
		stringBuffer.append(" while(");
		if(_expression_replace != null){
			stringBuffer.append(_expression_replace.toSrcString());
		} else {
			stringBuffer.append(_expression.toSrcString());
		}
		stringBuffer.append(");");
		return stringBuffer;
	}

	@Override
	public List<Literal> getLiterals() {
		List<Literal> list = _expression.getLiterals();
		if(_stmt != null){
			list.addAll(_stmt.getLiterals());
		}
		return list;
	}

	@Override
	public List<Variable> getVariables() {
		List<Variable> list = _expression.getVariables();
		if(_stmt != null){
			list.addAll(_stmt.getVariables());
		}
		return list;
	}

	@Override
	public List<LoopStruct> getLoopStruct() {
		List<LoopStruct> list = new LinkedList<>();
		LoopStruct loopStruct = new LoopStruct(this, LoopStruct.KIND.DO);
		list.add(loopStruct);
		if(_stmt != null){
			list.addAll(_stmt.getLoopStruct());
		}
		return list;
	}
	
	@Override
	public List<CondStruct> getCondStruct() {
		List<CondStruct> list = new ArrayList<>();
		if(_stmt != null){
			list.addAll(_stmt.getCondStruct());
		}
		return list;
	}

	@Override
	public List<MethodCall> getMethodCalls() {
		List<MethodCall> list = _expression.getMethodCalls();
		if(_stmt != null){
			list.addAll(_stmt.getMethodCalls());
		}
		return list;
	}

	@Override
	public List<Operator> getOperators() {
		List<Operator> list = _expression.getOperators();
		if(_stmt != null){
			list.addAll(_stmt.getOperators());
		}
		return list;
	}
	
	@Override
	public List<OtherStruct> getOtherStruct() {
		List<OtherStruct> list = new LinkedList<>();
		if(_stmt != null){
			list.addAll(_stmt.getOtherStruct());
		}
		return list;
	}
	
}
