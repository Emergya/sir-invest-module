package com.emergya.siradmin.invest.util;

import java.math.BigInteger;

public class WSCallException extends RuntimeException {

	private static final long serialVersionUID = 6825887152567891013L;
	private BigInteger codigoRespuesta;
	private String textoRespuesta;

	public WSCallException() {

	}

	public WSCallException(String message) {
		super(message);
	}

	public WSCallException(Throwable cause) {
		super(cause);

	}

	public WSCallException(String message, Throwable cause) {
		super(message, cause);

	}

	public WSCallException(BigInteger codigoRespuesta, String textoRespuesta) {
		this.setCodigoRespuesta(codigoRespuesta);
		this.setTextoRespuesta(textoRespuesta);
	}

	public BigInteger getCodigoRespuesta() {
		return codigoRespuesta;
	}

	public void setCodigoRespuesta(BigInteger codigoRespuesta) {
		this.codigoRespuesta = codigoRespuesta;
	}

	public String getTextoRespuesta() {
		return textoRespuesta;
	}

	public void setTextoRespuesta(String textoRespuesta) {
		this.textoRespuesta = textoRespuesta;
	}

}
