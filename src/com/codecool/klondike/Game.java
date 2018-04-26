package com.codecool.klondike;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Pane;

public class Game extends Pane {
  private List<Card> deck = new ArrayList<>();

  private Pile stockPile;
  private Pile discardPile;
  private List<Pile> foundationPiles = FXCollections.observableArrayList();
  private List<Pile> tableauPiles = FXCollections.observableArrayList();
  private List<Pile> dragablePiles = FXCollections.observableArrayList();

  private double dragStartX, dragStartY;
  private List<Card> draggedCards = FXCollections.observableArrayList();

  private static double STOCK_GAP = 1;
  private static double FOUNDATION_GAP = 0;
  private static double TABLEAU_GAP = 30;

  private static String actualCatalogueName = "card_images";

  private EventHandler<MouseEvent> onMouseClickedHandler =
      e -> {
        Card card = (Card) e.getSource();
        if (card.getContainingPile().getPileType() == Pile.PileType.STOCK
            && card == stockPile.getTopCard()) {
          card.moveToPile(discardPile);
          card.flip();
          card.setMouseTransparent(false);
          System.out.println("Placed " + card + " to the waste.");
        } else if (card.getContainingPile().getPileType() == Pile.PileType.TABLEAU
            && card == card.getContainingPile().getTopCard()
            && card.isFaceDown()) {
          card.flip();
        }
      };

  private EventHandler<MouseEvent> stockReverseCardsHandler =
      e -> {
        refillStockFromDiscard(discardPile);
      };

  private EventHandler<MouseEvent> onMousePressedHandler =
      e -> {
        dragStartX = e.getSceneX();
        dragStartY = e.getSceneY();
      };

  private EventHandler<MouseEvent> onMouseDraggedHandler =
      e -> {
        Card card = (Card) e.getSource();
        Pile activePile = card.getContainingPile();
        if (activePile.getPileType() == Pile.PileType.STOCK) return;
        double offsetX = e.getSceneX() - dragStartX;
        double offsetY = e.getSceneY() - dragStartY;

        draggedCards.clear();
        if (card == activePile.getTopCard() || (activePile != discardPile && !card.isFaceDown())) {
          moveDraggedCards(card, offsetX, offsetY);
        }
      };

  private EventHandler<MouseEvent> onMouseReleasedHandler =
      e -> {
        if (draggedCards.isEmpty()) {
          return;
        }
        Card card = (Card) e.getSource();
        dragablePiles.addAll(tableauPiles);
        dragablePiles.addAll(foundationPiles);
        Pile pile = getValidIntersectingPile(card, dragablePiles);
        if (pile != null) {

          handleValidMove(card, pile);

        } else {
          draggedCards.forEach(MouseUtil::slideBack);
          draggedCards.clear();
        }
        gameWon();
      };

  public boolean isGameWon() {
    int allCards = 52;
    int pileFull = 0;
    for (int foundationPileIndex = 0; foundationPileIndex < 4; foundationPileIndex++) {
      pileFull += foundationPiles.get(foundationPileIndex).numOfCards();
    }
    if (pileFull == (allCards - 1)) {
      return true;
    }
    return false;
  }

  public void gameWon() {
    if (isGameWon()) {
      System.out.println("Game is won!");
      Alert alert = new Alert(AlertType.CONFIRMATION);
      alert.setTitle("Game won!");
      alert.setHeaderText("You won the game!");
      alert.setContentText("Do you want to play once more?");

      Optional<ButtonType> result = alert.showAndWait();
      if (result.get() == ButtonType.OK) {
        for (int i = 0; i < 7; i++) {
          tableauPiles.get(i).moveTo(stockPile);
        }
        for (int i = 0; i < 4; i++) {
          foundationPiles.get(i).moveTo(stockPile);
        }
        discardPile.moveTo(stockPile);
        System.out.println(stockPile.numOfCards());
        stockPile.flipFaceUpCards();
        setCardsOnTableau();
        putStockToDiscard(stockPile);
        refillStockFromDiscard(discardPile);
        System.out.println(stockPile.numOfCards());
      }
    } else {
      System.out.println("Not yet!");
    }
  }

  public Game() {
    initToolbar();
    deck = Card.createNewDeck();
    initPiles();
    dealCards();
  }

  private void moveDraggedCards(Card draggedCard, double offsetX, double offsetY) {
    Pile activePile = draggedCard.getContainingPile();
    int index = activePile.getCards().indexOf(draggedCard);
    int cardsAmount = activePile.numOfCards();

    for (int i = index; i < cardsAmount; i++) {
      Card card = activePile.getCards().get(i);
      draggedCards.add(card);
      card.getDropShadow().setRadius(20);
      card.getDropShadow().setOffsetX(10);
      card.getDropShadow().setOffsetY(10);

      card.toFront();
      card.setTranslateX(offsetX + i * 5);
      card.setTranslateY(offsetY + i * 5);
    }
  }

  /** Sets cards on tableau in standard klondike way. */
  private void setCardsOnTableau() {
    for (int i = 0; i < 7; i++) {
      for (int j = i; j >= 0; j--) {
        Card card = stockPile.getRandomCard();
        card.moveToPile(tableauPiles.get(i));
        if (j == 0) card.flip();
      }
    }
  }

  public void addMouseEventHandlers(Card card) {
    card.setOnMousePressed(onMousePressedHandler);
    card.setOnMouseDragged(onMouseDraggedHandler);
    card.setOnMouseReleased(onMouseReleasedHandler);
    card.setOnMouseClicked(onMouseClickedHandler);
  }

  public void refillStockFromDiscard(Pile discardPile) {
    int discardPileAmount = discardPile.numOfCards();
    for (int i = 0; i < discardPileAmount; i++) {
      discardPile.getTopCard().moveToPile(stockPile);
      stockPile.getTopCard().flip();
    }
    System.out.println("Stock refilled from discard pile.");
  }

  public void putStockToDiscard(Pile stockPile) {
    int stockPileAmount = stockPile.numOfCards();
    for (int i = 0; i < stockPileAmount; i++) {
      stockPile.getTopCard().moveToPile(discardPile);
      discardPile.getTopCard().flip();
    }
    System.out.println("Stock refilled from discard pile.");
  }

  public boolean isMoveValid(Card card, Pile destPile) {
    if (destPile.getPileType() == Pile.PileType.TABLEAU) {
      return canDragOnTableau(card, destPile);
    } else if (destPile.getPileType() == Pile.PileType.FOUNDATION) {
      return canDragOnFoundation(card, destPile);
    }
    return true;
  }

  public boolean canDragOnTableau(Card card, Pile destPile) {
    if (destPile.numOfCards() < 1) {
      if (card.getRank() != 13) {
        return false;
      }
      return true;
    } else if (destPile.getTopCard().getRank() - card.getRank() != 1) {
      return false;
    } else if (!Card.isOppositeColor(destPile.getTopCard(), card)) {
      return false;
    }

    return true;
  }

  public boolean canDragOnFoundation(Card card, Pile destPile) {
    if (destPile.numOfCards() < 1) {
      if (card.getRank() != 1) {
        return false;
      }
    } else if (card.getRank() - destPile.getTopCard().getRank() != 1
        || destPile.getTopCard().getSuit() != card.getSuit()) {
      return false;
    }
    return true;
  }

  private Pile getValidIntersectingPile(Card card, List<Pile> piles) {
    Pile result = null;
    for (Pile pile : piles) {
      if (!pile.equals(card.getContainingPile())
          && isOverPile(card, pile)
          && isMoveValid(card, pile)) result = pile;
    }
    return result;
  }

  private boolean isOverPile(Card card, Pile pile) {
    if (pile.isEmpty()) return card.getBoundsInParent().intersects(pile.getBoundsInParent());
    else return card.getBoundsInParent().intersects(pile.getTopCard().getBoundsInParent());
  }

  private void handleValidMove(Card card, Pile destPile) {
    String msg = null;
    if (destPile.isEmpty()) {
      if (destPile.getPileType().equals(Pile.PileType.FOUNDATION))
        msg = String.format("Placed %s to the foundation.", card);
      if (destPile.getPileType().equals(Pile.PileType.TABLEAU))
        msg = String.format("Placed %s to a new pile.", card);
    } else {
      msg = String.format("Placed %s to %s.", card, destPile.getTopCard());
    }
    System.out.println(msg);
    MouseUtil.slideToDest(draggedCards, destPile);
    draggedCards.clear();
  }

  private void initToolbar() {
    Button undoButt = new Button("Undo");
    Button restartButt = new Button("Restart");
    Button exitButt = new Button("Exit");
    Button changeThemeButt = new Button("Change Theme");

    exitButt.setOnAction(
        new EventHandler<ActionEvent>() {
          @Override
          public void handle(ActionEvent e) {
            exitConfirmation();
          }
        });
    restartButt.setOnAction(
        new EventHandler<ActionEvent>() {
          @Override
          public void handle(ActionEvent e) {
            restartConfirmation();
          }
        });

    changeThemeButt.setOnAction(
        new EventHandler<ActionEvent>() {
          @Override
          public void handle(ActionEvent e) {
            changeTheme();
          }
        });

    ToolBar toolbar = new ToolBar(undoButt, restartButt, changeThemeButt, exitButt);
    toolbar.setBackground(new Background(new BackgroundFill(null, null, null)));
    toolbar.setLayoutX(0);
    toolbar.setLayoutY(0);
    toolbar.setOrientation(Orientation.VERTICAL);
    getChildren().add(toolbar);
  }

  private void exitConfirmation() {
    Alert alert = new Alert(AlertType.CONFIRMATION);
    alert.setTitle("Leave game");
    alert.setHeaderText("You going to leave game");
    alert.setContentText("Are you sure?");

    Optional<ButtonType> result = alert.showAndWait();
    if (result.get() == ButtonType.OK) {
      System.exit(0);
    }
  }

  private void restartConfirmation() {
    Alert alert = new Alert(AlertType.CONFIRMATION);
    alert.setTitle("Restarting game");
    alert.setHeaderText("You going to restart game");
    alert.setContentText("Are you sure?");

    Optional<ButtonType> result = alert.showAndWait();

    if (result.get() == ButtonType.OK) {
      for (int i = 0; i < 7; i++) {
        tableauPiles.get(i).moveTo(stockPile);
      }
      for (int i = 0; i < 4; i++) {
        foundationPiles.get(i).moveTo(stockPile);
      }
      discardPile.moveTo(stockPile);
      System.out.println(stockPile.numOfCards());
      stockPile.flipFaceUpCards();
      // stockPile.shufflePile();
      setCardsOnTableau();
      putStockToDiscard(stockPile);
      refillStockFromDiscard(discardPile);
      System.out.println(stockPile.numOfCards());
    }
  }

  public void changeTheme() {
    if (actualCatalogueName.equals("card_images/")) {
      actualCatalogueName = "new_card_images/";
      Card.loadCardImages(actualCatalogueName);

    } else {
      actualCatalogueName = "card_images/";
      Card.loadCardImages(actualCatalogueName);
    }
    stockPile.changeImages();
    discardPile.changeImages();
    for (Pile pile : foundationPiles) {
      pile.changeImages();
    }
    for (Pile pile : tableauPiles) {
      pile.changeImages();
    }

    /*
    Alert alert = new Alert(AlertType.CONFIRMATION);
    alert.setTitle("Changing theme");
    alert.setHeaderText("Which theme do you want?");
    alert.setContentText("Select one");
    ButtonType optionOne = new ButtonType("One");
    ButtonType optionTwo = new ButtonType("Two");
    Image imageOne = new Image(getClass().getResourceAsStream("/card_images/back.jpg"));
    optionOne.setGraphic(new ImageView(imageOne));
    Optional<ButtonType> result = alert.showAndWait();
    */
  }

  private void initPiles() {
    stockPile = new Pile(Pile.PileType.STOCK, "Stock", STOCK_GAP);
    stockPile.setBlurredBackground();
    stockPile.setLayoutX(95);
    stockPile.setLayoutY(20);
    stockPile.setOnMouseClicked(stockReverseCardsHandler);
    getChildren().add(stockPile);

    discardPile = new Pile(Pile.PileType.DISCARD, "Discard", STOCK_GAP);
    discardPile.setBlurredBackground();
    discardPile.setLayoutX(285);
    discardPile.setLayoutY(20);
    getChildren().add(discardPile);

    for (int i = 0; i < 4; i++) {
      Pile foundationPile = new Pile(Pile.PileType.FOUNDATION, "Foundation " + i, FOUNDATION_GAP);
      foundationPile.setBlurredBackground();
      foundationPile.setLayoutX(610 + i * 180);
      foundationPile.setLayoutY(20);
      foundationPiles.add(foundationPile);
      getChildren().add(foundationPile);
    }
    for (int i = 0; i < 7; i++) {
      Pile tableauPile = new Pile(Pile.PileType.TABLEAU, "Tableau " + i, TABLEAU_GAP);
      tableauPile.setBlurredBackground();
      tableauPile.setLayoutX(95 + i * 180);
      tableauPile.setLayoutY(275);
      tableauPiles.add(tableauPile);
      getChildren().add(tableauPile);
    }
  }

  public void dealCards() {
    Iterator<Card> deckIterator = deck.iterator();
    // TODO
    deckIterator.forEachRemaining(
        card -> {
          stockPile.addCard(card);
          addMouseEventHandlers(card);
          getChildren().add(card);
        });
    setCardsOnTableau();
    putStockToDiscard(stockPile);
    refillStockFromDiscard(discardPile);
  }

  public void setTableBackground(Image tableBackground) {
    setBackground(
        new Background(
            new BackgroundImage(
                tableBackground,
                BackgroundRepeat.REPEAT,
                BackgroundRepeat.REPEAT,
                BackgroundPosition.CENTER,
                BackgroundSize.DEFAULT)));
  }
}
