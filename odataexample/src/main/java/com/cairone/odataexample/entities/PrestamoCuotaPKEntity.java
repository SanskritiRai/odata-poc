package com.cairone.odataexample.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class PrestamoCuotaPKEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(name="id_prestamo", nullable = false)
	private Integer prestamoId = null;

	@Column(name="nro_cuota", nullable = false)
	private Integer nroCuota = null;

	public PrestamoCuotaPKEntity() {}

	public PrestamoCuotaPKEntity(Integer prestamoId, Integer nroCuota) {
		super();
		this.prestamoId = prestamoId;
		this.nroCuota = nroCuota;
	}
	
	public PrestamoCuotaPKEntity(PrestamoEntity prestamoEntity, Integer nroCuota) {
		this(prestamoEntity.getId(), nroCuota);
	}

	public Integer getPrestamoId() {
		return prestamoId;
	}

	public void setPrestamoId(Integer prestamoId) {
		this.prestamoId = prestamoId;
	}

	public Integer getNroCuota() {
		return nroCuota;
	}

	public void setNroCuota(Integer nroCuota) {
		this.nroCuota = nroCuota;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((nroCuota == null) ? 0 : nroCuota.hashCode());
		result = prime * result
				+ ((prestamoId == null) ? 0 : prestamoId.hashCode());
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
		PrestamoCuotaPKEntity other = (PrestamoCuotaPKEntity) obj;
		if (nroCuota == null) {
			if (other.nroCuota != null)
				return false;
		} else if (!nroCuota.equals(other.nroCuota))
			return false;
		if (prestamoId == null) {
			if (other.prestamoId != null)
				return false;
		} else if (!prestamoId.equals(other.prestamoId))
			return false;
		return true;
	}
	
}
