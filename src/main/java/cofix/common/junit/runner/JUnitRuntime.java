/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.common.junit.runner;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import cofix.common.util.Subject;

/**
 * @author Jiajun
 * @date Jun 19, 2017
 */
public class JUnitRuntime{
	
	private Subject _subject = null;
	private ClassLoader _classLoader = null;
	
	
	public JUnitRuntime(Subject subject) {
		_subject = subject;
		setup();
	}
	
	private void setup() {
		File testDir = new File(_subject.getHome() + _subject.getTbin());
		File srcDir = new File(_subject.getHome()+ _subject.getSbin());
		URL testUrl = null;
		URL srcUrl = null;
		try {
			testUrl = testDir.toURI().toURL();
			srcUrl = srcDir.toURI().toURL();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		List<File> files = new ArrayList<File>();
		if(_subject.getDependency() != null){
			for(String path : _subject.getDependency()){
				files.add(new File(path));
			}
		}
		URL[] loadpath = new URL[2+files.size()];
		int i = 0;
		for(; i < files.size(); i++){
			try {
				loadpath[i] = files.get(i).toURI().toURL();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		loadpath[i] = testUrl;
		loadpath[i+1] = srcUrl;
		
		_classLoader = new URLClassLoader(loadpath);
	}

	public boolean equals(Object other) {
		if (other == this) {
			return true;
		}
		if (other == null || !(other instanceof JUnitRuntime)) {
			return false;
		} else {
			JUnitRuntime testRuntime = (JUnitRuntime) other;
			return _subject.getHome().equals(testRuntime._subject.getHome());
		}
	}

	public ClassLoader getClassLoader() {
		return _classLoader;
	}

	public String getProjectPath() {
		return _subject.getHome();
	}

	public String getTestClasspath() {
		return _subject.getTbin();
	}

	public String getTestSrcpath() {
		return _subject.getTsrc();
	}

	public String getSourceClasspath() {
		return _subject.getSbin();
	}
}
