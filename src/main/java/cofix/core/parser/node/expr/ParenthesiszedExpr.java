/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.core.parser.node.expr;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jws.WebParam.Mode;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Type;

import cofix.common.util.Pair;
import cofix.core.metric.CondStruct;
import cofix.core.metric.Literal;
import cofix.core.metric.MethodCall;
import cofix.core.metric.NewFVector;
import cofix.core.metric.Operator;
import cofix.core.metric.Variable;
import cofix.core.modify.Modification;
import cofix.core.modify.Revision;
import cofix.core.parser.NodeUtils;
import cofix.core.parser.node.Node;

/**
 * @author Jiajun
 * @date Jun 23, 2017
 */
public class ParenthesiszedExpr extends Expr {

	private Expr _expression = null;
	
	private String _expression_replace = null;
	
	private final int WHOLE = 0;
	
	/**
	 * ParenthesizedExpression:
     *	( Expression )
	 */
	public ParenthesiszedExpr(int startLine, int endLine, ASTNode node) {
		super(startLine, endLine, node);
		_nodeType = TYPE.PARENTHESISZED;
	}

	public void setExpr(Expr expression){
		_expression = expression;
	}
	
	@Override
	public boolean match(Node node, Map<String, String> varTrans, Map<String, Type> allUsableVariables, List<Modification> modifications) {
		boolean match = false;
		if(node instanceof ParenthesiszedExpr){
			return _expression.match(((ParenthesiszedExpr) node)._expression, varTrans, allUsableVariables, modifications);
			// TODO : to finish
		} else {
			List<Modification> tmp = new ArrayList<>();
			List<Node> children = node.getChildren();
			if(NodeUtils.nodeMatchList(this, children, varTrans, allUsableVariables, tmp)){
				match = true;
				modifications.addAll(tmp);
			}
			
		}
		return match;
	}

	@Override
	public boolean adapt(Modification modification) {
		if(modification.getSourceID() == WHOLE){
			_expression_replace = modification.getTargetString();
			return true;
		}
		return false;
	}

	@Override
	public boolean restore(Modification modification) {
		if(modification.getSourceID() == WHOLE){
			_expression_replace = null;
			return true;
		}
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
		stringBuffer.append("(");
		if(_expression_replace != null){
			stringBuffer.append(_expression_replace);
		} else {
			stringBuffer.append(_expression.toSrcString());
		}
		stringBuffer.append(")");
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
	public List<CondStruct> getCondStruct() {
		return _expression.getCondStruct();
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
	public void computeFeatureVector() {
		_fVector = new NewFVector();
		_fVector.combineFeature(_expression.getFeatureVector());
	}
	
	@Override
	public List<Node> getChildren() {
		List<Node> list = new ArrayList<>();
		list.add(_expression);
		return list;
	}

	@Override
	public String simplify(Map<String, String> varTrans, Map<String, Type> allUsableVariables) {
		String string = _expression.simplify(varTrans, allUsableVariables);
		if(string == null){
			return null;
		}
		return "(" + string + ")";
	}
}
