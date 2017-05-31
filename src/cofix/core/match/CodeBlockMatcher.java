/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.core.match;

import java.util.ArrayList;
import java.util.List;

import cofix.common.astnode.CodeBlock;

/**
 * @author Jiajun
 * @datae May 31, 2017
 */
public class CodeBlockMatcher {
	
	List<Metric> _metrics = new ArrayList<>();
	
	public CodeBlockMatcher(List<Metric> metrics){
		_metrics = metrics;
	}
	
	public float getSimilirity(CodeBlock src, CodeBlock tar){
		float similarity = 0.0f;
		for(Metric metric : _metrics){
			similarity += metric.getSimilarity(src, tar);
		}
		return similarity;
	}
	
}
