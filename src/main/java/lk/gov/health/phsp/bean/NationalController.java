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

// <editor-fold defaultstate="collapsed" desc="Import">
import java.io.Serializable;
import java.text.DecimalFormat;

import lk.gov.health.phsp.entity.Client;
import lk.gov.health.phsp.bean.util.JsfUtil;
import lk.gov.health.phsp.facade.ClientFacade;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import lk.gov.health.phsp.entity.Area;
import lk.gov.health.phsp.entity.Encounter;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.enums.EncounterType;
import lk.gov.health.phsp.facade.EncounterFacade;
import lk.gov.health.phsp.facade.SmsFacade;
import javax.inject.Named;
import javax.persistence.TemporalType;
import lk.gov.health.phsp.entity.ClientEncounterComponentItem;
import lk.gov.health.phsp.entity.Item;
import lk.gov.health.phsp.entity.WebUser;
import lk.gov.health.phsp.enums.AreaType;
import lk.gov.health.phsp.enums.InstitutionType;
import lk.gov.health.phsp.enums.InvestigationFilterType;
import lk.gov.health.phsp.facade.ClientEncounterComponentItemFacade;
import lk.gov.health.phsp.facade.InstitutionFacade;
import lk.gov.health.phsp.pojcs.InstitutionCount;
import lk.gov.health.phsp.pojcs.InstitutionPeformance;
import lk.gov.health.phsp.pojcs.InstitutionTypeCount;
// </editor-fold>

/**
 *
 * @author buddhika
 */
@Named
@SessionScoped
public class NationalController implements Serializable {
// <editor-fold defaultstate="collapsed" desc="EJBs">

    @EJB
    private ClientFacade clientFacade;
    @EJB
    private EncounterFacade encounterFacade;
    @EJB
    ClientEncounterComponentItemFacade ceciFacade;
    @EJB
    private SmsFacade smsFacade;
    @EJB
    InstitutionFacade institutionFacade;

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Controllers">
    @Inject
    ClientApplicationController clientApplicationController;
    @Inject
    ApplicationController applicationController;
    @Inject
    ClientController clientController;
    @Inject
    private AreaApplicationController areaApplicationController;
    @Inject
    SessionController sessionController;
    @Inject
    private InstitutionApplicationController institutionApplicationController;
    @Inject
    private WebUserController webUserController;
    @Inject
    private ItemApplicationController itemApplicationController;
    @Inject
    private InstitutionController institutionController;
    @Inject
    private CommonController commonController;
    @Inject
    private AreaController areaController;
    @Inject
    DashboardApplicationController dashboardApplicationController;
// </editor-fold>

// <editor-fold defaultstate="collapsed" desc="Variables">
    private Boolean nicExistsForPcr;
    private Boolean nicExistsForRat;
    private Encounter rat;
    private Encounter pcr;
    private Encounter covidCase;
    private Encounter test;
    private Encounter deleting;

    private String searchingName;
    private String filter;

    private WebUser assignee;

    private List<Encounter> tests;
    private List<ClientEncounterComponentItem> cecItems;
    private List<ClientEncounterComponentItem> selectedCecis;
    private List<Encounter> selectedToAssign;
    private Date fromDate;
    private Date toDate;

    private Item orderingCategory;
    private Item result;
    private Item testType;
    private Item managementType;
    private Institution lab;
    private Institution mohOrHospital;

    private List<Institution> regionalMohsAndHospitals;
    private List<InstitutionCount> institutionCounts;
    private List<InstitutionTypeCount> institutionTypeCounts;
    private List<InstitutionPeformance> institutionPeformances;
    private List<InstitutionPeformance> institutionPeformancesFiltered;
    private InstitutionPeformance institutionPeformancesSummery;

    private List<InstitutionCount> labSummariesToReceive;

    private Area district;
    private Area mohArea;
    private Area pdhs;
    private Area rdhs;
    private InstitutionType institutionType;

    private Institution institution;
    private Institution referingInstitution;
    private Institution dispatchingLab;
    private Institution divertingLab;

    private List<InstitutionCount> awaitingDispatch;
    private List<InstitutionCount> awaitingReceipt;
    private List<InstitutionCount> awaitingResults;
    private List<InstitutionCount> resultsAvailable;

    private List<Encounter> listedToDispatch;
    private List<Encounter> listedToDivert;
    private List<Encounter> selectedToDivert;
    private List<Encounter> selectedToDispatch;

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Constructors">
    public NationalController() {
    }
// </editor-fold>

// <editor-fold defaultstate="collapsed" desc="Functions">


    public String searchByName() {
        if (searchingName == null && searchingName.trim().equals("")) {
            JsfUtil.addErrorMessage("Please enter a name to search");
            return "";
        }

        if (searchingName.length() < 5) {
            JsfUtil.addErrorMessage("Please enter at least 4 characters to serach");
            return "";
        }

        Map m = new HashMap();
        String jpql = "select c "
                + " from Client c "
                + " where c.retired=:ret "
                + " and lower(c.person.name) like :name";

        if (district != null) {
            jpql += " and c.person.district=:dis ";
            m.put("dis", district);
        }

        jpql += " order by c.person.name";

        m.put("ret", false);
        m.put("name", "%" + searchingName.toLowerCase() + "%");

        List<Client> tmpClients = clientFacade.findByJpql(jpql, m, 100);

        if (tmpClients == null || tmpClients.isEmpty()) {
            JsfUtil.addErrorMessage("No matches found");
            return "";
        }

        if (tmpClients.size() == 1) {
            clientController.setSelected(tmpClients.get(0));
            return clientController.toClientProfile();
        } else {
            if (tmpClients.size() > 99) {
                JsfUtil.addErrorMessage("Only the first 100 records are shown. Please increase the length of search keyword.");
            }
            clientController.setSelectedClients(tmpClients);
            return clientController.toSelectClient();
        }

    }

    public String toSummaryByOrderedInstitutionVsLabToReceive() {
        String j = "select new lk.gov.health.phsp.pojcs.InstitutionCount(c.institution, c.referalInstitution, count(c)) "
                + " from Encounter c "
                + " where c.retired=false "
                + " and c.encounterType=:type "
                + " and (c.encounterDate > :fd and c.encounterDate<:td) "
                + " and (c.receivedAtLab is null or c.receivedAtLab=:ral) "
                + " and c.sentToLab=:sl "
                + " group by c.institution, c.referalInstitution";
        Map m = new HashMap();
        m.put("type", EncounterType.Test_Enrollment);
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        m.put("ral", false);
        m.put("sl", true);
        labSummariesToReceive = new ArrayList<>();
        List<Object> obs = encounterFacade.findObjectByJpql(j, m, TemporalType.TIMESTAMP);
        // // System.out.println("obs = " + obs.size());
        for (Object o : obs) {
            if (o instanceof InstitutionCount) {
                labSummariesToReceive.add((InstitutionCount) o);
            }
        }
        return "/national/summary_lab_vs_ordered_to_receive";
    }

    public void prepareSummaryByOrderedInstitution() {
        String j = "select new lk.gov.health.phsp.pojcs.InstitutionCount(c.institution, count(c)) "
                + " from Encounter c "
                + " where c.retired=false "
                + " and c.encounterType=:type "
                + " and c.encounterDate between :fd and :td ";

        Map m = new HashMap();
        m.put("type", EncounterType.Test_Enrollment);
        m.put("fd", getFromDate());
        m.put("td", getToDate());

        if (testType != null) {
            j += " and c.pcrTestType=:tt ";
            m.put("tt", testType);
        }

        j += " group by c.institution";
        institutionCounts = new ArrayList<>();
        List<Object> obs = encounterFacade.findObjectByJpql(j, m, TemporalType.DATE);
        // // System.out.println("obs = " + obs.size());
        Long c = 1l;
        for (Object o : obs) {
            if (o instanceof InstitutionCount) {
                InstitutionCount ic = (InstitutionCount) o;
                ic.setId(c);
                c++;
                institutionCounts.add(ic);
            }
        }
    }

    public void prepareDispatchSummery() {
        Map m = new HashMap();
        String j = "select new lk.gov.health.phsp.pojcs.InstitutionCount(c.institution, count(c)) "
                + " from Encounter c "
                + " where (c.retired=:ret or c.retired is null) "
                + " and c.encounterType=:type "
                + " and c.encounterDate between :fd and :td ";
        if (testType != null) {
            j += " and c.pcrTestType=:tt ";
            m.put("tt", testType);
        }
        j += " and (c.sentToLab is null or c.sentToLab=:sl or c.referalInstitution is null) "
                + " group by c.institution "
                + " order by c.institution.name";
        m.put("ret", false);
        m.put("type", EncounterType.Test_Enrollment);
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        m.put("sl", false);
        List<Object> os = encounterFacade.findByJpql(j, m, TemporalType.TIMESTAMP);
        awaitingDispatch = new ArrayList<>();
        Long c = 0l;
        for (Object o : os) {
            if (o instanceof InstitutionCount) {
                InstitutionCount ic = (InstitutionCount) o;
                ic.setId(c);
                c++;
                awaitingDispatch.add(ic);
            }
        }
    }

//  This will return the number of test counts by ward
    public String toCountofTestsByWards() {
        Map m = new HashMap();

        String j = "select new lk.gov.health.phsp.pojcs.InstitutionCount(c.institution, count(c))   "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);

        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);

        if (this.filter == null) {
            this.filter = "RESULTSAT";
        }

        switch (this.filter.toUpperCase()) {
            case "CREATEDAT":
                j += " and (c.createdAt > :fd and c.createdAt < :td) ";
                break;
            case "SAMPLEDAT":
                j += " and (c.sampledAt > :fd and c.sampledAt < :td) ";
                break;
            case "RESULTSAT":
                j += " and (c.resultConfirmedAt > :fd and c.resultConfirmedAt < :td) ";
            default:
                j += " and (c.createdAt > :fd and c.createdAt < :td) ";
                break;
        }
        m.put("fd", getFromDate());
        m.put("td", getToDate());

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

        j += " and c.institution.institutionType=:it ";
        m.put("it", institutionType.Ward);

        this.setInstitutionType(institutionType.Ward);

        j += " group by c.institution"
                + " order by count(c) desc ";


        institutionCounts = new ArrayList<>();

        List<Object> objCounts = encounterFacade.findAggregates(j, m, TemporalType.TIMESTAMP);
        if (objCounts == null || objCounts.isEmpty()) {
            return "/national/count_of_tests_by_ordered_institution";
        }
        for (Object o : objCounts) {
            if (o instanceof InstitutionCount) {
                InstitutionCount ic = (InstitutionCount) o;
                institutionCounts.add(ic);
            }
        }

        return "/national/count_of_tests_by_ordered_institution";
    }

    public String toCountOfTestsByOrderedInstitution() {
        Map m = new HashMap();

        String j = "select new lk.gov.health.phsp.pojcs.InstitutionCount(c.institution, count(c))   "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);

        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);

        if (this.filter == null) {
            this.filter = "RESULTSAT";
        }

        switch (this.filter.toUpperCase()) {
            case "CREATEDAT":
                j += " and (c.createdAt > :fd and c.createdAt < :td) ";
                break;
            case "SAMPLEDAT":
                j += " and (c.sampledAt > :fd and c.sampledAt< :td) ";
                break;
            case "RESULTSAT":
                j += " and (c.resultConfirmedAt > :fd and c.resultConfirmedAt < :td) ";
                break;
            default:
                j += " and (c.createdAt > :fd and c.createdAt < :td) ";
                break;
        }

        m.put("fd", getFromDate());
        m.put("td", getToDate());

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
        if (institutionType != null) {
            j += " and c.institution.institutionType=:it ";
            m.put("it", institutionType);
        }

        j += " group by c.institution"
                + " order by count(c) desc ";


        institutionCounts = new ArrayList<>();

        List<Object> objCounts = encounterFacade.findAggregates(j, m, TemporalType.TIMESTAMP);
        if (objCounts == null || objCounts.isEmpty()) {
            return "/national/count_of_tests_by_ordered_institution";
        }
        for (Object o : objCounts) {
            if (o instanceof InstitutionCount) {
                InstitutionCount ic = (InstitutionCount) o;
                institutionCounts.add(ic);
            }
        }

        return "/national/count_of_tests_by_ordered_institution";
    }

    public String toCountOfTestsByOrderedInstitutionWithoutRdhs() {
        Map m = new HashMap();

        String j = "select new lk.gov.health.phsp.pojcs.InstitutionCount(c.institution, count(c))   "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);

        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);

        j += " and c.institution.rdhsArea is null ";

        j += " and (c.createdAt > :fd and c.createdAt < :td) ";
        m.put("fd", getFromDate());

        m.put("td", getToDate());

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

        j += " group by c.institution"
                + " order by count(c) desc ";


        institutionCounts = new ArrayList<>();

        List<Object> objCounts = encounterFacade.findAggregates(j, m, TemporalType.TIMESTAMP);
        if (objCounts == null || objCounts.isEmpty()) {
            return "/national/count_of_tests_by_ordered_institution_without_rdhs";
        }
        for (Object o : objCounts) {
            if (o instanceof InstitutionCount) {
                InstitutionCount ic = (InstitutionCount) o;
                institutionCounts.add(ic);
            }
        }

        return "/national/count_of_tests_by_ordered_institution_without_rdhs";
    }

    // This will return data of number of tests done by hospitals of a given RDHS
    // Rukshan

    public String toCountOfTestsFromRdhsToInstitution() {
        Map m = new HashMap();

        String j = "select new lk.gov.health.phsp.pojcs.InstitutionCount(c.institution, count(c))   "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);

        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);

        j += " and (c.createdAt > :fd and c.createdAt < :td) ";
        m.put("fd", getFromDate());



        m.put("td", getToDate());

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
        if (institutionType != null) {
            j += " and c.institution.institutionType=:it ";
            m.put("it", institutionType);
        }

        j += " and c.institution.rdhsArea=:rd";
        m.put("rd", this.rdhs);

        j += " group by c.institution"
                + " order by count(c) desc ";


        institutionCounts = new ArrayList<>();

        List<Object> objCounts = encounterFacade.findAggregates(j, m, TemporalType.TIMESTAMP);
        if (objCounts == null || objCounts.isEmpty()) {
            return "/national/count_of_tests_by_ordered_institution";
        }
        for (Object o : objCounts) {
            if (o instanceof InstitutionCount) {
                InstitutionCount ic = (InstitutionCount) o;
                institutionCounts.add(ic);
            }
        }

        return "/national/count_of_tests_by_ordered_institution";
    }

    public String toCountOfTestsFromPdhsToRdhs() {
        if (pdhs == null) {
            return toCountOfTestsByRdhs();
        } else {
            if (pdhs.getId() == null) {
                return toCountOfTestsByOrderedInstitutionWithoutRdhs();
            } else {
                return toCountOfTestsByRdhs();
            }
        }
    }

    public String toCountOfTestsByPdhs() {
        Map m = new HashMap();

        String j = "select new lk.gov.health.phsp.pojcs.InstitutionCount(c.institution.pdhsArea, count(c))   "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);

        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);

        if (this.filter == null) {
            this.filter = "RESULTSAT";
        }

        switch (this.filter.toUpperCase()) {
            case "CREATEDAT":
                j += " and (c.createdAt > :fd and c.createdAt < :td) ";
                break;
            case "RESULTSAT":
                j += " and (c.resultConfirmedAt > :fd and c.resultConfirmedAt < :td) ";
                break;
            case "SAMPLEDAT":
                j += " and (c.sampledAt > :fd and c.sampledAt < :td) ";
                break;
            default:
                j += " and (c.createdAt > :fd and c.createdAt < :td) ";
                break;
        }

        m.put("fd", getFromDate());
        m.put("td", getToDate());

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

        j += " group by c.institution.pdhsArea"
                + " order by count(c) desc ";

        institutionCounts = new ArrayList<>();
        List<Object> objCounts = encounterFacade.findAggregates(j, m, TemporalType.TIMESTAMP);

        if (objCounts == null || objCounts.isEmpty()) {
            return "/national/count_of_tests_by_pdhs";
        }
        for (Object o : objCounts) {
            if (o instanceof InstitutionCount) {
                InstitutionCount ic = (InstitutionCount) o;
                institutionCounts.add(ic);
            }
        }

        m = new HashMap();
        j = "select count(c)   "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);
        j += " and c.institution.pdhsArea is null ";
        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);

        if (this.filter == null) {
            this.filter = "RESULTSAT";
        }

        switch (this.filter.toUpperCase()) {
            case "CREATEDAT":
                j += " and (c.createdAt > :fd and c.createdAt < :td) ";
                break;
            case "SAMPLEDAT":
                j += " and (c.sampledAt > :fd and c.sampledAt < :td) ";
                break;
            case "RESULTSAT":
                j += " and (c.resultConfirmedAt > :fd and c.resultConfirmedAt < :td) ";
                break;
            default:
                j += " and (c.createdAt > :fd and c.createdAt < :td) ";
                break;
        }

        m.put("fd", getFromDate());
        m.put("td", getToDate());
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
        Long nullCounts = encounterFacade.findAggregateLong(j, m, TemporalType.TIMESTAMP);
        if (nullCounts != null) {
            InstitutionCount ic = new InstitutionCount();
            Area a = new Area();
            a.setName("Non RDHS");
            ic.setArea(a);
            ic.setCount(nullCounts);
            institutionCounts.add(ic);
        }
        return "/national/count_of_tests_by_pdhs";
    }

    public String toCountOfTestsByInstitutionType() {
        Map m = new HashMap();

        String j = "select new lk.gov.health.phsp.pojcs.InstitutionTypeCount(c.institution.institutionType, count(c))   "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);

        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);

        if (this.filter == null) {
            this.filter = "RESULTSAT";
        }

        switch (this.filter.toUpperCase()) {
            case "CREATEDAT":
                j += " and (c.createdAt > :fd and c.createdAt < :td) ";
                break;
            case "SAMPLEDAT":
                j += " and (c.sampledAt > :fd and c.sampledAt < :td) ";
                break;
            case "RESULTSAT":
                j += " and (c.resultConfirmedAt > :fd and c.resultConfirmedAt < :td) ";
                break;
            default:
                j += " and (c.createdAt > :fd and c.createdAt < :td) ";
                break;
        }
        m.put("fd", getFromDate());
        m.put("td", getToDate());

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

        j += " group by c.institution.institutionType"
                + " order by count(c) desc ";

        institutionTypeCounts = new ArrayList<>();
        List<Object> objCounts = encounterFacade.findAggregates(j, m, TemporalType.TIMESTAMP);

        if (objCounts == null || objCounts.isEmpty()) {
            return "/national/count_of_tests_by_institution_type";
        }
        for (Object o : objCounts) {
            if (o instanceof InstitutionTypeCount) {
                InstitutionTypeCount ic = (InstitutionTypeCount) o;
                institutionTypeCounts.add(ic);
            }
        }

        m = new HashMap();
        j = "select count(c)   "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);

        j += " and c.institution.institutionType is null ";
        j += " and c.encounterType=:etype ";

        m.put("etype", EncounterType.Test_Enrollment);

        if (this.filter == null) {
            this.filter = "RESULTSAT";
        }

        switch (this.filter.toUpperCase()) {
            case "CREATEDAT":
                j += " and (c.createdAt > :fd and c.createdAt < :td) ";
                break;
            case "SAMPLEDAT":
                j += " and (c.sampledAt > :fd and c.sampledAt < :td) ";
                break;
            case "RESULTSAT":
                j += " and (c.resultConfirmedAt > :fd and c.resultConfirmedAt < :td) ";
                break;
            default:
                j += " and (c.resultConfirmedAt > :fd and c.resultConfirmedAt < :td) ";
                break;
        }

        m.put("fd", getFromDate());
        m.put("td", getToDate());
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
        Long nullCounts = encounterFacade.findAggregateLong(j, m, TemporalType.TIMESTAMP);
        if (nullCounts != null) {
            InstitutionTypeCount ic = new InstitutionTypeCount();
            ic.setType(null);
            ic.setCount(nullCounts);
            institutionTypeCounts.add(ic);
        }
        return "/national/count_of_tests_by_institution_type";
    }

    public String toCountOfTestsByRdhsWithoutSpecifyingPdhs() {
        pdhs = null;
        return toCountOfTestsByRdhs();
    }

    // This will provide a test positive rate according to the area provided
    // RukshanR
    public String toTestPositiveRateByRdhs() {
        DecimalFormat df = new DecimalFormat("0.0");

        Map m = new HashMap();

        String j = "select new lk.gov.health.phsp.pojcs.InstitutionCount(c.institution.rdhsArea, count(c))   "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);

        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);

        if (this.filter == null) {
            this.filter = "RESULTSAT";
        }

        switch (this.filter.toUpperCase()) {
            case "CREATEDAT":
                j += " and (c.createdAt > :fd and c.createdAt < :td) ";
                break;
            case "SAMPLEDAT":
                j += " and (c.sampledAt > :fd and c.sampledAt < :td) ";
                break;
            case "RESULTSAT":
                j += " and (c.resultConfirmedAt > :fd and c.resultConfirmedAt < :td) ";
                break;
            default:
                j += " and (c.createdAt > :fd and c.createdAt < :td) ";
                break;
        }
        m.put("fd", getFromDate());
        m.put("td", getToDate());

        if (pdhs != null) {
            j += " and c.institution.rdhsArea.pdhsArea=:pd ";
            m.put("pd", pdhs);
        }

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

        j += " group by c.institution.rdhsArea "
                + " order by count(c) desc ";

        List<Object> objCounts = encounterFacade.findAggregates(j, m, TemporalType.TIMESTAMP);

        m = new HashMap();

        j = "select new lk.gov.health.phsp.pojcs.InstitutionCount(c.institution.rdhsArea, count(c))   "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);

        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);

        switch (this.filter.toUpperCase()) {
            case "CREATEDAT":
                j += " and (c.createdAt > :fd and c.createdAt < :td) ";
                break;
            case "SAMPLEDAT":
                j += " and (c.sampledAt > :fd and c.sampledAt < :td) ";
                break;
            case "RESULTSAT":
                j += " and (c.resultConfirmedAt > :fd and c.resultConfirmedAt < :td) ";
                break;
            default:
                j += " and (c.createdAt > :fd and c.createdAt < :td) ";
                break;
        }
        m.put("fd", getFromDate());
        m.put("td", getToDate());

        if (pdhs != null) {
            j += " and c.institution.rdhsArea.pdhsArea=:pd ";
            m.put("pd", pdhs);
        }

        if (testType != null) {
            j += " and c.pcrTestType=:tt ";
            m.put("tt", testType);
        }

        if (orderingCategory != null) {
            j += " and c.pcrOrderingCategory=:oc ";
            m.put("oc", orderingCategory);
        }


        j += " and c.pcrResult=:result ";
        m.put("result", itemApplicationController.getPcrPositive());

        if (lab != null) {
            j += " and c.referalInstitution=:ri ";
            m.put("ri", lab);
        }

        j += " group by c.institution.rdhsArea "
                + " order by count(c) desc ";


        List<Object> objPositives = encounterFacade.findAggregates(j, m, TemporalType.TIMESTAMP);
        institutionCounts = new ArrayList<>();


        if (objCounts == null || objCounts.isEmpty()) {
            return "/national/positivity_rate_by_rdhs";
        }


        for (int index = 0; index <= objPositives.size()-1; index++) {
            InstitutionCount incPositive = (InstitutionCount) objPositives.get(index);
            InstitutionCount incCounts = (InstitutionCount) objCounts.get(index);
            double tempPositiveRate = ((double) incPositive.getCount()/incCounts.getCount()*100);
            String tempRate = df.format(tempPositiveRate) + "%";
            InstitutionCount rateCount = new InstitutionCount();
            rateCount.setArea(incPositive.getArea());
            rateCount.setPositiveRate(tempRate);
            rateCount.setCount(incCounts.getCount());
            institutionCounts.add(rateCount);
        }

        return "/national/positivity_rate_by_rdhs";
    }

    public String toCountOfTestsByRdhs() {
        Map m = new HashMap();

        String j = "select new lk.gov.health.phsp.pojcs.InstitutionCount(c.institution.rdhsArea, count(c))   "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);

        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);

        if (this.filter == null) {
            this.filter = "RESULTSAT";
        }

        switch (this.filter.toUpperCase()) {
            case "CREATEDAT":
                j += " and (c.createdAt > :fd and c.createdAt < :td) ";
                break;
            case "RESULTSAT":
                j += " and (c.resultConfirmedAt > :fd and c.resultConfirmedAt < :td) ";
                break;
            case "SAMPLEDAT":
                j += " and (c.sampledAt > :fd and c.sampledAt < :td) ";
                break;
            default:
                j += " and (c.createdAt > :fd and c.createdAt < :td) ";
                break;
        }

        m.put("fd", getFromDate());
        m.put("td", getToDate());

        if (pdhs != null) {
            j += " and c.institution.rdhsArea.pdhsArea=:pd ";
            m.put("pd", pdhs);
        }

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

        j += " group by c.institution.rdhsArea "
                + " order by count(c) desc ";

        institutionCounts = new ArrayList<>();


        List<Object> objCounts = encounterFacade.findAggregates(j, m, TemporalType.TIMESTAMP);


        if (objCounts == null || objCounts.isEmpty()) {
            return "/national/count_of_tests_by_rdhs";
        }
        for (Object o : objCounts) {
            if (o instanceof InstitutionCount) {
                InstitutionCount ic = (InstitutionCount) o;
                Area a = new Area();
                institutionCounts.add(ic);
            }
        }

        j = "select count(c)   "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m = new HashMap();

        m.put("ret", false);
        j += " and c.institution.rdhsArea is null ";
        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);

        if (this.filter == null) {
            this.filter = "RESULTSAT";
        }

        switch (this.filter.toUpperCase()) {
            case "CREATEDAT":
                j += " and (c.createdAt > :fd and c.createdAt < :td) ";
                break;
            case "SAMPLEDAT":
                j += " and (c.sampledAt > :fd and c.sampledAt < :td) ";
                break;
            case "RESULTSAT":
                j += " and (c.resultConfirmedAt > :fd and c.resultConfirmedAt < :td) ";
                break;
            default:
                j += " and (c.createdAt > :fd and c.createdAt < :td) ";
                break;
        }

        m.put("fd", getFromDate());
        m.put("td", getToDate());

        if (pdhs != null) {
            j += " and (c.institution.rdhsArea is null and c.institution.province=:pro) ";
            m.put("pro", pdhs.getProvince());
        } else {
            j += " and (c.institution.rdhsArea is null) ";
        }
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
        Long nullCounts = encounterFacade.findAggregateLong(j, m, TemporalType.TIMESTAMP);
        if (nullCounts != null) {
            InstitutionCount ic = new InstitutionCount();
            Area a = new Area();
            a.setName("No RDHS");
            ic.setArea(a);
            ic.setCount(nullCounts);
            institutionCounts.add(ic);
        }
        return "/national/count_of_tests_by_rdhs";
    }

    public String toCountOfResultsByOrderedInstitution() {
        Map m = new HashMap();
        String j = "select new lk.gov.health.phsp.pojcs.InstitutionCount(c.institution, count(c))   "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);

        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);

        if (this.filter == null) {
            this.filter = "RESULTSAT";
        }

        switch (this.filter.toUpperCase()) {
            case "CREATEDAT":
                j += " and c.createdAt between :fd and :td ";
                break;
            case "SAMPLEDAT":
                j += " and c.sampledAt between :fd and :td ";
                break;
            case "RESULTSAT":
                j += " and c.resultConfirmedAt between :fd and :td ";
                break;
            default:
                j += " and c.resultConfirmedAt between :fd and :td ";
                break;
        }
        m.put("fd", getFromDate());
        m.put("td", getToDate());

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
        j += " group by c.institution"
                + " order by count(c) desc ";

        institutionCounts = new ArrayList<>();

        List<Object> objCounts = encounterFacade.findAggregates(j, m, TemporalType.TIMESTAMP);
        if (objCounts == null || objCounts.isEmpty()) {
            return "/national/count_of_results_by_ordered_institution";
        }
        for (Object o : objCounts) {
            if (o instanceof InstitutionCount) {
                InstitutionCount ic = (InstitutionCount) o;
                institutionCounts.add(ic);
            }
        }
        return "/national/count_of_results_by_ordered_institution";
    }

    public void processInstitutionVicePeformanceReport() {
        if (rdhs == null) {
            JsfUtil.addErrorMessage("Please select the RDHS");
            return;
        }
        institutionPeformances = new ArrayList<>();
        List<InstitutionType> types = new ArrayList<>();
        types.add(InstitutionType.Base_Hospital);
        types.add(InstitutionType.District_General_Hospital);
        types.add(InstitutionType.Divisional_Hospital);
        types.add(InstitutionType.Hospital);
        types.add(InstitutionType.Intermediate_Care_Centre);
        types.add(InstitutionType.Lab);
        types.add(InstitutionType.MOH_Office);
        types.add(InstitutionType.Mobile_Lab);
        types.add(InstitutionType.National_Hospital);
        types.add(InstitutionType.Private_Sector_Institute);
        types.add(InstitutionType.Primary_Medical_Care_Unit);
        types.add(InstitutionType.Private_Sector_Labatory);
        types.add(InstitutionType.Provincial_General_Hospital);
        types.add(InstitutionType.Teaching_Hospital);
        List<Institution> inss = institutionApplicationController.findRegionalInstitutions(types, rdhs);
        fromDate = CommonController.startOfTheDate(fromDate);
        toDate = CommonController.endOfTheDate(toDate);
        for (Institution ins : inss) {
            InstitutionPeformance ip = new InstitutionPeformance();
            ip.setInstitution(ins);
            ip.setPcrs(dashboardApplicationController.getOrderCount(ins, fromDate, toDate,
                    itemApplicationController.getPcr(), orderingCategory, null, null));
            ip.setRats(dashboardApplicationController.getOrderCount(ins, fromDate, toDate,
                    itemApplicationController.getRat(), orderingCategory, null, null));
            ip.setPcrPositives(dashboardApplicationController.getOrderCount(ins, fromDate, toDate,
                    itemApplicationController.getPcr(), orderingCategory, itemApplicationController.getPcrPositive(), null));
            ip.setRatPositives(dashboardApplicationController.getOrderCount(ins, fromDate, toDate,
                    itemApplicationController.getRat(), orderingCategory, itemApplicationController.getPcrPositive(), null));
            institutionPeformances.add(ip);
        }
        generateInstitutionPeformanceSummery();
    }

    public String toCountOfResultsByLab() {
        Map m = new HashMap();
        String j = "select new lk.gov.health.phsp.pojcs.InstitutionCount(c.referalInstitution, count(c))   "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);

        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);

        if (this.filter == null) {
            this.filter = "RESULTSAT";
        }

        switch (this.filter.toUpperCase()) {
            case "CREATEDAT":
                j += " and (c.createdAt > :fd and c.createdAt <:td) ";
                break;
            case "SAMPLEDAT":
                j += " and (c.sampledAt > :fd and c.sampledAt <:td) ";
                break;
            case "RESULTSAT":
                j += " and (c.resultConfirmedAt > :fd and c.resultConfirmedAt <:td) ";
                break;
            default:
                j += " and (c.resultConfirmedAt > :fd and c.resultConfirmedAt <:td) ";
                break;
        }
        m.put("fd", getFromDate());
        m.put("td", getToDate());

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
        j += " group by c.referalInstitution"
                + " order by count(c) desc ";

        institutionCounts = new ArrayList<>();

        List<Object> objCounts = encounterFacade.findAggregates(j, m, TemporalType.TIMESTAMP);
        if (objCounts == null || objCounts.isEmpty()) {
            return "/national/count_of_results_by_lab";
        }
        for (Object o : objCounts) {
            if (o instanceof InstitutionCount) {
                InstitutionCount ic = (InstitutionCount) o;
                institutionCounts.add(ic);
            }
        }

        return "/national/count_of_results_by_lab";
    }

    public String toCountOfResultsByMohArea() {
        Map m = new HashMap();
        String j = "select new lk.gov.health.phsp.pojcs.InstitutionCount(c.client.person.mohArea, count(c))   "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);

        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);

        if (this.filter == null) {
            this.filter = "RESULTSAT";
        }

        switch (this.filter.toUpperCase()) {
            case "CREATEDAT":
                j += " and c.createdAt between :fd and :td ";
                break;
            case "SAMPLEDAT":
                j += " and c.sampledAt between :fd and :td ";
                break;
            case "RESULTSAT":
                j += " and c.resultConfirmedAt between :fd and :td ";
                break;
            default:
                j += " and c.resultConfirmedAt between :fd and :td ";
                break;
        }
        m.put("fd", getFromDate());
        m.put("td", getToDate());

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
        j += " group by c.client.person.mohArea "
                + " order by count(c) desc ";

        institutionCounts = new ArrayList<>();

        List<Object> objCounts = encounterFacade.findAggregates(j, m, TemporalType.TIMESTAMP);
        if (objCounts == null || objCounts.isEmpty()) {
            return "/national/count_of_results_by_moh_area";
        }
        for (Object o : objCounts) {
            if (o instanceof InstitutionCount) {
                InstitutionCount ic = (InstitutionCount) o;
                institutionCounts.add(ic);
            }
        }

        return "/national/count_of_results_by_moh_area";
    }

    public String toAssignInvestigation() {
        testType = itemApplicationController.getPcr();
        result = itemApplicationController.getPcrPositive();

        Map m = new HashMap();

        String j = "select c "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) "
                + " and (c.completed is null or c.completed=:com) ";
        m.put("ret", false);
        m.put("com", false);

        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);

        j += " and c.client.person.mohArea=:moh ";
        m.put("moh", webUserController.getLoggedInstitution().getMohArea());

        j += " and c.createdAt between :fd and :td ";
        m.put("fd", getFromDate());
        m.put("td", getToDate());
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

        tests = encounterFacade.findByJpql(j, m, TemporalType.TIMESTAMP);

        return "/moh/assign_investigation";
    }

    public String toStartInvestigation() {
        return "/moh/start_investigation";
    }

    public String toViewInvestigatedCases() {
        return "/moh/view_investigated_cases";
    }

    public void assignToInvestigate() {
        if (assignee == null) {
            JsfUtil.addErrorMessage("Please select someone to assign the investigation");
            return;
        }
        if (selectedToAssign == null || selectedToAssign.isEmpty()) {
            JsfUtil.addErrorMessage("Please select cases to assign the investigation");
            return;
        }
        for (Encounter e : selectedToAssign) {
            e.setCompletedBy(assignee);
            encounterFacade.edit(e);
        }
        selectedToAssign = null;
    }

    public String assignMohAreaToContactScreeningAtRegionalLevel() {
        if (selectedCecis == null || selectedCecis.isEmpty()) {
            JsfUtil.addErrorMessage("Please select contacts");
            return "";
        }
        if (mohArea == null) {
            JsfUtil.addErrorMessage("Please select an MOH Area");
            return "";
        }
        for (ClientEncounterComponentItem i : selectedCecis) {
            i.setAreaValue(mohArea);
            i.setLastEditBy(webUserController.getLoggedUser());
            i.setLastEditeAt(new Date());
            ceciFacade.edit(i);
        }
        selectedCecis = null;
        mohArea = null;
        JsfUtil.addSuccessMessage("MOH Areas added");
        return toListOfFirstContactsWithoutMohForRegionalLevel();
    }

    public Boolean checkNicExists(String nic, Client c) {
        String jpql = "select count(c) from Client c "
                + " where c.retired=:ret "
                + " and c.reservedClient<>:res "
                + " and c.person.nic=:nic ";
        Map m = new HashMap();
        m.put("ret", false);
        m.put("res", true);
        m.put("nic", nic);
        if (c != null && c.getPerson() != null && c.getPerson().getId() != null) {
            jpql += " and c.person <> :person";
            m.put("person", c.getPerson());
        }
        Long count = clientFacade.countByJpql(jpql, m);
        if (count == null || count == 0l) {
            return false;
        } else {
            return true;
        }
    }

    public void checkNicExistsForRat() {
        nicExistsForRat = null;
        if (rat == null) {
            return;
        }
        if (rat.getClient() == null) {
            return;
        }
        if (rat.getClient().getPerson() == null) {
            return;
        }
        if (rat.getClient().getPerson().getNic() == null) {
            return;
        }
        if (rat.getClient().getPerson().getNic().trim().equals("")) {
            return;
        }
        nicExistsForRat = checkNicExists(rat.getClient().getPerson().getNic(), rat.getClient());
    }

    public void checkNicExistsForPcr() {
        nicExistsForPcr = null;
        if (pcr == null) {
            return;
        }
        if (pcr.getClient() == null) {
            return;
        }
        if (pcr.getClient().getPerson() == null) {
            return;
        }
        if (pcr.getClient().getPerson().getNic() == null) {
            return;
        }
        if (pcr.getClient().getPerson().getNic().trim().equals("")) {
            return;
        }
        nicExistsForPcr = checkNicExists(pcr.getClient().getPerson().getNic(), pcr.getClient());
    }

    public Client lastClientWithNic(String nic, Client c) {
        if (nic == null || nic.trim().equals("")) {
            return null;
        }
        String jpql = "select c "
                + " from Client c "
                + " where c.retired=:ret "
                + " and c.person.nic=:nic ";
        Map m = new HashMap();
        m.put("ret", false);
        m.put("nic", nic);
        if (c != null && c.getPerson() != null && c.getPerson().getId() != null) {
            jpql += " and c.person <> :person";
            m.put("person", c.getPerson());
        }
        jpql += " order by c.id desc";
        return clientFacade.findFirstByJpql(jpql, m);
    }

    public String toPcrPositiveReportsIndexNational() {
        fromDate = CommonController.startOfTheDate();
        toDate = CommonController.endOfTheDate();
        return "/national/pcr_positive_links";
    }

    public String toLabReportsIndexNational() {
        fromDate = CommonController.startOfTheDate();
        toDate = CommonController.endOfTheDate();
        return "/national/lab_report_links";
    }

    public String toPcrPositiveCasesList() {
        result = itemApplicationController.getPcrPositive();
        testType = itemApplicationController.getPcr();
        Map m = new HashMap();
        String j = "select c "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);
        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);
        j += " and c.resultConfirmedAt between :fd and :td ";
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        j += " and c.pcrTestType=:tt ";
        m.put("tt", testType);
        j += " and c.pcrResult=:result ";
        m.put("result", result);
        tests = encounterFacade.findByJpql(j, m, TemporalType.TIMESTAMP);
        return "/national/result_list_pcr_positive";
    }

    public String toPcrPositiveByDistrict() {
        result = itemApplicationController.getPcrPositive();
        testType = itemApplicationController.getPcr();
        Map m = new HashMap();
        String j = "select new lk.gov.health.phsp.pojcs.InstitutionCount(c.client.person.district, count(c))  "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);
        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);
        j += " and c.resultConfirmedAt between :fd and :td ";
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        j += " and c.pcrTestType=:tt ";
        m.put("tt", testType);
        j += " and c.pcrResult=:result ";
        m.put("result", result);
        j += " group by c.client.person.district";
        List<Object> objs = new ArrayList<>();
        try {
            objs = encounterFacade.findObjectByJpql(j, m, TemporalType.TIMESTAMP);
        } catch (Exception e) {

        }
        institutionCounts = new ArrayList<>();
        for (Object o : objs) {
            if (o instanceof InstitutionCount) {
                InstitutionCount ic = (InstitutionCount) o;
                institutionCounts.add(ic);
            }
        }
        return "/national/pcr_positive_counts_by_district";
    }

    public String toPcrPositiveByInstitutionDistrict() {
        result = itemApplicationController.getPcrPositive();
        testType = itemApplicationController.getPcr();
        Map m = new HashMap();
        String j = "select new lk.gov.health.phsp.pojcs.InstitutionCount(c.institution.district, count(c))  "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);
        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);
        j += " and c.resultConfirmedAt between :fd and :td ";
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        j += " and c.pcrTestType=:tt ";
        m.put("tt", testType);
        j += " and c.pcrResult=:result ";
        m.put("result", result);
        j += " group by c.institution.district";
        List<Object> objs = new ArrayList<>();
        try {
            objs = encounterFacade.findObjectByJpql(j, m, TemporalType.TIMESTAMP);
        } catch (Exception e) {

        }
        institutionCounts = new ArrayList<>();
        for (Object o : objs) {
            if (o instanceof InstitutionCount) {
                InstitutionCount ic = (InstitutionCount) o;
                institutionCounts.add(ic);
            }
        }
        return "/national/pcr_positive_counts_by_institution_district";
    }

    public String toPcrPositiveByOrderedInstitute() {
        result = itemApplicationController.getPcrPositive();
        testType = itemApplicationController.getPcr();
        Map m = new HashMap();
        String j = "select new lk.gov.health.phsp.pojcs.InstitutionCount(c.institution, count(c))  "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);
        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);
        j += " and c.resultConfirmedAt between :fd and :td ";
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        j += " and c.pcrTestType=:tt ";
        m.put("tt", testType);
        j += " and c.pcrResult=:result ";
        m.put("result", result);
        j += " group by c.institution";
        List<Object> objs = new ArrayList<>();
        try {
            objs = encounterFacade.findObjectByJpql(j, m, TemporalType.TIMESTAMP);
        } catch (Exception e) {

        }
        institutionCounts = new ArrayList<>();
        for (Object o : objs) {
            if (o instanceof InstitutionCount) {
                InstitutionCount ic = (InstitutionCount) o;
                institutionCounts.add(ic);
            }
        }
        return "/national/pcr_positive_counts_by_ordered_institution";
    }

    public String toPcrPositiveByLab() {
        result = itemApplicationController.getPcrPositive();
        testType = itemApplicationController.getPcr();
        Map m = new HashMap();
        String j = "select new lk.gov.health.phsp.pojcs.InstitutionCount(c.referalInstitution, count(c))  "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);
        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);
        j += " and c.resultConfirmedAt between :fd and :td ";
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        j += " and c.pcrTestType=:tt ";
        m.put("tt", testType);
        j += " and c.pcrResult=:result ";
        m.put("result", result);
        j += " group by c.referalInstitution";
        List<Object> objs = new ArrayList<>();
        try {
            objs = encounterFacade.findObjectByJpql(j, m, TemporalType.TIMESTAMP);
        } catch (Exception e) {

        }
        institutionCounts = new ArrayList<>();
        for (Object o : objs) {
            if (o instanceof InstitutionCount) {
                InstitutionCount ic = (InstitutionCount) o;
                institutionCounts.add(ic);
            }
        }
        return "/national/pcr_positive_counts_by_lab";
    }

    public void deleteTest() {
        if (deleting == null) {
            JsfUtil.addErrorMessage("Nothing to delete");
            return;
        }
        if (deleting.getInstitution() == null) {
            JsfUtil.addErrorMessage("No Institution");
            return;
        }
        if (deleting.getReceivedAtLab() != null && deleting.getReferalInstitution() != null) {
            if (deleting.getReceivedAtLab() != null && deleting.getReferalInstitution() != webUserController.getLoggedInstitution()) {
                JsfUtil.addErrorMessage("Already receievd by the Lab. Can't delete.");
                return;
            }
        }
        deleting.setRetired(true);
        deleting.setRetiredAt(new Date());
        deleting.setRetiredBy(webUserController.getLoggedUser());
        JsfUtil.addSuccessMessage("Removed");
        encounterFacade.edit(deleting);
    }

    private void fillRegionalMohsAndHospitals() {
        List<InstitutionType> its = new ArrayList<>();
        its.add(InstitutionType.Hospital);
        its.add(InstitutionType.MOH_Office);
        Area rdhs;
        if (webUserController.getLoggedInstitution() != null && webUserController.getLoggedInstitution().getRdhsArea() != null) {
            rdhs = webUserController.getLoggedInstitution().getRdhsArea();
            regionalMohsAndHospitals = institutionApplicationController.findRegionalInstitutions(its, rdhs);
        } else {
            regionalMohsAndHospitals = new ArrayList<>();
        }

    }

    public String toInstitutionvicePeformanceReport() {
        return "/national/institution_vice_peformance_report";
    }

    public String toListOfTests() {
        Map m = new HashMap();
        String j = "select c "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);

        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);

        if (this.filter == null) {
            this.filter = "RESULTSAT";
        }

        switch (this.filter.toUpperCase()) {
            case "CREATEDAT":
                j += " and c.createdAt between :fd and :td ";
                break;
            case "SAMPLEDAT":
                j += " and c.sampledAt between :fd and :td ";
                break;
            case "RESULTSAT":
                j += " and c.resultConfirmedAt between :fd and :td ";
                break;
            default:
                j += " and c.createdAt between :fd and :td ";
                break;
        }
        m.put("fd", getFromDate());
        m.put("td", getToDate());
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
        tests = encounterFacade.findByJpql(j, m, TemporalType.TIMESTAMP);
        return "/national/list_of_tests";
    }

    public String toListOfTestsWithoutMohForRegionalLevel() {
        Map m = new HashMap();
        String j = "select c "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);

        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);

        j += " and c.client.person.mohArea is null ";

        if (mohOrHospital != null) {
            j += " and c.institution=:ins ";
            m.put("ins", mohOrHospital);
        } else {
            j += " and (c.institution.rdhsArea=:rdhs or c.client.person.district=:district) ";
            m.put("rdhs", webUserController.getLoggedInstitution().getRdhsArea());
            m.put("district", webUserController.getLoggedInstitution().getDistrict());
        }

        j += " and c.createdAt between :fd and :td ";
        m.put("fd", getFromDate());
        m.put("td", getToDate());
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

        tests = encounterFacade.findByJpql(j, m, TemporalType.TIMESTAMP);

        return "/regional/list_of_tests_without_moh";
    }

    public String toListOfFirstContactsWithoutMohForRegionalLevel() {
        Map m = new HashMap();
        String j = "select ci "
                + " from ClientEncounterComponentItem ci"
                + " join ci.encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);
//        ClientEncounterComponentItem ci = new ClientEncounterComponentItem();
//        ci.getItem().getCode();
        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Case_Enrollment);

        j += " and ci.areaValue is null ";

        j += " and ci.item.code=:code ";
        m.put("code", "first_contacts");

        j += " and ci.areaValue2=:district ";
        m.put("district", webUserController.getLoggedInstitution().getDistrict());

        j += " and c.createdAt between :fd and :td ";
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        cecItems = ceciFacade.findByJpql(j, m, TemporalType.TIMESTAMP);
        return "/regional/list_of_first_contacts_without_moh";
    }

    public String toOrderTestsForFirstContactsForMoh() {
        Map m = new HashMap();
        String j = "select ci "
                + " from ClientEncounterComponentItem ci"
                + " join ci.encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);
//        ClientEncounterComponentItem ci = new ClientEncounterComponentItem();
//        ci.getItem().getCode();
        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Case_Enrollment);

        j += " and ci.areaValue=:mymoh ";
        m.put("mymoh", webUserController.getLoggedInstitution().getMohArea());

        j += " and ci.item.code=:code ";
        m.put("code", "first_contacts");

        j += " and c.createdAt between :fd and :td ";
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        cecItems = ceciFacade.findByJpql(j, m, TemporalType.TIMESTAMP);
        return "/moh/order_tests_for_moh";
    }

    public String toListOfFirstContactsForRegionalLevel() {
        Map m = new HashMap();
        String j = "select ci "
                + " from ClientEncounterComponentItem ci"
                + " join ci.encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);
//        ClientEncounterComponentItem ci = new ClientEncounterComponentItem();
//        ci.getItem().getCode();
        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Case_Enrollment);

        j += " and ci.item.code=:code ";
        m.put("code", "first_contacts");

        j += " and ci.areaValue2=:district ";
        m.put("district", webUserController.getLoggedInstitution().getDistrict());

        j += " and c.createdAt between :fd and :td ";
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        cecItems = ceciFacade.findByJpql(j, m, TemporalType.TIMESTAMP);
        return "/regional/list_of_first_contacts";
    }

    public String toListOfInvestigatedCasesForMoh() {
        return "/moh/investigated_list";
    }

    public void toDeleteTestFromLastPcrList() {
        deleteTest();
        sessionController.getPcrs().remove(deleting.getId());
    }

    public void toDeleteTestFromLastRatList() {
        deleteTest();
        sessionController.getRats().remove(deleting.getId());
    }

    public String toDeleteTestFromTestList() {
        deleteTest();
        return toListResults();
    }

    public String toRatView() {
        if (rat == null) {
            JsfUtil.addErrorMessage("No RAT");
            return "";
        }
        if (rat.getClient() == null) {
            JsfUtil.addErrorMessage("No Client");
            return "";
        }
        return "/moh/rat_view";
    }

    public String toRatOrderView() {
        if (rat == null) {
            JsfUtil.addErrorMessage("No RAT");
            return "";
        }
        if (rat.getClient() == null) {
            JsfUtil.addErrorMessage("No Client");
            return "";
        }
        return "/moh/rat_order_view";
    }

    public String toRatResultView() {
        if (rat == null) {
            JsfUtil.addErrorMessage("No RAT");
            return "";
        }
        if (rat.getClient() == null) {
            JsfUtil.addErrorMessage("No Client");
            return "";
        }
        return "/moh/rat_result_view";
    }

    public String toPcrResultView() {
        if (pcr == null) {
            JsfUtil.addErrorMessage("No RAT");
            return "";
        }
        if (pcr.getClient() == null) {
            JsfUtil.addErrorMessage("No Client");
            return "";
        }
        return "/moh/pcr_result_view";
    }

    public String toPcrView() {
        if (pcr == null) {
            JsfUtil.addErrorMessage("No RAT");
            return "";
        }
        if (pcr.getClient() == null) {
            JsfUtil.addErrorMessage("No Client");
            return "";
        }
        return "/moh/pcr_view";
    }

    public String toEditTest() {
        if (test == null) {
            JsfUtil.addErrorMessage("No Test");
            return "";
        }
        if (test.getPcrTestType().equals(itemApplicationController.getRat())) {
            rat = test;
            return toRatOrderEdit();
        } else if (test.getPcrTestType().equals(itemApplicationController.getPcr())) {
            pcr = test;
            return toPcrEdit();
        } else {
            //TODO: add to edit test when test type not given
            JsfUtil.addErrorMessage("Not a RAT or PCR.");
            return "";
        }
    }

    public String toViewTest() {
        if (test == null) {
            JsfUtil.addErrorMessage("No Test");
            return "";
        }
        if (test.getPcrTestType().equals(itemApplicationController.getRat())) {
            rat = test;
            return toRatOrderView();
        } else if (test.getPcrTestType().equals(itemApplicationController.getPcr())) {
            pcr = test;
            return toPcrView();
        } else {
            //TODO: add to edit test when test type not given
            JsfUtil.addErrorMessage("Not a RAT or PCR.");
            return "";
        }
    }

    public String toViewResult() {
        if (test == null) {
            JsfUtil.addErrorMessage("No Test");
            return "";
        }
        if (test.getPcrTestType().equals(itemApplicationController.getRat())) {
            rat = test;
            return toRatResultView();
        } else if (test.getPcrTestType().equals(itemApplicationController.getPcr())) {
            pcr = test;
            return toPcrResultView();
        } else {
            //TODO: add to edit test when test type not given
            JsfUtil.addErrorMessage("Not a RAT or PCR.");
            return "";
        }
    }

    public String toRatEdit() {
        if (rat == null) {
            JsfUtil.addErrorMessage("No RAT");
            return "";
        }
        if (!rat.getPcrTestType().equals(itemApplicationController.getRat())) {
            JsfUtil.addErrorMessage("Not a RAT");
            return "";
        }
        return "/moh/rat";
    }

    public String toRatOrderEdit() {
        if (rat == null) {
            JsfUtil.addErrorMessage("No RAT");
            return "";
        }
        if (!rat.getPcrTestType().equals(itemApplicationController.getRat())) {
            JsfUtil.addErrorMessage("Not a RAT");
            return "";
        }
        return "/moh/rat_order";
    }

    public String toPcrEdit() {
        if (pcr == null) {
            JsfUtil.addErrorMessage("No PCR");
            return "";
        }
        if (!pcr.getPcrTestType().equals(itemApplicationController.getPcr())) {
            JsfUtil.addErrorMessage("Not a PCR");
            return "";
        }
        return "/moh/pcr";
    }

    public List<Area> completeDistricts(String qry) {
        return areaController.completeDistricts(qry);
    }

    public List<Area> completeMohAreas(String qry) {
        return areaController.completeMoh(qry);
    }

    public List<Item> getCitizenships() {
        return itemApplicationController.getCitizenships();
    }

    public List<Item> getSexes() {
        return itemApplicationController.getSexes();
    }

    public String toListResults() {
        Map m = new HashMap();
        String j = "select c "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);
        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);

        j += " and c.resultConfirmedAt between :fd and :td ";
        m.put("fd", getFromDate());
        m.put("td", getToDate());
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

        tests = encounterFacade.findByJpql(j, m, TemporalType.TIMESTAMP);
        return "/national/list_of_results";
    }

    public String toDistrictViceTestListForOrderingCategories() {
        Map m = new HashMap();

        String j = "select new lk.gov.health.phsp.pojcs.InstitutionCount(c.client.person.district, c.pcrOrderingCategory, count(c))  "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);
        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);

        j += " and c.createdAt between :fd and :td ";
        m.put("fd", getFromDate());
        m.put("td", getToDate());

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
        j += " group by c.pcrOrderingCategory, c.client.person.district";
        List<Object> objs = encounterFacade.findObjectByJpql(j, m, TemporalType.TIMESTAMP);
        List<InstitutionCount> tics = new ArrayList<>();
        for (Object o : objs) {
            if (o instanceof InstitutionCount) {
                InstitutionCount ic = (InstitutionCount) o;
                tics.add(ic);
            }
        }
        institutionCounts = tics;
        return "/national/ordering_category_district";
    }

    public String toMohViceTestListForOrderingCategories() {
        Map m = new HashMap();

        String j = "select new lk.gov.health.phsp.pojcs.InstitutionCount(c.client.person.mohArea, c.pcrOrderingCategory, count(c))  "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);
        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);

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
        if (district != null) {
            j += " and c.client.person.district=:dis ";
            m.put("dis", district);
        }
        j += " group by c.pcrOrderingCategory, c.client.person.mohArea";
        List<Object> objs = encounterFacade.findObjectByJpql(j, m, TemporalType.TIMESTAMP);
        List<InstitutionCount> tics = new ArrayList<>();
        for (Object o : objs) {
            if (o instanceof InstitutionCount) {
                InstitutionCount ic = (InstitutionCount) o;
                tics.add(ic);
            }
        }
        institutionCounts = tics;
        return "/moh/ordering_category_moh";
    }

    public String toTestRequestDistrictCounts() {
        Map m = new HashMap();
        String j = "select c "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);
        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);
        j += " and c.createdAt between :fd and :td ";
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        if (testType != null) {
            j += " and c.pcrTestType=:tt ";
            m.put("tt", testType);
        }
        if (orderingCategory != null) {
            j += " and c.pcrOrderingCategory=:oc ";
            m.put("oc", orderingCategory);
        }
        tests = encounterFacade.findByJpql(j, m, TemporalType.TIMESTAMP);
        return "/moh/list_of_tests";
    }

    public String toTestListWithoutResults() {
        Map m = new HashMap();
        String j = "select c "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);

        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);

        j += " and c.institution=:ins ";
        m.put("ins", webUserController.getLoggedInstitution());

        j += " and c.createdAt between :fd and :td ";
        m.put("fd", getFromDate());
        m.put("td", getToDate());
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

        tests = encounterFacade.findByJpql(j, m, TemporalType.TIMESTAMP);
        return "/moh/list_of_tests_without_results";
    }

    public String toListOfTestsRegional() {
        Map m = new HashMap();

        String j = "select c "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);

        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);

        j += " and c.client.person.district=:district ";
        m.put("district", webUserController.getLoggedUser().getArea());

        if (mohOrHospital != null) {
            j += " and c.institution=:ins ";
            m.put("ins", mohOrHospital);
        } else {
            j += " and (c.institution.rdhsArea=:rdhs or c.client.person.district=:district) ";
            m.put("rdhs", webUserController.getLoggedInstitution().getRdhsArea());
            m.put("district", webUserController.getLoggedInstitution().getDistrict());
        }

        if (mohArea != null) {
            j += " and c.client.person.mohArea=:moh ";
            m.put("moh", mohArea);
        }

        j += " and c.createdAt between :fd and :td ";
        m.put("fd", getFromDate());
        m.put("td", getToDate());
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

        tests = encounterFacade.findByJpql(j, m, TemporalType.TIMESTAMP);
        return "/regional/list_of_tests";
    }

    public String toListCasesByManagement() {
        Map m = new HashMap();
        String j = "select distinct(c) "
                + " from ClientEncounterComponentItem ci join ci.encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);

        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Case_Enrollment);

        if (mohOrHospital != null) {
            j += " and c.institution=:ins ";
            m.put("ins", mohOrHospital);
        }

        j += " and c.createdAt between :fd and :td ";
        m.put("fd", getFromDate());
        m.put("td", getToDate());

        if (managementType != null) {
            j += " and (ci.item.code=:mxplan and ci.itemValue.code=:planType) ";
            m.put("mxplan", "placement_of_diagnosed_patient");
            m.put("planType", managementType.getCode());
        } else {
            j += " and ci.item.code=:mxplan ";
            m.put("mxplan", "placement_of_diagnosed_patient");
        }

        j += " group by c";

        tests = encounterFacade.findByJpql(j, m, TemporalType.TIMESTAMP);

        return "/national/list_of_cases_by_management_plan";
    }

    public String toListCasesByManagementForRegionalLevel() {
        Map m = new HashMap();
        String j = "select distinct(c) "
                + " from ClientEncounterComponentItem ci join ci.encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);

//        ClientEncounterComponentItem ci;
        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Case_Enrollment);

        if (mohOrHospital != null) {
            j += " and c.institution=:ins ";
            m.put("ins", mohOrHospital);
        } else {
            j += " and (c.institution.rdhsArea=:rdhs or c.client.person.district=:district) ";
            m.put("rdhs", webUserController.getLoggedInstitution().getRdhsArea());
            m.put("district", webUserController.getLoggedInstitution().getDistrict());
        }

        j += " and c.createdAt between :fd and :td ";
        m.put("fd", getFromDate());
        m.put("td", getToDate());

        if (managementType != null) {
            j += " and (ci.item.code=:mxplan and ci.itemValue.code=:planType) ";
            m.put("mxplan", "placement_of_diagnosed_patient");
            m.put("planType", managementType.getCode());
        } else {
            j += " and ci.item.code=:mxplan ";
            m.put("mxplan", "placement_of_diagnosed_patient");
        }

        j += " group by c";


        tests = encounterFacade.findByJpql(j, m, TemporalType.TIMESTAMP);

        return "/regional/list_of_cases_by_management_plan";
    }

    public String toListCasesByManagementForNationalLevel() {
        Map m = new HashMap();
        String j = "select distinct(c) "
                + " from ClientEncounterComponentItem ci join ci.encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);

//        ClientEncounterComponentItem ci;
        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Case_Enrollment);

        j += " and c.createdAt between :fd and :td ";
        m.put("fd", getFromDate());
        m.put("td", getToDate());

        if (managementType != null) {
            j += " and (ci.item.code=:mxplan and ci.itemValue.code=:planType) ";
            m.put("mxplan", "placement_of_diagnosed_patient");
            m.put("planType", managementType.getCode());
        } else {
            j += " and ci.item.code=:mxplan ";
            m.put("mxplan", "placement_of_diagnosed_patient");
        }

        j += " group by c";


        tests = encounterFacade.findByJpql(j, m, TemporalType.TIMESTAMP);

        return "/national/list_of_cases_by_management_plan";
    }

    public String toEnterResults() {
        Map m = new HashMap();

        String j = "select c "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);

        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);

        j += " and c.institution=:ins ";
        m.put("ins", webUserController.getLoggedInstitution());

        j += " and c.createdAt between :fd and :td ";
        m.put("fd", getFromDate());
        m.put("td", getToDate());
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

        tests = encounterFacade.findByJpql(j, m, TemporalType.TIMESTAMP);
        return "/moh/enter_results";
    }

    public List<Item> getCovidTestOrderingCategories() {
        return itemApplicationController.getCovidTestOrderingCategories();
    }

    public List<Item> getInvestigationFilters() {
        return itemApplicationController.getInvestigationFilters();
    }

    public List<Item> getCovidTestTypes() {
        return itemApplicationController.getCovidTestTypes();
    }

    public List<Item> getResultTypes() {
        return itemApplicationController.getPcrResults();
    }

    public List<Item> getManagementTypes() {
        return itemApplicationController.getManagementTypes();
    }

    public List<Institution> completeLab(String qry) {
        List<InstitutionType> its = new ArrayList<>();
        its.add(InstitutionType.Lab);
        return institutionController.fillInstitutions(its, qry, null);
    }

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Getters & Setters">
// </editor-fold>
    public Encounter getRat() {
        return rat;
    }

    public void setRat(Encounter rat) {
        this.rat = rat;
    }

    public Encounter getPcr() {
        return pcr;
    }

    public void setPcr(Encounter pcr) {
        this.pcr = pcr;
    }

    public Encounter getCovidCase() {
        return covidCase;
    }

    public void setCovidCase(Encounter covidCase) {
        this.covidCase = covidCase;
    }

    public List<Encounter> getTests() {
        return tests;
    }

    public void setTests(List<Encounter> tests) {
        this.tests = tests;
    }

    public Date getFromDate() {
        if (fromDate == null) {
            fromDate = CommonController.startOfTheDate();
        }
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        if (toDate == null) {
            toDate = commonController.endOfTheDay();
        }
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public Encounter getTest() {
        return test;
    }

    public void setTest(Encounter test) {
        this.test = test;
    }

    public Item getOrderingCategory() {
        return orderingCategory;
    }

    public void setOrderingCategory(Item orderingCategory) {
        this.orderingCategory = orderingCategory;
    }

    public Item getResult() {
        return result;
    }

    public void setResult(Item result) {
        this.result = result;
    }

    public Item getTestType() {
        return testType;
    }

    public void setTestType(Item testType) {
        this.testType = testType;
    }

    public Institution getLab() {
        return lab;
    }

    public void setLab(Institution lab) {
        this.lab = lab;
    }

    public Encounter getDeleting() {
        return deleting;
    }

    public void setDeleting(Encounter deleting) {
        this.deleting = deleting;
    }

    public List<Institution> getRegionalMohsAndHospitals() {
        if (regionalMohsAndHospitals == null) {
            fillRegionalMohsAndHospitals();
        }
        return regionalMohsAndHospitals;
    }

    public void setRegionalMohsAndHospitals(List<Institution> regionalMohsAndHospitals) {
        this.regionalMohsAndHospitals = regionalMohsAndHospitals;
    }

    public Institution getMohOrHospital() {
        return mohOrHospital;
    }

    public void setMohOrHospital(Institution mohOrHospital) {
        this.mohOrHospital = mohOrHospital;
    }

    public List<InstitutionCount> getInstitutionCounts() {
        return institutionCounts;
    }

    public Boolean getNicExistsForPcr() {
        return nicExistsForPcr;
    }

    public void setNicExistsForPcr(Boolean nicExistsForPcr) {
        this.nicExistsForPcr = nicExistsForPcr;
    }

    public Boolean getNicExistsForRat() {
        return nicExistsForRat;
    }

    public void setNicExistsForRat(Boolean nicExistsForRat) {
        this.nicExistsForRat = nicExistsForRat;
    }

    public Area getDistrict() {
        return district;
    }

    public void setDistrict(Area district) {
        this.district = district;
    }

    public WebUser getAssignee() {
        return assignee;
    }

    public void setAssignee(WebUser assignee) {
        this.assignee = assignee;
    }

    public List<Encounter> getSelectedToAssign() {
        return selectedToAssign;
    }

    public void setSelectedToAssign(List<Encounter> selectedToAssign) {
        this.selectedToAssign = selectedToAssign;
    }

    public List<Area> completeMohsPerDistrict(String qry) {
        return areaController.getMohAreasOfADistrict(areaController.getAreaByName(webUserController.getLoggedUser().getArea().toString(), AreaType.District, false, null));
    }

    public List<ClientEncounterComponentItem> getCecItems() {
        return cecItems;
    }

    public void setCecItems(List<ClientEncounterComponentItem> cecItems) {
        this.cecItems = cecItems;
    }

    public List<ClientEncounterComponentItem> getSelectedCecis() {
        return selectedCecis;
    }

    public void setSelectedCecis(List<ClientEncounterComponentItem> selectedCecis) {
        this.selectedCecis = selectedCecis;
    }

    public Area getMohArea() {
        return mohArea;
    }

    public void setMohArea(Area mohArea) {
        this.mohArea = mohArea;
    }

    public Item getManagementType() {
        return managementType;
    }

    public void setManagementType(Item managementType) {
        this.managementType = managementType;
    }

    public Area getPdhs() {
        return pdhs;
    }

    public void setPdhs(Area pdhs) {
        this.pdhs = pdhs;
    }

    public List<InstitutionTypeCount> getInstitutionTypeCounts() {
        return institutionTypeCounts;
    }

    public void setInstitutionTypeCounts(List<InstitutionTypeCount> institutionTypeCounts) {
        this.institutionTypeCounts = institutionTypeCounts;
    }

    public InstitutionType getInstitutionType() {
        return institutionType;
    }

    // Return the name of the current institution type

    public String getInstitutionTypeName() {
        return this.institutionType.name();
    }

    public void setInstitutionType(InstitutionType institutionType) {
        this.institutionType = institutionType;
    }

    public List<InstitutionPeformance> getInstitutionPeformances() {
        if (institutionPeformances == null) {
            institutionPeformances = new ArrayList<>();
        }
        return institutionPeformances;
    }

    public Area getRdhs() {
        return rdhs;
    }

    public void setRdhs(Area rdhs) {
        this.rdhs = rdhs;
    }

    public List<InstitutionPeformance> getInstitutionPeformancesFiltered() {
        if (institutionPeformancesFiltered == null) {
            institutionPeformancesFiltered = new ArrayList<>();
        }
        return institutionPeformancesFiltered;
    }

    public String toDispatchSamples() {
        Map m = new HashMap();
        String j = "select c "
                + " from Encounter c "
                + " where c.retired=:ret "
                + " and c.encounterType=:type "
                + " and c.encounterDate between :fd and :td "
                + " and c.institution=:ins ";
        if (testType != null) {
            j += " and c.pcrTestType=:tt ";
            m.put("tt", testType);
        }
        j += " and (c.sentToLab is null or c.sentToLab=:sl or c.referalInstitution is null) "
                + " order by c.id";

        m.put("ret", false);
        m.put("type", EncounterType.Test_Enrollment);
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        m.put("ins", institution);
        m.put("sl", false);
        listedToDispatch = encounterFacade.findByJpql(j, m, TemporalType.TIMESTAMP);
        return "/national/dispatch_samples";
    }

    public void setInstitutionPeformancesFiltered(List<InstitutionPeformance> institutionPeformancesFiltered) {
        this.institutionPeformancesFiltered = institutionPeformancesFiltered;
    }

    public void generateInstitutionPeformanceSummery() {
        Long p = 0l;
        Long r = 0l;
        Long pp = 0l;
        Long rp = 0l;
        if (getInstitutionPeformances() != null && !getInstitutionPeformances().isEmpty()) {
            for (InstitutionPeformance ip : getInstitutionPeformances()) {
                if (ip.getPcrPositives() != null) {
                    pp += ip.getPcrPositives();
                }
                if (ip.getRatPositives() != null) {
                    rp += ip.getRatPositives();
                }
                if (ip.getPcrs() != null) {
                    p += ip.getPcrs();
                }
                if (ip.getRats() != null) {
                    r += ip.getRats();
                }
            }
        }
        getInstitutionPeformancesSummery().setId(0l);
        getInstitutionPeformancesSummery().setPcrPositives(pp);
        getInstitutionPeformancesSummery().setPcrs(p);
        getInstitutionPeformancesSummery().setRats(r);
        getInstitutionPeformancesSummery().setRatPositives(rp);
    }

    public void generateFilteredInstitutionPeformanceSummery() {
        Long p = 0l;
        Long r = 0l;
        Long pp = 0l;
        Long rp = 0l;
        if (getInstitutionPeformancesFiltered() != null && !getInstitutionPeformancesFiltered().isEmpty()) {
            for (InstitutionPeformance ip : getInstitutionPeformancesFiltered()) {
                if (ip.getPcrPositives() != null) {
                    pp += ip.getPcrPositives();
                }
                if (ip.getRatPositives() != null) {
                    rp += ip.getRatPositives();
                }
                if (ip.getPcrs() != null) {
                    p += ip.getPcrs();
                }
                if (ip.getRats() != null) {
                    r += ip.getRats();
                }
            }
        }
        getInstitutionPeformancesSummery().setId(0l);
        getInstitutionPeformancesSummery().setPcrPositives(pp);
        getInstitutionPeformancesSummery().setPcrs(p);
        getInstitutionPeformancesSummery().setRats(r);
        getInstitutionPeformancesSummery().setRatPositives(rp);

    }

    public InstitutionPeformance getInstitutionPeformancesSummery() {
        if (institutionPeformancesSummery == null) {
            institutionPeformancesSummery = new InstitutionPeformance();
        }
        return institutionPeformancesSummery;
    }

    public void setInstitutionPeformancesSummery(InstitutionPeformance institutionPeformancesSummery) {
        this.institutionPeformancesSummery = institutionPeformancesSummery;
    }

    public List<InstitutionCount> getLabSummariesToReceive() {
        return labSummariesToReceive;
    }

    public void setLabSummariesToReceive(List<InstitutionCount> labSummariesToReceive) {
        this.labSummariesToReceive = labSummariesToReceive;
    }

    public List<InstitutionCount> getAwaitingDispatch() {
        return awaitingDispatch;
    }

    public void setAwaitingDispatch(List<InstitutionCount> awaitingDispatch) {
        this.awaitingDispatch = awaitingDispatch;
    }

    public List<InstitutionCount> getAwaitingReceipt() {
        return awaitingReceipt;
    }

    public void setAwaitingReceipt(List<InstitutionCount> awaitingReceipt) {
        this.awaitingReceipt = awaitingReceipt;
    }

    public List<InstitutionCount> getAwaitingResults() {
        return awaitingResults;
    }

    public void setAwaitingResults(List<InstitutionCount> awaitingResults) {
        this.awaitingResults = awaitingResults;
    }

    public List<InstitutionCount> getResultsAvailable() {
        return resultsAvailable;
    }

    public void setResultsAvailable(List<InstitutionCount> resultsAvailable) {
        this.resultsAvailable = resultsAvailable;
    }

    public List<Encounter> getListedToDispatch() {
        return listedToDispatch;
    }

    public void setListedToDispatch(List<Encounter> listedToDispatch) {
        this.listedToDispatch = listedToDispatch;
    }

    public List<Encounter> getListedToDivert() {
        return listedToDivert;
    }

    public void setListedToDivert(List<Encounter> listedToDivert) {
        this.listedToDivert = listedToDivert;
    }

    public String getFilter() {
        return this.filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public List<Encounter> getSelectedToDivert() {
        return selectedToDivert;
    }

    public void setSelectedToDivert(List<Encounter> selectedToDivert) {
        this.selectedToDivert = selectedToDivert;
    }

    public List<Encounter> getSelectedToDispatch() {
        return selectedToDispatch;
    }

      public String dispatchSelectedSamples() {
        if (dispatchingLab == null) {
            JsfUtil.addErrorMessage("Please select a lab to send samples");
            return "";
        }
        for (Encounter e : selectedToDispatch) {
            e.setSentToLab(true);
            e.setSentToLabAt(new Date());
            e.setSentToLabBy(webUserController.getLoggedUser());
            e.setReferalInstitution(dispatchingLab);
            encounterFacade.edit(e);
        }
        selectedToDispatch = null;
        return toDispatchSamples();
    }

    public void setSelectedToDispatch(List<Encounter> selectedToDispatch) {
        this.selectedToDispatch = selectedToDispatch;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public Institution getReferingInstitution() {
        return referingInstitution;
    }

    public void setReferingInstitution(Institution referingInstitution) {
        this.referingInstitution = referingInstitution;
    }

    public Institution getDispatchingLab() {
        return dispatchingLab;
    }

    public void setDispatchingLab(Institution dispatchingLab) {
        this.dispatchingLab = dispatchingLab;
    }

    public Institution getDivertingLab() {
        return divertingLab;
    }

    public void setDivertingLab(Institution divertingLab) {
        this.divertingLab = divertingLab;
    }

    public String getSearchingName() {
        return searchingName;
    }

    public void setSearchingName(String searchingName) {
        this.searchingName = searchingName;
    }



}
