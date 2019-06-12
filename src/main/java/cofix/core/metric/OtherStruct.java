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
public class OtherStruct extends Feature {

	public static enum KIND {
		BREAK,
		CONTINUE,
		RETURN,
		THROW
	}
	
	private KIND _kind = null;
	
	public OtherStruct(Node node, KIND kind) {
		super(node);
		_kind = kind;
	}

	public KIND getKind(){
		return _kind;
	}
	
}
