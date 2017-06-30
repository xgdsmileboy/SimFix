/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.core.modify;

import java.util.Map;

import org.eclipse.jdt.core.dom.Type;

/**
 * @author Jiajun
 * @datae Jun 30, 2017
 */
public class Insertion extends Modification {

	@Override
	public boolean apply(Map<String, Type> usableVars) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean restore() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean backup() {
		// TODO Auto-generated method stub
		return false;
	}

}
