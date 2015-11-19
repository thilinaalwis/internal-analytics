package wso2;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import org.json.JSONException;
import org.json.JSONObject;

public class JsonReader {

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public static JSONObject readJsonFromUrl1(String urlToRead) throws IOException, JSONException {
        
        
        StringBuilder result = new StringBuilder();
        URL url = new URL(urlToRead);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        rd.close();
        JSONObject json = new JSONObject(result.toString());
            return json;
        
        
        
        
//        InputStream is = new URL(url).openStream();
//        try {
//            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
//            String jsonText = readAll(rd);
//            JSONObject json = new JSONObject(jsonText);
//            return json;
//        } finally {
//            is.close();
//        }
    }

    public JSONObject readJsonFromUrl(URL urlToRead)
            throws IOException, JSONException, NoSuchAlgorithmException, KeyManagementException {
        String serverHome = System.getProperty("carbon.home");

        System.setProperty("javax.net.ssl.trustStore", serverHome + "/repository/resources/security/client-truststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");

       // URL url = new URL("https://wso2.org/jenkins/api/json");
        InputStream inStream = this.getClass().getClassLoader().getResourceAsStream("wso2_org.cert");
        SSLExcludeCipherConnectionHelper sslExclHelper = new SSLExcludeCipherConnectionHelper(inStream);
        String result = sslExclHelper.get(urlToRead);
        //System.out.println(result);
        JSONObject json = new JSONObject(result);
        return json;
    }
}