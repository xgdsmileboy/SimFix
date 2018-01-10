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
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.Type;

import cofix.common.util.Pair;
import cofix.core.metric.Literal;
import cofix.core.metric.MethodCall;
import cofix.core.metric.NewFVector;
import cofix.core.metric.Operator;
import cofix.core.metric.Variable;
import cofix.core.metric.Variable.USE_TYPE;
import cofix.core.modify.Modification;
import cofix.core.modify.Revision;
import cofix.core.parser.NodeUtils;
import cofix.core.parser.node.Node;

/**
 * @author Jiajun
 * @date Jun 23, 2017
 */
public class PrefixExpr extends Expr {

	private Expr _expression = null;
	private PrefixExpression.Operator _operator = null;
	
	private String _expression_replace = null;
	private String _operator_replace = null;
	private String _whole_replace = null;
	
	private final int EXPRID = 0;
	private final int OPID = 1;
	private final int WHOLE = 2;
	
	/**
	 * PrefixExpression:
     *	PrefixOperator Expression
	 */
	public PrefixExpr(int startLine, int endLine, ASTNode node) {
		super(startLine, endLine, node);
		_nodeType = TYPE.PREEXPR;
	}
	
	public void setExpression(Expr expression){
		_expression = expression;
	}
	
	public void setOperator(PrefixExpression.Operator operator){
		_operator = operator;
	}

	@Override
	public boolean match(Node node, Map<String, String> varTrans, Map<String, Type> allUsableVariables, List<Modification> modifications) {
		boolean match = false;
		if(node instanceof PrefixExpr){
			PrefixExpr other = (PrefixExpr) node;
			if(other.getType().toString().equals(getType().toString())) {
				if(!other._operator.toString().equals(_operator.toString())) {
					Revision revision = new Revision(this, OPID, other._operator.toString(), _nodeType);
					modifications.add(revision);
				}
				Map<SName, Pair<String, String>> record = NodeUtils.tryReplaceAllVariables(other._expression, varTrans, allUsableVariables);
				if(record != null) {
					NodeUtils.replaceVariable(record);
					String target = other._expression.toSrcString().toString();
					if(!target.equals(toSrcString().toString())) {
						Revision revision = new Revision(this, EXPRID, target, _nodeType);
						modifications.add(revision);
					}
					NodeUtils.restoreVariables(record);
				}
			}
		} else {
			List<Modification> tmp = new LinkedList<>();
			if(replaceExpr(node, WHOLE, varTrans, allUsableVariables,tmp)) {
				modifications.addAll(tmp);
				match = true;
			}
			tmp = new ArrayList<>();
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
		if(modification instanceof Revision) {
			switch (modification.getSourceID()) {
			case EXPRID:
				_expression_replace = modification.getTargetString();
				break;
			case OPID:
				_operator_replace = modification.getTargetString();
				break;
			case WHOLE:
				_whole_replace = modification.getTargetString();
				break;
			default:
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean restore(Modification modification) {
		if(modification instanceof Revision) {
			switch (modification.getSourceID()) {
			case EXPRID:
				_expression_replace = null;
				break;
			case OPID:
				_operator_replace = null;
				break;
			case WHOLE:
				_whole_replace = null;
				break;
			default:
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean backup(Modification modification) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public StringBuffer toSrcString() {
		StringBuffer stringBuffer = new StringBuffer();
		if(_whole_replace != null) {
			stringBuffer.append(_whole_replace);
		} else {
			if(_operator_replace != null){
				stringBuffer.append(_operator_replace);
			} else {
				stringBuffer.append(_operator.toString());
			}
			if(_expression_replace != null){
				stringBuffer.append(_expression_replace);
			} else {
				stringBuffer.append(_expression.toSrcString());
			}
		}
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
		List<Operator> list = new LinkedList<>();
		Operator operator = new Operator(this, Operator.KIND.PREFIX);
		list.add(operator);
		list.addAll(_expression.getOperators());
		return list;
	}
	
	@Override
	public void computeFeatureVector() {
		_fVector = new NewFVector();
		_fVector.inc(_operator.toString());
		_fVector.combineFeature(_expression.getFeatureVector());
	}

	@Override
	public USE_TYPE getUseType(Node child) {
		return USE_TYPE.USE_PREFIX_EXP;
	}
	
	@Override
	public List<Node> getChildren() {
		List<Node> list = new ArrayList<>();
		list.add(_expression);
		return list;
	}

	@Override
	public String simplify(Map<String, String> varTrans, Map<String, Type> allUsableVariables) {
		String expr = _expression.simplify(varTrans, allUsableVariables);
		if(expr == null){
			return null;
		}
		return _operator.toString() + expr;
	}
}
