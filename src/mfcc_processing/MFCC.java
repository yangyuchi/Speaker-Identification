package mfcc_processing;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * MFCC feature extraction, take raw .wav data as input, extract a list of MFCC vectors
 * @author yangyuchi
 *
 */
public class MFCC {
	
	private String Path; //Path of the wave file
	final private static double Alpha = 0.97; //preEmphasis coefficient
	private ArrayList<double[]> frameList = new ArrayList<double[]>();
	private ArrayList<double[]> frameListAfterHamming = new ArrayList<double[]>();
	private ArrayList<double[]> frameListAfterFFT = new ArrayList<double[]>();
	private ArrayList<double[]> CepstrumList = new ArrayList<double[]>();
	private ArrayList<double[]> originalFeature = new ArrayList<double[]>();
	final private static int frameLength = 512;	//length of a frame, such that a frame lasts around 30ms
	public static double hamming[] = {0.0}; //Hamming window
	private int numMFCC;
		
	/**
	 * Constructor
	 * @param Path(the absolute path of .wav data)
	 * @param numberofFeatures(dimensionality of MFCC features)
	 */
	public MFCC(String Path, int numberofFeatures)
	{
		this.Path = Path;	
		numMFCC=numberofFeatures;
	}
	
	/**
	 * take a .wav data as input, return normalized MFCC feature list (after K-Means)
	 * @return ArrayList of features
	 */
	public ArrayList <double[]> calcMFCC()
	{
		WavRead wavread = new WavRead(Path);
		double[] wav = wavread.getDoubleArray();	
		double[] rec_wav = rectify(wav);
		double[] emf_wav = preEmphasis(rec_wav);
		framing(emf_wav);
		applyHamming();
		applyFFT();		
		//generate Mel filter banks
		MelFilterBank melbank = new MelFilterBank(512, wavread.getFs(), 26);
		DCT(melbank.filters);
		addDelta();
		KMeans km;
		if(CepstrumList.size()>1000){
			km = new KMeans(CepstrumList, 500, 15, 0.05);
			originalFeature = km.getPrototypes();
		}else{
			originalFeature = CepstrumList;
		}	
		ArrayList <double[]> normalizedFeature = normalize(originalFeature);
		
		return normalizedFeature;
	}
	
	/**
	 * remove the DC component which is not related to frequency analysis
	 * @param wav(wav data in double)
	 * @return rectified .wav data
	 */
	private double[] rectify(double[] wav) {
		double sum = 0.0;
		for (int i=0;i<wav.length;i++)
			sum += wav[i];
		double avg = sum/wav.length;
		for (int i=0;i<wav.length;i++)
			wav[i] = wav[i]-avg;
		return wav;
	}

	/**
	 * preEmphasis enhance the high frequency part of voice signal by taking y[n]=x[n]-a*x[n-1]
	 * @param inputSignal(rectified .wav data in double[])
	 * @return signal after pre-emphasis
	 */
	public double[] preEmphasis(double inputSignal[]){
		final double outputSignal[] = new double[inputSignal.length];
		for (int n =1;n<inputSignal.length;n++){
			outputSignal[n]=(double)(inputSignal[n]-Alpha*inputSignal[n-1]);
		}
		return outputSignal;
	}

	/**
	 * divide original wave data into 30ms frames, each frame contains 512 digits
	 * step is 256 digits so that there is 256 digits overlap between 2 neighboring frames
	 * after that we get a list of frames
	 * @param originalData(whole .wav data)
	 */
	
	public void framing(double[] wavData)
	{			
		int originalDataLength = wavData.length;		
		for(int i=0;i<originalDataLength; i++){
			
			if((i%256)==0){
				double[] frame = new double[frameLength];
				for(int j=0;j<frameLength;j++){				
					if(i+j>=originalDataLength)
						break; 
					frame[j]=wavData[i+j];			
				}
				
				frameList.add((frame));
			}					
		}				
	}
	
	/**
	 * generate hamming window used for windowing
	 */
	private void hamming()
	{
		hamming = new double[frameLength];
		for(int i = 0;i < frameLength;i++)
		{ 
	        hamming[i] = 0.54 - 0.46 * Math.cos(2 * Math.PI * (i)/(frameLength-1)); 	       		
		}
	}
	
	/**
	 * apply windowing to frames, multiply each frame with the hamming window
	 */
	public void applyHamming() 
	{ 	
		hamming(); //generate hamming window
		for(double[] element:frameList){
			double[] window_frame = new double[frameLength];
			for(int i = 0;i < frameLength;i++){ 			
		        window_frame[i] =  hamming[i]*element[i];		
			}
			frameListAfterHamming.add((window_frame));		
		}	    	    
	} 

	/**
	 * apply discrete Fourier transformation, get the power spectrum
	 */
	public void applyFFT() 
	{ 	
		for(double[] element:frameListAfterHamming){
			Complex[] c_frame = new Complex[frameLength];
			for (int n=0;n<frameLength;n++){
				c_frame[n]=new Complex(element[n],0.0);
			}
			Complex[] fft_frame = FFT.fft(c_frame);
			//convert complex spectrum to real value power
			double[] FFT_frame = new double[frameLength];
			for (int n=0;n<frameLength;n++){
				FFT_frame[n]=Math.pow(fft_frame[n].abs(),2);
			}			
			frameListAfterFFT.add(FFT_frame);		
		}
	}
	
	/**
	 * apply discrete cosine transformation, get MFCC coefficients	
	 * @param melbank(Mel filter banks)
	 */
	public void DCT(double[][] melbank)
	{
		int M=melbank.length; //number of filters in the bank
		for(double[] spectrum:frameListAfterFFT){
			//First pass the power spectrum through Mel filter banks and take logarithm
			double[] log_Energy = new double[257]; //Spectrum is symmetric, only need the first 257 digits
			for (int m=0;m<M;m++){
				double S=0.0;
				for (int k=0;k<257;k++){
					
					S+=spectrum[k]*melbank[m][k];
					
				}
				log_Energy[m]=Math.log(S);
			}
			//discrete cosine transformation
			double[] c = new double[numMFCC];
			for (int n=0;n<numMFCC;n++){
				double sum =0.0;
				for (int m=0;m<M;m++){
					
					sum = sum + log_Energy[m]*Math.cos(Math.PI*(n+1)*(m-0.5)/M);
				
				}
				c[n]=sum;
			}
			CepstrumList.add(c);		
		}	
	}
	
	/**
	 * add Delta(Dynamic) information of MFCC
	 */
	public void addDelta()
	{
		for(int t = 2;t<(CepstrumList.size()-2);t++){
			double[] MFCCwithDelta=Arrays.copyOf(CepstrumList.get(t), numMFCC*2);
			for (int i=0;i<numMFCC;i++){				
				MFCCwithDelta[i+numMFCC] = (CepstrumList.get(t+1)[i]-CepstrumList.get(t-1)[i]
						+ 2*(CepstrumList.get(t+2)[i]-CepstrumList.get(t-2)[i]))/10;
			}
			CepstrumList.set(t, MFCCwithDelta);
		}	
		//remove the first and last 2 frames which don't have delta information
		CepstrumList.remove(0);
		CepstrumList.remove(0);
		CepstrumList.remove(CepstrumList.size()-1);
		CepstrumList.remove(CepstrumList.size()-1);
	}

	/**
	 * Get the MFCC feature list
	 * @return Cepstrum vector as a list
	 */
	public ArrayList<double[]> getCepstrumList()
	{
		return CepstrumList;
	}
	
	/**
	 * Feature Normalized to [-1,1], otherwise network cannot be trained
	 * @param inputFeature(Feature before normalization)
	 * @return Feature after normalization
	 */
	public static ArrayList<double[]> normalize(ArrayList <double[]> inputFeature)
	{
		
		
		int row = 0;
		//normalize each feature to range [-1,1]
		for (double[] arr : inputFeature) {
			
			double[] temp = new double[arr.length];
			double x_min = getMin(arr); 
			double x_max = getMax(arr);
			for(int i = 0; i<arr.length;i++)
			{	
				double x_std = (arr[i]-x_min)/(x_max-x_min);
				double x_scaled = (x_std * (1-(-1)))+(-1);
				temp[i] = x_scaled;				
			}
			inputFeature.set(row, temp);
			row++;
		}
					
		return inputFeature;
	
	}
	
	/**
	 * get maximum value of an array
	 * @param arr(input vector)
	 * @return maximum value of the vector
	 */
	public static double getMax(double[] arr) {
		
		double Max = arr[0];
		for(int i=0; i<arr.length; i++)
		{
			if (arr[i]>Max)
				Max = arr[i];
		}
		
		return Max;
	}
	
	/**
	 * get minimum value of an array
	 * @param arr(input vector)
	 * @return minimum value of the vector
	 */
	private static double getMin(double[] arr) {

		double Min = arr[0];
		for(int i=0; i<arr.length; i++)
		{
			if (arr[i]<Min)
				Min = arr[i];
		}
		
		return Min;
	}
		
	/**
	 * add the labels for the feature vector, the labels and feature vectors together make
	 * the training patterns. Each feature should get one label, with the format 
	 * [0 0 1 0](speaker 3)[1 0 0 0](speaker 1)etc....

	 * @param Features(MFCC feature vectors)
	 * @param speakerID(the label of this known speaker)
	 * @param totalNumber(total number of speakers)
	 * @return Feature with labels
	 */
	public ArrayList <double[]> addLabel(ArrayList <double[]> Features, int speakerID, int totalNumber) {

		int count = 0;
		for(double[] arr : Features){
			//copy the feature vector, add space for labels
			double[] temp=Arrays.copyOf(arr, arr.length+totalNumber);
			for(int j=0; j<totalNumber;j++)
			{
				if(j==(speakerID-1))
					temp[arr.length + j] = 1;
				else
					temp[arr.length + j] = 0;
			}
			Features.set(count, temp);
			count++;
		}   		
		return Features;	
	}
	
}


