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
      rs = stat.executeQuery("select count(*) from connections");
      System.out.println("connections");
      System.out.println("-----------");
      System.out.println("way too many to print, so here's the total number of rows:");
      while (rs.next())
      {
        System.out.println(rs.getInt("count(*)"));
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

    conn.close();
  }
}
