/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */

package cofix.core.match;

import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Statement;
import org.junit.Test;

public class TestCollectIdentifier {

	@Test
	public void test() {
		String code = "for(int i = 0; i < weights.length; i++){ sumWts += weights[i]; }";
		Statement statement = (Statement) ASTGenerator.genAST(code, ASTParser.K_STATEMENTS);

		String code2 = "for(int i = begin; i < begin + length; i++){ dev = values[i] - mean; accum+=weights[i]*(dev*dev);accum2+=weights[i]*dev; }";
		Statement statement2 = (Statement) ASTGenerator.genAST(code2, ASTParser.K_STATEMENTS);

		Map<String, Integer> identifiers1 = new CollectIdentifier(statement).getIdentifiers();
		Map<String, Integer> identifiers2 = new CollectIdentifier(statement2).getIdentifiers();

		int similarity = 0;
		for (Entry<String, Integer> entry : identifiers1.entrySet()) {
			Integer value = identifiers2.get(entry.getKey());
			if (value != null && (entry.getValue() == value || (entry.getValue() > 1 && value > 1))) {
				System.out.println(
						"find similar variable : " + entry.getKey() + " : " + value + " ? " + entry.getValue());
				similarity += 1;
			}
		}
		System.out.println(similarity);

	}

}
