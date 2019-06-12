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

/**
 * @author Jiajun
 * @date Jun 23, 2017
 */
public class ForStmt extends Stmt {

	private List<Expr> _initializers = null;
	private List<Expr> _updaters = null;
	private Expr _condition = null;
	private Stmt _body = null;
	
	private Expr _condition_replace = null;
	private Stmt _body_replace = null;
	
	/**
	 * for (
     *           [ ForInit ];
     *           [ Expression ] ;
     *           [ ForUpdate ] )
     *           Statement
     * ForInit:
     *           Expression { , Expression }
     * ForUpdate:
     *           Expression { , Expression }
	 */
	public ForStmt(int startLine, int endLine, ASTNode node) {
		this(startLine, endLine, node, null);
		_nodeType = TYPE.FOR;
	}
	
	public ForStmt(int startLine, int endLine, ASTNode node, Node parent) {
		super(startLine, endLine, node, parent);
	}
	
	public void setCondition(Expr condition){
		_condition = condition;
	}
	
	public void setInitializer(List<Expr> initializers){
		_initializers = initializers;
	}
	
	public void setUpdaters(List<Expr> updaters){
		_updaters = updaters;
	}
	
	public void setBody(Stmt body){
		_body = body;
	}

	@Override
	public StringBuffer toSrcString() {
		StringBuffer stringBuffer = new StringBuffer("for(");
		if(_initializers != null && _initializers.size() > 0){
			stringBuffer.append(_initializers.get(0).toSrcString());
			for(int i = 1; i < _initializers.size(); i++){
				stringBuffer.append(",");
				stringBuffer.append(_initializers.get(i).toSrcString());
			}
		}
		stringBuffer.append(";");
		if(_condition_replace != null){
			stringBuffer.append(_condition_replace.toSrcString());
		} else {
			if(_condition != null){
				stringBuffer.append(_condition.toSrcString());
			}
		}
		stringBuffer.append(";");
		if(_updaters != null && _updaters.size() > 0){
			stringBuffer.append(_updaters.get(0).toSrcString());
			for(int i = 1; i < _updaters.size(); i++){
				stringBuffer.append(",");
				stringBuffer.append(_updaters.get(i).toSrcString());
			}
		}
		stringBuffer.append(")");
		if(_body_replace != null){
			stringBuffer.append(_body_replace.toSrcString());
		} else {
			stringBuffer.append(_body.toSrcString());
		}
		return stringBuffer;
	}
	
	@Override
	public boolean match(Node node, Map<String, String> varTrans, Map<String, Type> allUsableVariables, List<Modification> modifications) {
		boolean match = false;
		if(node instanceof ForStmt){
			match = true;
			ForStmt other = (ForStmt) node;
			if(_initializers != null && other._initializers != null){
				List<Modification> tmp = NodeUtils.listNodeMatching(this, _nodeType, _initializers, other._initializers, varTrans, allUsableVariables);
				if(tmp != null){
					modifications.addAll(tmp);
				}
			}
			if(_condition != null && other._condition != null){
				List<Modification> tmp = new ArrayList<>();
				if(_condition.match(other._condition, varTrans, allUsableVariables, tmp)){
					modifications.addAll(tmp);
				}
			}
			List<Modification> tmp = new ArrayList<>();
			if(_body.match(other._body, varTrans, allUsableVariables, tmp)){
				modifications.addAll(tmp);
			}
		} else {
			List<Node> children = node.getChildren();
			List<Modification> tmp = new ArrayList<>();
			if(NodeUtils.nodeMatchList(this, children, varTrans, allUsableVariables, tmp)){
				match = true;
				modifications.addAll(tmp);
			}
			if(!match){
				tmp = new ArrayList<>();
				if(NodeUtils.nodeMatchList(_body, children, varTrans, allUsableVariables, tmp)){
					match = true;
					modifications.addAll(tmp);
				}
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
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<Literal> getLiterals() {
		List<Literal> list = new LinkedList<>();
		if(_initializers != null){
			for(Expr expr : _initializers){
				list.addAll(expr.getLiterals());
			}
		}
		if(_condition != null){
			list.addAll(_condition.getLiterals());
		}
		if(_updaters != null){
			for(Expr expr : _updaters){
				list.addAll(expr.getLiterals());
			}
		}
		if(_body != null){
			list.addAll(_body.getLiterals());
		}
		return list;
	}

	@Override
	public List<Variable> getVariables() {
		List<Variable> list = new LinkedList<>();
		if(_initializers != null){
			for(Expr expr : _initializers){
				list.addAll(expr.getVariables());
			}
		}
		if(_condition != null){
			list.addAll(_condition.getVariables());
		}
		if(_updaters != null){
			for(Expr expr : _updaters){
				list.addAll(expr.getVariables());
			}
		}
		if(_body != null){
			list.addAll(_body.getVariables());
		}
		return list;
	}

	@Override
	public List<LoopStruct> getLoopStruct() {
		List<LoopStruct> list = new LinkedList<>();
		LoopStruct loopStruct = new LoopStruct(this, LoopStruct.KIND.FOR);
		list.add(loopStruct);
		if(_body != null){
			list.addAll(_body.getLoopStruct());
		}
		return list;
	}
	
	@Override
	public List<CondStruct> getCondStruct() {
		List<CondStruct> list = new LinkedList<>();
		if(_initializers != null){
			for(Expr expr : _initializers){
				list.addAll(expr.getCondStruct());
			}
		}
		if(_condition != null){
			list.addAll(_condition.getCondStruct());
		}
		if(_updaters != null){
			for(Expr expr : _updaters){
				list.addAll(expr.getCondStruct());
			}
		}
		if(_body != null){
			list.addAll(_body.getCondStruct());
		}
		return list;
	}

	@Override
	public List<MethodCall> getMethodCalls() {
		List<MethodCall> list = new LinkedList<>();
		if(_initializers != null){
			for(Expr expr : _initializers){
				list.addAll(expr.getMethodCalls());
			}
		}
		if(_condition != null){
			list.addAll(_condition.getMethodCalls());
		}
		if(_updaters != null){
			for(Expr expr : _updaters){
				list.addAll(expr.getMethodCalls());
			}
		}
		if(_body != null){
			list.addAll(_body.getMethodCalls());
		}
		return list;
	}

	@Override
	public List<Operator> getOperators() {
		List<Operator> list = new LinkedList<>();
		if(_initializers != null){
			for(Expr expr : _initializers){
				list.addAll(expr.getOperators());
			}
		}
		if(_condition != null){
			list.addAll(_condition.getOperators());
		}
		if(_updaters != null){
			for(Expr expr : _updaters){
				list.addAll(expr.getOperators());
			}
		}
		if(_body != null){
			list.addAll(_body.getOperators());
		}
		return list;
	}
	
	@Override
	public List<OtherStruct> getOtherStruct() {
		List<OtherStruct> list = new LinkedList<>();
		if(_body != null){
			list.addAll(_body.getOtherStruct());
		}
		return list;
	}
	
	@Override
	public void computeFeatureVector() {
		_fVector = new NewFVector();
		_fVector.inc(NewFVector.INDEX_STRUCT_FOR);
		if(_initializers != null){
			for(Expr expr : _initializers){
				_fVector.combineFeature(expr.getFeatureVector());
			}
		}
		if(_condition != null){
			_fVector.combineFeature(_condition.getFeatureVector());
		}
		if(_updaters != null){
			for(Expr expr : _updaters){
				_fVector.combineFeature(expr.getFeatureVector());
			}
		}
		_fVector.combineFeature(_body.getFeatureVector());
	}

	@Override
	public USE_TYPE getUseType(Node child) {
		return USE_TYPE.USE_LOOP;
	}
	
	@Override
	public List<Node> getChildren() {
		List<Node> list = new ArrayList<>();
		list.add(_body);
		return list;
	}
	
	@Override
	public String simplify(Map<String, String> varTrans, Map<String, Type> allUsableVariables) {
		StringBuffer stringBuffer = new StringBuffer("for(");
		if(_initializers != null && _initializers.size() > 0){
			int index = 0;
			for(; index < _initializers.size(); index ++){
				String init = _initializers.get(index).simplify(varTrans, allUsableVariables);
				if(init != null){
					stringBuffer.append(init);
					break;
				}
			}
			for(index ++; index < _initializers.size(); index++){
				String init = _initializers.get(index).simplify(varTrans, allUsableVariables);
				if(init != null){
					stringBuffer.append(",");
					stringBuffer.append(init);
				}
			}
		}
		stringBuffer.append(";");
		if(_condition != null){
			String cond = _condition.simplify(varTrans, allUsableVariables);
			if(cond == null){
				return null;
			}
			stringBuffer.append(cond);
		}
		stringBuffer.append(";");
		if(_updaters != null && _updaters.size() > 0){
			int index = 0;
			for(; index < _updaters.size(); index ++){
				String update = _updaters.get(index).simplify(varTrans, allUsableVariables);
				if(update != null){
					stringBuffer.append(update);
					break;
				}
			}
			for(index ++; index < _updaters.size(); index++){
				String update = _updaters.get(index).simplify(varTrans, allUsableVariables);
				if(update != null){
					stringBuffer.append(",");
					stringBuffer.append(update);
				}
			}
		}
		stringBuffer.append(")");
		String body = _body.simplify(varTrans, allUsableVariables);
		if(body == null){
			return null;
		}
		stringBuffer.append(body);
		return stringBuffer.toString();
	}
	
	@Override
	public List<CodeBlock> reduce() {
		List<CodeBlock> list = new LinkedList<>();
		if(_body != null){
			list.addAll(_body.reduce());
		}
		List<ASTNode> nodes = new LinkedList<>();
		nodes.add(_originalNode);
		CodeBlock codeBlock = new CodeBlock(null, null, nodes);
		list.add(codeBlock);
		return list;
	}
}
