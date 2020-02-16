package org.apterous.ufcoptimizer;

import com.google.common.collect.ImmutableMap;

import javax.annotation.concurrent.Immutable;

/**
 * An immutable representation of a card representing a boost in the game.
 */
@Immutable
final class BoostCard extends Card {

  public BoostCard(
      int id,
      ImmutableMap<Skill, Integer> skillModifiers, Tier tier) {
    super(id, skillModifiers, tier);
  }

  @Override
  public String toString() {
    return String.format("%03d:%c:%s",
        getIndex(),
        getTier().name().charAt(0),
        getSkillModifierDescription());
  }
}
