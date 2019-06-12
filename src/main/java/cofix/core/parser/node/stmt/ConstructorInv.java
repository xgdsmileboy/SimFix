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

import cofix.common.util.Pair;
import cofix.core.metric.CondStruct;
import cofix.core.metric.Literal;
import cofix.core.metric.MethodCall;
import cofix.core.metric.NewFVector;
import cofix.core.metric.Operator;
import cofix.core.metric.Variable;
import cofix.core.metric.Variable.USE_TYPE;
import cofix.core.modify.Modification;
import cofix.core.parser.NodeUtils;
import cofix.core.parser.node.Node;
import cofix.core.parser.node.expr.Expr;
import cofix.core.parser.node.expr.SName;

/**
 * @author Jiajun
 * @date Jun 23, 2017
 */
public class ConstructorInv  extends Stmt{

	private Type _thisType = null;
	private List<Expr> _arguments = null;
	
	private String _arguments_replace = null;
	
	private int ARGID = 0;
	
	/**
	 * ConstructorInvocation:
     *	[ < Type { , Type } > ]
     *	       this ( [ Expression { , Expression } ] ) ;
	 */
	public ConstructorInv(int startLine, int endLine, ASTNode node) {
		this(startLine, endLine, node, null);
		_nodeType = TYPE.CONSTRUCTORINV;
	}
	
	public ConstructorInv(int startLine, int endLine, ASTNode node, Node parent) {
		super(startLine, endLine, node, parent);
	}
	
	public void setThisType(Type thisType){
		_thisType = thisType;
	}
	
	public void setArguments(List<Expr> arguments){
		_arguments = arguments;
	}

	@Override
	public boolean match(Node node, Map<String, String> varTrans, Map<String, Type> allUsableVariables, List<Modification> modifications) {
		boolean match = false;
		if(node instanceof ConstructorInv){
			match = true;
			ConstructorInv other = (ConstructorInv) node;
			modifications.addAll(NodeUtils.handleArguments(this, ARGID, _nodeType, _arguments, other._arguments, varTrans, allUsableVariables));
		} else {
			List<Node> children = node.getChildren();
			List<Modification> tmp = new ArrayList<>();
			if(NodeUtils.nodeMatchList(this, children, varTrans, allUsableVariables, tmp)){
				match = true;
				modifications.addAll(tmp);
			}
		}
		return match;
	}

	@Override
	public boolean adapt(Modification modification) {
		_arguments_replace = modification.getTargetString();
		return true;
	}

	@Override
	public boolean restore(Modification modification) {
		_arguments_replace = null;
		return true;
	}

	@Override
	public boolean backup(Modification modification) {
		return false;
	}
	
	@Override
	public StringBuffer toSrcString() {
		StringBuffer stringBuffer = new StringBuffer("this(");
		if(_arguments_replace != null){
			stringBuffer.append(_arguments_replace);
		} else if(_arguments != null && _arguments.size() > 0){
			stringBuffer.append(_arguments.get(0).toSrcString());
			for(int i = 1; i < _arguments.size(); i++){
				stringBuffer.append(",");
				stringBuffer.append(_arguments.get(i).toSrcString());
			}
		}
		stringBuffer.append(");");
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
		if(_arguments != null){
			for(Expr expr : _arguments){
				list.addAll(expr.getVariables());
			}
		}
		return list;
	}
	
	@Override
	public List<CondStruct> getCondStruct() {
		List<CondStruct> list = new LinkedList<>();
		if(_arguments != null){
			for(Expr expr : _arguments){
				list.addAll(expr.getCondStruct());
			}
		}
		return list;
	}

	@Override
	public List<MethodCall> getMethodCalls() {
		List<MethodCall> list = new LinkedList<>();
		MethodCall methodCall = new MethodCall(this, "this", _arguments);
		list.add(methodCall);
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
		if(_arguments != null){
			for(Expr expr : _arguments){
				_fVector.combineFeature(expr.getFeatureVector());
			}
		}
	}
	
	@Override
	public USE_TYPE getUseType(Node child) {
		return USE_TYPE.USE_METHOD_PARAM;
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
