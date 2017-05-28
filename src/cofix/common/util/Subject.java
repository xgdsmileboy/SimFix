/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */

package cofix.common.util;

import cofix.common.config.Constant;

/**
 * 
 * @author Jiajun
 *
 */
public class Subject {

	private String _name = null;
	private int _id = 0;
	private String _ssrc = null;
	private String _tsrc = null;
	private String _sbin = null;
	private String _tbin = null;

	/**
	 * subject
	 * 
	 * @param name
	 *            : name of subject, e.g., "chart".
	 * @param id
	 *            : number of subject, e.g., 1.
	 * @param ssrc
	 *            : relative path for source folder, e.g., "/source"
	 * @param tsrc
	 *            : relative path for test folder, e.g., "/tests"
	 * @param sbin
	 *            : relative path for source byte code, e.g., "/classes"
	 * @param tbin
	 *            : relative path for test byte code, e.g., "/test-classes"
	 */
	public Subject(String name, int id, String ssrc, String tsrc, String sbin, String tbin) {
		_name = name;
		_id = id;
		_ssrc = ssrc;
		_tsrc = tsrc;
		_sbin = sbin;
		_tbin = tbin;
	}

	public String getName() {
		return _name;
	}

	public int getId() {
		return _id;
	}

	public String getSsrc() {
		return _ssrc;
	}

	public String getTsrc() {
		return _tsrc;
	}

	public String getSbin() {
		return _sbin;
	}

	public String getTbin() {
		return _tbin;
	}

	/**
	 * get absolute home path for subject
	 * 
	 * @return e.g., "/home/user/chart/chart_1_buggy"
	 */
	public String getHome() {
		return Constant.PROJECT_HOME + "/" + _name + "/" + _name + "_" + _id + "_buggy";
	}

	@Override
	public String toString() {
		return "[_name=" + _name + ", _id=" + _id + ", _ssrc=" + _ssrc + ", _tsrc=" + _tsrc + ", _sbin=" + _sbin
				+ ", _tbin=" + _tbin + "]";
	}
}