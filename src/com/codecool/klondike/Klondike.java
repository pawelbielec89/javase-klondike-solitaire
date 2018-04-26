package com.codecool.klondike;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Klondike extends Application {

  private static final double WINDOW_WIDTH = 1400;
  private static final double WINDOW_HEIGHT = 900;

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) {
    Card.loadCardImages("card_images/");
    Game game = new Game();
    game.setTableBackground(new Image("/table/background.jpg"));

    primaryStage.setTitle("Klondike Solitaire");
    primaryStage.setScene(new Scene(game, WINDOW_WIDTH, WINDOW_HEIGHT));
    primaryStage.show();
  }
}
