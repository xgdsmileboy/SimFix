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
public class DoStmt extends Stmt {

	private Stmt _stmt = null;
	private Expr _expression = null;
	
	private Stmt _stmt_replace = null;
	private Expr _expression_replace = null;
	
	/**
	 * DoStatement:
     *	do Statement while ( Expression ) ;
	 */
	public DoStmt(int startLine, int endLine, ASTNode node) {
		this(startLine, endLine, node, null);
		_nodeType = TYPE.DO;
	}

	public DoStmt(int startLine, int endLine, ASTNode node, Node parent) {
		super(startLine, endLine, node, parent);
	}
	
	public void setBody(Stmt stmt){
		_stmt = stmt;
	}
	
	public void setExpression(Expr expression){
		_expression = expression;
	}
	
	@Override
	public boolean match(Node node, Map<String, String> varTrans, Map<String, Type> allUsableVariables, List<Modification> modifications) {
		boolean match = false;
		if(node instanceof DoStmt){
			match = true;
			DoStmt other = (DoStmt) node;
			List<Modification> tmp = new ArrayList<>();
			if(_expression.match(other._expression, varTrans, allUsableVariables, tmp)){
				modifications.addAll(tmp);
			}
			
			tmp = new ArrayList<>();
			if(_stmt.match(other._stmt, varTrans, allUsableVariables, tmp)){
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
				if(NodeUtils.nodeMatchList(_stmt, children, varTrans, allUsableVariables, tmp)){
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
		stringBuffer.append("do ");
		if(_stmt_replace != null){
			stringBuffer.append(_stmt_replace.toSrcString());
		} else {
			stringBuffer.append(_stmt.toSrcString());
		}
		stringBuffer.append(" while(");
		if(_expression_replace != null){
			stringBuffer.append(_expression_replace.toSrcString());
		} else {
			stringBuffer.append(_expression.toSrcString());
		}
		stringBuffer.append(");");
		return stringBuffer;
	}

	@Override
	public List<Literal> getLiterals() {
		List<Literal> list = _expression.getLiterals();
		if(_stmt != null){
			list.addAll(_stmt.getLiterals());
		}
		return list;
	}

	@Override
	public List<Variable> getVariables() {
		List<Variable> list = _expression.getVariables();
		if(_stmt != null){
			list.addAll(_stmt.getVariables());
		}
		return list;
	}

	@Override
	public List<LoopStruct> getLoopStruct() {
		List<LoopStruct> list = new LinkedList<>();
		LoopStruct loopStruct = new LoopStruct(this, LoopStruct.KIND.DO);
		list.add(loopStruct);
		if(_stmt != null){
			list.addAll(_stmt.getLoopStruct());
		}
		return list;
	}
	
	@Override
	public List<CondStruct> getCondStruct() {
		List<CondStruct> list = new ArrayList<>();
		if(_stmt != null){
			list.addAll(_stmt.getCondStruct());
		}
		return list;
	}

	@Override
	public List<MethodCall> getMethodCalls() {
		List<MethodCall> list = _expression.getMethodCalls();
		if(_stmt != null){
			list.addAll(_stmt.getMethodCalls());
		}
		return list;
	}

	@Override
	public List<Operator> getOperators() {
		List<Operator> list = _expression.getOperators();
		if(_stmt != null){
			list.addAll(_stmt.getOperators());
		}
		return list;
	}
	
	@Override
	public List<OtherStruct> getOtherStruct() {
		List<OtherStruct> list = new LinkedList<>();
		if(_stmt != null){
			list.addAll(_stmt.getOtherStruct());
		}
		return list;
	}
	
	@Override
	public void computeFeatureVector() {
		_fVector = new NewFVector();
		_fVector.inc(NewFVector.INDEX_STRUCT_DO);
		_fVector.combineFeature(_expression.getFeatureVector());
		_fVector.combineFeature(_stmt.getFeatureVector());
	}

	@Override
	public USE_TYPE getUseType(Node child) {
		return USE_TYPE.USE_LOOP;
	}
	
	@Override
	public List<Node> getChildren() {
		List<Node> list = new ArrayList<>();
		list.add(_stmt);
		return list;
	}
	
	@Override
	public String simplify(Map<String, String> varTrans, Map<String, Type> allUsableVariables) {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("do ");
		String body = _stmt.simplify(varTrans, allUsableVariables);
		if(body == null){
			return null;
		}
		stringBuffer.append(body);
		stringBuffer.append(" while(");
		String cond = _expression.simplify(varTrans, allUsableVariables);
		if(cond == null){
			return null;
		}
		stringBuffer.append(cond);
		stringBuffer.append(");");
		return stringBuffer.toString();
	}
	
	@Override
	public List<CodeBlock> reduce() {
		List<CodeBlock> list = new LinkedList<>();
		if(_stmt != null){
			list.addAll(_stmt.reduce());
		}
		List<ASTNode> nodes = new LinkedList<>();
		nodes.add(_originalNode);
		CodeBlock codeBlock = new CodeBlock(null, null, nodes);
		list.add(codeBlock);
		return  list;
	}
	
}
