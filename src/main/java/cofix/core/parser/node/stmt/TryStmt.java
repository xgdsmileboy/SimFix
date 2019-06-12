/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.core.parser.node.stmt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.TryStatement;
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
import cofix.core.parser.node.Node;

/**
 * @author Jiajun
 * @date Jun 23, 2017
 */
public class TryStmt extends Stmt {

	private Blk _blk = null;
	
	/**
	 * TryStatement:
     *	try [ ( Resources ) ]
     *	    Block
     *	    [ { CatchClause } ]
     *	    [ finally Block ]
	 */
	public TryStmt(int startLine, int endLine, ASTNode node) {
		this(startLine, endLine, node, null);
	}

	public TryStmt(int startLine, int endLine, ASTNode node, Node parent) {
		super(startLine, endLine, node, parent);
		_nodeType = TYPE.TRY;
	}
	
	public void setBody(Blk blk){
		_blk = blk;
	}
	
	@Override
	public boolean match(Node node, Map<String, String> varTrans, Map<String, Type> allUsableVariables, List<Modification> modifications) {
		boolean match = false;
		if(node instanceof TryStmt){
			match = true;
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
		StringBuffer stringBuffer = new StringBuffer("try");
		TryStatement tryStatement = (TryStatement)_originalNode;
		if(tryStatement.resources() != null && tryStatement.resources().size() > 0){
			stringBuffer.append("(");
			for(Object object : tryStatement.resources()){
				stringBuffer.append(object.toString());
			}
			stringBuffer.append(")");
		}
		stringBuffer.append(_blk.toSrcString());
		if(tryStatement.catchClauses() != null){
			for(Object object : tryStatement.catchClauses()){
				stringBuffer.append(object.toString());
			}
		}
		if(tryStatement.getFinally() != null){
			stringBuffer.append("finally");
			stringBuffer.append(tryStatement.getFinally().toString());
		}
		return stringBuffer;
	}

	@Override
	public List<Literal> getLiterals() {
		return _blk.getLiterals();
	}

	@Override
	public List<Variable> getVariables() {
		return _blk.getVariables();
	}

	@Override
	public List<LoopStruct> getLoopStruct() {
		return _blk.getLoopStruct();
	}
	
	@Override
	public List<CondStruct> getCondStruct() {
		return _blk.getCondStruct();
	}

	@Override
	public List<MethodCall> getMethodCalls() {
		return _blk.getMethodCalls();
	}

	@Override
	public List<Operator> getOperators() {
		return _blk.getOperators();
	}

	@Override
	public List<OtherStruct> getOtherStruct() {
		return _blk.getOtherStruct();
	}
	
	@Override
	public void computeFeatureVector() {
		_fVector = new NewFVector();
		_fVector.combineFeature(_blk.getFeatureVector());
	}

	@Override
	public USE_TYPE getUseType(Node child) {
		return USE_TYPE.USE_TRY;
	}
	
	@Override
	public List<Node> getChildren() {
		List<Node> list = new ArrayList<>();
		list.add(_blk);
		return list;
	}
	
	@Override
	public String simplify(Map<String, String> varTrans, Map<String, Type> allUsableVariables) {
		String body = _blk.simplify(varTrans, allUsableVariables);
		if(body == null){
			return null;
		}
		StringBuffer stringBuffer = new StringBuffer("try");
		TryStatement tryStatement = (TryStatement)_originalNode;
		if(tryStatement.resources() != null && tryStatement.resources().size() > 0){
			stringBuffer.append("(");
			for(Object object : tryStatement.resources()){
				stringBuffer.append(object.toString());
			}
			stringBuffer.append(")");
		}
		stringBuffer.append(body);
		if(tryStatement.catchClauses() != null){
			for(Object object : tryStatement.catchClauses()){
				stringBuffer.append(object.toString());
			}
		}
		if(tryStatement.getFinally() != null){
			stringBuffer.append("finally");
			stringBuffer.append(tryStatement.getFinally().toString());
		}
		return stringBuffer.toString();
	}
}
