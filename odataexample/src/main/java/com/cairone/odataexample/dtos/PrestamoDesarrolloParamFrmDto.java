package com.cairone.odataexample.dtos;

import java.io.Serializable;
import java.math.BigDecimal;

public class PrestamoDesarrolloParamFrmDto implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private BigDecimal prestamo = null;
	private String tipoTasa = null;
	private BigDecimal tasa = null;
	private Integer cuotas = null;
	private BigDecimal alicuota = null;
	
	public PrestamoDesarrolloParamFrmDto() {}

	public PrestamoDesarrolloParamFrmDto(BigDecimal prestamo, String tipoTasa, BigDecimal tasa, Integer cuotas, BigDecimal alicuota) {
		super();
		this.prestamo = prestamo;
		this.tipoTasa = tipoTasa;
		this.tasa = tasa;
		this.cuotas = cuotas;
		this.alicuota = alicuota;
	}

	public BigDecimal getPrestamo() {
		return prestamo;
	}

	public PrestamoDesarrolloParamFrmDto setPrestamo(BigDecimal prestamo) {
		this.prestamo = prestamo;
		return this;
	}

	public String getTipoTasa() {
		return tipoTasa;
	}

	public PrestamoDesarrolloParamFrmDto setTipoTasa(String tipoTasa) {
		this.tipoTasa = tipoTasa;
		return this;
	}

	public BigDecimal getTasa() {
		return tasa;
	}

	public PrestamoDesarrolloParamFrmDto setTasa(BigDecimal tasa) {
		this.tasa = tasa;
		return this;
	}

	public Integer getCuotas() {
		return cuotas;
	}

	public PrestamoDesarrolloParamFrmDto setCuotas(Integer cuotas) {
		this.cuotas = cuotas;
		return this;
	}

	public BigDecimal getAlicuota() {
		return alicuota;
	}

	public PrestamoDesarrolloParamFrmDto setAlicuota(BigDecimal alicuota) {
		this.alicuota = alicuota;
		return this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((alicuota == null) ? 0 : alicuota.hashCode());
		result = prime * result + ((cuotas == null) ? 0 : cuotas.hashCode());
		result = prime * result
				+ ((prestamo == null) ? 0 : prestamo.hashCode());
		result = prime * result + ((tasa == null) ? 0 : tasa.hashCode());
		result = prime * result
				+ ((tipoTasa == null) ? 0 : tipoTasa.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PrestamoDesarrolloParamFrmDto other = (PrestamoDesarrolloParamFrmDto) obj;
		if (alicuota == null) {
			if (other.alicuota != null)
				return false;
		} else if (!alicuota.equals(other.alicuota))
			return false;
		if (cuotas == null) {
			if (other.cuotas != null)
				return false;
		} else if (!cuotas.equals(other.cuotas))
			return false;
		if (prestamo == null) {
			if (other.prestamo != null)
				return false;
		} else if (!prestamo.equals(other.prestamo))
			return false;
		if (tasa == null) {
			if (other.tasa != null)
				return false;
		} else if (!tasa.equals(other.tasa))
			return false;
		if (tipoTasa == null) {
			if (other.tipoTasa != null)
				return false;
		} else if (!tipoTasa.equals(other.tipoTasa))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return String.format("PRESTAMO: %s - TIPO DE TASA: %s  - TASA: %s - CUOTAS: %s - ALICUOTA: %s", prestamo, tipoTasa, tasa, cuotas, alicuota);
	}
}
