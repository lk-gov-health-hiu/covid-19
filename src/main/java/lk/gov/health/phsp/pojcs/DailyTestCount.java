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
package lk.gov.health.phsp.pojcs;

import java.util.Date;

/**
 *
 * @author buddhika
 */
public class DailyTestCount {
    private Date date;
    private Long totalPcr;
    private Long totalRat;
    private Long positiveRat;
    private Long positivePcr;
    private Long totalPositives;
    private Long totalTests;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Long getTotalPcr() {
        return totalPcr;
    }

    public void setTotalPcr(Long totalPcr) {
        this.totalPcr = totalPcr;
    }

    public Long getTotalRat() {
        return totalRat;
    }

    public void setTotalRat(Long totalRat) {
        this.totalRat = totalRat;
    }

    public Long getPositiveRat() {
        return positiveRat;
    }

    public void setPositiveRat(Long positiveRat) {
        this.positiveRat = positiveRat;
    }

    public Long getPositivePcr() {
        return positivePcr;
    }

    public void setPositivePcr(Long positivePcr) {
        this.positivePcr = positivePcr;
    }

    public Long getTotalPositives() {
        return totalPositives;
    }

    public void setTotalPositives(Long totalPositives) {
        this.totalPositives = totalPositives;
    }

    public Long getTotalTests() {
        return totalTests;
    }

    public void setTotalTests(Long totalTests) {
        this.totalTests = totalTests;
    }
    
    
    
    
}
