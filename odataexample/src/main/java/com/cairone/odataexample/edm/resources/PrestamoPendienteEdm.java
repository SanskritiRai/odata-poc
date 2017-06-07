package com.cairone.odataexample.edm.resources;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.cairone.odataexample.OdataExample;
import com.cairone.olingo.ext.jpa.annotations.EdmEntity;
import com.cairone.olingo.ext.jpa.annotations.EdmEntitySet;
import com.cairone.olingo.ext.jpa.annotations.EdmNavigationProperty;
import com.cairone.olingo.ext.jpa.annotations.EdmProperty;

@EdmEntity(name = "PrestamoPendiente", key = { "clave" }, namespace = OdataExample.NAME_SPACE, containerName = OdataExample.CONTAINER_NAME)
@EdmEntitySet("PrestamosPendientes")
public class PrestamoPendienteEdm implements Serializable {

	private static final long serialVersionUID = 1L;

	@EdmProperty(name="clave", nullable = false)
	private String clave = null;
	
	@EdmProperty(name="fechaAlta", nullable = false)
	private LocalDate fechaAlta = null;
	
	@EdmProperty(name="prestamo", nullable = false)
	private BigDecimal prestamo = null;
	
	@EdmProperty(name="intereses", nullable = false)
	private BigDecimal intereses = null;
	
	@EdmProperty(name="iva", nullable = false)
	private BigDecimal iva = null;
	
	@EdmProperty(name="total", nullable = false)
	private BigDecimal total = null;
	
	@EdmNavigationProperty(name="cuotas")
	private List<PrestamoCuotaEdm> cuotas = null;
	
	private PersonaEdm persona = null;
	
	public PrestamoPendienteEdm() {
		cuotas = new ArrayList<PrestamoCuotaEdm>();
	}

	public String getClave() {
		return clave;
	}

	public void setClave(String clave) {
		this.clave = clave;
	}

	public LocalDate getFechaAlta() {
		return fechaAlta;
	}

	public void setFechaAlta(LocalDate fechaAlta) {
		this.fechaAlta = fechaAlta;
	}

	public BigDecimal getPrestamo() {
		return prestamo;
	}

	public void setPrestamo(BigDecimal prestamo) {
		this.prestamo = prestamo;
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

	public BigDecimal getTotal() {
		return total;
	}

	public void setTotal(BigDecimal total) {
		this.total = total;
	}

	public List<PrestamoCuotaEdm> getCuotas() {
		return cuotas;
	}

	public void setCuotas(List<PrestamoCuotaEdm> cuotas) {
		this.cuotas = cuotas;
	}

	public PersonaEdm getPersona() {
		return persona;
	}

	public void setPersona(PersonaEdm persona) {
		this.persona = persona;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((clave == null) ? 0 : clave.hashCode());
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
		PrestamoPendienteEdm other = (PrestamoPendienteEdm) obj;
		if (clave == null) {
			if (other.clave != null)
				return false;
		} else if (!clave.equals(other.clave))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return String.format("CLAVE: %s", clave);
	}
}
