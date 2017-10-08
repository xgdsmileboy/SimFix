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
import cofix.core.metric.MethodCall;
import cofix.core.metric.NewFVector;
import cofix.core.metric.Operator;
import cofix.core.metric.Variable;
import cofix.core.metric.Variable.USE_TYPE;
import cofix.core.modify.Deletion;
import cofix.core.modify.Insertion;
import cofix.core.modify.Modification;
import cofix.core.parser.NodeUtils;
import cofix.core.parser.node.CodeBlock;
import cofix.core.parser.node.Node;
import cofix.core.parser.node.expr.Expr;

/**
 * @author Jiajun
 * @date Jun 23, 2017
 */
public class SwCase extends Stmt {

	private Expr _expression = null;
	private List<Node> _siblings = null;
	
	private String _siblings_replace = null;
	
	/**
	 * SwitchCase:
     *           case Expression  :
     *           default :
	 */
	public SwCase(int startLine, int endLine, ASTNode node) {
		this(startLine, endLine, node, null);
	}
	
	public SwCase(int startLine, int endLine, ASTNode node, Node parent) {
		super(startLine, endLine, node, parent);
		_nodeType = TYPE.SWCASE;
	}
	
	public void setExpression(Expr expression){
		_expression = expression;
	}
	
	public Expr getExpression(){
		return _expression;
	}
	
	public void addSibling(Node sibling){
		if(_siblings == null){
			_siblings = new ArrayList<>();
		}
		_siblings.add(sibling);
	}
	
	public void setSiblings(List<Node> siblings){
		_siblings = siblings;
	}
	
	public List<Node> getSiblings(){
		return _siblings;
	}

	@Override
	public boolean match(Node node, Map<String, String> varTrans, Map<String, Type> allUsableVariables, List<Modification> modifications) {
		boolean match = false;
		if(node instanceof SwCase){
			match = true;
			SwCase other = (SwCase) node;
			modifications.addAll(NodeUtils.listNodeMatching(this, _nodeType, _siblings, other._siblings, varTrans, allUsableVariables));
		} else if(node instanceof IfStmt){
			IfStmt other = (IfStmt) node;
			modifications.addAll(NodeUtils.listNodeMatching(this, _nodeType, _siblings, other.getChildren(), varTrans, allUsableVariables));
		} else {
			List<Node> children = node.getChildren();
			List<Modification> tmp = new ArrayList<>();
			if(NodeUtils.nodeMatchList(this, children, varTrans, allUsableVariables, tmp)){
				match = true;
				modifications.addAll(tmp);
			}

			if(_siblings != null){
				for(Node sib : _siblings){
					tmp = new ArrayList<>();
					if(sib.match(node, varTrans, allUsableVariables, tmp)){
						match = true;
						modifications.addAll(tmp);
					}
				}
			}
		}
		return match;
	}

	@Override
	public boolean adapt(Modification modification) {
		int index = modification.getSourceID();
		if(_siblings == null  || index > _siblings.size()){
			return false;
		}
		if(modification instanceof Deletion){
			StringBuffer stringBuffer = new StringBuffer();
			for(int i = 0; i < _siblings.size(); i++){
				if (i == index) {
					continue;
				}
				stringBuffer.append(_siblings.get(i).toSrcString());
				stringBuffer.append("\n");
			}
			_siblings_replace = stringBuffer.toString();
		} else if(modification instanceof Insertion){
			StringBuffer stringBuffer = new StringBuffer();
			for(int i = 0; i < _siblings.size(); i++){
				if (i == index) {
					stringBuffer.append(modification.getTargetString());
					stringBuffer.append("\n");
				}
				stringBuffer.append(_siblings.get(i).toSrcString());
				stringBuffer.append("\n");
			}
			_siblings_replace = stringBuffer.toString();
		} else {
			return false;
		}
		return true;
	}

	@Override
	public boolean restore(Modification modification) {
		int index = modification.getSourceID();
		if(_siblings == null  || index > _siblings.size()){
			return false;
		}
		if(modification instanceof Deletion){
			_siblings_replace = null;
		} else if(modification instanceof Insertion){
			_siblings_replace = null;
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
		if(_expression == null){
			stringBuffer.append("default :\n");
		} else {
			stringBuffer.append("case ");
			stringBuffer.append(_expression.toSrcString());
			stringBuffer.append(" :\n");
		}
		if(_siblings_replace != null){
			stringBuffer.append(_siblings_replace);
			stringBuffer.append("\n");
		} else {
			if(_siblings != null){
				for(Node sibling : _siblings){
					stringBuffer.append(sibling.toSrcString());
					stringBuffer.append("\n");
				}
			}
		}
		return stringBuffer;
	}

	@Override
	public List<Literal> getLiterals() {
		if(_expression != null){
			return _expression.getLiterals();
		}
		return new LinkedList<>();
	}

	@Override
	public List<Variable> getVariables() {
		List<Variable> list = new LinkedList<>();
		if(_expression != null){
			list.addAll(_expression.getVariables());
		}
		if(_siblings != null){
			for(Node node : _siblings){
				list.addAll(node.getVariables());
			}
		}
		return list;
	}

	
	@Override
	public List<CondStruct> getCondStruct() {
		List<CondStruct> list = new ArrayList<>();
		CondStruct condStruct = new CondStruct(this, CondStruct.KIND.SC);
		list.add(condStruct);
		return list;
	}

	@Override
	public List<MethodCall> getMethodCalls() {
		if(_expression != null){
			return _expression.getMethodCalls();
		}
		return new LinkedList<>();
	}

	@Override
	public List<Operator> getOperators() {
		if(_expression != null){
			return _expression.getOperators();
		}
		return new LinkedList<>();
	}

	@Override
	public void computeFeatureVector() {
		_fVector = new NewFVector();
		_fVector.inc(NewFVector.INDEX_STRUCT_COND);
		if(_expression != null){
			_fVector.combineFeature(_expression.getFeatureVector());
		}
		if(_siblings != null){
			for(Node node : _siblings){
				_fVector.combineFeature(node.getFeatureVector());
			}
		}
	}

	@Override
	public USE_TYPE getUseType(Node child) {
		return USE_TYPE.USE_SWCASE;
	}
	
	@Override
	public List<Node> getChildren() {
		List<Node> list = new ArrayList<>();
		if(_siblings != null){
			for(Node node : _siblings){
				list.add(node);
			}
		}
		return list;
	}
	
	@Override
	public String simplify(Map<String, String> varTrans, Map<String, Type> allUsableVariables) {
		StringBuffer stringBuffer = new StringBuffer();
		if(_expression == null){
			stringBuffer.append("default :\n");
		} else {
			stringBuffer.append("case ");
			String expr = _expression.simplify(varTrans, allUsableVariables);
			if(expr == null){
				return null;
			}
			stringBuffer.append(expr);
			stringBuffer.append(" :\n");
		}
		boolean empty = true;
		if(_siblings != null){
			for(Node sibling : _siblings){
				String sib = sibling.simplify(varTrans, allUsableVariables);
				if(sib != null){
					empty = false;
					stringBuffer.append(sib);
					stringBuffer.append("\n");
				}
			}
		}
		if(empty){
			return null;
		}
		return stringBuffer.toString();
	}
	
	@Override
	public List<CodeBlock> reduce() {
		List<CodeBlock> list = new LinkedList<>();
		if(_siblings != null){
			for(Node node : _siblings){
				list.addAll(node.reduce());
			}
		}
		List<ASTNode> nodes = new LinkedList<>();
		nodes.add(_originalNode);
		if(_siblings != null){
			for(Node node : _siblings){
				nodes.add(node.getOriginalAST());
			}
		}
		CodeBlock codeBlock = new CodeBlock(null, null, nodes);
		list.add(codeBlock);
		return list;
	}
	
}
