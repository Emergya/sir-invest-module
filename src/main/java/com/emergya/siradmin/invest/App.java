//package com.emergya.siradmin.invest;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.List;
//
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//import org.springframework.beans.factory.config.PropertiesFactoryBean;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.support.ClassPathXmlApplicationContext;
//
//import com.emergya.siradmin.invest.util.LlaveBean;
//import com.google.common.collect.Ordering;
//
///**
// * Hello world!
// * 
// */
//public class App {
//	private static final Log LOGGER = LogFactory.getLog(App.class);
//
//	public static void main(String[] args) throws IOException {
//
//		if (LOGGER.isInfoEnabled()) {
//			LOGGER.info("");
//			LOGGER.info("******************************************************");
//			LOGGER.info("Comenzando actualización a a las "
//					+ Calendar.getInstance().getTime());
//		}
//		ApplicationContext context = new ClassPathXmlApplicationContext(
//				"/applicationContext.xml");
//		PropertiesFactoryBean pfb = context
//				.getBean(org.springframework.beans.factory.config.PropertiesFactoryBean.class);
//		String firstYearString = pfb.getObject().getProperty("firstyear",
//				"2008");
//		Integer firstYear = Integer.valueOf(firstYearString);
//		Integer lastYear = Calendar.getInstance().get(Calendar.YEAR);
//		InvestmentUpdater updater = context.getBean(InvestmentUpdater.class);
//
//		List<LlaveBean> lista = new ArrayList<LlaveBean>();
//		for (int year = firstYear; year <= lastYear; year++) {
//			lista.addAll(updater.getExistingKeysInChileindica(year, 15));
//		}
//
//		updater.getWsDataAndUpdateDB(lista);
//
//		if (LOGGER.isInfoEnabled()) {
//			LOGGER.info("");
//			LOGGER.info("******************************************************");
//			LOGGER.info("Resultado de la actualización:");
//			for (LlaveBean llave : lista) {
//				LOGGER.info(llave);
//			}
//			LOGGER.info("******************************************************");
//			LOGGER.info("Actualización finalizada a a las "
//					+ Calendar.getInstance().getTime());
//		}
//
//		System.exit(0);
//
//	}
// }
