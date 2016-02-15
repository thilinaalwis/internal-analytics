package wso2;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.io.InputStream;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.startup.Task;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;




public class Builds implements Task, ManagedLifecycle {

    private SynapseEnvironment synapseEnvironment;
    static Properties prop = new Properties();
    InputStream input = null;
    private static final String CARBON_HOME = System.getProperty("carbon.home");

    @Override
    public void destroy() {
        // TODO Auto-generated method stub

    }

    @Override
    public void init(SynapseEnvironment arg0) {
        this.synapseEnvironment = synapseEnvironment;
    }

    @Override
    public void execute() {

        Connection conn;
        Statement stmt;
        String Product = "";
        ArrayList<Integer> Component = new ArrayList<Integer>();

        try {
            //Loading property files
            input = new FileInputStream(CARBON_HOME + File.separator + "repository" +
                    File.separator + "conf" + File.separator + "etc" + File.separator +
                    "dashboard-config.properties");

            // load a properties file
            prop.load(input);


            // Read database configs from property file
            final String DB_URL = prop.getProperty("dburl");
            final String USER = prop.getProperty("dbusername");
            final String PASS = prop.getProperty("dbpassword");


            String lastCompletedBuildStatus = "";
            String componentName = "";
            int lastFailedBuildNum;
            int lastSuccessfulBuildNum;
            int lastCompletedBuildNum;            //the build number of the previous build. //lastBuild refers to the
            boolean addComponentStatus;

            Date date = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

            //getting the list of products
            JSONObject jsonObjectnew;
            JSONObject jsonObject;
            System.out.println("Connecting to wso2 org Jenkins...");
            URL uri1 = new URL("https://wso2.org/jenkins/api/json");
            jsonObjectnew = new JsonReader().readJsonFromUrl(uri1);
            JSONArray component = (JSONArray) jsonObjectnew.get("jobs");

            for (int i = 0; i < component.length(); i++) {
                addComponentStatus = true;
                Component.clear();
                JSONObject jsonTemp = (JSONObject) component.get(i);
                componentName = jsonTemp.get("name").toString();
                //System.out.println("Product/Repo: "+componentName);

                // getting the snapshot for each product
                URL uri2 = new URL("https://wso2.org/jenkins/job/" + componentName + "/api/json");
                jsonObject = new JsonReader().readJsonFromUrl(uri2);

                String ComponentStatus = jsonObject.get("buildable").toString();
                //System.out.println(ComponentStatus);

                if (ComponentStatus.equalsIgnoreCase("true")) {

                    // get product lastCompletedBuild number
                    if (jsonObject.isNull("lastCompletedBuild")) {
                        lastCompletedBuildNum = 0;
                    } else {
                        JSONObject lastCompletedBuild = (JSONObject) jsonObject.get("lastCompletedBuild");
                        lastCompletedBuildNum = Integer.parseInt(lastCompletedBuild.get("number").toString());
                    }
                    // get product lastSuccessful build numbers
                    if (jsonObject.isNull("lastSuccessfulBuild")) {
                        lastSuccessfulBuildNum = 0;
                    } else {
                        JSONObject lastSuccessfulBuild = (JSONObject) jsonObject.get("lastSuccessfulBuild");
                        lastSuccessfulBuildNum = Integer.parseInt(lastSuccessfulBuild.get("number").toString());
                    }
                    // get product lastFailed build numbers
                    if (jsonObject.isNull("lastFailedBuild")) {
                        lastFailedBuildNum = 0;
                    } else {
                        JSONObject lastFailedBuild = (JSONObject) jsonObject.get("lastFailedBuild");
                        lastFailedBuildNum = Integer.parseInt(lastFailedBuild.get("number").toString());
                    }

                    // checking the status of the last build
                    if (lastCompletedBuildNum == lastSuccessfulBuildNum) {
                        lastCompletedBuildStatus = "Success";
                    } else if (lastCompletedBuildNum == lastFailedBuildNum) {
                        lastCompletedBuildStatus = "Failed";
                    } else {
                        lastCompletedBuildStatus = "Aborted";
                    }


                    Class.forName("com.mysql.jdbc.Driver");
                    conn = DriverManager.getConnection(DB_URL, USER, PASS);
                    stmt = conn.createStatement();

                    //check if component exists in the ComponentProduct table or not.
                    String queryComponents = "SELECT distinct Component = '" + componentName + "' as 'exist' FROM " +
                            "JNKS_COMPONENTPRODUCT";
                    ResultSet rsComp = stmt.executeQuery(queryComponents);

                    while (rsComp.next()) {
                        Component.add(rsComp.getInt("exist"));
                    }
                    //if Component arraylist consists of 1 at any location, addComponentStatus=false. Means the
                    // component exists.
                    for (int y = 0; y < Component.size(); y++) {
                        int p = (int) Component.get(y);
                        //System.out.println(p);
                        if (p == 1) {
                            addComponentStatus = false;
                            break;
                        }

                    }
                    //if component not available already, then add it to the ComponentProduct table as 'unknown' product
                    if (addComponentStatus) {
                        stmt = conn.createStatement();
                        String sql2 = "INSERT INTO JNKS_COMPONENTPRODUCT VALUES('unknown','" + componentName + "')";
                        stmt.executeUpdate(sql2);
                        //System.out.println("added " + componentName + " as 'unknown' product");
                    }
                    //Get the appropriate product for the component
                    String sqlComponents = "SELECT Product FROM JNKS_COMPONENTPRODUCT WHERE Component = '" +
                            componentName + "'";

                    ResultSet rs = stmt.executeQuery(sqlComponents);
                    while (rs.next()) {
                        Product = rs.getString("Product");
                        //System.out.println("Product is "+Product+ "\n");
                    }
                    //insert the current values to the BuildData table
                    stmt = conn.createStatement();
                    String sql = "INSERT INTO JNKS_BUILDDATA VALUES('" + dateFormat.format(date) + "','" + Product +
                            "','" + componentName + "','" + lastCompletedBuildStatus + "','" + lastCompletedBuildNum
                            + "')";
                    stmt.executeUpdate(sql);
                    stmt.close();
                    conn.close();
                }
            }
            //System.out.println("Total number of products/repos written to database: "+component.length());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
        }


    }//end of execute


    public static void main(String[] args) {
        // TODO Auto-generated method stub
        Builds tt = new Builds();
        tt.execute();
    }
}

