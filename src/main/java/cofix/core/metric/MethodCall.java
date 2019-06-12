/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.core.metric;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.Type;

import cofix.core.parser.NodeUtils;
import cofix.core.parser.node.Node;
import cofix.core.parser.node.expr.Expr;

/**
 * @author Jiajun
 * @date Jun 23, 2017
 */
public class MethodCall extends Feature {
	
	private String _name = null;
	private List<Expr> _arguments = null;
	
	public MethodCall(Node node, String name, List<Expr> args) {
		super(node);
		_name = name;
		_arguments = args;
	}
	
	public String getName(){
		return _name;
	}
	
	public Map<String, String> matchArgs(MethodCall other){
		Map<String, String> map = new HashMap<>();
		List<String> source = new ArrayList<>();
		for(int i = 0; i < _arguments.size(); i++){
			Type type = _arguments.get(i).getType();
			String typeStr = type == null ? "?" : type.toString();
			source.add(typeStr);
		}
		
		List<String> tar = new ArrayList<>();
		for(int i = 0; i < other._arguments.size(); i++){
			Type type = other._arguments.get(i).getType();
			String typeStr = type == null ? "?" : type.toString();
			tar.add(typeStr);
		}
		
		if(source.size() == tar.size()){
			for(int i = 0; i < source.size(); i++){
				if(source.get(i) .equals(tar.get(i))){
					String name = _arguments.get(i).toSrcString().toString();
					String newName = other._arguments.get(i).toSrcString().toString();
					String already = map.get(name);
					if(already != null){
						double existSimilary = NodeUtils.nameSimilarity(name, already);
						double similarity = NodeUtils.nameSimilarity(name, newName);
						if(similarity > existSimilary){
							map.put(name, newName);
						}
					} else {
						map.put(name, newName);
					}
				}
			}
		} else if(source.size() > tar.size()){
			int start = 0;
			for(int i = 0; i < tar.size(); i++){
				for(; start < source.size();){
					if(tar.get(i).equals(source.get(start))){
						map.put(_arguments.get(start).toSrcString().toString(), other._arguments.get(i).toSrcString().toString());
						start ++;
						break;
					}
					start ++;
				}
			}
		} else {
			int start = 0;
			for(int i = 0; i < source.size(); i++){
				for(; start < tar.size();){
					if(source.get(i).equals(tar.get(start))){
						map.put(_arguments.get(i).toSrcString().toString(), other._arguments.get(start).toSrcString().toString());
						start ++;
						break;
					}
					start ++;
				}
			}
		}
		
		
		return map;
	}
	
	@Override
	public int hashCode() {
		return _name.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null || !(obj instanceof MethodCall)){
			return false;
		}
		return _name.equals(((MethodCall)obj)._name);
	}
}
