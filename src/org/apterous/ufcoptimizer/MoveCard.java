package org.apterous.ufcoptimizer;

import com.google.common.collect.ImmutableMap;

import javax.annotation.concurrent.Immutable;

/** An immutable representation of a card representing a move in the game. */
@Immutable
final class MoveCard extends Card {

  private final Weight weight;
  private final Style style;
  private final MoveType moveType;

  public MoveCard(
      int id, Weight weight, Style style, MoveType moveType,
      ImmutableMap<Skill, Integer> skillModifiers, Tier tier) {
    super(id, skillModifiers, tier);
    this.weight = weight;
    this.style = style;
    this.moveType = moveType;
  }

  @Override
  public String toString() {
    return String.format("%03d:%s:%s:%s:%c:%29s",
        getIndex(),
        moveType,
        weight,
        style,
        getTier().name().charAt(0),
        getSkillModifierDescription());
  }

  public Style getStyle() { return style; }

  public MoveType getMoveType() {
    return moveType;
  }

  public int getChemistryInSlot(Puzzle puzzle, MoveType slotMoveType) {
    int matches = 0;
    if (weight.equals(puzzle.getFighterWeight())) {
      matches++;
    }
    if (style.equals(puzzle.getFighterStyle())) {
      matches++;
    }
    if (moveType.equals(slotMoveType)) {
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
