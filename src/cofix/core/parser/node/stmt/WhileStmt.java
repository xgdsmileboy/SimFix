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

/**
 * @author Jiajun
 * @date Jun 23, 2017
 */
public class WhileStmt extends Stmt {

	private Expr _expression = null;
	private Stmt _body = null;
	
	private Expr _expression_replace = null;
	private Stmt _body_replace = null;
	
	/**
	 * WhileStatement:
     *	while ( Expression ) Statement
	 */
	public WhileStmt(int startLine, int endLine, ASTNode node) {
		this(startLine, endLine, node, null);
	}

	public WhileStmt(int startLine, int endLine, ASTNode node, Node parent) {
		super(startLine, endLine, node, parent);
		_nodeType = TYPE.WHILE;
	}
	
	public void setExpression(Expr expression){
		_expression = expression;
	}
	
	public void setBody(Stmt body){
		_body = body;
	}
	
	@Override
	public boolean match(Node node, Map<String, String> varTrans, Map<String, Type> allUsableVariables, List<Modification> modifications) {
		boolean match = false;
		if(node instanceof WhileStmt){
			match = true;
			WhileStmt other = (WhileStmt) node;
			List<Modification> tmp = new ArrayList<>();
			if(_expression.match(other._expression, varTrans, allUsableVariables, tmp)){
				modifications.addAll(tmp);
			}
			tmp = new ArrayList<>();
			if(_body.match(other._body, varTrans, allUsableVariables, tmp)){
				modifications.addAll(tmp);
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
				if(NodeUtils.nodeMatchList(_body, children, varTrans, allUsableVariables, tmp)){
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
		StringBuffer stringBuffer = new StringBuffer("while(");
		if(_expression_replace != null){
			stringBuffer.append(_expression_replace.toSrcString());
		} else {
			stringBuffer.append(_expression.toSrcString());
		}
		stringBuffer.append(")");
		if(_body_replace != null){
			stringBuffer.append(_body_replace.toSrcString());
		} else {
			stringBuffer.append(_body.toSrcString());
		}
		return stringBuffer;
	}

	@Override
	public List<Literal> getLiterals() {
		List<Literal> list = _expression.getLiterals();
		list.addAll(_body.getLiterals());
		return list;
	}

	@Override
	public List<Variable> getVariables() {
		List<Variable> list = _expression.getVariables();
		list.addAll(_body.getVariables());
		return list;
	}

	@Override
	public List<LoopStruct> getLoopStruct() {
		List<LoopStruct> list = new LinkedList<>();
		LoopStruct loopStruct = new LoopStruct(this, LoopStruct.KIND.WHILE);
		list.add(loopStruct);
		list.addAll(_body.getLoopStruct());
		return list;
	}
	
	@Override
	public List<CondStruct> getCondStruct() {
		List<CondStruct> list = new LinkedList<>();
		list.addAll(_body.getCondStruct());
		return list;
	}

	@Override
	public List<MethodCall> getMethodCalls() {
		List<MethodCall> list = _expression.getMethodCalls();
		list.addAll(_body.getMethodCalls());
		return list;
	}

	@Override
	public List<Operator> getOperators() {
		List<Operator> list = _expression.getOperators();
		list.addAll(_body.getOperators());
		return list;
	}

	@Override
	public List<OtherStruct> getOtherStruct() {
		List<OtherStruct> list = new LinkedList<>();
		list.addAll(_body.getOtherStruct());
		return list;
	}
	
	@Override
	public void computeFeatureVector() {
		_fVector = new NewFVector();
		_fVector.inc(NewFVector.INDEX_STRUCT_WHILE);
		_fVector.combineFeature(_expression.getFeatureVector());
		_fVector.combineFeature(_body.getFeatureVector());
	}

	@Override
	public USE_TYPE getUseType(Node child) {
		return USE_TYPE.USE_LOOP;
	}
	
	@Override
	public List<Node> getChildren() {
		List<Node> list = new ArrayList<>();
		list.add(_body);
		return list;
	}
	
	@Override
	public String simplify(Map<String, String> varTrans, Map<String, Type> allUsableVariables) {
		StringBuffer stringBuffer = new StringBuffer("while(");
		String cond = _expression.simplify(varTrans, allUsableVariables);
		if(cond == null){
			return null;
		}
		stringBuffer.append(cond);
		stringBuffer.append(")");
		String body = _body.simplify(varTrans, allUsableVariables);
		if(body == null){
			return null;
		}
		stringBuffer.append(body);
		return stringBuffer.toString();
	}
	
	@Override
	public List<CodeBlock> reduce() {
		List<CodeBlock> list = new LinkedList<>();
		if(_body != null){
			list.addAll(_body.reduce());
		}
		List<ASTNode> nodes = new LinkedList<>();
		nodes.add(_originalNode);
		CodeBlock codeBlock = new CodeBlock(null, null, nodes);
		list.add(codeBlock);
		return list;
	}
}
