package com.idos.util;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.Organization;
import model.Users;

import java.util.logging.Logger;
import java.util.logging.Level;

import controllers.StaticController;

public class DateUtil {

    public static Logger log = Logger.getLogger("controllers");
    public static DecimalFormat decimalFormat = new DecimalFormat("######.00");
    public static SimpleDateFormat idosdf = new SimpleDateFormat("MMM dd,yyyy");
    public static SimpleDateFormat idosmdtdf = new SimpleDateFormat("MMM dd");
    public static SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");
    public static SimpleDateFormat mysqldf = new SimpleDateFormat("yyyy-MM-dd");
    public static SimpleDateFormat mysqlmdtdf = new SimpleDateFormat("MM-dd");
    public static SimpleDateFormat reportdf = new SimpleDateFormat("dd-MM-yyyy");
    public static SimpleDateFormat mysqldtf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static SimpleDateFormat timefmt = new SimpleDateFormat("HH:mm:ss");
    public static Map criterias = new HashMap();
    public static SimpleDateFormat monthtext = new SimpleDateFormat("MMM");
    public static SimpleDateFormat monthnumber = new SimpleDateFormat("MM");
    public static SimpleDateFormat yearnumber = new SimpleDateFormat("yyyy");

    public static long calculateDays(String startDate, String endDate) {
        Date sDate = new Date(startDate);
        Date eDate = new Date(endDate);
        Calendar cal3 = Calendar.getInstance();
        cal3.setTime(sDate);
        Calendar cal4 = Calendar.getInstance();
        cal4.setTime(eDate);
        return daysBetween(cal3, cal4);
    }

    public static long daysBetween(Calendar startDate, Calendar endDate) {
        Calendar date = (Calendar) startDate.clone();
        long daysBetween = 0;
        while (date.before(endDate)) {
            date.add(Calendar.DAY_OF_MONTH, 1);
            daysBetween++;
        }
        return daysBetween;
    }

    public static List<String> daysList(Calendar startDate, Calendar endDate) {
        Calendar date = (Calendar) startDate.clone();
        List<String> listOfdate = new ArrayList<String>();
        while (date.before(endDate)) {
            listOfdate.add(mysqldf.format(date.getTime()));
            date.add(Calendar.DATE, 1);
        }
        if (date.equals(endDate)) {
            listOfdate.add(mysqldf.format(date.getTime()));
        }
        return listOfdate;
    }

    public static List<String> returnListOfDateString(String startDate, String endDate) {
        List<String> listOfdate = new ArrayList<String>();
        try {
            Date sDate = new Date(startDate);
            Date eDate = new Date(endDate);
            Calendar cal3 = Calendar.getInstance();
            cal3.setTime(sDate);
            Calendar cal4 = Calendar.getInstance();
            cal4.setTime(eDate);
            listOfdate = daysList(cal3, cal4);
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
        }
        return listOfdate;
    }

    public static List<String> returnFirstDayAndLastDayOfMonth() {
        List<String> listOfdate = new ArrayList<String>();
        try {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_MONTH, 1);
            Date firstDayOfTheMonth = cal.getTime();
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
            Date lastDayOfTheMonth = cal.getTime();
            String fstDt = mysqldf.format(firstDayOfTheMonth);
            String lstDt = mysqldf.format(lastDayOfTheMonth);
            listOfdate.add(fstDt);
            listOfdate.add(lstDt);
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
        }
        return listOfdate;
    }

    public static List<String> returnFirstDayAndLastDayOfGivenMonth(int month, int year) {
        List<String> listOfdate = new ArrayList<String>();
        try {
            Calendar cal = Calendar.getInstance();
            cal.set(year, month, 1);
            Date firstDayOfTheMonth = cal.getTime();
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
            Date lastDayOfTheMonth = cal.getTime();
            String fstDt = mysqldf.format(firstDayOfTheMonth);
            String lstDt = mysqldf.format(lastDayOfTheMonth);
            listOfdate.add(fstDt);
            listOfdate.add(lstDt);
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
        }
        return listOfdate;
    }

    public static String returnOneYearBackDate() {
        String oneYearPrevDate = null;
        try {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.YEAR, -1);
            Date prvYrDt = cal.getTime();
            oneYearPrevDate = mysqldf.format(prvYrDt);
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
        }
        return oneYearPrevDate;
    }

    public static String returnPrevOneMonthDate(final Date fromDate) {
        String prevOneMonthDate = null;
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(fromDate);
            cal.add(Calendar.MONTH, -1);
            Date prevMonDt = cal.getTime();
            prevOneMonthDate = mysqldf.format(prevMonDt);
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
        }
        return prevOneMonthDate;
    }

    public static Date returnPrevOneMonthDateDate(final Date fromDate) {
        Date prevOneMonthDateDate = null;
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(fromDate);
            cal.add(Calendar.MONTH, -1);
            Date prevMonDt = cal.getTime();
            String prevOneMonthDate = mysqldf.format(prevMonDt);
            prevOneMonthDateDate = mysqldf.parse(prevOneMonthDate);
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
        }
        return prevOneMonthDateDate;
    }

    public static String returnPrevThreeMonthDate(final Date fromDate) {
        String prevThreeMonthDate = null;
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(fromDate);
            cal.add(Calendar.MONTH, -3);
            Date prev3MonDt = cal.getTime();
            prevThreeMonthDate = mysqldf.format(prev3MonDt);
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
        }
        return prevThreeMonthDate;
    }

    public static Date returnPrevThreeMonthDateDate(final Date fromDate) {
        Date prevThreeMonthDateDate = null;
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(fromDate);
            cal.add(Calendar.MONTH, -3);
            Date prev3MonDt = cal.getTime();
            String prevThreeMonthDate = mysqldf.format(prev3MonDt);
            prevThreeMonthDateDate = mysqldf.parse(prevThreeMonthDate);
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
        }
        return prevThreeMonthDateDate;
    }

    public static String returnMonthsDate(int month) {
        String oneYearPrevDate = null;
        try {
            if (0 == month) {
                month = 1;
            }
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MONTH, -month);
            Date prvYrDt = cal.getTime();
            oneYearPrevDate = mysqldf.format(prvYrDt);
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
        }
        return oneYearPrevDate;
    }

    public static String returnOneYearBackDate(final Date fromDate) {
        String oneYearPrevDate = null;
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(fromDate);
            cal.add(Calendar.YEAR, -1);
            Date prvYrDt = cal.getTime();
            oneYearPrevDate = mysqldf.format(prvYrDt);
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
        }
        return oneYearPrevDate;
    }

    public static String returnOneMonthBackDate() {
        String oneMonthBackDate = null;
        try {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MONTH, -1);
            Date prvYrDt = cal.getTime();
            oneMonthBackDate = mysqldf.format(prvYrDt);
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
        }
        return oneMonthBackDate;
    }

    public static String returnOneMonthBackDate(final Date fromDate) {
        String oneMonthBackDate = null;
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(fromDate);
            cal.add(Calendar.MONTH, -1);
            Date prvYrDt = cal.getTime();
            oneMonthBackDate = mysqldf.format(prvYrDt);
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
        }
        return oneMonthBackDate;
    }

    public static String returnOneBackDate(String Date) {
        String oneDayBackDate = null;
        try {
            Date dt = mysqldf.parse(Date);
            Calendar cal = Calendar.getInstance();
            cal.setTime(dt);
            cal.add(Calendar.DATE, -1);
            Date prvOneDt = cal.getTime();
            oneDayBackDate = mysqldf.format(prvOneDt);
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
        }
        return oneDayBackDate;
    }

    public static String returnOneEightyDaysBackDate() {
        String oneDayBackDate = null;
        try {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -180);
            Date prvOneDt = cal.getTime();
            oneDayBackDate = mysqldf.format(prvOneDt);
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
        }
        return oneDayBackDate;
    }

    public static Date returnOneEightyDaysBackDateDate() {
        Date oneEightyDaysBackDate = null;
        try {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -180);
            Date prvOneDt = cal.getTime();
            String oneDayBackDate = mysqldf.format(prvOneDt);
            oneEightyDaysBackDate = mysqldf.parse(oneDayBackDate);
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
        }
        return oneEightyDaysBackDate;
    }

    public static String returnSixMonthBackDate(final Date fromDate) {
        String oneMonthBackDate = null;
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(fromDate);
            cal.add(Calendar.MONTH, -6);
            Date prvYrDt = cal.getTime();
            oneMonthBackDate = mysqldf.format(prvYrDt);
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
        }
        return oneMonthBackDate;
    }

    public static String returnTwoYearBackDate() {
        String oneYearPrevDate = null;
        try {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.YEAR, -2);
            Date prvYrDt = cal.getTime();
            oneYearPrevDate = mysqldf.format(prvYrDt);
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
        }
        return oneYearPrevDate;
    }

    public static List<String> returnOrgFinancialStartEndDate(Organization org) {
        List<String> listOfFinYeardate = new ArrayList<String>();
        try {
            String finStartDate = null;
            String finStDt = null;
            StringBuilder startYear = null;
            String finEndDate = null;
            String finEndDt = null;
            StringBuilder endYear = null;
            int currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
            int finStartMonth = 4;
            int finEndMonth = 3;
            if (org.getFinancialStartDate() != null) {
                finStartMonth = org.getFinancialStartDate().getMonth() + 1;
            }
            if (org.getFinancialEndDate() != null) {
                finEndMonth = org.getFinancialEndDate().getMonth() + 1;
            }
            if (currentMonth < finStartMonth) {
                startYear = new StringBuilder(String.valueOf(Calendar.getInstance().get(Calendar.YEAR) - 1));
                endYear = new StringBuilder(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
            } else {
                startYear = new StringBuilder(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
                endYear = new StringBuilder(String.valueOf(Calendar.getInstance().get(Calendar.YEAR) + 1));
            }
            if (org.getFinancialStartDate() != null && !org.getFinancialStartDate().equals("")) {
                finStDt = StaticController.idosmdtdf.format(org.getFinancialStartDate()) + "," + startYear;
                finStartDate = StaticController.mysqldf.format(StaticController.idosdf.parse(finStDt));
            } else {
                finStDt = "Apr 01" + "," + startYear;
                finStartDate = StaticController.mysqldf.format(StaticController.idosdf.parse(finStDt));
            }
            if (org.getFinancialEndDate() != null && !org.getFinancialEndDate().equals("")) {
                finEndDt = StaticController.idosmdtdf.format(org.getFinancialEndDate()) + "," + endYear;
                finEndDate = StaticController.mysqldf.format(StaticController.idosdf.parse(finEndDt));
            } else {
                finEndDt = "Mar 31" + "," + endYear;
                finEndDate = StaticController.mysqldf.format(StaticController.idosdf.parse(finEndDt));
            }
            listOfFinYeardate.add(finStartDate);
            listOfFinYeardate.add(finEndDate);
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
        }
        return listOfFinYeardate;
    }

    public synchronized static String[] getFinancialDate(Users user) {
        String[] dateArr = new String[2];
        try {
            Organization org = user.getOrganization();
            int currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
            int finStartMonth = 4;
            int finEndMonth = 3;
            String finStartDate = null;
            String finStDt = null;
            StringBuilder startYear = null;
            String finEndDate = null;
            String finEndDt = null;
            StringBuilder endYear = null;
            if (org.getFinancialStartDate() != null) {
                finStartMonth = org.getFinancialStartDate().getMonth() + 1;
            }
            if (org.getFinancialEndDate() != null) {
                finEndMonth = org.getFinancialEndDate().getMonth() + 1;
            }
            if (currentMonth < finStartMonth) {
                startYear = new StringBuilder(String.valueOf(Calendar.getInstance().get(Calendar.YEAR) - 1));
                endYear = new StringBuilder(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
            } else {
                startYear = new StringBuilder(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
                endYear = new StringBuilder(String.valueOf(Calendar.getInstance().get(Calendar.YEAR) + 1));
            }
            if (org.getFinancialStartDate() != null && !org.getFinancialStartDate().equals("")) {
                finStDt = StaticController.idosmdtdf.format(org.getFinancialStartDate()) + "," + startYear;
                finStartDate = StaticController.mysqldf.format(StaticController.idosdf.parse(finStDt));
            } else {
                finStDt = "Apr 01" + "," + startYear;
                finStartDate = StaticController.mysqldf.format(StaticController.idosdf.parse(finStDt));
            }
            if (org.getFinancialStartDate() != null && !org.getFinancialStartDate().equals("")) {
                finStDt = StaticController.idosmdtdf.format(org.getFinancialStartDate()) + "," + startYear;
                finStartDate = StaticController.mysqldf.format(StaticController.idosdf.parse(finStDt));
            } else {
                finStDt = "Apr 01" + "," + startYear;
                finStartDate = StaticController.mysqldf.format(StaticController.idosdf.parse(finStDt));
            }
            if (org.getFinancialEndDate() != null && !org.getFinancialEndDate().equals("")) {
                finEndDt = StaticController.idosmdtdf.format(org.getFinancialEndDate()) + "," + endYear;
                finEndDate = StaticController.mysqldf.format(StaticController.idosdf.parse(finEndDt));
            } else {
                finEndDt = "Mar 31" + "," + endYear;
                finEndDate = StaticController.mysqldf.format(StaticController.idosdf.parse(finEndDt));
            }
            dateArr[0] = finStartDate;
            dateArr[1] = finEndDate;
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
        }
        return dateArr;
    }

    public static int getDateMonth(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int month = cal.get(Calendar.MONTH);
        return month;
    }

    public static int getDayOfMonth(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        return day;
    }

    public static int getDateYear(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int year = cal.get(Calendar.YEAR);
        return year;
    }

    public static int isDateInFinancialYearRange(Date finStartDate, Date finEndDate, Date transactionDt) throws Exception {
        int returnValue = 0;
        if (finStartDate == null || finEndDate == null || transactionDt == null) {
            return returnValue;
        }
        int finStartDay = DateUtil.getDayOfMonth(finStartDate);
        int finEndDay = DateUtil.getDayOfMonth(finEndDate);
        int finStartMonth = DateUtil.getDateMonth(finStartDate) + 1;
        int finEndMonth = DateUtil.getDateMonth(finEndDate) + 1;
        int currentY = DateUtil.getDateYear(Calendar.getInstance().getTime());
        int lastY = currentY - 1;
        int nextY = currentY + 1;
        Date startDate = mysqldf.parse(lastY + "-" + finStartMonth + "-" + finStartDay);
        Date endDate = mysqldf.parse(nextY + "-" + finEndMonth + "-" + finEndDay);
        if (transactionDt.getTime() >= startDate.getTime() && transactionDt.getTime() <= endDate.getTime()) {
            returnValue = 1;
        }
        return returnValue;
    }

    public static int isDateInFinancialOneYearRange(Date finStartDate, Date finEndDate, Date transactionDt) throws Exception {
        int returnValue = 0;
        if (finStartDate == null || finEndDate == null || transactionDt == null) {
            return returnValue;
        }
        int finStartDay = DateUtil.getDayOfMonth(finStartDate);
        int finEndDay = DateUtil.getDayOfMonth(finEndDate);
        int finStartMonth = DateUtil.getDateMonth(finStartDate) + 1;
        int finEndMonth = DateUtil.getDateMonth(finEndDate) + 1;
        int currentY = DateUtil.getDateYear(Calendar.getInstance().getTime());
        int lastY = currentY - 1;
        int nextY = currentY;
        Date startDate = mysqldf.parse(lastY + "-" + finStartMonth + "-" + finStartDay);
        Date endDate = mysqldf.parse(nextY + "-" + finEndMonth + "-" + finEndDay);
        if (transactionDt.getTime() >= startDate.getTime() && transactionDt.getTime() <= endDate.getTime()) {
            returnValue = 1;
        }
        return returnValue;
    }

    public static String getCurrentFinacialStartDate(java.util.Date startDate) throws IDOSException {
        String finStartDate = null;
        try {
            int currentYear = Calendar.getInstance().get(Calendar.YEAR);
            int currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
            int nextYear = currentYear + 1;
            int finStartMonth = 4;
            int finEndMonth = 3;
            String finStDt = null;
            StringBuilder startYear = null;
            if (startDate != null) {
                finStartMonth = startDate.getMonth() + 1;
            }
            if (startDate != null) {
                finEndMonth = startDate.getMonth() + 1;
            }
            if (currentMonth < finStartMonth) {
                startYear = new StringBuilder(String.valueOf(Calendar.getInstance().get(Calendar.YEAR) - 1));
            } else {
                startYear = new StringBuilder(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
            }
            if (startDate != null && !startDate.equals("")) {
                finStDt = StaticController.idosmdtdf.format(startDate) + "," + startYear;
                finStartDate = mysqldf.format(idosdf.parse(finStDt));
            } else {
                finStDt = "Apr 01" + "," + startYear;
                finStartDate = mysqldf.format(mysqldf.parse(mysqldf.format(idosdf.parse(finStDt))));
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, ex.getMessage());
            throw new IDOSException(IdosConstants.DATA_FORMAT_ERRCODE, IdosConstants.DATA_FORMAT_EXCEPTION, "Wrong date format", ex.getMessage());
        }
        return finStartDate;
    }

    public static Date convertStringToDate(String dateStr, SimpleDateFormat format) throws IDOSException {
        try {
            return format.parse(dateStr);
        } catch (Exception ex) {
            log.log(Level.SEVERE, ex.getMessage());
            throw new IDOSException(IdosConstants.DATA_FORMAT_ERRCODE, IdosConstants.DATA_FORMAT_EXCEPTION, "Wrong date format", ex.getMessage());
        }
    }

    public static String getCurrentFinacialEndDate(java.util.Date endDate) throws ParseException {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
        int nextYear = currentYear + 1;
        int finStartMonth = 4;
        int finEndMonth = 3;

        String finEndDate = null;
        String finEndDt = null;
        StringBuilder endYear = null;
        if (endDate != null) {
            finEndMonth = endDate.getMonth() + 1;
        }
        if (currentMonth < finStartMonth) {
            endYear = new StringBuilder(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
        } else {
            endYear = new StringBuilder(String.valueOf(Calendar.getInstance().get(Calendar.YEAR) + 1));
        }

        if (endDate != null && !endDate.equals("")) {
            finEndDt = IdosConstants.IDOSMDTDF.format(endDate) + "," + endYear;
            finEndDate = mysqldf.format(idosdf.parse(finEndDt));
        } else {
            finEndDt = "Mar 31" + "," + endYear;
            finEndDate = mysqldf.format(idosdf.parse(finEndDt));
        }
        return finEndDate;
    }

    public static String getMonthName(Date date) {
        if (date == null) {
            return null;
        }
        return monthtext.format(date);
    }

    public static String getMonthNumber(Date date) {
        if (date == null) {
            return null;
        }
        return monthnumber.format(date);
    }

    public static String getYear(Date date) {
        if (date == null) {
            return null;
        }
        return yearnumber.format(date);
    }

    public static boolean isBackDate(Date date) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(Calendar.getInstance().getTime());
        int month = cal1.get(Calendar.MONTH);
        int day = cal1.get(Calendar.DAY_OF_MONTH);
        int year = cal1.get(Calendar.YEAR);

        int month1 = cal2.get(Calendar.MONTH);
        int day1 = cal2.get(Calendar.DAY_OF_MONTH);
        int year1 = cal2.get(Calendar.YEAR);
        if (day == day1 && month == month1 && year == year1) {
            return false;
        } else {
            return true;
        }
    }

    public static String getMonthName(int monthNumber) {
        String name = null;
        if (monthNumber == 1) {
            name = "January";
        } else if (monthNumber == 2) {
            name = "February";
        } else if (monthNumber == 3) {
            name = "March";
        } else if (monthNumber == 4) {
            name = "April";
        } else if (monthNumber == 5) {
            name = "May";
        } else if (monthNumber == 6) {
            name = "June";
        } else if (monthNumber == 7) {
            name = "July";
        } else if (monthNumber == 8) {
            name = "August";
        } else if (monthNumber == 9) {
            name = "September";
        } else if (monthNumber == 10) {
            name = "October";
        } else if (monthNumber == 11) {
            name = "November";
        } else if (monthNumber == 12) {
            name = "December";
        } else {
            name = "";
        }
        return name;
    }
}
