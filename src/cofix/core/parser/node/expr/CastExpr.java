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
public class CastExpr extends Expr {

	private Type _castType = null;
	private Expr _expression = null;

	private String _replace_expression = null;
	private String _replace_type = null;
	private String _replace_whole = null;
	
	private final int EXPR = 0;
	private final int CTYPE = 1;
	private final int WHOLE = 2;
	
	/**
	 * CastExpression:
     *	( Type ) Expression
	 */
	public CastExpr(int startLine, int endLine, ASTNode node) {
		super(startLine, endLine, node);
		_nodeType = TYPE.CAST;
	}
	
	public void setCastType(Type type){
		_castType = type;
	}
	
	public void setExpression(Expr expression){
		_expression = expression;
	}

	@Override
	public boolean match(Node node, Map<String, String> varTrans, Map<String, Type> allUsableVariables, List<Modification> modifications) {
		boolean match = false;
		if(node instanceof CastExpr){
			match = true;
			CastExpr other = (CastExpr) node;
			if(!_castType.toString().equals(other._castType.toString())) {
				Revision revision = new Revision(this, CTYPE, other._castType.toString(), _nodeType);
				modifications.add(revision);
			}
			String thisType = _expression.getType().toString();
			if(!this.equals("?") && other._expression.getType().toString().equals(thisType)) {
				Map<SName, Pair<String, String>> record = NodeUtils.tryReplaceAllVariables(other._expression, varTrans, allUsableVariables);
				if(record != null) {
					NodeUtils.replaceVariable(record);
					String target = other._expression.toSrcString().toString();
					if(!_expression.toSrcString().toString().equals(target)) {
						Revision revision = new Revision(this, EXPR, target, _nodeType);
						modifications.add(revision);
					}
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
			Revision revision = (Revision) modification;
			switch(revision.getSourceID()) {
			case WHOLE:
				_replace_whole = revision.getTargetString();
				break;
			case EXPR:
				_replace_expression = revision.getTargetString();
				break;
			case CTYPE:
				_replace_type = revision.getTargetString();
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
			Revision revision = (Revision) modification;
			switch(revision.getSourceID()) {
			case WHOLE:
				_replace_whole = null;
				break;
			case EXPR:
				_replace_expression = null;
				break;
			case CTYPE:
				_replace_type = null;
				break;
			default:
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean backup(Modification modification) {
		return true;
	}
	
	@Override
	public StringBuffer toSrcString() {
		StringBuffer stringBuffer = new StringBuffer();
		if(_replace_whole != null) {
			stringBuffer.append(_replace_whole);
		} else {
			stringBuffer.append("(");
			if(_replace_type != null) {
				stringBuffer.append(_replace_type);
			} else {
				stringBuffer.append(_castType);
			}
			stringBuffer.append(")");
			if(_replace_expression != null){
				stringBuffer.append(_replace_expression);
			} else {
				stringBuffer.append(_expression.toSrcString());
			}
		}
		return stringBuffer;
	}

	@Override
	public List<Literal> getLiterals() {
		List<Literal> list = new LinkedList<>();
		list.addAll(_expression.getLiterals());
		return list;
	}

	@Override
	public List<Variable> getVariables() {
		List<Variable> list = new LinkedList<>();
		list.addAll(_expression.getVariables());
		return list;
	}
	
	@Override
	public List<CondStruct> getCondStruct() {
		List<CondStruct> list = new LinkedList<>();
		list.addAll(_expression.getCondStruct());
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
		return "(" + _castType + ")" + expr;
	}
}
