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
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Type;

import cofix.common.util.Pair;
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
 * @datae Jun 23, 2017
 */
public class InfixExpr extends Expr {

	private Expr _lhs = null;
	private InfixExpression.Operator _operator = null;
	private Expr _rhs = null;
	
	private String _replaceAll = null;
	private String _lhs_replace = null;
	private String _operator_replace = null;
	private String _rhs_replace = null;
	
	private final int WHOLE = 0;
	private final int LHSID = 1;
	private final int OPID = 2;
	private final int RHSID = 3;
	
	/**
	 * InfixExpression:
     *	Expression InfixOperator Expression { InfixOperator Expression }
	 */
	public InfixExpr(int startLine, int endLine, ASTNode node) {
		super(startLine, endLine, node);
		_nodeType = TYPE.INFIXEXPR;
	}
	
	public void setLeftHandSide(Expr lhs){
		_lhs = lhs;
	}
	
	public void setOperator(InfixExpression.Operator operator){
		_operator = operator;
	}
	
	public void setRightHandSide(Expr rhs){
		_rhs = rhs;
	}

	@Override
	public boolean match(Node node, Map<String, String> varTrans, Map<String, Type> allUsableVariables, List<Modification> modifications) {
		boolean match = false;
		if(node instanceof InfixExpr){
			match = true;
			InfixExpr other = (InfixExpr) node;
			
			// replace operator
			if(NodeUtils.canReplace(_operator.toString(), other._operator.toString())){
				modifications.add(new Revision(this, OPID, other._operator.toString(), _nodeType));
			}
			// replace left hand side
			List<Modification> tmp = new ArrayList<>();
			if(NodeUtils.replaceExpr(LHSID, _lhs, other._lhs, varTrans, allUsableVariables, tmp)){
				modifications.addAll(tmp);
			}
			tmp = new ArrayList<>();
			if(NodeUtils.replaceExpr(RHSID, _rhs, other._rhs, varTrans, allUsableVariables, tmp)){
				modifications.addAll(tmp);
			}
			
			tmp = new ArrayList<>();
			if(_lhs.match(node, varTrans, allUsableVariables, tmp)){
				modifications.addAll(tmp);
			}
			
			tmp = new ArrayList<>();
			if(_rhs.match(node, varTrans, allUsableVariables, tmp)){
				modifications.addAll(tmp);
			}
//			
//			// try to replace all 
//			Pair<List<Expr>, List<InfixExpression.Operator>> sPair = splitSingleBooleanExpr();
//			Pair<List<Expr>, List<InfixExpression.Operator>> tPair = other.splitSingleBooleanExpr();
//			
//			// TODO : try to replace all
//			for(Expr tExpr : tPair.getFirst()){
//				for(Expr sExpr : sPair.getFirst()){
//					
//				}
//			}
			
			
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
		int id = modification.getSourceID();
		if(id == WHOLE){
			_replaceAll = modification.getTargetString();
		 }else if(id == LHSID){
			_lhs_replace = modification.getTargetString();
		} else if(id == OPID){
			_operator_replace = modification.getTargetString();
		} else if(id == RHSID){
			_rhs_replace = modification.getTargetString();
		} else {
			return false;
		}
		return true;
	}

	@Override
	public boolean restore(Modification modification) {
		int id = modification.getSourceID();
		if(id == WHOLE){
			_replaceAll = null;
		} else if(id == LHSID){
			_lhs_replace = null;
		} else if(id == OPID){
			_operator_replace = null;
		} else if(id == RHSID){
			_rhs_replace = null;
		} else {
			return false;
		}
		return true;
	}

	@Override
	public boolean backup(Modification modification) {
		return false;
	}
	
	@Override
	public StringBuffer toSrcString() {
		StringBuffer stringBuffer = new StringBuffer();
		if(_replaceAll == null){
			if(_lhs_replace != null){
				stringBuffer.append(_lhs_replace);
			} else {
				stringBuffer.append(_lhs.toSrcString());
			}
			if(_operator_replace != null){
				stringBuffer.append(_operator_replace);
			} else {
				stringBuffer.append(_operator.toString());
			}
			if(_rhs_replace != null){
				stringBuffer.append(_rhs_replace);
			} else {
				stringBuffer.append(_rhs.toSrcString());
			}
		} else {
			stringBuffer.append(_replaceAll);
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
	public List<MethodCall> getMethodCalls() {
		List<MethodCall> list = new LinkedList<>();
		list.addAll(_lhs.getMethodCalls());
		list.addAll(_rhs.getMethodCalls());
		return list;
	}

	@Override
	public List<Operator> getOperators() {
		List<Operator> list = new LinkedList<>();
		Operator operator = new Operator(this, Operator.KIND.INFIX);
		list.add(operator);
		list.addAll(_lhs.getOperators());
		list.addAll(_rhs.getOperators());
		return list;
	}
	
	@Override
	public void computeFeatureVector() {
		_fVector = new NewFVector();
		_fVector.inc(_operator.toString());
		_fVector.combineFeature(_lhs.getFeatureVector());
		_fVector.combineFeature(_rhs.getFeatureVector());
	}
	
	public Pair<List<Expr>, List<InfixExpression.Operator>> splitSingleBooleanExpr(){
		List<Expr> exprs = new ArrayList<>();
		List<InfixExpression.Operator> operators = new ArrayList<>();
		String op = _operator.toString();
		if(op.equals("&&") || op.equals("||")){
			if(_lhs instanceof InfixExpr){
				Pair<List<Expr>, List<InfixExpression.Operator>> pair = ((InfixExpr)_lhs).splitSingleBooleanExpr();
				exprs.addAll(pair.getFirst());
				operators.addAll(pair.getSecond());
			} else {
				exprs.add(_lhs);
			}
			operators.add(_operator);
			if(_rhs instanceof InfixExpr){
				Pair<List<Expr>, List<InfixExpression.Operator>> pair = ((InfixExpr)_rhs).splitSingleBooleanExpr();
				exprs.addAll(pair.getFirst());
				operators.addAll(pair.getSecond());
			} else {
				exprs.add(_rhs);
			}
		} else {
			exprs.add(this);
		}
		return new Pair<List<Expr>, List<InfixExpression.Operator>>(exprs, operators);
	}

	@Override
	public USE_TYPE getUseType(Node child) {
		return USE_TYPE.USE_INFIX_EXP;
	}
	
	@Override
	public List<Node> getChildren() {
		List<Node> list = new ArrayList<>();
		list.add(_lhs);
		list.add(_rhs);
		return list;
	}
}
