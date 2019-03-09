package neural_network;
/**
 * The Layer is a combination of multiple neurons, it takes the output vector of the previous layer 
 * as the input, calculate the weighted sum, then generate the output by selected transfer functions.
 * @author yangyuchi
 */

public class Layer {
	public double Input[];		
	public	Neuron[] Neurons;		

	public Layer (int NumberOfNeurons, int NumberOfInputs) {
		Neurons = new Neuron[NumberOfNeurons];

		for (int i = 0; i < NumberOfNeurons; i++){
			Neurons[i] = new Neuron(NumberOfInputs);
		}
		Input = new double[NumberOfInputs];
	}
	
	/**
	 * Calculate the weighted sum and output of each neuron and save in each neuron
	 * @param indicator(indicates the choice of transfer function)
	 */
	public void feedforward(int indicator) {
		for (int i = 0; i < Neurons.length; i++) {
			
			double sum = -(Neurons[i].Threshold);
			//calculate the aggregate input of the neuron
			for (int j = 0; j < Neurons[i].Weight.length; j++){
				sum = sum + Input[j] * Neurons[i].Weight[j];
			}
			Neurons[i].Sum = sum;
			//use Sigmoid function as transfer function
			if (indicator == 0)				
				Neurons[i].Output = Sigmoid(sum);
			//use tangent hyperbolic as transfer function
			if (indicator == 1)
				Neurons[i].Output = Tanh(sum);
			//use ReLU as transfer function
			if (indicator == 2)
				Neurons[i].Output = ReLU(sum);
			//use SoftPlus as transfer function
			if (indicator == 3)
				Neurons[i].Output = SoftPlus(sum);
				
		}
	}

	/**
	 * Softplus transfer function
	 * @param x
	 * @return log(1+exp(x))
	 */
	private double SoftPlus(double x) {
		return Math.log(1+Math.exp(x));
	}
	
	/**
	 * Rectified linear unit transfer function
	 * @param x
	 * @return ReLU(x)
	 */
	private double ReLU(double x) {
		return Math.max(x, 0);
	}

	/**
	 * Sigmoid transfer function
	 * @param x
	 * @return 1/(1+Math.exp(-x))
	 */
	private double Sigmoid (double x) {
		return 1/(1+Math.exp(-x));
	}

	/**
	 * Tangent hyperbolic transfer function
	 * @param x
	 * @return (exp(x)-exp(-x))/(exp(x)+exp(-x))
	 */
	private double Tanh(double x){
		return Math.tanh(x);
	}
	
	/**
	 * Put outputs of all Neurons into one Vector
	 * the output vector will be used as input vector for next layer
	 * @return the output of all neurons in this layer in vector form
	 */
	public double[] OutputVector() {
		double Vector[];
		Vector = new double[Neurons.length];
		for (int i=0; i < Neurons.length; i++){
			Vector[i] = Neurons[i].Output;
		}
		return (Vector);
	}

};

