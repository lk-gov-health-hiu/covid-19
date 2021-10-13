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
import lk.gov.health.phsp.facade.ClientEncounterComponentItemFacade;
import lk.gov.health.phsp.facade.InstitutionFacade;
import lk.gov.health.phsp.pojcs.InstitutionCount;
import lk.gov.health.phsp.pojcs.InstitutionPeformance;
import lk.gov.health.phsp.pojcs.InstitutionTypeCount;
import lk.gov.health.phsp.pojcs.LabSummary;
// </editor-fold>

/**
 *
 * @author buddhika
 */
@Named
@SessionScoped
public class LabController implements Serializable {
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
    private EncounterController encounterController;
    @Inject
    private ItemController itemController;
    @Inject
    private ItemApplicationController itemApplicationController;
    @Inject
    private InstitutionController institutionController;
    @Inject
    private CommonController commonController;
    @Inject
    private AreaController areaController;
    @Inject
    private UserTransactionController userTransactionController;
    @Inject
    private PreferenceController preferenceController;
// </editor-fold>

// <editor-fold defaultstate="collapsed" desc="Variables">
    private Institution dispatchingLab;
    private Boolean nicExistsForPcr;
    private Boolean nicExistsForRat;
    private Encounter rat;
    private Encounter pcr;
    private Encounter covidCase;
    private Encounter test;
    private Encounter deleting;
    private Institution institution;
    private Institution referingInstitution;

    private List<LabSummary> labSummaries;
    private LabSummary labSummary;

    private String searchingName;
    private String searchingBhtno;
    private String searchingLabNo;
    private String searchingTestId;
    private String searchingTestNo;

    private String encryptedId;

    private Area pdhs;
    private Area rdhs;
    private InstitutionType institutionType;

    private WebUser assignee;

    private List<Encounter> tests;
    private List<Encounter> testList;
    private List<ClientEncounterComponentItem> cecItems;
    private List<ClientEncounterComponentItem> selectedCecis;
    private List<Encounter> selectedToAssign;
    private List<Encounter> listedToDispatch;
    private List<Encounter> selectedToDispatch;
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

    private Area district;
    private Area mohArea;
    private String phoneNumber;
    private boolean clientViewReportDisplayError = true;
    private boolean clientViewReportDisplayCheck = false;
    private boolean clientViewReportDisplay = false;

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Constructors">
    public LabController() {
    }
// </editor-fold>

// <editor-fold defaultstate="collapsed" desc="Functions">
    public String toDispatchSamplesWithReferringLab() {
        String j = "select c "
                + " from Encounter c "
                + " where c.retired=:ret "
                + " and c.encounterType=:type "
                + " and c.encounterDate between :fd and :td "
                + " and c.institution=:ins "
                + " and c.referalInstitution=:rins "
                + " and c.sentToLab is null "
                + " order by c.id";
        Map m = new HashMap();
        m.put("ret", false);
        m.put("type", EncounterType.Test_Enrollment);
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        m.put("ins", institution);
        m.put("rins", referingInstitution);
        listedToDispatch = getEncounterFacade().findByJpql(j, m, TemporalType.DATE);
        return "/lab/dispatch_samples";
    }

    public String searchByName() {
        if (searchingName == null && searchingName.trim().equals("")) {
            JsfUtil.addErrorMessage("Please enter a name to search");
            return "";
        }

        if (searchingName.length() < 5) {
            JsfUtil.addErrorMessage("Please enter at least 4 characters to serach");
            return "";
        }

        Map hashmap = new HashMap<>();
        String jpql = "select e from Encounter e where e.retired=:retired";
        hashmap.put("retired", false);

        jpql += " and e.referalInstitution in :ref";
        hashmap.put("ref", webUserController.getLoggableInstitutions());

        jpql += " and e.encounterType=:etype";
        hashmap.put("etype", EncounterType.Test_Enrollment);

        jpql += " and e.encounterDate between :fd and :td";
        hashmap.put("fd", this.fromDate);
        hashmap.put("td", this.toDate);

        jpql += " and e.client.person.name like :name";
        hashmap.put("name", "%" + searchingName.toLowerCase() + "%");

        jpql += " order by e.id";

        testList = encounterFacade.findByJpql(jpql, hashmap, TemporalType.DATE);
        return "/lab/search";

    }

    public String searchByTestNo() {
        if (this.searchingTestNo == null || this.searchingTestNo.length() == 0) {
            JsfUtil.addErrorMessage("Please enter a valid test number");
            return "";
        }

        this.searchingBhtno = null;
        this.searchingLabNo = null;
        this.searchingTestId = null;
        this.searchingName = null;

        Map hashmap = new HashMap<>();
        String jpql = "select e from Encounter e where e.retired=:retired";

        jpql += " and e.referalInstitution in :ref";
        hashmap.put("retired", false);

        hashmap.put("ref", webUserController.getLoggableInstitutions());

        jpql += " and e.encounterNumber=:eno";
        hashmap.put("eno", this.searchingTestNo.toUpperCase());

        jpql += " and e.encounterType=:etype";
        hashmap.put("etype", EncounterType.Test_Enrollment);

        jpql += " and e.createdAt between :fd and :td";
        hashmap.put("fd", fromDate);
        hashmap.put("td", toDate);

        jpql += " order by e.encounterNumber";

        testList = encounterFacade.findByJpql(jpql, hashmap, TemporalType.TIMESTAMP);
        System.out.println(testList);
        return "/lab/search";
    }

    public String searchByBhtNo() {
        if (this.searchingBhtno == null || this.searchingBhtno.trim().length() == 0) {
            JsfUtil.addErrorMessage("Please enter a valid BHT number");
            return "";
        }
        this.searchingLabNo = null;
        this.searchingTestId = null;
        this.searchingTestNo = null;
        this.searchingName = null;

        Map hashmap = new HashMap<>();
        String jpql = "select e from Encounter e where e.retired=:retired";
        hashmap.put("retired", false);

        jpql += " and e.referalInstitution in :ref";
        hashmap.put("ref", webUserController.getLoggableInstitutions());

        jpql += " and e.encounterType=:etype";
        hashmap.put("etype", EncounterType.Test_Enrollment);

        jpql += " and e.createdAt between :fd and :td";
        hashmap.put("fd", this.fromDate);
        hashmap.put("td", this.toDate);

        jpql += " and e.bht like :bht";
        hashmap.put("bht", "%" + this.searchingBhtno.toLowerCase() + "%");

        jpql += " order by e.bht";

        testList = encounterFacade.findByJpql(jpql, hashmap, TemporalType.TIMESTAMP);
        System.out.println(testList);
        return "/lab/search";
    }

    public String searchByLabNo() {
        if (this.searchingLabNo == null || this.searchingLabNo.trim().length() == 0) {
            JsfUtil.addErrorMessage("Please enter a valid lab number");
            return "";
        }

        this.searchingBhtno = null;
        this.searchingTestId = null;
        this.searchingTestNo = null;
        this.searchingName = null;

        Map hashmap = new HashMap<>();
        String jpql = "select e from Encounter e where e.retired=:retired";
        hashmap.put("retired", false);

        jpql += " and e.referalInstitution in :ref";
        hashmap.put("ref", webUserController.getLoggableInstitutions());

        jpql += " and e.encounterType=:etype";
        hashmap.put("etype", EncounterType.Test_Enrollment);

        jpql += " and e.createdAt between :fd and :td";
        hashmap.put("fd", this.fromDate);
        hashmap.put("td", this.toDate);

        jpql += " and e.labNumber like :labNo";
        hashmap.put("labNo", "%" + this.searchingLabNo.toLowerCase() + "%");

        jpql += " order by e.bht";

        testList = encounterFacade.findByJpql(jpql, hashmap, TemporalType.TIMESTAMP);
        System.out.println(testList);
        return "/lab/search";
    }

    public String toLabSummeries() {
        processLabSummary();
        return "/lab/summary";
    }

    public String toDispatchFromActionSummary() {
        if (labSummary == null || labSummary.getInstitution() == null) {
            JsfUtil.addErrorMessage("Lab is not selected");
            return "";
        }
        institution = labSummary.getInstitution();
        return toDispatchSamples();
    }

    public String toReceiveFromActionSummary() {
        if (labSummary == null || labSummary.getInstitution() == null) {
            JsfUtil.addErrorMessage("Lab is not selected");
            return "";
        }
        clientController.setFromDate(fromDate);
        clientController.setToDate(toDate);
        clientController.setInstitution(labSummary.getInstitution());
        return clientController.toLabReceiveSelected();
    }

    public String toEnterFromActionSummary() {
        if (labSummary == null || labSummary.getInstitution() == null) {
            JsfUtil.addErrorMessage("Lab is not selected");
            return "";
        }
        clientController.setFromDate(fromDate);
        clientController.setToDate(toDate);
        clientController.setInstitution(labSummary.getInstitution());
        return clientController.toLabEnterResults();
    }

    public String toReviewFromActionSummary() {
        if (labSummary == null || labSummary.getInstitution() == null) {
            JsfUtil.addErrorMessage("Lab is not selected");
            return "";
        }
        clientController.setFromDate(fromDate);
        clientController.setToDate(toDate);
        clientController.setInstitution(labSummary.getInstitution());
        return clientController.toLabReviewResults();
    }

    public String toConfirmFromActionSummary() {
        if (labSummary == null || labSummary.getInstitution() == null) {
            JsfUtil.addErrorMessage("Lab is not selected");
            return "";
        }
        clientController.setFromDate(fromDate);
        clientController.setToDate(toDate);
        clientController.setInstitution(labSummary.getInstitution());
        return clientController.toConfirmResults();
    }

    public void processLabSummary() {
        labSummaries = new ArrayList<>();
        String j;
        Map m = new HashMap();

        //Institutions
        j = "select i "
                + " from Encounter e join e.institution i "
                + " where (e.retired is null or e.retired=:pf) "
                + " and e.createdAt between :fd and :td "
                + " and e.referalInstitution in :rins "
                + " and (e.sampleRejectedAtLab is null or e.sampleRejectedAtLab=:pf) "
                + " and (e.sampleMissing is null or e.sampleMissing=:pf) "
                + " group by i"
                + " order by i.name";
        m = new HashMap();
        m.put("pf", false);
        m.put("rins", webUserController.getLoggableInstitutions());
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        List<Institution> tins = institutionFacade.findByJpql(j, m, TemporalType.TIMESTAMP);
        for (Institution ti : tins) {
            LabSummary ls = new LabSummary();

            ls.setInstitution(ti);

            m = new HashMap();
            m.put("rins", webUserController.getLoggableInstitutions());
            m.put("ins", ti);

            m.put("pf", false);
            m.put("ins", ti);
            m.put("fd", getFromDate());
            m.put("td", getToDate());

            j = "select count(e) "
                    + " from Encounter e "
                    + " where  (e.retired is null or e.retired=:pf) "
                    + " and e.createdAt between :fd and :td "
                    + " and e.referalInstitution in :rins "
                    + " and e.institution=:ins "
                    + " and (e.sampleRejectedAtLab is null or e.sampleRejectedAtLab=:pf) "
                    + " and (e.sampleMissing is null or e.sampleMissing=:pf) "
                    + " and (e.sentToLab is null or e.sentToLab=:pf) ";

            ls.setToDispatch(encounterFacade.countByJpql(j, m, TemporalType.TIMESTAMP));

            m.put("pt", true);

            j = "select count(e) "
                    + " from Encounter e "
                    + " where  (e.retired is null or e.retired=:pf) "
                    + " and e.createdAt between :fd and :td "
                    + " and e.referalInstitution in :rins "
                    + " and e.institution=:ins "
                    + " and e.sentToLab=:pt "
                    + " and (e.sampleRejectedAtLab is null or e.sampleRejectedAtLab=:pf) "
                    + " and (e.sampleMissing is null or e.sampleMissing=:pf) "
                    + " and (e.receivedAtLab is null or e.receivedAtLab=:pf) ";
            ls.setToReceive(encounterFacade.countByJpql(j, m, TemporalType.TIMESTAMP));

            j = "select count(e) "
                    + " from Encounter e "
                    + " where  (e.retired is null or e.retired=:pf) "
                    + " and e.createdAt between :fd and :td "
                    + " and e.referalInstitution in :rins "
                    + " and e.institution=:ins "
                    + " and e.receivedAtLab=:pt "
                    + " and (e.sampleRejectedAtLab is null or e.sampleRejectedAtLab=:pf) "
                    + " and (e.sampleMissing is null or e.sampleMissing=:pf) "
                    + " and (e.resultEntered is null or e.resultEntered=:pf) ";
            ls.setToEnterData(encounterFacade.countByJpql(j, m, TemporalType.TIMESTAMP));

            j = "select count(e) "
                    + " from Encounter e "
                    + " where  (e.retired is null or e.retired=:pf) "
                    + " and e.createdAt between :fd and :td "
                    + " and e.referalInstitution in :rins "
                    + " and e.institution=:ins "
                    + " and e.resultEntered=:pt "
                    + " and (e.sampleRejectedAtLab is null or e.sampleRejectedAtLab=:pf) "
                    + " and (e.sampleMissing is null or e.sampleMissing=:pf) "
                    + " and (e.resultReviewed is null or e.resultReviewed=:pf) ";
            ls.setToReview(encounterFacade.countByJpql(j, m, TemporalType.TIMESTAMP));

            j = "select count(e) "
                    + " from Encounter e "
                    + " where  (e.retired is null or e.retired=:pf) "
                    + " and e.createdAt between :fd and :td "
                    + " and e.referalInstitution in :rins "
                    + " and e.institution=:ins "
                    + " and e.resultReviewed=:pt "
                    + " and (e.sampleRejectedAtLab is null or e.sampleRejectedAtLab=:pf) "
                    + " and (e.sampleMissing is null or e.sampleMissing=:pf) "
                    + " and (e.resultConfirmed is null or e.resultConfirmed=:pf) ";
            ls.setToConfirm(encounterFacade.countByJpql(j, m, TemporalType.TIMESTAMP));

            labSummaries.add(ls);
        }
    }

    public void prepareToViewClientReport() {
        clientViewReportDisplayError = true;
        clientViewReportDisplayCheck = false;
        clientViewReportDisplay = false;
        if (encryptedId == null || encryptedId.trim().equals("")) {
            JsfUtil.addErrorMessage("Wrong Data");
            return;
        }
        System.out.println("encryptedId = " + encryptedId);
        String decryptedId = CommonController.decrypt(encryptedId);
        System.out.println("decryptedId = " + decryptedId);
        if (decryptedId == null) {
            encryptedId = encryptedId.replaceAll("\\s+", "+");
        }
        System.out.println("new encryptedId = " + encryptedId);
        decryptedId = CommonController.decrypt(encryptedId);
        System.out.println("new decryptedId = " + decryptedId);
        Long id = CommonController.getLongValue(decryptedId);
        System.out.println("id = " + id);
        if (id == null) {
            JsfUtil.addErrorMessage("Wrong Data.");
            return;
        }
        test = encounterController.getEncounter(id);
        if (test == null) {
            JsfUtil.addErrorMessage("Wrong Data..");
            return;
        }
        clientViewReportDisplayError = false;
        clientViewReportDisplayCheck = true;
        clientViewReportDisplay = false;
    }

    public String viewClientReport() {
        clientViewReportDisplayError = false;
        clientViewReportDisplayCheck = true;
        clientViewReportDisplay = false;
        if (phoneNumber == null || phoneNumber.trim().equals("")) {
            JsfUtil.addErrorMessage("Please give a phone number");
            return "";
        }
        if (test == null || test.getClient() == null || test.getClient().getPerson() == null || test.getClient().getPerson().getPhone1() == null) {
            JsfUtil.addErrorMessage("No such Record Found.");
            return "";
        }
        String p1 = phoneNumber.replaceAll("\\s+", "");
        p1 = p1.replace("+", "");

        String p2 = test.getClient().getPerson().getPhone1().replaceAll("\\s+", "");
        p2 = p2.replace("+", "");

        if (p1.length() < 9 || p2.length() < 9) {
            JsfUtil.addErrorMessage("Wrong Phone Number.");
            return "";
        }
        if (p1.length() > 13 || p2.length() > 13) {
            JsfUtil.addErrorMessage("Wrong Phone Number.");
            return "";
        }

        if (p1.length() == p2.length()) {
            if (p1.equalsIgnoreCase(p2)) {
                clientViewReportDisplayError = false;
                clientViewReportDisplayCheck = false;
                clientViewReportDisplay = true;
                return "";
            }
        } else if (p1.length() > p2.length()) {
            if (p1.contains(p2)) {
                clientViewReportDisplayError = false;
                clientViewReportDisplayCheck = false;
                clientViewReportDisplay = true;
                return "";
            }
        } else if (p1.length() < p2.length()) {
            if (p2.contains(p1)) {
                clientViewReportDisplayError = false;
                clientViewReportDisplayCheck = false;
                clientViewReportDisplay = true;
                return "";
            }
        }
        JsfUtil.addErrorMessage("No match");
        return "";
    }

    public String toCountOfResultsByOrderedInstitution() {
        Map m = new HashMap();
        String j = "select new lk.gov.health.phsp.pojcs.InstitutionCount(c.institution, count(c))   "
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

        j += " and c.referalInstitution=:ri ";
        m.put("ri", webUserController.getLoggedInstitution());

        j += " group by c.institution"
                + " order by count(c) desc ";

        institutionCounts = new ArrayList<>();

        List<Object> objCounts = encounterFacade.findAggregates(j, m, TemporalType.TIMESTAMP);
        if (objCounts == null || objCounts.isEmpty()) {
            return "/lab/count_of_results_by_ordered_institution";
        }
        for (Object o : objCounts) {
            if (o instanceof InstitutionCount) {
                InstitutionCount ic = (InstitutionCount) o;
                institutionCounts.add(ic);
            }
        }
        return "/lab/count_of_results_by_ordered_institution";
    }

    public String toCountOfTestsByPdhs() {
        Map m = new HashMap();

        String j = "select new lk.gov.health.phsp.pojcs.InstitutionCount(c.institution.pdhsArea, count(c))   "
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
        j += " and c.referalInstitution=:ri ";
        m.put("ri", webUserController.getLoggedInstitution());

        j += " group by c.institution.pdhsArea"
                + " order by count(c) desc ";

        institutionCounts = new ArrayList<>();
        List<Object> objCounts = encounterFacade.findAggregates(j, m, TemporalType.TIMESTAMP);

        if (objCounts == null || objCounts.isEmpty()) {
            return "/lab/count_of_tests_by_pdhs";
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

        j += " and c.referalInstitution=:ri ";
        m.put("ri", webUserController.getLoggedInstitution());

        Long nullCounts = encounterFacade.findAggregateLong(j, m, TemporalType.TIMESTAMP);
        if (nullCounts != null) {
            InstitutionCount ic = new InstitutionCount();
            Area a = new Area();
            a.setName("Non RDHS");
            ic.setArea(a);
            ic.setCount(nullCounts);
            institutionCounts.add(ic);
        }
        return "/lab/count_of_tests_by_pdhs";
    }

    public String toCountOfTestsByRdhsWithoutSpecifyingPdhs() {
        pdhs = null;
        return toCountOfTestsByRdhs();
    }

    public String toCountOfTestsByInstitutionType() {
        Map m = new HashMap();

        String j = "select new lk.gov.health.phsp.pojcs.InstitutionTypeCount(c.institution.institutionType, count(c))   "
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
        j += " and c.referalInstitution=:ri ";
        m.put("ri", webUserController.getLoggedInstitution());

        j += " group by c.institution.institutionType"
                + " order by count(c) desc ";

        institutionTypeCounts = new ArrayList<>();
        List<Object> objCounts = encounterFacade.findAggregates(j, m, TemporalType.TIMESTAMP);

        if (objCounts == null || objCounts.isEmpty()) {
            return "/lab/count_of_tests_by_institution_type";
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

        j += " and c.referalInstitution=:ri ";
        m.put("ri", webUserController.getLoggedInstitution());

        Long nullCounts = encounterFacade.findAggregateLong(j, m, TemporalType.TIMESTAMP);
        if (nullCounts != null) {
            InstitutionTypeCount ic = new InstitutionTypeCount();
            ic.setType(null);
            ic.setCount(nullCounts);
            institutionTypeCounts.add(ic);
        }
        return "/lab/count_of_tests_by_institution_type";
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

        j += " and c.referalInstitution=:ri ";
        m.put("ri", webUserController.getLoggedUser());

        j += " group by c.institution"
                + " order by count(c) desc ";

        institutionCounts = new ArrayList<>();

        List<Object> objCounts = encounterFacade.findAggregates(j, m, TemporalType.TIMESTAMP);
        if (objCounts == null || objCounts.isEmpty()) {
            return "/lab/count_of_tests_by_ordered_institution_without_rdhs";
        }
        for (Object o : objCounts) {
            if (o instanceof InstitutionCount) {
                InstitutionCount ic = (InstitutionCount) o;
                institutionCounts.add(ic);
            }
        }

        return "/lab/count_of_tests_by_ordered_institution_without_rdhs";
    }

    public String toCountOfTestsByRdhs() {
        Map m = new HashMap();

        String j = "select new lk.gov.health.phsp.pojcs.InstitutionCount(c.institution.rdhsArea, count(c))   "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);

        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);

        j += " and (c.createdAt > :fd and c.createdAt < :td) ";
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

        j += " and c.referalInstitution=:ri ";
        m.put("ri", webUserController.getLoggedInstitution());

        j += " group by c.institution.rdhsArea "
                + " order by count(c) desc ";

        institutionCounts = new ArrayList<>();

        List<Object> objCounts = encounterFacade.findAggregates(j, m, TemporalType.TIMESTAMP);

        if (objCounts == null || objCounts.isEmpty()) {
            return "/lab/count_of_tests_by_rdhs";
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
        j += " and (c.createdAt > :fd and c.createdAt < :td) ";
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
        j += " and c.referalInstitution=:ri ";
        m.put("ri", webUserController.getLoggedInstitution());
        Long nullCounts = encounterFacade.findAggregateLong(j, m, TemporalType.TIMESTAMP);
        if (nullCounts != null) {
            InstitutionCount ic = new InstitutionCount();
            Area a = new Area();
            a.setName("No RDHS");
            ic.setArea(a);
            ic.setCount(nullCounts);
            institutionCounts.add(ic);
        }
        return "/lab/count_of_tests_by_rdhs";
    }

    public String toLabOrderByReferringInstitution() {
        referingInstitution = webUserController.getLoggedInstitution();
        Map m = new HashMap();
        String j = "select c "
                + " from Encounter c "
                + " where c.retired<>:ret "
                + " and c.encounterType=:type "
                + " and c.encounterDate between :fd and :td ";
        if (institution != null) {
            j += " and c.institution=:ins ";
            m.put("ins", institution);
        }
        j += " and c.referalInstitution=:rins"
                + " order by c.encounterNumber";
        m.put("ret", true);
        m.put("type", EncounterType.Test_Enrollment);
        m.put("fd", fromDate);
        m.put("td", toDate);
        m.put("rins", referingInstitution);
        testList = encounterFacade.findByJpql(j, m, TemporalType.DATE);
        return "/lab/order_list";
    }

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

        j += " and c.referalInstitution=:ri ";
        m.put("ri", webUserController.getLoggedInstitution());

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
            return "/lab/count_of_tests_by_ordered_institution";
        }
        for (Object o : objCounts) {
            if (o instanceof InstitutionCount) {
                InstitutionCount ic = (InstitutionCount) o;
                institutionCounts.add(ic);
            }
        }

        return "/lab/count_of_tests_by_ordered_institution";
    }

    public String toCountOfTestsByOrderedInstitution() {
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

        j += " and c.referalInstitution=:ri ";
        m.put("ri", webUserController.getLoggedInstitution());

        if (institutionType != null) {
            j += " and c.institution.institutionType=:it ";
            m.put("it", institutionType);
        }

        j += " group by c.institution"
                + " order by count(c) desc ";

        institutionCounts = new ArrayList<>();

        List<Object> objCounts = encounterFacade.findAggregates(j, m, TemporalType.TIMESTAMP);
        if (objCounts == null || objCounts.isEmpty()) {
            return "/lab/count_of_tests_by_ordered_institution";
        }
        for (Object o : objCounts) {
            if (o instanceof InstitutionCount) {
                InstitutionCount ic = (InstitutionCount) o;
                institutionCounts.add(ic);
            }
        }

        return "/lab/count_of_tests_by_ordered_institution";
    }

    public String toDispatchSamplesByMohOrHospital() {
        String j = "select c "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret ) "
                + " and c.encounterType=:type "
                + " and c.encounterDate between :fd and :td "
                + " and c.institution=:ins "
                + " and (c.sentToLab is null or c.sentToLab=:stl) "
                + " order by c.id";
        Map m = new HashMap();
        m.put("ret", false);
        m.put("type", EncounterType.Test_Enrollment);
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        m.put("stl", false);
        m.put("ins", webUserController.getLoggedInstitution());
        listedToDispatch = encounterFacade.findByJpql(j, m, TemporalType.DATE);
        return "/lab/dispatch_samples";
    }

    public String toDispatchSamples() {
        String j = "select c "
                + " from Encounter c "
                + " where c.retired=:ret "
                + " and c.encounterType=:type "
                + " and c.encounterDate between :fd and :td "
                + " and c.institution=:ins "
                + " and c.referalInstitution in :rins "
                + " and c.sentToLab is null "
                + " order by c.id";
        Map m = new HashMap();
        m.put("ret", false);
        m.put("type", EncounterType.Test_Enrollment);
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        m.put("ins", institution);
        m.put("rins", webUserController.getLoggableInstitutions());
        listedToDispatch = encounterFacade.findByJpql(j, m, TemporalType.DATE);
        return "/lab/dispatch_samples";
    }

    public String toAssignInvestigation() {
        testType = itemApplicationController.getPcr();
        result = itemApplicationController.getPcrPositive();

        System.out.println("toTestList");
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

        return "/lab/assign_investigation";
    }

    public String toStartInvestigation() {
        return "/lab/start_investigation";
    }

    public String toViewInvestigatedCases() {
        return "/lab/view_investigated_cases";
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
        return "/lab/pcr_positive_links";
    }

    public String toLabReportsIndexNational() {
        fromDate = CommonController.startOfTheDate();
        toDate = CommonController.endOfTheDate();
        return "/lab/lab_report_links";
    }

    public String toResultList() {
        fromDate = CommonController.startOfTheDate();
        toDate = CommonController.endOfTheDate();
        return "/lab/lab_report_links";
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
        return "/lab/result_list_pcr_positive";
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
        return "/lab/pcr_positive_counts_by_district";
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
        return "/lab/pcr_positive_counts_by_institution_district";
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
        return "/lab/pcr_positive_counts_by_ordered_institution";
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
        return "/lab/pcr_positive_counts_by_lab";
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
    
    public String toEditTestSingle() {
        if (test == null) {
            JsfUtil.addErrorMessage("Nothing to Edit");
            return "";
        }
        return "/lab/edit_single_text";
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

    public String toListOfTests() {
        return "/lab/list_of_tests";
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
        return "/lab/order_tests_for_moh";
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
        return "/lab/investigated_list";
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
        return toTestList();
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
        return "/lab/rat_view";
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
        return "/lab/rat_order_view";
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
        return "/lab/rat_result_view";
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
        return "/lab/pcr_result_view";
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
        return "/lab/pcr_view";
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
        return "/lab/rat";
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
        return "/lab/rat_order";
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
        return "/lab/pcr";
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

    public String toAddNewRatWithNewClient() {
        rat = new Encounter();
        nicExistsForRat = null;
        Date d = new Date();
        Client c = new Client();
        c.getPerson().setDistrict(webUserController.getLoggedInstitution().getDistrict());
        c.getPerson().setMohArea(webUserController.getLoggedInstitution().getMohArea());
        rat.setPcrTestType(itemApplicationController.getRat());
        rat.setPcrOrderingCategory(sessionController.getLastRatOrderingCategory());
        rat.setClient(c);

        if (sessionController.getLastInstitution() != null) {
            rat.setInstitution(sessionController.getLastInstitution());
        } else {
            if (webUserController.getLoggedInstitution().getParent() != null) {
                rat.setInstitution(webUserController.getLoggedInstitution().getParent());
            } else {
                rat.setInstitution(webUserController.getLoggedInstitution());
            }
        }
        rat.setInstitutionUnit(sessionController.getLastInstitutionUnit());
        rat.setCreatedInstitution(webUserController.getLoggedInstitution());
        rat.setReferalInstitution(webUserController.getLoggedInstitution());

        rat.setEncounterType(EncounterType.Test_Enrollment);
        rat.setEncounterDate(d);
        rat.setEncounterFrom(d);
        rat.setEncounterMonth(CommonController.getMonth(d));
        rat.setEncounterQuarter(CommonController.getQuarter(d));
        rat.setEncounterYear(CommonController.getYear(d));

        rat.setSampled(true);
        rat.setSampledAt(new Date());
        rat.setSampledBy(webUserController.getLoggedUser());

        rat.setResultConfirmed(Boolean.TRUE);
        rat.setResultConfirmedAt(d);
        rat.setResultConfirmedBy(webUserController.getLoggedUser());

        rat.setCreatedAt(new Date());
        return "/lab/rat";
    }

    public String toAddNewRatOrderWithNewClient() {
        rat = new Encounter();
        nicExistsForRat = null;
        Date d = new Date();
        Client c = new Client();
        c.getPerson().setDistrict(webUserController.getLoggedInstitution().getDistrict());
        c.getPerson().setMohArea(webUserController.getLoggedInstitution().getMohArea());
        rat.setPcrTestType(itemApplicationController.getRat());
        rat.setPcrOrderingCategory(sessionController.getLastRatOrderingCategory());
        rat.setClient(c);

        if (sessionController.getLastInstitution() != null) {
            rat.setInstitution(sessionController.getLastInstitution());
        } else {
            if (webUserController.getLoggedInstitution().getParent() != null) {
                rat.setInstitution(webUserController.getLoggedInstitution().getParent());
            } else {
                rat.setInstitution(webUserController.getLoggedInstitution());
            }
        }
        rat.setInstitutionUnit(sessionController.getLastInstitutionUnit());
        rat.setCreatedInstitution(webUserController.getLoggedInstitution());
        rat.setReferalInstitution(webUserController.getLoggedInstitution());

        rat.setEncounterType(EncounterType.Test_Enrollment);
        rat.setEncounterDate(d);
        rat.setEncounterFrom(d);
        rat.setEncounterMonth(CommonController.getMonth(d));
        rat.setEncounterQuarter(CommonController.getQuarter(d));
        rat.setEncounterYear(CommonController.getYear(d));
        rat.setSampled(true);
        rat.setSampledAt(new Date());
        rat.setSampledBy(webUserController.getLoggedUser());
        rat.setCreatedAt(new Date());
        return "/lab/rat_order";
    }

    public String toAddNewPcrWithNewClient() {
        pcr = new Encounter();
        nicExistsForPcr = null;
        Date d = new Date();
        Client c = new Client();
        c.getPerson().setDistrict(webUserController.getLoggedInstitution().getDistrict());
        c.getPerson().setMohArea(webUserController.getLoggedInstitution().getMohArea());
        pcr.setPcrTestType(itemApplicationController.getPcr());
        pcr.setPcrOrderingCategory(sessionController.getLastPcrOrdringCategory());
        pcr.setClient(c);
        if (sessionController.getLastInstitution() != null) {
            pcr.setInstitution(sessionController.getLastInstitution());
        } else {
            if (webUserController.getLoggedInstitution().getParent() != null) {
                pcr.setInstitution(webUserController.getLoggedInstitution().getParent());
            } else {
                pcr.setInstitution(webUserController.getLoggedInstitution());
            }
        }
        pcr.setInstitution(sessionController.getLastInstitution());
        pcr.setUnitWard(sessionController.getLastWardUnit());
        pcr.setInstitutionUnit(sessionController.getLastInstitutionUnit());
        pcr.setCreatedInstitution(webUserController.getLoggedInstitution());
        pcr.setReferalInstitution(webUserController.getLoggedInstitution());
        pcr.setEncounterType(EncounterType.Test_Enrollment);
        pcr.setEncounterDate(d);
        pcr.setEncounterFrom(d);
        pcr.setEncounterMonth(CommonController.getMonth(d));
        pcr.setEncounterQuarter(CommonController.getQuarter(d));
        pcr.setEncounterYear(CommonController.getYear(d));
        pcr.setSampled(true);
        pcr.setSampledAt(new Date());
        pcr.setSampledBy(webUserController.getLoggedUser());
        pcr.setSentToLab(true);
        pcr.setSentToLabAt(new Date());
        pcr.setSentToLabBy(webUserController.getLoggedUser());
        pcr.setCreatedAt(new Date());
        return "/lab/pcr";
    }

    public String toAddNewPcrWithExistingNic() {
        if (pcr == null) {
            return "";
        }
        if (pcr.getClient() == null) {
            return "";
        }
        if (pcr.getClient().getPerson() == null) {
            return "";
        }
        if (pcr.getClient().getPerson().getNic() == null || pcr.getClient().getPerson().getNic().trim().equals("")) {
            return "";
        }
        Client nicClient = lastClientWithNic(pcr.getClient().getPerson().getNic(), pcr.getClient());
        if (nicClient == null) {
            return "";
        }
        nicExistsForPcr = null;
        Encounter tmpEnc = pcr;
        pcr = new Encounter();
        pcr.setEncounterNumber(tmpEnc.getEncounterNumber());
        Date d = new Date();
        Client c = nicClient;
        c.getPerson().setDistrict(webUserController.getLoggedInstitution().getDistrict());
        c.getPerson().setMohArea(webUserController.getLoggedInstitution().getMohArea());
        pcr.setPcrTestType(itemApplicationController.getPcr());
        pcr.setPcrOrderingCategory(sessionController.getLastPcrOrdringCategory());
        pcr.setClient(c);
        if (sessionController.getLastInstitution() != null) {
            pcr.setInstitution(sessionController.getLastInstitution());
        } else {
            if (webUserController.getLoggedInstitution().getParent() != null) {
                pcr.setInstitution(webUserController.getLoggedInstitution().getParent());
            } else {
                pcr.setInstitution(webUserController.getLoggedInstitution());
            }
        }
        pcr.setInstitutionUnit(sessionController.getLastInstitutionUnit());
        pcr.setCreatedInstitution(webUserController.getLoggedInstitution());
        pcr.setReferalInstitution(webUserController.getLoggedInstitution());
        pcr.setEncounterType(EncounterType.Test_Enrollment);
        pcr.setEncounterDate(d);
        pcr.setEncounterFrom(d);
        pcr.setEncounterMonth(CommonController.getMonth(d));
        pcr.setEncounterQuarter(CommonController.getQuarter(d));
        pcr.setEncounterYear(CommonController.getYear(d));
        pcr.setSampled(true);
        pcr.setSampledAt(new Date());
        pcr.setSampledBy(webUserController.getLoggedUser());
        pcr.setCreatedAt(new Date());
        return "/lab/pcr";
    }

    public String toAddNewRatOrderWithExistingNic() {
        if (rat == null) {
            return "";
        }
        if (rat.getClient() == null) {
            return "";
        }
        if (rat.getClient().getPerson() == null) {
            return "";
        }
        if (rat.getClient().getPerson().getNic() == null || rat.getClient().getPerson().getNic().trim().equals("")) {
            return "";
        }
        Client nicClient = lastClientWithNic(rat.getClient().getPerson().getNic(), rat.getClient());
        if (nicClient == null) {
            return "";
        }
        nicExistsForRat = null;
        Encounter tmpEnc = rat;
        rat = new Encounter();
        rat.setEncounterNumber(tmpEnc.getEncounterNumber());
        Date d = new Date();

        Client c = nicClient;
        c.getPerson().setDistrict(webUserController.getLoggedInstitution().getDistrict());
        c.getPerson().setMohArea(webUserController.getLoggedInstitution().getMohArea());
        rat.setPcrTestType(itemApplicationController.getRat());
        rat.setPcrOrderingCategory(sessionController.getLastRatOrderingCategory());
        rat.setClient(c);
        if (sessionController.getLastInstitution() != null) {
            rat.setInstitution(sessionController.getLastInstitution());
        } else {
            if (webUserController.getLoggedInstitution().getParent() != null) {
                rat.setInstitution(webUserController.getLoggedInstitution().getParent());
            } else {
                rat.setInstitution(webUserController.getLoggedInstitution());
            }
        }
        rat.setInstitutionUnit(sessionController.getLastInstitutionUnit());
        rat.setCreatedInstitution(webUserController.getLoggedInstitution());
        rat.setReferalInstitution(webUserController.getLoggedInstitution());
        rat.setEncounterType(EncounterType.Test_Enrollment);
        rat.setEncounterDate(d);
        rat.setEncounterFrom(d);
        rat.setEncounterMonth(CommonController.getMonth(d));
        rat.setEncounterQuarter(CommonController.getQuarter(d));
        rat.setEncounterYear(CommonController.getYear(d));
        rat.setSampled(true);
        rat.setSampledAt(new Date());
        rat.setSampledBy(webUserController.getLoggedUser());
        rat.setCreatedAt(new Date());
        return "/lab/rat_order";
    }

    public String toAddNewRatWithExistingNic() {
        if (rat == null) {
            return "";
        }
        if (rat.getClient() == null) {
            return "";
        }
        if (rat.getClient().getPerson() == null) {
            return "";
        }
        if (rat.getClient().getPerson().getNic() == null || rat.getClient().getPerson().getNic().trim().equals("")) {
            return "";
        }
        Client nicClient = lastClientWithNic(rat.getClient().getPerson().getNic(), rat.getClient());
        if (nicClient == null) {
            return "";
        }
        nicExistsForRat = null;
        Encounter tmpEnc = rat;
        rat = new Encounter();
        rat.setEncounterNumber(tmpEnc.getEncounterNumber());
        Date d = new Date();
        Client c = nicClient;
        c.getPerson().setDistrict(webUserController.getLoggedInstitution().getDistrict());
        c.getPerson().setMohArea(webUserController.getLoggedInstitution().getMohArea());
        rat.setPcrTestType(itemApplicationController.getRat());
        rat.setPcrOrderingCategory(sessionController.getLastRatOrderingCategory());
        rat.setClient(c);
        if (sessionController.getLastInstitution() != null) {
            rat.setInstitution(sessionController.getLastInstitution());
        } else {
            if (webUserController.getLoggedInstitution().getParent() != null) {
                rat.setInstitution(webUserController.getLoggedInstitution().getParent());
            } else {
                rat.setInstitution(webUserController.getLoggedInstitution());
            }
        }
        rat.setInstitutionUnit(sessionController.getLastInstitutionUnit());
        rat.setCreatedInstitution(webUserController.getLoggedInstitution());
        rat.setReferalInstitution(webUserController.getLoggedInstitution());
        rat.setEncounterType(EncounterType.Test_Enrollment);
        rat.setEncounterDate(d);
        rat.setEncounterFrom(d);
        rat.setEncounterMonth(CommonController.getMonth(d));
        rat.setEncounterQuarter(CommonController.getQuarter(d));
        rat.setEncounterYear(CommonController.getYear(d));

        rat.setSampled(true);
        rat.setSampledAt(new Date());
        rat.setSampledBy(webUserController.getLoggedUser());
        rat.setResultConfirmed(Boolean.TRUE);
        rat.setResultConfirmedAt(d);
        rat.setResultConfirmedBy(webUserController.getLoggedUser());

        rat.setCreatedAt(new Date());
        return "/lab/rat";
    }

    public String saveRatAndToNewRat() {
        if (saveRat() == null) {
            return "";
        }
        JsfUtil.addSuccessMessage("Ready to enter a new RAT");
        return toAddNewRatWithNewClient();
    }

    public String saveRatAndToNewRatOrder() {
        if (saveRat() == null) {
            return "";
        }
        JsfUtil.addSuccessMessage("Ready to enter a new RAT Order");
        return toAddNewRatOrderWithNewClient();
    }

    public String saveRatAndToRatView() {
        if (saveRat() != null) {
            return toRatView();
        } else {
            return "";
        }
    }

    public String saveRatAndToRatOrderView() {
        saveRat();
        return toRatView();
    }

    public String savePcrAndToNewPcr() {
        if (savePcr() != null) {
            return toAddNewPcrWithNewClient();
        } else {
            return "";
        }
    }

    public String savePcrAndToPcrView() {
        if (savePcr() != null) {
            return toPcrView();
        } else {
            return "";
        }
    }

    public String saveRat() {
        if (rat == null) {
            JsfUtil.addErrorMessage("No RAT to save");
            return "";
        }
        if (rat.getClient() == null) {
            JsfUtil.addErrorMessage("No Client to save");
            return "";
        }
        Institution createdIns = null;
        if (rat.getClient().getCreateInstitution() == null) {
            if (webUserController.getLoggedInstitution().getPoiInstitution() != null) {
                createdIns = webUserController.getLoggedInstitution().getPoiInstitution();
            } else {
                createdIns = webUserController.getLoggedInstitution();
            }
            rat.getClient().setCreateInstitution(createdIns);
        } else {
            createdIns = rat.getClient().getCreateInstitution();
        }

        if (createdIns == null || createdIns.getPoiNumber() == null || createdIns.getPoiNumber().trim().equals("")) {
            JsfUtil.addErrorMessage("The institution you logged has no POI. Can not generate a PHN.");
            return "";
        }

        if (rat.getClient().getPhn() == null || rat.getClient().getPhn().trim().equals("")) {
            String newPhn = applicationController.createNewPersonalHealthNumberformat(createdIns);

            int count = 0;
            while (clientApplicationController.checkPhnExists(newPhn, null)) {
                newPhn = applicationController.createNewPersonalHealthNumberformat(createdIns);
                count++;
                if (count > 100) {
                    JsfUtil.addErrorMessage("Generating New PHN Failed. Client NOT saved. Please contact System Administrator.");
                    return "";
                }
            }
            rat.getClient().setPhn(newPhn);
        }
        if (rat.getClient().getId() == null) {
            if (clientApplicationController.checkPhnExists(rat.getClient().getPhn(), null)) {
                JsfUtil.addErrorMessage("PHN already exists.");
                return null;
            }
            if (rat.getClient().getPerson().getNic() != null && !rat.getClient().getPerson().getNic().trim().equals("")) {
                if (clientApplicationController.checkNicExists(rat.getClient().getPerson().getNic(), null)) {
                    JsfUtil.addErrorMessage("NIC already exists.");
                    return null;
                }
            }
        } else {
            if (clientApplicationController.checkPhnExists(rat.getClient().getPhn(), rat.getClient())) {
                JsfUtil.addErrorMessage("PHN already exists.");
                return null;
            }
            if (rat.getClient().getPerson().getNic() != null && !rat.getClient().getPerson().getNic().trim().equals("")) {
                if (clientApplicationController.checkNicExists(rat.getClient().getPerson().getNic(), rat.getClient())) {
                    JsfUtil.addErrorMessage("NIC already exists.");
                    return null;
                }
            }
        }
        clientController.saveClient(rat.getClient());
        if (rat.getEncounterNumber() == null
                || rat.getEncounterNumber().trim().equals("")) {
            rat.setEncounterNumber(encounterController.createTestNumber(webUserController.getLoggedInstitution()));
        }

        encounterController.save(rat);

        sessionController.setLastRatOrderingCategory(rat.getPcrOrderingCategory());
        sessionController.setLastRat(rat);

        sessionController.getRats().put(rat.getId(), rat);

        JsfUtil.addSuccessMessage("Saved.");
        return "/lab/rat_view";
    }

    public String savePcr() {
        if (pcr == null) {
            JsfUtil.addErrorMessage("No pcr to save");
            return "";
        }
        if (pcr.getClient() == null) {
            JsfUtil.addErrorMessage("No Client to save");
            return "";
        }
        Institution createdIns = null;
        pcr.setCreatedInstitution(webUserController.getLoggedInstitution());
        pcr.setReferalInstitution(webUserController.getLoggedInstitution());

        Institution moh = institutionApplicationController.findMinistryOfHealth();

        if (pcr.getClient().getCreateInstitution() == null) {
            if (webUserController.getLoggedInstitution().getPoiInstitution() != null) {
                createdIns = webUserController.getLoggedInstitution().getPoiInstitution();
            } else {
                createdIns = webUserController.getLoggedInstitution();
            }
            pcr.getClient().setCreateInstitution(createdIns);
        } else {
            createdIns = pcr.getClient().getCreateInstitution();
        }

        if (createdIns == null || createdIns.getPoiNumber() == null || createdIns.getPoiNumber().trim().equals("")) {
            JsfUtil.addErrorMessage("The institution you logged has no POI. Can not generate a PHN.");
            return "";
        }

        if (pcr.getClient().getPhn() == null || pcr.getClient().getPhn().trim().equals("")) {
            String newPhn = applicationController.createNewPersonalHealthNumberformat(createdIns);

            int count = 0;
            while (clientApplicationController.checkPhnExists(newPhn, null)) {
                newPhn = applicationController.createNewPersonalHealthNumberformat(createdIns);
                count++;
                if (count > 100) {
                    JsfUtil.addErrorMessage("Generating New PHN Failed. Client NOT saved. Please contact System Administrator.");
                    return "";
                }
            }
            pcr.getClient().setPhn(newPhn);
        }
        if (pcr.getClient().getId() == null) {
            if (clientApplicationController.checkPhnExists(pcr.getClient().getPhn(), null)) {
                JsfUtil.addErrorMessage("PHN already exists.");
                return null;
            }
            if (pcr.getClient().getPerson().getNic() != null && !pcr.getClient().getPerson().getNic().trim().equals("")) {
                if (clientApplicationController.checkNicExists(pcr.getClient().getPerson().getNic(), null)) {
                    JsfUtil.addErrorMessage("NIC already exists.");
                    return null;
                }
            }
        } else {
            if (clientApplicationController.checkPhnExists(pcr.getClient().getPhn(), pcr.getClient())) {
                JsfUtil.addErrorMessage("PHN already exists.");
                return null;
            }
            if (pcr.getClient().getPerson().getNic() != null && !pcr.getClient().getPerson().getNic().trim().equals("")) {
                if (clientApplicationController.checkNicExists(pcr.getClient().getPerson().getNic(), pcr.getClient())) {
                    JsfUtil.addErrorMessage("NIC already exists.");
                    return null;
                }
            }
        }
        clientController.saveClient(pcr.getClient());
        if (pcr.getEncounterNumber() == null
                || pcr.getEncounterNumber().trim().equals("")) {
            pcr.setEncounterNumber(encounterController.createTestNumber(webUserController.getLoggedInstitution()));
        }

        pcr.setSampled(true);
        pcr.setSampledAt(new Date());
        pcr.setSampledBy(webUserController.getLoggedUser());

        pcr.setSentToLab(true);
        pcr.setSentToLabAt(new Date());
        pcr.setSentToLabBy(webUserController.getLoggedUser());

        encounterController.save(pcr);

        sessionController.setLastPcrOrdringCategory(pcr.getPcrOrderingCategory());
        sessionController.setLastInstitution(pcr.getInstitution());
        sessionController.setLastWardUnit(pcr.getUnitWard());

        sessionController.setLastPcr(pcr);
        lab = pcr.getReferalInstitution();
        sessionController.getPcrs().put(pcr.getId(), pcr);

        JsfUtil.addSuccessMessage("PCR Saved.");
        return "/lab/pcr_view";
    }

    public void retrieveLastAddressForRat() {
        if (rat == null || rat.getClient() == null || sessionController.getLastRat() == null || sessionController.getLastRat().getClient() == null) {
            return;
        }
        rat.getClient().getPerson().setAddress(sessionController.getLastRat().getClient().getPerson().getAddress());
    }

    public void retrieveLastAddressForPcr() {
        if (pcr == null || pcr.getClient() == null
                || sessionController.getLastPcr() == null
                || sessionController.getLastPcr().getClient() == null) {
            return;
        }
        pcr.getClient().getPerson().setAddress(sessionController.getLastPcr().getClient().getPerson().getAddress());
    }

    public void retrieveLastPhoneForRat() {
        if (rat == null || rat.getClient() == null || sessionController.getLastRat() == null || sessionController.getLastRat().getClient() == null) {
            return;
        }
        rat.getClient().getPerson().setPhone1(sessionController.getLastRat().getClient().getPerson().getPhone1());
    }

    public void retrieveLastPhoneForPcr() {
        if (pcr == null
                || pcr.getClient() == null
                || sessionController.getLastPcr() == null
                || sessionController.getLastPcr().getClient() == null) {
            return;
        }
        pcr.getClient().getPerson().setPhone1(sessionController.getLastPcr().getClient().getPerson().getPhone1());
    }

    public void retrieveLastGnRat() {
        if (rat == null || rat.getClient() == null || sessionController.getLastRat() == null || sessionController.getLastRat().getClient() == null) {
            return;
        }
        rat.getClient().getPerson().setGnArea(sessionController.getLastRat().getClient().getPerson().getGnArea());
    }

    public void retrieveLastGnPcr() {
        if (pcr == null
                || pcr.getClient() == null
                || sessionController.getLastPcr() == null
                || sessionController.getLastPcr().getClient() == null) {
            return;
        }
        pcr.getClient().getPerson().setGnArea(sessionController.getLastPcr().getClient().getPerson().getGnArea());
    }

    public List<Area> completePhiAreasForRat(String qry) {
        List<Area> areas = new ArrayList<>();
        if (rat == null) {
            return areas;
        }
        if (rat.getClient() == null) {
            return areas;
        }
        if (rat.getClient().getPerson().getMohArea() == null) {
            return areaApplicationController.completePhiAreas(qry);
        } else {
            return areaApplicationController.completePhiAreasOfMoh(qry, rat.getClient().getPerson().getMohArea());
        }
    }

    public List<Area> completePhiAreasForPcr(String qry) {
        List<Area> areas = new ArrayList<>();
        if (pcr == null) {
            return areas;
        }
        if (pcr.getClient() == null) {
            return areas;
        }
        if (pcr.getClient().getPerson().getMohArea() == null) {
            return areaApplicationController.completePhiAreas(qry);
        } else {
            return areaApplicationController.completePhiAreasOfMoh(qry, pcr.getClient().getPerson().getMohArea());
        }
    }

    public List<Area> completeGnAreasForRat(String qry) {
        return completeGnAreas(qry, rat);
    }

    public List<Area> completeGnAreasForPcr(String qry) {
        return completeGnAreas(qry, pcr);
    }

    private List<Area> completeGnAreas(String qry, Encounter e) {
        List<Area> areas = new ArrayList<>();
        if (e == null) {
            return areas;
        }
        if (e.getClient() == null) {
            return areas;
        }
        if (e.getClient().getPerson().getDistrict() == null) {
            return areaApplicationController.completeGnAreas(qry);
        } else {
            return areaApplicationController.completeGnAreasOfDistrict(qry, e.getClient().getPerson().getDistrict());
        }
    }

    public String toTestList() {
        System.out.println("toTestList");
        Map m = new HashMap();

        String j = "select c "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);

        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);

        j += " and c.institution=:ins ";
        m.put("ins", webUserController.getLoggedInstitution());

        //c.client.person.mohArea = :moh
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
        return "/lab/list_of_tests";
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
        return "/lab/ordering_category_district";
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
        return "/lab/ordering_category_moh";
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
        return "/lab/list_of_tests";
    }

    public String toTestListWithoutResults() {
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
        if (institution != null) {
            j += " and c.institution=:ins ";
            m.put("ins", institution);
        }

        j += " and c.referalInstitution=:ri ";
        m.put("ri", webUserController.getLoggedInstitution());

        tests = encounterFacade.findByJpql(j, m, TemporalType.TIMESTAMP);
        return "/lab/list_of_tests";
    }

    public String toEditNames() {
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
        if (result != null) {
            j += " and c.pcrResult=:result ";
            m.put("result", result);
        }
        if (institution != null) {
            j += " and c.institution=:ins ";
            m.put("ins", institution);
        }
        j += " and c.referalInstitution=:ri ";
        m.put("ri", webUserController.getLoggedInstitution());
        tests = encounterFacade.findByJpql(j, m, TemporalType.TIMESTAMP);
        return "/lab/edit_names";
    }

    public String toListOfRequestsByOrderedDate() {
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
        if (result != null) {
            j += " and c.pcrResult=:result ";
            m.put("result", result);
        }
        if (institution != null) {
            j += " and c.institution=:ins ";
            m.put("ins", institution);
        }

        j += " and c.referalInstitution=:ri ";
        m.put("ri", webUserController.getLoggedInstitution());

        tests = encounterFacade.findByJpql(j, m, TemporalType.TIMESTAMP);
        return "/lab/list_of_requests_by_ordered_date";
    }

    public String toListOfRequestsBySampelledDate() {
        Map m = new HashMap();
        String j = "select c "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);

        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);

        j += " and c.sampledAt between :fd and :td ";
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
        if (institution != null) {
            j += " and c.institution=:ins ";
            m.put("ins", institution);
        }

        j += " and c.referalInstitution=:ri ";
        m.put("ri", webUserController.getLoggedInstitution());

        tests = encounterFacade.findByJpql(j, m, TemporalType.TIMESTAMP);
        return "/lab/list_of_requests_by_sampelled_date";
    }

    public String toListOfRequestsByDispatchedDate() {
        Map m = new HashMap();
        String j = "select c "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);

        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);

        j += " and c.sentToLabAt between :fd and :td ";
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
        if (institution != null) {
            j += " and c.institution=:ins ";
            m.put("ins", institution);
        }

        j += " and c.referalInstitution=:ri ";
        m.put("ri", webUserController.getLoggedInstitution());

        tests = encounterFacade.findByJpql(j, m, TemporalType.TIMESTAMP);
        return "/lab/list_of_requests_by_dispatched_date";
    }

    public String toListOfRequestsByReceivedDate() {
        Map m = new HashMap();
        String j = "select c "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);

        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);

        j += " and c.receivedAtLabAt between :fd and :td ";
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
        if (institution != null) {
            j += " and c.institution=:ins ";
            m.put("ins", institution);
        }

        j += " and c.referalInstitution=:ri ";
        m.put("ri", webUserController.getLoggedInstitution());

        tests = encounterFacade.findByJpql(j, m, TemporalType.TIMESTAMP);
        return "/lab/list_of_requests_by_received_date";
    }

    public String toListOfRequestsByResultEnteredDate() {
        Map m = new HashMap();
        String j = "select c "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);

        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);

        j += " and c.resultEnteredAt between :fd and :td ";
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
        if (institution != null) {
            j += " and c.institution=:ins ";
            m.put("ins", institution);
        }

        j += " and c.referalInstitution=:ri ";
        m.put("ri", webUserController.getLoggedInstitution());

        tests = encounterFacade.findByJpql(j, m, TemporalType.TIMESTAMP);
        return "/lab/list_of_requests_by_entered_date";
    }

    public String toListOfRequestsByResultReviewedDate() {
        Map m = new HashMap();
        String j = "select c "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);

        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);

        j += " and c.resultReviewedAt between :fd and :td ";
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
        if (institution != null) {
            j += " and c.institution=:ins ";
            m.put("ins", institution);
        }

        j += " and c.referalInstitution=:ri ";
        m.put("ri", webUserController.getLoggedInstitution());

        tests = encounterFacade.findByJpql(j, m, TemporalType.TIMESTAMP);
        return "/lab/list_of_requests_by_reviewed_date";
    }

    public String toListOfRequestsByResultConfirmedDate() {
        Map m = new HashMap();
        String j = "select c "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);

        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);

        j += " and c.resultConfirmed between :fd and :td ";
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
        if (institution != null) {
            j += " and c.institution=:ins ";
            m.put("ins", institution);
        }

        j += " and c.referalInstitution=:ri ";
        m.put("ri", webUserController.getLoggedInstitution());

        tests = encounterFacade.findByJpql(j, m, TemporalType.TIMESTAMP);
        return "/lab/list_of_requests_by_confirmed_date";
    }

    public String toListOfTestsRegional() {
        System.out.println("toTestList");
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

        return "/lab/list_of_cases_by_management_plan";
    }

    public String toEnterResults() {
        System.out.println("toTestList");
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
        return "/lab/enter_results";
    }

    public List<Item> getCovidTestOrderingCategories() {
        return itemApplicationController.getCovidTestOrderingCategories();
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

    public List<Institution> completeMohOrHospitals(String qry) {
        List<InstitutionType> its = new ArrayList<>();
        its.add(InstitutionType.MOH_Office);
        its.add(InstitutionType.Hospital);
        return institutionController.fillInstitutions(its, qry, null);
    }

    public List<Institution> completeUnitsForPcr(String qry) {
        if (pcr == null || pcr.getInstitution() == null) {
            return null;
        }
        List<InstitutionType> its = new ArrayList<>();
        its.add(InstitutionType.MOH_Office);
        its.add(InstitutionType.Hospital);
        return institutionController.findChildrenPmcis(pcr.getInstitution(), qry);
    }

    public List<Institution> completeUnitsForRat(String qry) {
        List<InstitutionType> its = new ArrayList<>();
        its.add(InstitutionType.MOH_Office);
        its.add(InstitutionType.Hospital);
        return institutionController.findChildrenPmcis(rat.getInstitution(), qry);
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

    public List<Encounter> getListedToDispatch() {
        return listedToDispatch;
    }

    public void setListedToDispatch(List<Encounter> listedToDispatch) {
        this.listedToDispatch = listedToDispatch;
    }

    public Institution getDispatchingLab() {
        return dispatchingLab;
    }

    public void setDispatchingLab(Institution dispatchingLab) {
        this.dispatchingLab = dispatchingLab;
    }

    public List<Encounter> getSelectedToDispatch() {
        return selectedToDispatch;
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

    public List<Encounter> getTestList() {
        return testList;
    }

    public void setTestList(List<Encounter> testList) {
        this.testList = testList;
    }

    public Area getPdhs() {
        return pdhs;
    }

    public void setPdhs(Area pdhs) {
        this.pdhs = pdhs;
    }

    public Area getRdhs() {
        return rdhs;
    }

    public void setRdhs(Area rdhs) {
        this.rdhs = rdhs;
    }

    public InstitutionType getInstitutionType() {
        return institutionType;
    }

    public void setInstitutionType(InstitutionType institutionType) {
        this.institutionType = institutionType;
    }

    public ClientFacade getClientFacade() {
        return clientFacade;
    }

    public EncounterFacade getEncounterFacade() {
        return encounterFacade;
    }

    public ClientEncounterComponentItemFacade getCeciFacade() {
        return ceciFacade;
    }

    public SmsFacade getSmsFacade() {
        return smsFacade;
    }

    public ClientApplicationController getClientApplicationController() {
        return clientApplicationController;
    }

    public ApplicationController getApplicationController() {
        return applicationController;
    }

    public ClientController getClientController() {
        return clientController;
    }

    public AreaApplicationController getAreaApplicationController() {
        return areaApplicationController;
    }

    public SessionController getSessionController() {
        return sessionController;
    }

    public InstitutionApplicationController getInstitutionApplicationController() {
        return institutionApplicationController;
    }

    public WebUserController getWebUserController() {
        return webUserController;
    }

    public EncounterController getEncounterController() {
        return encounterController;
    }

    public ItemController getItemController() {
        return itemController;
    }

    public ItemApplicationController getItemApplicationController() {
        return itemApplicationController;
    }

    public InstitutionController getInstitutionController() {
        return institutionController;
    }

    public CommonController getCommonController() {
        return commonController;
    }

    public AreaController getAreaController() {
        return areaController;
    }

    public UserTransactionController getUserTransactionController() {
        return userTransactionController;
    }

    public PreferenceController getPreferenceController() {
        return preferenceController;
    }

    public InstitutionPeformance getInstitutionPeformancesSummery() {
        return institutionPeformancesSummery;
    }

    public List<InstitutionTypeCount> getInstitutionTypeCounts() {
        return institutionTypeCounts;
    }

    public void setInstitutionTypeCounts(List<InstitutionTypeCount> institutionTypeCounts) {
        this.institutionTypeCounts = institutionTypeCounts;
    }

    public List<InstitutionPeformance> getInstitutionPeformances() {
        return institutionPeformances;
    }

    public void setInstitutionPeformances(List<InstitutionPeformance> institutionPeformances) {
        this.institutionPeformances = institutionPeformances;
    }

    public List<InstitutionPeformance> getInstitutionPeformancesFiltered() {
        return institutionPeformancesFiltered;
    }

    public void setInstitutionPeformancesFiltered(List<InstitutionPeformance> institutionPeformancesFiltered) {
        this.institutionPeformancesFiltered = institutionPeformancesFiltered;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEncryptedId() {
        return encryptedId;
    }

    public void setEncryptedId(String encryptedId) {
        this.encryptedId = encryptedId;
    }

    public boolean isClientViewReportDisplayError() {
        return clientViewReportDisplayError;
    }

    public void setClientViewReportDisplayError(boolean clientViewReportDisplayError) {
        this.clientViewReportDisplayError = clientViewReportDisplayError;
    }

    public boolean isClientViewReportDisplayCheck() {
        return clientViewReportDisplayCheck;
    }

    public void setClientViewReportDisplayCheck(boolean clientViewReportDisplayCheck) {
        this.clientViewReportDisplayCheck = clientViewReportDisplayCheck;
    }

    public boolean isClientViewReportDisplay() {
        return clientViewReportDisplay;
    }

    public void setClientViewReportDisplay(boolean clientViewReportDisplay) {
        this.clientViewReportDisplay = clientViewReportDisplay;
    }

    public List<LabSummary> getLabSummaries() {
        return labSummaries;
    }

    public void setLabSummaries(List<LabSummary> labSummaries) {
        this.labSummaries = labSummaries;
    }

    public LabSummary getLabSummary() {
        return labSummary;
    }

    public void setLabSummary(LabSummary labSummary) {
        this.labSummary = labSummary;
    }

    public String getSearchingName() {
        return searchingName;
    }

    public void setSearchingName(String searchingName) {
        this.searchingName = searchingName;
    }

    public String getSearchingBhtno() {
        return searchingBhtno;
    }

    public void setSearchingBhtno(String searchingBhtno) {
        this.searchingBhtno = searchingBhtno;
    }

    public String getSearchingLabNo() {
        return searchingLabNo;
    }

    public void setSearchingLabNo(String searchingLabNo) {
        this.searchingLabNo = searchingLabNo;
    }

    public String getSearchingTestId() {
        return searchingTestId;
    }

    public void setSearchingTestId(String searchingTestId) {
        this.searchingTestId = searchingTestId;
    }

    public String getSearchingTestNo() {
        return searchingTestNo;
    }

    public void setSearchingTestNo(String searchingTestNo) {
        this.searchingTestNo = searchingTestNo;
    }

}
