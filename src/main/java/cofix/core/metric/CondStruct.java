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
 * @date Jun 28, 2017
 */
public class CondStruct extends Feature {

	public static enum KIND {
		IF, //if-else
		SC, // switch-case
		CE  // conditional-expression
	}
	
	private KIND _kind = null;
	
	public CondStruct(Node node, KIND kind) {
		super(node);
		_kind = kind;
	}

	public KIND getKind(){
		return _kind;
	}
}
