package com.cairone.odataexample.entities;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity @Table(name="prestamos_cuotas")
public class PrestamoCuotaEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@EmbeddedId
	private PrestamoCuotaPKEntity pk = null;
	
	@OneToOne @JoinColumn(name="id_prestamo", nullable = false, insertable = false, updatable = false)
	private PrestamoEntity prestamo = null;
	
	@Column(name="nro_cuota", nullable = false, insertable = false, updatable = false)
	private Integer nroCuota = null;
	
	@Column(name="capital", nullable = false)
	private BigDecimal capital = null;

	@Column(name="intereses", nullable = false)
	private BigDecimal intereses = null;
	
	@Column(name="iva", nullable = false)
	private BigDecimal iva = null;

	@Column(name="intereses_gravados", nullable = false)
	private BigDecimal interesesGravados = null;
	
	@Column(name="monto", nullable = false)
	private BigDecimal monto = null;
	
	@Column(name="saldo_capital", nullable = false)
	private BigDecimal saldoCapital = null;
	
	public PrestamoCuotaEntity() {
		pk = new PrestamoCuotaPKEntity();
	}
	
	public PrestamoCuotaEntity(PrestamoEntity prestamo, Integer nroCuota) {
		this.prestamo = prestamo;
		this.nroCuota = nroCuota;
		this.pk = new PrestamoCuotaPKEntity(prestamo, nroCuota);
	}

	public PrestamoCuotaEntity(PrestamoEntity prestamo, Integer nroCuota, BigDecimal capital, BigDecimal intereses, BigDecimal iva, BigDecimal interesesGravados, BigDecimal monto, BigDecimal saldoCapital) {
		super();
		this.prestamo = prestamo;
		this.nroCuota = nroCuota;
		this.capital = capital;
		this.intereses = intereses;
		this.iva = iva;
		this.interesesGravados = interesesGravados;
		this.monto = monto;
		this.saldoCapital = saldoCapital;
		this.pk = new PrestamoCuotaPKEntity(prestamo, nroCuota);
	}

	public PrestamoEntity getPrestamo() {
		return prestamo;
	}

	public void setPrestamo(PrestamoEntity prestamo) {
		this.prestamo = prestamo;
		this.pk.setPrestamoId(prestamo.getId());
	}

	public Integer getNroCuota() {
		return nroCuota;
	}

	public void setNroCuota(Integer nroCuota) {
		this.nroCuota = nroCuota;
		this.pk.setNroCuota(nroCuota);
	}

	public BigDecimal getCapital() {
		return capital;
	}

	public void setCapital(BigDecimal capital) {
		this.capital = capital;
	}

	public BigDecimal getIntereses() {
		return intereses;
	}

	public void setIntereses(BigDecimal intereses) {
		this.intereses = intereses;
	}

	public BigDecimal getIva() {
		return iva;
	}

	public void setIva(BigDecimal iva) {
		this.iva = iva;
	}

	public BigDecimal getInteresesGravados() {
		return interesesGravados;
	}

	public void setInteresesGravados(BigDecimal interesesGravados) {
		this.interesesGravados = interesesGravados;
	}

	public BigDecimal getMonto() {
		return monto;
	}

	public void setMonto(BigDecimal monto) {
		this.monto = monto;
	}

	public BigDecimal getSaldoCapital() {
		return saldoCapital;
	}

	public void setSaldoCapital(BigDecimal saldoCapital) {
		this.saldoCapital = saldoCapital;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((pk == null) ? 0 : pk.hashCode());
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
		PrestamoCuotaEntity other = (PrestamoCuotaEntity) obj;
		if (pk == null) {
			if (other.pk != null)
				return false;
		} else if (!pk.equals(other.pk))
			return false;
		return true;
	}
	
}
