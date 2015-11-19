/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.solutions.sf;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.xpath.operations.String;

import javax.xml.namespace.QName;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

/**
 * Formats the Salesforce data.
 */
public class DataFormatter extends AbstractMediator {

    @Override
    public boolean mediate(MessageContext messageContext) {
        OMElement body = messageContext.getEnvelope().getBody();
        OMFactory omFac = body.getOMFactory();
        Iterator<OMElement> records = (Iterator<OMElement>) body.getFirstChildWithName(new QName("urn:partner.soap.sforce.com", "queryResponse")).
                getFirstChildWithName(new QName("urn:partner.soap.sforce.com", "result")).getChildrenWithLocalName("records");
        OMElement record, tqThisMonth,tqThisQuarter,tqThisYear,tqNextMonth,tqNextQtr,tqNextYear,tqRecordDate;
        Date closeDate;
        Date recordDate = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String tqRecordDateStr = sdf.format(recordDate);
        int thisMonth = Calendar.getInstance().get(Calendar.MONTH);
        int thisQuarter = (Calendar.getInstance().get(Calendar.MONTH) +3)/3;
        int thisYear = Calendar.getInstance().get(Calendar.YEAR);
        int nextMonth = thisMonth + 1;
        int nextQuarter = thisQuarter +1;
        int nextYear = thisYear +1;

        int fYear, fQtr, fMonth;
        Calendar cal = Calendar.getInstance();
        String dateStr, tqThisMonthStr, tqThisQtrStr, tqThisYearStr, tqNextMonthStr, tqNextQtrStr, tqNextYearStr;

        while (records.hasNext()) {
            tqThisMonthStr = tqThisQtrStr = tqThisYearStr = tqNextMonthStr = tqNextQtrStr = tqNextYearStr =  "0";
            record = records.next();
            tqThisMonth = omFac.createOMElement(new QName("urn:sobject.partner.soap.sforce.com", "ThisMonth","sf"));
            tqThisQuarter = omFac.createOMElement(new QName("urn:sobject.partner.soap.sforce.com", "ThisQuarter","sf"));
            tqThisYear = omFac.createOMElement(new QName("urn:sobject.partner.soap.sforce.com", "ThisYear","sf"));
            tqNextMonth = omFac.createOMElement(new QName("urn:sobject.partner.soap.sforce.com", "NextMonth","sf"));
            tqNextQtr = omFac.createOMElement(new QName("urn:sobject.partner.soap.sforce.com", "NextQuarter","sf"));
            tqNextYear = omFac.createOMElement(new QName("urn:sobject.partner.soap.sforce.com", "NextYear","sf"));
            tqRecordDate = omFac.createOMElement(new QName("urn:sobject.partner.soap.sforce.com", "RecordDate","sf"));
            dateStr = record.getFirstChildWithName(new QName("urn:sobject.partner.soap.sforce.com","CloseDate")).getText();


            fQtr = Integer.parseInt(record.getFirstChildWithName(new QName("urn:sobject.partner.soap.sforce.com","FiscalQuarter")).getText());
            fYear = Integer.parseInt(record.getFirstChildWithName(new QName("urn:sobject.partner.soap.sforce.com", "FiscalYear")).getText());

            try {
                closeDate = sdf.parse(dateStr);
                cal.setTime(closeDate);
                fMonth = cal.get(Calendar.MONTH);

                if (fYear == thisYear) {
                    tqThisYearStr = "1";
                    if (fQtr == thisQuarter) {
                        tqThisQtrStr = "1";
                        if(fMonth == thisMonth) {
                            tqThisMonthStr = "1";
                        } else if (fMonth == nextMonth) {
                            tqNextMonthStr = "1";
                        }
                    } else if (fQtr == nextQuarter) {
                        tqNextQtrStr = "1";
                        if (fMonth == nextMonth) {
                            tqNextMonthStr = "1";
                        }
                    }
                } else if (fYear == nextYear) {
                    tqNextYear.setText("1");
                    if (fQtr == nextQuarter) {
                        tqNextQtrStr = "1";
                        if (fMonth == nextMonth) {
                            tqNextMonthStr = "1";
                        }
                    }
                }
                tqThisMonth.setText(tqThisMonthStr);
                tqThisQuarter.setText(tqThisQtrStr);
                tqThisYear.setText(tqThisYearStr);
                tqNextMonth.setText(tqNextMonthStr);
                tqNextQtr.setText(tqNextQtrStr);
                tqNextYear.setText(tqNextYearStr);
                tqRecordDate.setText(tqRecordDateStr);

                record.addChild(tqThisMonth);
                record.addChild(tqThisQuarter);
                record.addChild(tqThisYear);
                record.addChild(tqNextMonth);
                record.addChild(tqNextQtr);
                record.addChild(tqNextYear);
                record.addChild(tqRecordDate);

            } catch (ParseException e) {
                System.out.println(":(");
                e.printStackTrace();
            }
        }
        return true;
    }
}
