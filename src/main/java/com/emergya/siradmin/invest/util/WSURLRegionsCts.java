package com.emergya.siradmin.invest.util;

public enum WSURLRegionsCts {
   ARICA("arica"), OHIGGINS("ohiggins"), TARAPACA("tarapaca"), ANTOFAGASTA("antofagasta"), ATACAMA("atacama"), COQUIMBO("coquimbo"), VALPARAISO("valparaiso"), MAULE(
            "maule"), BIOBIO("biobio"), ARAUCANIA("araucania"), LOS_RIOS("losrios"), LOS_LAGOS("loslagos"), AYSEN("aysen"), MAGALLANES("magallanes"), SANTIAGO(
            "rms");

    private String value;

    WSURLRegionsCts(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
