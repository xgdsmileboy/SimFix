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
import cofix.common.localization.FLocalization;
import cofix.common.util.Subject;

/**
 * @author Jiajun
 * @datae Jun 19, 2017
 */
public class Main {
	
	public static void main(String[] args) {
		Constant.PROJECT_HOME = System.getProperty("user.dir") + "/testfile";
		System.out.println(Constant.PROJECT_HOME);
		List<Subject> subjects = Configure.getSubjectFromXML("project.xml");
		for(Subject subject : subjects){
			FLocalization fLocalization = new FLocalization();
			fLocalization.locateFault(subject, 0);
			Repair repair = new Repair(subject, fLocalization);
			Timer timer = new Timer(5, 0);
			timer.start();
			repair.fix(timer);
		}
	}
	
}
