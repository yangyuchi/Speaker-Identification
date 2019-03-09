package neural_network;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The network consists of layers and neurons and can do learning via Back Propagation
 * Other functions such as label adding and speaker checking are also realized in the network
 * @author yangyuchi
 *
 */
public class Network{
	
	List<Double> ErrorTrain = new ArrayList<Double>(); //record the total error of all training patterns for each iteration 
	List<Double> ErrorValidate = new ArrayList<Double>(); //record the total error of all validation patterns for each iteration 
	List<Double> Accuracy = new ArrayList<Double>(); //record the total cost at the output layer for each iteration 
	private double LabelTraining[][]; //Label of Training materials
	private double ActualOutput[][];	//actual output calculated by network
	private double ActualOutputofValidation[][];	//actual validation output calculated by network
	private double LearningRate; 
	private double Momentum;
	private final int costfunction;
	private final int transferfunction;
	private double MinimumError; //target minimum Error, stop iteration when MinimumError is achieved
	private int NumberOfLayers; 
	private int	NumberOfTrainingSamples; //Total number of Samples(Training Material)
	private long MaxIterations; 
	private int SizeofFeature;
	private int SizeofOutput;
	public double identificationRate;
	public Layer[] Layers;
	
	
	/**
	 * Create a network with selected functions and parameters
	 * @param NumberOfNodes(Number of neurons in each layer in a array form)
	 * @param transfer_Function(choice of transfer function)
	 * @param cost_function(choice of cost/error function)
	 * @param LearnRate(learning rate)
	 * @param Moment(Momentum term for acceleration and stabilization of network);
	 * @param MinError(targeted minimum normalized error)
	 * @param MaxIter(Maximum iterations of learning)
	 */
	public Network(int NumberOfNodes[], int transfer_Function, int cost_function, double LearnRate, double Moment, double MinError, long MaxIter) {
			
		MinimumError = MinError;
		LearningRate = LearnRate;
		Momentum = Moment;
		NumberOfLayers = NumberOfNodes.length;
		MaxIterations = MaxIter;
		costfunction = cost_function;
		transferfunction = transfer_Function;
		SizeofFeature = NumberOfNodes[0];
		SizeofOutput = NumberOfNodes[NumberOfLayers-1];
		
		Layers = new Layer[NumberOfLayers];
		//for input layer, number of neurons equals to input dimensionality
		Layers[0] = new Layer(NumberOfNodes[0],NumberOfNodes[0]); 
		//for hidden layer and output layer, number of inputs equals to number of neurons in previous layer
		for (int i = 1; i < NumberOfLayers; i++) 
			Layers[i] = new Layer(NumberOfNodes[i],NumberOfNodes[i-1]);
				
}

	/**
	 * Training the network with Back propagation algorithm, 90% of all patterns used for training
	 * 10% used to test the identification rate of the network after training
	 * SGD is used for gradient descent,
	 * 4 kinds of transfer functions and 2 kinds of cost functions can be selected,
	 * after each iteration, the normalized error(cost) is saved, the training stops when minimum error
	 * is achieved or maximum iteration number is reached.
	 * Early stopping is applied to avoid overfitting
	 * @param allPatterns(all {feature,label} patterns get from feature extraction)
	 */
	public void TrainNetwork(ArrayList <double[]> allPatterns) {
		
		int k=0; //k indicates current iteration number
		
		//Stochastic Gradient Descend, first shuffle the training materials, after each pattern, 
		//update the weights and thresholds
		//Train the network until minimum Error is achieved or maximum iteration number is reached
		double error = 0.0;
		do{
			
			//Split all patterns into training pattern(90%) and test pattern(10%)
			Collections.shuffle(allPatterns);
			int TotalNumber = allPatterns.size();
			NumberOfTrainingSamples =  (int) (TotalNumber*0.9);
			List<double[]> TrainingPatterns = allPatterns.subList(0, NumberOfTrainingSamples);
			List<double[]> TestingPatterns = allPatterns.subList(NumberOfTrainingSamples, TotalNumber);
		
			//record the actual Output for each pattern
			ActualOutput = new double[NumberOfTrainingSamples][SizeofOutput];
			//extract feature and label from patterns
			double[][] TrainingMaterials = new double[NumberOfTrainingSamples][SizeofFeature];
			LabelTraining = new double[NumberOfTrainingSamples][SizeofOutput];
			ActualOutputofValidation = new double[TestingPatterns.size()][SizeofOutput];

			for (int i = 0; i < NumberOfTrainingSamples; i++){
				TrainingMaterials[i] = Arrays.copyOfRange(TrainingPatterns.get(i), 0, SizeofFeature);		
				LabelTraining[i] = Arrays.copyOfRange(TrainingPatterns.get(i), SizeofFeature, SizeofFeature+SizeofOutput);
			}
			
			
			for (int SampleNumber = 0; SampleNumber < NumberOfTrainingSamples; SampleNumber++){
           		Layers[0].Input = TrainingMaterials[SampleNumber];
           		FeedForward();			
				//Actual Output is the output of the Neurons in Output layer
           		ActualOutput[SampleNumber] = Layers[NumberOfLayers-1].OutputVector();
           		if (costfunction == 1){
           			ActualOutput[SampleNumber] = Softmax();
           		}
	      		CalculateSignalErrors(SampleNumber);
	    		BackPropagateError();    		
			}
			error = Calc_cost(TrainingMaterials);
			ErrorTrain.add(error);
			ValidateNetwork(TestingPatterns);
			//early stopping, if during 6 successive iterations the cost is not 
			//decreasing, stop training to avoid overfitting
			if ((k>5)&&(ErrorValidate.get(k)>ErrorValidate.get(k-1))&&(ErrorValidate.get(k-1)>ErrorValidate.get(k-2))
					&&(ErrorValidate.get(k-2)>ErrorValidate.get(k-3))&&(ErrorValidate.get(k-3)>ErrorValidate.get(k-4))
					&&(ErrorValidate.get(k-4)>ErrorValidate.get(k-5))&&(ErrorValidate.get(k-5)>ErrorValidate.get(k-6)))
				break;
			k++;
	
		} while ((ErrorValidate.get(k-1) > MinimumError) && (k < MaxIterations));
		
		

		System.out.println(Arrays.toString(ErrorTrain.toArray()));
		System.out.println(Arrays.toString(ErrorValidate.toArray()));
		System.out.println(Arrays.toString(Accuracy.toArray()));
		
	}

	/**
	 * Feed forward step of back propagation algorithm
	 * Take one feature as input, calculate the ultimate output of the network
	 */
	//Calculate the output from input layer till output layer
	private void FeedForward(){
		//for input Layers, no need to do calculation
        for (int i = 0; i < Layers[0].Neurons.length; i++){
			Layers[0].Neurons[i].Output = Layers[0].Input[i];
        }
        //for hidden layer and output layer, the input is the output of previous layer
		Layers[1].Input = Layers[0].OutputVector();
		
		for (int i = 1; i < NumberOfLayers; i++) {
			Layers[i].feedforward(transferfunction);
			//do Feedforward calculation until reach the output layer
			if (i < NumberOfLayers-1)
				Layers[i+1].Input = Layers[i].OutputVector();
		}

	}
	
	/**
	 * Calculate the signal errors which are used to update weights
	 * the formulas are derived by the chain rule, different 
	 * @param SampleNumber(indicates the id of current feature)
	 */
	private void CalculateSignalErrors(int SampleNumber) {

		int i,n,j,OutputLayer;
		double Sum;

		OutputLayer = NumberOfLayers-1;
		//First calculate error in output layer, use formula delta=(T-y)*f'(u)
		//for Sigmoid function, f'(x)=f(x)*(1-f(x))
		//for Tanh function, f'(x)=4/(exp(x)+exp(-x))^2
		
		if (transferfunction == 0){
			//Calculate error in output layer
			for (i = 0; i < Layers[OutputLayer].Neurons.length; i++){
				double y = ActualOutput[SampleNumber][i];
				if (costfunction == 0){
					Layers[OutputLayer].Neurons[i].Error 
						= (LabelTraining[SampleNumber][i] - y) * y * (1-y);
				}
				if (costfunction == 1){
					Layers[OutputLayer].Neurons[i].Error 
						= (y - LabelTraining[SampleNumber][i]);			
				}
			}
			//Calculate error in hidden layers
			for (n = NumberOfLayers-2; n > 0; n--) {
				for (i = 0; i < Layers[n].Neurons.length; i++) {
					
					Sum = 0;
					for (j = 0; j < Layers[n+1].Neurons.length; j++){
						Sum = Sum + Layers[n+1].Neurons[j].Weight[i] * Layers[n+1].Neurons[j].Error;
					}
					Layers[n].Neurons[i].Error 
						= Layers[n].Neurons[i].Output * (1 - Layers[n].Neurons[i].Output) * Sum;
				}
			}
		}
		
		if (transferfunction == 1){
			//Calculate error in output layer
			for (i = 0; i < Layers[OutputLayer].Neurons.length; i++){
				double u = Layers[OutputLayer].Neurons[i].Sum;
				Layers[OutputLayer].Neurons[i].Error 
					= (LabelTraining[SampleNumber][i] - Layers[OutputLayer].Neurons[i].Output)
					/Math.pow((Math.cosh(u)),2);
			}
			//Calculate error in hidden layers
			for (n = NumberOfLayers-2; n > 0; n--) {
				for (i = 0; i < Layers[n].Neurons.length; i++) {
					
					Sum = 0;
					for (j = 0; j < Layers[n+1].Neurons.length; j++){
						Sum = Sum + Layers[n+1].Neurons[j].Weight[i] * Layers[n+1].Neurons[j].Error;
					}
					Layers[n].Neurons[i].Error 
						= 1/Math.pow((Math.cosh(Layers[n].Neurons[i].Sum)),2) * Sum;
				}
			}
		}
		
		if (transferfunction == 2){
			//Calculate error in output layer
			for (i = 0; i < Layers[OutputLayer].Neurons.length; i++){
				double u = Layers[OutputLayer].Neurons[i].Sum;
				double grad = (u>0)?1:0;
				Layers[OutputLayer].Neurons[i].Error 
					= (LabelTraining[SampleNumber][i] - Layers[OutputLayer].Neurons[i].Output) * grad;
			}
			//Calculate error in hidden layers
			for (n = NumberOfLayers-2; n > 0; n--) {
				for (i = 0; i < Layers[n].Neurons.length; i++) {
					
					Sum = 0;
					for (j = 0; j < Layers[n+1].Neurons.length; j++){
						Sum = Sum + Layers[n+1].Neurons[j].Weight[i] * Layers[n+1].Neurons[j].Error;
					}
					double grad = ((Math.cosh(Layers[n].Neurons[i].Sum))>0)?1:0;
					Layers[n].Neurons[i].Error 
						= grad * Sum;
				}
			}
		}
		
		if (transferfunction == 3){
			//Calculate error in output layer
			for (i = 0; i < Layers[OutputLayer].Neurons.length; i++){
				double u = Layers[OutputLayer].Neurons[i].Sum;
				Layers[OutputLayer].Neurons[i].Error 
					= (LabelTraining[SampleNumber][i] - Layers[OutputLayer].Neurons[i].Output)
					/(1+Math.exp(-u));
			}
			//Calculate error in hidden layers
			for (n = NumberOfLayers-2; n > 0; n--) {
				for (i = 0; i < Layers[n].Neurons.length; i++) {
					
					Sum = 0;
					for (j = 0; j < Layers[n+1].Neurons.length; j++){
						Sum = Sum + Layers[n+1].Neurons[j].Weight[i] * Layers[n+1].Neurons[j].Error;
					}
					double u = Layers[n].Neurons[i].Sum;
					Layers[n].Neurons[i].Error 
						= Sum/(1+Math.exp(-u));
				}
			}
		}
	}

	/**
	 * Update the weights with signal error, a momentum term is used to accelerate and stabilize 
	 * learning
	 */
	private void BackPropagateError() {

		int i,j,k;

		for (i = NumberOfLayers-1; i > 0; i--) {
			for (j = 0; j < Layers[i].Neurons.length; j++) {
				if (costfunction == 0){
					Layers[i].Neurons[j].Delta_Threshold 
						= LearningRate * 
						Layers[i].Neurons[j].Error + 
						Momentum*Layers[i].Neurons[j].Delta_Threshold;
				}
				if (costfunction == 1){
					Layers[i].Neurons[j].Delta_Threshold 
						= (-LearningRate) * 
						Layers[i].Neurons[j].Error + 
						Momentum*Layers[i].Neurons[j].Delta_Threshold;
				}

				Layers[i].Neurons[j].Threshold = 
					Layers[i].Neurons[j].Threshold - 
					Layers[i].Neurons[j].Delta_Threshold;

				for (k = 0; k < Layers[i].Input.length; k++) {
					if (costfunction == 0){
						Layers[i].Neurons[j].Delta_Weight[k] = 
						LearningRate * 
						Layers[i].Neurons[j].Error * Layers[i-1].Neurons[k].Output +
						Momentum*Layers[i].Neurons[j].Delta_Weight[k];
					}
					if (costfunction == 1){
						Layers[i].Neurons[j].Delta_Weight[k] = 
						(-LearningRate) * 
						Layers[i].Neurons[j].Error * Layers[i-1].Neurons[k].Output +
						Momentum*Layers[i].Neurons[j].Delta_Weight[k];
					}
					Layers[i].Neurons[j].Weight[k] = 
						Layers[i].Neurons[j].Weight[k] + 
						Layers[i].Neurons[j].Delta_Weight[k];
				}
			}
		}
	}

	/**
	 * After each iteration, calculate the total cost of all training patterns
	 * @return error of current iteration
	 */
	private double Calc_cost(double [][]TrainingMaterials)
	{

		double cost = 0.0;
       	
		for (int i = 0; i < NumberOfTrainingSamples; i++){
           	Layers[0].Input = TrainingMaterials[i];
			FeedForward();   		
      		//if the cost function is log-likelihood, need to apply a softmax layer to get probability
       		if (costfunction == 1){
       			ActualOutput[i] = Softmax();
       		}else{
       			ActualOutput[i] = Layers[NumberOfLayers-1].OutputVector();
       		}
			for (int k = 0; k < Layers[NumberOfLayers-1].Neurons.length; k++) {
				if (costfunction == 0){
					cost += 0.5*( Math.pow(LabelTraining[i][k] - ActualOutput[i][k],2) );
				}
				if (costfunction == 1){
					cost += (-(LabelTraining[i][k] * Math.log(ActualOutput[i][k])));
				}
			}
		}
		return (cost/NumberOfTrainingSamples); //normalize the error
	}
	
	/**
	 * Test the identification performance with testing patterns,
	 * check the identified speaker and compare with the label,
	 * a identification rate is calculated by dividing Number of correct identified patterns
	 * with number of total test patterns
	 * @param TestingPatterns
	 */
	private void ValidateNetwork(List <double[]> TestingPatterns)
	{
		double cost = 0.0;
		int numberOfTest = TestingPatterns.size();
		//extract testing inputs and labels from testing patterns
		double[][] Testinginputs = new double[numberOfTest][SizeofFeature];
		double[][] Lables = new double[numberOfTest][SizeofOutput];
		for (int i = 0; i < numberOfTest; i++){
			Testinginputs[i] = Arrays.copyOfRange(TestingPatterns.get(i), 0, SizeofFeature);		
			Lables[i] = Arrays.copyOfRange(TestingPatterns.get(i), SizeofFeature, SizeofFeature+SizeofOutput);
		}

		int count = 0; //count the correct test result
		for (int i = 0; i < numberOfTest; i++) {
       		Layers[0].Input = Testinginputs[i];           		
			FeedForward();
      		if (costfunction == 1){
      			ActualOutputofValidation[i] = Softmax();
       		}else{
       			ActualOutputofValidation[i] = Layers[NumberOfLayers-1].OutputVector();
      		}
			for (int k = 0; k < Layers[NumberOfLayers-1].Neurons.length; k++) {
				if (costfunction == 0){
					cost += 0.5*( Math.pow(Lables[i][k] - ActualOutputofValidation[i][k],2) );
				}
				if (costfunction == 1){
					cost += (-(Lables[i][k] * Math.log(ActualOutputofValidation[i][k])));
				}
			}

			double[] output = Softmax(); //map output to probability
			double max = output[0];
			int indicator=0;
			for (int n=1;n<output.length;n++){
				if (output[n]>max){
					indicator=n;
					max=output[n];
				}
			}
			
			if ((Lables[i][indicator]==1) && (max>0.5))
				count++;
		}
		
		identificationRate = (double)count/numberOfTest * 100;
		double error = cost/numberOfTest;
		ErrorValidate.add(error);
		Accuracy.add(identificationRate);
		
	}

	/**
	 * Check the speaker ID of a new file
	 * @param testFeature(Features for testing(without label))
	 * @return ID of speaker
	 */
	public int checkSpeaker(ArrayList <double[]> testFeature)
	{

		ArrayList <double[]> TestOutput = new ArrayList<double[]>();

		for (int n = 0; n < testFeature.size(); n++) {
       		for (int i = 0; i < SizeofFeature; i++){
       			Layers[0].Input[i] = testFeature.get(n)[i];
       		}       		
			FeedForward();
			double[] output = Softmax();  
			TestOutput.add(output);
		}
		
		int[] testResult = new int[TestOutput.size()];
		int index = 0;

		for(double[] arr: TestOutput)
		{
			double max = arr[0];
			int indicator=0;
			for (int i=1;i<arr.length;i++){
				if (arr[i]>max){
					indicator=i;
					max=arr[i];
				}
			}
			if (max>0.5)
				testResult[index]=indicator+1;
			else 
				testResult[index]=0;
			index++;
		}
		
		int[] accuracy = new int[SizeofOutput+1];
		for (int i=0;i<testResult.length;i++){
				accuracy[testResult[i]]++;
		}
		
		int maxValue = accuracy[0];
		int index_max = 0;
		for (int i = 1;i<accuracy.length;i++){
			if (accuracy[i]>=maxValue){
				maxValue = accuracy[i];
				index_max = i;
			}
		}
		System.out.println(Arrays.toString(accuracy));
		
		return index_max;
	}

	/**
	 * Softmax function, used for multi-classification task
	 * @return mapped value(probabilities)
	 */
	private double[] Softmax() {
		double[] z = new double[SizeofOutput];
		double sum = 0.0;
		for (int i=0;i<SizeofOutput;i++){
			z[i]=Layers[NumberOfLayers-1].Neurons[i].Sum;
			sum += Math.exp(z[i]);
		}
		double[] output = new double[SizeofOutput];
		for (int i=0;i<SizeofOutput;i++){
			output[i]=Math.exp(z[i])/sum;
		}
		return output;
	}
		
}
