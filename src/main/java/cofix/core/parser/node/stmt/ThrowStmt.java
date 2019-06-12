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
import cofix.core.metric.Literal;
import cofix.core.metric.MethodCall;
import cofix.core.metric.NewFVector;
import cofix.core.metric.Operator;
import cofix.core.metric.OtherStruct;
import cofix.core.metric.Variable;
import cofix.core.metric.Variable.USE_TYPE;
import cofix.core.modify.Insertion;
import cofix.core.modify.Modification;
import cofix.core.modify.Revision;
import cofix.core.parser.NodeUtils;
import cofix.core.parser.node.Node;
import cofix.core.parser.node.expr.ClassInstanceCreate;
import cofix.core.parser.node.expr.Expr;
import cofix.core.parser.node.expr.SName;

/**
 * @author Jiajun
 * @date Jun 23, 2017
 */
public class ThrowStmt extends Stmt {

	private Expr _expression = null;
	
	private List<String> _insertBefore = new ArrayList<>();;
	private String _expression_replace = null;
	
	private final int EXPID = 0; 
	private final int INSID = 1;
	
	/**
	 * ThrowStatement:
     *	throw Expression ;
	 */
	public ThrowStmt(int startLine, int endLine, ASTNode node) {
		this(startLine, endLine, node, null);
	}

	public ThrowStmt(int startLine, int endLine, ASTNode node, Node parent) {
		super(startLine, endLine, node, parent);
		_nodeType = TYPE.THROW;
	}
	
	public void setExpression(Expr expression){
		_expression = expression;
	}
	
	public String getExceptionType(){
		if(_expression instanceof ClassInstanceCreate){
			return ((ClassInstanceCreate)_expression).getClassType().toString();
		} else {
			return _expression.getType().toString();
		}
	}
	
	@Override
	public boolean match(Node node, Map<String, String> varTrans, Map<String, Type> allUsableVariables, List<Modification> modifications) {
		boolean match = false;
		if(node instanceof ThrowStmt){
			match = true;
			ThrowStmt other = (ThrowStmt) node;
			String source = _expression.toSrcString().toString(); 
			if(!source.equals(other._expression.toSrcString().toString())){
				Map<SName, Pair<String, String>> record = NodeUtils.tryReplaceAllVariables(other._expression, varTrans, allUsableVariables);
				if(record != null){
					NodeUtils.replaceVariable(record);
					String target = other._expression.toSrcString().toString();
					if(!source.equals(target)){
						Revision revision = new Revision(this, EXPID, target, _nodeType);
						modifications.add(revision);
					}
					NodeUtils.restoreVariables(record);
				}
			}
		} else if(node instanceof ReturnStmt){
			match = true;
			if(node.getParent() != null){
				switch(node.getParent().getUseType(node)){
				case USE_IF :
					Node parent = node.getParent();
					while(parent != null){
						if(parent instanceof IfStmt){
							break;
						}
						parent = parent.getParent();
					}
					if(parent != null){
						Map<SName, Pair<String, String>> record = NodeUtils.tryReplaceAllVariables(parent, varTrans, allUsableVariables);
						if(record != null){
							NodeUtils.replaceVariable(record);
							Insertion insertion = new Insertion(this, INSID, parent.toSrcString().toString(), _nodeType);
							modifications.add(insertion);
							NodeUtils.restoreVariables(record);
						}
					}
					break;
				default:
					
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
		switch (modification.getSourceID()) {
		case EXPID:
			_expression_replace = modification.getTargetString();
			break;
		case INSID:
			_insertBefore.add(modification.getTargetString());
			break;
		}
		return true;
	}

	@Override
	public boolean restore(Modification modification) {
		switch (modification.getSourceID()) {
		case EXPID:
			_expression_replace = null;
			break;
		case INSID:
			_insertBefore.remove(modification.getTargetString());
			break;
		}
		return true;
	}

	@Override
	public boolean backup(Modification modification) {
		return true;
	}
	
	@Override
	public StringBuffer toSrcString() {
		StringBuffer stringBuffer = new StringBuffer();
		if(_insertBefore.size() > 0){
			for(String string : _insertBefore){
				stringBuffer.append(string);
				stringBuffer.append("\n");
			}
		}
		stringBuffer.append("throw ");
		if(_expression_replace != null){
			stringBuffer.append(_expression_replace);
		} else {
			stringBuffer.append(_expression.toSrcString());
		}
		stringBuffer.append(";");
		return stringBuffer;
	}

	@Override
	public List<Literal> getLiterals() {
		return _expression.getLiterals();
	}

	@Override
	public List<Variable> getVariables() {
		return _expression.getVariables();
	}

	@Override
	public List<MethodCall> getMethodCalls() {
		return _expression.getMethodCalls();
	}

	@Override
	public List<Operator> getOperators() {
		return _expression.getOperators();
	}

	@Override
	public List<OtherStruct> getOtherStruct() {
		List<OtherStruct> list = new LinkedList<>();
		OtherStruct otherStruct = new OtherStruct(this, OtherStruct.KIND.THROW);
		list.add(otherStruct);
		list.addAll(_expression.getOtherStruct());
		return list;
	}
	
	@Override
	public void computeFeatureVector() {
		_fVector = new NewFVector();
		_fVector.inc(NewFVector.INDEX_STRUCT_OTHER);
		_fVector.combineFeature(_expression.getFeatureVector());
	}
	

	@Override
	public USE_TYPE getUseType(Node child) {
		return USE_TYPE.USE_THROW;
	}
	
	@Override
	public List<Node> getChildren() {
		return new ArrayList<>();
	}
	
	@Override
	public String simplify(Map<String, String> varTrans, Map<String, Type> allUsableVariables) {
		String string = _expression.simplify(varTrans, allUsableVariables);
		if(string == null){
			return null;
		}
		return "throw " + string + ";";
	}
}
