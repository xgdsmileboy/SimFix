/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */

package cofix.common.util;

public class Pair<T1, T2> {

	private T1 first;
	private T2 second;
	
	public Pair() {
	}

	public Pair(T1 fst, T2 snd){
		this.first = fst;
		this.second = snd;
	}
	
	public T1 getFirst(){
		return this.first;
	}
	
	public T2 getSecond(){
		return this.second;
	}
	
	public void setFirst(T1 fst){
		this.first = fst;
	}
	
	public void setSecond(T2 snd){
		this.second = snd;
	}
	
	@Override
	public String toString() {
		return "<" + first.toString() + "," + second.toString() + ">";
	}
}
