import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Stack;

public class Functions {

    static ArrayList<Integer> checked = new ArrayList<Integer>();
    static Stack<Integer> st = new Stack<Integer>();

    public static void main(String args[]) throws Exception {
	findPath("Belgrave", "East Camberwell");

    }

    public static void findPath(String startingID, String endingID) throws Exception {

	Class.forName("org.sqlite.JDBC");
	Connection conn = DriverManager.getConnection("jdbc:sqlite:ptv.db");
	int departing, destination;
	boolean reached = false;
	ResultSet rs;

	try {
	    Class.forName("org.sqlite.JDBC");
	} catch (ClassNotFoundException e) {
	    System.out.println("missing SQLite JDBC driver: run with -cp .:sqlitejdbc-vXXX.jar");
	    System.out.println("latest jar file available at http://www.zentus.com/sqlitejdbc/");
	    System.exit(0);
	}

	// open database
	try {
	    conn = DriverManager.getConnection("jdbc:sqlite:ptv.db");
	    Statement stat = conn.createStatement();
	    rs = stat.executeQuery("select id from stations where name = '" + startingID + "';");
	    departing = rs.getInt("id");
	    rs = stat.executeQuery("select id from stations where name = '" + endingID + "';");
	    destination = rs.getInt("id");
	    rs.close();
	    stat.close();

	  
	    boolean possible = recursiveFunction(departing, destination, conn);

	    if (possible == true) {
		while(st.size()!=0) {
		    System.out.println(st.pop());
		}
	    }
	} catch (SQLException e) {
	    System.out.println("couldn't create/access database: ptv.db");
	    System.out.println(e.getMessage());
	    e.printStackTrace(System.out);
	    System.exit(0);
	}
    }

    public static boolean recursiveFunction(int station, int destination, Connection conn) throws SQLException {
	ArrayList<Integer> connected = new ArrayList<Integer>();
	Statement stat = conn.createStatement();
	int j = 0;
	if (station == destination) {
	    st.push(station);

	    return true;
	} else {
	    ResultSet temp;

	    temp = stat.executeQuery("select distinct destination from connections where source = '" + station + "';");

	    while (temp.next()) {
		connected.add(temp.getInt("destination"));	//copy connecting stations into an arraylist
	    }
	    stat.close();	//close statement to avoid multiple statements being open when function recurses

	    while (j < connected.size()) {	
		checked.add(connected.get(j));
		boolean visited = false;

		for (int i = 0; i < checked.size() - 1; i++) {		//check to see if station has been visited before
		    if (checked.get(i) == connected.get(j)) {
			visited = true;

		    }
		}

		if (visited == false) {		
		    boolean path = recursiveFunction(connected.get(j), destination, conn); //run function again on next node
		    if (path == true) {		//if a path is found push station number onto stack
			st.push(station);
			return true;
		    }

		}
		j++;
	    }

	}

	return false;
    }
}
