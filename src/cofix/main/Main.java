/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.main;

import java.io.IOException;
import java.util.List;

import cofix.common.config.Configure;
import cofix.common.config.Constant;
import cofix.common.localization.AbstractFaultlocalization;
import cofix.common.localization.FLocalization;
import cofix.common.localization.ManualLocator;
import cofix.common.util.Status;
import cofix.common.util.Subject;
import cofix.core.parser.ProjectInfo;

/**
 * @author Jiajun
 * @datae Jun 19, 2017
 */
public class Main {
	
	private static void loop(List<Subject> subjects) throws IOException{
		for (Subject subject : subjects) {
			subject.backup();
			ProjectInfo.init(subject);
			AbstractFaultlocalization fLocalization = new ManualLocator();
			fLocalization.locateFault(subject, 0);
			Repair repair = new Repair(subject, fLocalization);
			Timer timer = new Timer(5, 20);
			timer.start();
			Status status = repair.fix(timer);
			switch (status) {
			case TIMEOUT:
				System.out.println(status);
				break;
			case SUCCESS:
				System.out.println(status);
				break;
			case FAILED:
				System.out.println(status);
			default:
				break;
			}
			subject.restore();
		}
	}
	

	public static void main(String[] args) throws IOException {
		Constant.PROJECT_HOME = System.getProperty("user.dir") + "/testfile";
		System.out.println(Constant.PROJECT_HOME);
		List<Subject> subjects = Configure.getSubjectFromXML("project.xml");
		loop(subjects);
	}

}
