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

/**
 * @author Jiajun
 * @date Jun 23, 2017
 */
public class VarDeclarationExpr extends Expr {

	private Type _declType = null;
	private List<Vdf> _vdfs = null;
	
	private String _declType_replace = null;
	private String _vdfs_replace = null;
	
	private int TYPEID = 0;
	private int VDFID = 1;
	
	/**
	 * VariableDeclarationExpression:
     *	{ ExtendedModifier } Type VariableDeclarationFragment
     *	    { , VariableDeclarationFragment }
	 */
	public VarDeclarationExpr(int startLine, int endLine, ASTNode node) {
		super(startLine, endLine, node);
		_nodeType = TYPE.VARDECLEXPR;
	}
	
	public void setDeclType(Type declType){
		_declType = declType;
	}
	
	public void setVarDeclFrags(List<Vdf> vdfs){
		_vdfs = vdfs;
	}

	@Override
	public boolean match(Node node, Map<String, String> varTrans, Map<String, Type> allUsableVariables, List<Modification> modifications) {
		boolean match = false;
		if(node instanceof VarDeclarationExpr){
			VarDeclarationExpr other = (VarDeclarationExpr) node;
			// TODO : all referenced variable should be changed correspondingly
			if(_declType.isPrimitiveType() && other._declType.isPrimitiveType() && NodeUtils.isWidenType(_declType, other._declType)){
				match = true;
				Revision revision = new Revision(this, TYPEID, other._declType.toString(), _nodeType);
				modifications.add(revision);
			} else if(_declType.toString().equals(other._declType.toString())){
				match = true;
			}
			
			if(match){
				List<Modification> tmp = NodeUtils.listNodeMatching(this, _nodeType, _vdfs, other._vdfs, varTrans, allUsableVariables);
				if(tmp != null){
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
		if(_declType_replace != null){
			stringBuffer.append(_declType_replace);
		} else {
			stringBuffer.append(_declType.toString());
		}
		stringBuffer.append(" ");
		if(_vdfs_replace != null){
			stringBuffer.append(_vdfs_replace);
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
	
	@Override
	public void computeFeatureVector() {
		_fVector = new NewFVector();
		for(Vdf vdf : _vdfs){
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
		stringBuffer.append(_declType.toString());
		stringBuffer.append(" ");
		int index = 0;
		boolean find = false;
		for(; index < _vdfs.size(); index++){
			String vdf = _vdfs.get(index).simplify(varTrans, allUsableVariables);
			if(vdf != null){
				find = true;
				stringBuffer.append(vdf);
				break;
			}
		}
		if(!find){
			return null;
		}
		for(index ++; index < _vdfs.size(); index++){
			String vdf = _vdfs.get(index).simplify(varTrans, allUsableVariables);
			if(vdf != null){
				find = true;
				stringBuffer.append(",");
				stringBuffer.append(vdf);
			}
		}
		return stringBuffer.toString();
	}
}
