package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
//import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;

//import mfcc_processing.WavWrite;
import neural_network.Network;

    

public class UserInterface {
        
	static JFrame frame ;
	JProgressBar pbar;
	 
	static final int MY_MINIMUM = 0;
	
	static final int MY_MAXIMUM = 100;
	
	// Field members
	static JPanel panel = new JPanel();
	static Integer indexer = 1;
	static List<JLabel> listOfLabels = new ArrayList<JLabel>();
	private static List<JTextField> listOfTextFields = new ArrayList<JTextField>();
	
	static JPanel InputPanel;
	static JFileChooser chooser;
	static String choosertitle;
	
	private static int numClicks = 0;
	
	static JPanel TestPanel;
	static JPanel ResultPanel;
	static JLabel result;
	static JLabel IdentificationRate;
	//Parameters
	public static int dimension_of_Features=12;
	public static int number_of_Speakers = 0;
	
	static ArrayList<double[]> TrainingPatterns;
	static JComboBox<String> trasnsfer_functions;
	static JComboBox<String> cost_functions;
	static Network bp;
	static ArrayList<ArrayList<File>> FilesForSpeakersNames;
	static ArrayList<File> FilesForTestName; 	
    	
	static JTextField inputField;
	static JTextField HidenNeuronsinputField;
	static JTextField ErrorRate ;
	static JTextField lernRate ;
	static JTextField momentum;
	static JTextField maxIterations;
   	
	static boolean trainflag = false;
    	
    	
    public static void  showGui(){       
    	// Construct frame
        frame = new JFrame();
        frame.setLayout(new GridBagLayout());
        frame.setPreferredSize(new Dimension(1000, 800));
        frame.setTitle("Speaker Identification");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            
        JPanel ParametersPanel = new JPanel();
    	ParametersPanel.setLayout(new BoxLayout(ParametersPanel, BoxLayout.PAGE_AXIS));
    	
    	InputPanel = new JPanel();
//    	InputPanel.setLayout(new BoxLayout(InputPanel, BoxLayout.PAGE_AXIS));
    	
    	JPanel costFunction = new JPanel(new BorderLayout());
    	costFunction.add(new JLabel("Cost function", SwingConstants.LEFT), BorderLayout.PAGE_START);
    	cost_functions = new JComboBox<String>();
    	cost_functions.addItem("Mean Squared Error");
    	cost_functions.addItem("Cross Entropy");
        costFunction.add(cost_functions);
   
        JPanel selectNeurons = new JPanel(new BorderLayout());
        selectNeurons.add(new JLabel("Hidden neurons (space separated)", SwingConstants.LEFT), BorderLayout.PAGE_START);
        HidenNeuronsinputField = new JTextField(20);
        HidenNeuronsinputField.setText("50 30");
        selectNeurons.add(HidenNeuronsinputField);
            
        JPanel selectFunction = new JPanel(new BorderLayout());
        selectFunction.add(new JLabel("Transfer function", SwingConstants.LEFT), BorderLayout.PAGE_START);
        trasnsfer_functions = new JComboBox<String>();
        trasnsfer_functions.addItem("sigmoid");
        trasnsfer_functions.addItem("tanh");
        trasnsfer_functions.addItem("relu");
        trasnsfer_functions.addItem("softplus");
        selectFunction.add(trasnsfer_functions);

        JPanel chooseLernRate = new JPanel();
        chooseLernRate.setLayout(new BoxLayout(chooseLernRate, BoxLayout.LINE_AXIS));
        JLabel label2 = new JLabel("Learn Rate: ");
        chooseLernRate.add(label2);
        lernRate = new JTextField(5);
        lernRate.setText("0.001");
        chooseLernRate.add(lernRate);
        
        JPanel chooseMaxError = new JPanel();
        chooseMaxError.setLayout(new BoxLayout(chooseMaxError, BoxLayout.LINE_AXIS));
        JLabel label3 = new JLabel("Max Error:         ");
        chooseMaxError.add(label3);
        ErrorRate = new JTextField(5);
        ErrorRate.setText("0.03");
        chooseMaxError.add(ErrorRate);
        
        JPanel chooseMomentum = new JPanel();
        chooseMomentum.setLayout(new BoxLayout(chooseMomentum, BoxLayout.LINE_AXIS));
        JLabel label4 = new JLabel("Momentum:      ");
        chooseMomentum.add(label4);
        momentum = new JTextField(5);
        momentum.setText("0.9");
        chooseMomentum.add(momentum);

        JPanel chooseMaxIterations = new JPanel();
        chooseMaxIterations.setLayout(new BoxLayout(chooseMaxIterations, BoxLayout.LINE_AXIS));
        JLabel label5 = new JLabel("Max Iterations: ");
        chooseMaxIterations.add(label5);
        maxIterations = new JTextField(5);
        maxIterations.setText("2500");
        chooseMaxIterations.add(maxIterations);
        
        JPanel buttons = new JPanel(new BorderLayout());
        JButton btnBottom = new JButton("Train");
      
        btnBottom.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				//System.out.println("Number of Speakers found: "+number_of_Speakers);
				
				train_network();
	
			}
		
    	});
            
        buttons.add(btnBottom);
            
        ParametersPanel.add(costFunction);
        ParametersPanel.add(selectFunction);
        ParametersPanel.add(selectNeurons);
        ParametersPanel.add(chooseLernRate);
        ParametersPanel.add(chooseMaxError);
        ParametersPanel.add(chooseMomentum);
        ParametersPanel.add(chooseMaxIterations);
        ParametersPanel.add(buttons);
           
        IdentificationRate = new JLabel("Identification Rate:         ");
        ParametersPanel.add(IdentificationRate);
        
        FilesForSpeakersNames = new ArrayList<ArrayList<File>> ();
        FilesForTestName = new ArrayList<File>();
        
        // Frame constraints
        GridBagConstraints frameConstraints = new GridBagConstraints();

        // Construct button
        JButton addButton = new JButton("Click here to add Speaker");
        addButton.addActionListener(new ButtonListener());

        // Add button to frame
        frameConstraints.gridx = 0;
        frameConstraints.gridy = 0;
        frame.add(addButton, frameConstraints);

        // Construct panel
        panel.setPreferredSize(new Dimension(600, 600));
        panel.setLayout(new GridBagLayout());
        panel.setBorder(LineBorder.createBlackLineBorder());

        //TestPanel
        
        JButton sendBtn = new JButton("Browse...");
        sendBtn.addActionListener(new ActionListener() {

        	public void actionPerformed(ActionEvent e) {
				//int result;
        		
        		String FilesNames = "";
        		boolean flag = false;
        		
				chooser = new JFileChooser(); 
				chooser.setMultiSelectionEnabled(true);
			    chooser.setCurrentDirectory(new java.io.File("."));
			    chooser.setDialogTitle(choosertitle);
			    chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			    //
			    // disable the "All files" option.
			    //
			    chooser.setAcceptAllFileFilterUsed(false);
			    //
			    
			    if (chooser.showOpenDialog(chooser) == JFileChooser.APPROVE_OPTION) { 
			    				    	
			    	File[] Files = chooser.getSelectedFiles();
			    	
			    	for(File file:Files )
			    	{
			    		if(!file.isDirectory() && file.getName().endsWith("wav"))
			    		{
			    			FilesNames += file.getAbsolutePath() + ";";
			    		}else{
			    			if(file.isDirectory())
			    			{
			    				JOptionPane.showMessageDialog(frame, "The choosen Path is a directory, please choose a file!");
			    				//System.out.println("The choosen Path is a directory, please choose a file!");
			    				flag = false;
    			    			break;
			    			}
			    			if(!file.getName().endsWith("wav"))
			    			{
			    				JOptionPane.showMessageDialog(frame, "File must end with '.wav'!");
			    				//System.out.println("File must end with '.wav'");
			    				flag = false;
    			    			break;
			    			}
			    			
			    		}
			    		
			    		flag = true;
			    		System.out.println(file.getAbsolutePath());
			    	}
			    	
			    	if (flag)
			    	{
			    		FilesForTestName.clear();
			    		for(File file:Files )
			    		{
			    			FilesForTestName.add(file);
			    		}
			    		
			    	}
			    	
			    }    	
			    
        		if(flag)
        			inputField.setText(FilesNames);
        		else
        			inputField.setText("");
        	}	
			
		});
        JPanel inputPanel = new JPanel();
        inputField = new JTextField(45);
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.LINE_AXIS));
        inputPanel.add(sendBtn);
        inputPanel.add(inputField);
        

        JPanel youLabelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        youLabelPanel.add(new JLabel("Select a path:"));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        JButton test = new JButton("Test");
        
        test.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				
				test_network();
				
			}	
		});
        
        
        //-------------------
        
        
        //-----------------
        
        buttonPanel.add(test);
        
        TestPanel = new JPanel();
        TestPanel.setLayout(new BoxLayout(TestPanel, BoxLayout.PAGE_AXIS));
        //TestPanel.add(chatPanel);
        TestPanel.add(Box.createVerticalStrut(10));
        TestPanel.add(youLabelPanel);
        TestPanel.add(inputPanel);
        TestPanel.add(buttonPanel);
        
        //ResultPanel
        ResultPanel = new JPanel();
        ResultPanel.setLayout(new BoxLayout(ResultPanel, BoxLayout.PAGE_AXIS));
        //ResultPanel.add(chatPanel);
        ResultPanel.add(Box.createVerticalStrut(10));
        result  = new JLabel("                               Result:                                       ");
        ResultPanel.add(result);
        
        
     // Add panel to frame
        frameConstraints.gridx = 0;
        frameConstraints.gridy = 1;
        frameConstraints.weighty = 1;
        frame.add(panel, frameConstraints);
        
     // Add button to frame
        frameConstraints.gridx = 2;
        frameConstraints.gridy = 1;
        frame.add(ParametersPanel, frameConstraints);
       //Add testPanel to frame 
        frameConstraints.gridx = 0;
        frameConstraints.gridy = 2;
        frame.add(TestPanel, frameConstraints);
       
      //Add ResultPanel  
        frameConstraints.gridx = 2;
        frameConstraints.gridy = 2;
        frame.add(ResultPanel, frameConstraints);
        
        // Pack frame
        frame.pack();

        // Make frame visible
        frame.setVisible(true);
    }

    
    protected static void test_network() {
    	
    	if(FilesForTestName.isEmpty() ||  !trainflag)
		{
			
			if(!trainflag)
					
					JOptionPane.showMessageDialog(frame, "Please train the network!");
			//System.out.println("Test not possible!");
			else if(FilesForTestName.isEmpty())
				JOptionPane.showMessageDialog(frame, "Please add Speakers to test!");
			
		}else
		{	
		
			java.awt.EventQueue.invokeLater(new Runnable() {

	            @Override
	            public void run() {
	                
					TestProgressing.createAndShowUI(FilesForTestName, dimension_of_Features);	
					
	            }
	        });
			
			
			
		}
    	
    }
    
    
    
    
    protected static void train_network() {
		// TODO Auto-generated method stub
    	//Has to check if the Paths are the same or not
		
    	if(FilesForSpeakersNames.isEmpty())
    	{	
    		JOptionPane.showMessageDialog(frame, "There is no Train Material!");
    		//System.out.println("Train not possible!");
    	}
    	else
    	{	
    		
    		boolean start = true;
    		if(HidenNeuronsinputField.getText().equals(""))
    		{
    			JOptionPane.showMessageDialog(frame, "Please set the number of hidden Neurons");
    			start = false;
    		}
    		
    		if(lernRate.getText().equals(""))
    		{
    			JOptionPane.showMessageDialog(frame, "Please set the Learning Rate");
    			start = false;
    		}
    		
    		if(ErrorRate.getText().equals(""))
    		{
    			JOptionPane.showMessageDialog(frame, "Please set the max Error Rate");
    			start = false;
    		}
    		
    		int tranfer_Function = trasnsfer_functions.getSelectedIndex();
    		int cost_Function = cost_functions.getSelectedIndex();
    		
    		
    		
    		if(momentum.getText().equals(""))
    		{
    			JOptionPane.showMessageDialog(frame, "Please set the Momentum");
    			start = false;
    		}
    		
    		if(maxIterations.getText().equals(""))
    		{
    			JOptionPane.showMessageDialog(frame, "Please set the Max Iterations");
    			start = false;
    		}
    		
    		
    		if(start)
    		{	
        		
    			boolean check = true;
    			int[] HidenNeuronsinputValuesTmp = {};
    			double LearningRateFieldValueTmp = 0;
    			double MaxErrorRateFieldValueTmp = 0;
    			double MomentumFieldValueTmp = 0;
    			int maxIterationsFieldValueTmp = 0;
    			
    			
    			
    			
    				String[] strArray = HidenNeuronsinputField.getText().split(" ");
    				HidenNeuronsinputValuesTmp = new int[strArray.length];
    				for(int i = 0; i < strArray.length; i++) {
    					try {
    					HidenNeuronsinputValuesTmp[i] = Integer.parseInt(strArray[i]);
    					} catch (NumberFormatException e) {
            				JOptionPane.showMessageDialog(frame, "HidenNeurons Inputfield has to be an Integer");
            				check = false;
            				HidenNeuronsinputField.setText("");
            			}
    				}
    				
    				
    				
    			
        		
    			try {
    				LearningRateFieldValueTmp = Double.parseDouble(lernRate.getText());
    				
    			} catch (NumberFormatException e) {
    				JOptionPane.showMessageDialog(frame, "Learning Rate Inputfield has to be a double");
    				check = false;
    				lernRate.setText("");
    			}
        		
    			try {
    				MaxErrorRateFieldValueTmp = Double.parseDouble(ErrorRate.getText());
    			} catch (NumberFormatException e) {
    				JOptionPane.showMessageDialog(frame, "Max Error Inputfield has to be an Integer");
    				check = false;
    				ErrorRate.setText("");
    			}
        		
        		
        		
    			try {
    				MomentumFieldValueTmp = Double.parseDouble(momentum.getText());
    			} catch (NumberFormatException e) {
    				JOptionPane.showMessageDialog(frame, "Momentum Inputfield has to be an Integer");
    				check = false;
    				momentum.setText("");
    			}
        		
    			try {
    				maxIterationsFieldValueTmp = Integer.parseInt(maxIterations.getText());
    			} catch (NumberFormatException e) {
    				JOptionPane.showMessageDialog(frame, "Max Iterations Inputfield has to be an Integer");
    				check = false;
    				maxIterations.setText("");
        			}
        			
        			
        						        	
        			if(check)
        			{
        				TrainingPatterns = new ArrayList<double[]>(); 
        				final double LearningRateFieldValue = LearningRateFieldValueTmp;
        				final int[] HidenNeuronsinputValues = HidenNeuronsinputValuesTmp;
        				final double MomentumFieldValue = MomentumFieldValueTmp;
        				final double MaxErrorRateFieldValue = MaxErrorRateFieldValueTmp; 
        				final int maxIterationsFieldValue = maxIterationsFieldValueTmp;
        				
        				
        				java.awt.EventQueue.invokeLater(new Runnable() {

	    		            @Override
	    		            public void run() {
	    		                
									TrainProgressing.createAndShowUI(TrainingPatterns, FilesForSpeakersNames, dimension_of_Features, number_of_Speakers
											, HidenNeuronsinputValues, tranfer_Function, cost_Function, LearningRateFieldValue,
											MomentumFieldValue, MaxErrorRateFieldValue, maxIterationsFieldValue);
								
	    		            }
	    		        });
        				
        			}	
        		}
	        }
        }
        
  

		static class ButtonListener implements ActionListener
        {
            @Override
            public void actionPerformed(ActionEvent arg0) 
            {       
            	if(!listOfTextFields.isEmpty())
            	{
            		if(listOfTextFields.get(indexer-2).getText().equals(""))
        		{	
        			JOptionPane.showMessageDialog(frame, "Please select a wav Data for Speaker: "+(indexer-1));
        			//System.out.println("Please select a wav Data for Speaker: "+(indexer-1));
        		}
        		else
        		{
                	// Clear panel
                    panel.removeAll();

                    // Create label and text field
                    JTextField jTextField = new JTextField();
                    jTextField.setSize(100, 200);
                    listOfTextFields.add(jTextField);
                    listOfLabels.add(new JLabel("Speaker " + indexer));

                    // Create constraints
                    GridBagConstraints textFieldConstraints = new GridBagConstraints();
                    GridBagConstraints labelConstraints = new GridBagConstraints();
                    GridBagConstraints buttonConstraints = new GridBagConstraints();

                    // Add labels and text fields
                    for(int i = 0; i < indexer; i++)
                    {
                        
                    	numClicks = i;
                    	
                    	// Text field constraints
                        textFieldConstraints.gridx = 2;
                        textFieldConstraints.fill = GridBagConstraints.HORIZONTAL;
                        textFieldConstraints.weightx = 0.5;
                        textFieldConstraints.insets = new Insets(10, 10, 10, 10);
                        textFieldConstraints.gridy = i;

                        // Label constraints
                        labelConstraints.gridx = 0;
                        labelConstraints.gridy = i;
                        labelConstraints.insets = new Insets(10, 10, 10, 10);
                        
                        // Buttons constraints
                        buttonConstraints.gridx = 1;
                        buttonConstraints.gridy = i;
                        buttonConstraints.insets = new Insets(10, 10, 10, 10);
                        
                        JButton button = new JButton("Browse...");
                        
                        button.addActionListener(new ActionListener() {

                        	public void actionPerformed(ActionEvent e) {
                				//int result;
                        		
                        		
                				chooser = new JFileChooser(); 
                				chooser.setMultiSelectionEnabled(true);
                			    chooser.setCurrentDirectory(new java.io.File("."));
                			    chooser.setDialogTitle(choosertitle);
                			    chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                			    //
                			    // disable the "All files" option.
                			    //
                			    chooser.setAcceptAllFileFilterUsed(false);
                			    //    
                			    if (chooser.showOpenDialog(chooser) == JFileChooser.APPROVE_OPTION) { 
                			      
                			    	
                			    	//String Path = chooser.getSelectedFile().getAbsolutePath();
                			    	File[] Files = chooser.getSelectedFiles();
                			    	
                			    	String FilesNames = "";
                			    	boolean flag = true;
                			    	
                			    	
                			    	
                			    	for(File file:Files )
                			    	{
                			    		if(!file.isDirectory() && file.getName().endsWith("wav"))
                			    		{
                			    			FilesNames += file.getAbsolutePath() + ";";
                			    		}else
                			    		{
                			    			if(file.isDirectory())
                			    			{
                			    				JOptionPane.showMessageDialog(frame, "The choosen Path is a directory, please choose a file!");
                			    				//System.out.println("The choosen Path is a directory, please choose a file!");
                			    				flag = false;
                    			    			break;
                			    			}
                			    			if(!file.getName().endsWith("wav"))
                			    			{
                			    				JOptionPane.showMessageDialog(frame, "File must end with '.wav'");
                			    				//System.out.println("File must end with '.wav'");
                			    				flag = false;
                    			    			break;
                			    			}
                			    			
                			    		}
                			    		
                			    		
                			    		System.out.println(file.getAbsolutePath());
                			    	}
                			    	
                			    
                			    	if(flag)
                			    	{
                			    		
                			    		//FilesForSpeakersNames.clear();
                    			    	
                			    		ArrayList<File> myArrayList = new ArrayList<File>();
                			    		for(File file:Files)
                    			    	{
                			    			myArrayList.add(file);
                			    			
                    			    		
                    			    	}
                			    		FilesForSpeakersNames.add(myArrayList);
                			    		
                			    		listOfTextFields.get(numClicks).setText(FilesNames);
                			    	}else
                			    	{
                			    		listOfTextFields.get(numClicks).setText("");
                			    	}
                			    	
                			    	
                			    	/*System.out.println("getCurrentDirectory(): " 
                			         +  chooser.getCurrentDirectory());
                			      System.out.println("getSelectedFile() : " 
                			         +  chooser.getSelectedFile());
                			      System.out.println("Alooo: " +numClicks+ listOfTextFields.get(numClicks).getText());*/
                			    	number_of_Speakers = listOfTextFields.size();
                			      }
                			    else {
                			      
                			    	JOptionPane.showMessageDialog(frame, "No Selection ");
                			    	//System.out.println("No Selection ");
                			      
                			      }
                				
                			    
                			}

                			
                			
                		});
                        
                        // Add them to panel
                        panel.add(listOfLabels.get(i), labelConstraints);
                        panel.add(button, buttonConstraints);
                        panel.add(listOfTextFields.get(i), textFieldConstraints);
                    }

                    // Align components top-to-bottom
                    GridBagConstraints c = new GridBagConstraints();
                    c.gridx = 0;
                    c.gridy = indexer;
                    c.weighty = 1;
                    panel.add(new JLabel(), c);

                    // Increment indexer
                    indexer++;
                    panel.updateUI();
        			
        			
        		}
        	}else
        	{
        		// Clear panel
                panel.removeAll();

                // Create label and text field
                JTextField jTextField = new JTextField();
                jTextField.setSize(100, 200);
                listOfTextFields.add(jTextField);
                listOfLabels.add(new JLabel("Speaker " + indexer));

                // Create constraints
                GridBagConstraints textFieldConstraints = new GridBagConstraints();
                GridBagConstraints labelConstraints = new GridBagConstraints();
                GridBagConstraints buttonConstraints = new GridBagConstraints();

                // Add labels and text fields
                for(int i = 0; i < indexer; i++)
                {
                    
                	numClicks = i;
                	
                	// Text field constraints
                    textFieldConstraints.gridx = 2;
                    textFieldConstraints.fill = GridBagConstraints.HORIZONTAL;
                    textFieldConstraints.weightx = 0.5;
                    textFieldConstraints.insets = new Insets(10, 10, 10, 10);
                    textFieldConstraints.gridy = i;

                    // Label constraints
                    labelConstraints.gridx = 0;
                    labelConstraints.gridy = i;
                    labelConstraints.insets = new Insets(10, 10, 10, 10);
                    
                    // Buttons constraints
                    buttonConstraints.gridx = 1;
                    buttonConstraints.gridy = i;
                    buttonConstraints.insets = new Insets(10, 10, 10, 10);
                    
                    JButton button = new JButton("Browse...");
                    
                    button.addActionListener(new ActionListener() {

                    	public void actionPerformed(ActionEvent e) {
            				//int result;
                    		
                    		
            				chooser = new JFileChooser(); 
            				chooser.setMultiSelectionEnabled(true);
            			    chooser.setCurrentDirectory(new java.io.File("."));
            			    chooser.setDialogTitle(choosertitle);
            			    chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            			    //
            			    // disable the "All files" option.
            			    //
            			    chooser.setAcceptAllFileFilterUsed(false);
            			    //    
            			    if (chooser.showOpenDialog(chooser) == JFileChooser.APPROVE_OPTION) { 
            			      
            			    	
            			    	//String Path = chooser.getSelectedFile().getAbsolutePath();
            			    	File[] Files = chooser.getSelectedFiles();
            			    	
            			    	String FilesNames = "";
            			    	boolean flag = true;
            			    	
            			    	for(File file:Files )
            			    	{
            			    		if(!file.isDirectory() && file.getName().endsWith("wav"))
            			    		{
            			    			FilesNames += file.getAbsolutePath() + ";";
            			    		}else
            			    		{
            			    			if(file.isDirectory())
            			    			{
            			    				JOptionPane.showMessageDialog(frame, "The choosen Path is a directory, please choose a file!");
            			    				//System.out.println("The choosen Path is a directory, please choose a file!");
            			    				flag = false;
                			    			break;
            			    			}
            			    			if(!file.getName().endsWith("wav"))
            			    			{
            			    				JOptionPane.showMessageDialog(frame, "File must end with '.wav'");
            			    				//System.out.println("File must end with '.wav'");
            			    				flag = false;
                			    			break;
            			    			}
            			    			
            			    		}
            			    		
            			    		
            			    		
            			    	}
            			    	
            			    	if(flag)
            			    	{
            			    		
            			    		//FilesForSpeakersNames.clear();
            			    		ArrayList<File> myArrayList = new ArrayList<File>();
            			    		for(File file:Files)
                			    	{
            			    			myArrayList.add(file);
            			    			
                			    		
                			    	}
            			    		FilesForSpeakersNames.add(myArrayList);
            			    		
            			    		listOfTextFields.get(numClicks).setText(FilesNames);
            			    	}else
            			    	{
            			    		listOfTextFields.get(numClicks).setText("");
            			    	}
            			    	
            			    	
            			    	/*System.out.println("getCurrentDirectory(): " 
            			         +  chooser.getCurrentDirectory());
            			      System.out.println("getSelectedFile() : " 
            			         +  chooser.getSelectedFile());
            			      System.out.println("Alooo: " +numClicks+ listOfTextFields.get(numClicks).getText());*/
            			    	number_of_Speakers = listOfTextFields.size();
            			      }
            			    else {
            			      
            			    	JOptionPane.showMessageDialog(frame, "No Selection ");
            			    	//System.out.println("No Selection ");
            			      
            			      }
            				
            			    
            			}

            			
            			
            		});
                    
                    // Add them to panel
                    panel.add(listOfLabels.get(i), labelConstraints);
                    panel.add(button, buttonConstraints);
                    panel.add(listOfTextFields.get(i), textFieldConstraints);
                }

                // Align components top-to-bottom
                GridBagConstraints c = new GridBagConstraints();
                c.gridx = 0;
                c.gridy = indexer;
                c.weighty = 1;
                panel.add(new JLabel(), c);

                // Increment indexer
                indexer++;
                panel.updateUI();
        		
        	}
            	

                
                
            }
        }
    }