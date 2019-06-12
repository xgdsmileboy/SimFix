/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.common.localization;

import com.gzoltar.core.spectra.Spectra;

import cofix.common.localization.alg.Algorithm;

/**
 * @author Jiajun
 * @date Jun 21, 2017
 */
public class SBFL {
	
	public static void sfl(Spectra spectra, Algorithm algorithm){
		for (int i = 0; i < spectra.getNumberOfComponents(); ++i) {
			double n11;
			double n10;
			double n01;
			double n00 = n01 = n10 = n11 = 0.0D;

			for (int j = 0; j < spectra.getNumberOfTests(); ++j) {
				boolean testResult = spectra.getTestResult(j);

				if (spectra.isCovered(j, i)) {
					if (!(testResult))
						n11 += 1.0D;
					else {
						n10 += 1.0D;
					}
				} else if (!(testResult))
					n01 += 1.0D;
				else {
					n00 += 1.0D;
				}
			}

			spectra.setSuspiciouness(i, algorithm.compute(n00, n01, n10, n11));
		}
	}
}
