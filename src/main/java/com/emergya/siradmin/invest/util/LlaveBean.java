/* LlaveBean.java
 * 
 * Copyright (C) 2012
 * 
 * This file is part of project ohiggins-core
 * 
 * This software is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * As a special exception, if you link this library with other files to
 * produce an executable, this library does not by itself cause the
 * resulting executable to be covered by the GNU General Public License.
 * This exception does not however invalidate any other reasons why the
 * executable file might be covered by the GNU General Public License.
 * 
 * Authors:: Juan Luis Rodríguez Ponce (mailto:jlrodriguez@emergya.com)
 */
package com.emergya.siradmin.invest.util;

import com.emergya.ohiggins.model.ChileindicaInversionDataEntity.UpdateStatus;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;

/**
 * @author jlrodriguez
 * 
 */
public class LlaveBean implements Comparable<LlaveBean> {

	private Integer region;
	private Integer ano;
	private Integer cFicha;
	private Integer cInstitucion;
	private Integer cPreinversion;
	private Integer fechaRegistro;
	private boolean updatable;
	private UpdateStatus status;

	public Integer getRegion() {
		return region;
	}

	public void setRegion(Integer region) {
		this.region = region;
	}

	public Integer getAno() {
		return ano;
	}

	public void setAno(Integer ano) {
		this.ano = ano;
	}

	public Integer getcFicha() {
		return cFicha;
	}

	public void setcFicha(Integer cFicha) {
		this.cFicha = cFicha;
	}

	public Integer getcInstitucion() {
		return cInstitucion;
	}

	public void setcInstitucion(Integer cInstitucion) {
		this.cInstitucion = cInstitucion;
	}

	public Integer getcPreinversion() {
		return cPreinversion;
	}

	public void setcPreinversion(Integer cPreinversion) {
		this.cPreinversion = cPreinversion;
	}

	public Integer getFechaRegistro() {
		return fechaRegistro;
	}

	public void setFechaRegistro(Integer fechaRegistro) {
		this.fechaRegistro = fechaRegistro;
	}

	public boolean isUpdatable() {
		return updatable;
	}

	public void setUpdatable(boolean updatable) {
		this.updatable = updatable;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ano == null) ? 0 : ano.hashCode());
		result = prime * result + ((cFicha == null) ? 0 : cFicha.hashCode());
		result = prime * result
				+ ((cInstitucion == null) ? 0 : cInstitucion.hashCode());
		result = prime * result
				+ ((cPreinversion == null) ? 0 : cPreinversion.hashCode());
		result = prime * result
				+ ((fechaRegistro == null) ? 0 : fechaRegistro.hashCode());
		result = prime * result + ((region == null) ? 0 : region.hashCode());
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
		LlaveBean other = (LlaveBean) obj;
		if (ano == null) {
			if (other.ano != null)
				return false;
		} else if (!ano.equals(other.ano))
			return false;
		if (cFicha == null) {
			if (other.cFicha != null)
				return false;
		} else if (!cFicha.equals(other.cFicha))
			return false;
		if (cInstitucion == null) {
			if (other.cInstitucion != null)
				return false;
		} else if (!cInstitucion.equals(other.cInstitucion))
			return false;
		if (cPreinversion == null) {
			if (other.cPreinversion != null)
				return false;
		} else if (!cPreinversion.equals(other.cPreinversion))
			return false;
		if (fechaRegistro == null) {
			if (other.fechaRegistro != null)
				return false;
		} else if (!fechaRegistro.equals(other.fechaRegistro))
			return false;
		if (region == null) {
			if (other.region != null)
				return false;
		} else if (!region.equals(other.region))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("LlaveBean [region=").append(region).append(", ano=")
				.append(ano).append(", cFicha=").append(cFicha)
				.append(", cInstitucion=").append(cInstitucion)
				.append(", cPreinversion=").append(cPreinversion)
				.append(", fechaRegistro=").append(fechaRegistro)
				.append(", updatable=").append(updatable)
				.append(", UPDATE_STATUS=").append(status).append("]");
		return builder.toString();
	}

	@Override
	public int compareTo(LlaveBean o) {
		return ComparisonChain
				.start()
				.compare(this.ano, o.ano, Ordering.natural().nullsLast())
				.compare(this.region, o.region, Ordering.natural().nullsLast())
				.compare(this.cInstitucion, o.cInstitucion,
						Ordering.natural().nullsLast())
				.compare(this.cFicha, o.cFicha, Ordering.natural().nullsLast())
				.compare(this.cPreinversion, o.cPreinversion,
						Ordering.natural().nullsLast()).result();
	}

	public UpdateStatus getStatus() {
		return status;
	}

	public void setStatus(UpdateStatus status) {
		this.status = status;
	}

}
