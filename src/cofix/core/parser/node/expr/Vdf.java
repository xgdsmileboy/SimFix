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

/**
 * @author Jiajun
 * @date Jun 23, 2017
 */
public class Vdf extends Node {

	private SName _identifier = null;
	private int _dimensions = 0; 
	private Expr _expression = null;
	
	private Expr _expression_replace = null;
	
	/**
	 * VariableDeclarationFragment:
     *	Identifier { Dimension } [ = Expression ]
	 */
	public Vdf(int startLine, int endLine, ASTNode node) {
		super(startLine, endLine, node);
		_nodeType = TYPE.VARDECLFRAG;
	}
	
	public Vdf(int startLine, int endLine, ASTNode node, Node parent) {
		super(startLine, endLine, node, parent);
	}
	
	public void setName(SName identifier){
		_identifier = identifier;
	}
	
	public void setDimensions(int dimensions){
		_dimensions = dimensions;
	}
	
	public void setExpression(Expr expression){
		_expression = expression;
	}

	@Override
	public boolean match(Node node, Map<String, String> varTrans, Map<String, Type> allUsableVariables, List<Modification> modifications) {
		boolean match = false;
		if(node instanceof Vdf){
			Vdf other = (Vdf) node;
			if(_expression != null && other._expression != null){
				if(_expression instanceof NumLiteral && other._expression instanceof NumLiteral){
					match = true;
					if(NodeUtils.isBoundaryValue((NumLiteral) _expression) && NodeUtils.isBoundaryValue((NumLiteral) other._expression)){
						return match;
					}
				}
				List<Modification> tmp = new ArrayList<>();
				if(!match && _expression.match(other._expression, varTrans, allUsableVariables, tmp)){
					match = true;
					modifications.addAll(tmp);
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
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean backup(Modification modification) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public StringBuffer toSrcString() {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(_identifier.toSrcString());
		for(int i = 0; i < _dimensions; i++){
			stringBuffer.append("[]");
		}
		if(_expression_replace != null){
			stringBuffer.append("=");
			stringBuffer.append(_expression_replace.toSrcString());
		} else if(_expression != null){
			stringBuffer.append("=");
			stringBuffer.append(_expression.toSrcString());
		}
		return stringBuffer;
	}

	@Override
	public List<Literal> getLiterals() {
		List<Literal> list = new LinkedList<>();
		if(_expression != null){
			list.addAll(_expression.getLiterals());
		}
		return list;
	}

	@Override
	public List<Variable> getVariables() {
		List<Variable> list = _identifier.getVariables();
		if(_expression != null){
			list.addAll(_expression.getVariables());
		}
		return list;
	}

	@Override
	public List<MethodCall> getMethodCalls() {
		List<MethodCall> list = new LinkedList<>();
		if(_expression != null){
			list.addAll(_expression.getMethodCalls());
		}
		return list;
	}

	@Override
	public List<Operator> getOperators() {
		if(_expression != null){
			return _expression.getOperators();
		}
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
	public List<LoopStruct> getLoopStruct() {
		return new LinkedList<>();
	}
	
	@Override
	public List<OtherStruct> getOtherStruct() {
		return new LinkedList<>();
	}
	
	@Override
	public void computeFeatureVector() {
		_fVector = new NewFVector();
		_fVector.combineFeature(_identifier.getFeatureVector());
		if(_expression != null){
			_fVector.combineFeature(_expression.getFeatureVector());
		}
	}

	@Override
	public USE_TYPE getUseType(Node child) {
		if(_expression != null){
			if(child == _identifier){
				return USE_TYPE.USE_ASSIGN_LHS;
			} else {
				return USE_TYPE.USE_ASSIGN_RHS;
			}
		}
		return _parent.getUseType(this);
	}
	
	@Override
	public List<Node> getChildren() {
		return new LinkedList<>();
	}
	
	@Override
	public List<CodeBlock> reduce() {
		return new LinkedList<>();
	}

	@Override
	public String simplify(Map<String, String> varTrans, Map<String, Type> allUsableVariables) {
		Map<SName, Pair<String, String>> record = NodeUtils.tryReplaceAllVariables(this, varTrans, allUsableVariables);
		if(record == null){
			return null;
		}
		NodeUtils.replaceVariable(record);
		String string = toSrcString().toString();
		NodeUtils.restoreVariables(record);
		return string;
	}

}
