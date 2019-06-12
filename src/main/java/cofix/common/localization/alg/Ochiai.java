/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.common.localization.alg;

/**
 * @author Jiajun
 * @date Jun 21, 2017
 */
public class Ochiai extends Algorithm {

	@Override
	public double compute(double n00, double n01, double n10, double n11) {
		if ((n11 + n10 == 0.0D) || (n11 + n01 == 0.0D)) {
			return 0.0D;
		}
		return (n11 / Math.sqrt((n11 + n01) * (n11 + n10)));
	}

}
