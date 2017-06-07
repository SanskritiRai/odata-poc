package com.cairone.odataexample.entities;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity @Table(name="prestamos")
public class PrestamoEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id @Column(name="id_prestamo", nullable = false)
	private Integer id = null;
	
	@OneToOne @JoinColumns({
		@JoinColumn(name = "id_tipodoc", referencedColumnName = "id_tipodoc", nullable = false, insertable = true, updatable = true),
		@JoinColumn(name = "numero_documento", referencedColumnName = "numero_documento", nullable = false, insertable = true, updatable = true)
	})
	private PersonaEntity persona = null;
	
	@Column(name="fecha_alta", nullable = false)
	private LocalDate fechaAlta = null;
	
	@Column(name="fecha_aprobacion", nullable = true)
	private LocalDate fechaAprobacion = null;
	
	@Column(name="capital_prestado", nullable = false)
	private BigDecimal capitalPrestado = null;
	
	@Column(name="intereses", nullable = false)
	private BigDecimal intereses = null;
	
	@Column(name="iva", nullable = false)
	private BigDecimal iva = null;
	
	@Column(name="total_intereses", nullable = false)
	private BigDecimal totalIntereses = null;
	
	@Column(name="cuotas", nullable = false)
	private Integer cuotas = null;
	
	public PrestamoEntity() {}

	public PrestamoEntity(Integer id, PersonaEntity persona, LocalDate fechaAlta, LocalDate fechaAprobacion, BigDecimal capitalPrestado, BigDecimal intereses, BigDecimal iva, BigDecimal totalIntereses, Integer cuotas) {
		super();
		this.id = id;
		this.persona = persona;
		this.fechaAlta = fechaAlta;
		this.fechaAprobacion = fechaAprobacion;
		this.capitalPrestado = capitalPrestado;
		this.intereses = intereses;
		this.iva = iva;
		this.totalIntereses = totalIntereses;
		this.cuotas = cuotas;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public PersonaEntity getPersona() {
		return persona;
	}

	public void setPersona(PersonaEntity persona) {
		this.persona = persona;
	}

	public LocalDate getFechaAlta() {
		return fechaAlta;
	}

	public void setFechaAlta(LocalDate fechaAlta) {
		this.fechaAlta = fechaAlta;
	}

	public LocalDate getFechaAprobacion() {
		return fechaAprobacion;
	}

	public void setFechaAprobacion(LocalDate fechaAprobacion) {
		this.fechaAprobacion = fechaAprobacion;
	}

	public BigDecimal getCapitalPrestado() {
		return capitalPrestado;
	}

	public void setCapitalPrestado(BigDecimal capitalPrestado) {
		this.capitalPrestado = capitalPrestado;
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

	public BigDecimal getTotalIntereses() {
		return totalIntereses;
	}

	public void setTotalIntereses(BigDecimal totalIntereses) {
		this.totalIntereses = totalIntereses;
	}

	public Integer getCuotas() {
		return cuotas;
	}

	public void setCuotas(Integer cuotas) {
		this.cuotas = cuotas;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		PrestamoEntity other = (PrestamoEntity) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return String.format("PRESTAMO %s PARA %s", id, persona);
	}
}
