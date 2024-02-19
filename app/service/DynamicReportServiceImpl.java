package service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;
import java.util.Map;
import play.Environment;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.JRCsvExporterParameter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRRtfExporter;
import net.sf.jasperreports.engine.export.JRTextExporter;
import net.sf.jasperreports.engine.export.JRTextExporterParameter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.util.JRLoader;

import java.util.logging.Logger;
import java.util.logging.Level;

// import org.omg.CORBA.Environment;
import org.springframework.beans.factory.annotation.Autowired;

import com.typesafe.config.ConfigFactory;
import play.mvc.Http;

import com.idos.dao.GenericDAO;
import play.Application;
import javax.inject.Inject;

/**
 * @author mritunjay
 */
@SuppressWarnings({ "unchecked", "deprecation" })
public class DynamicReportServiceImpl implements DynamicReportService {
    private final Application application;

    @Inject
    public DynamicReportServiceImpl(Application application) {
        this.application = application;
    }

    @Override
    public ByteArrayOutputStream generateStaticReport(String reportName, List datas, Map params, String type,
            Http.Request request) {
        try {
            JasperPrint jp = getJasperPrintFromFile(reportName, datas, params, request);
            ByteArrayOutputStream out = getStreamByType(type, jp, request);
            return out;
        } catch (Exception ex) {
            log.log(Level.FINE, "Error", ex);
            return null;
        }
    }

    @Override
    // Use this method, when in Detail section, we are using direct fields, so it is
    // on same page, so we are passing data directly in datasource and NOT as param
    public ByteArrayOutputStream generateStaticReportOld(String reportName, List datas, Map params, String type,
            Http.Request request, Application application) {
        try {
            JasperPrint jp = getJasperPrintFromFileOld(reportName, datas, params, request, application);
            ByteArrayOutputStream out = getStreamByType(type, jp, request);
            return out;
        } catch (Exception ex) {
            log.log(Level.FINE, "Error", ex);
            return null;
        }
    }

    private JasperPrint getJasperPrintFromFileOld(String reportName, List datas, Map params, Http.Request request,
            Application application) {
        log.log(Level.FINE, "============ Start");
        try {
            File reportFile = new File(application.path().toString() + "/public/report/" + reportName + ".jasper");
            JasperReport jasperReport = (JasperReport) JRLoader.loadObjectFromFile(reportFile.getPath());

            JasperPrint jp = null;
            if (datas != null) {
                JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(datas);
                try {
                    jp = JasperFillManager.fillReport(jasperReport, params, new JREmptyDataSource());
                } catch (Exception ex) {
                    if (ex.toString().contains("Error opening input stream from URL")) {
                        params.put("companyLogo", null);
                        jp = JasperFillManager.fillReport(jasperReport, params, new JREmptyDataSource());
                    }
                    log.log(Level.SEVERE, "Error", ex);
                }
            } else {
                try {
                    jp = JasperFillManager.fillReport(jasperReport, params);
                } catch (Exception ex) {
                    if (ex.toString().contains("Error opening input stream from URL")) {
                        params.put("companyLogo", null);
                        jp = JasperFillManager.fillReport(jasperReport, params);
                    }

                }
            }
            return jp;
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            return null;
        }
    }

    @Override
    // Use this method when you have table in your report and want to pass data to
    // that as param:transactionDS, because in fillreport, we are having empty
    // datasource
    public JasperPrint getJasperPrintFromFile(String reportName, List datas, Map params, Http.Request request) {
        log.log(Level.FINE, "============ Start");
        try {
            Environment environment = Environment.simple();
            File reportFile = new File(environment.rootPath() + "/public/report/" + reportName + ".jasper");
            JasperReport jasperReport = (JasperReport) JRLoader.loadObjectFromFile(reportFile.getPath());

            JasperPrint jp = null;
            if (datas != null) {
                JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(datas);
                // jp = JasperFillManager.fillReport(jasperReport, params, dataSource);
                params.put("transactionDS", dataSource);

                try {
                    jp = JasperFillManager.fillReport(jasperReport, params, new JREmptyDataSource());
                } catch (Exception ex) {
                    if (ex.toString().contains("Error opening input stream from URL")) {
                        params.put("companyLogo", null);
                        jp = JasperFillManager.fillReport(jasperReport, params, new JREmptyDataSource());
                    }
                    log.log(Level.SEVERE, "Error", ex);
                }

            } else {
                try {
                    jp = JasperFillManager.fillReport(jasperReport, params);

                } catch (Exception ex) {
                    if (ex.toString().contains("Error opening input stream from URL")) {
                        params.put("companyLogo", null);
                        jp = JasperFillManager.fillReport(jasperReport, params);
                    }
                    log.log(Level.SEVERE, "Error", ex);
                }
            }
            return jp;
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            return null;
        }
    }

    @Override
    // Use this method when you have table in your report and want to pass data to
    // that as idosDataSource, because in fillreport, we are having empty datasource
    public ByteArrayOutputStream getJasperPrintFromFileUsingJtable(String reportName, List datas, Map params,
            String type, Http.Request request, Application application) {
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, "============ Start > " + reportName);
        try {
            File reportFile = new File(application.path().toString() + "/public/report/" + reportName + ".jasper");
            JasperReport jasperReport = (JasperReport) JRLoader.loadObjectFromFile(reportFile.getPath());

            JasperPrint jp = null;
            if (datas != null) {
                JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(datas);
                params.put("idosDataSource", dataSource);
                params.put("transactionDS", dataSource);
                try {
                    jp = JasperFillManager.fillReport(jasperReport, params, new JREmptyDataSource());
                } catch (Exception ex) {
                    if (ex.toString().contains("Error opening input stream from URL")) {
                        params.put("companyLogo", null);
                        jp = JasperFillManager.fillReport(jasperReport, params, new JREmptyDataSource());
                    }
                    log.log(Level.SEVERE, "Error", ex);
                }
            } else {
                try {
                    jp = JasperFillManager.fillReport(jasperReport, params);
                } catch (Exception ex) {
                    if (ex.toString().contains("Error opening input stream from URL")) {
                        params.put("companyLogo", null);
                        jp = JasperFillManager.fillReport(jasperReport, params);
                    }
                    log.log(Level.SEVERE, "Error", ex);
                }
            }
            ByteArrayOutputStream out = getStreamByType(type, jp, request);
            return out;
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            return null;
        }
    }

    @Override
    public ByteArrayOutputStream getJasperPrintFromFileUsingJtable(String reportName, List idosDataList,
            List advAdjList, Map params, String type, Http.Request request, Application application) {
        log.log(Level.FINE, "============ Start " + reportName);
        try {
            File reportFile = new File(application.path().toString() + "/public/report/" + reportName + ".jasper");
            JasperReport jasperReport = (JasperReport) JRLoader.loadObjectFromFile(reportFile.getPath());
            JasperPrint jp = null;

            if (idosDataList != null && advAdjList == null) {
                JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(idosDataList);
                params.put("transactionDS", dataSource);
                try {
                    jp = JasperFillManager.fillReport(jasperReport, params, new JREmptyDataSource());
                } catch (Exception ex) {
                    if (ex.toString().contains("Error opening input stream from URL")) {
                        params.put("companyLogo", null);
                        jp = JasperFillManager.fillReport(jasperReport, params, new JREmptyDataSource());
                    }
                    log.log(Level.SEVERE, "Error", ex);
                }
            } else if (idosDataList == null && advAdjList != null) {

                JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(advAdjList);
                params.put("advanceDS", dataSource);
                try {
                    jp = JasperFillManager.fillReport(jasperReport, params, new JREmptyDataSource());
                } catch (Exception ex) {
                    if (ex.toString().contains("Error opening input stream from URL")) {
                        params.put("companyLogo", null);
                        jp = JasperFillManager.fillReport(jasperReport, params, new JREmptyDataSource());
                    }
                    log.log(Level.SEVERE, "Error", ex);
                }
            } else if (idosDataList != null && advAdjList != null) {
                JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(idosDataList);
                params.put("transactionDS", dataSource);
                JRBeanCollectionDataSource dataSource2 = new JRBeanCollectionDataSource(advAdjList);
                params.put("advanceDS", dataSource2);
                try {
                    jp = JasperFillManager.fillReport(jasperReport, params, new JREmptyDataSource());
                } catch (Exception ex) {
                    if (ex.toString().contains("Error opening input stream from URL")) {
                        params.put("companyLogo", null);
                        jp = JasperFillManager.fillReport(jasperReport, params, new JREmptyDataSource());
                    }
                    log.log(Level.SEVERE, "Error", ex);
                }
            } else {
                try {
                    jp = JasperFillManager.fillReport(jasperReport, params);
                } catch (Exception ex) {
                    if (ex.toString().contains("Error opening input stream from URL")) {
                        params.put("companyLogo", null);
                        jp = JasperFillManager.fillReport(jasperReport, params);
                    }
                    log.log(Level.SEVERE, "Error", ex);
                }
            }
            ByteArrayOutputStream out = getStreamByType(type, jp, request);

            return out;
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            return null;
        }

    }

    private ByteArrayOutputStream getStreamByType(String type, JasperPrint jp,
            Http.Request request) throws JRException {
        log.log(Level.FINE, "============ Start");
        if ("xls".equalsIgnoreCase(type)) {
            return exportReportXls(jp);
        } else if ("xlsx".equalsIgnoreCase(type)) {
            return exportReportXlsx(jp);
        } else if ("pdf".equalsIgnoreCase(type)) {
            return exportReportPdf(jp);
        } else if ("csv".equalsIgnoreCase(type)) {
            return exportReportCsv(jp);
        } else if ("rtf".equalsIgnoreCase(type)) {
            return exportReportRtf(jp);
        } else {
            return exportReportText(jp);
        }
    }

    private ByteArrayOutputStream exportReportXls(JasperPrint jp)
            throws JRException {
        log.log(Level.FINE, "============ Start");
        JRXlsExporter exporter = new JRXlsExporter();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        exporter.setParameter(JRExporterParameter.JASPER_PRINT, jp);
        exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, out);
        exporter.setParameter(JRXlsExporterParameter.IS_DETECT_CELL_TYPE,
                Boolean.TRUE);
        exporter.setParameter(JRXlsExporterParameter.IS_WHITE_PAGE_BACKGROUND,
                Boolean.FALSE);
        exporter.setParameter(JRXlsExporterParameter.IS_IGNORE_GRAPHICS,
                Boolean.FALSE);
        exporter.exportReport();

        return out;
    }

    private ByteArrayOutputStream exportReportXlsx(JasperPrint jp)
            throws JRException {
        log.log(Level.FINE, "============ Start");
        JRXlsxExporter exporter = new JRXlsxExporter();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        exporter.setParameter(JRExporterParameter.JASPER_PRINT, jp);
        exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, out);
        exporter.setParameter(JRXlsExporterParameter.IS_DETECT_CELL_TYPE,
                Boolean.TRUE);
        exporter.setParameter(JRXlsExporterParameter.IS_WHITE_PAGE_BACKGROUND,
                Boolean.FALSE);
        exporter.setParameter(JRXlsExporterParameter.IS_IGNORE_GRAPHICS,
                Boolean.FALSE);
        exporter.exportReport();

        return out;
    }

    private ByteArrayOutputStream exportReportPdf(JasperPrint jp)
            throws JRException {
        log.log(Level.FINE, "============ Start");
        JRPdfExporter exporter = new JRPdfExporter();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        exporter.setParameter(JRExporterParameter.JASPER_PRINT, jp);
        exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, out);
        exporter.exportReport();
        log.log(Level.FINE, "============ End");
        return out;
    }

    private ByteArrayOutputStream exportReportRtf(JasperPrint jp)
            throws JRException {
        log.log(Level.FINE, "============ Start");
        JRRtfExporter exporter = new JRRtfExporter();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        exporter.setParameter(JRExporterParameter.JASPER_PRINT, jp);
        exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, out);
        exporter.exportReport();

        return out;
    }

    private ByteArrayOutputStream exportReportText(JasperPrint jp)
            throws JRException {
        log.log(Level.FINE, "============ Start");
        JRTextExporter exporter = new JRTextExporter();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        exporter.setParameter(JRExporterParameter.JASPER_PRINT, jp);
        exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, out);
        exporter.setParameter(JRTextExporterParameter.CHARACTER_WIDTH, 10F);
        exporter.setParameter(JRTextExporterParameter.CHARACTER_HEIGHT, 10F);
        exporter.exportReport();
        return out;
    }

    private ByteArrayOutputStream exportReportCsv(JasperPrint jp)
            throws JRException {
        log.log(Level.FINE, "============ Start");
        JRCsvExporter exporter = new JRCsvExporter();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        exporter.setParameter(JRExporterParameter.JASPER_PRINT, jp);
        exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, out);
        exporter.setParameter(JRCsvExporterParameter.IGNORE_PAGE_MARGINS,
                Boolean.TRUE);
        exporter.exportReport();

        return out;
    }
}
