/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.common.node.expr;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Type;

import cofix.common.node.Node;
import cofix.common.node.metric.Literal;
import cofix.common.node.metric.MethodCall;
import cofix.common.node.metric.Operator;
import cofix.common.node.metric.Structure;
import cofix.common.node.metric.Variable;
import cofix.common.node.modify.Modification;
import cofix.common.node.stmt.AnonymousClassDecl;

/**
 * @author Jiajun
 * @datae Jun 23, 2017
 */
public class ClassInstanceCreate extends Expr {

	private Expr _expression = null;
	private Type _classType = null;
	private List<Expr> _arguments = null;
	private AnonymousClassDecl _decl = null;
	
	/**
	 * ClassInstanceCreation:
     *   [ Expression . ]
     *       new [ < Type { , Type } > ]
     *       Type ( [ Expression { , Expression } ] )
     *       [ AnonymousClassDeclaration ]
	 */
	public ClassInstanceCreate(int startLine, int endLine, ASTNode node) {
		super(startLine, endLine, node);
	}
	
	public void setExpression(Expr expression){
		_expression = expression;
	}
	
	public void setClassType(Type classType){
		_classType = classType;
	}
	
	public void setArguments(List<Expr> arguments){
		_arguments = arguments;
	}
	
	public void setAnonymousClassDecl(AnonymousClassDecl decl){
		_decl = decl;
	}

	@Override
	public boolean match(Node node, Map<String, Type> allUsableVariables, List<Modification> modifications) {
		// TODO Auto-generated method stub
		return false;
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
	public List<Literal> getLiterals() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Variable> getVariables() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Structure> getStructures() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<MethodCall> getMethodCalls() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Operator> getOperators() {
		// TODO Auto-generated method stub
		return null;
	}
}
