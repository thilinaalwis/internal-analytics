package com.wso2.internal;

import java.io.File;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.synapse.core.SynapseEnvironment;
import org.w3c.dom.Document;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.startup.Task;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

public class PMTConnector implements Task, ManagedLifecycle {


    private SynapseEnvironment synapseEnvironment;
    private static ConfigurationContext configContext = null;
    private static final String CARBON_HOME = System.getProperty("carbon.home");
    //"/Users/Chanuka/Downloads/wso2greg-4.6.0-2";
    private static final String axis2Repo = CARBON_HOME + File.separator + "repository" +
            File.separator + "deployment" + File.separator + "client";
    private static final String axis2Conf = ServerConfiguration.getInstance().getFirstProperty("Axis2Config" + "" +
            ".clientAxis2XmlLocation");

    static Properties prop = new Properties();
    InputStream input = null;

    private static WSRegistryServiceClient initialize() throws Exception {

        //Read from property file
        String username = prop.getProperty("gregusername");
        String password = prop.getProperty("gregpassword");
        String serverURL = prop.getProperty("gregserviceURL");

        // get the property value and print it out
        System.out.println();
        System.out.println();
        System.out.println();

        System.setProperty("javax.net.ssl.trustStore", CARBON_HOME + File.separator + "repository" +
                File.separator + "resources" + File.separator + "security" + File.separator +
                "wso2carbon.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
        System.setProperty("javax.net.ssl.trustStoreType", "JKS");
        System.setProperty("carbon.repo.write.mode", "true");
        String tmp = null;
        configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(axis2Repo, tmp);
        return new WSRegistryServiceClient(serverURL, username, password, configContext);

    }

    public static void main(String[] args) throws Exception {

        PMTConnector pmtConnector = new PMTConnector();
        pmtConnector.execute();
    }

    private static String formatDate(String date) {
        String formattedDate = " NULL ";

        try {
            SimpleDateFormat from = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat to = new SimpleDateFormat("yyyy-MM-dd");
            Date releasedOn = from.parse(date);
            formattedDate = to.format(releasedOn);

        } catch (Exception e) {
            return formattedDate;
        }
        return "'" + formattedDate + "'";
    }


    @Override
    public void init(SynapseEnvironment synapseEnvironment) {
        this.synapseEnvironment = synapseEnvironment;
    }

    @Override
    public void destroy() {

    }

    @Override
    public void execute() {

        try {

            //Loading property files
            input = new FileInputStream(CARBON_HOME + File.separator + "repository" +
                    File.separator + "conf" + File.separator + "etc" + File.separator +
                    "dashboard-config.properties");

            // load a properties file
            prop.load(input);


            //get current timestamp
            Date date = new Date();
            Timestamp timestamp = new Timestamp(date.getTime());

            Registry registry = initialize();
            //We should use the UserRegisty to initialize the GenericArtifactManager
            registry = GovernanceUtils.getGovernanceUserRegistry(registry, "admin");

            //You should load governance Artifacts
            GovernanceUtils.loadGovernanceArtifacts((UserRegistry) registry);
            //"applications" is the short name of the RXT

            String[] patches = (String[]) registry.get("patchs").getContent();
            DocumentBuilder documentBuilder;

            // Read database configs from property file
            final String DB_URL = prop.getProperty("dburl");
            final String USER = prop.getProperty("dbusername");
            final String PASS = prop.getProperty("dbpassword");

            Connection conn;
            Statement stmt;
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);


            for (String patch : patches) {

                String patchRecords = RegistryUtils.decodeBytes((byte[]) registry.get(patch).getContent());
                documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                InputSource inputSource = new InputSource();
                inputSource.setCharacterStream(new StringReader(patchRecords));
                Document document = documentBuilder.parse(inputSource);
                Version defaultPlatformVersion = new Version("4.0.0");
                Version OverviewCarbonPlatform;

                try {
                    if (document.getElementsByTagName("carbonPlatform") != null) {
                        OverviewCarbonPlatform = new Version(document.getElementsByTagName("carbonPlatform").item(0)
                                .getTextContent());
                    } else {
                        OverviewCarbonPlatform = new Version("0.0.0");

                    }
                } catch (Exception e) {
                    OverviewCarbonPlatform = new Version("0.0.0");
                }

                if (defaultPlatformVersion.compareTo(OverviewCarbonPlatform) == -1 || defaultPlatformVersion.equals
                        (OverviewCarbonPlatform)) {

                    String MediaType = null;
                    String PatchID = null;
                    String LifecycleName = null;
                    String LifecycleState = null;
                    String JarsInvolved = null;
                    String OverviewIssueType = null;
                    String DateReleasedOn = null;
                    String OverviewName = null;
                    String TestAutomationAvailable = null;
                    String DateDevelopmentStartedOn = null;
                    String OverviewProducts = null;
                    String DateQAStartedOn = null;
                    String QAedBy = null;
                    String DevelopedBy = null;
                    String OverviewClient = null;

                    if (registry.get(patch).getMediaType() != null) {
                        MediaType = registry.get(patch).getMediaType();
                    }
                    if (registry.get(patch).getUUID() != null) {
                        PatchID = registry.get(patch).getUUID();
                    }
                    if (registry.get(patch).getProperty("registry.LC.name") != null) {
                        LifecycleName = registry.get(patch).getProperty("registry.LC.name");
                    }
                    if (registry.get(patch).getProperty("registry.lifecycle.PatchLifeCycle.state") != null) {
                        LifecycleState = registry.get(patch).getProperty("registry.lifecycle.PatchLifeCycle.state");
                    }
                    if (document.getElementsByTagName("jarsInvolved").getLength() == 1) {
                        JarsInvolved = document.getElementsByTagName("jarsInvolved").item(0).getTextContent();
                    }
                    if (document.getElementsByTagName("issueType").getLength() == 1) {
                        OverviewIssueType = document.getElementsByTagName("issueType").item(0).getTextContent();
                    }
                    if (document.getElementsByTagName("client").getLength() == 1) {
                        OverviewClient = document.getElementsByTagName("client").item(0).getTextContent().replace
                                ("'", "\\'");
                    }
                    if (document.getElementsByTagName("releasedOn").getLength() == 1) {
                        DateReleasedOn = formatDate(document.getElementsByTagName("releasedOn").item(0)
                                .getTextContent());
                    }
                    if (document.getElementsByTagName("name").getLength() == 1) {
                        OverviewName = document.getElementsByTagName("name").item(0).getTextContent();
                    }
                    if (document.getElementsByTagName("releasedOn").getLength() == 1) {
                        DateReleasedOn = formatDate(document.getElementsByTagName("releasedOn").item(0)
                                .getTextContent());
                    }
                    if (document.getElementsByTagName("automationavailable").getLength() == 1) {
                        TestAutomationAvailable = document.getElementsByTagName("automationavailable").item(0)
                                .getTextContent();
                    }
                    if (document.getElementsByTagName("developmentStartedOn").getLength() == 1) {
                        DateDevelopmentStartedOn = formatDate(document.getElementsByTagName("developmentStartedOn")
                                .item(0).getTextContent());
                    }
                    if (document.getElementsByTagName("products").getLength() == 1) {
                        OverviewProducts = document.getElementsByTagName("products").item(0).getTextContent();
                    }
                    if (document.getElementsByTagName("qaStartedOn").getLength() == 1) {
                        DateQAStartedOn = formatDate(document.getElementsByTagName("qaStartedOn").item(0)
                                .getTextContent());
                    }
                    if (document.getElementsByTagName("qaedby").getLength() == 1) {
                        QAedBy = document.getElementsByTagName("qaedby").item(0).getTextContent().replace("'", "\\'");
                    }
                    if (document.getElementsByTagName("developedby").getLength() == 1) {
                        DevelopedBy = document.getElementsByTagName("developedby").item(0).getTextContent();
                    }


                    stmt = conn.createStatement();

                    String sql3 = "INSERT INTO PMTData VALUES( NULL ,'" + timestamp + "','" + MediaType + "','" +
                            PatchID + "','" + LifecycleName + "','" + LifecycleState + "','" +
                            JarsInvolved + "','" +
                            OverviewIssueType + "'," + DateReleasedOn + ",'" +
                            OverviewName + "','" + TestAutomationAvailable + "'," +
                            DateDevelopmentStartedOn + ",'" + OverviewProducts + "'," +
                            DateQAStartedOn + ",'" + OverviewCarbonPlatform.version + "','" +
                            QAedBy + "','" + DevelopedBy + "','" + OverviewClient + "')";

                    /**System.out.println(sql3);
                     System.out.println
                     ("**********************************************************************************************************");
                     */
                    stmt.executeUpdate(sql3);
                    stmt.close();
                }
            }
            conn.close();

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (KeyManagementException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (RegistryException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
