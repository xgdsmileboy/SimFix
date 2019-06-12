/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.core.parser.node.expr;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Type;

import cofix.core.metric.CondStruct;
import cofix.core.metric.Literal;
import cofix.core.metric.MethodCall;
import cofix.core.metric.NewFVector;
import cofix.core.metric.Operator;
import cofix.core.metric.Variable;
import cofix.core.metric.Variable.USE_TYPE;
import cofix.core.modify.Modification;
import cofix.core.parser.NodeUtils;
import cofix.core.parser.node.Node;

/**
 * @author Jiajun
 * @date Jun 23, 2017
 */
public class ConditionalExpr extends Expr {

	private Expr _condition = null;
	private Expr _first = null;
	private Expr _snd = null;
	
	private Expr _condition_replace = null;
	private Expr _first_replace = null;
	private Expr _snd_replace = null;
	
	private Set<String> _conditionSet = new HashSet<>();
	private Set<String> _firstSet = new HashSet<>();
	private Set<String> _sndSet = new HashSet<>();
	
	/**
	 * ConditionalExpression:
     *	Expression ? Expression : Expression
	 */
	public ConditionalExpr(int startLine, int endLine, ASTNode node) {
		super(startLine, endLine, node);
		_nodeType = TYPE.CONDEXPR;
	}

	public void setCondition(Expr condition){
		_condition = condition;
	}
	
	public void setFirst(Expr first){
		_first = first;
	}
	
	public void setSecond(Expr snd){
		_snd = snd;
	}
	
	public Expr getCondition(){
		return _condition;
	}
	
	public Expr getfirst(){
		return _first;
	}
	
	public Expr getSecond(){
		return _snd;
	}
	
	@Override
	public boolean match(Node node, Map<String, String> varTrans, Map<String, Type> allUsableVariables, List<Modification> modifications) {
		boolean match = false;
		if(node instanceof ConditionalExpr){
			match = true;
			ConditionalExpr other = (ConditionalExpr) node;
			List<Modification> tmp = new ArrayList<>();
			if(_condition.match(other._condition, varTrans, allUsableVariables, tmp)){
				for(Modification modification : tmp){
					if(!_conditionSet.contains(modification.getTargetString())){
						modifications.add(modification);
						_conditionSet.add(modification.getTargetString());
					}
				}
			}
			tmp = new ArrayList<>();
			if(_first.match(other._first, varTrans, allUsableVariables, tmp)){
				for(Modification modification : tmp){
					if(!_firstSet.contains(modification.getTargetString())){
						modifications.add(modification);
						_firstSet.add(modification.getTargetString());
					}
				}
			}
			tmp = new ArrayList<>();
			if(_snd.match(other._snd, varTrans, allUsableVariables, tmp)){
				for(Modification modification : tmp){
					if(!_sndSet.contains(modification.getTargetString())){
						modifications.add(modification);
						_sndSet.add(modification.getTargetString());
					}
				}
			}
			
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
		_condition_replace = null;
		_first_replace = null;
		_snd_replace = null;
		return true;
	}

	@Override
	public boolean backup(Modification modification) {
		return true;
	}
	
	@Override
	public StringBuffer toSrcString() {
		StringBuffer stringBuffer = new StringBuffer();
		if(_condition_replace != null){
			stringBuffer.append(_condition_replace.toSrcString());
		} else {
			stringBuffer.append(_condition.toSrcString());
		}
		stringBuffer.append("?");
		if(_first_replace != null){
			stringBuffer.append(_first_replace.toSrcString());
		} else {
			stringBuffer.append(_first.toSrcString());
		}
		stringBuffer.append(":");
		if(_snd_replace != null){
			stringBuffer.append(_snd_replace.toSrcString());
		} else {
			stringBuffer.append(_snd.toSrcString());
		}
		return stringBuffer;
	}

	@Override
	public List<Literal> getLiterals() {
		List<Literal> list = new LinkedList<>();
		list.addAll(_condition.getLiterals());
		list.addAll(_first.getLiterals());
		list.addAll(_snd.getLiterals());
		return list;
	}

	@Override
	public List<Variable> getVariables() {
		List<Variable> list = new LinkedList<>();
		list.addAll(_condition.getVariables());
		list.addAll(_first.getVariables());
		list.addAll(_snd.getVariables());
		return list;
	}

	@Override
	public List<MethodCall> getMethodCalls() {
		List<MethodCall> list = new LinkedList<>();
		list.addAll(_condition.getMethodCalls());
		list.addAll(_first.getMethodCalls());
		list.addAll(_snd.getMethodCalls());
		return list;
	}

	@Override
	public List<Operator> getOperators() {
		List<Operator> list = new LinkedList<>();
		list.addAll(_condition.getOperators());
		list.addAll(_first.getOperators());
		list.addAll(_snd.getOperators());
		return list;
	}
	
	@Override
	public List<CondStruct> getCondStruct() {
		List<CondStruct> list = new LinkedList<>();
		CondStruct condStruct = new CondStruct(this, CondStruct.KIND.CE);
		list.add(condStruct);
		list.addAll(_first.getCondStruct());
		list.addAll(_snd.getCondStruct());
		return list;
	}
	
	@Override
	public void computeFeatureVector() {
		_fVector = new NewFVector();
		_fVector.inc(NewFVector.INDEX_STRUCT_COND);
		_fVector.combineFeature(_condition.getFeatureVector());
		_fVector.combineFeature(_first.getFeatureVector());
		_fVector.combineFeature(_snd.getFeatureVector());
	}
	

	@Override
	public USE_TYPE getUseType(Node child) {
		return USE_TYPE.USE_CONDITIONAL;
	}
	
	@Override
	public List<Node> getChildren() {
		List<Node> list = new ArrayList<>();
		list.add(_first);
		list.add(_snd);
		return list;
	}

	@Override
	public String simplify(Map<String, String> varTrans, Map<String, Type> allUsableVariables) {
		String cond = _condition.simplify(varTrans, allUsableVariables);
		if(cond == null){
			return null;
		}
		String fst = _first.simplify(varTrans, allUsableVariables);
		if(fst == null){
			return null;
		}
		String snd = _snd.simplify(varTrans, allUsableVariables);
		if(snd == null){
			return null;
		}
		return cond + "?" + fst + ":" + snd;
	}
}
