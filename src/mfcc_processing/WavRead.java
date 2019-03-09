package mfcc_processing;
import java.io.*;
import javax.sound.sampled.*;
import java.nio.ByteBuffer;

/**
 * Read the wav data from memory and normalize
 * @author yangyuchi
 *
 */
public class WavRead {
	private double Fs; //Sampling rate
	String path; //path of the wave file
	public WavRead(String path){
		this.path = path;
		this.Fs = 0.0;
	}
	
	//Read the wave file and convert to double
	public double[] getDoubleArray(){

		double[] audioDouble = null;
		float Samplerate = 0.0f;
		int lengthofAudio = 0;
		byte[] audioBytes = null;
		File fileIn = new File(path);
		try {			
			AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(fileIn);
			//resolution of wave file
			int bytesPerSignal = audioInputStream.getFormat().getFrameSize();
			Samplerate = audioInputStream.getFormat().getSampleRate();
			Fs = Samplerate;
			long l = audioInputStream.getFrameLength();
			lengthofAudio = (int)l;
			//create a buffer to save audio bytes
			audioBytes = new byte[lengthofAudio*bytesPerSignal];	
			audioDouble = new double[lengthofAudio];
			try {
				
				// Try to read numBytes bytes from the file.
				while ((audioInputStream.read(audioBytes)) != -1) {
			    
					//the audio signal are converted from Byte to Short at first, because Short data type
					//has 2 Bytes, which equals to the resolution of wav file (16bit signed)
					short[] audioShort = new short[lengthofAudio];
					
					//every time take 2 Bytes from audioBytes and convert to short
					for (int i=0; i<lengthofAudio;i++){
						byte[] b = new byte[2];
						b[1]=audioBytes[2*i];
						b[0]=audioBytes[2*i+1];
						audioShort[i] = ByteBuffer.wrap(b).getShort();
					}
					
					//Short type data are then converted to double and rescaled to [-1,1]				
					for (int j=0;j<lengthofAudio;j++){
						audioDouble[j]=(double)audioShort[j]/32678; 
					}
						
				}   
			} catch (Exception ex) { 
				// Handle the error...
			}			
		} catch (Exception e) {
		  // Handle the error...
			}
		
		return audioDouble;

	}
	
	/**
	 * 
	 * @return return the sampling rate of file
	 */
	public double getFs(){
		return Fs;
	}


}
