import java.sql.*;

public class DumpDatabase
{
  public static void main (String args[])
    throws Exception
  {
    Class.forName("org.sqlite.JDBC");
    Connection conn = DriverManager.getConnection("jdbc:sqlite:ptv.db");
    Statement stat = conn.createStatement();
    ResultSet rs = null;

    // print out stations
    try
    {
      rs = stat.executeQuery("select * from stations");
      System.out.println("stations");
      System.out.println("--------");
      System.out.println("id\tname");
      System.out.println("--\t----");
      while (rs.next())
      {
        System.out.printf("%d\t%s\n", rs.getInt("id"), rs.getString("name"));
      }
      System.out.println();
    }
    catch (SQLException e)
    {
      System.out.println(e.getMessage());
    }
    finally
    {
      if (rs != null)
        rs.close();
    }

    // print out connection count
    try
    {
      String days[] = {"monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"};
      System.out.println("connections");
      System.out.println("-----------");
      System.out.println("day       count   accessible");
      System.out.println("--------- ------- ----------");
      for (int i = 0; i < days.length; i++)
      {
        rs = stat.executeQuery("select count(*) from connections WHERE " + days[i] + " = 1");
        int total = 0;
        while (rs.next())
        {
          total = rs.getInt("count(*)");
        }
        rs.close();
        rs = stat.executeQuery("select count(*) from connections WHERE " + days[i] + " = 1 and accessible = 1");
        while (rs.next())
        {
          System.out.printf("%-9s %-7d %6.2f%%\n", days[i], total, 100.0 * rs.getInt("count(*)") / total);
        }
        rs.close();
      }
      rs = stat.executeQuery("select count(*) from connections");
      int total = 0;
      while (rs.next())
      {
        total = rs.getInt("count(*)");
      }
      rs.close();
      rs = stat.executeQuery("select count(*) from connections where accessible = 1");
      while (rs.next())
      {
        System.out.printf("total     %-7d %6.2f%%\n", total, 100.0 * rs.getInt("count(*)") / total);
      }
      rs.close();
      System.out.println();
    }
    catch (SQLException e)
    {
      System.out.println(e.getMessage());
    }
    finally
    {
      if (rs != null)
        rs.close();
    }

    conn.close();
  }
}
