// ===========================================
// I will be using the code I've written for the other project where the neural network guesses the handwritten numbers.
// Only this time I will only have a forward propagation and no backpropagation, since the neural network parameters
// will be updated with Evolutionary Algorithm.
// My neural network Java code: https://github.com/RusFortunat/java_ML_library
// =============================================
package root.intelligentasteroidsshooter.trainAI;

import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import root.intelligentasteroidsshooter.model.Hitbox;
import root.intelligentasteroidsshooter.singlePlayer.SinglePlayerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class NeuralNetwork implements Comparable<NeuralNetwork> {
    private double mutationRate;
    private int inputSize;
    private int hiddenSize;
    private int outputSize;
    private double[] hiddenVector;
    private double[] outputVector;
    // Neural Network parameters
    private double[][] firstLayerWeights;
    private double[] firstLayerBiases;
    private double[][] secondLayerWeights;
    private double[] secondLayerBiases;

    private Hitbox ship; // every respectable neural network should have its own spaceship
    private double shipSize;
    private int score; // each network's performance will be evaluated through this score meter
    private int scoreForPrinting; // scores are being zeroed for every evolutional iteration, but this one is stored
    private double averagePopulationScore; // network is a part of population and this is a measure of population fitness

    // constructor; initialize a fully-connected neural network with random weights and biases
    public NeuralNetwork(double mutationRate, int inputSize, int hiddenSize, int outputSize){
        this.mutationRate = mutationRate;
        this.inputSize = inputSize;
        this.hiddenSize = hiddenSize;
        this.outputSize = outputSize;
        this.hiddenVector = new double[hiddenSize];
        this.outputVector = new double[outputSize];
        this.firstLayerWeights = new double[hiddenSize][inputSize];
        this.firstLayerBiases = new double[hiddenSize];
        this.secondLayerWeights = new double[outputSize][hiddenSize];
        this.secondLayerBiases = new double[outputSize];

        // it is a good practice to limit distribution to inverse vector size
        double rangeW1 = 1.0;
        for(int i = 0; i < hiddenSize; i++){
            firstLayerBiases[i] = ThreadLocalRandom.current().nextDouble(-rangeW1,rangeW1);
            for(int j = 0; j < inputSize; j++) {
                firstLayerWeights[i][j] = ThreadLocalRandom.current().nextDouble(-rangeW1,rangeW1);
            }
        }
        double rangeW2 = 1.0;
        for(int i = 0; i < outputSize; i++){
            secondLayerBiases[i] = ThreadLocalRandom.current().nextDouble(-rangeW2,rangeW2);
            for(int j = 0; j < hiddenSize; j++) {
                secondLayerWeights[i][j] = ThreadLocalRandom.current().nextDouble(-rangeW2,rangeW2);
            }
        }

        shipSize = 40;
        Polygon squarePolygon = new Polygon(-shipSize/2, -shipSize/2,
                shipSize/2, -shipSize/2, shipSize/2, shipSize/2, -shipSize/2, shipSize/2); // ship size
        this.ship = new Hitbox(squarePolygon, Color.BLACK,
                SinglePlayerView.WIDTH/2,
                SinglePlayerView.HEIGHT/2); // all ships will be initialized exactly at the middle of the screen
    }

    // overloaded constructor for creating empty neural network (all params are zero)
    public NeuralNetwork(int inputSize, int hiddenSize, int outputSize){
        this.inputSize = inputSize;
        this.hiddenSize = hiddenSize;
        this.outputSize = outputSize;
        this.hiddenVector = new double[hiddenSize];
        this.outputVector = new double[outputSize];
        this.firstLayerWeights = new double[hiddenSize][inputSize];
        this.firstLayerBiases = new double[hiddenSize];
        this.secondLayerWeights = new double[outputSize][hiddenSize];
        this.secondLayerBiases = new double[outputSize];
    }

    // create a new network from two parents via crossover
    public NeuralNetwork(NeuralNetwork parent1, NeuralNetwork parent2){
        this.inputSize = parent1.getInputSize();
        this.hiddenSize = parent1.getHiddenSize();
        this.outputSize = parent1.getOutputSize();
        this.hiddenVector = new double[hiddenSize];
        this.outputVector = new double[outputSize];
        this.firstLayerWeights = new double[hiddenSize][inputSize];
        this.firstLayerBiases = new double[hiddenSize];
        this.secondLayerWeights = new double[outputSize][hiddenSize];
        this.secondLayerBiases = new double[outputSize];
        double[] parent1Biases1 = parent1.getFirstLayerBiases();
        double[][] parent1Weights1 = parent1.getFirstLayerWeights();
        double[] parent1Biases2 = parent1.getSecondLayerBiases();
        double[][] parent1Weights2 = parent1.getSecondLayerWeights();
        double[] parent2Biases1 = parent2.getFirstLayerBiases();
        double[][] parent2Weights1 = parent2.getFirstLayerWeights();
        double[] parent2Biases2 = parent2.getSecondLayerBiases();
        double[][] parent2Weights2 = parent2.getSecondLayerWeights();

        // create a new network with completely shuffled parameters from two parents
        for(int i = 0; i < hiddenSize; i++){
            int dice = ThreadLocalRandom.current().nextInt(0,2);
            if(dice == 0) {
                firstLayerBiases[i] = parent1Biases1[i];
            }else{
                firstLayerBiases[i] = parent2Biases1[i];
            }
            for(int j = 0; j < inputSize; j++) {
                dice = ThreadLocalRandom.current().nextInt(0,2);
                if(dice == 0){
                    firstLayerWeights[i][j] = parent1Weights1[i][j];
                }else{
                    firstLayerWeights[i][j] = parent2Weights1[i][j];
                }
            }
        }
        for(int i = 0; i < outputSize; i++){
            int dice = ThreadLocalRandom.current().nextInt(0,2);
            if(dice == 0) {
                secondLayerBiases[i] = parent1Biases2[i];
            }else{
                secondLayerBiases[i] = parent2Biases2[i];
            }
            for(int j = 0; j < hiddenSize; j++) {
                dice = ThreadLocalRandom.current().nextInt(0,2);
                if(dice == 0){
                    secondLayerWeights[i][j] = parent1Weights2[i][j];
                }else{
                    secondLayerWeights[i][j] = parent2Weights2[i][j];
                }
            }
        }
    }

    // forward pass -- we take in (agent surroundings, its orientation, speed) and ask for action (move/shoot)
    public int forward(double[] input){
        // forward propagation is simple; just do the following:
        // 1. Compute [z] = [firstLayerWeights][input] + [firstLayerBiases];
        // 2. Obtain the activation values of the hidden vector [y] by applying to [z] some activation function f([z]).
        //    Here we use ReLU activation: f(z) > 0 ? z : 0;
        // 3. Repeat for the next layer with secondLayerWeights and secondLayerBiases to get the [output] vector.

        // compute hidden activation values
        for(int i = 0; i < hiddenSize; i++){
            double sum = 0;
            for(int j = 0; j < inputSize; j++){
                double activation = firstLayerWeights[i][j]*input[j] + firstLayerBiases[i];
                if(activation > 0) sum+= activation; // ReLU activation
            }
            hiddenVector[i] = sum;
        }
        // compute output activations
        double totalSum = 0.0;
        for(int i = 0; i < outputSize; i++){
            double sum = 0;
            for(int j = 0; j < hiddenSize; j++){
                double activation = secondLayerWeights[i][j]*hiddenVector[j] + secondLayerBiases[i];
                if(activation > 0) sum+= activation; // ReLU activation
            }
            outputVector[i] = sum;
            totalSum += Math.exp(sum);
        }

        // SoftMax -- creates probability distribution over actions; totalSum should normalize distribution, making it to sum up to 1.0
        for(int i = 0; i < outputSize; i++){
            outputVector[i] = Math.exp(outputVector[i]) / totalSum;
        }

        // randomly sample the action from probability distribution and return it
        Random rnd = new Random();
        double dice = rnd.nextDouble();
        int action = -1;
        double sum = 0;
        for(int i = 0; i < outputSize; i++){
            sum += outputVector[i];
            if(sum >= dice){
                action = i;
                break;
            }
        }

        return action;
    }

    // mutate neural network parameters -- we will use Gaussian distribution with mutation rate as a standard deviation
    public void mutate(){
        Random rng = new Random();
        for(int i = 0; i < hiddenSize; i++){
            double mutateBias = mutationRate*rng.nextGaussian(); // nextGaussian() with mean 0 and standard deviation 1
            firstLayerBiases[i] += mutateBias;
            for(int j = 0; j < inputSize; j++) {
                double mutateWeight =  mutationRate*rng.nextGaussian();
                firstLayerWeights[i][j] += mutateWeight;
            }
        }
        for(int i = 0; i < outputSize; i++){
            double mutateBias = mutationRate*rng.nextGaussian();
            secondLayerBiases[i] += mutateBias;
            for(int j = 0; j < hiddenSize; j++) {
                double mutateWeight =  mutationRate*rng.nextGaussian();
                secondLayerWeights[i][j] += mutateWeight;
            }
        }
    }

    // save params
    public List<String> NNParametersToList(){
        List<String> NNparameters = new ArrayList<>();

        String firstLayerWeightsStr = "";
        for(int i = 0; i < hiddenSize; i++){
            for(int j = 0; j < inputSize; j++) {
                firstLayerWeightsStr += firstLayerWeights[i][j] + ",";
            }
        }

        String firstLayerBiasesStr = "";
        for(int i = 0; i < hiddenSize; i++){
            firstLayerBiasesStr += firstLayerBiases[i] + ",";
        }

        String secondLayerWeightsStr = "";
        for(int i = 0; i < outputSize; i++){
            for(int j = 0; j < hiddenSize; j++) {
                secondLayerWeightsStr += secondLayerWeights[i][j] + ",";
            }
        }

        String secondLayerBiasesStr = "";
        for(int i = 0; i < outputSize; i++){
            secondLayerBiasesStr += secondLayerBiases[i] + ",";
        }

        NNparameters.add(firstLayerWeightsStr);
        NNparameters.add(firstLayerBiasesStr);
        NNparameters.add(secondLayerWeightsStr);
        NNparameters.add(secondLayerBiasesStr);

        return NNparameters;
    }

    public void addPoints(int points) {this.score += points;}

    @Override
    public int compareTo(NeuralNetwork otherNetwork){
        return otherNetwork.getScore() - this.score; // order from big to small
    }

    // setters
    public void setScore(int value) { score = value;}
    public void setScoreForPrinting(int value) { scoreForPrinting = value;}
    public void setAveragePopulationScore(double value) { averagePopulationScore = value;}

    // getters
    public int getScore() { return score; }
    public int getScoreForPrinting() {return scoreForPrinting; }
    public int getInputSize(){ return inputSize; }
    public int getHiddenSize(){ return hiddenSize; }
    public int getOutputSize(){ return outputSize; }
    public double[] getFirstLayerBiases(){  return firstLayerBiases;}
    public double[] getSecondLayerBiases(){  return secondLayerBiases;}
    public double[][] getFirstLayerWeights(){ return firstLayerWeights; }
    public double[][] getSecondLayerWeights(){ return secondLayerWeights; }

    // printers, for debug purposes
    public void printNetworkParameteres(){
        System.out.println("firstLayerWeights:");
        for(int i = 0; i < hiddenSize; i++){
            for(int j = 0; j < inputSize; j++) {
                System.out.print(firstLayerWeights[i][j] + " ");
            }
            System.out.println("");
        }
        System.out.println("\nfirstLayerBiases:");
        for(int i = 0; i < hiddenSize; i++){
            System.out.print(firstLayerBiases[i] + " ");
        }
        System.out.println("");

        System.out.println("\nsecondLayerWeights:");
        for(int i = 0; i < outputSize; i++){
            for(int j = 0; j < hiddenSize; j++) {
                System.out.print(secondLayerWeights[i][j] + " ");
            }
            System.out.println("");
        }
        System.out.println("\nsecondLayerBiases:");
        for(int i = 0; i < outputSize; i++){
            System.out.print(secondLayerBiases[i] + " ");
        }
        System.out.println("\n");
    }

    public void printHiddenVector(){
        System.out.println("Hidden vector:");
        for(int i = 0; i < hiddenSize; i++){
            System.out.print(hiddenVector[i] + " ");
        }
        System.out.println("");
    }

    public void printOutputVector(){
        System.out.println("Output vector:");
        for(int i = 0; i < outputSize; i++){
            System.out.print(outputVector[i] + " ");
        }
        System.out.println("");
    }
}
