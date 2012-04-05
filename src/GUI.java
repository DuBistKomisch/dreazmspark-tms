
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.*;

import java.sql.*;

public class GUI extends JFrame implements ActionListener, KeyListener {
	
	ArrayList<String> listStops = new ArrayList<String>();
	ArrayList<String> filterStopsArr = new ArrayList<String>();
	ArrayList<String> filterStopsDep = new ArrayList<String>();
	DefaultListModel depModel;
	DefaultListModel arrModel;
	DefaultListModel dayModel;
	
	JList dep;
	JList arr;
	JScrollPane arrList;
	JScrollPane depList;
	TextField depSearch = new TextField(14);
	TextField arrSearch = new TextField(14);
	JList day;
	JScrollPane dayList;
	JSpinner timeSpinner;	
	JButton go;

	JPanel pane = new JPanel(new GridBagLayout());
	GridBagConstraints a = new GridBagConstraints(); //gridx, gridy, gridwidth, gridheight, fill, ipadx, ipady, insets, anchor
	
	
	
	
	
	//GridLayout layout = new GridLayout(3,7);
	
	public static void main(String args[]) throws Exception {
		new GUI();
	}
	
	GUI () throws Exception
	{
		super("Dreazmspark Transport Management Solutions");

    Class.forName("org.sqlite.JDBC");

		setSize(1000,600);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container con = this.getContentPane();
		
		con.add(pane);

    Connection conn = DriverManager.getConnection("jdbc:sqlite:ptv.db");
    Statement stat =conn.createStatement();
    ResultSet rs = stat.executeQuery("select * from stations");
    while (rs.next())
    {
      listStops.add(rs.getString("name"));
		}
		
		depModel = new DefaultListModel();
		arrModel = new DefaultListModel();
		dayModel = new DefaultListModel();
		
		for (int i = 0; i < listStops.size(); i++)
		{
			depModel.addElement(listStops.get(i));
			arrModel.addElement(listStops.get(i));
		}
		
		dayModel.addElement("Monday");
		dayModel.addElement("Tuesday");
		dayModel.addElement("Wednesday");
		dayModel.addElement("Thursday");
		dayModel.addElement("Friday");
		dayModel.addElement("Saturday");
		dayModel.addElement("Sunday");
		
		dep = new JList(depModel);
		arr = new JList(arrModel);
		day = new JList(dayModel);
		
		arrList = new JScrollPane(arr);
		depList = new JScrollPane(dep);
		dayList = new JScrollPane(day);
		
		dep.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		arr.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		day.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		go = new JButton("GO!");

    timeSpinner = new JSpinner( new SpinnerDateModel() );
    JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "HH:mm");
    timeSpinner.setEditor(timeEditor);
    timeSpinner.setValue(new Date());
    
    	a.gridx = 0;
    	a.gridy = 0;
    	a.anchor = GridBagConstraints.LINE_START;
    	a.ipadx = 20;
    	
    	
		
		pane.add(new JLabel("Start Point"),a);
		a.gridx = 1;
		pane.add(new JLabel("Destination"),a);
		a.gridx = 2;
		pane.add(new JLabel("Time"),a);
		a.gridx = 3;
		pane.add(new JLabel("Day"),a);
		
		a.gridx = 0;
		a.gridy = 1;
		pane.add(depSearch,a);
		a.gridx = 1;
		pane.add(arrSearch,a);
		
		

		a.gridx = 0;
		a.gridy = 2;
		pane.add(depList,a);
		a.gridx = 1;
		pane.add(arrList,a);
		a.gridx = 2;
		pane.add(timeSpinner,a);
		a.gridx = 3;
		pane.add(dayList,a);
		
		a.gridy = 3;
		a.gridx = 0;
		a.gridwidth = 5;
		a.gridheight = 2;
		a.fill = GridBagConstraints.BOTH;
		a.ipady = 80;
		pane.add(go,a);
		
		depSearch.addKeyListener(this);
		arrSearch.addKeyListener(this);
		go.addActionListener(this);
		
		setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
		//WHAT HAPPENS WHEN YOU PRESS THE BIG GO BUTTON (Hopefully)
    System.out.println(timeSpinner.getValue());
	}

  public void updateLists() {
	
    //Refresh stop lists whenever search terms change.
    //Possible TODO: Update lists independently depending on which was changed so selection is not reset.
    filterStopsArr.clear();
    filterStopsDep.clear();
    depModel.clear();
    arrModel.clear();
    int i = 0;
	
      if (depSearch.getText().isEmpty()){
      for (int q = 0; q < listStops.size(); q++)
      {
        depModel.addElement(listStops.get(q));
      }
    } else {
      i = 0;
      for (String s : listStops)
      {
        if (s.toLowerCase().contains(depSearch.getText().toLowerCase())) {
          filterStopsDep.add(listStops.get(i));
          depModel.addElement(listStops.get(i));
        }
        i++;
      }
    }
      
    if (arrSearch.getText().isEmpty()){
      
      arrModel.clear();
      
      for (int q = 0; q < listStops.size(); q++)
      {
        arrModel.addElement(listStops.get(q));
      }
      
    } else {
      
      i = 0;
      for (String s : listStops)
      {
        if (s.toLowerCase().contains(arrSearch.getText().toLowerCase())) {
          filterStopsArr.add(listStops.get(i));
          arrModel.addElement(listStops.get(i));
        }
        i++;
      }
    }
  }

  public void keyPressed(KeyEvent arg0) {
    
    updateLists();
        
  }
      
    
  public void keyReleased(KeyEvent arg0) {
          
      updateLists();
          
  }


  public void keyTyped(KeyEvent e) {
  
		updateLists();
		
	}
}

