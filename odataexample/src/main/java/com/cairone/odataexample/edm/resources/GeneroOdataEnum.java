package com.cairone.odataexample.edm.resources;

import com.cairone.odataexample.annotations.EdmEnum;


@EdmEnum(name="genero")
public enum GeneroOdataEnum {
	MASCULINO(1), FEMENINO(2);

	private final int valor;
	
	private GeneroOdataEnum(int valor) {
		this.valor = valor;
	}

	public int getValor() {
		return valor;
	}
}
