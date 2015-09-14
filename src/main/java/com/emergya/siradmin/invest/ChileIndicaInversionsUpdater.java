package com.emergya.siradmin.invest;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.inject.Inject;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.emergya.ohiggins.dto.ChileIndicaInversionsDataDto;
import com.emergya.ohiggins.service.ChileindicaInversionDataService;
import com.emergya.persistenceGeo.dao.RegionDatabasesContextHolder;
import com.emergya.siradmin.invest.util.Utils;
import com.emergya.siradmin.invest.util.WSCallException;
import com.emergya.siradmin.invest.util.WSURLRegionsCts;
import com.google.common.collect.ImmutableList;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

@Service
public class ChileIndicaInversionsUpdater {

	private static final String WS_PATH = "ws.chileindica.inversiones.";
	private static final String TOKEN = "&token=";
	@Value("#{webProperties['ws.chileindica.inversiones.token']}")
	private String tokenValue;
	@Inject
	private RegionDatabasesContextHolder regionDBContext;

	public enum InversionType {
		ARI("ari"), PROPIR("propir"), EJECUCION("ejecucion"), PREINVERSION(
				"preinversion");

		private String value;

		InversionType(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}

	private enum GeometryType {
		PUNTO, LINEA
	}

	private static final Log LOGGER = LogFactory
			.getLog(ChileIndicaInversionsUpdater.class);

	@Autowired
	private ChileindicaInversionDataService service;

	@Autowired
	private RestTemplate restTemplate;

	public void getExistingKeysInChileindica() {
		LOGGER.info("Obteniendo datos de iniciativas de inversión");
		List<ChileIndicaInversionsDataDto> result = new ArrayList<ChileIndicaInversionsDataDto>();
		// Iterate each region
		for (WSURLRegionsCts regionName : WSURLRegionsCts.values()) {
			List<ChileIndicaInversionsDataDto> regionProjectsInversions = new ArrayList<ChileIndicaInversionsDataDto>();
			// Iterate each inversionType
			for (InversionType inversionType : InversionType.values()) {

				restTemplate.setMessageConverters(getMessageConverters());
				restTemplate.getInterceptors().add((new TokenInterceptor()));
				//String plainCreds = "developer:LfvKJt3NjudRVxmoNAoq";
				//byte[] plainCredsBytes = plainCreds.getBytes();
				//byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
				//String base64Creds = new String(base64CredsBytes);
				HttpHeaders headers = new HttpHeaders();
				//headers.add("Authorization", "Basic " + base64Creds);
				headers.setAccept(Arrays.asList(MediaType.TEXT_HTML));
				HttpEntity<?> entity = new HttpEntity<Object>(headers);
				// headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
				StringBuilder wsUrlBuilder = new StringBuilder();
				wsUrlBuilder.append(WS_PATH)
						.append(regionName.toString().toLowerCase())
						.append(".").append(inversionType.getValue());
				// 1: Obtener fichero propiedades url de los web services
				Properties p = Utils.getPropertiesInversionsWebServices();
				String wsUrl = p.getProperty(wsUrlBuilder.toString());
				if (wsUrl != null && !"".equals(wsUrl)) {
					// ResponseEntity<ChileIndicaInversionsDataDto[]>
					// responseEntity
					// = restTemplate.getForEntity(wsUrl,
					// ChileIndicaInversionsDataDto[].class);

					ResponseEntity<ChileIndicaInversionsDataDto[]> responseEntity = null;

					try {
						responseEntity = restTemplate.exchange(wsUrl,
								HttpMethod.GET, entity,
								ChileIndicaInversionsDataDto[].class);
					} catch (Exception e) {
						// Custom error handler in action, here we're supposed
						// to receive a WSCallException
						if (e instanceof WSCallException) {
							WSCallException exception = (WSCallException) e;
							LOGGER.info("An error occurred while calling inversions service endpoint: "
									+ exception.getMessage());
						} else {
							LOGGER.info("An error occurred while trying to parse ChileIndicaInversions Response JSON object. Cause: " + e.getMessage());
						}
					}
					if (responseEntity != null
							&& HttpStatus.OK.equals(responseEntity
									.getStatusCode())) {
						ChileIndicaInversionsDataDto[] regionInversionTypeProjects = responseEntity
								.getBody();
						if (regionInversionTypeProjects != null
								&& regionInversionTypeProjects.length > 0) {
							for (ChileIndicaInversionsDataDto inversionProject : regionInversionTypeProjects) {

								if (inversionProject.getGeoreferenciacion() != null
										&& (inversionProject
												.getGeoreferenciacion()).length > 0) {
									if (inversionProject.getGeoreferenciacion()[0]
											.getOgc() != null
											&& inversionProject
													.getGeoreferenciacion()[0]
													.getX() != null
											&& !"".equals(inversionProject
													.getGeoreferenciacion()[0]
													.getX())
											&& inversionProject
													.getGeoreferenciacion()[0]
													.getY() != null
											&& !"".equals(inversionProject
													.getGeoreferenciacion()[0]
													.getY())) {
										Geometry geometry = buildGeometry(
												inversionProject
														.getGeoreferenciacion()[0]
														.getX(),
												inversionProject
														.getGeoreferenciacion()[0]
														.getY(),
												inversionProject
														.getGeoreferenciacion()[0]
														.getOgc());
										inversionProject
												.setInversionGeoreferenciacion(geometry);
									}
								}

								if (inversionProject.getFechaRegistro() != null) {
									inversionProject
											.setFechaRegistroIniciativa(service
													.getValidDate(inversionProject
															.getFechaRegistro()));
								}
								regionProjectsInversions.add(inversionProject);
							}
						}
					}
				}
			}
			setRegionsDatabase(regionName);
			getWsDataAndUpdateDB(regionProjectsInversions);
			result.addAll(regionProjectsInversions);
		}
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("");
			LOGGER.info("******************************************************");
			LOGGER.info("Resultado de la actualización:");
			for (ChileIndicaInversionsDataDto projectInversion : result) {
				LOGGER.info(projectInversion);
			}
			LOGGER.info("******************************************************");
			LOGGER.info("Actualización finalizada a a las "
					+ Calendar.getInstance().getTime());
		}
		// return result;
	}

	private List<HttpMessageConverter<?>> getMessageConverters() {
		List<HttpMessageConverter<?>> converters = new ArrayList<HttpMessageConverter<?>>();
		converters.add(new MappingJackson2HttpMessageConverter());
		MappingJackson2HttpMessageConverter converter = (MappingJackson2HttpMessageConverter) converters
				.get(0);
		converter.setSupportedMediaTypes(ImmutableList.of(new MediaType(
				"application", "json",
				MappingJackson2HttpMessageConverter.DEFAULT_CHARSET),
				new MediaType("text", "html",
						MappingJackson2HttpMessageConverter.DEFAULT_CHARSET)));
		return converters;
	}

	public class TokenInterceptor implements ClientHttpRequestInterceptor {

		@Override
		public ClientHttpResponse intercept(HttpRequest request, byte[] body,
				ClientHttpRequestExecution execution) throws IOException {

			HttpHeaders headers = request.getHeaders();
			headers.setAccept(Arrays.asList(MediaType.TEXT_HTML,
					MediaType.APPLICATION_JSON));
			return execution.execute(request, body);
		}
	}

	public void getWsDataAndUpdateDB(
			List<ChileIndicaInversionsDataDto> wsProjectList) {
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
			ChileIndicaInversionsDataDto dto = (ChileIndicaInversionsDataDto) service
					.getById(dbId);
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Tratando proyecto existente BBDD "
						+ dto.getIdIniciativaInversion());
			}
			// Buscamos si el proyecto existe entre las claves actuales
			int foundKeyIndex = Collections.binarySearch(wsProjectList, dto);
			if (foundKeyIndex >= 0) {
				ChileIndicaInversionsDataDto foundKey = wsProjectList
						.get(foundKeyIndex);
				iterator.remove();
				wsProjectList.remove(foundKeyIndex);
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info("El proyecto sigue existiendo en el servicio web de consulta de iniciativas de inversión");
				}
				// Si el proyecto ha sido actualizado en Chileindica recuperamos
				// los detalles del servicio web, borramos el proyecto original
				// de la base de datos y creamos una entrada nueva en BD
				boolean updatable = service.checkIfProjectMustBeUpdated(
						foundKey.getIdIniciativaInversion(),
						foundKey.getFechaRegistro());
				if (updatable) {
					service.delete(dto);
					service.create(foundKey);
					if (LOGGER.isInfoEnabled()) {
						LOGGER.info("Proyecto en BBDD actualizado");
					}
				} else {
					if (LOGGER.isInfoEnabled()) {
						LOGGER.info("El proyecto no necesita ser actualizado en BBDD");
					}
				}
			} else {
				// si el proyecto no ha sido devuleto por el WS Consulta de
				// iniciativas, se borra de la base de datos
				service.delete(dto);
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info("Se elimina el proyecto por no existir en la consulta de llaves "
							+ dto);
				}
			}
		}

		// En este punto los elementos que quedan en projectDbIds pueden ser
		// eliminados de la base de datos puesto que no han sido devueltos por
		// el servicio web de consulta de iniciativas de inversión

		// Ahora se crean los proyectos que queden en la lista de iniciativas de
		// inversión
		// devueltas por el servicio web.
		for (ChileIndicaInversionsDataDto inversionProject : wsProjectList) {
			service.create(inversionProject);
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Creada nueva entrada en BBDD");
			}
		}
	}

	protected void setRegionsDatabase(final WSURLRegionsCts regionName) {
		switch (regionName) {
		case TARAPACA:
			// inversionProject.setRegion(Long.valueOf(1));
			regionDBContext.setMultiSirRegionBeanDatabase(Long.valueOf(1));
			break;
		case ANTOFAGASTA:
			// inversionProject.setRegion(Long.valueOf(2));
			regionDBContext.setMultiSirRegionBeanDatabase(Long.valueOf(2));
			break;
		case ATACAMA:
			// inversionProject.setRegion(Long.valueOf(3));
			regionDBContext.setMultiSirRegionBeanDatabase(Long.valueOf(3));
			break;
		case COQUIMBO:
			// inversionProject.setRegion(Long.valueOf(4));
			regionDBContext.setMultiSirRegionBeanDatabase(Long.valueOf(4));
			break;
		case VALPARAISO:
			// inversionProject.setRegion(Long.valueOf(5));
			regionDBContext.setMultiSirRegionBeanDatabase(Long.valueOf(5));
			break;
		case OHIGGINS:
			// inversionProject.setRegion(Long.valueOf(6));
			regionDBContext.setMultiSirRegionBeanDatabase(Long.valueOf(6));
			break;
		case MAULE:
			// inversionProject.setRegion(Long.valueOf(7));
			regionDBContext.setMultiSirRegionBeanDatabase(Long.valueOf(7));
			break;
		case BIOBIO:
			// inversionProject.setRegion(Long.valueOf(8));
			regionDBContext.setMultiSirRegionBeanDatabase(Long.valueOf(8));
			break;
		case ARAUCANIA:
			// inversionProject.setRegion(Long.valueOf(9));
			regionDBContext.setMultiSirRegionBeanDatabase(Long.valueOf(9));
			break;
		case LOS_LAGOS:
			// inversionProject.setRegion(Long.valueOf(10));
			regionDBContext.setMultiSirRegionBeanDatabase(Long.valueOf(10));
			break;
		case AYSEN:
			// inversionProject.setRegion(Long.valueOf(11));
			regionDBContext.setMultiSirRegionBeanDatabase(Long.valueOf(11));
			break;
		case MAGALLANES:
			// inversionProject.setRegion(Long.valueOf(12));
			regionDBContext.setMultiSirRegionBeanDatabase(Long.valueOf(12));
			break;
		case SANTIAGO:
			// inversionProject.setRegion(Long.valueOf(13));
			regionDBContext.setMultiSirRegionBeanDatabase(Long.valueOf(13));
			break;
		case LOS_RIOS:
			// inversionProject.setRegion(Long.valueOf(14));
			regionDBContext.setMultiSirRegionBeanDatabase(Long.valueOf(14));
			break;
		// case ARICA:
		// inversionProject.setRegion(Long.valueOf(15));
		// regionDBContext.setMultiSirRegionBeanDatabase(Long.valueOf(15));
		// break;
		default:
			// inversionProject.setRegion(Long.valueOf(15));
			regionDBContext.setMultiSirRegionBeanDatabase(Long.valueOf(15));
			break;
		}
	}

	private Geometry buildGeometry(final String x, final String y,
			final BigInteger ogc) {
		// GeometryType gt = GeometryType.PUNTO;
		int srs = getAsInteger(ogc);
		Geometry geom = null;

		try {
			geom = buildPoint(srs, x, y);
		} catch (Exception e) {
			LOGGER.error("Error construyendo la geometría ", e);
		}

		return geom;
	}

	private Geometry buildPoint(final int srs, final String x, final String y)
			throws NoSuchAuthorityCodeException, FactoryException,
			MismatchedDimensionException, TransformException {
		int ejeX = Integer.valueOf(x);
		int ejeY = Integer.valueOf(y);
		CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:" + srs);
		CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:4326", true);
		boolean lenient = true; // allow for some error due to different datums
		MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS,
				lenient);

		GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
		Point p = geometryFactory.createPoint(new Coordinate(ejeX, ejeY));
		p.setSRID(srs);

		Geometry result = JTS.transform(p, transform);

		result.setSRID(4326);

		return result;
	}

	private Integer getAsInteger(BigInteger source) {
		return source != null ? source.intValue() : null;
	}

}
