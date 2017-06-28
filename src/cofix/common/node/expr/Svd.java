/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.common.node.expr;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Type;

import com.sun.org.apache.xml.internal.security.Init;

import cofix.common.node.Node;
import cofix.common.node.metric.CondStruct;
import cofix.common.node.metric.Literal;
import cofix.common.node.metric.LoopStruct;
import cofix.common.node.metric.MethodCall;
import cofix.common.node.metric.Operator;
import cofix.common.node.metric.OtherStruct;
import cofix.common.node.metric.Variable;
import cofix.common.node.modify.Modification;

/**
 * @author Jiajun
 * @datae Jun 28, 2017
 */
public class Svd extends Expr {
	
	private Type _decType = null;
	private SName _name = null;
	private Expr _initializer = null;
	
	/**
	 * { ExtendedModifier } Type {Annotation} [ ... ] Identifier { Dimension } [ = Expression ]
	 */
	public Svd(int startLine, int endLine, ASTNode node) {
		super(startLine, endLine, node);
	}
	
	public void setDecType(Type decType){
		_decType = decType;
	}
	
	public void setName(SName name){
		_name = name;
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
	public boolean match(Node node, Map<String, Type> allUsableVariables, List<Modification> modifications) {
		// TODO Auto-generated method stub
		return false;
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

}
