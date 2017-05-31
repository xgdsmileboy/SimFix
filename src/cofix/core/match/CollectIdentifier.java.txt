/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */

package cofix.core.match;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.SimpleName;

import cofix.common.util.LevelLogger;

public class CollectIdentifier extends ASTVisitor {
	
	private Map<String, Integer> identifiers = new HashMap<>();

	public CollectIdentifier(ASTNode source) {
		source.accept(this);
	}
	
	public Map<String, Integer> getIdentifiers(){
		return this.identifiers;
	}
	
	@Override
	public boolean visit(SimpleName node) {
		if(LevelLogger.logON){
			LevelLogger.info("@CollectIdentifiers #visit Find identifier : " + node.getIdentifier());
		}
		String name = node.getIdentifier();
		if(name.length() > 1){
			if(identifiers.containsKey(name)){
				identifiers.put(name, identifiers.get(name) + 1);;
			} else {
				identifiers.put(name, Integer.valueOf(1));
			}
		}
		return true;
	}
	
}
