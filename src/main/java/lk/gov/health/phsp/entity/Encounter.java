/*
 * The MIT License
 *
 * Copyright 2019 Dr M H B Ariyaratne<buddhika.ari@gmail.com>.
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
package lk.gov.health.phsp.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import lk.gov.health.phsp.enums.EncounterType;

/**
 *
 * @author buddhika
 */
@Entity
@Table
@XmlRootElement
public class Encounter implements Serializable {

    @OneToMany(mappedBy = "itemEncounter")
    private List<ClientEncounterComponentItem> clientEncounterComponentItems;

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    private Client client;
    @ManyToOne
    private Area area;

    private boolean firstEncounter;

    private Boolean sampled;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date sampledAt;
    @ManyToOne(fetch = FetchType.LAZY)
    private WebUser sampledBy;

    private Boolean sentToLab;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date sentToLabAt;
    @ManyToOne(fetch = FetchType.LAZY)
    private WebUser sentToLabBy;

    private Boolean receivedAtLab;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date receivedAtLabAt;
    @ManyToOne(fetch = FetchType.LAZY)
    private WebUser receivedAtLabBy;

    private Boolean resultEntered;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date resultEnteredAt;
    @ManyToOne(fetch = FetchType.LAZY)
    private WebUser resultEnteredBy;

    private Boolean resultConfirmed;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date resultConfirmedAt;
    @ManyToOne(fetch = FetchType.LAZY)
    private WebUser resultConfirmedBy;

    private Double ctValue;
    @ManyToOne
    private Item pctResult;

    private Boolean resultPositive;
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date resultDate;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date resultDateTime;
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date resultMarkedUser;

    @Temporal(javax.persistence.TemporalType.DATE)
    private Date encounterDate;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date encounterFrom;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date encounterTo;

    private Integer encounterYear;
    private Integer encounterMonth;
    private Integer encounterQuarter;

    @Enumerated(EnumType.STRING)
    private EncounterType encounterType;

    private String encounterNumber;

    @ManyToOne
    private Encounter parentEncounter;

    @ManyToOne
    Institution institution;

    @ManyToOne
    Institution referalInstitution;

    @ManyToOne
    private WebUser createdBy;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date createdAt;

    private boolean retired;
    @ManyToOne
    private WebUser retiredBy;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date retiredAt;
    @Lob
    private String retireComments;

    @ManyToOne
    private WebUser retiredReversedBy;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date retiredReversedAt;

    /*
    Last Edit Properties
     */
    @ManyToOne
    private WebUser lastEditBy;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date lastEditeAt;

    private boolean completed;
    @ManyToOne
    private WebUser completedBy;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date completedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {

        if (!(object instanceof Encounter)) {
            return false;
        }
        Encounter other = (Encounter) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "id=" + id + "";
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }
    
    

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public Date getEncounterDate() {
        return encounterDate;
    }

    public void setEncounterDate(Date encounterDate) {
        this.encounterDate = encounterDate;
    }

    public Date getEncounterFrom() {
        return encounterFrom;
    }

    public void setEncounterFrom(Date encounterFrom) {
        this.encounterFrom = encounterFrom;
    }

    public Date getEncounterTo() {
        return encounterTo;
    }

    public void setEncounterTo(Date encounterTo) {
        this.encounterTo = encounterTo;
    }

    public EncounterType getEncounterType() {
        return encounterType;
    }

    public void setEncounterType(EncounterType encounterType) {
        this.encounterType = encounterType;
    }

    public Encounter getParentEncounter() {
        return parentEncounter;
    }

    public void setParentEncounter(Encounter parentEncounter) {
        this.parentEncounter = parentEncounter;
    }

    public boolean getCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public WebUser getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(WebUser createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isRetired() {
        return retired;
    }

    public void setRetired(boolean retired) {
        this.retired = retired;
    }

    public WebUser getRetiredBy() {
        return retiredBy;
    }

    public void setRetiredBy(WebUser retiredBy) {
        this.retiredBy = retiredBy;
    }

    public Date getRetiredAt() {
        return retiredAt;
    }

    public void setRetiredAt(Date retiredAt) {
        this.retiredAt = retiredAt;
    }

    public WebUser getRetiredReversedBy() {
        return retiredReversedBy;
    }

    public void setRetiredReversedBy(WebUser retiredReversedBy) {
        this.retiredReversedBy = retiredReversedBy;
    }

    public Date getRetiredReversedAt() {
        return retiredReversedAt;
    }

    public void setRetiredReversedAt(Date retiredReversedAt) {
        this.retiredReversedAt = retiredReversedAt;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public Institution getReferalInstitution() {
        return referalInstitution;
    }

    public void setReferalInstitution(Institution referalInstitution) {
        this.referalInstitution = referalInstitution;
    }

    public WebUser getCompletedBy() {
        return completedBy;
    }

    public void setCompletedBy(WebUser completedBy) {
        this.completedBy = completedBy;
    }

    public Date getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Date completedAt) {
        this.completedAt = completedAt;
    }

    public String getEncounterNumber() {
        return encounterNumber;
    }

    public void setEncounterNumber(String encounterNumber) {
        this.encounterNumber = encounterNumber;
    }

    public WebUser getLastEditBy() {
        return lastEditBy;
    }

    public void setLastEditBy(WebUser lastEditBy) {
        this.lastEditBy = lastEditBy;
    }

    public Date getLastEditeAt() {
        return lastEditeAt;
    }

    public void setLastEditeAt(Date lastEditeAt) {
        this.lastEditeAt = lastEditeAt;
    }

    public Integer getEncounterYear() {
        return encounterYear;
    }

    public void setEncounterYear(Integer encounterYear) {
        this.encounterYear = encounterYear;
    }

    public Integer getEncounterMonth() {
        return encounterMonth;
    }

    public void setEncounterMonth(Integer encounterMonth) {
        this.encounterMonth = encounterMonth;
    }

    public Integer getEncounterQuarter() {
        return encounterQuarter;
    }

    public void setEncounterQuarter(Integer encounterQuarter) {
        this.encounterQuarter = encounterQuarter;
    }

    public boolean isFirstEncounter() {
        return firstEncounter;
    }

    public void setFirstEncounter(boolean firstEncounter) {
        this.firstEncounter = firstEncounter;
    }

    @XmlTransient
    public List<ClientEncounterComponentItem> getClientEncounterComponentItems() {
        return clientEncounterComponentItems;
    }

    public void setClientEncounterComponentItems(List<ClientEncounterComponentItem> clientEncounterComponentItems) {
        this.clientEncounterComponentItems = clientEncounterComponentItems;
    }

    public ClientEncounterComponentItem getClientEncounterComponentItem(Item item) {
        for (ClientEncounterComponentItem i : getClientEncounterComponentItems()) {
            if (i.getItem().equals(item)) {
                return i;
            }
        }
        return null;
    }

    public String getRetireComments() {
        return retireComments;
    }

    public void setRetireComments(String retireComments) {
        this.retireComments = retireComments;
    }

    public Boolean getResultPositive() {
        return resultPositive;
    }

    public void setResultPositive(Boolean resultPositive) {
        this.resultPositive = resultPositive;
    }

    public Date getResultDate() {
        return resultDate;
    }

    public void setResultDate(Date resultDate) {
        this.resultDate = resultDate;
    }

    public Date getResultDateTime() {
        return resultDateTime;
    }

    public void setResultDateTime(Date resultDateTime) {
        this.resultDateTime = resultDateTime;
    }

    public Date getResultMarkedUser() {
        return resultMarkedUser;
    }

    public void setResultMarkedUser(Date resultMarkedUser) {
        this.resultMarkedUser = resultMarkedUser;
    }

    public Boolean getSampled() {
        return sampled;
    }

    public void setSampled(Boolean sampled) {
        this.sampled = sampled;
    }

    public Date getSampledAt() {
        return sampledAt;
    }

    public void setSampledAt(Date sampledAt) {
        this.sampledAt = sampledAt;
    }

    public Boolean getSentToLab() {
        return sentToLab;
    }

    public void setSentToLab(Boolean sentToLab) {
        this.sentToLab = sentToLab;
    }

    public Date getSentToLabAt() {
        return sentToLabAt;
    }

    public void setSentToLabAt(Date sentToLabAt) {
        this.sentToLabAt = sentToLabAt;
    }

    public Boolean getReceivedAtLab() {
        return receivedAtLab;
    }

    public void setReceivedAtLab(Boolean receivedAtLab) {
        this.receivedAtLab = receivedAtLab;
    }

    public Date getReceivedAtLabAt() {
        return receivedAtLabAt;
    }

    public void setReceivedAtLabAt(Date receivedAtLabAt) {
        this.receivedAtLabAt = receivedAtLabAt;
    }

    public Boolean getResultEntered() {
        return resultEntered;
    }

    public void setResultEntered(Boolean resultEntered) {
        this.resultEntered = resultEntered;
    }

    public Date getResultEnteredAt() {
        return resultEnteredAt;
    }

    public void setResultEnteredAt(Date resultEnteredAt) {
        this.resultEnteredAt = resultEnteredAt;
    }

    public Boolean getResultConfirmed() {
        return resultConfirmed;
    }

    public void setResultConfirmed(Boolean resultConfirmed) {
        this.resultConfirmed = resultConfirmed;
    }

    public Date getResultConfirmedAt() {
        return resultConfirmedAt;
    }

    public void setResultConfirmedAt(Date resultConfirmedAt) {
        this.resultConfirmedAt = resultConfirmedAt;
    }

    public Double getCtValue() {
        return ctValue;
    }

    public void setCtValue(Double ctValue) {
        this.ctValue = ctValue;
    }

    public Item getPctResult() {
        return pctResult;
    }

    public void setPctResult(Item pctResult) {
        this.pctResult = pctResult;
    }

    public WebUser getSampledBy() {
        return sampledBy;
    }

    public void setSampledBy(WebUser sampledBy) {
        this.sampledBy = sampledBy;
    }

    public WebUser getSentToLabBy() {
        return sentToLabBy;
    }

    public void setSentToLabBy(WebUser sentToLabBy) {
        this.sentToLabBy = sentToLabBy;
    }

    public WebUser getReceivedAtLabBy() {
        return receivedAtLabBy;
    }

    public void setReceivedAtLabBy(WebUser receivedAtLabBy) {
        this.receivedAtLabBy = receivedAtLabBy;
    }

    public WebUser getResultEnteredBy() {
        return resultEnteredBy;
    }

    public void setResultEnteredBy(WebUser resultEnteredBy) {
        this.resultEnteredBy = resultEnteredBy;
    }

    public WebUser getResultConfirmedBy() {
        return resultConfirmedBy;
    }

    public void setResultConfirmedBy(WebUser resultConfirmedBy) {
        this.resultConfirmedBy = resultConfirmedBy;
    }

}
