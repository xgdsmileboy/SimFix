/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.core.parser.node.stmt;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Type;

import cofix.core.metric.CondStruct;
import cofix.core.metric.Literal;
import cofix.core.metric.MethodCall;
import cofix.core.metric.NewFVector;
import cofix.core.metric.Operator;
import cofix.core.metric.Variable;
import cofix.core.metric.Variable.USE_TYPE;
import cofix.core.modify.Modification;
import cofix.core.modify.Revision;
import cofix.core.parser.NodeUtils;
import cofix.core.parser.node.Node;
import cofix.core.parser.node.expr.Vdf;

/**
 * @author Jiajun
 * @date Jun 23, 2017
 */
public class VarDeclarationStmt extends Stmt {

	private String _modifier = null;
	private Type _declType = null;
	private List<Vdf> _fragments = null;
	
	private String _declType_replace = null;
	private String _fragments_replace = null;
	
	private int TYPEID = 0;
	private int FRAGID = 1;
	/**
	 * VariableDeclarationStatement:
     *	{ ExtendedModifier } Type VariableDeclarationFragment
     *	   { , VariableDeclarationFragment } ;
	 */
	public VarDeclarationStmt(int startLine, int endLine, ASTNode node) {
		this(startLine, endLine, node, null);
	}

	public VarDeclarationStmt(int startLine, int endLine, ASTNode node, Node parent) {
		super(startLine, endLine, node, parent);
		_nodeType = TYPE.VARDECLSTMT;
	}
	
	/**
	 * @param modifier the modifier to set
	 */
	public void setModifier(String modifier) {
		this._modifier = modifier;
	}
	
	public void setDeclType(Type declType){
		_declType = declType;
	}
	
	public void setFragments(List<Vdf> fragments){
		_fragments = fragments;
	}
	
	@Override
	public boolean match(Node node, Map<String, String> varTrans, Map<String, Type> allUsableVariables, List<Modification> modifications) {
		boolean match = false;
		if(node instanceof VarDeclarationStmt){
//			match = true;
			VarDeclarationStmt other = (VarDeclarationStmt) node;
			Set<Vdf> record = new HashSet<>();
			for(Vdf vdf : _fragments){
				for(Vdf ovdf: other._fragments){
					if(record.contains(ovdf)){
						continue;
					}
					List<Modification> tmp = new ArrayList<>();
					if(vdf.match(ovdf, varTrans, allUsableVariables, tmp)){
						match = true;
						record.add(ovdf);
						modifications.addAll(tmp);
					}
				}
			}
			if(match){
				Type otherType = other._declType;
				if(_declType.isPrimitiveType() && otherType.isPrimitiveType()){
					if(!_declType.toString().equals(otherType.toString())){
						Revision revision = new Revision(this, TYPEID, otherType.toString(), _nodeType);
						modifications.add(revision);
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
		if(modification.getSourceID() == TYPEID){
			_declType_replace = modification.getTargetString();
		} else if(modification.getSourceID() == FRAGID){
			_fragments_replace = modification.getTargetString();
		}
		return true;
	}

	@Override
	public boolean restore(Modification modification) {
		if(modification.getSourceID() == TYPEID){
			_declType_replace = null;
		} else if(modification.getSourceID() == FRAGID){
			_fragments_replace = null;
		}
		return true;
	}

	@Override
	public boolean backup(Modification modification) {
		return false;
	}
	
	@Override
	public StringBuffer toSrcString() {
		StringBuffer stringBuffer = new StringBuffer();
		if(_modifier != null){
			stringBuffer.append(_modifier + " ");
		}
		if(_declType_replace != null){
			stringBuffer.append(_declType_replace);
		} else {
			stringBuffer.append(_declType.toString());
		}
		stringBuffer.append(" ");
		if(_fragments_replace != null){
			stringBuffer.append(_fragments_replace);
		} else {
			stringBuffer.append(_fragments.get(0).toSrcString());
			for(int i = 1; i < _fragments.size(); i++){
				stringBuffer.append(",");
				stringBuffer.append(_fragments.get(i).toSrcString());
			}
		}
		stringBuffer.append(";");
		return stringBuffer;
	}
	
	@Override
	public List<Literal> getLiterals() {
		List<Literal> list = new LinkedList<>();
		if(_fragments != null){
			for(Vdf vdf : _fragments){
				list.addAll(vdf.getLiterals());
			}
		}
		return list;
	}

	@Override
	public List<Variable> getVariables() {
		List<Variable> list = new LinkedList<>();
		if(_fragments != null){
			for(Vdf vdf : _fragments){
				list.addAll(vdf.getVariables());
			}
		}
		return list;
	}

	@Override
	public List<MethodCall> getMethodCalls() {
		List<MethodCall> list = new LinkedList<>();
		if(_fragments != null){
			for(Vdf vdf : _fragments){
				list.addAll(vdf.getMethodCalls());
			}
		}
		return list;
	}

	@Override
	public List<Operator> getOperators() {
		List<Operator> list = new LinkedList<>();
		if(_fragments != null){
			for(Vdf vdf : _fragments){
				list.addAll(vdf.getOperators());
			}
		}
		return list;
	}
	
	@Override
	public List<CondStruct> getCondStruct() {
		List<CondStruct> list = new LinkedList<>();
		if(_fragments != null){
			for(Vdf vdf : _fragments){
				list.addAll(vdf.getCondStruct());
			}
		}
		return list;
	}
	
	@Override
	public void computeFeatureVector() {
		_fVector = new NewFVector();
		for(Vdf vdf : _fragments){
			_fVector.combineFeature(vdf.getFeatureVector());
		}
	}
	

	@Override
	public USE_TYPE getUseType(Node child) {
		return USE_TYPE.USE_VAR_DECL;
	}

	@Override
	public List<Node> getChildren() {
		return new ArrayList<>();
	}
	
	@Override
	public String simplify(Map<String, String> varTrans, Map<String, Type> allUsableVariables) {
		StringBuffer stringBuffer = new StringBuffer();
		if(_modifier != null){
			stringBuffer.append(_modifier + " ");
		}
		stringBuffer.append(_declType.toString());
		stringBuffer.append(" ");
		int index = 0;
		boolean find = false;
		for(; index < _fragments.size(); index ++){
			String frag = _fragments.get(index).simplify(varTrans, allUsableVariables);
			if(frag != null){
				find = true;
				stringBuffer.append(frag);
				break;
			}
		}
		if(!find){
			return null;
		}
		for(index ++; index < _fragments.size(); index++){
			String string = _fragments.get(index).simplify(varTrans, allUsableVariables);
			if(string != null){
				stringBuffer.append(",");
				stringBuffer.append(string);
			}
		}
		
		stringBuffer.append(";");
		return stringBuffer.toString();	
	}
	
}
