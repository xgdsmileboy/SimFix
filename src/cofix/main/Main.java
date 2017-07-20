/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.main;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import cofix.common.config.Configure;
import cofix.common.config.Constant;
import cofix.common.localization.AbstractFaultlocalization;
import cofix.common.localization.ManualLocator;
import cofix.common.util.JavaFile;
import cofix.common.util.Status;
import cofix.common.util.Subject;
import cofix.core.parser.ProjectInfo;

/**
 * @author Jiajun
 * @datae Jun 19, 2017
 */
public class Main {
	
	private static void tryFix(Subject subject) throws IOException{
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("=================================================\n");
		stringBuffer.append("Project : " + subject.getName() + "_" + subject.getId() + "\t");
		SimpleDateFormat myFmt1=new SimpleDateFormat("yy/MM/dd HH:mm"); 
		stringBuffer.append("start : " + myFmt1.format(new Date()) + "\n");
		System.out.println(stringBuffer.toString());
		JavaFile.writeStringToFile(Constant.HOME + "/result.log", stringBuffer.toString(), true);
		subject.backup();
		ProjectInfo.init(subject);
		AbstractFaultlocalization fLocalization = new ManualLocator(subject);
//		AbstractFaultlocalization fLocalization = new OchiaiResult(subject);
		Repair repair = new Repair(subject, fLocalization);
		Timer timer = new Timer(1, 0);
		timer.start();
		Status status = repair.fix(timer);
		switch (status) {
		case TIMEOUT:
			System.out.println(status);
			JavaFile.writeStringToFile("result.log", "Timeout time : " + myFmt1.format(new Date()) + "\n", true);
			break;
		case SUCCESS:
			System.out.println(status);
			JavaFile.writeStringToFile("result.log", "Success time : " + myFmt1.format(new Date()) + "\n", true);
			break;
		case FAILED:
			System.out.println(status);
			JavaFile.writeStringToFile("result.log", "Failed time : " + myFmt1.format(new Date()) + "\n", true);
		default:
			break;
		}
		subject.restore();
	}
	

	public static void main(String[] args) throws IOException {
		Constant.PROJECT_HOME = System.getProperty("user.dir") + "/testfile";
		System.out.println(Constant.PROJECT_HOME);
//		List<Subject> subjects = Configure.getSubjectFromXML("project.xml");
		Subject subject = Configure.getSubject("lang", 33);
		tryFix(subject);
		
	}

}
