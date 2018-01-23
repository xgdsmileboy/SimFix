/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.core.modify.pattern.match;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import cofix.common.util.LevelLogger;
import cofix.common.util.Pair;

/**
 * @author Jiajun
 * @date Oct 9, 2017
 */
public class Matcher {

	public static List<Pair<MethodDeclaration, MethodDeclaration>> match(CompilationUnit src, CompilationUnit tar) {
		List<Pair<MethodDeclaration, MethodDeclaration>> matchPair = new LinkedList<>();
		MethodDeclCollector methodDeclCollector = new MethodDeclCollector();
		methodDeclCollector.init();
		src.accept(methodDeclCollector);
		List<MethodDeclaration> srcMethods = methodDeclCollector.getAllMethDecl();
		methodDeclCollector.init();
		tar.accept(methodDeclCollector);
		List<MethodDeclaration> tarMethods = methodDeclCollector.getAllMethDecl();
		
		if(srcMethods.size() != tarMethods.size()) {
			LevelLogger.warn("Different numbers of method declarations for two source files.");
			return matchPair;
		}
		
		for(MethodDeclaration sm : srcMethods) {
			boolean noMatch = true;
			for(int i = 0; noMatch && i < tarMethods.size(); i++) {
				MethodDeclaration tm = tarMethods.get(i);
				final DiffType diff = sameSignature(sm, tm);
				switch(diff) {
				case SAME:
					matchPair.add(new Pair<MethodDeclaration, MethodDeclaration>(sm, tm));
					tarMethods.remove(tm);
					noMatch = false;
					break;
				default :
					LevelLogger.info(diff.toString());
				}
			}
			if(noMatch) {
				LevelLogger.warn("No match for method declaration : \n" + sm.toString());
				return new LinkedList<>();
			}
		}
		
		return matchPair;
	}
	
	static enum DiffType{
		DIFF_MODIFIER("different modifiers"),
		DIFF_NAME("different names"),
		DIFF_RETURN("different return types"),
		DIFF_PARAM("different parameters"),
		SAME("same");
		
		private String message;
		private DiffType(String msg) {
			message = msg;
		}
		
		public String toString() {return message;}
	}
	
	@SuppressWarnings("unchecked")
	private static DiffType sameSignature(MethodDeclaration sm, MethodDeclaration tm) {
		int smdf = sm.getModifiers();
		int tmdf = tm.getModifiers();
		if((smdf & tmdf) != smdf) return DiffType.DIFF_MODIFIER;
		if(!sm.getName().getFullyQualifiedName().equals(tm.getName().getFullyQualifiedName())) return DiffType.DIFF_NAME;
		String sType = sm.getReturnType2() == null ? "?" : sm.getReturnType2().toString();
		String tType = tm.getReturnType2() == null ? "?" : tm.getReturnType2().toString(); 
		if(!sType.equals(tType)) return DiffType.DIFF_RETURN; 
		List<Object> sp = sm.typeParameters();
		List<Object> tp = tm.typeParameters();
		if(sp.size() != tp.size()) return DiffType.DIFF_PARAM;
		for(int i = 0; i < sp.size(); i++){
			if(!sp.get(i).toString().equals(tp.get(i).toString()))
				return DiffType.DIFF_PARAM;
		}
		return DiffType.SAME;
	}
	
	
	static class MethodDeclCollector extends ASTVisitor {
		
		List<MethodDeclaration> methodDeclarations;
		
		public MethodDeclCollector() {
		}
		
		public void init() {
			methodDeclarations = new LinkedList<>();
		}
		
		public List<MethodDeclaration> getAllMethDecl() {
			return methodDeclarations;
		}
		
		public boolean visit(MethodDeclaration md) {
			methodDeclarations.add(md);
			return true;
		}
	}

}
