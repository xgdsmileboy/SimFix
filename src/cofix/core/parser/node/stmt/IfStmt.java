/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.core.parser.node.stmt;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
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
import cofix.core.parser.node.CodeBlock;
import cofix.core.parser.node.Node;
import cofix.core.parser.node.expr.Expr;
import cofix.core.parser.node.expr.Label;

/**
 * @author Jiajun
 * @date Jun 23, 2017
 */
public class IfStmt extends Stmt {

	private Expr _condition = null;
	private Stmt _then = null;
	private Stmt _else = null;
	
	private Expr _condition_replace = null;
	private Expr _then_replace = null;
	private Expr _else_replace = null;
	
	/**
	 * IfStatement:
     *	if ( Expression ) Statement [ else Statement]
	 */
	public IfStmt(int startLine, int endLine, ASTNode node) {
		this(startLine, endLine, node, null);
		_nodeType = TYPE.IF;
	}
	
	public IfStmt(int startLine, int endLine, ASTNode node, Node parent) {
		super(startLine, endLine, node, parent);
	}
	
	public void setCondition(Expr condition){
		_condition = condition;
	}
	
	public void setThen(Stmt then){
		_then = then;
	}
	
	public void setElse(Stmt els){
		_else = els;
	}
	
	public Expr getCondition(){
		return _condition;
	}
	
	public Stmt getThen(){
		return _then;
	}
	
	public Stmt getElse(){
		return _else;
	}

	@Override
	public boolean match(Node node, Map<String, String> varTrans, Map<String, Type> allUsableVariables, List<Modification> modifications) {
		boolean match = false;
		if(node instanceof IfStmt){
			IfStmt other = (IfStmt) node;
			List<Modification> tmp = new ArrayList<>();
			if(_condition.match(other._condition, varTrans, allUsableVariables, tmp)){
				match = true;
				if(!(_condition instanceof Label || other._condition instanceof Label)){
					modifications.addAll(tmp);
				}
			}

			if(match){
				tmp = new ArrayList<>();
				if(_then.match(other._then, varTrans, allUsableVariables, tmp)){
					modifications.addAll(tmp);
				}
				
				if(_else != null){
					if(other._else != null){
						tmp = new ArrayList<>();
						if(_else.match(other._else, varTrans, allUsableVariables, tmp)){
							modifications.addAll(tmp);
						}
					} else {
						tmp = new ArrayList<>();
						if(_else.match(other._then, varTrans, allUsableVariables, tmp)){
							modifications.addAll(tmp);
						}
					}
				} else{
					if(other._else != null){
						tmp = new ArrayList<>();
						if(_then.match(other._else, varTrans, allUsableVariables, tmp)){
							modifications.addAll(tmp);
						}
					}
				}
			} else {
				List<Node> children = node.getChildren();
				tmp = new ArrayList<>();
				if(NodeUtils.nodeMatchList(this, children, varTrans, allUsableVariables, tmp)){
					match = true;
					modifications.addAll(tmp);
				}
			}
			
		} else if(node instanceof SwCase){
			SwCase other = (SwCase) node;
			Expr expr = other.getExpression();
			if(expr != null && _condition.toSrcString().toString().contains(expr.toSrcString().toString())){
				match = true;
				if(_then instanceof Blk){
					List<Node> nodes = _then.getChildren();
					modifications.addAll(NodeUtils.listNodeMatching(_then, _nodeType, nodes, other.getSiblings(), varTrans, allUsableVariables));
				}
			} else {
				List<Node> siblings = other.getSiblings();
				if(siblings != null){
					for(Node sib : siblings){
						List<Modification> tmp = new ArrayList<>();
						if(this.match(sib, varTrans, allUsableVariables, tmp)){
							match = true;
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
		return false;
	}

	@Override
	public boolean restore(Modification modification) {
		return false;
	}

	@Override
	public boolean backup(Modification modification) {
		return false;
	}
	
	@Override
	public StringBuffer toSrcString() {
		StringBuffer stringBuffer = new StringBuffer("if(");
		if(_condition_replace != null){
			stringBuffer.append(_condition_replace.toSrcString());
		} else {
			stringBuffer.append(_condition.toSrcString());
		}
		stringBuffer.append(")");
		String then = null;
		if(_then_replace != null){
			then = _then_replace.toSrcString().toString();
		} else {
			then = _then.toSrcString().toString();
		}
		String els = null;
		if(_else_replace != null){
			els = _else_replace.toSrcString().toString();
		} else if(_else != null){
			els = _else.toSrcString().toString();
		}
		stringBuffer.append(then);
		if(els != null){
			stringBuffer.append("else ");
			if(els.equals(then)){
				stringBuffer.append("error");
			} else {
				stringBuffer.append(els);
			}
		}
		return stringBuffer;
	}

	@Override
	public List<Literal> getLiterals() {
		List<Literal> list = _condition.getLiterals();
		if(_then != null){
			list.addAll(_then.getLiterals());
		}
		if(_else != null){
			list.addAll(_else.getLiterals());
		}
		return list;
	}

	@Override
	public List<Variable> getVariables() {
		List<Variable> list = _condition.getVariables();
		if(_then != null){
			list.addAll(_then.getVariables());
		}
		if(_else != null){
			list.addAll(_else.getVariables());
		}
		return list;
	}

	@Override
	public List<LoopStruct> getLoopStruct() {
		List<LoopStruct> list = new LinkedList<>();
		if(_then != null){
			list.addAll(_then.getLoopStruct());
		}
		if(_else != null){
			list.addAll(_else.getLoopStruct());
		}
		return list;
	}
	
	@Override
	public List<CondStruct> getCondStruct() {
		List<CondStruct> list = new LinkedList<>();
		CondStruct condStruct = new CondStruct(this, CondStruct.KIND.IF);
		list.add(condStruct);
		if(_then != null){
			list.addAll(_then.getCondStruct());
		}
		if(_else != null){
			list.addAll(_else.getCondStruct());
		}
		return list;
	}

	@Override
	public List<MethodCall> getMethodCalls() {
		List<MethodCall> list = _condition.getMethodCalls();
		if(_then != null){
			list.addAll(_then.getMethodCalls());
		}
		if(_else != null){
			list.addAll(_else.getMethodCalls());
		}
		return list;
	}

	@Override
	public List<Operator> getOperators() {
		List<Operator> list = _condition.getOperators();
		if(_then != null){
			list.addAll(_then.getOperators());
		}
		if(_else != null){
			list.addAll(_else.getOperators());
		}
		return list;
	}
	
	@Override
	public List<OtherStruct> getOtherStruct() {
		List<OtherStruct> list = new LinkedList<>();
		if(_then != null){
			list.addAll(_then.getOtherStruct());
		}
		if(_else != null){
			list.addAll(_else.getOtherStruct());
		}
		return list;
	}
	
	@Override
	public void computeFeatureVector() {
		_fVector = new NewFVector();
		_fVector.inc(NewFVector.INDEX_STRUCT_COND);
		_fVector.combineFeature(_condition.getFeatureVector());
		_fVector.combineFeature(_then.getFeatureVector());
		if(_else != null){
			_fVector.combineFeature(_else.getFeatureVector());
		}
	}
	
	@Override
	public USE_TYPE getUseType(Node child) {
		return USE_TYPE.USE_IF;
	}
	
	@Override
	public List<Node> getChildren() {
		List<Node> list = new ArrayList<>();
		list.add(_then);
		if(_else != null){
			list.add(_else);
		}
		return list;
	}
	
	@Override
	public String simplify(Map<String, String> varTrans, Map<String, Type> allUsableVariables) {
		StringBuffer stringBuffer = new StringBuffer("if(");
		String cond = _condition.simplify(varTrans, allUsableVariables);
		if(cond == null){
			return null;
		}
		stringBuffer.append(cond);
		stringBuffer.append(")");
		String then = _then.simplify(varTrans, allUsableVariables);
		if(then == null){
			return null;
		}
		stringBuffer.append(then);
		if(_else != null){
			String els = _else.simplify(varTrans, allUsableVariables);
			if(els != null){
				stringBuffer.append("else ");
				stringBuffer.append(els);
			}
		}
		return stringBuffer.toString();
	}
	
	@Override
	public List<CodeBlock> reduce() {
		List<CodeBlock> list = new LinkedList<>();
		if(_then != null){
			list.addAll(_then.reduce());
		}
		if(_else != null){
			list.addAll(_else.reduce());
		}
		List<ASTNode> nodes = new LinkedList<>();
		nodes.add(_originalNode);
		CodeBlock codeBlock = new CodeBlock(null, null, nodes);
		list.add(codeBlock);
		return list;
	}
	
}
