/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.core.parser.node.expr;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Type;

import cofix.core.metric.Literal;
import cofix.core.metric.MethodCall;
import cofix.core.metric.NewFVector;
import cofix.core.metric.Operator;
import cofix.core.metric.Variable;
import cofix.core.modify.Modification;
import cofix.core.parser.node.Node;

/**
 * @author Jiajun
 * @datae Jun 23, 2017
 */
public class FieldAcc extends Expr {

	private Expr _expression = null;
	private SName _identifier = null;
	
	private Expr _replace = null;
	
	/**
	 * FieldAccess:
     *           Expression . Identifier
	 */
	public FieldAcc(int startLine, int endLine, ASTNode node) {
		super(startLine, endLine, node);
		_nodeType = TYPE.FIELDACC;
	}

	public void setExpression(Expr expression){
		_expression = expression;
	}
	
	public void setIdentifier(SName identifier){
		_identifier = identifier;
	}
	
	@Override
	public boolean match(Node node, Map<String, String> varTrans, Map<String, Type> allUsableVariables, List<Modification> modifications) {
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
		_replace = null;
		return true;
	}

	@Override
	public boolean backup(Modification modification) {
		return true;
	}

	@Override
	public StringBuffer toSrcString() {
		if(_replace != null){
			return _replace.toSrcString();
		} else {
			StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append(_expression.toSrcString());
			stringBuffer.append(".");
			stringBuffer.append(_identifier.toSrcString());
			return stringBuffer;
		}
	}
	
	@Override
	public List<Literal> getLiterals() {
		List<Literal> list = new LinkedList<>();
		list.addAll(_expression.getLiterals());
		list.addAll(_identifier.getLiterals());
		return list;
	}

	@Override
	public List<Variable> getVariables() {
		List<Variable> list = new LinkedList<>();
		list.addAll(_expression.getVariables());
		list.addAll(_identifier.getVariables());
		return list;
	}

	@Override
	public List<MethodCall> getMethodCalls() {
		List<MethodCall> list = new LinkedList<>();
		list.addAll(_expression.getMethodCalls());
		return list;
	}

	@Override
	public List<Operator> getOperators() {
		List<Operator> list = new LinkedList<>();
		list.addAll(_expression.getOperators());
		return list;
	}
	
	@Override
	public void computeFeatureVector() {
		_fVector = new NewFVector();
		_fVector.combineFeature(_expression.getFeatureVector());
		_fVector.combineFeature(_identifier.getFeatureVector());
	}
	
	@Override
	public List<Node> getChildren() {
		return new ArrayList<>();
	}
}
