package com.wso2.internal;

import org.apache.synapse.commons.evaluators.source.SourceTextRetriever;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Chanuka on 1/18/16 AD.
 */
public class UpdateProductName {

    static String produ1 = "";
    static String proVersion = "";

public static String getVersion(String productName){

    String re1=".*?";	// Non-greedy match on filler
    String re2="(\\d)";	// Any Single Digit 1
    String re3="(.)";	// Any Single Character 1
    String re4="(\\d)";	// Any Single Digit 2
    String re5="(.)";	// Any Single Character 2
    String re6="(\\d)";	// Any Single Digit 3

    Pattern p = Pattern.compile(re1+re2+re3+re4+re5+re6,Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    Matcher m = p.matcher(productName);
    if (m.find())
    {
        String d1=m.group(1);
        String c1=m.group(2);
        String d2=m.group(3);
        String c2=m.group(4);
        String d3=m.group(5);
        return (d1.toString()+c1.toString()+d2.toString()+c2.toString()+d3.toString());
    }
    return String.valueOf(false);
}

    public static String UpdateName(String overview_products) {


        String finalproductallname = "";
        String produ = overview_products.toUpperCase().trim();
        if (!produ.contains("SES") && !"-".equals(produ)) {
            produ1 = overview_products;//.toUpperCase().trim();

            //proVersion = produ.substring((produ.length() - 5), produ.length());
            produ = produ.replaceAll("\\s", "").trim();
            produ = produ.replaceAll("-", "").trim();
            if (produ.length() > 4) {
                if (produ.substring(0, 4).equals("WSO2")) {
                    produ = produ.substring(4, produ.length());
                }
            }

//                                        System.out.println(produ);
//                                        System.out.println
// ("---------------------------------------------------------------------");

            String intermi = "abc";
            if (produ.length() > 11) {
//                                                System.out.println
// ("---------------------------------"+produ+"--------------------------------------------");
                if (produ.substring(0, 10).equals("ENTERPRISE")) {
                    produ = produ.substring(10, produ.length()).trim();
//                                                    System.out.println
// ("---------------------------------------------------------------------------------------");
                    if (produ.substring(0, 2).equals("SE")) {
                        intermi = "Enterprise Service Bus";
                    }
                    if (produ.substring(0, 2).equals("ST")) {
                        intermi = "Enterprise Store";
                    }
                    if (produ.substring(0, 1).equals("M")) {
                        intermi = " Enterprise Mobility Manager";
                    }
                }

                if (produ.substring(0, 8).equals("BUSINESS")) {
                    produ = produ.substring(8, produ.length());

                    if (produ.substring(0, 2).equals("PR")) {
                        intermi = "Business Process Server";
                    }
                    if (produ.substring(0, 2).equals("AC")) {
                        intermi = "Business Activity Monitor";
                    }
                    if (produ.substring(0, 2).equals("RU")) {
                        intermi = "Business Rules Server";
                    }
                }

                if (produ.substring(0, 2).equals("AP")) {
                    produ = produ.substring(2, produ.length());
//                                            System.out.println(produ1);
//                                            System.out.println
// ("--------------------------------------------------------------------------------------------");
                    if (produ.substring(0, 2).equals("PL")) {
                        intermi = "Application Server";

                    }
                    if (produ.substring(0, 2).equals("IM")) {
                        intermi = "API Manager";
                    }
                    if (produ.substring(0, 2).equals("PF")) {
                        intermi = "App Factory";
                    }
                }
            }
            String finalproname = "";
            if (intermi.equals("abc")) {
                finalproname = findProduct(produ.toUpperCase());
            } else {
                finalproname = intermi;
            }
//                                            if (all.getLifecycleName().equals("Security_PatchLifeCycle")) {
//                                                finalproname = "security";
//                                                proVersion = "security";
//                                            }
            //String cliCo = all.getAttribute("overview_client").toString();

            // System.out.println(all.getQName() + "\t | \t" + produ1);

            //This is final working header

            //finalproductallname += finalproname + " " + proVersion + " ";
            String version = getVersion(finalproname);
            if(!version.equalsIgnoreCase("false"))
            {
                finalproname = finalproname.replace(version,"");
            }

            return finalproname;
        } else return "";
    }

    static void writetoAfile(String finalproductname, String OverviewName, String OrginalName) {
        Writer writer = null;

        try {
//            writer = new BufferedWriter(new OutputStreamWriter(
//                    new FileOutputStream("filename.txt"), "utf-8"));
            writer = new BufferedWriter(new FileWriter("filename.csv", true));
            writer.append(OverviewName + " ; " +OrginalName  + " ; " + finalproductname);
            writer.append('\n');


        } catch (IOException ex) {
            // report
        } finally {
            try {
                writer.close();
            } catch (Exception ex) {/*ignore*/}
        }
    }

    static void writetoCSV(){

    }


    public static String findProduct(String productName) {
        if (productName.substring(0, 2).equals("ES") || productName.substring(0, 2).equals("Se---")) {// ||
        // productName.toString().substring(0, 8) == "WSO2 ESB" || productName.toString().substring(0, 7) ==
        // "WSO2ESB") {
            return "Enterprise Service Bus";
        } else if (productName.substring(0, 2).equals("GR") || productName.substring(0, 2).equals("G-") ||
                productName.substring(0, 2).equals("GO") || productName.contains("GREG")) {
            return "Governance Registry";
        } else if (productName.substring(0, 2).equals("BA") || productName.substring(0, 2).equals("BU")) {
            return "Business Activity Monitor";
        } else if (productName.substring(0, 2).equals("DS") || productName.substring(0, 2).equals("DA")) {
            return "Data Services Server";
        } else if (productName.substring(0, 2).equals("WS")) {
            return "Web Services Application Server";
        } else if (productName.substring(0, 2).equals("Mo")) {
            return "Enterprise Mobility Manager";
        } //else if (productName.substring(0, 2).equals("AL")) {
        //return "all";
        //}
        else if (productName.substring(0, 2).equals("AS")) {
            return "Application Server";
        } else if (productName.substring(0, 2).equals("BP")) {
            return "Business Process Server";
        } else if (productName.substring(0, 2).equals("CA") || productName.substring(0, 2).equals("AL") ||
                productName.substring(0, 2).equals("KE")) {
            return "Carbon";
        } else if (productName.substring(0, 2).equals("IS") || productName.substring(0, 2).equals("ID")) {
            return "Identity Server";
        } else if (productName.substring(0, 2).equals("MB") || productName.substring(0, 2).equals("ME")) {
            return "Message Broker";
        } else if (productName.substring(0, 2).equals("AM") || productName.substring(0, 2).equals("AP")) { // //||
        // productName.substring(0,4) == "APIM") {
            return "API Manager";
        } else if (productName.substring(0, 2).equals("EL")) {
            return "Elastic Load Balancer";
        } else if (productName.substring(0, 2).equals("CG")) {
            return "Cloud Gateway";
        } else if (productName.substring(0, 2).equals("AF")) {
            return "App Factory";
        } else if (productName.substring(0, 2).equals("UE")) {
            return "User Engagement Server";
        }
//        else if (productName.substring(0, 2).equals("ST")) {
//            return "stratos";User Engagement Server
//        }
        else if (productName.substring(0, 2).equals("SS")) {
            return "Storage Server";
        } else if (productName.substring(0, 2).equals("BR")) {
            return "Business Rules Server";
        } else if (productName.substring(0, 2).equals("CE") || productName.substring(0, 2).equals("CO")) {
            return "Complex Event Processor";
//        } else if (productName.substring(0, 2).equals("CL")) {
//            return "cloud";
//        }
        } else if (productName.substring(0, 2).equals("DE")) {
            return "Developer Studio";
        } else if (productName.substring(0, 2).equals("DA")) {
            return "DATA SERVICES SERVER";
        } else if (productName.substring(0, 2).equals("TA")) {
            return "TASK SERVER";
        } else if (productName.substring(0, 2).equals("ST")) {
            proVersion = "";
            return produ1;
        }
        return productName;
    }
}
