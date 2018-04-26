package com.codecool.klondike;

import java.util.*;
import java.util.Collections;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

public class Card extends ImageView {

  private SuitType suitType;
  private RankType rankType;
  private boolean faceDown;
  private boolean isBlack;
  private Image backFace;
  private Image frontFace;
  private Pile containingPile;
  private DropShadow dropShadow;

  static Image cardBackImage;

  private static final Map<Integer, SuitType> suitMap = new HashMap<>();
  private static final Map<Integer, RankType> rankMap = new HashMap<>();
  private static final Map<String, Image> cardFaceImages = new HashMap<>();
  private static final Map<SuitType, Boolean> isBlackList = new HashMap<>();

  static {
    int isItBlack = 0;
    for (SuitType suit : SuitType.values()) {
      if (isItBlack < 2) {
        isBlackList.put(suit, false);
      } else {
        isBlackList.put(suit, true);
      }
      isItBlack++;
    }
  };

  static {
    int suitAmount = 0;
    for (SuitType dir : SuitType.values()) {
      suitAmount++;
      suitMap.put(suitAmount, dir);
    }
  };

  static {
    int rankAmount = 0;
    for (RankType dir : RankType.values()) {
      rankAmount++;
      rankMap.put(rankAmount, dir);
    }
  };

  public static final int WIDTH = 150;
  public static final int HEIGHT = 215;

  public Card(SuitType suitType, RankType rankType, boolean faceDown) {
    this.suitType = suitType;
    this.rankType = rankType;
    this.faceDown = faceDown;
    this.dropShadow = new DropShadow(2, Color.gray(0, 0.75));
    this.isBlack = isBlackList.get(suitType);
    backFace = cardBackImage;
    frontFace = cardFaceImages.get(getShortName());
    setImage(faceDown ? backFace : frontFace);
    setEffect(dropShadow);
  }

  public String getSuit() {
    return suitType.getName();
  }

  public int getRank() {
    return rankType.getIndex();
  }

  public boolean getIsBlack() {
    return isBlack;
  }

  public boolean isFaceDown() {
    return faceDown;
  }

  public void setFaceDown() {
    this.faceDown = true;
  }

  public String getShortName() {
    int suit = suitType.getIndex();
    int rank = rankType.getIndex();
    String suitString = Integer.toString(suit);
    String rankString = Integer.toString(rank);

    return "S" + suitString + "R" + rankString;
  }

  public DropShadow getDropShadow() {
    return dropShadow;
  }

  public Pile getContainingPile() {
    return containingPile;
  }

  public void setContainingPile(Pile containingPile) {
    this.containingPile = containingPile;
  }

  public void moveToPile(Pile destPile) {
    this.getContainingPile().getCards().remove(this);
    destPile.addCard(this);
  }

  public void autoFlip() {
    if (this.getContainingPile().getPileType() == Pile.PileType.TABLEAU
        && this.isFaceDown()) this.flip();
  }

  public void flip() {
    faceDown = !faceDown;
    setImage(faceDown ? backFace : frontFace);
  }

  @Override
  public String toString() {
    String suit = suitType.getName();
    String rank = rankType.getName();

    return "The " + "Rank " + rank + " of " + "Suit " + suit;
  }

  public static boolean isOppositeColor(Card card1, Card card2) {
    if (card1.getIsBlack() == card2.getIsBlack()) {
      return false;
    } else {
      return true;
    }
  }

  public static boolean isSameSuit(Card card1, Card card2) {
    return card1.getSuit() == card2.getSuit();
  }

  public static List<Card> createNewDeck() {
    List<Card> result = new ArrayList<>();
    for (int suit = 1; suit < 5; suit++) {
      for (int rank = 1; rank < 14; rank++) {
        result.add(new Card(suitMap.get(suit), rankMap.get(rank), true));
      }
    }
    Collections.shuffle(result);
    return result;
  }

  public static void loadCardImages() {
    cardBackImage = new Image("card_images/card_back.png");
    String suitName = "";
    int suitToPass;
    for (int suit = 1; suit < 5; suit++) {
      suitName = suitMap.get(suit).getName();
      suitToPass = suit;

      for (int rank = 1; rank < 14; rank++) {
        String cardName = suitName + rank;
        String cardId = "S" + suitToPass + "R" + rank;
        String imageFileName = "card_images/" + cardName + ".png";
        cardFaceImages.put(cardId, new Image(imageFileName));
      }
    }
  }

  public enum SuitType {
    HEARTS(1, "hearts"),
    DIAMONDS(2, "diamonds"),
    SPADES(3, "spades"),
    CLUBS(4, "clubs");

    private final int index;
    private final String suitName;

    SuitType(int index, String suitName) {
      this.index = index;
      this.suitName = suitName;
    }

    String getName() {
      return this.suitName;
    }

    int getIndex() {
      return this.index;
    }
  }

  public enum RankType {
    ACE(1, "ace"),
    TWO(2, "two"),
    THREE(3, "three"),
    FOUR(4, "four"),
    FIVE(5, "five"),
    SIX(6, "six"),
    SEVEN(7, "seven"),
    EIGHT(8, "eight"),
    NINE(9, "nine"),
    TEN(10, "ten"),
    JACK(11, "jack"),
    QUEEN(12, "queen"),
    KING(13, "king");

    private final int index;
    private final String rankName;

    RankType(int index, String rankName) {
      this.index = index;
      this.rankName = rankName;
    }

    String getName() {
      return this.rankName;
    }

    int getIndex() {
      return this.index;
    }
  }
}
