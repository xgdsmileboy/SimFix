/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.core.parser.node.stmt;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;

import cofix.core.metric.CondStruct;
import cofix.core.metric.Literal;
import cofix.core.metric.LoopStruct;
import cofix.core.metric.MethodCall;
import cofix.core.metric.Operator;
import cofix.core.metric.OtherStruct;
import cofix.core.metric.Variable;
import cofix.core.metric.Variable.USE_TYPE;
import cofix.core.parser.node.CodeBlock;
import cofix.core.parser.node.Node;

/**
 * @author Jiajun
 * @date Jun 23, 2017
 */
public abstract class Stmt extends Node{
	
	protected Stmt(int startLine, int endLine, ASTNode node, Node parent) {
		super(startLine, endLine, node, parent);
	}
	
	@Override
	public List<Literal> getLiterals() {
		return new LinkedList<>();
	}

	@Override
	public List<Variable> getVariables() {
		return new LinkedList<>();
	}

	@Override
	public List<LoopStruct> getLoopStruct() {
		return new LinkedList<>();
	}
	
	@Override
	public List<CondStruct> getCondStruct() {
		return new LinkedList<>();
	}

	@Override
	public List<MethodCall> getMethodCalls() {
		return new LinkedList<>();
	}

	@Override
	public List<Operator> getOperators() {
		return new LinkedList<>();
	}

	@Override
	public List<OtherStruct> getOtherStruct() {
		return new LinkedList<>();
	}
	

	@Override
	public USE_TYPE getUseType(Node child) {
		if(_parent == null){
			return USE_TYPE.USE_UNKNOWN;
		} else {
			return _parent.getUseType(this);
		}
	}
	
	@Override
	public List<CodeBlock> reduce() {
		List<CodeBlock> linkedList = new LinkedList<>();
		for(Node node : getChildren()){
			linkedList.addAll(node.reduce());
		}
		return linkedList;
	}
}
