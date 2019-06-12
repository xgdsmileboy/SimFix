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
import cofix.core.modify.Modification;
import cofix.core.parser.NodeUtils;
import cofix.core.parser.node.Node;

/**
 * @author Jiajun
 * @date Jun 28, 2017
 */
public class Svd extends Expr {
	
	private Type _decType = null;
	private SName _name = null;
	private Expr _initializer = null;
	
	private Expr _initializer_replace = null;
	
	/**
	 * { ExtendedModifier } Type {Annotation} [ ... ] Identifier { Dimension } [ = Expression ]
	 * "..." should not be appear since it is only used in method declarations
	 */
	public Svd(int startLine, int endLine, ASTNode node) {
		super(startLine, endLine, node);
		_nodeType = TYPE.SINGLEVARDECL;
	}
	
	public void setDecType(Type decType){
		_decType = decType;
	}
	
	public void setName(SName name){
		_name = name;
	}
	
	public SName getName(){
		return _name;
	}
	
	public void setInitializer(Expr initializer){
		_initializer = initializer;
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
	public boolean match(Node node, Map<String, String> varTrans, Map<String, Type> allUsableVariables, List<Modification> modifications) {
		boolean match = false;
		if(node instanceof Svd){
			match = true;
			// TODO : to finish
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
	public StringBuffer toSrcString() {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(_decType);
		stringBuffer.append(" ");
		stringBuffer.append(_name.toSrcString());
		if(_initializer_replace != null){
			stringBuffer.append("=");
			stringBuffer.append(_initializer_replace.toSrcString());
		} else if(_initializer != null){
			stringBuffer.append("=");
			stringBuffer.append(_initializer.toSrcString());
		}
		return stringBuffer;
	}

	@Override
	public List<Literal> getLiterals() {
		if(_initializer != null){
			return _initializer.getLiterals();
		}
		return new LinkedList<>();
	}

	@Override
	public List<Variable> getVariables() {
		List<Variable> list = _name.getVariables();
		if(_initializer != null){
			list.addAll(_initializer.getVariables());
		}
		return list;
	}
	
	@Override
	public List<CondStruct> getCondStruct() {
		if(_initializer != null){
			return _initializer.getCondStruct();
		}
		return new LinkedList<>();
	}
	
	@Override
	public List<Operator> getOperators() {
		if(_initializer != null){
			return _initializer.getOperators();
		}
		return new LinkedList<>();
	}
	
	@Override
	public List<MethodCall> getMethodCalls() {
		if(_initializer != null){
			return _initializer.getMethodCalls();
		}
		return new LinkedList<>();
	}
	
	@Override
	public void computeFeatureVector() {
		_fVector = new NewFVector();
		_fVector.combineFeature(_name.getFeatureVector());
		if(_initializer != null){
			_fVector.combineFeature(_initializer.getFeatureVector());
		}
	}
	
	@Override
	public List<Node> getChildren() {
		return new ArrayList<>();
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
