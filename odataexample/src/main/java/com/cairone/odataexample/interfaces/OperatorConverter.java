package com.cairone.odataexample.interfaces;

public interface OperatorConverter<X,Y> {

	public Y convertToJpqlOperator (X x);
	public X convertToOlingoOperator (Y y);
}
