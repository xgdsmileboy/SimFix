/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.core.parser.node.expr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jws.WebParam.Mode;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Type;

import cofix.core.metric.Literal;
import cofix.core.metric.MethodCall;
import cofix.core.metric.NewFVector;
import cofix.core.metric.Operator;
import cofix.core.metric.Variable;
import cofix.core.modify.Deletion;
import cofix.core.modify.Insertion;
import cofix.core.modify.Modification;
import cofix.core.parser.NodeUtils;
import cofix.core.parser.node.Node;

/**
 * @author Jiajun
 * @date Jun 23, 2017
 */
public class ArrayInitial extends Expr {

	private List<Expr> _expressions = null;
	
	private Map<Integer, List<String>> _insertions = new HashMap<>();
	private Set<Integer> _deletions = new HashSet<>();

	private String _replace = null;
	
	private final int WHOLE = 0;
	
	/**
	 * ArrayInitializer:
     *           { [ Expression { , Expression} [ , ]] }
	 */
	public ArrayInitial(int startLine, int endLine, ASTNode node) {
		super(startLine, endLine, node);
		_nodeType = TYPE.ARRINIT;
	}
	
	public void setExpressions(List<Expr> expressions){
		_expressions = expressions;
	}

	@Override
	public boolean match(Node node, Map<String, String> varTrans, Map<String, Type> allUsableVariables, List<Modification> modifications) {
		boolean match = false;
		if(node instanceof ArrayInitial){
			match = true;
			ArrayInitial other = (ArrayInitial) node;
			modifications.addAll(NodeUtils.listNodeMatching(this, _nodeType, _expressions, other._expressions, varTrans, allUsableVariables));
		} else {
			List<Modification> tmp = new LinkedList<>();
			if(replaceExpr(node, WHOLE, varTrans, allUsableVariables,tmp)) {
				modifications.addAll(tmp);
				match = true;
			}
			tmp = new ArrayList<>();
			List<Node> children = node.getChildren();
			if(NodeUtils.nodeMatchList(this, children, varTrans, allUsableVariables, tmp)){
				match = true;
				modifications.addAll(tmp);
			}
		}
		return match;
	}

	@Override
	public boolean adapt(Modification modification) {
		if(modification instanceof Insertion){
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
		} else if(modification.getSourceID()==WHOLE){
			_replace = modification.getTargetString();
		} else {
			return false;
		}
		return true;
	}

	@Override
	public boolean restore(Modification modification) {
		if(modification instanceof Insertion){
			List<String> list = _insertions.get(modification.getSourceID());
			if(list != null){
				list.remove(modification.getTargetString());
			}
		} else if(modification instanceof Deletion){
			if(_deletions.contains(modification.getSourceID())){
				_deletions.remove(modification.getTargetString());
			}
		} else if(modification.getSourceID() == WHOLE) {
			_replace = null;
		} else {
			return false;
		}
		return true;
	}

	@Override
	public boolean backup(Modification modification) {
		return true;
	}
	
	@Override
	public StringBuffer toSrcString() {
		StringBuffer stringBuffer = new StringBuffer();
		if(_replace != null) {
			stringBuffer.append(_replace);
		} else {
			stringBuffer.append("{");
			if(_expressions.size() > 0){
				for(int i = 0; i < _expressions.size(); i++){
					if(_insertions.containsKey(i)){
						for(String string : _insertions.get(i)){
							stringBuffer.append(string);
							stringBuffer.append(",");
						}
					} else if(_deletions.contains(i)){
						continue;
					}
					stringBuffer.append(_expressions.get(i).toSrcString());
					stringBuffer.append(",");
				}
			}
			if(stringBuffer.charAt(stringBuffer.length() - 1) == ',') {
				stringBuffer.deleteCharAt(stringBuffer.length() - 1);
			}
			stringBuffer.append("}");
		}
		return stringBuffer;
	}

	@Override
	public List<Literal> getLiterals() {
		List<Literal> list = new LinkedList<>();
		if(_expressions != null){
			for(Expr expr : _expressions){
				list.addAll(expr.getLiterals());
			}
		}
		return list;
	}

	@Override
	public List<Variable> getVariables() {
		List<Variable> list = new LinkedList<>();
		if(_expressions != null){
			for(Expr expr : _expressions){
				list.addAll(expr.getVariables());
			}
		}
		return list;
	}
	
	@Override
	public List<MethodCall> getMethodCalls() {
		List<MethodCall> list = new LinkedList<>();
		if(_expressions != null){
			for(Expr expr : _expressions){
				list.addAll(expr.getMethodCalls());
			}
		}
		return list;
	}
	
	@Override
	public List<Operator> getOperators() {
		List<Operator> list = new LinkedList<>();
		if(_expressions != null){
			for(Expr expr : _expressions){
				list.addAll(expr.getOperators());
			}
		}
		return list;
	}

	@Override
	public void computeFeatureVector() {
		_fVector = new NewFVector();
		if(_expressions != null){
			for(Expr expr : _expressions){
				_fVector.combineFeature(expr.getFeatureVector());
			}
		}
	}

	@Override
	public List<Node> getChildren() {
		return new ArrayList<>();
	}

	@Override
	public String simplify(Map<String, String> varTrans, Map<String, Type> allUsableVariables) {
		// TODO Auto-generated method stub
		return null;
	}
}
