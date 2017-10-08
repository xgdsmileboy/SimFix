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
import org.eclipse.jdt.core.dom.Assignment;
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
public class Assign extends Expr {

	private Expr _lhs = null;
	private Assignment.Operator _operator = null;
	private Expr _rhs = null;
	
	private Assignment.Operator _operator_repalce = null;
	private Expr _rhs_replace = null;
	
	/**
	 * Assignment:
     *	Expression AssignmentOperator Expression
	 */
	public Assign(int startLine, int endLine, ASTNode node) {
		super(startLine, endLine, node);
		_nodeType = TYPE.ASSIGN;
	}

	public void setLeftHandSide(Expr lhs){
		_lhs = lhs;
	}
	
	public void setOperator(Assignment.Operator operator){
		_operator = operator;
	}
	
	public void setRightHandSide(Expr rhs){
		_rhs = rhs;
	}
	
	public Expr getLhs(){
		return _lhs;
	}
	
	public Expr getRhs(){
		return _rhs;
	}
	
	@Override
	public boolean match(Node node, Map<String, String> varTrans, Map<String, Type> allUsableVariables, List<Modification> modifications) {
		boolean match = false;
		if(node instanceof Assign){
			Assign assign = (Assign) node;
			List<Variable> tarVars = assign._lhs.getVariables();
			List<Variable> srcVars = _lhs.getVariables();
			if(tarVars.size() > 0 && srcVars.size() > 0){
				String source = varTrans.get(tarVars.get(0).getName()); 
				if(source != null && source.equals(srcVars.get(0).toString())){
					match = true;
					List<Modification> tmp = new ArrayList<>();
					if(_rhs.match(assign._rhs, varTrans, allUsableVariables, tmp)){
						modifications.addAll(tmp);
					}
				} else {
					if(tarVars.get(0).getName().equals(srcVars.get(0).getName()) && tarVars.get(0).getType().toString().equals(srcVars.get(0).getType().toString())){
						match = true;
						List<Modification> tmp = new ArrayList<>();
						if(_rhs.match(assign._rhs, varTrans, allUsableVariables, tmp)){
							modifications.addAll(tmp);
						}
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
		_operator_repalce = null;
		_rhs_replace = null;
		return true;
	}

	@Override
	public boolean backup(Modification modification) {
		return true;
	}
	
	@Override
	public StringBuffer toSrcString() {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(_lhs.toSrcString());
		if(_operator_repalce != null){
			stringBuffer.append(_operator_repalce.toString());
		} else {
			stringBuffer.append(_operator.toString());
		}
		if(_rhs_replace != null){
			stringBuffer.append(_rhs_replace.toSrcString());
		} else {
			stringBuffer.append(_rhs.toSrcString());
		}
		return stringBuffer;
	}

	@Override
	public List<Literal> getLiterals() {
		List<Literal> list = new LinkedList<>();
		list.addAll(_lhs.getLiterals());
		list.addAll(_rhs.getLiterals());
		return list;
	}

	@Override
	public List<Variable> getVariables() {
		List<Variable> list = new LinkedList<>();
		list.addAll(_lhs.getVariables());
		list.addAll(_rhs.getVariables());
		return list;
	}

	@Override
	public List<CondStruct> getCondStruct() {
		List<CondStruct> list = new LinkedList<>();
		list.addAll(_rhs.getCondStruct());
		return list;
	}

	@Override
	public List<MethodCall> getMethodCalls() {
		List<MethodCall> list = new LinkedList<>();
		list.addAll(_rhs.getMethodCalls());
		return list;
	}

	@Override
	public List<Operator> getOperators() {
		List<Operator> list = new LinkedList<>();
		list.addAll(_rhs.getOperators());
		return list;
	}

	@Override
	public void computeFeatureVector() {
		_fVector = new NewFVector();
		_fVector.inc(NewFVector.INDEX_OP_ASSIGN);
		_fVector.combineFeature(_lhs.getFeatureVector());
		_fVector.combineFeature(_rhs.getFeatureVector());
	}

	@Override
	public USE_TYPE getUseType(Node child) {
		if(child == _lhs){
			return USE_TYPE.USE_ASSIGN_LHS;
		} if(child == _rhs){
			return USE_TYPE.USE_ASSIGN_RHS;
		}
		return USE_TYPE.USE_UNKNOWN;
	}
	
	@Override
	public List<Node> getChildren() {
		List<Node> list = new ArrayList<>();
		list.add(_rhs);
		return list;
	}

	@Override
	public String simplify(Map<String, String> varTrans, Map<String, Type> allUsableVariables) {
		String right = _rhs.simplify(varTrans, allUsableVariables);
		if(right == null){
			return null;
		}
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(_lhs.toSrcString());
		stringBuffer.append(_operator.toString());
		stringBuffer.append(right);
		return stringBuffer.toString();
	}
}
