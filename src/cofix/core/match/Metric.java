/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.core.match;

import cofix.common.astnode.CodeBlock;

/**
 * @author Jiajun
 * @datae May 31, 2017
 */
public abstract class Metric {

	
	protected float _weight = 0.0f;
	
	public abstract float getSimilarity(CodeBlock src, CodeBlock tar);
	
}
