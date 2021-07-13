/*
 * The MIT License
 *
 * Copyright 2021 buddhika.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package lk.gov.health.phsp.bean;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.TemporalType;
import lk.gov.health.phsp.ejb.CovidDataHolder;
import lk.gov.health.phsp.entity.Area;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.entity.Item;
import lk.gov.health.phsp.enums.AreaType;
import lk.gov.health.phsp.enums.EncounterType;
import lk.gov.health.phsp.enums.InstitutionType;
import lk.gov.health.phsp.facade.ClientEncounterComponentItemFacade;
import lk.gov.health.phsp.facade.ClientFacade;
import lk.gov.health.phsp.facade.EncounterFacade;
import lk.gov.health.phsp.facade.NumbersFacade;

/**
 *
 * @author buddhika
 */
@Named
@ApplicationScoped
public class DashboardApplicationController {

    @EJB
    ClientFacade clientFacade;
    @EJB
    EncounterFacade encounterFacade;
    @EJB
    ClientEncounterComponentItemFacade clientEncounterComponentItemFacade;
    @EJB
    NumbersFacade numbersFacade;

    @Inject
    ItemApplicationController itemApplicationController;
    @Inject
    InstitutionApplicationController institutionApplicationController;
    @Inject
    AreaApplicationController areaApplicationController;
    @Inject
    CovidDataHolder covidDataHolder;

    private Long todayPcr;
    private Long todayRat;
    private Long todayPositivePcr;
    private Long todayPositiveRat;
    private Long yesterdayPcr;
    private Long yesterdayRat;
    private Long yesterdayPositivePcr;
    private Long yesterdayPositiveRat;
    private Long yesterdayTests;
    private Long todaysTests;

    Item testType;
    Item orderingCat;
    Item pcr;
    Item rat;

    Boolean dashboardPrepared;

    /**
     * Creates a new instance of DashboardController
     */
    public DashboardApplicationController() {
    }

    @PostConstruct
    public void updateDashboard() {
        Calendar c = Calendar.getInstance();
        Date now = c.getTime();
        Date todayStart = CommonController.startOfTheDate();

        c.add(Calendar.DATE, -1);

        Date yesterdayStart = CommonController.startOfTheDate(c.getTime());
        Date yesterdayEnd = CommonController.endOfTheDate(c.getTime());

        todayPcr = getOrderCount(null, todayStart, now,
                itemApplicationController.getPcr(), null, null, null);
        todayRat = getOrderCount(null, todayStart, now,
                itemApplicationController.getRat(), null, null, null);
        yesterdayPcr = getOrderCount(null, yesterdayStart, yesterdayEnd,
                itemApplicationController.getPcr(), null, null, null);
        yesterdayRat = getOrderCount(null, yesterdayStart, yesterdayEnd,
                itemApplicationController.getRat(), null, null, null);

        todayPositivePcr = getConfirmedCount(null,
                todayStart,
                now,
                itemApplicationController.getPcr(),
                null,
                itemApplicationController.getPcrPositive(),
                null);
        todayPositiveRat = getConfirmedCount(null,
                todayStart,
                now,
                itemApplicationController.getRat(),
                null,
                itemApplicationController.getPcrPositive(),
                null);

        yesterdayPositivePcr = getConfirmedCount(null,
                yesterdayStart,
                yesterdayEnd,
                itemApplicationController.getPcr(),
                null,
                itemApplicationController.getPcrPositive(),
                null);
        yesterdayPositiveRat = getConfirmedCount(null,
                yesterdayStart,
                yesterdayEnd,
                itemApplicationController.getRat(),
                null,
                itemApplicationController.getPcrPositive(),
                null);

    }

    public Long getOrderCount(Institution ins,
            Date fromDate,
            Date toDate,
            Item testType,
            Item orderingCategory,
            Item result,
            Institution lab) {
        Map m = new HashMap();
        String j = "select count(c) "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);
        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);

        if (ins != null) {
            j += " and c.institution=:ins ";
            m.put("ins", ins);
        }

        j += " and c.createdAt between :fd and :td ";
        m.put("fd", fromDate);
        m.put("td", toDate);

        if (testType != null) {
            j += " and c.pcrTestType=:tt ";
            m.put("tt", testType);
        }
        if (orderingCategory != null) {
            j += " and c.pcrOrderingCategory=:oc ";
            m.put("oc", orderingCategory);
        }
        if (result != null) {
            j += " and c.pcrResult=:result ";
            m.put("result", result);
        }
        if (lab != null) {
            j += " and c.referalInstitution=:ri ";
            m.put("ri", lab);
        }
        return encounterFacade.findLongByJpql(j, m, TemporalType.TIMESTAMP);
    }

    public Long getOrderCountArea(Area area,
            Date fromDate,
            Date toDate,
            Item testType,
            Item orderingCategory,
            Item result,
            Institution lab) {
        Map m = new HashMap();
        String j = "select count(c) "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);
        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);

        if (area != null) {
            if (area.getType() == AreaType.RdhsAra) {
                j += " and c.institution.rdhsArea=:area ";
                m.put("area", area);
            } else if (area.getType() == AreaType.Province) {
                j += " and c.institution.pdhsArea=:area ";
                m.put("area", area);
            }
        }

        j += " and c.createdAt between :fd and :td ";
        m.put("fd", fromDate);
        m.put("td", toDate);

        if (testType != null) {
            j += " and c.pcrTestType=:tt ";
            m.put("tt", testType);
        }
        if (orderingCategory != null) {
            j += " and c.pcrOrderingCategory=:oc ";
            m.put("oc", orderingCategory);
        }
        if (result != null) {
            j += " and c.pcrResult=:result ";
            m.put("result", result);
        }
        if (lab != null) {
            j += " and c.referalInstitution=:ri ";
            m.put("ri", lab);
        }
        return encounterFacade.findLongByJpql(j, m, TemporalType.TIMESTAMP);
    }

    public Long getConfirmedCount(Institution ins,
            Date fromDate,
            Date toDate,
            Item testType,
            Item orderingCategory,
            Item result,
            Institution lab) {
        Map m = new HashMap();
        String j = "select count(c) "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);
        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);

        if (ins != null) {
            j += " and c.institution=:ins ";
            m.put("ins", ins);
        }

        j += " and c.resultConfirmedAt between :fd and :td ";
        m.put("fd", fromDate);
        m.put("td", toDate);

        if (testType != null) {
            j += " and c.pcrTestType=:tt ";
            m.put("tt", testType);
        }
        if (orderingCategory != null) {
            j += " and c.pcrOrderingCategory=:oc ";
            m.put("oc", orderingCategory);
        }
        if (result != null) {
            j += " and c.pcrResult=:result ";
            m.put("result", result);
        }
        if (lab != null) {
            j += " and c.referalInstitution=:ri ";
            m.put("ri", lab);
        }
        System.out.println("j = " + j);
        System.out.println("m = " + m);
        return encounterFacade.findLongByJpql(j, m, TemporalType.TIMESTAMP);
    }

    public Long getConfirmedCountArea(Area area,
            Date fromDate,
            Date toDate,
            Item testType,
            Item orderingCategory,
            Item result,
            Institution lab) {
        Map m = new HashMap();
        String j = "select count(c) "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);
        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);

        if (area != null) {
            if (area.getType() == AreaType.RdhsAra) {
                j += " and c.institution.rdhsArea=:area ";
                m.put("area", area);
            } else if (area.getType() == AreaType.Province) {
                j += " and c.institution.pdhsArea=:area ";
                m.put("area", area);
            }
        }

        j += " and c.resultConfirmedAt between :fd and :td ";
        m.put("fd", fromDate);
        m.put("td", toDate);

        if (testType != null) {
            j += " and c.pcrTestType=:tt ";
            m.put("tt", testType);
        }
        if (orderingCategory != null) {
            j += " and c.pcrOrderingCategory=:oc ";
            m.put("oc", orderingCategory);
        }
        if (result != null) {
            j += " and c.pcrResult=:result ";
            m.put("result", result);
        }
        if (lab != null) {
            j += " and c.referalInstitution=:ri ";
            m.put("ri", lab);
        }
        System.out.println("j = " + j);
        System.out.println("m = " + m);
        return encounterFacade.findLongByJpql(j, m, TemporalType.TIMESTAMP);
    }

    public Long getPositivePcr(Date fd, Date td) {
        String j = "select count(e "
                + " from Encounter e "
                + " where (e.retired is null or e.retired=false) "
                + " and e.pcrTestType=:pcr "
                + " and e.resultConfirmedAt between :fd and :td "
                + " and e.pcrResult=:pos ";
        Map m;
        m = new HashMap();
        m.put("fd", fd);
        m.put("td", td);
        m.put("pos", itemApplicationController.getPcrPositive());
        m.put("pcr", itemApplicationController.getPcr());
        return encounterFacade.countByJpql(j, m, TemporalType.TIMESTAMP);
    }

    public Boolean getDashboardPrepared() {
        if (dashboardPrepared == null) {
            updateDashboard();
            dashboardPrepared = true;
        }
        return dashboardPrepared;
    }

    public Long getTodayPcr() {
        if (todayPcr == null) {
            updateDashboard();
        }
        return todayPcr;
    }

    public Long getTodayRat() {
        if (todayRat == null) {
            updateDashboard();
        }
        return todayRat;
    }

    public Long getTodayPositivePcr() {
        if (todayPositivePcr == null) {
            updateDashboard();
        }
        return todayPositivePcr;
    }

    public Long getTodayPositiveRat() {
        if (todayPositiveRat == null) {
            updateDashboard();
        }
        return todayPositiveRat;
    }

    public Long getYesterdayPcr() {
        if (yesterdayPcr == null) {
            updateDashboard();
        }
        return yesterdayPcr;
    }

    public void setYesterdayPcr(Long yesterdayPcr) {
        this.yesterdayPcr = yesterdayPcr;
    }

    public Long getYesterdayRat() {
        if (yesterdayRat == null) {
            updateDashboard();
        }
        return yesterdayRat;
    }

    public void setYesterdayRat(Long yesterdayRat) {
        this.yesterdayRat = yesterdayRat;
    }

    public Long getYesterdayPositivePcr() {
        if (yesterdayPositivePcr == null) {
            updateDashboard();
        }
        return yesterdayPositivePcr;
    }

    public void setYesterdayPositivePcr(Long yesterdayPositivePcr) {
        this.yesterdayPositivePcr = yesterdayPositivePcr;
    }

    public Long getYesterdayPositiveRat() {
        if (yesterdayPositiveRat == null) {
            updateDashboard();
        }
        return yesterdayPositiveRat;
    }

    public void setYesterdayPositiveRat(Long yesterdayPositiveRat) {
        this.yesterdayPositiveRat = yesterdayPositiveRat;
    }

    public Long getYesterdayTests() {
        if (getYesterdayPcr() != null && getYesterdayRat() != null) {
            yesterdayTests = getYesterdayPcr() + getYesterdayRat();
        } else if (getYesterdayPcr() != null) {
            yesterdayTests = getYesterdayPcr();
        } else if (getYesterdayRat() != null) {
            yesterdayTests = getYesterdayRat();
        } else {
            yesterdayTests = 0l;
        }
        return yesterdayTests;
    }

    public Long getTodaysTests() {
        if (getTodayPcr() != null && getTodayRat() != null) {
            todaysTests = getTodayPcr() + getTodayRat();
        } else if (getTodayPcr() != null) {
            todaysTests = getTodayPcr();
        } else if (getTodayRat() != null) {
            todaysTests = getTodayRat();
        } else {
            todaysTests = 0l;
        }
        return todaysTests;
    }

    public void setTodaysTests(Long todaysTests) {
        this.todaysTests = todaysTests;
    }

}
