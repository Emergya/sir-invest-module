package com.emergya.siradmin.invest;

import java.util.Calendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:testDataSource.xml" })
// @TransactionConfiguration(defaultRollback = true, transactionManager =
// "multiSIRDatabaseTransactionManager")
// @Transactional("multiSIRDatabaseTransactionManager")
public class TestChileIndicaInversionsWS {

    private static final Log LOGGER = LogFactory.getLog(TestChileIndicaInversionsWS.class);

    @Autowired(required = false)
    ChileIndicaInversionsUpdater chileIndicaInversionsUpdater;

    @Test
    public void chileIndicaInversionsWS() {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("");
            LOGGER.info("******************************************************");
            LOGGER.info("Comenzando actualización a a las " + Calendar.getInstance().getTime());
        }

        // List<ChileIndicaInversionsDataDto> projectsInversions = new
        // ArrayList<ChileIndicaInversionsDataDto>();
        // projectsInversions.addAll(chileIndicaInversionsUpdater.getExistingKeysInChileindica());
        chileIndicaInversionsUpdater.getExistingKeysInChileindica();

        // chileIndicaInversionsUpdater.getWsDataAndUpdateDB(projectsInversions);

        // if (LOGGER.isInfoEnabled()) {
        // LOGGER.info("");
        // LOGGER.info("******************************************************");
        // LOGGER.info("Resultado de la actualización:");
        // for (ChileIndicaInversionsDataDto projectInversion :
        // projectsInversions) {
        // LOGGER.info(projectInversion);
        // }
        // LOGGER.info("******************************************************");
        // LOGGER.info("Actualización finalizada a a las " +
        // Calendar.getInstance().getTime());
        // }
    }
}
