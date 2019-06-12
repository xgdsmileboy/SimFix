/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */

package cofix.core.match;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Statement;
import org.junit.Test;

public class TestSearchSimilarCode {

	@Test
	public void test_lang_60(){
		String path = "testfile/StrBuilder.java";
		CompilationUnit unit = ASTGenerator.genAST(path);
		
		String search = "for (int i = 0; i < thisBuf.length; i++) {"
						+ "if (thisBuf[i] == ch) {"
						+ "return true;"
            			+ "}"
        				+ "}";
		
		Block searchstatement = (Block) ASTGenerator.genAST(search, ASTParser.K_STATEMENTS);

		SearchSimilarCode searchSimilarCode = new SearchSimilarCode((Statement)searchstatement.statements().get(0), unit, 0.3f);
		List<ASTNode> matches = searchSimilarCode.getMatches();
		for(ASTNode node : matches){
			System.out.println(node.toString());
		}
	}
	
	@Test
	public void test_Math_41() {
		String path = "testfile/Variance.java";
		
		CompilationUnit unit = ASTGenerator.genAST(path);
		
		String search = "for (int i = 0; i < weights.length; i++) {"
                    	+ "sumWts += weights[i];"
                		+  "}";
		Block searchstatement = (Block)ASTGenerator.genAST(search, ASTParser.K_STATEMENTS);
		
		SearchSimilarCode searchSimilarCode = new SearchSimilarCode((Statement)searchstatement.statements().get(0), unit, 0.6f);
		List<ASTNode> matches = searchSimilarCode.getMatches();
		for(ASTNode node : matches){
			System.out.println(node.toString());
		}
		
	}

}
