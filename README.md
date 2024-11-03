# Intelligent Asteroid Shooter

Classic ATARI-like asteroid shooter game with an AI twist -- in additional to Single Player mode, you can train your very own neural network that will take control of the ship. 

## Installation

This is a Java desktop application that was written with JavaFX. Therefore, to install and run the application after cloning the project, you will need to have [Maven](https://maven.apache.org/download.cgi), [JDK 23](https://www.oracle.com/java/technologies/downloads/#jdk23-linux), and [JavaFX](https://gluonhq.com/products/javafx/) on your machine. Assuming you have all three (and added Maven & JDK to your PATH as well), use the following command to generate .jar file that is needed for app execution:
```
mvn package
```
and then this command to launch the app
```
java --add-modules javafx.controls,javafx.fxml --module-path .\dependency-jars\ -jar .\Intelligent-Asteroids-Shooter-1.0-SNAPSHOT.jar
```

## Descriptiion
### Single Player

The single player mode was inspired by the exercise that comes from [MOOC Java Programming II course](https://java-programming.mooc.fi/part-14/3-larger-application-asteroids). After selecting your ship and game difficulty level, try shooting as much asteroids as you can and immortalize your name in the table of records. The later was implemented with use of H2 Database. 

![](https://github.com/RusFortunat/Intelligent-Asteroids-Shooter/blob/main/docs/Single-Player-mode-480.gif)

### Train Neural Networks with the Evolutionary Algorithm

In our implementation, the neural network receives information about the ship surroundings and whether the ship is close to the screen edges where asteroids spawn. The neural network processes the input using regular forward-propagation with ReLU activation and produces probabilities of the ship to choose a certain orientation. 

![](https://github.com/RusFortunat/Intelligent-Asteroids-Shooter/blob/main/docs/schematics.png)

We train the neural networks by using the following implementation of **Evolutionary Algorithm**:
1. Create a collection of different networks with random parameters 
2. Let the networks stir the ship and estimate their individual performances
3. Select top 25% networks that perform better and discard the rest
4. Generate new "child" networks via crossover: pick two successful "parents" and create a new set of parameters by randomly picking the same parameters from parent1 or parent2.
5. Mutate the neural network parameters of all networks by some small amount
6. Repeat 2-5

If all works well, you should see the average performance of the network population steadily growing, as in this sample video:

![](https://github.com/RusFortunat/Intelligent-Asteroids-Shooter/blob/main/docs/trainAI-mode.gif)

## Author
Ruslan Mukhamadiarov

## License
This project is licensed under the MIT License - see the LICENSE.md file for details
