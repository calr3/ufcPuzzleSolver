package org.apterous.ufcoptimizer;

import javax.annotation.concurrent.Immutable;

/** An immutable representation of a card in the game. */
@Immutable
final class Card {

  private final int id;
  private final Weight weight;
  private final Style style;
  private final MoveType moveType;

  private final int headMovement;
  private final int throwSkill;
  private final Level level;

  public int getHeadMovement() {
    return headMovement;
  }

  public int getThrowSkill() {
    return throwSkill;
  }

  public Level getLevel() {
    return level;
  }

  public Card(int id, Weight weight, Style style, MoveType moveType, int headMovement, int throwSkill, Level level) {
    this.id = id;
    this.weight = weight;
    this.style = style;
    this.moveType = moveType;
    this.headMovement = headMovement;
    this.throwSkill = throwSkill;
    this.level = level;
  }

  @Override
  public boolean equals(Object o) {
    return (o instanceof Card) && ((Card) o).id == id;
  }

  @Override
  public int hashCode() {
    return id;
  }

  @Override
  public String toString() {
    return String.format("%03d:%s:%s:%s:%+d:%+d:%s",
        id, weight, style, moveType, headMovement, throwSkill, level.name().substring(0, 1));
  }

  public int getIndex() {
    return id;
  }

  public MoveType getMoveType() {
    return moveType;
  }

  // TODO(constants need checking and could be optimized)
  public int getChemistryAt(MoveType moveType) {
    int matches = 0;
    if (weight == Weight.BW) {
      matches++;
    }
    if (style == Style.BALANCED) {
      matches++;
    }
    if (moveType == this.moveType) {
      matches++;
    }

    switch (matches) {
      case 0: return 0;
      case 1: return 2;
      case 2: return 5;
      case 3: return 8;
      default: throw new IllegalStateException();
    }
  }
}
