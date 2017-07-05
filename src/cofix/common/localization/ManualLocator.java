/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.common.localization;

import java.util.List;

import cofix.common.util.Pair;

/**
 * @author Jiajun
 * @datae Jul 4, 2017
 */
public class ManualLocator extends AbstractFaultlocalization {

	
	public ManualLocator() {
		super();
	}
	
	@Override
	public List<Pair<String, Integer>> getLocations() {
		return null;
	}

}
