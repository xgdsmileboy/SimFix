/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.core.parser.node.stmt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.util.ISignatureAttribute;

import cofix.common.util.Pair;
import cofix.core.metric.CondStruct;
import cofix.core.metric.Literal;
import cofix.core.metric.LoopStruct;
import cofix.core.metric.MethodCall;
import cofix.core.metric.NewFVector;
import cofix.core.metric.Operator;
import cofix.core.metric.OtherStruct;
import cofix.core.metric.Variable;
import cofix.core.modify.Deletion;
import cofix.core.modify.Insertion;
import cofix.core.modify.Modification;
import cofix.core.modify.Revision;
import cofix.core.parser.NodeUtils;
import cofix.core.parser.node.Node;
import cofix.core.parser.node.expr.SName;

/**
 * @author Jiajun
 * @date Jun 23, 2017
 */
public class Blk extends Stmt {

	private List<Stmt> _statements = null;
	
	private String _replace = null;
	
	private Map<Integer, List<String>> _insertions = new HashMap<>();
	private Set<Integer> _deletions = new HashSet<>(); 
	
	private int WHOLE = 10000;
	
	/**
	 * Block:
     *	{ { Statement } }
	 */
	public Blk(int startLine, int endLine, ASTNode node) {
		this(startLine, endLine, node, null);
		_nodeType = TYPE.BLOCK;
	}
	
	public Blk(int startLine, int endLine, ASTNode node, Node parent) {
		super(startLine, endLine, node, parent);
	}
	
	public void setStatement(List<Stmt> statements){
		_statements = statements;
	}

	@Override
	public boolean match(Node node, Map<String, String> varTrans, Map<String, Type> allUsableVariables, List<Modification> modifications) {
		boolean match = false;
		if(node instanceof Blk){
			match = true;
			Blk other = (Blk) node;
			modifications.addAll(NodeUtils.listNodeMatching(this, _nodeType, _statements, other._statements, varTrans, allUsableVariables));
		}
		return match;
	}

	@Override
	public boolean adapt(Modification modification) {
		int index = modification.getSourceID();
		if(index == WHOLE){
			_replace = modification.getTargetString();
		} else if(modification instanceof Insertion){
			List<String> list = _insertions.get(modification.getSourceID());
			if(list == null){
				list = new ArrayList<>();
			}
			list.add(modification.getTargetString());
			_insertions.put(modification.getSourceID(), list);
		} else if(modification instanceof Deletion){
			if(!_deletions.contains(modification.getSourceID())){
				return false;
			}
			_deletions.remove(modification.getSourceID());
		} else {
			return false;
		}
		return true;
	}

	@Override
	public boolean restore(Modification modification) {
		if(modification.getSourceID() == WHOLE){
			_replace = null;
		} else if(modification instanceof Insertion){
			List<String> list = _insertions.get(modification.getSourceID());
			if(list != null){
				list.remove(modification.getTargetString());
			}
		} else if(modification instanceof Deletion){
			if(_deletions.contains(modification.getSourceID())){
				_deletions.remove(modification.getTargetString());
			}
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
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("{\n");
		if(_replace != null){
			stringBuffer.append(_replace);
			stringBuffer.append("\n");
		} else {
			int i = 0;
			for(i = 0; i < _statements.size(); i++){
				if(_insertions.containsKey(i)){
					for(String string : _insertions.get(i)){
						stringBuffer.append(string);
						stringBuffer.append("\n");
					}
				} else if(_deletions.contains(i)){
					continue;
				}
				stringBuffer.append(_statements.get(i).toSrcString());
				stringBuffer.append("\n");
			}
			if(_insertions.containsKey(i)){
				for(String string : _insertions.get(i)){
					stringBuffer.append(string);
					stringBuffer.append("\n");
				}
			} 
		}
		stringBuffer.append("}");
		return stringBuffer;
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
	
	@Override
	public void computeFeatureVector() {
		_fVector = new NewFVector();
		if(_statements != null){
			for(Stmt stmt : _statements){
				_fVector.combineFeature(stmt.getFeatureVector());
			}
		}
	}
	
	@Override
	public List<Node> getChildren() {
		List<Node> list = new ArrayList<>();
		for(Stmt stmt : _statements){
			list.add(stmt);
		}
		return list;
	}
	
	@Override
	public String simplify(Map<String, String> varTrans, Map<String, Type> allUsableVariables) {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("{\n");
		boolean empty = true;
		for(Stmt stmt : _statements){
			String string = stmt.simplify(varTrans, allUsableVariables);
			if(string != null){
				empty = false;
				stringBuffer.append(string);
				stringBuffer.append("\n");
			}
		}
		stringBuffer.append("}");
		if(empty){
			return null;
		}
		return stringBuffer.toString();
	}
	
}
