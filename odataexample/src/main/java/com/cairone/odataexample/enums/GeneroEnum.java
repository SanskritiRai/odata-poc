package com.cairone.odataexample.enums;


public enum GeneroEnum {
	MASCULINO('M'), FEMENINO('F');

	private final char valor;
	
	private GeneroEnum(char valor) {
		this.valor = valor;
	}

	public char getValor() {
		return valor;
	}
}
