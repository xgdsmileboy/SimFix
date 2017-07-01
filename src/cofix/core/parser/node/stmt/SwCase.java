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
import cofix.core.metric.MethodCall;
import cofix.core.metric.NewFVector;
import cofix.core.metric.Operator;
import cofix.core.metric.Variable;
import cofix.core.modify.Modification;
import cofix.core.parser.node.Node;
import cofix.core.parser.node.expr.Expr;

/**
 * @author Jiajun
 * @datae Jun 23, 2017
 */
public class SwCase extends Stmt {

	private Expr _expression = null;
	private List<Node> _siblings = null;
	
	private List<Node> _siblings_replace = null;
	
	/**
	 * SwitchCase:
     *           case Expression  :
     *           default :
	 */
	public SwCase(int startLine, int endLine, ASTNode node) {
		this(startLine, endLine, node, null);
	}
	
	public SwCase(int startLine, int endLine, ASTNode node, Node parent) {
		super(startLine, endLine, node, parent);
	}
	
	public void setExpression(Expr expression){
		_expression = expression;
	}
	
	public void addSibling(Node sibling){
		if(_siblings == null){
			_siblings = new ArrayList<>();
		}
		_siblings.add(sibling);
	}
	
	public void setSiblings(List<Node> siblings){
		_siblings = siblings;
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
		if(_expression == null){
			stringBuffer.append("default :\n");
		} else {
			stringBuffer.append("case ");
			stringBuffer.append(_expression.toSrcString());
			stringBuffer.append(" :\n");
		}
		if(_siblings_replace != null){
			for(Node sibling : _siblings_replace){
				stringBuffer.append(stringBuffer.append(sibling.toSrcString()));
				stringBuffer.append("\n");
			}
		} else {
			if(_siblings != null){
				for(Node sibling : _siblings){
					stringBuffer.append(sibling.toSrcString());
					stringBuffer.append("\n");
				}
			}
		}
		return stringBuffer;
	}

	@Override
	public List<Literal> getLiterals() {
		if(_expression != null){
			return _expression.getLiterals();
		}
		return new LinkedList<>();
	}

	@Override
	public List<Variable> getVariables() {
		if(_expression != null){
			return _expression.getVariables();
		}
		return new LinkedList<>();
	}

	
	@Override
	public List<CondStruct> getCondStruct() {
		List<CondStruct> list = new ArrayList<>();
		CondStruct condStruct = new CondStruct(this, CondStruct.KIND.SC);
		list.add(condStruct);
		return list;
	}

	@Override
	public List<MethodCall> getMethodCalls() {
		if(_expression != null){
			return _expression.getMethodCalls();
		}
		return new LinkedList<>();
	}

	@Override
	public List<Operator> getOperators() {
		if(_expression != null){
			return _expression.getOperators();
		}
		return new LinkedList<>();
	}

	@Override
	public void computeFeatureVector() {
		_fVector = new NewFVector();
		_fVector.inc(NewFVector.INDEX_STRUCT_COND);
		if(_expression != null){
			_fVector.combineFeature(_expression.getFeatureVector());
		}
		if(_siblings != null){
			for(Node node : _siblings){
				_fVector.combineFeature(node.getFeatureVector());
			}
		}
	}
	
}
