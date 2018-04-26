package com.codecool.klondike;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

public class Pile extends Pane {

  private PileType pileType;
  private String name;
  private double cardGap;
  private ObservableList<Card> cards = FXCollections.observableArrayList();

  public Pile(PileType pileType, String name, double cardGap) {
    this.pileType = pileType;
    this.cardGap = cardGap;
  }

  public PileType getPileType() {
    return pileType;
  }

  public String getName() {
    return name;
  }

  public double getCardGap() {
    return cardGap;
  }

  public ObservableList<Card> getCards() {
    return cards;
  }

  public int numOfCards() {
    int cardsAmount = cards.size();
    return cardsAmount;
  }

  public boolean isEmpty() {
    return cards.isEmpty();
  }

  public void moveTo(Pile stockPile) {
    int cardsAmount = this.numOfCards();
    for (int card = 0; card < cardsAmount; card++) {
      this.getTopCard().moveToPile(stockPile);
    }
  }

  public void addCard(Card card) {
    cards.add(card);
    card.setContainingPile(this);
    card.toFront();
    layoutCard(card);
  }

  private void layoutCard(Card card) {
    card.relocate(
        card.getLayoutX() + card.getTranslateX(), card.getLayoutY() + card.getTranslateY());
    card.setTranslateX(0);
    card.setTranslateY(0);
    card.setLayoutX(getLayoutX());
    card.setLayoutY(getLayoutY() + (cards.size() - 1) * cardGap);
  }

  public Card getTopCard() {
    if (cards.isEmpty()) return null;
    else return cards.get(cards.size() - 1);
  }

  public void setBlurredBackground() {
    setPrefSize(Card.WIDTH, Card.HEIGHT);
    BackgroundFill backgroundFill = new BackgroundFill(Color.gray(0.0, 0.2), null, null);
    Background background = new Background(backgroundFill);
    GaussianBlur gaussianBlur = new GaussianBlur(10);
    setBackground(background);
    setEffect(gaussianBlur);
  }

  public enum PileType {
    STOCK,
    DISCARD,
    FOUNDATION,
    TABLEAU
  }

  public void tableauReverseCardsHandler(Pile destPile) {
    int cardsAmount = this.cards.size();
    Card card = this.cards.get(cardsAmount);
    card.moveToPile(destPile);
  }

  public void shufflePile() {
    FXCollections.shuffle(cards);
  }

  public void flushPile() {
    cards.clear();
  }

  public void flipFaceUpCards() {
    for (int i = 0; i < numOfCards(); i++) {
      if (cards.get(i).isFaceDown() == false) {
        cards.get(i).flip();
      }
    }
  }

  public void changeImages() {
    for (Card card : cards) {
      card.changeImage();
    }
  }
}
