/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.common.node.stmt;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Type;

import com.gzoltar.core.components.Method;

import cofix.common.node.Node;
import cofix.common.node.expr.Vdf;
import cofix.common.node.metric.CondStruct;
import cofix.common.node.metric.Literal;
import cofix.common.node.metric.MethodCall;
import cofix.common.node.metric.Operator;
import cofix.common.node.metric.OtherStruct;
import cofix.common.node.metric.LoopStruct;
import cofix.common.node.metric.Variable;
import cofix.common.node.modify.Modification;

/**
 * @author Jiajun
 * @datae Jun 23, 2017
 */
public class VarDeclarationStmt extends Stmt {

	private Type _declType = null;
	private List<Vdf> _fragments = null;
	
	private Type _declType_replace = null;
	private List<Vdf> _fragments_replace = null;
	
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
	}
	
	public void setDeclType(Type declType){
		_declType = declType;
	}
	
	public void setFragments(List<Vdf> fragments){
		_fragments = fragments;
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
	public StringBuffer toSrcString() {
		StringBuffer stringBuffer = new StringBuffer();
		if(_declType_replace != null){
			stringBuffer.append(_declType_replace.toString());
		} else {
			stringBuffer.append(_declType.toString());
		}
		stringBuffer.append(" ");
		if(_fragments_replace != null){
			stringBuffer.append(_fragments_replace.get(0).toSrcString());
			for(int i = 1; i < _fragments_replace.size(); i++){
				stringBuffer.append(",");
				stringBuffer.append(_fragments_replace.get(i).toSrcString());
			}
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

}
