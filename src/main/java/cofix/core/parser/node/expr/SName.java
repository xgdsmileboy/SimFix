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

import javax.print.attribute.standard.MediaSize.Other;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.WildcardType;

import com.sun.xml.internal.bind.v2.runtime.Name;

import cofix.common.util.Pair;
import cofix.core.metric.Literal;
import cofix.core.metric.NewFVector;
import cofix.core.metric.Variable;
import cofix.core.modify.Modification;
import cofix.core.modify.Revision;
import cofix.core.parser.NodeUtils;
import cofix.core.parser.node.Node;

/**
 * @author Jiajun
 * @date Jun 23, 2017
 */
public class SName extends Label {

	private String _name = null; 
	
	private Expr _directDependency = null;
	
	private String _replace = null;
	
	private final int NAMEID = 0;
	
	/**
	 * SimpleName:
     *	Identifier
	 */
	public SName(int startLine, int endLine, ASTNode node) {
		super(startLine, endLine, node);
		_nodeType = TYPE.SNAME;
	}
	
	public void setName(String name){
		_name = name;
	}
	
	public String getName(){
		return _name;
	}
	
	public void setDirectDependency(Expr expr){
		_directDependency = expr;
	}
	
	public Expr getDirectDependency(){
		return _directDependency;
	}

	@Override
	public boolean adapt(Modification modification) {
		if(modification.getSourceID() == NAMEID){
			_replace = modification.getTargetString();
			return true;
		}
		return false;
	}

	@Override
	public boolean restore(Modification modification) {
		if(modification.getSourceID() == NAMEID){
			_replace = null;
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
	public boolean match(Node node, Map<String, String> varTrans, Map<String, Type> allUsableVariables, List<Modification> modifications) {
		boolean match = false;
		if(node instanceof SName){
			SName other = (SName) node;
			if(_name.equals(other.getName())){
				match = true;
				// try to replace a variable using an similar one
			} else {
				if(_exprType.toString().equals(((SName) node).getType())){
					match = true;
					if(!other.getName().equals(_name) && allUsableVariables.containsKey(_name) && !NodeUtils.isClass(_name) && !NodeUtils.isClass(other._name)){
						Revision revision = new Revision(this, NAMEID, other.getName(), _nodeType);
						modifications.add(revision);
					}
				} else {
					if((_exprType.isPrimitiveType() && other.getType().isPrimitiveType() && NodeUtils.isWidenType(_exprType, other.getType()))
							|| (_exprType instanceof WildcardType || other.getType() instanceof WildcardType ) || _exprType.toString().equals(other.getType().toString())){
						match = true;
						if(!other.getName().equals(_name) && allUsableVariables.containsKey(_name) && !NodeUtils.isClass(_name) && !NodeUtils.isClass(other._name)){
							Revision revision = new Revision(this, NAMEID, other.getName(), _nodeType);
							modifications.add(revision);
						}
					}
				}
			}
		} else {
			if(node instanceof MethodInv) {
				MethodInv methodInv = (MethodInv) node;
				if(_exprType.toString().equals(methodInv.getType().toString())) {
					Map<SName, Pair<String, String>> record = NodeUtils.tryReplaceAllVariables(methodInv, varTrans, allUsableVariables);
					if(record != null) {
						NodeUtils.replaceVariable(record);
						Revision revision = new Revision(this, NAMEID, methodInv.toSrcString().toString(), _nodeType);
						modifications.add(revision);
					}
				}
			}
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
		if(_replace != null){
			return new StringBuffer(_replace);
		}
		return new StringBuffer(_name);
	}

	@Override
	public List<Literal> getLiterals() {
		return new LinkedList<>();
	}

	@Override
	public List<Variable> getVariables() {
		List<Variable> list = new LinkedList<>();
		if(!Character.isUpperCase(_name.charAt(0))){
			Variable variable = new Variable(this, _name, _exprType);
			list.add(variable);
		}
		return list;
	}
	
	@Override
	public void computeFeatureVector() {
		_fVector = new NewFVector();
		_fVector.inc(NewFVector.INDEX_VAR);
	}
	
	@Override
	public String toString() {
		return _name;
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
