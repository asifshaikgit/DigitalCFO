package service;

import com.idos.util.IDOSException;
import model.AgeingReport;
import model.Users;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import java.util.List;

/**
 * Created by Sunil K. Namdev on 25-11-2017.
 */
public class AgeingReportServiceImpl implements AgeingReportService {
    @Override
    public List<AgeingReport> getAgeingReportList(String ageingDate, Users user, EntityManager entityManager,
            boolean isPayable) throws IDOSException {
        return AGEING_REPORT_DAO.getAgeingReportList(ageingDate, user, entityManager, isPayable);
    }

    @Override
    public List<AgeingReport> getAgeingReport2List(String ageingDate, Users user, EntityManager entityManager,
            boolean isPayable) throws IDOSException {
        return AGEING_REPORT_DAO.getAgeingReport2List(ageingDate, user, entityManager, isPayable);
    }

    @Override
    public List<AgeingReport> getBrsTempList(String ageingDate, long bankid, Users user, EntityManager em,
            boolean isPayable) throws IDOSException {
        return AGEING_REPORT_DAO.getBrsTempList(ageingDate, bankid, user, em, isPayable);
    }
}
