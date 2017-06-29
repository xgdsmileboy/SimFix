/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.common.node;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Type;

import cofix.common.node.metric.CondStruct;
import cofix.common.node.metric.Literal;
import cofix.common.node.metric.MethodCall;
import cofix.common.node.metric.Operator;
import cofix.common.node.metric.OtherStruct;
import cofix.common.node.metric.LoopStruct;
import cofix.common.node.metric.Variable;
import cofix.common.node.modify.Modification;

/**
 * @author Jiajun
 * @datae Jun 23, 2017
 */
public abstract class Node implements Adapter {
	
	protected int _startLine = 0;
	protected int _endLine = 0;
	protected ASTNode _originalNode = null;
	protected Node _parent = null;
	
	protected Node(int startLine, int endLine, ASTNode node){
		this(startLine, endLine, node, null);
	}
	
	protected Node(int startLine, int endLine, ASTNode node, Node parent){
		_startLine = startLine;
		_endLine = endLine;
		_originalNode = node;
		_parent = parent;
	}
	
	public int getStartLine(){
		return _startLine;
	}
	
	public int getEndLine(){
		return _endLine;
	}
	
	public ASTNode getOriginalAST(){
		return _originalNode;
	}
	
	public Node getParent(){
		return _parent;
	}
	
	public void setParent(Node parent){
		_parent = parent;
	}
	
	public abstract boolean match(Node node, Map<String, Type> allUsableVariables, List<Modification> modifications);

	public abstract StringBuffer toSrcString();
	
	public abstract List<Literal> getLiterals();
	
	public abstract List<Variable> getVariables();
	
	public abstract List<LoopStruct> getLoopStruct();
	
	public abstract List<CondStruct> getCondStruct();
	
	public abstract List<OtherStruct> getOtherStruct();
	
	public abstract List<MethodCall> getMethodCalls();
	
	public abstract List<Operator> getOperators();
	
}
