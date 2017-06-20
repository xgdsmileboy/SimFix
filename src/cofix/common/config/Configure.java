/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.common.config;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import cofix.common.util.LevelLogger;
import cofix.common.util.Subject;

/**
 * @author Jiajun
 * @datae Jun 19, 2017
 */
public class Configure {

	private final static String __name__ = "@Configure ";
	
	public static List<Subject> getSubjectFromXML(String fileName) throws NumberFormatException {
		List<Subject> list = new ArrayList<>();

		File inputXml = new File(fileName);
		SAXReader saxReader = new SAXReader();
		try {
			Document document = saxReader.read(inputXml);
			Element root = document.getRootElement();

			for (Iterator iterator = root.elementIterator(); iterator.hasNext();) {
				Element element = (Element) iterator.next();
				String name = element.attributeValue("name");
				int id = 0;
				try {
					id = Integer.parseInt(element.attributeValue("id"));
				} catch (NumberFormatException e) {
					throw new NumberFormatException("Parse id failed!");
				}
				String ssrc = element.elementText("ssrc");
				String tsrc = element.elementText("tsrc");
				String sbin = element.elementText("sbin");
				String tbin = element.elementText("tbin");
				
				Element pathElem = element.element("classpath");
				
				List<String> classpath = new ArrayList<>();
				for(Iterator iterInner = pathElem.elementIterator(); iterInner.hasNext();){
					Element path = (Element) iterInner.next();
					String clp = path.getText();
					if(clp != null){
						classpath.add(clp);
					}
				}
				Subject subject = new Subject(name, id, ssrc, tsrc, sbin, tbin, classpath);
				list.add(subject);
			}
		} catch (DocumentException e) {
			LevelLogger.fatal(__name__ + "#getSubjectFromXML parse xml file failed !", e);
		}
		return list;
	}
	
}
