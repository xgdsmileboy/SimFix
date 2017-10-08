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
import cofix.core.metric.LoopStruct;
import cofix.core.metric.MethodCall;
import cofix.core.metric.NewFVector;
import cofix.core.metric.Operator;
import cofix.core.metric.OtherStruct;
import cofix.core.metric.Variable;
import cofix.core.metric.Variable.USE_TYPE;
import cofix.core.modify.Modification;
import cofix.core.parser.NodeUtils;
import cofix.core.parser.node.CodeBlock;
import cofix.core.parser.node.Node;
import cofix.core.parser.node.expr.Expr;
import cofix.core.parser.node.expr.Svd;

/**
 * @author Jiajun
 * @date Jun 23, 2017
 */
public class EnhancedForStmt extends Stmt {

	private Svd _varDecl = null;
	private Expr _expression = null;
	private Stmt _statement = null;
	
	private Stmt _statement_replace = null;
	
	/**
	 * EnhancedForStatement:
     *	for ( FormalParameter : Expression )
     *	                   Statement
	 */
	public EnhancedForStmt(int startLine, int endLine, ASTNode node) {
		this(startLine, endLine, node, null);
	}
	
	public EnhancedForStmt(int startLine, int endLine, ASTNode node, Node parent) {
		super(startLine, endLine, node, parent);
		_nodeType = TYPE.EFOR;
	}
	
	public void setParameter(Svd varDecl){
		_varDecl = varDecl;
	}
	
	public void setExpression(Expr expression){
		_expression = expression;
	}
	
	public void setBody(Stmt statement){
		_statement = statement;
	}

	@Override
	public boolean match(Node node, Map<String, String> varTrans, Map<String, Type> allUsableVariables, List<Modification> modifications) {
		boolean match = false;
		if(node instanceof EnhancedForStmt){
			match = true;
			EnhancedForStmt other = (EnhancedForStmt) node;
			String name = _varDecl.getName().getName();
			String origin = varTrans.get(name);
			varTrans.put(name, other._varDecl.getName().getName());
			List<Modification> tmp = new ArrayList<>();
			if(_statement.match(other._statement, varTrans, allUsableVariables, tmp)){
				modifications.addAll(tmp);
			}
			if(origin != null){
				varTrans.put(name, origin);
			}
		} else {
			List<Node> children = node.getChildren();
			List<Modification> tmp = new ArrayList<>();
			if(NodeUtils.nodeMatchList(this, children, varTrans, allUsableVariables, tmp)){
				match = true;
				modifications.addAll(tmp);
			}
			if(!match){
				tmp = new ArrayList<>();
				if(NodeUtils.nodeMatchList(_statement, children, varTrans, allUsableVariables, tmp)){
					match = true;
					modifications.addAll(tmp);
				}
			}
		}
		return match;
	}

	@Override
	public boolean adapt(Modification modification) {
		return false;
	}

	@Override
	public boolean restore(Modification modification) {
		return false;
	}

	@Override
	public boolean backup(Modification modification) {
		return false;
	}
	
	@Override
	public StringBuffer toSrcString() {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("for(");
		stringBuffer.append(_varDecl.toSrcString());
		stringBuffer.append(" : ");
		stringBuffer.append(_expression.toSrcString());
		stringBuffer.append(")");
		if(_statement_replace != null){
			stringBuffer.append(_statement_replace.toSrcString());
		} else {
			stringBuffer.append(_statement.toSrcString());
		}
		return stringBuffer;
	}

	@Override
	public List<Literal> getLiterals() {
		List<Literal> list = _varDecl.getLiterals();
		list.addAll(_expression.getLiterals());
		if(_statement != null){
			list.addAll(_statement.getLiterals());
		}
		return list;
	}

	@Override
	public List<Variable> getVariables() {
		List<Variable> list = _varDecl.getVariables();
		list.addAll(_expression.getVariables());
		if(_statement != null){
			list.addAll(_statement.getVariables());
		}
		return list;
	}

	@Override
	public List<LoopStruct> getLoopStruct() {
		List<LoopStruct> list = new LinkedList<>();
		LoopStruct loopStruct = new LoopStruct(this, LoopStruct.KIND.EFOR);
		list.add(loopStruct);
		if(_statement != null){
			list.addAll(_statement.getLoopStruct());
		}
		return list;
	}
	
	@Override
	public List<CondStruct> getCondStruct() {
		List<CondStruct> list = new LinkedList<>();
		if(_statement != null){
			list.addAll(_statement.getCondStruct());
		}
		return list;
	}

	@Override
	public List<MethodCall> getMethodCalls() {
		List<MethodCall> list = _expression.getMethodCalls();
		if(_statement != null){
			list.addAll(_statement.getMethodCalls());
		}
		return list;
	}

	@Override
	public List<Operator> getOperators() {
		List<Operator> list = _expression.getOperators();
		if(_statement != null){
			list.addAll(_statement.getOperators());
		}
		return list;
	}
	
	@Override
	public List<OtherStruct> getOtherStruct() {
		List<OtherStruct> list = new LinkedList<>();
		if(_statement != null){
			list.addAll(_statement.getOtherStruct());
		}
		return list;
	}
	
	@Override
	public void computeFeatureVector() {
		_fVector = new NewFVector();
		_fVector.inc(NewFVector.INDEX_STRUCT_ENFOR);
		_fVector.combineFeature(_varDecl.getFeatureVector());
		_fVector.combineFeature(_expression.getFeatureVector());
		_fVector.combineFeature(_statement.getFeatureVector());
	}

	@Override
	public USE_TYPE getUseType(Node child) {
		return USE_TYPE.USE_LOOP;
	}
	
	@Override
	public List<Node> getChildren() {
		List<Node> list = new ArrayList<>();
		list.add(_statement);
		return list;
	}
	
	@Override
	public String simplify(Map<String, String> varTrans, Map<String, Type> allUsableVariables) {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("for(");
		String vd = _varDecl.simplify(varTrans, allUsableVariables);
		if(vd == null){
			return null;
		}
		stringBuffer.append(vd);
		stringBuffer.append(" : ");
		String expr = _expression.simplify(varTrans, allUsableVariables);
		if(expr == null){
			return null;
		}
		stringBuffer.append(expr);
		stringBuffer.append(")");
		String body = _statement.simplify(varTrans, allUsableVariables);
		if(body == null){
			return null;
		}
		stringBuffer.append(body);
		return stringBuffer.toString();
	}
	
	@Override
	public List<CodeBlock> reduce() {
		List<CodeBlock> list = new LinkedList<>();
		if(_statement != null){
			list.addAll(_statement.reduce());
		}
		List<ASTNode> nodes = new LinkedList<>();
		nodes.add(_originalNode);
		CodeBlock codeBlock = new CodeBlock(null, null, nodes);
		list.add(codeBlock);
		return list;
	}
}
