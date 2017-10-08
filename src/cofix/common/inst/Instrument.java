/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.common.inst;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;

import cofix.common.util.JavaFile;
import cofix.common.util.LevelLogger;

/**
 * @author Jiajun
 * @date Jun 21, 2017
 */
public class Instrument {
	private static String __name__ = "@Instrument ";
	
	public static boolean execute(String path, ASTVisitor visitor) {
		if (path == null || path.length() <= 1) {
			LevelLogger.error(__name__ + "#execute illegal input file : " + path);
			return false;
		}
		File file = new File(path);
		if (!file.exists()) {
			LevelLogger.error(__name__ + "#execute input file not exist : " + path);
			return false;
		}
		List<File> fileList = new ArrayList<>();
		if (file.isDirectory()) {
			fileList = JavaFile.ergodic(file, fileList);
		} else if (file.isFile()) {
			fileList.add(file);
		} else {
			LevelLogger.error(
					__name__ + "#execute input file is neither a file nor directory : " + file.getAbsolutePath());
			return false;
		}

		for (File f : fileList) {
			CompilationUnit unit = JavaFile.genASTFromFile(f);
			if (unit == null || unit.toString().trim().length() < 1) {
				continue;
			}
			unit.accept(visitor);
			JavaFile.writeStringToFile(f, unit.toString());
		}
		return true;
	}
}
