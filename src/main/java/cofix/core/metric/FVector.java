/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.core.metric;

import cofix.core.parser.node.CodeBlock;

/**
 * @author Jiajun
 * @date Jun 30, 2017
 */
public class FVector {
	private final int F_LOOP_INDEX = 0;
	private final int F_CONDITION_INDEX = 1;
	private final int F_OTHERSTRUCT_INDEX = 2;
	private final int F_OP_INDEX = 3;
	private final int F_MCALL_INDEX = 4;
	private final int F_VAR_INDEX = 5;
	private final int F_LITERAL_INDEX = 6;
	private final int VECTOR_LEN = 7;
	private int[] _vector = new int[VECTOR_LEN];
	
	public enum ALG {
		NORM_1,
		NORM_2,
		COSINE
	}
	
	public FVector(CodeBlock codeBlock){
		_vector[F_LOOP_INDEX] = codeBlock.getLoopStruct().size();
		_vector[F_CONDITION_INDEX] = codeBlock.getCondStruct().size();
		_vector[F_OTHERSTRUCT_INDEX] = codeBlock.getCondStruct().size();
		_vector[F_OP_INDEX] = codeBlock.getOperators().size();
		_vector[F_MCALL_INDEX] = codeBlock.getMethodCalls().size();
		_vector[F_VAR_INDEX] = codeBlock.getVariables().size();
		_vector[F_LITERAL_INDEX] = codeBlock.getConstants().size();
	}
	
	public double computeSimilarity(FVector fVector, ALG alg){
		if(fVector == null){
			return 0.0;
		} else {
			switch (alg) {
			case NORM_1:
				return norm_1(fVector);
			case NORM_2:
				return norm_2(fVector);
			case COSINE:
				return cosine(fVector);
			default:
				break;
			}
		}
		return 0.0;
	}
	
	private double cosine(FVector fVector){
		double product = 0.0;
		double squareAsum = 0.0;
		double squareBsum = 0.0;
		for(int i = 0; i < VECTOR_LEN; i++){
			product += _vector[i] * fVector._vector[i];
			squareAsum += _vector[i] * _vector[i];
			squareBsum += fVector._vector[i] * fVector._vector[i];
		}
		double denorminator = Math.sqrt(squareAsum) * Math.sqrt(squareBsum);
		if(denorminator == 0){
			return 0.0;
		}
		// positive value 0 - 1: 0 means exactly non-similar, 1 means most-similar
		double delta = product / denorminator;
		return delta;
	}
	
	//Manhattan Distance
	private double norm_1(FVector fVector){
		double delta = 0.0;
		double biggest = 0.0;
		for(int i = 0; i < VECTOR_LEN; i ++){
			delta += Math.abs(_vector[i] - fVector._vector[i]);
			biggest += _vector[i] > fVector._vector[i] ? _vector[i] : fVector._vector[i];
		}
		if(biggest == 0){
			return 0.0;
		}
		return delta / biggest;
	}
	
	private double norm_2(FVector fVector) {
		double delta = 0.0;
		double biggest = 0.0;
		for(int i = 0; i < VECTOR_LEN; i ++){
			delta += Math.pow(_vector[i] - fVector._vector[i], 2.0);
			double big = _vector[i] > fVector._vector[i] ? _vector[i] : fVector._vector[i];
			biggest += big * big;
		}
		if(biggest == 0){
			return 0.0;
		}
		delta = Math.sqrt(delta / biggest);
		return delta;
	}
	
	@Override
	public String toString() {
		StringBuffer stringBuffer = new StringBuffer("[");
		stringBuffer.append(_vector[0]);
		for(int i = 1; i < VECTOR_LEN; i++){
			stringBuffer.append(", " + _vector[i]);
		}
		stringBuffer.append("]");
		return stringBuffer.toString();
	}
}
