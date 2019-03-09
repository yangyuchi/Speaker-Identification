package gui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.*;

import mfcc_processing.MFCC;
import mfcc_processing.WavWrite;
import neural_network.Network;

public class TrainProgressing {

	static ArrayList<double[]> TrainingPatterns;
	static ArrayList<ArrayList<File>> FilesForSpeakersNames;
	static int dimension_of_Features;
	static int number_of_Speakers;
	static int[] HidenNeuronsinputValues;
	static Network bp;
	static int tranfer_Function;
	static int costfunction;
	static double LearningRateFieldValue;
	static double MomentumFieldValue;
	static double MaxErrorRateFieldValue;
	static int maxIterationsFieldValue;
	
	static double idenRate = 0; 
	
	
    public static void createAndShowUI( ArrayList<double[]> TrainingPatterns1, ArrayList<ArrayList<File>> FilesForSpeakersNames1, int dimension_of_Features1, int number_of_Speakers1, int[] HidenNeuronsinputValues1,
    		int tranfer_Function1,int costfunction1, double LearningRateFieldValue1, double MomentumFieldValue1, double MaxErrorRateFieldValue1, int maxIterationsFieldValue1) {
        
    	
    	TrainingPatterns = TrainingPatterns1;
    	FilesForSpeakersNames = FilesForSpeakersNames1;
    	dimension_of_Features = dimension_of_Features1;
    	number_of_Speakers = number_of_Speakers1;
    	HidenNeuronsinputValues = HidenNeuronsinputValues1;
    	tranfer_Function = tranfer_Function1;
    	costfunction = costfunction1;
    	LearningRateFieldValue = LearningRateFieldValue1;
    	MomentumFieldValue = MomentumFieldValue1;
    	MaxErrorRateFieldValue = MaxErrorRateFieldValue1;
    	maxIterationsFieldValue = maxIterationsFieldValue1;
    	
    	TrainPBGui.myAttemptActionPerformed();
    
       
    }

    

    private TrainProgressing() {
    }
}

class TrainPBGui {

    private static JPanel mainPanel = new JPanel();

    public TrainPBGui() {
    	
    	myAttemptActionPerformed();
      
    }

   

    public static void myAttemptActionPerformed() {
        Window thisWin = SwingUtilities.getWindowAncestor(mainPanel);
        final JDialog progressDialog = new JDialog(thisWin, "Training...");
        JPanel contentPane = new JPanel();
        contentPane.setPreferredSize(new Dimension(300, 100));
        final JProgressBar bar = new JProgressBar(0, 100);
        bar.setIndeterminate(true);
        contentPane.add(bar);
        progressDialog.setContentPane(contentPane);
        progressDialog.pack();
        progressDialog.setLocationRelativeTo(null);
        final TrainTask task = new TrainTask();
        task.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equalsIgnoreCase("progress")) {
                    int progress = task.getProgress();
                    if (progress == 0) {
                        bar.setIndeterminate(true);
                    } else {
                        bar.setIndeterminate(false);
                        bar.setValue(progress);
                        progressDialog.dispose();
                    }
                }
            }
        });
        task.execute();
        progressDialog.setVisible(true);
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }
}

class TrainTask extends SwingWorker<Void, Void> {

    private static final long SLEEP_TIME = 0;
    private double idenRate = 0;

    public TrainTask() {
        
    }

    @Override
    public Void doInBackground() {
        setProgress(0);
        try {
            System.out.println("Train");
            int i = 0;
        	for(ArrayList<File> iteratorList:TrainProgressing.FilesForSpeakersNames)
			{
				i++;
        		for(File file:iteratorList)
				{
					MFCC mfcc = new MFCC(file.getAbsolutePath(),TrainProgressing.dimension_of_Features);
					ArrayList <double[]> Feature = mfcc.calcMFCC();
					ArrayList <double[]> Feature_with_Label = mfcc.addLabel(Feature, i, TrainProgressing.number_of_Speakers);
					TrainProgressing.TrainingPatterns.addAll(Feature_with_Label);						
				}			        					        		
        		
			}Thread.sleep(SLEEP_TIME);// imitate a long-running task
        	
			WavWrite wr1 = new WavWrite("StoredData/data1.txt");
        	wr1.storeFeatures(TrainProgressing.TrainingPatterns);
		
        	int[] NumberOfNodes = new int[2 + TrainProgressing.HidenNeuronsinputValues.length]; 
        	NumberOfNodes[0] = TrainProgressing.TrainingPatterns.get(0).length - TrainProgressing.number_of_Speakers;
        	int j=0;
        	for(j=0; j<TrainProgressing.HidenNeuronsinputValues.length; j++){
        		NumberOfNodes[j+1] = TrainProgressing.HidenNeuronsinputValues[j];			        		
        	}
        	NumberOfNodes[j+1] = TrainProgressing.number_of_Speakers;
        	//{dimension_of_Features, HidenNeuronsinputValues, number_of_Speakers}; //Hidden Neurons in Gui is here 20
        	
        	TrainProgressing.bp = new Network(NumberOfNodes, 
        			TrainProgressing.tranfer_Function,
        			TrainProgressing.costfunction,
        			TrainProgressing.LearningRateFieldValue, //Learning Rate in Gui 
        			TrainProgressing.MomentumFieldValue, // Momentum
        			TrainProgressing.MaxErrorRateFieldValue,  //Max Error
        			TrainProgressing.maxIterationsFieldValue) ; //Max Iterations
        	
		
        	TrainProgressing.bp.TrainNetwork(TrainProgressing.TrainingPatterns);
        	
        	
        	
        	setIdenRate(TrainProgressing.bp.identificationRate);
   			
        } catch (InterruptedException e) {
        }
        setProgress(100);
        return null;
    }

    @Override
    public void done() {
    	TrainProgressing.idenRate = this.getIdenRate();
    	UserInterface.IdentificationRate.setText("Identification Rate is: "+ String.format("%.1f", TrainProgressing.idenRate) +"%");
    	Toolkit.getDefaultToolkit().beep();
    	JOptionPane.showMessageDialog(UserInterface.frame, "Training Done!");
    	UserInterface.trainflag = true;
    	
    	
        
    }

	public double getIdenRate() {
		return idenRate;
	}

	public void setIdenRate(double idenRate) {
		this.idenRate = idenRate;
	}
}