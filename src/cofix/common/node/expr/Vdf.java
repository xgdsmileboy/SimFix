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
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import cofix.common.node.Node;
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
public class Vdf extends Node {

	private SName _identifier = null;
	private int _dimensions = 0; 
	private Expr _expression = null;
	
	private Expr _expression_replace = null;
	
	/**
	 * VariableDeclarationFragment:
     *	Identifier { Dimension } [ = Expression ]
	 */
	public Vdf(int startLine, int endLine, ASTNode node) {
		super(startLine, endLine, node);
	}
	
	public Vdf(int startLine, int endLine, ASTNode node, Node parent) {
		super(startLine, endLine, node, parent);
	}
	
	public void setName(SName identifier){
		_identifier = identifier;
	}
	
	public void setDimensions(int dimensions){
		_dimensions = dimensions;
	}
	
	public void setExpression(Expr expression){
		_expression = expression;
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
	public boolean match(Node node, Map<String, Type> allUsableVariables, List<Modification> modifications) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public StringBuffer toSrcString() {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(_identifier.toSrcString());
		for(int i = 0; i < _dimensions; i++){
			stringBuffer.append("[]");
		}
		if(_expression_replace != null){
			stringBuffer.append("=");
			stringBuffer.append(_expression_replace.toSrcString());
		} else if(_expression != null){
			stringBuffer.append("=");
			stringBuffer.append(_expression.toSrcString());
		}
		return stringBuffer;
	}

	@Override
	public List<Literal> getLiterals() {
		List<Literal> list = new LinkedList<>();
		if(_expression != null){
			list.addAll(_expression.getLiterals());
		}
		return list;
	}

	@Override
	public List<Variable> getVariables() {
		List<Variable> list = _identifier.getVariables();
		if(_expression != null){
			list.addAll(_expression.getVariables());
		}
		return list;
	}

	@Override
	public List<MethodCall> getMethodCalls() {
		List<MethodCall> list = new LinkedList<>();
		if(_expression != null){
			list.addAll(_expression.getMethodCalls());
		}
		return list;
	}

	@Override
	public List<Operator> getOperators() {
		if(_expression != null){
			return _expression.getOperators();
		}
		return new LinkedList<>();
	}

	@Override
	public List<CondStruct> getCondStruct() {
		if(_expression != null){
			return _expression.getCondStruct();
		}
		return new LinkedList<>();
	}

	@Override
	public List<LoopStruct> getLoopStruct() {
		return new LinkedList<>();
	}
	
	@Override
	public List<OtherStruct> getOtherStruct() {
		return new LinkedList<>();
	}

}
