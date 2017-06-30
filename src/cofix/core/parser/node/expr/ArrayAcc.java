/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.core.parser.node.expr;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Type;

import cofix.core.metric.Literal;
import cofix.core.metric.MethodCall;
import cofix.core.metric.Operator;
import cofix.core.metric.Variable;
import cofix.core.modify.Modification;
import cofix.core.parser.node.Node;

/**
 * @author Jiajun
 * @datae Jun 23, 2017
 */
public class ArrayAcc extends Expr {

	private Expr _index = null;
	private Expr _array = null;
	
	private Expr _index_replace = null;
	private Expr _array_replace = null;
	
	/**
	 * ArrayAccess:
     *	Expression [ Expression ]
	 */
	public ArrayAcc(int startLine, int endLine, ASTNode node) {
		super(startLine, endLine, node);
	}
	
	public void setArray(Expr array){
		_array = array;
	}
	
	public void setIndex(Expr index){
		_index = index;
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
		if(_array_replace != null){
			stringBuffer.append(_array_replace.toSrcString());
		} else {
			stringBuffer.append(_array.toSrcString());
		}
		stringBuffer.append("[");
		if(_index_replace != null){
			stringBuffer.append(_index_replace.toSrcString());
		} else {
			stringBuffer.append(_index.toSrcString());
		}
		stringBuffer.append("]");
		return stringBuffer;
	}

	@Override
	public List<Literal> getLiterals() {
		List<Literal> list = new LinkedList<>();
		list.addAll(_array.getLiterals());
		list.addAll(_index.getLiterals());
		return list;
	}

	@Override
	public List<Variable> getVariables() {
		List<Variable> list = new LinkedList<>();
		list.addAll(_array.getVariables());
		list.addAll(_index.getVariables());
		return list;
	}

	@Override
	public List<MethodCall> getMethodCalls() {
		List<MethodCall> list = new LinkedList<>();
		list.addAll(_array.getMethodCalls());
		list.addAll(_index.getMethodCalls());
		return list;
	}

	@Override
	public List<Operator> getOperators() {
		List<Operator> list = new LinkedList<>();
		Operator operator = new Operator(this, Operator.KIND.ACC);
		list.add(operator);
		list.addAll(_array.getOperators());
		list.addAll(_index.getOperators());
		return list;
	}

}
