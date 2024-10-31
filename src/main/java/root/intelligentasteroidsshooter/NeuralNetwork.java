// ===========================================
// I will be using the code I've written for the other project where the neural network guesses the handwritten numbers.
// Only this time I will only have a forward propagation and no backpropagation, since the neural network parameters
// will be updated with Evolutionary Algorithm.
// My neural network Java code: https://github.com/RusFortunat/java_ML_library
// =============================================
package root.intelligentasteroidsshooter;


import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
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
    private double averagePopulationScore; // we won't be zeroing this one

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
        //double rangeW1 = 1.0/inputSize;
        double rangeW1 = 0.2;
        for(int i = 0; i < hiddenSize; i++){
            firstLayerBiases[i] = ThreadLocalRandom.current().nextDouble(-rangeW1,rangeW1);
            for(int j = 0; j < inputSize; j++) {
                firstLayerWeights[i][j] = ThreadLocalRandom.current().nextDouble(-rangeW1,rangeW1);
            }
        }
        //double rangeW2 = 1.0/hiddenSize;
        double rangeW2 = 0.2;
        for(int i = 0; i < outputSize; i++){
            secondLayerBiases[i] = ThreadLocalRandom.current().nextDouble(-rangeW2,rangeW2);
            for(int j = 0; j < hiddenSize; j++) {
                secondLayerWeights[i][j] = ThreadLocalRandom.current().nextDouble(-rangeW2,rangeW2);
            }
        }
        //System.out.println("Check that NN parameters are initialized properly:");
        //printNetworkParameteres();

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
        //System.out.print("outputVector: [");
        for(int i = 0; i < outputSize; i++){
            outputVector[i] = Math.exp(outputVector[i]) / totalSum;
            //System.out.print(outputVector[i] + ", ");
        }
        //System.out.println("]");

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

    // load params
    public void loadNNparameters(Scanner fileReader){
        boolean wePrint = false;
        String skipString = fileReader.nextLine();
        if(wePrint) System.out.println(skipString);
        for(int i = 0; i < hiddenSize; i++){
            String readFirstWeights = fileReader.nextLine();
            String[] parts = readFirstWeights.split(",");
            for(int j = 0; j < inputSize; j++) {
                firstLayerWeights[i][j] = Double.valueOf(parts[j]);
                if(wePrint) System.out.print(firstLayerWeights[i][j]);
            }
        }
        if(wePrint) System.out.println("");
        skipString = fileReader.nextLine();
        skipString = fileReader.nextLine();
        if(wePrint) System.out.println(skipString);
        String readFirstBiases = fileReader.nextLine();
        String[] parts = readFirstBiases.split(",");
        for(int i = 0; i < hiddenSize; i++){
            firstLayerBiases[i] = Double.valueOf(parts[i]);
            if(wePrint) System.out.print(firstLayerBiases[i]);
        }
        if(wePrint) System.out.println("");
        skipString = fileReader.nextLine();
        skipString = fileReader.nextLine();
        if(wePrint) System.out.println(skipString);
        for(int i = 0; i < outputSize; i++){
            String readSecondWeights = fileReader.nextLine();
            parts = readSecondWeights.split(",");
            for(int j = 0; j < hiddenSize; j++) {
                secondLayerWeights[i][j] = Double.valueOf(parts[j]);
            }
        }
        if(wePrint) System.out.println(secondLayerWeights);
        skipString = fileReader.nextLine();
        skipString = fileReader.nextLine();
        if(wePrint) System.out.println(skipString);
        readFirstBiases = fileReader.nextLine();
        parts = readFirstBiases.split(",");
        for(int i = 0; i < outputSize; i++){
            secondLayerBiases[i] = Double.valueOf(parts[i]);
        }
        if(wePrint) System.out.println(secondLayerBiases);
        skipString = fileReader.nextLine();
    }

    public void copyNetworkParameters(NeuralNetwork successfulNetwork){
        firstLayerBiases = successfulNetwork.getFirstLayerBiases().clone();
        firstLayerWeights = successfulNetwork.getFirstLayerWeights().clone();
        secondLayerBiases = successfulNetwork.getSecondLayerBiases().clone();
        secondLayerWeights = successfulNetwork.getSecondLayerWeights().clone();
    }

    public void addPoints(int points) {this.score += points;}

    public void resetShip(){
        this.ship.getPolygon().setTranslateX(SinglePlayerView.WIDTH/2);
        this.ship.getPolygon().setTranslateX(SinglePlayerView.HEIGHT/2);
    }

    @Override
    public int compareTo(NeuralNetwork otherNetwork){
        return otherNetwork.getScore() - this.score; // order from big to small
    }

    // setters
    public void setScore(int value) { score = value;}
    public void setScoreForPrinting(int value) { scoreForPrinting = value;}
    public void setAveragePopulationScore(double value) { averagePopulationScore = value;}

    // getters
    public Hitbox getShip() { return ship; }
    public int getScore() { return score; }
    public int getScoreForPrinting() {return scoreForPrinting; }
    public double getAveragePopulationScore(){ return averagePopulationScore; }
    public double getMutationRate() { return mutationRate; }
    public int getInputSize(){ return inputSize; }
    public int getHiddenSize(){ return hiddenSize; }
    public int getOutputSize(){ return outputSize; }
    public double[] getFirstLayerBiases(){  return firstLayerBiases;}
    public double[] getSecondLayerBiases(){  return secondLayerBiases;}
    public double[][] getFirstLayerWeights(){ return firstLayerWeights; }
    public double[][] getSecondLayerWeights(){ return secondLayerWeights; }

    // printers
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
