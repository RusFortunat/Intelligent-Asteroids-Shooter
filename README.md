# Intelligent Asteroid Shooter

Classic ATARI-like asteroid shooter game with an AI twist -- in additional to Single Player mode, you can train your very own neural network that will take control of the ship. 

## Installation

This is a Java desktop application that was written with JavaFX. Therefore, to install and run the application you will need to have [Maven](https://maven.apache.org/download.cgi), [JDK 23](https://www.oracle.com/java/technologies/downloads/#jdk23-linux), and latest [JavaFX](https://gluonhq.com/products/javafx/) on your machine. Assuming you have all three (and added all to your PATH as well), use the following command to generate .jar file that is needed for app execution:
```
mvn install
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

The neural network will be taking the observations of asteroid positions and the ship's velocity and then will be proposing possible actions. I will train the network with gradient-free Evolutionary Strategy apporach and track the performance improvement in the API as well.

![](https://github.com/RusFortunat/Intelligent-Asteroids-Shooter/blob/main/docs/trainAI-mode.gif)

