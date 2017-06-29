/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.common.node.stmt;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Type;

import cofix.common.node.Node;
import cofix.common.node.expr.Expr;
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
public class SuperConstructorInv extends Stmt {

	private Expr _expression = null;
	private Type _superType = null;
	private List<Expr> _arguments = null;
	
	private List<Expr> _arguments_replace = null;
	
	/**
	 * SuperConstructorInvocation:
     *	[ Expression . ]
     *	    [ < Type { , Type } > ]
     *	    super ( [ Expression { , Expression } ] ) ;
	 */
	public SuperConstructorInv(int startLine, int endLine, ASTNode node) {
		this(startLine, endLine, node, null);
	}

	public SuperConstructorInv(int startLine, int endLine, ASTNode node, Node parent) {
		super(startLine, endLine, node, parent);
	}
	
	public void setExpression(Expr expression){
		_expression = expression;
	}
	
	public void setSuperType(Type type){
		_superType = type;
	}
	
	public void setArguments(List<Expr> arguments){
		_arguments = arguments;
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
		if(_expression != null){
			stringBuffer.append(_expression.toSrcString());
			stringBuffer.append(".");
		}
		stringBuffer.append("super(");
		if(_arguments_replace != null){
			if(_arguments_replace.size() > 0){
				stringBuffer.append(_arguments_replace.get(0).toSrcString());
				for(int i= 1; i < _arguments_replace.size(); i++){
					stringBuffer.append(",");
					stringBuffer.append(_arguments_replace.get(i).toSrcString());
				}
			}
		}else if(_arguments != null && _arguments.size() > 0){
			stringBuffer.append(_arguments.get(0).toSrcString());
			for(int i= 1; i < _arguments.size(); i++){
				stringBuffer.append(",");
				stringBuffer.append(_arguments.get(i).toSrcString());
			}
		}
		stringBuffer.append(");");
		return null;
	}

	@Override
	public List<Literal> getLiterals() {
		List<Literal> list = new LinkedList<>();
		if(_expression != null){
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
		MethodCall methodCall = new MethodCall(this);
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
		if(_arguments != null){
			for(Expr expr : _arguments){
				list.addAll(expr.getOperators());
			}
		}
		return list;
	}
}
