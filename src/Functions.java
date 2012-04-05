import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Functions {
	
	public static void main(String args[]) throws Exception{
		findPath("Alamein","Flinders Street");
		
	}

	public static void findPath(String startingID, String endingID) throws Exception{
		
		Class.forName("org.sqlite.JDBC");
		Connection conn = DriverManager.getConnection("jdbc:sqlite:ptv.db");
		int departing, destination;
		boolean reached = false;
		ResultSet curr;
		
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			System.out
					.println("missing SQLite JDBC driver: run with -cp .:sqlitejdbc-vXXX.jar");
			System.out
					.println("latest jar file available at http://www.zentus.com/sqlitejdbc/");
			System.exit(0);
		}

		// open database
		try {
			conn = DriverManager.getConnection("jdbc:sqlite:ptv.db");
			Statement stat = conn.createStatement();
			ResultSet rs = stat.executeQuery("select id from stations where name = '" + startingID + "';");
			departing = rs.getInt("id");
			System.out.print(departing);
			rs = stat.executeQuery("select id from stations where name = '" + endingID + "';");
			destination = rs.getInt("id");
			System.out.print(destination);
			rs.close();
			
			while(reached==false){
			rs = stat.executeQuery("select distinct destination from connections where source = '" + departing + "';");
			while(rs.next()){
				
				int current = rs.getInt("destination");
				if(current==destination){
					reached = true;
					System.out.println("hooray");
				}
			}
			}
		} catch (SQLException e) {
			System.out.println("couldn't create/access database: ptv.db");
			System.out.println(e.getMessage());
			e.printStackTrace(System.out);
			System.exit(0);
		}
	}
	
	
	public boolean recursiveFunction(int station, int destination){
		
		
		
		
		
		
		
		
		return false;
		
		
	}
}
