package org.apterous.ufcoptimizer;

import com.google.common.collect.ImmutableMap;

import javax.annotation.concurrent.Immutable;

/**
 * An immutable representation of a card representing a boost in the game.
 */
@Immutable
final class BoostCard extends Card {

  // TODO: model usage count. (Not a constraint, but nice to optimize.)
  public BoostCard(
      int id,
      ImmutableMap<Skill, Integer> skillModifiers, Tier tier) {
    super(id, skillModifiers, tier);
  }

  @Override
  public String toString() {
    return String.format("%03d:BST:      :%c:%-29s",
        getIndex(),
        getTier().name().charAt(0),
        getSkillModifierDescription());
  }
}
