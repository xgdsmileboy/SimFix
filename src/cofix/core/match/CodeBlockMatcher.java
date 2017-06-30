/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.core.match;

import cofix.core.metric.FVector;
import cofix.core.parser.node.CodeBlock;

/**
 * @author Jiajun
 * @datae Jun 29, 2017
 */
public class CodeBlockMatcher {

	public static double getSimilarity(CodeBlock buggyCode, CodeBlock codeBlock){
		FVector buggy = new FVector(buggyCode);
		FVector other = new FVector(codeBlock);
		return buggy.computeSimilarity(other, FVector.ALG.COSINE);
	}
	
	
}
