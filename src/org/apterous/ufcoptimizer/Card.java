package org.apterous.ufcoptimizer;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import javax.annotation.concurrent.Immutable;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

/** An immutable representation of a card in the game. */
@Immutable
public class Card {

  private final int id;
  private final ImmutableMap<Skill, Integer> skillModifiers;
  private final Tier tier;

  public Card(int id, ImmutableMap<Skill, Integer> skillModifiers, Tier tier) {
    this.id = id;
    this.skillModifiers = Preconditions.checkNotNull(skillModifiers);
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

  public int getIndex() {
    return id;
  }

  public Tier getTier() {
    return tier;
  }

  public int getSkillModifier(Skill skill) {
    checkNotNull(skill);
    return skillModifiers.getOrDefault(skill, 0);
  }

  protected String getSkillModifierDescription() {
    return skillModifiers.entrySet().stream()
        .filter(entry -> !entry.getValue().equals(0))
        .map(entry -> String.format("%4s=%+d", entry.getKey(), entry.getValue()))
        .collect(Collectors.joining(","));
  }
}
