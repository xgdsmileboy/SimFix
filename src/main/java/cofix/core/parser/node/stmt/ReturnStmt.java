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

import cofix.common.util.Pair;
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
import cofix.core.modify.Revision;
import cofix.core.parser.NodeUtils;
import cofix.core.parser.node.Node;
import cofix.core.parser.node.expr.Expr;
import cofix.core.parser.node.expr.SName;

/**
 * @author Jiajun
 * @date Jun 23, 2017
 */
public class ReturnStmt extends Stmt {

	private Expr _expression = null;
	
	private String _expression_replace = null;
	
	private int EXPRID = 0;
	
	/**
	 * ReturnStatement:
     *	return [ Expression ] ;
	 */
	public ReturnStmt(int startLine, int endLine, ASTNode node) {
		this(startLine, endLine, node, null);
		_nodeType = TYPE.RETURN;
	}

	public ReturnStmt(int startLine, int endLine, ASTNode node, Node parent) {
		super(startLine, endLine, node, parent);
	}
	
	public void setExpression(Expr expression){
		_expression = expression;
	}
	
	@Override
	public boolean match(Node node, Map<String, String> varTrans, Map<String, Type> allUsableVariables, List<Modification> modifications) {
		boolean match = false;
		if(node instanceof ReturnStmt){
			ReturnStmt other = (ReturnStmt) node;
			if(_expression == null && other._expression == null){
				match = true;
			} else if(_expression != null && other._expression != null){
				String otherStr = other._expression.toSrcString().toString();
				if(_expression.getType().toString().equals(other._expression.getType().toString()) && otherStr.length() > 1){
					String source = _expression.toSrcString().toString();
					if(!source.equals(other._expression.toSrcString().toString()) && NodeUtils.isSameNodeType(_expression, other._expression)){
						Map<SName, Pair<String, String>> record = NodeUtils.tryReplaceAllVariables(other._expression, varTrans, allUsableVariables);
						if(record != null){
							NodeUtils.replaceVariable(record);
							String target = other._expression.toSrcString().toString();
							if(!source.equals(target)){
								Revision revision = new Revision(this, EXPRID, target, _nodeType);
								modifications.add(revision);
							}
							NodeUtils.restoreVariables(record);
						}
					}
				}
				List<Modification> tmp = new ArrayList<>();
				if(_expression.match(other._expression, varTrans, allUsableVariables, tmp)){
					match = true;
					if(otherStr.length() > 1){
						modifications.addAll(tmp);
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
		if(modification.getSourceID() == EXPRID){
			_expression_replace = modification.getTargetString();
			return true;
		}
		return false;
	}

	@Override
	public boolean restore(Modification modification) {
		if(modification.getSourceID() == EXPRID){
			_expression_replace = null;
			return true;
		}
		return false;
	}

	@Override
	public boolean backup(Modification modification) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public StringBuffer toSrcString() {
		StringBuffer stringBuffer = new StringBuffer("return ");
		if(_expression_replace != null){
			stringBuffer.append(_expression_replace);
		} else if(_expression != null){
			stringBuffer.append(_expression.toSrcString());
		}
		stringBuffer.append(";");
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
		if(_expression != null){
			return _expression.getVariables();
		}
		return new LinkedList<>();
	}

	@Override
	public List<LoopStruct> getLoopStruct() {
		return new LinkedList<>();
	}
	
	@Override
	public List<CondStruct> getCondStruct() {
		if(_expression != null){
			return _expression.getCondStruct();
		}
		return new LinkedList<>();
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
	public List<OtherStruct> getOtherStruct() {
		List<OtherStruct> list = new LinkedList<>();
		OtherStruct otherStruct = new OtherStruct(this, OtherStruct.KIND.RETURN);
		list.add(otherStruct);
		return list;
	}
	
	@Override
	public void computeFeatureVector() {
		_fVector = new NewFVector();
		_fVector.inc(NewFVector.INDEX_STRUCT_OTHER);
		if(_expression != null){
			_fVector.combineFeature(_expression.getFeatureVector());
		}
	}
	

	@Override
	public USE_TYPE getUseType(Node child) {
		return USE_TYPE.USE_RETURN;
	}
	
	@Override
	public List<Node> getChildren() {
		return new ArrayList<>();
	}
	
	@Override
	public String simplify(Map<String, String> varTrans, Map<String, Type> allUsableVariables) {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("return ");
		if(_expression != null){
			String expr = _expression.simplify(varTrans, allUsableVariables);
			if(expr == null){
				return null;
			}
			stringBuffer.append(expr);
		}
		stringBuffer.append(";");
		return stringBuffer.toString();
	}
}
