package mfcc_processing;


import java.io.*;
import java.util.ArrayList;

public class WavWrite {

	private String path;
	
	public WavWrite(String path)
	{
		this.setPath(path);
		
		File file = new File(path);

		// if file exists, then delete it
		if (file.exists()) {
			file.delete();
		}		
	}

	public void storeFeatures(ArrayList <double[]> CepstrumList)
	{
		BufferedWriter bw = null;
		FileWriter fw = null;

		try {

			

			File file = new File(path);

			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			
			
			
			// true = append file
			fw = new FileWriter(file.getAbsoluteFile(), true);
			bw = new BufferedWriter(fw);

			for(double[] Cepstrum:CepstrumList)
			{
				String data = "";

				for(int i=0;i<Cepstrum.length;i++)
				{
					data += Cepstrum[i]+ " ";
					
				}
				
				bw.write(data);
				bw.write(System.getProperty( "line.separator" ));
				
			}
			

		} catch (IOException e) {

			e.printStackTrace();

		} finally {

			try {

				if (bw != null)
					bw.close();

				if (fw != null)
					fw.close();

			} catch (IOException ex) {

				ex.printStackTrace();

			}
		}
		
		
	}
	
	
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
	
	
}
