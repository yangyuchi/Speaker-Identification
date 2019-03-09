package gui;
import java.awt.Dimension;
import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import javax.swing.*;
import mfcc_processing.MFCC;
import neural_network.Network;

public class TestProgressing {

	static ArrayList<File> FilesForTestName;
	static int dimension_of_Features;
	static Network bp;
	static int max_index;
	
	
    @SuppressWarnings("unused")
	public static void createAndShowUI(ArrayList<File> FilesForTestName1, int dimension_of_Features1)
    {
        
    	FilesForTestName = FilesForTestName1;
    	dimension_of_Features = dimension_of_Features1;
    	
    	
    	TestPBGui test = new TestPBGui();
    
       
    }

    

    private TestProgressing() {
    }
}

class TestPBGui {

    private JPanel mainPanel = new JPanel();

    public TestPBGui() {
    	
    	myAttemptActionPerformed();
      
    }

   

    private void myAttemptActionPerformed() {
        Window thisWin = SwingUtilities.getWindowAncestor(mainPanel);
        final JDialog progressDialog = new JDialog(thisWin, "Testing...");
        JPanel contentPane = new JPanel();
        contentPane.setPreferredSize(new Dimension(300, 100));
        final JProgressBar bar = new JProgressBar(0, 100);
        bar.setIndeterminate(true);
        contentPane.add(bar);
        progressDialog.setContentPane(contentPane);
        progressDialog.pack();
        progressDialog.setLocationRelativeTo(null);
        final TestTask task = new TestTask();
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

class TestTask extends SwingWorker<Void, Void> {

    private static final long SLEEP_TIME = 0;
    private double idenRate = 0;

    public TestTask() {
        
    }

    @Override
    public Void doInBackground() {
        setProgress(0);
        try {
           
        	
        	
        	Thread.sleep(SLEEP_TIME);// imitate a long-running task
        	
        	MFCC mfccTest = new MFCC(TestProgressing.FilesForTestName.get(0).getAbsolutePath(),TestProgressing.dimension_of_Features);
    		ArrayList <double[]> testFeature = mfccTest.calcMFCC();
    		
    		int index_max = TrainProgressing.bp.checkSpeaker(testFeature);
    		
    		
			
    		TestProgressing.max_index = index_max;
			
        } catch (InterruptedException e) {
        }
        setProgress(100);
        return null;
    }

    @Override
    public void done() {
    	
    	UserInterface.result.setText("This is speaker: "+TestProgressing.max_index);
    	
        
    }

	public double getIdenRate() {
		return idenRate;
	}

	public void setIdenRate(double idenRate) {
		this.idenRate = idenRate;
	}
}