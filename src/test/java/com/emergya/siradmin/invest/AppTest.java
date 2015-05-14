//package com.emergya.siradmin.invest;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.*;
//
//import java.math.BigInteger;
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.rmi.RemoteException;
//import java.util.List;
//
//import javax.annotation.Resource;
//import javax.validation.constraints.AssertTrue;
//import javax.xml.namespace.QName;
//import javax.xml.rpc.ServiceException;
//
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//import org.hibernate.SessionFactory;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//import org.springframework.test.context.transaction.TransactionConfiguration;
//import org.springframework.transaction.annotation.Transactional;
//
//import com.emergya.ohiggins.model.ChileindicaInversionDataEntity;
//import com.emergya.siradmin.invest.client.keyquery.ConsultaLlavesResponse;
//import com.emergya.siradmin.invest.client.keyquery.DatosLlamada;
//import com.emergya.siradmin.invest.client.keyquery.LlavesInversionData;
//import com.emergya.siradmin.invest.client.keyquery.Respuesta;
//import com.emergya.siradmin.invest.client.keyquery.WSConsultaLlaves;
//import com.emergya.siradmin.invest.client.keyquery.WSConsultaLlavesLocator;
//import com.emergya.siradmin.invest.client.keyquery.WSConsultaLlavesPortType;
//
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations = { "classpath:applicationContext.xml" })
//@TransactionConfiguration(defaultRollback = true, transactionManager = "transactionManager")
//@Transactional
//public class AppTest {
//	private static final Log LOGGER = LogFactory.getLog(AppTest.class);
//
//	WSConsultaLlaves service;
//	@Resource
//	private SessionFactory sessionFactory;
//
//	@Before
//	public void setUp() throws Exception {
//		service = new WSConsultaLlavesLocator(
//				"http://qaweb.chileindica.cl/ws/consultallaves.php?wsdl",
//				QName.valueOf("{urn:WSConsultaLlaves}WSConsultaLlaves"));
//	}
//
//	@Test
//	public void testProjectShouldBeUpdated() throws MalformedURLException,
//			ServiceException, RemoteException {
//		ChileindicaInversionDataEntity nueva = new ChileindicaInversionDataEntity();
//		Long nuevaId = (Long) sessionFactory.getCurrentSession().save(nueva);
//		assertNotNull(nuevaId);
//
//		WSConsultaLlavesPortType port = service
//				.getWSConsultaLlavesPort(new URL(
//						"http://qaweb.chileindica.cl/ws/consultallaves.php"));
//		ConsultaLlavesResponse response = port.WSConsultaLlaves(
//				BigInteger.valueOf(2013), BigInteger.valueOf(15));
//		Respuesta respuesta = response.getRespuesta();
//		assertNotNull(respuesta.getCodigoRespuesta());
//		assertEquals(BigInteger.ZERO, respuesta.getCodigoRespuesta());
//		assertEquals("Respuesta OK.", respuesta.getTextoRespuesta());
//		DatosLlamada datosLlamada = response.getDatosLlamada();
//		assertEquals(BigInteger.valueOf(2013), datosLlamada.getAno());
//		assertEquals(BigInteger.valueOf(15), datosLlamada.getRegion());
//		LlavesInversionData[] llavesInversion = response.getLlavesInversion();
//		if (LOGGER.isDebugEnabled()) {
//			for (LlavesInversionData llave : llavesInversion) {
//				LOGGER.debug("Año = " + llave.getAno() + "; C_Institucion = "
//						+ llave.getC_Institucion() + "; C_Preinversion = "
//						+ llave.getC_Preinversion() + "; C_Ficha = "
//						+ llave.getC_Ficha() + "; Fecha_Registro = "
//						+ llave.getFechaRegistro());
//			}
//		}
//
//	}
//
// }
