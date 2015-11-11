package org.wso2;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.rmi.RemoteException;
import java.util.Iterator;
import javax.xml.namespace.QName;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.mediators.AbstractMediator;

/**
 *
 * @author nashry
 */
public class CurrencyRate extends AbstractMediator {

    private static final Log log = LogFactory.getLog(CurrencyRate.class);

    public static void main(String[] args) throws RemoteException {

    }

    public CurrencyRate() {
    }

    public boolean mediate(MessageContext mc) {
        Double rate = 0.0;
        Double amount = 0.0;
        Double amountConverted = 0.0;
        String fromCurrency = "";
        String amountString = "";
        //get parameters from proxy webservice
        String rateEUR = mc.getProperty("rateEUR") + "";
        String rateGBP = mc.getProperty("rateGBP") + "";
        String rateCAD = mc.getProperty("rateCAD") + "";

        OMElement body = mc.getEnvelope().getBody();
        OMFactory omFac = body.getOMFactory();
        Iterator<OMElement> records = (Iterator<OMElement>) body.getFirstChildWithName(new QName("urn:partner.soap.sforce.com", "queryResponse")).
                getFirstChildWithName(new QName("urn:partner.soap.sforce.com", "result")).getChildrenWithLocalName("records");
        OMElement record, amountConvertedOMElement;
        while (records.hasNext()) {
            record = records.next();
            //create a new OMElement to hold converted amount
            amountConvertedOMElement = omFac.createOMElement(new QName("urn:sobject.partner.soap.sforce.com", "AmountConverted", "sf"));

            //get the Amount and from currency from the SOAP message
            amountString = record.getFirstChildWithName(new QName("urn:sobject.partner.soap.sforce.com", "Amount")).getText();
            fromCurrency = record.getFirstChildWithName(new QName("urn:sobject.partner.soap.sforce.com", "CurrencyIsoCode")).getText();

            try {
                if (amountString.equalsIgnoreCase("")) {
                    amount = 0.0;
                } else {
                    amount = Double.parseDouble(amountString);
                }
                if (fromCurrency.equalsIgnoreCase("EUR")) {
                    rate = Double.parseDouble(rateEUR);
                } else if (fromCurrency.equalsIgnoreCase("GBP")) {
                    rate = Double.parseDouble(rateGBP);
                } else if (fromCurrency.equalsIgnoreCase("CAD")) {
                    rate = Double.parseDouble(rateCAD);
                } else {
                    rate = 1.0;
                }

                amountConverted = rate * amount;
                //set the converted value to the OMElement
                amountConvertedOMElement.setText(amountConverted.toString());

                //add child to record
                record.addChild(amountConvertedOMElement);

            } catch (Exception e) {
                System.out.println("exception occured: " + e);
                e.printStackTrace();
            }
        }
        return true;
    }
}
