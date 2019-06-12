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
import cofix.core.metric.Literal;
import cofix.core.metric.MethodCall;
import cofix.core.metric.NewFVector;
import cofix.core.metric.Operator;
import cofix.core.metric.Variable;
import cofix.core.metric.Variable.USE_TYPE;
import cofix.core.modify.Modification;
import cofix.core.parser.NodeUtils;
import cofix.core.parser.node.Node;
import sun.security.provider.MD2;

/**
 * @author Jiajun
 * @date Jun 23, 2017
 */
public class SuperMethodInv extends Expr {

	private Label _label = null;
	private String _name = null;
	private List<Expr> _arguments = null;
	
	private String _arguments_replace = null;
	private String _replace = null;
	
	private final int ARGID = 0;
	private final int WHOLE = 1;
	/**
	 * SuperMethodInvocation:
     *	[ ClassName . ] super .
     *    [ < Type { , Type } > ]
     *    Identifier ( [ Expression { , Expression } ] )
	 */
	public SuperMethodInv(int startLine, int endLine, ASTNode node) {
		super(startLine, endLine, node);
		_nodeType = TYPE.SMINVOCATION;
	}
	
	public void setLabel(Label label){
		_label = label;
	}
	
	public void setName(String name){
		_name = name;
	}
	
	public void setArguments(List<Expr> arguments){
		_arguments = arguments;
	}

	@Override
	public boolean match(Node node, Map<String, String> varTrans, Map<String, Type> allUsableVariables, List<Modification> modifications) {
		boolean match = false;
		if(node instanceof SuperMethodInv){
			match = true;
			SuperMethodInv other = (SuperMethodInv) node;
			modifications.addAll(NodeUtils.handleArguments(this, ARGID, _nodeType, _arguments, other._arguments, varTrans, allUsableVariables));
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
		if(modification.getSourceID() == ARGID){
			_arguments_replace = modification.getTargetString();
			return true;
		} else if(modification.getSourceID() == WHOLE){
			_replace = modification.getTargetString();
			return true;
		}
		return false;
	}

	@Override
	public boolean restore(Modification modification) {
		if(modification.getSourceID() == ARGID){
			_arguments_replace = null;
		}else if(modification.getSourceID() == WHOLE){
			_replace = null;
		} else {
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
		if(_replace != null){
			return new StringBuffer(_replace);
		}
		StringBuffer stringBuffer = new StringBuffer();
		if(_label != null){
			stringBuffer.append(_label.toSrcString());
			stringBuffer.append(".");
		}
		stringBuffer.append("super.");
		stringBuffer.append(_name);
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
		return stringBuffer;
	}

	@Override
	public List<Literal> getLiterals() {
		List<Literal> list = new LinkedList<>();
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
		if(_label != null){
			list.addAll(_label.getVariables());
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
		if(_label != null){
			list.addAll(_label.getMethodCalls());
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
		_fVector.inc(NewFVector.INDEX_MCALL);
		if(_label != null){
			_fVector.combineFeature(_label.getFeatureVector());
		}
		if(_arguments != null){
			for(Expr expr : _arguments){
				_fVector.combineFeature(expr.getFeatureVector());
			}
		}
	}

	@Override
	public USE_TYPE getUseType(Node child) {
		if(_label == child){
			return USE_TYPE.USE_METHOD_EXP;
		} else {
			return USE_TYPE.USE_METHOD_PARAM;
		}
	}
	
	@Override
	public List<Node> getChildren() {
		return new ArrayList<>();
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
