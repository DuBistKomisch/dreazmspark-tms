import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import javax.imageio.ImageIO;
import javax.swing.*;

@SuppressWarnings("serial")
public class GUI extends JFrame implements ActionListener, KeyListener
{
	ArrayList<String> listStops = new ArrayList<String>();
	ArrayList<String> filterStopsArr = new ArrayList<String>();
	ArrayList<String> filterStopsDep = new ArrayList<String>();
	DefaultListModel depModel;
	DefaultListModel arrModel;
	
	ArrayList<String> displayStops = new ArrayList<String>();
	ArrayList<String> displayTimesOne = new ArrayList<String>();
	ArrayList<String> displayTimesTwo = new ArrayList<String>();
	ArrayList<String> displayTimesThree = new ArrayList<String>();
	
	JList dep;
	JList arr;
	JScrollPane arrList;
	JScrollPane depList;
	JTextField depSearch = new JTextField(14);
	JTextField arrSearch = new JTextField(14);
	JSpinner timeSpinner;	
	JButton go;

	JPanel pane = new JPanel(new GridBagLayout());
	GridBagConstraints a = new GridBagConstraints(); //gridx, gridy, gridwidth, gridheight, fill, ipadx, ipady, insets, anchor
	
	public static void main(String args[]) throws Exception
	{
		new GUI();
	}
	
	public GUI () throws Exception
	{
		// set up window
		super("Dreazmspark Transport Management Solutions");
		setSize(1000,600);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		Container con = this.getContentPane();
		con.add(pane);
		
		// import database driver and init connection
		Class.forName("org.sqlite.JDBC");
		Connection conn = DriverManager.getConnection("jdbc:sqlite:ptv.db");
		Statement stat = conn.createStatement();

		// get list of stations from the database
		try
		{
			ResultSet rs = stat.executeQuery("select * from stations");
			while (rs.next())
			{
				listStops.add(rs.getString("name"));
			}
		}
		catch (SQLException e)
		{
			System.out.println("Couldn't get list of stations from database.");
		}
		
		// set up lists
		depModel = new DefaultListModel();
		arrModel = new DefaultListModel();
		
		for (int i = 0; i < listStops.size(); i++)
		{
			depModel.addElement(listStops.get(i));
			arrModel.addElement(listStops.get(i));
		}
		
		dep = new JList(depModel);
		arr = new JList(arrModel);
		
		arrList = new JScrollPane(arr);
		depList = new JScrollPane(dep);
		
		dep.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		arr.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		dep.setSelectedIndex(0);
		arr.setSelectedIndex(0);
		
		// set up date selector
		timeSpinner = new JSpinner(new SpinnerDateModel());
		timeSpinner.setEditor(new JSpinner.DateEditor(timeSpinner, "E HH:mm"));
		timeSpinner.setValue(new Date());
		
		// most important line of code
		go = new JButton("GO!");

		// splash animation frame 1
   		a.gridx = 0;
   		a.gridy = 0;
    	JLabel title = new JLabel("<html><FONT COLOR=RED SIZE=40>DREAZMSPARK PRESENTS</FONT></html>");
    	pane.add(title,a);
    	
    	a.gridy = 1;
    	a.insets = new Insets(100, 0, 0, 0);
    	BufferedImage myPicture = ImageIO.read(new File("content/turtle.png"));
    	JLabel picLabel = new JLabel(new ImageIcon( myPicture ));
    	pane.add(picLabel,a);
    	
    	int p = 0;
    	setVisible(true);
    	for (int i = 0; i < 30; i++)
    	{
    		Toolkit.getDefaultToolkit().beep(); 
    		if (p == 0)
    		{
    			p = 1;
    			pane.setBackground(Color.cyan);
    		}
    		else
    		{
    			p = 0;
    			pane.setBackground(Color.green);
    		}
    		waiting(1);
    	}
    	
    	pane.remove(title);
    	pane.remove(picLabel);
    	this.repaint();
    	
    	// splash animation frame 2
    	a.insets = new Insets(0, 0, 0, 0);
    	JLabel produced = new JLabel("<html><FONT COLOR=#00FFFF SIZE=30>PRODUCED BY:<br><br>Harry<br>Ashley<br>and Jake</FONT></html>");
    	pane.add(produced,a);
        
    	setVisible(true);
    	for (int i = 0; i < 30; i++)
    	{
        	Toolkit.getDefaultToolkit().beep();
        	if (p == 0)
        	{
        		p = 1;
        		pane.setBackground(Color.red);
        	}
        	else
        	{
        		p = 0;
        		pane.setBackground(Color.yellow);
        	}
        	waiting(1);
        }
    	
    	pane.remove(produced);
    	this.repaint();
    	
    	// splash animation frame 3
    	a.gridx = 0;
   		a.gridy = 0;
    	JLabel name = new JLabel("<html><CENTER><FONT COLOR=GREEN SIZE=80>TRANSPORT MANAGEMENT SYSTEM<br><br>PRO EDITION</FONT></CENTER></html>");
    	pane.add(name, a);
    	
    	setVisible(true);
    	for (int i = 0; i < 30; i++)
    	{
        	Toolkit.getDefaultToolkit().beep();
        	if (p == 0)
        	{
        		p = 1;
        		pane.setBackground(Color.pink);
        	}
        	else
        	{
        		p = 0;
        		pane.setBackground(Color.black);
        	}
        	waiting(1);
        }
    	
    	pane.remove(name);
    	this.repaint();
    	
    	// actual application
    	pane.setBackground(Color.yellow);
		
    	a.gridx = 0;
    	a.gridy = 0;
    	a.anchor = GridBagConstraints.LINE_START;
    	a.ipadx = 20;
    	a.ipady = 0;
    	a.fill = GridBagConstraints.NONE;
    	a.insets = new Insets(0, 50, 20, 50);
		
		pane.add(new JLabel("Start Point"), a);
		a.gridx = 1;
		pane.add(new JLabel("Destination"), a);
		a.gridx = 2;
		pane.add(new JLabel("Day & Time"), a);
		
		a.insets = new Insets(0,50,0,50);
		a.gridx = 0;
		a.gridy = 1;
		pane.add(depSearch, a);
		a.gridx = 1;
		pane.add(arrSearch, a);
		a.gridx = 2;
		pane.add(timeSpinner, a);
		
		a.gridx = 0;
		a.gridy = 2;
		pane.add(depList, a);
		a.gridx = 1;
		pane.add(arrList, a);
		
		a.gridy = 3;
		a.gridx = 0;
		a.gridwidth = 4;
		a.gridheight = 2;
		a.fill = GridBagConstraints.BOTH;
		a.ipady = 80;
		a.insets = new Insets(50, 50, 0, 50);
		pane.add(go,a);
		
		depSearch.addKeyListener(this);
		arrSearch.addKeyListener(this);
		go.addActionListener(this);
		
		Toolkit.getDefaultToolkit().beep();
		setVisible(true);
	}

	// on pressing GO!
	public void actionPerformed(ActionEvent e)
	{
		// TODO get results from Functions
		for (int i = 0; i < 70; i++){
			displayStops.add("SampleStop");
			displayTimesOne.add("TimeOne");
			displayTimesTwo.add("TimeTwo");
			displayTimesThree.add("TimeThree");
		}
		
		JPanel results = new JPanel(new GridBagLayout());
		JScrollPane Scrolltastic = new JScrollPane(results);
		
		pane.removeAll();
		Toolkit.getDefaultToolkit().beep();
		
		//Scrolltastic.add(pane);
		
		a.gridx = 0;
		a.gridy = 0;
		a.gridwidth = 1;
		a.gridheight = 1;
		a.fill = GridBagConstraints.NONE;
		a.ipady = 0;
		a.ipadx = 0;

		for (int i = 0; i < displayStops.size(); i++)
		{
			a.gridx = 0;
			results.add(new JLabel(displayStops.get(i)), a);
			a.gridx = 1;
			results.add(new JLabel(displayTimesOne.get(i)), a);
			a.gridx = 2;
			results.add(new JLabel(displayTimesTwo.get(i)), a);
			a.gridx = 3;
			results.add(new JLabel(displayTimesThree.get(i)), a);
			
			a.gridy = i + 1;
		}
		
		pane.setLayout(new BorderLayout());
		pane.add(Scrolltastic);
		this.repaint();
		setVisible(true);
	}

	// Refresh stop lists whenever search terms change.
	public void updateLists(Component c) {
		Toolkit.getDefaultToolkit().beep();
		if (c == depSearch)
		{
			filterStopsDep.clear();
			depModel.clear();
			
			if (depSearch.getText().isEmpty())
			{
				for (int q = 0; q < listStops.size(); q++)
				{
					depModel.addElement(listStops.get(q));
				}
			}
			else
			{
				int i = 0;
				for (String s : listStops)
				{
					if (s.toLowerCase().contains(depSearch.getText().toLowerCase()))
					{
						filterStopsDep.add(listStops.get(i));
						depModel.addElement(listStops.get(i));
					}
					i++;
				}
			}
			
			dep.setSelectedIndex(0);
		}
		else if (c == arrSearch)
		{
			filterStopsArr.clear();
			arrModel.clear();
		
			if (arrSearch.getText().isEmpty())
			{
				arrModel.clear();
				for (int q = 0; q < listStops.size(); q++)
				{
					arrModel.addElement(listStops.get(q));
				}
			}
			else
			{
				int i = 0;
				for (String s : listStops)
				{
					if (s.toLowerCase().contains(arrSearch.getText().toLowerCase()))
					{
						filterStopsArr.add(listStops.get(i));
						arrModel.addElement(listStops.get(i));
					}
					i++;
				}
			}
			
			arr.setSelectedIndex(0);
		}
	}

	public void keyPressed(KeyEvent e)
	{
		updateLists(e.getComponent());
	}
      
	public void keyReleased(KeyEvent e)
	{
		updateLists(e.getComponent());
	}

	public void keyTyped(KeyEvent e)
	{  
		updateLists(e.getComponent());
	}
  
	// TODO better animation method
	public static void waiting (int n)
	{
		long t0, t1;
		t0 =  System.currentTimeMillis();
		do
		{
			t1 = System.currentTimeMillis();
		}
		while ((t1 - t0) < (n * 100));
	}
}

