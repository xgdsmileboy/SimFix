/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.main;

import java.util.List;

import cofix.common.config.Configure;
import cofix.common.config.Constant;
import cofix.common.util.Subject;

/**
 * @author Jiajun
 * @datae Jun 19, 2017
 */
public class Main {
	
	public static void main(String[] args) {
		Constant.PROJECT_HOME = "";
		List<Subject> subjects = Configure.getSubjectFromXML("project.xml");
		for(Subject subject : subjects){
			
		}
	}
	
}
