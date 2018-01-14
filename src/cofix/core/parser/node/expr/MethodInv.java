/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.core.parser.node.expr;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
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
public class MethodInv extends Expr {

	private Expr _expression = null;
	private String _name = null;
	private List<Expr> _arguments = null;
	
	private String _expression_replace = null;
	private String _name_replace = null;
	private String _arguments_replace = null;
	private String _replace = null;
	
	private final int EXPRID = 0;
	private final int NAMEID = 1;
	private final int ARGID = 2;
	private final int WHOLE = 3;
	
	private static Set<String> _avoid = new HashSet<>(); 
	static{
		_avoid.add("equals");
		_avoid.add("toString");
		_avoid.add("hash");
		_avoid.add("clone");
	}
	
	/**
	 *  MethodInvocation:
     *  [ Expression . ]
     *    [ < Type { , Type } > ]
     *    Identifier ( [ Expression { , Expression } ] )
	 */
	public MethodInv(int startLine, int endLine, ASTNode node) {
		super(startLine, endLine, node);
		_nodeType = TYPE.MINVOCATION;
	}
	
	public void setExpression(Expr expression){
		_expression = expression;
	}
	
	public void setName(String name){
		_name = name;
	}
	
	public void setArguments(List<Expr> arguments){
		_arguments = arguments;
	}
	
	public Expr getExpression(){
		return _expression;
	}

	@Override
	public boolean match(Node node, Map<String, String> varTrans, Map<String, Type> allUsableVariables, List<Modification> modifications) {
		boolean match = false;
		if(node instanceof MethodInv){
			MethodInv other = (MethodInv) node;
			if(getType().toString().equals(other.getType().toString())){
				match = true;
				if(_expression != null && other._expression != null){
					List<Modification> tmp = new ArrayList<>();
					if(_expression.match(other._expression, varTrans, allUsableVariables, tmp)){
						modifications.addAll(tmp);
					}
				}
				if(_arguments.size() == other._arguments.size()){
					// TODO : types of arguments should be compatible
					boolean compatible = true;
					for(int i = 0; i < _arguments.size(); i++){
						if(!NodeUtils.compatibleType(_arguments.get(i).getType(), other._arguments.get(i).getType())){
							compatible = false;
							break;
						}
					}
					if(compatible && _arguments.size() > 0 && !_name.equals(other._name) && !MethodInv._avoid.contains(_name) && !MethodInv._avoid.contains(other._name) && _arguments.size() > 0){
						Revision revision = new Revision(this, NAMEID, other._name, _nodeType);
						modifications.add(revision);
					}
				}
				if(_name.equals(other._name)){
					modifications.addAll(NodeUtils.handleArguments(this, ARGID, _nodeType, _arguments, other._arguments, varTrans, allUsableVariables));
				}
			}
			
		} else {
			List<Modification> tmp = new ArrayList<>();
			if(node instanceof ConditionalExpr){
				ConditionalExpr conditionalExpr = (ConditionalExpr) node;
				if(NodeUtils.conditionalMatch(this, WHOLE, conditionalExpr, varTrans, allUsableVariables, tmp)){
					match = true;
					modifications.addAll(tmp);
				}
			} else {
				List<Node> children = node.getChildren();
				if(NodeUtils.nodeMatchList(this, children, varTrans, allUsableVariables, tmp)){
					match = true;
					modifications.addAll(tmp);
				}
			}
		}
		return match;
	}

	@Override
	public boolean adapt(Modification modification) {
		int id = modification.getSourceID();
		switch (id) {
		case EXPRID:
			_expression_replace = modification.getTargetString();
			break;
		case NAMEID:
			_name_replace = modification.getTargetString();
			break;
		case ARGID:
			_arguments_replace = modification.getTargetString();
			break;
		case WHOLE:
			_replace = modification.getTargetString();
			break;
		default:
			return false;
		}
		return true;
	}

	@Override
	public boolean restore(Modification modification) {
		int id = modification.getSourceID();
		switch (id) {
		case EXPRID:
			_expression_replace = null;
			break;
		case NAMEID:
			_name_replace = null;
			break;
		case ARGID:
			_arguments_replace = null;
			break;
		case WHOLE:
			_replace = null;
			break;
		default:
			return false;
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
		if(_replace != null){
			stringBuffer.append(_replace);
		} else {
			if(_expression_replace != null){
				stringBuffer.append(_expression_replace);
				stringBuffer.append(".");
			} else if(_expression != null){
				stringBuffer.append(_expression.toSrcString());
				stringBuffer.append(".");
			}
			if(_name_replace != null){
				stringBuffer.append(_name_replace);
			} else {
				stringBuffer.append(_name);
			}
			stringBuffer.append("(");
			if(_arguments_replace != null){
				stringBuffer.append(_arguments_replace);
			} else if(_arguments != null && _arguments.size() > 0){
				stringBuffer.append(_arguments.get(0).toSrcString());
				for(int i = 1; i < _arguments.size(); i++){
					stringBuffer.append(",");
					stringBuffer.append(_arguments.get(i).toSrcString());
				}
			}
			stringBuffer.append(")");
		}
		return stringBuffer;
	}

	@Override
	public List<Literal> getLiterals() {
		List<Literal> list = new LinkedList<>();
		if(_expression != null) {
			list.addAll(_expression.getLiterals());
		}
		if(_arguments != null){
			for(Expr expr : _arguments){
				list.addAll(expr.getLiterals());
			}
		}
		return list;
	}

	@Override
	public List<Variable> getVariables() {
		List<Variable> list = new LinkedList<>();
		if(_expression != null){
			list.addAll(_expression.getVariables());
		}
		if(_arguments != null){
			for(Expr expr : _arguments){
				list.addAll(expr.getVariables());
			}
		}
		return list;
	}

	@Override
	public List<MethodCall> getMethodCalls() {
		List<MethodCall> list = new LinkedList<>();
		MethodCall methodCall = new MethodCall(this, _name, _arguments);
		list.add(methodCall);
		if(_expression != null){
			list.addAll(_expression.getMethodCalls());
		}
		if(_arguments != null){
			for(Expr expr : _arguments){
				list.addAll(expr.getMethodCalls());
			}
		}
		return list;
	}

	@Override
	public List<Operator> getOperators() {
		List<Operator> list = new LinkedList<>();
		if(_expression != null){
			list.addAll(_expression.getOperators());
		}
		if(_arguments != null){
			for(Expr expr : _arguments){
				list.addAll(expr.getOperators());
			}
		}
		return list;
	}
	
	@Override
	public void computeFeatureVector() {
		_fVector = new NewFVector();
		if(_expression == null || !NodeUtils.skipMethodCall(_expression.toSrcString().toString(), _name)){
			_fVector.inc(NewFVector.INDEX_MCALL);
		}
		if(_expression != null){
			_fVector.combineFeature(_expression.getFeatureVector());
		}
		if(_arguments != null){
			for(Expr expr : _arguments){
				_fVector.combineFeature(expr.getFeatureVector());
			}
		}
	}
	
	@Override
	public USE_TYPE getUseType(Node child) {
		if(child == _expression){
			return USE_TYPE.USE_METHOD_EXP;
		} else {
			return USE_TYPE.USE_METHOD_PARAM;
		}
	}
	
	@Override
	public List<Node> getChildren() {
		List<Node> list = new ArrayList<>();
		if(_expression != null){
			list.add(_expression);
		}
		return list;
	}

	@Override
	public String simplify(Map<String, String> varTrans, Map<String, Type> allUsableVariables) {
		Map<SName, Pair<String, String>> record = NodeUtils.tryReplaceAllVariables(this, varTrans, allUsableVariables);
		if(record == null){
			return null;
		}
		NodeUtils.replaceVariable(record);
		String string = toSrcString().toString();
		NodeUtils.restoreVariables(record);
		return string;
	}
}
