import java.io.File;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactFilter;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PMTConnector {



    private static ConfigurationContext configContext = null;


    private static final String CARBON_HOME = "/Users/Chanuka/Downloads/wso2greg-4.6.0-2";
    private static final String axis2Repo = CARBON_HOME + File.separator + "repository" +
            File.separator + "deployment" + File.separator + "client";
    private static final String axis2Conf =
            ServerConfiguration.getInstance().getFirstProperty("Axis2Config.clientAxis2XmlLocation");
    private static final String username = "admin";
    private static final String password = "admin";
    private static final String serverURL = "https://192.168.3.66:9445/services/";

    private static WSRegistryServiceClient initialize() throws Exception {

        System.setProperty("javax.net.ssl.trustStore", CARBON_HOME + File.separator + "repository" +
                File.separator + "resources" + File.separator + "security" + File.separator +
                "wso2carbon.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
        System.setProperty("javax.net.ssl.trustStoreType", "JKS");
        System.setProperty("carbon.repo.write.mode", "true");
        configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(
                axis2Repo, axis2Conf);
        return new WSRegistryServiceClient(serverURL, username, password, configContext);

    }

    public static void main(String[] args) throws Exception {

        Registry registry = initialize();
        //We should use the UserRegisty to initialize the GenericArtifactManager
        registry = GovernanceUtils.getGovernanceUserRegistry(registry, "admin");
        //You should load governance Artifacts
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) registry);
        //"applications" is the short name of the RXT
        GenericArtifactManager artifactManager = new GenericArtifactManager(registry, "patch");

        // database URL
        final String DB_URL = "jdbc:mysql://localhost:3306/dashboard"; //"jdbc:mysql://127.0.0.1:9000/sf";

        // Database credentials
        final String USER = "root";
        final String PASS = "abc";

        //get current timestamp
        Date date= new Date();
        Timestamp timestamp = new Timestamp(date.getTime());

        Connection conn = null;
        Statement stmt = null;
        Class.forName("com.mysql.jdbc.Driver");
        conn = DriverManager.getConnection(DB_URL, USER, PASS);

        System.out.println("start");
        GenericArtifact[] artifacts;
        //artifacts = artifactManager.getAllGenericArtifacts();


        //Search for Application using attributes.
        GenericArtifact[] filteredArtifacts1 = artifactManager.getAllGenericArtifacts();
        GenericArtifact[] filteredArtifacts = artifactManager.findGenericArtifacts(
                new GenericArtifactFilter() {
                    public boolean matches(GenericArtifact artifact) throws GovernanceException {
                        String attributeVal = artifact.getAttribute("overview_products");
                        return (attributeVal != null && attributeVal.equals("G-Reg"));
                    }
                });
        //Print some search values
        for(GenericArtifact filter : filteredArtifacts1){
//            System.out.println( "overview_products : " +
//                    filter.getAttribute("overview_products") +", Platform Version " +filter.getAttribute
//                    ("overview_carbonPlatform")+
//                    " Id :" +filter.getId() + ", dates_developmentStartedOn :" + filter.getAttribute
//                    ("dates_developmentStartedOn") + ", dates_qaStartedOn :" + filter.getAttribute("dates_qaStartedOn") );

            SimpleDateFormat from = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat to = new SimpleDateFormat("yyyy-MM-dd");
            String dates_qaStartedOn = "0000-00-00";
            String dates_developmentStartedOn = "0000-00-00";
            String dates_releasedOn = "0000-00-00";



//            if(isValidDate(filter.getAttribute("dates_releasedOn"))) {
//                Date releasedOn = from.parse(filter.getAttribute("dates_releasedOn"));
//                dates_releasedOn = to.format(releasedOn);
//            }
//            if(isValidDate(filter.getAttribute("dates_developmentStartedOn"))) {
//                Date developmentStartedOn = from.parse(filter.getAttribute("dates_developmentStartedOn"));
//                dates_developmentStartedOn = to.format(developmentStartedOn);
//            }
//            if(isValidDate(filter.getAttribute("dates_qaStartedOn"))) {
//                Date qaStartedOn = from.parse(filter.getAttribute("dates_qaStartedOn"));
//                dates_qaStartedOn = to.format(qaStartedOn);
//            }


            stmt = conn.createStatement();


            String developedBy = filter.getAttribute("peopleInvolved_developedby");
            if(developedBy == "Won't verify"){
                System.out.println("###############");
            }
            if(developedBy!=null){
                developedBy.replaceAll("'", "\\'");
            }
            String sql2 = "INSERT INTO pmt VALUES( NULL ,'" + timestamp + "','" + filter.getQName() + "','" + filter
                    .getMediaType() + "','" + filter.getId() + "','" + filter.getLifecycleName() +"','" + filter
                    .getLifecycleState()+ "','" +filter.getAttribute("patchInformation_jarsInvolved")
                    +"','"  + filter.getAttribute("overview_issueType") + "','" + formatDate(filter.getAttribute("dates_releasedOn")) + "','" + filter
                    .getAttribute("overview_name")+"','"+filter.getAttribute("testInformation_automationavailable")
                    +"','" + formatDate(filter.getAttribute("dates_developmentStartedOn"))+"','"+ filter.getAttribute
                    ("overview_products")+"','"+ formatDate(filter.getAttribute("dates_qaStartedOn"))
                    +"','" + filter.getAttribute("overview_carbonPlatform")+"','"+
                    ""+"','"+ "" +"','"+filter.getAttribute
                    ("overview_client")+"')";


            System.out.println(sql2);
            System.out.println
                    ("***************************************************************************************************************");
            stmt.executeUpdate(sql2);
            stmt.close();
        }
        conn.close();
    }
    private static String formatDate(String date) {
        String formattedDate = "0000-00-00";

        try {
            SimpleDateFormat from = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat to = new SimpleDateFormat("yyyy-MM-dd");
            Date releasedOn = from.parse(date);
            formattedDate = to.format(releasedOn);

        } catch (Exception e) {
            return formattedDate;
        }
        return formattedDate;
    }
}
