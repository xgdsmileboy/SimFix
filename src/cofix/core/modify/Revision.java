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
 * @datae Jun 30, 2017
 */
public class Revision extends Modification {

	public Revision(Node node, int srcID, String target, TYPE changeNodeType) {
		super(node, srcID, target, changeNodeType);
		// TODO Auto-generated constructor stub
	}

}