/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.common.junit.runner;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Jiajun
 * @date Jun 20, 2017
 */
public class OutStream extends OutputStream{

	private Set<Integer> _out = new HashSet<>();
	
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		String message = new String(b, off, len, "utf-8").trim();
		Integer id = null;
		if(message.length() > 0){
			try{
				id = Integer.parseInt(message);
			} catch (Exception e){
				return;
			}
			_out.add(id);
		}
	}
	
	@Override
	public void write(int b) throws IOException {
	}
	
	/**
	 * @return the _out
	 */
	public Set<Integer> getOut() {
		return _out;
	}

}
