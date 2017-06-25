/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.common.node;

import cofix.common.node.modify.Modification;

/**
 * @author Jiajun
 * @datae Jun 23, 2017
 */
public interface Adapter {
	public boolean adapt(Modification modification);
	public boolean restore(Modification modification);
	public boolean backup(Modification modification);
}
