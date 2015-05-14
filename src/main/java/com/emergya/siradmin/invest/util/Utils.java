package com.emergya.siradmin.invest.util;

import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class Utils {

    private static Log log = LogFactory.getLog(Utils.class);

    /**
     * Obtener el properties de la configuracion de los web services de iniciativas
     * 
     * @return
     */
    public static Properties getPropertiesInversionsWebServices() {
        Properties p = new Properties();

        try {
            InputStream inStream = Utils.class.getResourceAsStream("/webservices.properties");
            p.load(inStream);
        } catch (Exception e) {
            log.info("Error al obtener las propiedades de los web services de inversiones del sistema : " + e);
        }

        return p;
    }
}
