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

	private String _name = null;
	private Type _type = null;
	
	public Variable(Node node, String name, Type type) {
		super(node);
		_name = name;
		_type = type;
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
}
