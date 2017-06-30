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

import javax.print.attribute.standard.MediaSize.Other;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Type;

import com.sun.org.apache.bcel.internal.generic.LSTORE;

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
public class IfStmt extends Stmt {

	private Expr _condition = null;
	private Stmt _then = null;
	private Stmt _else = null;
	
	private Expr _condition_replace = null;
	private Expr _then_replace = null;
	private Expr _else_replace = null;
	
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
	public StringBuffer toSrcString() {
		StringBuffer stringBuffer = new StringBuffer("if(");
		if(_condition_replace != null){
			stringBuffer.append(_condition_replace.toSrcString());
		} else {
			stringBuffer.append(_condition.toSrcString());
		}
		stringBuffer.append(")");
		if(_then_replace != null){
			stringBuffer.append(_then_replace.toSrcString());
		} else {
			stringBuffer.append(_then.toSrcString());
		}
		if(_else_replace != null){
			stringBuffer.append("else ");
			stringBuffer.append(_else_replace.toSrcString());
		} else if(_else != null){
			stringBuffer.append("else ");
			stringBuffer.append(_else.toSrcString());
		}
		return stringBuffer;
	}

	@Override
	public List<Literal> getLiterals() {
		List<Literal> list = _condition.getLiterals();
		if(_then != null){
			list.addAll(_then.getLiterals());
		}
		if(_else != null){
			list.addAll(_else.getLiterals());
		}
		return list;
	}

	@Override
	public List<Variable> getVariables() {
		List<Variable> list = _condition.getVariables();
		if(_then != null){
			list.addAll(_then.getVariables());
		}
		if(_else != null){
			list.addAll(_else.getVariables());
		}
		return list;
	}

	@Override
	public List<LoopStruct> getLoopStruct() {
		List<LoopStruct> list = new LinkedList<>();
		if(_then != null){
			list.addAll(_then.getLoopStruct());
		}
		if(_else != null){
			list.addAll(_else.getLoopStruct());
		}
		return list;
	}
	
	@Override
	public List<CondStruct> getCondStruct() {
		List<CondStruct> list = new LinkedList<>();
		CondStruct condStruct = new CondStruct(this, CondStruct.KIND.IF);
		list.add(condStruct);
		if(_then != null){
			list.addAll(_then.getCondStruct());
		}
		if(_else != null){
			list.addAll(_else.getCondStruct());
		}
		return list;
	}

	@Override
	public List<MethodCall> getMethodCalls() {
		List<MethodCall> list = _condition.getMethodCalls();
		if(_then != null){
			list.addAll(_then.getMethodCalls());
		}
		if(_else != null){
			list.addAll(_else.getMethodCalls());
		}
		return list;
	}

	@Override
	public List<Operator> getOperators() {
		List<Operator> list = _condition.getOperators();
		if(_then != null){
			list.addAll(_then.getOperators());
		}
		if(_else != null){
			list.addAll(_else.getOperators());
		}
		return list;
	}
	
	@Override
	public List<OtherStruct> getOtherStruct() {
		List<OtherStruct> list = new LinkedList<>();
		if(_then != null){
			list.addAll(_then.getOtherStruct());
		}
		if(_else != null){
			list.addAll(_else.getOtherStruct());
		}
		return list;
	}
}
