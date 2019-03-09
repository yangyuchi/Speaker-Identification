package mfcc_processing;

import java.util.ArrayList;

/**
 * Incremental K-Means clustering to reduce size of training data
 * Need to be improved: use HashMap (O(n)) other than iterations to generate random
 * indexes, otherwise if number of clusters is large, the initialization can
 * be extremely inefficient O(n^2)
 */
public class KMeans {
	//prototypes after clustering(cluster centers)
	private ArrayList<double[]> prototypes;
	//raw feature list
	private ArrayList<double[]> CepstrumList;
	//number of clusters
	private int numberOfCluster;
	//maximum iteration
	private int iter;
	//minimum cluster change(proportion) for one iteration over all data
	private double epsilon;
	
	public KMeans(ArrayList<double[]> CepstrumList, int numberOfCluster, int maxIter, double delta)
	{
		this.CepstrumList = CepstrumList;
		this.numberOfCluster = numberOfCluster;
		this.iter = maxIter;
		this.epsilon = delta;
		prototypes = new ArrayList<double[]>();
		
		initPrototypes();
		doInkrementalKMeans();
	}
		
	/**
	 * Incremental KMeans algorithm, stop if maximum iteration or minimum 
	 * cluster change(0.1) reached
	 */
	private void doInkrementalKMeans()
	{
		double delta_C = 0.0;
		int winner_index=0;		
		int[] n = new int[numberOfCluster];
		int count = 0;
		double deltaC_old = 0.0;
		double improve = 0.0;
		do{
			delta_C = 0.0;
			for(double[] Cepstrum: CepstrumList){			
				winner_index = detect_winner(Cepstrum);
				n[winner_index]++; //increment the number of data in corresponding cluster
	            delta_C += update_winner(Cepstrum, winner_index, n[winner_index]);	
			}
			improve = Math.abs(delta_C-deltaC_old);
			deltaC_old = delta_C;
		}while ((++count<iter) && ((improve/delta_C)>epsilon));
		
	}
	
	/**
	 * for a given feature, find the nearest prototype
	 * @param Cepstrum(selected feature)
	 * @return index of the winner prototype
	 */
	private int detect_winner(double[] Cepstrum)
	{
		
		int index = 0;		
		double new_difference = 0.0;
		//initialize the smallest distance to the first distance
		double smallest_difference = calcDistance(Cepstrum, prototypes.get(0));	
		int index_for_smallest_difference = 0;
		//compare the distances from input data to all prototypes, find the index of the nearest prototype
		for(double[] prototype: prototypes)	{	
				new_difference = calcDistance(Cepstrum, prototype);
				if(new_difference < smallest_difference){				
					smallest_difference = new_difference;
					index_for_smallest_difference = index;
				}
				index++;
		}		
		return index_for_smallest_difference;
	}
		
	/**
	 * update the winner prototype, return the distance change
	 * @param Cepstrum(feature)
	 * @param winner_index(index of winner prototype)
	 * @param clusterSize(size of corresponding cluster)
	 * @return
	 */
	private double update_winner(double[] Cepstrum, int winner_index, int clusterSize)
	{
		
		double[] updated_array = new double[Cepstrum.length];
		double[] Winnerprototyp =  prototypes.get(winner_index);
		double delta_c = 0.0;

		for(int i=0; i<updated_array.length;i++){
			//update rule for incremental K-Means
			updated_array[i] = Winnerprototyp[i] + (Cepstrum[i]-Winnerprototyp[i])/clusterSize;
			//save the absolute value of update
			delta_c += Math.abs(Cepstrum[i]-Winnerprototyp[i])/clusterSize;
		}
		//substitute old prototype with updated one
		prototypes.set(winner_index, updated_array);
		//return the distance of change
		return Math.sqrt(delta_c);
		
	}
	
		
	/**
	 * initialize prototypes with random feature points
	 */
	private void initPrototypes()
	{	
		int[] array = getIndex(0, CepstrumList.size()-1, numberOfCluster);	
		
		for(int i=0; i<numberOfCluster; i++){
			double[] element = CepstrumList.get(array[i]);
			prototypes.add(i, element);		
		}	
	}
	
	/**
	 * Generate random indexs between 0 and number of Clusters to initialize prototypes
	 * @param lower range
	 * @param higher range
	 * @param number of int data need to be generated
	 * @return a random non-replicate array ranging from 0 to nClusters
	 */
	public static int[] getIndex(int min, int max, int nClusters){  
	    //check if within range
		if (nClusters > (max - min + 1) || max < min) {  
	           return null;  
	       }  
	    int[] result = new int[nClusters];  
	    int count = 0;  
	    while(count < nClusters) {  
	        int num = (int) (Math.random() * (max - min)) + min;  
	        boolean flag = true;  
	        for (int j = 0; j < count; j++) {  
	            if(num == result[j]){  
	                flag = false;  
	                break;  
	            }  
	        }  
	        if(flag){  
	            result[count] = num;  
	            count++;  
	        }  
	    }  
	    return result;  
	}
	
	/**
	 * Calculate the Euclidean distance between two data points
	 * @param x(feature data)
	 * @param c(prototype)
	 * @return distance between feature and its prototype
	 */
	private double calcDistance(double[] x, double[] c)
	{
		
		double sum = 0.0;
		
		for(int i=0; i<x.length; i++){
			sum += Math.pow(x[i]-c[i], 2);	
		}
		
		return Math.sqrt(sum);
		
	}
	
	/**
	 * 
	 * @return return the prototypes
	 */
	public ArrayList<double[]> getPrototypes()
	{		
		return prototypes;
	}
	
	/**
	 * return the (MFCC)cepstrum lists
	 * @return
	 */
	public ArrayList<double[]> getCepstrumList()
	{	
		return this.CepstrumList;
	}
	
//	//convert arrayList<double[]> to double[][]
//	public double[][] getPrototypes_as_DoubleArray()
//	{
//		return prototypes.toArray(new double[0][]);	
//	}
//	
//	
}
