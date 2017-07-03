/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.core.metric;

import org.eclipse.jdt.core.dom.Type;

import cofix.core.parser.node.Node;

/**
 * @author Jiajun
 * @datae Jun 23, 2017
 */
public class Variable extends Feature {

	public static enum USE_TYPE{
		USE_METHOD_EXP,
		USE_METHOD_PARAM,
		USE_INFIX_EXP,
		USE_POSTFIX_EXP,
		USE_PREFIX_EXP,
		USE_ARR_ACC,
		USE_VAR_DECL,
		USE_ASSIGN_LHS,
		USE_ASSIGN_RHS,
		USE_CONDITIONAL,
		USE_LOOP,
		USE_IF,
		USE_RETURN,
		USE_SWCASE,
		USE_SWSTMT,
		USE_SYNC,
		USE_THROW,
		USE_TRY,
		USE_UNKNOWN
	}
	
	private String _name = null;
	private Type _type = null;
	private USE_TYPE _kind = null;
	
	public Variable(Node node, String name, Type type) {
		super(node);
		_name = name;
		_type = type;
		if(node != null){
			_kind = node.getUseType(node);
		} else {
			_kind = USE_TYPE.USE_UNKNOWN;
		}
	}
	
	public String getName(){
		return _name;
	}
	
	public USE_TYPE getUseType(){
		return _kind;
	}
	
	@Override
	public int hashCode() {
		return _name.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null || !(obj instanceof Variable)){
			return false;
		}
		Variable other = (Variable) obj;
		if(!_name.equals(other._name)){
			return false;
		}
		if(_type == other._type){
			return true;
		}
		if(_type == null || other._type == null){
			return false;
		}
		return  _type.toString().equals(other._type.toString());
	}
	
	@Override
	public String toString() {
		return _name;
	}
}
