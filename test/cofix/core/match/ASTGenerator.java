/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */

package cofix.core.match;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;


public class ASTGenerator {
	
	public static ASTNode genAST(String source, int type){
		ASTParser astParser = ASTParser.newParser(AST.JLS8);
		Map<?, ?> options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_7, options);
		astParser.setCompilerOptions(options);
		astParser.setSource(source.toCharArray());
		astParser.setKind(type);
		astParser.setResolveBindings(true);
		return astParser.createAST(null);
	}
	
	public static CompilationUnit genAST(String filePath){
		if(filePath == null){
			return null;
		}
		File file = new File(filePath);
		StringBuffer stringBuffer = new StringBuffer();
		String line = null;
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(file));
			while((line = br.readLine()) != null){
				stringBuffer.append(line);
				stringBuffer.append("\n");
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return (CompilationUnit) ASTGenerator.genAST(stringBuffer.toString(), ASTParser.K_COMPILATION_UNIT);
	}
	
}
