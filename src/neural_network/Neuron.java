package neural_network;

import java.util.Random;

/**
 * A neuron is the basic component of a network. Each neuron has weights for the connections between
 * it and the neurons in the previous layer. The Calculated results such as weighted sum, output, 
 * Error(for backpropagation) and Delta information(momentum term) are all saved in the neurons.
 * @author yangyuchi
 *
 */
public class Neuron {
	public double Sum;
	public double Output;		
	public double Weight[];		
	public double Threshold;	
	public double Delta_Weight[];	
	public double Delta_Threshold;
	public double Error;
	Random rd = new Random();
	
	/**
	 * number of weights depends on the number of inputs from previous layer
	 * @param NumberOfInputs(number of Neurons in previous layer)
	 */
	public Neuron (int NumberOfInputs) {
		Weight = new double[NumberOfInputs];		
		Delta_Weight = new double[NumberOfInputs];	
		//Initialize Weights automatically when creating new Neurons
		InitialiseWeights();				
	}

	/**
	 * All weights are initialized randomly by Xavier Normalization method.
	 * each weight initialized by a Gaussian distribution
	 */
	private void InitialiseWeights() {
		
		Threshold = rd.nextGaussian()/Math.sqrt(Weight.length);	    

		//momentum term initialized to 0, weights initialized to small random values
		Delta_Threshold = 0;				
		
     	for(int i = 0; i < Weight.length; i++) {
			
     		Weight[i]= rd.nextGaussian()/Math.sqrt(Weight.length);	
			Delta_Weight[i] = 0;			

		}
	}
	
}

