/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.main;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author Jiajun
 * @date Jun 21, 2017
 */
public class Timer {
	private long _start = 0;
	private long _timeout = 0;
	
	public Timer(int hour, int min){
		_timeout += TimeUnit.HOURS.toMillis(hour);
		_timeout += TimeUnit.MINUTES.toMillis(min);
		System.out.println("TIMEOUT : " + _timeout);
	}
	
	public String start(){
		_start = System.currentTimeMillis();
		return new Date(_start).toString();
	}
	
	public boolean timeout(){
		return (System.currentTimeMillis() - _start) > _timeout;
	}
}
