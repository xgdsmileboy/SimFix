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
import cofix.common.node.metric.CondStruct;
import cofix.common.node.metric.Literal;
import cofix.common.node.metric.MethodCall;
import cofix.common.node.metric.Operator;
import cofix.common.node.metric.Variable;
import cofix.common.node.modify.Modification;

/**
 * @author Jiajun
 * @datae Jun 23, 2017
 */
public class ConditionalExpr extends Expr {

	private Expr _condition = null;
	private Expr _first = null;
	private Expr _snd = null;
	
	private Expr _condition_replace = null;
	private Expr _first_replace = null;
	private Expr _snd_replace = null;
	
	/**
	 * ConditionalExpression:
     *	Expression ? Expression : Expression
	 */
	public ConditionalExpr(int startLine, int endLine, ASTNode node) {
		super(startLine, endLine, node);
	}

	public void setCondition(Expr condition){
		_condition = condition;
	}
	
	public void setFirst(Expr first){
		_first = first;
	}
	
	public void setSecond(Expr snd){
		_snd = snd;
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
		if(_condition_replace != null){
			stringBuffer.append(_condition_replace.toSrcString());
		} else {
			stringBuffer.append(_condition.toSrcString());
		}
		stringBuffer.append("?");
		if(_first_replace != null){
			stringBuffer.append(_first_replace.toSrcString());
		} else {
			stringBuffer.append(_first.toSrcString());
		}
		stringBuffer.append(":");
		if(_snd_replace != null){
			stringBuffer.append(_snd_replace.toSrcString());
		} else {
			stringBuffer.append(_snd.toSrcString());
		}
		return stringBuffer;
	}

	@Override
	public List<Literal> getLiterals() {
		List<Literal> list = new LinkedList<>();
		list.addAll(_condition.getLiterals());
		list.addAll(_first.getLiterals());
		list.addAll(_snd.getLiterals());
		return list;
	}

	@Override
	public List<Variable> getVariables() {
		List<Variable> list = new LinkedList<>();
		list.addAll(_condition.getVariables());
		list.addAll(_first.getVariables());
		list.addAll(_snd.getVariables());
		return list;
	}

	@Override
	public List<MethodCall> getMethodCalls() {
		List<MethodCall> list = new LinkedList<>();
		list.addAll(_condition.getMethodCalls());
		list.addAll(_first.getMethodCalls());
		list.addAll(_snd.getMethodCalls());
		return list;
	}

	@Override
	public List<Operator> getOperators() {
		List<Operator> list = new LinkedList<>();
		list.addAll(_condition.getOperators());
		list.addAll(_first.getOperators());
		list.addAll(_snd.getOperators());
		return list;
	}
	
	@Override
	public List<CondStruct> getCondStruct() {
		List<CondStruct> list = new LinkedList<>();
		CondStruct condStruct = new CondStruct(this, CondStruct.KIND.CE);
		list.add(condStruct);
		list.addAll(_first.getCondStruct());
		list.addAll(_snd.getCondStruct());
		return list;
	}
}
