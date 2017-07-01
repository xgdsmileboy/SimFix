/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.core.match;

import cofix.core.metric.NewFVector;
import cofix.core.parser.node.CodeBlock;

/**
 * @author Jiajun
 * @datae Jun 29, 2017
 */
public class CodeBlockMatcher {

	public static double getSimilarity(CodeBlock buggyCode, CodeBlock codeBlock){
		NewFVector buggy = buggyCode.getFeatureVector();
		NewFVector other = codeBlock.getFeatureVector();
		return buggy.computeSimilarity(other, NewFVector.ALGO.COSINE);
	}
	
}
