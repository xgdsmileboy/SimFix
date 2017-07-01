/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.core.metric;

import cofix.core.parser.node.Node;

/**
 * @author Jiajun
 * @datae Jun 23, 2017
 */
public class MethodCall extends Feature {
	
	private String _name = null;
	
	public MethodCall(Node node, String name) {
		super(node);
		_name = name;
	}
	
	public String getName(){
		return _name;
	}
}
