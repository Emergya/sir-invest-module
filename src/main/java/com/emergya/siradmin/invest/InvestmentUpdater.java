/* InvestmentUpdater.java
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
package com.emergya.siradmin.invest;

import java.math.BigInteger;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.xml.rpc.ServiceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.geometry.GeometryBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.CRS;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emergya.ohiggins.dto.ChileindicaInversionDataDto;
import com.emergya.ohiggins.dto.ChileindicaInversionFinanciamientoDataDto;
import com.emergya.ohiggins.dto.ChileindicaRelacionIntrumentosDataDto;
import com.emergya.ohiggins.model.ChileindicaInversionDataEntity.UpdateStatus;
import com.emergya.ohiggins.service.ChileindicaInversionDataService;
import com.emergya.siradmin.invest.client.investmentdata.ConsultaInversionPorLlaveGeorefResponse;
import com.emergya.siradmin.invest.client.investmentdata.InversionData;
import com.emergya.siradmin.invest.client.investmentdata.InversionFinanciamientoData;
import com.emergya.siradmin.invest.client.investmentdata.InversionGeorefCoordenadasData;
import com.emergya.siradmin.invest.client.investmentdata.InversionGeorefData;
import com.emergya.siradmin.invest.client.investmentdata.RelacionInstrumentosData;
import com.emergya.siradmin.invest.client.investmentdata.WSConsultaInversionPorLlaveGeoref;
import com.emergya.siradmin.invest.client.keyquery.ConsultaLlavesResponse;
import com.emergya.siradmin.invest.client.keyquery.LlavesInversionData;
import com.emergya.siradmin.invest.client.keyquery.Respuesta;
import com.emergya.siradmin.invest.client.keyquery.WSConsultaLlaves;
import com.emergya.siradmin.invest.util.LlaveBean;
import com.emergya.siradmin.invest.util.WSCallException;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

@Service
public class InvestmentUpdater {

	private enum GeometryType {
		PUNTO, LINEA
	}

	private static final Log LOGGER = LogFactory
			.getLog(InvestmentUpdater.class);
	@Autowired
	private WSConsultaLlaves wsConsultaLlaves;

	@Autowired
	private WSConsultaInversionPorLlaveGeoref wsConsultaInversion;

	@Autowired
	private ChileindicaInversionDataService service;

	public List<LlaveBean> getExistingKeysInChileindica(Integer ano,
			Integer codigoRegion) {
		LOGGER.info("Obteniendo llaves para del año " + ano + " y la región "
				+ codigoRegion);
		try {
			List<LlaveBean> keys = null;
			ConsultaLlavesResponse response = wsConsultaLlaves
					.getWSConsultaLlavesPort().WSConsultaLlaves(
							BigInteger.valueOf(ano.longValue()),
							BigInteger.valueOf(codigoRegion.longValue()));
			Respuesta respuesta = response.getRespuesta();
			if (!BigInteger.ZERO.equals(respuesta.getCodigoRespuesta())) {
				LOGGER.error("El servicio web Consulta de llaves "
						+ wsConsultaLlaves.getWSConsultaLlavesPortAddress()
						+ " ha devuelto un código de error "
						+ respuesta.getCodigoRespuesta()
						+ ". El mensaje de error fue: "
						+ respuesta.getTextoRespuesta());
				throw new WSCallException(respuesta.getCodigoRespuesta(),
						respuesta.getTextoRespuesta());

			}
			if (response.getLlavesInversion() != null) {
				keys = new ArrayList<LlaveBean>(
						response.getLlavesInversion().length);
				for (LlavesInversionData projectKey : response
						.getLlavesInversion()) {
					LlaveBean key = new LlaveBean();
					key.setAno(projectKey.getAno().intValue());
					key.setRegion(codigoRegion);
					key.setcFicha(projectKey.getC_Ficha().intValue());
					key.setcInstitucion(projectKey.getC_Institucion()
							.intValue());
					key.setcPreinversion(projectKey.getC_Preinversion()
							.intValue());
					BigInteger fechaRegistro = projectKey.getFechaRegistro();
					if (fechaRegistro != null) {
						key.setFechaRegistro(fechaRegistro.intValue());
					}

					boolean updatable = service.checkIfProjectMustBeUpdated(
							key.getRegion(), key.getAno(),
							key.getcInstitucion(), key.getcPreinversion(),
							key.getcFicha(), key.getFechaRegistro());
					key.setUpdatable(updatable);

					keys.add(key);
				}
				return keys;
			} else {
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info("El WS de consulta de llaves no ha devuelto datos para el año "
							+ ano + " y la región " + codigoRegion);
				}
				return new ArrayList<LlaveBean>();
			}

		} catch (RemoteException e) {
			LOGGER.error(
					"Error llamando al servicio web "
							+ wsConsultaLlaves.getWSConsultaLlavesPortAddress(),
					e);
			throw new WSCallException(e);
		} catch (ServiceException e) {
			LOGGER.error(
					"Error llamando al servicio web "
							+ wsConsultaLlaves.getWSConsultaLlavesPortAddress(),
					e);
			throw new WSCallException(e);
		}

	}

	public void getWsDataAndUpdateDB(List<LlaveBean> wsProjectList) {
		// 1. Get an iterator of all Chileindica
		// 2. Traverse the list of LlaveBean
		// 3. For every LlaveBean mark database element as no removable
		// 4. If llaveBean is Updatable, create or update element in DataBase.
		// 5. If there is any problem cheking with the web service, mark the
		// element as update error.
		List<Long> projectDbIds = service.getAllProjectDbIds();
		Collections.sort(wsProjectList);

		for (Iterator<Long> iterator = projectDbIds.iterator(); iterator
				.hasNext();) {
			Long dbId = iterator.next();
			ChileindicaInversionDataDto dto = (ChileindicaInversionDataDto) service
					.getById(dbId);
			LlaveBean keyToSearch = new LlaveBean();
			keyToSearch.setAno(dto.getAno());
			keyToSearch.setRegion(dto.getRegion());
			keyToSearch.setcFicha(dto.getCFicha());
			keyToSearch.setcInstitucion(dto.getCInstitucion());
			keyToSearch.setcPreinversion(dto.getCPreinversion());

			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Tratando proyecto existente BBDD " + keyToSearch);
			}
			// Buscamos si el proyecto existe entre las claves actuales
			int foundKeyIndex = Collections.binarySearch(wsProjectList,
					keyToSearch);
			if (foundKeyIndex >= 0) {
				LlaveBean foundKey = wsProjectList.get(foundKeyIndex);
				iterator.remove();
				wsProjectList.remove(foundKeyIndex);
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info("El proyecto sigue existiendo en el servicio web de consulta de llaves");
				}
				// Si el proyecto ha sido actualizado en Chileindica recuperamos
				// los detalles del servicio web, borramos el proyecto original
				// de
				// la base de datos y creamos una entrada nueva en BD
				if (foundKey.isUpdatable()) {
					try {
						ConsultaInversionPorLlaveGeorefResponse response = this.wsConsultaInversion
								.getWSConsultaInversionPorLlaveGeorefPort()
								.WSConsultaInversionPorLlaveGeoref(
										BigInteger
												.valueOf(foundKey.getRegion()),
										BigInteger.valueOf(foundKey.getAno()),
										BigInteger.valueOf(foundKey
												.getcInstitucion()),
										BigInteger.valueOf(foundKey
												.getcPreinversion()),
										BigInteger.valueOf(foundKey.getcFicha()));
						com.emergya.siradmin.invest.client.investmentdata.Respuesta respuesta = response
								.getRespuesta();
						BigInteger codRespuesta = respuesta
								.getCodigoRespuesta();
						if (BigInteger.ZERO.equals(codRespuesta)) {
							// delete from DB and create new instance
							service.delete(dto);
							ChileindicaInversionDataDto updatedProject = createDtoFromWsResponse(
									response, foundKey);
							service.create(updatedProject);
							foundKey.setStatus(UpdateStatus.OK);
							if (LOGGER.isInfoEnabled()) {
								LOGGER.info("Proyecto en BBDD actualizado");
							}
						} else {
							if (LOGGER.isInfoEnabled()) {
								LOGGER.info("Error obteniendo información del proyecto "
										+ foundKey
										+ ". Código de repuesta = "
										+ codRespuesta
										+ ". Mensaje = "
										+ respuesta.getTextoRespuesta());

							}
							// Guardamos el proyecto en BBDD con estado error
							dto.setUpdateStatus(UpdateStatus.WS_ERROR);
							dto.setLastUpdateTry(Calendar.getInstance()
									.getTime());
							foundKey.setStatus(UpdateStatus.WS_ERROR);
							service.update(dto);
						}

					} catch (DataAccessException e) {
						if (LOGGER.isErrorEnabled()) {
							LOGGER.error("Error almacenando proyecto "
									+ foundKey + " en base de datos", e);
						}
						foundKey.setStatus(UpdateStatus.DB_ERROR);

					} catch (RemoteException e) {
						// Guardamos el proyecto en BBDD con estado error
						dto.setUpdateStatus(UpdateStatus.WS_ERROR);
						dto.setLastUpdateTry(Calendar.getInstance().getTime());
						service.update(dto);						
						foundKey.setStatus(UpdateStatus.WS_ERROR);
					} catch (ServiceException e) {
						// Guardamos el proyecto en BBDD con estado error
						dto.setUpdateStatus(UpdateStatus.WS_ERROR);
						dto.setLastUpdateTry(Calendar.getInstance().getTime());
						service.update(dto);						
						foundKey.setStatus(UpdateStatus.WS_ERROR);
					} catch(Throwable e) {                            
                                            dto.setUpdateStatus(UpdateStatus.WS_ERROR);
                                            dto.setLastUpdateTry(Calendar.getInstance().getTime());
                                            service.update(dto);	
                                            foundKey.setStatus(UpdateStatus.WS_ERROR);
                                        }
				} else {
					if (LOGGER.isInfoEnabled()) {
						LOGGER.info("El proyecto no necesita ser actualizado en BBDD");
					}
				}
			} else {
				// si el proyecto no ha sido devuleto por el WS Consulta de
				// llaves, se borra de la base de datos
				service.delete(dto);
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info("Se elimina el proyecto por no existir en la consulta de llaves "
							+ keyToSearch);
				}
			}
		}

		// En este punto los elementos que quedan en projectDbIds pueden ser
		// eliminados de la base de datos puesto que no han sido devueltos por
		// el servicio web de consulta de llaves
		// for (Long id : projectDbIds) {
		// service.deleteById(id);
		// }

		// Ahora se crean los proyectos que queden en la lista de llaves
		// devueltas
		// por el servicio web.
		for (LlaveBean llave : wsProjectList) {
			ConsultaInversionPorLlaveGeorefResponse response;
			try {
				response = this.wsConsultaInversion
						.getWSConsultaInversionPorLlaveGeorefPort()
						.WSConsultaInversionPorLlaveGeoref(
								BigInteger.valueOf(llave.getRegion()),
								BigInteger.valueOf(llave.getAno()),
								BigInteger.valueOf(llave.getcInstitucion()),
								BigInteger.valueOf(llave.getcPreinversion()),
								BigInteger.valueOf(llave.getcFicha()));
				com.emergya.siradmin.invest.client.investmentdata.Respuesta respuesta = response
						.getRespuesta();
				BigInteger codRespuesta = respuesta.getCodigoRespuesta();
				if (BigInteger.ZERO.equals(codRespuesta)) {
					ChileindicaInversionDataDto newProject = createDtoFromWsResponse(
							response, llave);
					service.create(newProject);
					llave.setStatus(UpdateStatus.OK);
					if (LOGGER.isInfoEnabled()) {
						LOGGER.info("Creada nueva entrada en BBDD");
					}

				} else {
					if (LOGGER.isErrorEnabled()) {
						LOGGER.error("Error obteniendo información del proyecto "
								+ llave
								+ ". Código de repuesta = "
								+ codRespuesta
								+ ". Mensaje = "
								+ respuesta.getTextoRespuesta());
					}
					llave.setStatus(UpdateStatus.WS_ERROR);
				}

			} catch (DataAccessException e) {
				if (LOGGER.isErrorEnabled()) {
					LOGGER.error("Error almacenando proyecto " + llave
							+ " en base de datos", e);
				}
				llave.setStatus(UpdateStatus.DB_ERROR);
			} catch (RemoteException e) {
				if (LOGGER.isErrorEnabled()) {
					LOGGER.error("Error obteniendo información del proyecto "
							+ llave, e);
				}
				llave.setStatus(UpdateStatus.WS_ERROR);
			} catch (ServiceException e) {
				if (LOGGER.isErrorEnabled()) {
					LOGGER.error("Error obteniendo información del proyecto "
							+ llave, e);
				}
				llave.setStatus(UpdateStatus.WS_ERROR);
			} catch(Throwable e) {                            
                            if (LOGGER.isErrorEnabled()) {
					LOGGER.error("Error obteniendo información del proyecto "
							+ llave, e);
                            }
                            llave.setStatus(UpdateStatus.WS_ERROR);
                        }
                                
                                
		}

	}

	private ChileindicaInversionDataDto createDtoFromWsResponse(
			ConsultaInversionPorLlaveGeorefResponse response, LlaveBean key) {
		ChileindicaInversionDataDto dto = new ChileindicaInversionDataDto();
		InversionData inversionData = response.getInversionData();
		if (inversionData != null) {
			dto.setAno(getAsInteger(inversionData.getAno()));
			dto.setAsignacionDisponibleInversion(inversionData
					.getAsignacion_Disponible_Inversion());
			dto.setcEtapaIdi(getAsInteger(inversionData.getC_Etapa_Idi()));
			dto.setcFicha(getAsInteger(inversionData.getC_Ficha()));
			dto.setcInstitucion(getAsInteger(inversionData.getC_Institucion()));
			dto.setCodigo(inversionData.getCodigo());
			dto.setCostoTotalAjustadoInversion(inversionData
					.getCosto_Total_Ajustado_Inversion());
			dto.setcPreinversion(getAsInteger(inversionData.getC_Preinversion()));
			dto.setcTipoCodigo(getAsInteger(inversionData.getC_Tipo_Codigo()));
			dto.setFechaRegistroChileindica(service.getValidDate(key
					.getFechaRegistro()));
			dto.setGastadoAnosAnterioresInversion(inversionData
					.getGastado_Anos_Anteriores_Inversion());
			dto.setItemPresupuestario(inversionData.getItem_Presupuestario());
			dto.setLastUpdateTry(new Date());
			dto.setnEtapaIdi(inversionData.getN_Etapa_Idi());
			dto.setNombreComuna(inversionData.getNombre_Comuna());
			dto.setNombreInstitucionResponsable(inversionData
					.getNombre_Institucion_Responsable());
			dto.setNombreItemPresupuestario(inversionData
					.getNombre_Item_Presupuestario());
			dto.setNombreLocalidad(inversionData.getNombre_Localidad());
			dto.setNombreProvincia(inversionData.getNombre_Provincia());
			dto.setNombreProyecto(inversionData.getNombre_Proyecto());
			dto.setNombreUnidadTecnica(inversionData.getNombre_Unidad_Tecnica());
			dto.setnRegion(inversionData.getN_Region());
			dto.setnTipoCodigo(inversionData.getN_Tipo_Codigo());
			dto.setRegion(getAsInteger(inversionData.getRegion()));
			dto.setSaldoAnosRestantesInversion(inversionData
					.getSaldo_Anos_Restantes_Inversion());
			dto.setSaldoPorAsignarInversion(inversionData
					.getSaldo_Por_Asignar_Inversion());
			dto.setSaldoProximoAnoInversion(inversionData
					.getSaldo_Proximo_Ano_Inversion());
			dto.setTotalAsignadoInversion(inversionData
					.getTotal_Asignado_Inversion());
			dto.setTotalPagadoInversion(inversionData
					.getTotal_Pagado_Inversion());
			dto.setUpdateStatus(UpdateStatus.OK);

			// Add InversionFinanciamentoData to DTO
			InversionFinanciamientoData[] ifds = response
					.getInversionFinanciamiento();
			if (ifds != null) {
				for (InversionFinanciamientoData ifd : ifds) {
					ChileindicaInversionFinanciamientoDataDto fdto = buildInversionFinanciamientoDto(ifd);
					fdto.setInversionData(dto);
					dto.getFinanciamientosList().add(fdto);
				}
			}

			// Add RelacionInstrumentosData to DTO
			RelacionInstrumentosData[] rids = response
					.getRelacionInstrumentos();
			if (rids != null) {
				for (RelacionInstrumentosData rid : rids) {
					ChileindicaRelacionIntrumentosDataDto rdto = buildRelacionInstrumentosDto(rid);
					rdto.setInversionData(dto);
					dto.getRelacionInstrumentosList().add(rdto);
				}
			}

			// Build GeometryInfo and add it to the DTO
			InversionGeorefData georefData = response.getInversionGeorefData();
			InversionGeorefCoordenadasData[] coordenadas = response
					.getInversionGeorefCoordenadas();
			if (georefData != null && georefData.getElementoGrafico() != null
					&& georefData.getCodigoOGC() != null && coordenadas != null
					&& coordenadas.length > 0) {
				Geometry geometry = buildGeometry(georefData, coordenadas);
				dto.setTheGeom(geometry);
			}

		}
		return dto;
	}

	private Geometry buildGeometry(InversionGeorefData georefData,
			InversionGeorefCoordenadasData[] coordenadas) {
		GeometryType gt = GeometryType.values()[getAsInteger(georefData
				.getElementoGrafico())];
		int srs = getAsInteger(georefData.getCodigoOGC());
		Geometry geom = null;

		try {
			switch (gt) {
			case PUNTO:
				geom = buildPoint(srs, coordenadas[0]);
				break;
			case LINEA:
				geom = buildLine(srs, coordenadas);

			default:
				break;
			}
		} catch (Exception e) {
			LOGGER.error("Error construyendo la geometría ", e);

		}

		return geom;
	}

	private Geometry buildLine(int srs,
			InversionGeorefCoordenadasData[] coordenadas)
			throws NoSuchAuthorityCodeException, FactoryException,
			MismatchedDimensionException, TransformException {
		CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:" + srs);
		CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:4326");
		boolean lenient = true; // allow for some error due to different datums
		MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS,
				lenient);
		GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();

		Arrays.sort(coordenadas,
				new Comparator<InversionGeorefCoordenadasData>() {
					public int compare(InversionGeorefCoordenadasData o1,
							InversionGeorefCoordenadasData o2) {
						return o1.getOrden().compareTo(o2.getOrden());

					};
				});
		Coordinate[] coordinates = new Coordinate[coordenadas.length];

		for (int i = 0; i < coordinates.length; i++) {
			InversionGeorefCoordenadasData igc = coordenadas[i];
			int ejeX = Integer.valueOf(igc.getEjeX());
			int ejeY = Integer.valueOf(igc.getEjeY());
			Coordinate c = new Coordinate(ejeX, ejeY);
			coordinates[i] = c;
		}
		LineString ls = geometryFactory.createLineString(coordinates);
		ls.setSRID(srs);

		Geometry result = JTS.transform(ls, transform);

		return result;
	}

	private Geometry buildPoint(int srs,
			InversionGeorefCoordenadasData coordenada)
			throws NoSuchAuthorityCodeException, FactoryException,
			MismatchedDimensionException, TransformException {
		int ejeX = Integer.valueOf(coordenada.getEjeX());
		int ejeY = Integer.valueOf(coordenada.getEjeY());
		CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:" + srs);
		CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:4326");
		boolean lenient = true; // allow for some error due to different datums
		MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS,
				lenient);
		GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
		Point p = geometryFactory.createPoint(new Coordinate(ejeX, ejeY));
		p.setSRID(srs);

		Geometry result = JTS.transform(p, transform);

		return result;
	}

	private ChileindicaRelacionIntrumentosDataDto buildRelacionInstrumentosDto(
			RelacionInstrumentosData rid) {
		ChileindicaRelacionIntrumentosDataDto dto = new ChileindicaRelacionIntrumentosDataDto();
		dto.setCodigoInstrumento(getAsInteger(rid.getCodigo_Instrumento()));
		dto.setEspecificacion(rid.getEspecificacion());
		dto.setNombreInstrumento(rid.getNombre_Instrumento());
		dto.setRelacionAsociadaCodigo(rid.getRelacion_Asociada_Codigo());
		dto.setRelacionAsociadaNombre(rid.getRelacion_Asociada_Nombre());
		dto.setRelacionPrincipalCodigo(rid.getRelacion_Principal_Codigo());
		dto.setRelacionPrincipalNombre(rid.getRelacion_Principal_Nombre());

		return dto;
	}

	/**
	 * @param dto
	 * @param ifd
	 * @return
	 */
	private ChileindicaInversionFinanciamientoDataDto buildInversionFinanciamientoDto(
			InversionFinanciamientoData ifd) {
		ChileindicaInversionFinanciamientoDataDto fdto = new ChileindicaInversionFinanciamientoDataDto();
		fdto.setArrastre(ifd.getArrastre());
		fdto.setAsignacionDisponible(ifd.getAsignacionDisponible());
		fdto.setAsignacionPresupuestaria(ifd.getAsignacionPresupuestaria());
		fdto.setCostoTotalAjustado(ifd.getCostoTotalAjustado());
		fdto.setCostoTotalCore(ifd.getCostoTotalCORE());
		fdto.setCostoTotalEbi(ifd.getCostoTotalEBI());
		fdto.setCostoTotalEbiActualizado(ifd.getCostoTotalEBIActualizado());
		fdto.setCostoTotalEbiActualizadoMasDiezPorciento(ifd
				.getCostoTotalEBIActualizadoMasDiezPorciento());
		fdto.setGastadoAnosAnteriores(ifd.getGastadoAnosAnteriores());
		fdto.setNombreAsignacionPresupuestaria(ifd
				.getNombreAsignacionPresupuestaria());
		fdto.setNombreFuenteFinanciamiento(ifd.getNombreFuenteFinanciamiento());
		fdto.setSaldoAnosRestantes(ifd.getSaldoAnosRestantes());
		fdto.setSaldoPorAsignar(ifd.getSaldoPorAsignar());
		fdto.setSaldoProximoAno(ifd.getSaldoProximoAno());
		fdto.setSolicitadoAno(ifd.getSolicitadoAno());
		fdto.setTotalAsignado(ifd.getTotalAsignado());
		fdto.setTotalContratado(ifd.getTotalContratado());
		fdto.setTotalPagado(ifd.getTotalPagado());
		fdto.setTotalProgramado(ifd.getTotalProgramado());

		fdto.setEneroPagado(ifd.getEneroPagado());
		fdto.setFebreroPagado(ifd.getFebreroPagado());
		fdto.setMarzoPagado(ifd.getMarzoPagado());
		fdto.setAbrilPagado(ifd.getAbrilPagado());
		fdto.setMayoPagado(ifd.getMayoPagado());
		fdto.setJunioPagado(ifd.getJunioPagado());
		fdto.setJulioPagado(ifd.getJulioPagado());
		fdto.setAgostoPagado(ifd.getAgostoPagado());
		fdto.setSeptiembrePagado(ifd.getSeptiembrePagado());
		fdto.setOctubrePagado(ifd.getOctubrePagado());
		fdto.setNoviembrePagado(ifd.getNoviembrePagado());
		fdto.setDiciembrePagado(ifd.getDiciembrePagado());

		fdto.setEneroProgramado(ifd.getEneroProgramado());
		fdto.setFebreroProgramado(ifd.getFebreroProgramado());
		fdto.setMarzoProgramado(ifd.getMarzoProgramado());
		fdto.setAbrilProgramado(ifd.getAbrilProgramado());
		fdto.setMayoProgramado(ifd.getMayoProgramado());
		fdto.setJunioProgramado(ifd.getJunioProgramado());
		fdto.setJulioProgramado(ifd.getJulioProgramado());
		fdto.setAgostoProgramado(ifd.getAgostoProgramado());
		fdto.setSeptiembreProgramado(ifd.getSeptiembreProgramado());
		fdto.setCodigoFuenteFinanciamiento(ifd.getCodigoFuenteFinanciamiento());
		fdto.setOctubreProgramado(ifd.getOctubreProgramado());
		fdto.setNoviembreProgramado(ifd.getNoviembreProgramado());
		fdto.setDiciembreProgramado(ifd.getDiciembreProgramado());

		return fdto;
	}

	private Integer getAsInteger(BigInteger source) {
		return source != null ? source.intValue() : null;
	}

}
