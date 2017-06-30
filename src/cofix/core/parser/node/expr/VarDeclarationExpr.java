/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.core.parser.node.expr;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Type;

import cofix.core.metric.CondStruct;
import cofix.core.metric.Literal;
import cofix.core.metric.MethodCall;
import cofix.core.metric.Operator;
import cofix.core.metric.Variable;
import cofix.core.modify.Modification;
import cofix.core.parser.node.Node;

/**
 * @author Jiajun
 * @datae Jun 23, 2017
 */
public class VarDeclarationExpr extends Expr {

	private Type _declType = null;
	private List<Vdf> _vdfs = null;
	
	private Type _declType_replace = null;
	private List<Vdf> _vdfs_replace = null;
	
	/**
	 * VariableDeclarationExpression:
     *	{ ExtendedModifier } Type VariableDeclarationFragment
     *	    { , VariableDeclarationFragment }
	 */
	public VarDeclarationExpr(int startLine, int endLine, ASTNode node) {
		super(startLine, endLine, node);
	}
	
	public void setDeclType(Type declType){
		_declType = declType;
	}
	
	public void setVarDeclFrags(List<Vdf> vdfs){
		_vdfs = vdfs;
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
			stringBuffer.append(_declType_replace);
		} else {
			stringBuffer.append(_declType);
		}
		stringBuffer.append(" ");
		if(_vdfs_replace != null){
			stringBuffer.append(_vdfs_replace.get(0).toSrcString());
			for(int i = 1; i < _vdfs_replace.size(); i++){
				stringBuffer.append(",");
				stringBuffer.append(_vdfs_replace.get(i).toSrcString());
			}
		} else {
			stringBuffer.append(_vdfs.get(0).toSrcString());
			for(int i = 1; i < _vdfs.size(); i++){
				stringBuffer.append(",");
				stringBuffer.append(_vdfs.get(i).toSrcString());
			}
		}
		return stringBuffer;
	}

	@Override
	public List<Literal> getLiterals() {
		List<Literal> list = new LinkedList<>();
		for(Vdf vdf : _vdfs){
			list.addAll(vdf.getLiterals());
		}
		return list;
	}

	@Override
	public List<Variable> getVariables() {
		List<Variable> list = new LinkedList<>();
		for(Vdf vdf : _vdfs){
			list.addAll(vdf.getVariables());
		}
		return list;
	}

	@Override
	public List<MethodCall> getMethodCalls() {
		List<MethodCall> list = new LinkedList<>();
		for(Vdf vdf : _vdfs){
			list.addAll(vdf.getMethodCalls());
		}
		return list;
	}

	@Override
	public List<Operator> getOperators() {
		List<Operator> list = new LinkedList<>();
		for(Vdf vdf : _vdfs){
			list.addAll(vdf.getOperators());
		}
		return list;
	}
	
	@Override
	public List<CondStruct> getCondStruct() {
		List<CondStruct> list = new LinkedList<>();
		for(Vdf vdf : _vdfs){
			list.addAll(vdf.getCondStruct());
		}
		return list;
	}
}
