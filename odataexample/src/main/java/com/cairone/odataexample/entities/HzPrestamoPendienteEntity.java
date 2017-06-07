package com.cairone.odataexample.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity @Table(name="hazelcast_prestamos_pendientes")
public class HzPrestamoPendienteEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id @Column(name="clave", nullable=false)
	private String clave = null;
	
	@Column(name="serialized_object", nullable=false) @Lob
	private byte[] serializedObject = null;
	
	public HzPrestamoPendienteEntity() {}

	public HzPrestamoPendienteEntity(String clave, byte[] serializedObject) {
		super();
		this.clave = clave;
		this.serializedObject = serializedObject;
	}

	public HzPrestamoPendienteEntity(String clave) {
		this(clave, null);
	}
	
	public String getClave() {
		return clave;
	}

	public void setClave(String clave) {
		this.clave = clave;
	}

	public byte[] getSerializedObject() {
		return serializedObject;
	}

	public void setSerializedObject(byte[] serializedObject) {
		this.serializedObject = serializedObject;
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
		HzPrestamoPendienteEntity other = (HzPrestamoPendienteEntity) obj;
		if (clave == null) {
			if (other.clave != null)
				return false;
		} else if (!clave.equals(other.clave))
			return false;
		return true;
	}
	
}
