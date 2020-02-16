package org.apterous.ufcoptimizer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.primitives.Ints;

import javax.annotation.concurrent.Immutable;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;

/** An immutable general description of a puzzle to be solved. */
@Immutable
final class Puzzle {

  private final ImmutableList<Card> availableCards;
  private final ImmutableList<Card> strikingCards;
  private final ImmutableList<Card> grapplingCards;

  private final Weight fighterWeight;
  private final Style fighterStyle;

  private final ImmutableMultiset<MoveType> moveSlots;
  private final int strikingSlotCount;
  private final int grapplingSlotCount;

  private final int minimumChemistry;
  private final ImmutableMap<Skill, RangeConstraint> skillConstraints;
  private final int maximumSilvers;
  private final ImmutableMap<Skill, Integer> initialSkill;

  public Puzzle(
      ImmutableList<Card> availableCards,
      Weight fighterWeight,
      Style fighterStyle,
      ImmutableMultiset<MoveType> moveSlots,
      int minimumChemistry,
      ImmutableMap<Skill, RangeConstraint> skillConstraints,
      int maximumSilvers,
      ImmutableMap<Skill, Integer> initialSkill) {
    this.availableCards = availableCards;
    strikingCards =
        availableCards.stream().filter(card -> card.getMoveType().isStriking()).collect(toImmutableList());
    grapplingCards =
        availableCards.stream().filter(card -> !card.getMoveType().isStriking()).collect(toImmutableList());

    this.fighterWeight = checkNotNull(fighterWeight);
    this.fighterStyle = checkNotNull(fighterStyle);

    this.moveSlots = moveSlots;
    this.strikingSlotCount =
        Ints.checkedCast(moveSlots.stream().filter(MoveType::isStriking).count());
    this.grapplingSlotCount = moveSlots.size() - strikingSlotCount;

    this.minimumChemistry = minimumChemistry;
    this.skillConstraints = checkNotNull(skillConstraints);
    this.maximumSilvers = maximumSilvers;
    this.initialSkill = checkNotNull(initialSkill);
  }

  public ImmutableList<Card> getAvailableCards() {
    return availableCards;
  }

  public ImmutableList<Card> getStrikingCards() {
    return strikingCards;
  }

  public ImmutableList<Card> getGrapplingCards() {
    return grapplingCards;
  }

  public Weight getFighterWeight() {
    return fighterWeight;
  }

  public Style getFighterStyle() {
    return fighterStyle;
  }

  public int getMoveSlotCount() {
    return moveSlots.size();
  }

  public int getStrikingSlotCount() {
    return strikingSlotCount;
  }

  public int getGrapplingSlotCount() {
    return grapplingSlotCount;
  }

  public ImmutableMultiset<MoveType> getMoveSlots() {
    return moveSlots;
  }

  public int getMinimumChemistry() {
    return minimumChemistry;
  }

  public RangeConstraint getSkillConstraint(Skill skill) {
    return skillConstraints.getOrDefault(skill, RangeConstraint.UNCONSTRAINED);
  }

  public int getMaximumSilvers() {
    return maximumSilvers;
  }

  public int getInitialSkill(Skill skill) {
    return initialSkill.getOrDefault(skill, 0);
  }
}
