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
import cofix.core.metric.Literal;
import cofix.core.metric.LoopStruct;
import cofix.core.metric.MethodCall;
import cofix.core.metric.NewFVector;
import cofix.core.metric.Operator;
import cofix.core.metric.Variable;
import cofix.core.metric.Variable.USE_TYPE;
import cofix.core.modify.Modification;
import cofix.core.parser.NodeUtils;
import cofix.core.parser.node.Node;
import cofix.core.parser.node.stmt.AnonymousClassDecl;

/**
 * @author Jiajun
 * @date Jun 23, 2017
 */
public class ClassInstanceCreate extends Expr {

	private Expr _expression = null;
	private Type _classType = null;
	private List<Expr> _arguments = null;
	private AnonymousClassDecl _decl = null;
	
	private String _expression_replace = null;
	private String _arguments_replace = null;
	
	private final int ARGID = 0;
	
	/**
	 * ClassInstanceCreation:
     *   [ Expression . ]
     *       new [ < Type { , Type } > ]
     *       Type ( [ Expression { , Expression } ] )
     *       [ AnonymousClassDeclaration ]
	 */
	public ClassInstanceCreate(int startLine, int endLine, ASTNode node) {
		super(startLine, endLine, node);
		_nodeType = TYPE.CLASSCREATION;
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
	
	public Type getClassType(){
		return _classType;
	}

	@Override
	public boolean match(Node node, Map<String, String> varTrans, Map<String, Type> allUsableVariables, List<Modification> modifications) {
		boolean match = false;
		if(node instanceof ClassInstanceCreate){
			ClassInstanceCreate other = (ClassInstanceCreate) node;
			if(_classType.toString().equals(other._classType)){
				match = true;
				modifications.addAll(NodeUtils.handleArguments(this, ARGID, _nodeType, _arguments, other._arguments, varTrans, allUsableVariables));
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
		if(modification.getSourceID() == ARGID){
			_arguments_replace = modification.getTargetString();
			return true;
		}
		return false;
	}

	@Override
	public boolean restore(Modification modification) {
		if(modification.getSourceID() == ARGID){
			_arguments_replace = null;
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
		StringBuffer stringBuffer = new StringBuffer();
		if(_expression != null){
			if(_expression_replace != null){
				stringBuffer.append(_expression_replace);
			} else {
				stringBuffer.append(_expression);
			}
			stringBuffer.append(".");
		}
		stringBuffer.append("new ");
		stringBuffer.append(_classType);
		stringBuffer.append("(");
		if(_arguments_replace != null){
			stringBuffer.append(_arguments_replace);
		}else if(_arguments != null && _arguments.size() > 0){
			stringBuffer.append(_arguments.get(0).toSrcString());
			for(int i = 1; i < _arguments.size(); i++){
				stringBuffer.append(",");
				stringBuffer.append(_arguments.get(i).toSrcString());
			}
		}
		stringBuffer.append(")");
		if(_decl != null){
			stringBuffer.append(_decl.toSrcString());
		}
		return stringBuffer;
	}
	
	@Override
	public List<Literal> getLiterals() {
		List<Literal> list = new LinkedList<>();
		if(_expression != null){
			list.addAll(_expression.getLiterals());
		}
		for(Expr expr : _arguments){
			list.addAll(expr.getLiterals());
		}
		if(_decl != null){
			list.addAll(_decl.getLiterals());
		}
		return list;
	}

	@Override
	public List<Variable> getVariables() {
		List<Variable> list = new LinkedList<>();
		if(_expression != null){
			list.addAll(_expression.getVariables());
		}
		for(Expr expr : _arguments){
			list.addAll(expr.getVariables());
		}
		if(_decl != null){
			list.addAll(_decl.getVariables());
		}
		return list;
	}

	@Override
	public List<LoopStruct> getLoopStruct() {
		List<LoopStruct> list = new LinkedList<>();
		if(_decl != null){
			list.addAll(_decl.getLoopStruct());
		}
		return list;
	}

	@Override
	public List<MethodCall> getMethodCalls() {
		List<MethodCall> list = new LinkedList<>();
		MethodCall methodCall = new MethodCall(this, _classType.toString(), _arguments);
		list.add(methodCall);
		if(_decl != null){
			list.addAll(_decl.getMethodCalls());
		}
		return list;
	}

	@Override
	public List<Operator> getOperators() {
		List<Operator> list = new LinkedList<>();
		if(_decl != null){
			list.addAll(_decl.getOperators());
		}
		return list;
	}
	
	@Override
	public void computeFeatureVector() {
		_fVector = new NewFVector();
		_fVector.inc(NewFVector.INDEX_MCALL);
		if(_expression != null){
			_fVector.combineFeature(_expression.getFeatureVector());
		}
		if(_arguments != null){
			for(Expr expr : _arguments){
				_fVector.combineFeature(expr.getFeatureVector());
			}
		}
	}
	

	@Override
	public USE_TYPE getUseType(Node child) {
		if(_expression == child){
			return USE_TYPE.USE_METHOD_EXP;
		} else {
			return USE_TYPE.USE_METHOD_PARAM;
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
