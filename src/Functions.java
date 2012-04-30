import java.sql.*;
import java.util.*;

public class Functions
{
  static Stack<Step> visited = new Stack<Step>();
  static PreparedStatement connectedQuery;
  static PreparedStatement timesQuery;
  
        // TODO use Dijkstra's algorithm to find best path, not just soonest option at each step
  public static Step[] findPath(String startingID, String endingID, int time, String day, Connection conn, boolean accessible, int maxChanges)
  {
    // convert strings to ints
    int departing, destination;
    try
    {
      Statement stat = conn.createStatement();
      ResultSet rs = stat.executeQuery("select id from stations where name = '" + startingID + "';");
      departing = rs.getInt("id");
      rs.close();
      rs = stat.executeQuery("select id from stations where name = '" + endingID + "';");
      destination = rs.getInt("id");
      rs.close();
      stat.close();
    }
    catch (SQLException e)
    {
      System.out.println(e.getMessage());
      e.printStackTrace(System.out);
      return null;
    }
    
    // find path
    try
    {
      // gets all destinations reachable from a source after a certain time
      connectedQuery = conn.prepareStatement("select destination from connections where source = ? and source_time >= ? and " + day + " = 1 " + (accessible ? "and accessible = 1 " : "") + "order by source_time asc, destination_time asc;");
      // gets soonest connection between source and destination after a certain time
      timesQuery = conn.prepareStatement("select source_time, destination_time from connections where source = ? and destination = ? and source_time >= ? and " + day + " = 1 " + (accessible ? "and accessible = 1 " : "") + "order by source_time asc, destination_time asc limit 1;");
      visited.clear();
      visited.push(new Step(departing, time, time));
      if (recursiveFunction(departing, destination, time, conn, maxChanges, true))
      {
        return visited.toArray(new Step[] {});
      }
      connectedQuery.close();
    }
    catch (SQLException e)
    {
      System.out.println(e.getMessage());
      e.printStackTrace(System.out);
    }
    
    return null;
  }

  private static boolean recursiveFunction(int station, int destination, int time, Connection conn, int changesLeft, boolean initial) throws SQLException
  {
    // success condition
    if (station == destination)
    {
      return true;
    }
    
    HashSet<Integer> destinations = new HashSet<Integer>();
    connectedQuery.setInt(1, station);
    connectedQuery.setInt(2, time);
    ResultSet rs = connectedQuery.executeQuery();
    while (rs.next())
    {
      destinations.add(rs.getInt("destination"));
    }
    rs.close();
    
    for (Integer d : destinations) {
      // check to see if station has been visited before
      boolean invalid = false;
      for (int i = 0; i < visited.size(); i++)
        if (visited.get(i).id == d)
          invalid = true;
      if (invalid)
        continue;

      // get next few times
      ArrayList<Integer[]> times = new ArrayList<Integer[]>();
      timesQuery.setInt(1, station);
      timesQuery.setInt(2, d);
      timesQuery.setInt(3, time);
      rs = timesQuery.executeQuery();
      while (rs.next())
      {
        times.add(new Integer[] {rs.getInt("source_time"), rs.getInt("destination_time")});
      }
      rs.close();
      
      // try this path
      for (Integer[] t : times)
      {
        // check if change
        boolean change = time < t[0] && !initial;
        if (change && changesLeft == 0)
          continue;

        visited.push(new Step(d, t[1], t[0]));
        if (recursiveFunction(d, destination, t[1], conn, changesLeft - (change ? 1 : 0), false))
          return true;
        visited.pop();
      }
    }
    
    return false;
  }
  
  public static class Step
  {
    int id, st, dt;
    String sh, sm, dh, dm;
    
    Step (int id, int dt, int st)
    {
      this.id = id;
      this.dt = dt;
      dh = ((Integer)(dt / 60)).toString();
      dm = ((Integer)(dt % 60)).toString();
      if (dm.length() == 1)
        dm = "0" + dm;
      this.st = st;
      sh = ((Integer)(st / 60)).toString();
      sm = ((Integer)(st % 60)).toString();
      if (sm.length() == 1)
        sm = "0" + sm;
    }
  }
}
