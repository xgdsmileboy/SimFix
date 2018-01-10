/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.core.parser.node.expr;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Type;

import cofix.common.util.Pair;
import cofix.core.metric.CondStruct;
import cofix.core.metric.Literal;
import cofix.core.metric.MethodCall;
import cofix.core.metric.NewFVector;
import cofix.core.metric.Operator;
import cofix.core.metric.Variable;
import cofix.core.metric.Variable.USE_TYPE;
import cofix.core.modify.Modification;
import cofix.core.modify.Revision;
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
	
	private String _condition_replace = null;
	private String _first_replace = null;
	private String _snd_replace = null;
	private String _whole_replace = null;
	
	private final int COND = 0;
	private final int FIRST = 1;
	private final int SND = 2;
	private final int WHOLE = 3;
	
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
			
			Map<SName, Pair<String, String>> record = NodeUtils.tryReplaceAllVariables(other._condition, varTrans, allUsableVariables);
			if(record != null) {
				NodeUtils.replaceVariable(record);
				String target = other._condition.toSrcString().toString();
				if(!target.equals(_condition.toSrcString().toString())) {
					Revision revision = new Revision(this, COND, target, _nodeType);
					modifications.add(revision);
				}
				NodeUtils.restoreVariables(record);
			}
			
			if(_first.getType().toString().equals(other._first.getType().toString())) {
				record = NodeUtils.tryReplaceAllVariables(other._first, varTrans, allUsableVariables);
				if(record != null) {
					NodeUtils.replaceVariable(record);
					String target = other._first.toSrcString().toString();
					if(!toSrcString().toString().equals(target)) {
						Revision revision = new Revision(this, FIRST, target, _nodeType);
						modifications.add(revision);
					}
					NodeUtils.restoreVariables(record);
				}
			}
			
			if(_snd.getType().toString().equals(other._snd.getType().toString())) {
				record = NodeUtils.tryReplaceAllVariables(other._snd, varTrans, allUsableVariables);
				if(record != null) {
					NodeUtils.replaceVariable(record);
					String target = other._snd.toSrcString().toString();
					if(!target.equals(toSrcString().toString())) {
						Revision revision = new Revision(this, SND, target, _nodeType);
						modifications.add(revision);
					}
					NodeUtils.restoreVariables(record);
 				}
			}
			
			List<Modification> tmp = new ArrayList<>();
			if(_condition.match(other._condition, varTrans, allUsableVariables, tmp)){
				modifications.addAll(tmp);
			}
			tmp = new ArrayList<>();
			if(_first.match(other._first, varTrans, allUsableVariables, tmp)){
				modifications.addAll(tmp);
			}
			tmp = new ArrayList<>();
			if(_snd.match(other._snd, varTrans, allUsableVariables, tmp)){
				modifications.addAll(tmp);
			}
			
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
		if(modification instanceof Revision) {
			Revision revision = (Revision)modification;
			switch (revision.getSourceID()) {
			case COND:
				_condition_replace = revision.getTargetString();
				break;
			case FIRST:
				_first_replace = revision.getTargetString();
				break;
			case SND:
				_snd_replace = revision.getTargetString();
				break;
			case WHOLE:
				_whole_replace = revision.getTargetString();
				break;
			default:
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean restore(Modification modification) {
		if(modification instanceof Revision) {
			Revision revision = (Revision)modification;
			switch (revision.getSourceID()) {
			case COND:
				_condition_replace = null;
				break;
			case FIRST:
				_first_replace = null;
				break;
			case SND:
				_snd_replace = null;
				break;
			case WHOLE:
				_whole_replace = null;
				break;
			default:
				return false;
			}
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
		if(_whole_replace != null) {
			stringBuffer.append(_whole_replace);
		} else {
			if(_condition_replace != null){
				stringBuffer.append(_condition_replace);
			} else {
				stringBuffer.append(_condition.toSrcString());
			}
			stringBuffer.append("?");
			if(_first_replace != null){
				stringBuffer.append(_first_replace);
			} else {
				stringBuffer.append(_first.toSrcString());
			}
			stringBuffer.append(":");
			if(_snd_replace != null){
				stringBuffer.append(_snd_replace);
			} else {
				stringBuffer.append(_snd.toSrcString());
			}
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
