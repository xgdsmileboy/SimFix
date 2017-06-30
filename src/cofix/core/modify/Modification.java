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
 * @datae Jun 23, 2017
 */
public abstract class Modification {

	public abstract boolean apply(Map<String, Type> usableVars);
	public abstract boolean backup();
	public abstract boolean restore();
	
}
