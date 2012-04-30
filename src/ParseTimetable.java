import java.io.*;
import java.sql.*;
import java.util.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import javax.swing.text.html.parser.*;

/**
 * Parses HTML timetables into an sqlite db.
 */
public class ParseTimetable
{
  static Connection conn;
  static String mode;

  /**
   * @param args Command line arguments: file or directory to parse, and mode being "train" or "bus"
   */
  public static void main (String args[])
  {
    // import JDBC SQLite driver
    try
    {
      Class.forName("org.sqlite.JDBC");
    }
    catch (ClassNotFoundException e)
    {
      System.out.println("missing SQLite JDBC driver: run with -cp .:sqlitejdbc-vXXX.jar");
      System.out.println("latest jar file available at http://www.zentus.com/sqlitejdbc/");
      System.exit(0);
    }

    // open database
    try
    {
      conn = DriverManager.getConnection("jdbc:sqlite:ptv.db");
      Statement stat = conn.createStatement();
      stat.executeUpdate("create table if not exists stations (id integer primary key autoincrement, name string);");
      stat.executeUpdate("create table if not exists connections (source integer references stations (id), destination integer references stations (id), source_time integer, destination_time integer, monday boolean, tuesday boolean, wednesday boolean, thursday boolean, friday boolean, saturday boolean, sunday boolean, accessible boolean, primary key (source, destination, source_time, destination_time, monday, tuesday, wednesday, thursday, friday, saturday, sunday) on conflict ignore);");
    }
    catch (SQLException e)
    {
      System.out.println("couldn't create/access database: ptv.db");
      System.out.println(e.getMessage());
      System.exit(0);
    }

    // check filename was passed
    if (args.length < 1)
    {
      System.out.println("syntax: java ParseTimetable <file> <mode>");
      System.exit(0);
    }
    mode = args.length > 1 ? args[1] : "train";

    File files[] = {};
    try
    {
      // open file
      File root = new File(args[0]);
      if (root.isDirectory())
      {
        files = root.listFiles(new HTMLFileFilter());
      }
      else
      {
        files = new File[] {root};
      }
    }
    catch (Exception e)
    {
      System.out.println("couldn't access file: " + args[0]);
      System.exit(0);
    }

    // parse file(s)
    ParserDelegator delegator = new ParserDelegator();
    for (int i = 0; i < files.length; i++)
    {
      System.out.printf("[%3d%%] parsing %s\n", 100 * i / files.length, files[i].getName());
      try
      {
        delegator.parse(new FileReader(files[i]), new Callback(), true);
      }
      catch (Exception e)
      {
        System.err.println("unhandled error while parsing: " + e.getMessage());
        e.printStackTrace(System.err);
      }
    }

    // done
    System.out.println("[100%] parsed " + files.length + " files");
    try
    {
      conn.close();
    }
    catch (SQLException e)
    {
      System.err.println("couldn't close database connection");
    }
  }

  /**
   * Filters only .html files when a directory is passed.
   */
  public static class HTMLFileFilter implements FileFilter
  {
    public boolean accept (File f)
    {
      String name = f.getName();
      return name.substring(name.length() - 5).equals(".html");
    }
  }

  /**
   * Callbacks to process the HTML.
   */
  public static class Callback extends HTMLEditorKit.ParserCallback
  {
    // keeps track of open tags
    Stack<HTML.Tag> stack = new Stack<HTML.Tag>();

    int isProcessingStations = -1;
    ArrayList<Integer> stations = new ArrayList<Integer>(); // station id for each row of timetable
    
    int isProcessingTimetableRow = -1;
    ArrayList<ArrayList<Integer>> columns = new ArrayList<ArrayList<Integer>>(); // store timetable since html does row by row
    int currentColumn = -1;

    // which day(s) is/are the timetable for
    static int days[][] = {{1, 1, 1, 1, 1, 0, 0}, {1, 1, 1, 1, 0, 0, 0}, {0, 0, 0, 0, 1, 0, 0}, {0, 0, 0, 0, 0, 1, 0}, {0, 0, 0, 0, 0, 0, 1}};
    int whichDay = 0;
    int isProcessingFridayOnly = -1;
    ArrayList<Integer> fridayOnly = new ArrayList<Integer>();

    // accessible
    int isProcessingAccessible = -1;
    ArrayList<Boolean> accessible = new ArrayList<Boolean>();

    /** checks whether an attribute actually exists whatsoever, even if it has no value
     * e.g. "selected" in HTML such as <option value="abc" selected>abc</option>
     */
    public static boolean isDefinedAtAll (AttributeSet a, Object name)
    {
      for (Enumeration<?> e = a.getAttributeNames(); e.hasMoreElements(); )
        if (e.nextElement() == name)
          return true;
      return false;
    }

    /**
     * Handles opening a tag.
     *
     * Used to keep track of which section of the file we're in.
     */
    public void handleStartTag (HTML.Tag tag, MutableAttributeSet a, int pos)
    {
      stack.push(tag);

      // pad accessible
      if (tag == HTML.Tag.DIV && isProcessingAccessible > -1)
      {
        accessible.add(false);
      }

      // pad friday only
      if (tag == HTML.Tag.DIV && isProcessingFridayOnly > -1)
      {
        fridayOnly.add(0);
      }

      // weekdays
      if (tag == HTML.Tag.OPTION && a.containsAttribute(HTML.Attribute.VALUE, "T0") && isDefinedAtAll(a, HTML.Attribute.SELECTED))
      {
        whichDay = 0;
      }

      // saturday
      if (tag == HTML.Tag.OPTION && a.containsAttribute(HTML.Attribute.VALUE, "T2") && isDefinedAtAll(a, HTML.Attribute.SELECTED))
      {
        whichDay = 3;
      }

      // sunday
      if (tag == HTML.Tag.OPTION && a.containsAttribute(HTML.Attribute.VALUE, "UJ") && isDefinedAtAll(a, HTML.Attribute.SELECTED))
      {
        whichDay = 4;
      }

      // started processing accessible header
      if (tag == HTML.Tag.DIV && a.containsAttribute(HTML.Attribute.CLASS, "ttHeader") && a.isDefined(HTML.Attribute.STYLE))
      {
        isProcessingAccessible = stack.size();
      }

      // started processing friday only header
      if (tag == HTML.Tag.DIV && a.containsAttribute(HTML.Attribute.CLASS, "ttHeader") && !a.isDefined(HTML.Attribute.STYLE) && isProcessingFridayOnly == -1)
      {
        isProcessingFridayOnly = stack.size();
      }

      // started processing stations
      if (tag == HTML.Tag.DIV && a.containsAttribute(HTML.Attribute.ID, "ttMargin"))
      {
        isProcessingStations = stack.size();
      }
      
      /* WTF time:
         for no apparent reason, the parser seems to skip the open and the close callbacks for just the div with id=ttBR_row_1 
         i.e. the div containing the first row of the timetable
         to work around this, we assume we're processing the first row as soon as we hit the containing div with id=ttBody
         however, the stack size is now off by 1, so we have to set our finished processing trigger to one smaller
         luckily this is the last thing we're processing so we don't have to worry about anything later
         TODO better fix for this */
      if (tag == HTML.Tag.DIV && a.containsAttribute(HTML.Attribute.ID, "ttBody"))
      {
        isProcessingTimetableRow = stack.size() - 1;
        currentColumn = 0;
      }
      
      if (tag == HTML.Tag.DIV && (a.containsAttribute(HTML.Attribute.CLASS, "ttBodyTP") || a.containsAttribute(HTML.Attribute.CLASS, "ttBodyNTP")))
      {
        currentColumn = 0;
      }
    }

    /**
     * Handles tags of the form &lt;tag attr="foo"/&gt;. 
     *
     * Only used for detecting accessiblity images.
     */
    public void handleSimpleTag (HTML.Tag tag, MutableAttributeSet a, int pos)
    {
      // found accessible
      if (tag == HTML.Tag.IMG && isProcessingAccessible > -1)
      {
        accessible.set(accessible.size() - 1, true);
      }
    }

    /**
     * Handles close tags.
     *
     * Used to keep track of where we are.
     */
    public void handleEndTag (HTML.Tag tag, int pos)
    {
      stack.pop();

      // finished processing accesible header
      if (stack.size() < isProcessingAccessible)
      {
        isProcessingAccessible = -1;
      }

      // finished processing friday only header
      if (stack.size() < isProcessingFridayOnly)
      {
        isProcessingFridayOnly = -2; // ambiguous class above will match a second div!
      }

      // finished processing stations
      if (stack.size() < isProcessingStations)
      {
        isProcessingStations = -1;
      }

      // finished processing timetable row
      if (stack.size() < isProcessingTimetableRow)
      {
        isProcessingTimetableRow = -1;
      }
    }

    /**
     * Handles actual content.
     *
     * Most parsing is done here.
     */
    public void handleText (char data[], int pos)
    {
      HTML.Tag tag = stack.peek();
      String strData = new String(data);

      // found friday only
      if (tag == HTML.Tag.SPAN && isProcessingFridayOnly > -1)
      {
        if (strData.equals("Mo-Th"))
        {
          fridayOnly.set(fridayOnly.size() - 1, 1);
        }
        if (strData.equals("Fr"))
        {
          fridayOnly.set(fridayOnly.size() - 1, 2);
        }
      }

      // found a station
      if (isProcessingStations != -1 && tag == HTML.Tag.A)
      {
        int index = -1;
        if (mode.equals("train"))
          index = strData.indexOf(" Station");
        else
        {
          index = strData.indexOf(" Railway");
          if (index == -1)
            index = strData.indexOf(" (");
        }
        String station = strData.substring(0, index);

        try
        {
          Statement stat = conn.createStatement();

          ResultSet rs = stat.executeQuery("select id from stations where name = '" + station + "';");
          int id = -1;
          while (rs.next())
          {
            id = rs.getInt("id");
          }
          rs.close();

          if (id == -1)
          {
            // new station
            stat.executeUpdate("insert into stations (name) values ('" + station + "')");
            rs = stat.executeQuery("select id from stations where name = '" + station + "';");
            while (rs.next())
            {
              id = rs.getInt("id");
            }
            rs.close();
          }

          stations.add(id);
        }
        catch (SQLException e)
        {
          System.err.println(e.getMessage());
          e.printStackTrace(System.err);
        }
      }

      // found timetable data
      if (isProcessingTimetableRow != -1 && (tag == HTML.Tag.SPAN || tag == HTML.Tag.B))
      {
        if (columns.size() <= currentColumn)
          columns.add(new ArrayList<Integer>());

        String[] parts = strData.split(":");
        int time = -1;
        if (parts.length > 1)
        {
          time = Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
          if (tag == HTML.Tag.B)
          {
            if (time < 12 * 60)
              time += 12 * 60;
          }
          else
          {
            if (time > 12 * 60)
              time -= 12 * 60;
          }
        }

        columns.get(currentColumn).add(time);

        currentColumn++;
      }
    }

    /**
     * Handles done parsing, writes everything to database.
     */
    public void handleEndOfLineString (String eol)
    {
      // hooray for prepared statements
      PreparedStatement prep = null;
      try
      {
        prep = conn.prepareStatement("insert into connections values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
      }
      catch (Exception e)
      {
        System.err.println(e.getMessage());
        e.printStackTrace(System.err);
        return;
      }

      // process each column
      for (int c = 0; c < columns.size(); c++)
      {
        ArrayList<Integer> column = columns.get(c);
        int prevStation = -1;
        int prevTime = -1;

        // process each station
        for (int row = 0; row < column.size(); row++)
        {
          // if the train stops here
          if (column.get(row) > -1)
          {
            // if there was a previous station and it's not a "waiting at the station" step
            if (prevStation != -1 && prevStation != stations.get(row))
            {
              try
              {
                prep.setInt(1, prevStation);
                prep.setInt(2, stations.get(row));
                prep.setInt(3, prevTime);
                prep.setInt(4, column.get(row));
                for (int i = 0; i < 7; i++)
                  prep.setBoolean(5 + i, days[whichDay == 0 ? fridayOnly.get(c) : whichDay][i] != 0);
                prep.setBoolean(12, accessible.get(c));
                prep.addBatch();
              }
              catch (Exception e)
              {
                System.err.println(e.getMessage());
                e.printStackTrace(System.err);
              }
            }
            
            prevStation = stations.get(row);
            prevTime = column.get(row);
          }
        }
      }

      // wait to commit all updates at once, WAY faster
      int updateCount[] = {};
      try
      {
        conn.setAutoCommit(false);
        updateCount = prep.executeBatch();
        conn.commit();
        conn.setAutoCommit(true);
        prep.close();
      }
      catch (Exception e)
      {
        System.err.println(e.getMessage());
        e.printStackTrace(System.err);
      }

      // print stats
      int inserts = 0, duplicates = 0;
      for (int i = 0; i < updateCount.length; i++)
      {
        if (updateCount[i] > 0)
          inserts++;
        else if (updateCount[i] == 0)
          duplicates++;
        else
          System.out.println("???");
      }
      System.out.printf("%d rows, %d columns, %d inserts, %d duplicates\n", stations.size(), columns.size(), inserts, duplicates);
    }
  }
}
