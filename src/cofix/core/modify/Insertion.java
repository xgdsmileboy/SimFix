/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.core.modify;

import cofix.core.parser.node.Node;
import cofix.core.parser.node.Node.TYPE;

/**
 * @author Jiajun
 * @date Jun 30, 2017
 */
public class Insertion extends Modification {

	public Insertion(Node node, int srcID, String target, TYPE changeNodeType) {
		super(node, srcID, target, changeNodeType, 1);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String toString() {
		return "[INS | " +_nodeType + " | " + _sourceID + "]" + _target;
	}
}
