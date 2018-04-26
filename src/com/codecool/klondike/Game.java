package com.codecool.klondike;

import com.codecool.klondike.Pile.PileType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
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
      };


  public boolean isGameWon() {
    int tableauEmpty = 0;
    for (int tableauPileIndex = 0; tableauPileIndex < tableauPiles.size(); tableauPileIndex++) {
      if (tableauPiles.get(tableauPileIndex).isEmpty()) {
        tableauEmpty += 0;
      } else {
        tableauEmpty++;
      }
    }

    if (stockPile.isEmpty() && discardPile.isEmpty() && tableauEmpty == 0) {
      System.out.print("WYGRANAAAAAAAAAAAAAAAAA!!!!!!!!!!!!!!!!!!!!!!!!!!");
      return true;
    } else {
      //System.out.print("Jeszcze nie wygrales!");
      return false;
    }
  }

  public Game() {
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
        Card card = stockPile.getTopCard();
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

  public boolean isMoveValid(Card card, Pile destPile) {
    if (destPile.getPileType() == PileType.TABLEAU){
      return canDragOnTableau(card, destPile);
    } else if (destPile.getPileType() == PileType.FOUNDATION) {
      return canDragOnFoundation(card, destPile);
    }  
    return true;
  }

  public boolean canDragOnTableau(Card card, Pile destPile) {
    if (destPile.numOfCards() < 1){
      if (card.getRank() != 13) { 
        return false; 
      }
      return true;
    } else if (destPile.getTopCard().getRank() - card.getRank() != 1) {
      return false;
    }  else if (!Card.isOppositeColor(destPile.getTopCard(), card)) {
      return false;
    }
    isGameWon();
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
    Card lowerCard = card.getContainingPile().getLowerCard(card);    

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
    if (lowerCard != null) lowerCard.autoFlip();
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
