package mfcc_processing;

/**
 * 
 *  Compute the Mel-spaced filterbank. 
 *	This is a set of 20-40 (26 is standard) triangular filters that we apply to the periodogram 
 *	power spectral estimate.
 *	Our filterbank comes in the form of 26 vectors of length 257 (assuming we perform a 512 point FFT).
 *	Each vector is mostly zeros, but is non-zero for a certain section of the spectrum. 
 *	To calculate filterbank energies we multiply each filterbank with the power spectrum, 
 *	then add up the coefficents.
 *  
 * @author yangyuchi
 *
 */


public class MelFilterBank 
{ 
	 //maximum Frequency depends on the sampling rate of wave file 
	 private final double minFrequency = 0.0;
	 private final double maxFrequency;
	 private final double minMel, maxMel, SampleFrequency;  
	 private final int numFilters, numFFT; 
	 public double[][] filters;
	 
	 /**
	  * Constructor for filter banks
	  * @param numfft(number of FFT calculation, normally 512)
	  * @param Fs(Sampling rate)
	  * @param num(number of filter banks, standard 26)
	  */
	 public MelFilterBank(int numfft, double Fs, int num) 
	 { 
		//according to Nyquist sampling theorem, sampling rate must be at least twice of the frequency 
		 maxFrequency = Fs/2; 
		 minMel = freqToMel(minFrequency); 
		 maxMel = freqToMel(maxFrequency);
		 SampleFrequency = Fs;
		 numFilters = num;
		 numFFT = numfft;
		 filters = get_Filters();
	 } 
	  
	 /**
	  * generate filter banks
	  * @return return filter banks
	  */
	 public double[][] get_Filters() 
	 { 
		 double[] f= new double[numFilters+2];
		 int length = numFFT/2+1;
		 
		 //the boundary points f(m) are uniformly distributed in the Mel scale
		 for (int m=0;m<=numFilters+1;m++)
		 {
			  f[m]=numFFT/SampleFrequency*melToFreq(minMel + m*(maxMel - minMel) / (numFilters + 1));
		 }
		 
		 //use a 2-D array to represent filter banks, where each row is one triangle filter
		 double [][] filters = new double [numFilters][length];
		 
		 //calculate according to the definition formula
		 for (int M=1;M<=numFilters;M++){
			 
			 for (int k=0;k<(length);k++){
				 if ((k<f[M-1])||(k>f[M+1]))
					 filters[M-1][k] = 0;
				 else if((k>=f[M-1])&&(k<=f[M]))
					 filters[M-1][k] = 2*(k-f[M-1])/((f[M+1]-f[M-1])*(f[M]-f[M-1]));
				 else if((k>=f[M])&&(k<=f[M+1]))
					 filters[M-1][k] = 2*(f[M+1]-k)/((f[M+1]-f[M-1])*(f[M+1]-f[M]));
			 }
			 //each filter must to normalized to [0,1]
			 double max = MFCC.getMax(filters[M-1]);
			 for (int k=0;k<(length);k++){
				 filters[M-1][k] = filters[M-1][k]/max;			 
			 }
		 }
		 
		 return filters;
	 } 
  
	 /**
	  * convert frequency to Mel scale
	  * @param freq(frequency in Hz)
	  * @return frequency in Mel scale
	  */
	 public static double freqToMel(double freq) 
	 { 
		 return (double) (1125 * Math.log(1 + freq / 700)); 
	 } 
	  
	 /**
	  * convert Mel scale frequency to frequency in Hz
	  * @param mel(Mel scaled frequency)
	  * @return frequency in Hz
	  */
	 public static double melToFreq(double mel) 
	 { 
		 return (double) (700 * (Math.exp(mel / 1125) - 1)); 
	 } 
  
}
