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
public class Blk extends Stmt {

	private List<Stmt> _statements = null;
	
	private List<Stmt> _statements_replace = null;
	/**
	 * Block:
     *	{ { Statement } }
	 */
	public Blk(int startLine, int endLine, ASTNode node) {
		this(startLine, endLine, node, null);
	}
	
	public Blk(int startLine, int endLine, ASTNode node, Node parent) {
		super(startLine, endLine, node, parent);
	}
	
	public void setStatement(List<Stmt> statements){
		_statements = statements;
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
		stringBuffer.append("{\n");
		if(_statements_replace != null){
			for(Stmt stmt : _statements_replace){
				stringBuffer.append(stmt.toSrcString());
				stringBuffer.append("\n");
			}
		} else {
			for(Stmt stmt : _statements){
				stringBuffer.append(stmt.toSrcString());
				stringBuffer.append("\n");
			}
		}
		stringBuffer.append("}\n");
		return null;
	}

	@Override
	public List<Literal> getLiterals() {
		List<Literal> list = new LinkedList<>();;
		if(_statements != null){
			for(Stmt stmt : _statements){
				list.addAll(stmt.getLiterals());
			}
		}
		return list;
	}

	@Override
	public List<Variable> getVariables() {
		List<Variable> list = new LinkedList<>();;
		if(_statements != null){
			for(Stmt stmt : _statements){
				list.addAll(stmt.getVariables());
			}
		}
		return list;
	}

	@Override
	public List<LoopStruct> getLoopStruct() {
		List<LoopStruct> list = new LinkedList<>();;
		if(_statements != null){
			for(Stmt stmt : _statements){
				list.addAll(stmt.getLoopStruct());
			}
		}
		return list;
	}
	
	@Override
	public List<CondStruct> getCondStruct() {
		List<CondStruct> list = new LinkedList<>();;
		if(_statements != null){
			for(Stmt stmt : _statements){
				list.addAll(stmt.getCondStruct());
			}
		}
		return list;
	}

	@Override
	public List<MethodCall> getMethodCalls() {
		List<MethodCall> list = new LinkedList<>();;
		if(_statements != null){
			for(Stmt stmt : _statements){
				list.addAll(stmt.getMethodCalls());
			}
		}
		return list;
	}

	@Override
	public List<Operator> getOperators() {
		List<Operator> list = new LinkedList<>();;
		if(_statements != null){
			for(Stmt stmt : _statements){
				list.addAll(stmt.getOperators());
			}
		}
		return list;
	}
	
	@Override
	public List<OtherStruct> getOtherStruct() {
		List<OtherStruct> list = new LinkedList<>();;
		if(_statements != null){
			for(Stmt stmt : _statements){
				list.addAll(stmt.getOtherStruct());
			}
		}
		return list;
	}
}
