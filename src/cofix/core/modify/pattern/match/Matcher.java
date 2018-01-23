/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.core.modify.pattern.match;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
	
	public static Map<Integer, Integer> match(Object[] src, Object[] tar) {
		return match(Arrays.asList(src), Arrays.asList(tar), new Comparator<Object>() {
			@Override
			public int compare(Object o1, Object o2) {
				if(o1.equals(o2)) {
					return 1;
				} else {
					return 0;
				}
			};
		});
	}
	
	private static enum Direction {
		LEFT,
		UP,
		ANDGLE
	}
	
	public static <T> Map<Integer, Integer> match(List<T> src, List<T> tar, Comparator<T> comparator) {
		Map<Integer, Integer> map = new HashMap<>();
		int srcLen = src.size();
		int tarLen = tar.size();
		if(srcLen == 0 || tarLen == 0) {
			return map;
		}
		int[][] score = new int[srcLen + 1][tarLen + 1];

		// LCS matching with path retrieval
		Direction[][] path = new Direction[srcLen + 1][tarLen + 1];
		for(int i = 0; i < srcLen; i++){
			for(int j = 0; j < tarLen; j++){
				if(comparator.compare(src.get(i), tar.get(j)) > 0){
					score[i + 1][j + 1] = score[i][j] + 1;
					path[i + 1][j + 1] = Direction.ANDGLE;
				} else {
					int left = score[i + 1][j];
					int up = score[i][j + 1];
					if(left >= up) {
						score[i + 1][j + 1] = left;
						path[i + 1][j + 1] = Direction.LEFT;
					} else {
						score[i + 1][j + 1] = up;
						path[i + 1][j + 1] = Direction.UP;
					}
				}
			}
		}
		
		for(int i = srcLen, j = tarLen; i > 0 && j > 0;) {
			switch(path[i][j]){
			case ANDGLE:
				map.put(i-1, j-1);
				i --;
				j --;
				break;
			case LEFT:
				j --;
				break;
			case UP:
				i --;
				break;
			default:
				LevelLogger.error("should not happen!");
				System.exit(0);
			}
		}
		
		assert map.size() == score[srcLen][tarLen];
		return map;
	}

}
