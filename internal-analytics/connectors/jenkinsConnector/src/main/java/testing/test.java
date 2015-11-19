package testing;

import java.sql.Connection;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.startup.Task;

import java.text.SimpleDateFormat;
import java.util.Date;


public class test implements Task, ManagedLifecycle {
	 
	 private SynapseEnvironment synapseEnvironment;

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
		 
				// database URL
				final String DB_URL = "jdbc:mysql://127.0.0.1:3306/sf"; //"jdbc:mysql://127.0.0.1:9000/sf";

				// Database credentials
				final String USER = "root";//"sf";
				final String PASS = "root";//"sf!23";

				Connection conn = null;
				Statement stmt = null;
				String timestampArray[]=new String[5];
				int i=0;
				try {	
						Class.forName("com.mysql.jdbc.Driver");
						conn = DriverManager.getConnection(DB_URL, USER, PASS);
						stmt = conn.createStatement();
						String sql;
						sql = "SELECT * FROM BuildData WHERE Component='andes' limit 0,2";
						ResultSet rs = stmt.executeQuery(sql);
						
						while (rs.next()) {
							  String timestamp = rs.getString("Timestamp");
							  String component = rs.getString("Component");
							  String status = rs.getString("BuildStatus");
							  timestampArray[i++] = timestamp;
							}
						
						//Getting the time difference*************************************************
						
						String dateStart = timestampArray[0];//"01/14/2012 09:29:58";"2015-09-13 00:00";
						String dateStop = timestampArray[1];//"01/15/2012 10:31:48";"2015-09-15 14:30";

						//HH converts hour in 24 hours format (0-23), day calculation
						SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

						Date d1 = null;
						Date d2 = null;

							d1 = (Date) format.parse(dateStart);
							d2 = (Date) format.parse(dateStop);
//System.out.println("date is "+d1.getDate());
							//in milliseconds
							long diff = d2.getTime() - d1.getTime();

							long diffSeconds = diff / 1000 % 60;
							long diffMinutes = diff / (60 * 1000) % 60;
							long diffHours = diff / (60 * 60 * 1000) % 24;
							long diffDays = diff / (24 * 60 * 60 * 1000);

//							System.out.print(diffDays + " days, ");
//							System.out.print(diffHours + " hours, ");
//							System.out.print(diffMinutes + " minutes, ");
//							System.out.print(diffSeconds + " seconds.");
//
						//End of getting the time difference*************************************************
					
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			
	  
	 }//end of execute

	 
	 public static void main(String[] args) {
			// TODO Auto-generated method stub
		 test tt = new test();
		 tt.execute();
//System.out.println("Finished execution!");
		}
	}

