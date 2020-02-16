package org.apterous.ufcoptimizer;

import com.google.common.collect.ImmutableMap;

import javax.annotation.concurrent.Immutable;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

/** An immutable representation of a card in the game. */
@Immutable
final class Card {

  private final int id;
  private final Weight weight;
  private final Style style;
  private final MoveType moveType;

  private final ImmutableMap<Skill, Integer> skillModifiers;
  private final Tier tier;

  public Card(
      int id, Weight weight, Style style, MoveType moveType,
      ImmutableMap<Skill, Integer> skillModifiers, Tier tier) {
    this.id = id;
    this.weight = weight;
    this.style = style;
    this.moveType = moveType;
    this.skillModifiers = skillModifiers;
    this.tier = tier;
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
    return String.format("%03d:%s:%s:%s:%c:%s",
        id, weight, style, moveType, tier.name().charAt(0), getSkillModifierDescription());
  }

  private String getSkillModifierDescription() {
    return skillModifiers.entrySet().stream()
        .filter(entry -> !entry.getValue().equals(0))
        .map(entry -> String.format("%4s=%+d", entry.getKey(), entry.getValue()))
        .collect(Collectors.joining(","));
  }

  public int getIndex() {
    return id;
  }

  public Style getStyle() { return style; }

  public MoveType getMoveType() {
    return moveType;
  }

  public int getSkillModifier(Skill skill) {
    checkNotNull(skill);
    return skillModifiers.getOrDefault(skill, 0);
  }

  public Tier getTier() {
    return tier;
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
