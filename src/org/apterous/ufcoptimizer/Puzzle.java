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

  private final ImmutableList<MoveCard> availableCards;
  private final ImmutableList<MoveCard> strikingCards;
  private final ImmutableList<MoveCard> grapplingCards;

  private final Weight fighterWeight;
  private final Style fighterStyle;

  private final ImmutableMultiset<MoveType> moveSlots;
  private final int strikingSlotCount;
  private final int grapplingSlotCount;

  private final int minimumChemistry;
  private final ImmutableMap<Skill, RangeConstraint> skillConstraints;
  private final ImmutableMap<Tier, RangeConstraint> cardTierConstraints;
  private final ImmutableMap<Style, RangeConstraint> cardStyleConstraints;
  private final ImmutableMap<Skill, Integer> initialSkill;

  public Puzzle(
      ImmutableList<MoveCard> availableCards,
      Weight fighterWeight,
      Style fighterStyle,
      ImmutableMultiset<MoveType> moveSlots,
      int minimumChemistry,
      ImmutableMap<Skill, RangeConstraint> skillConstraints,
      ImmutableMap<Tier, RangeConstraint> cardTierConstraints,
      ImmutableMap<Style, RangeConstraint> cardStyleConstraints,
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
    this.cardTierConstraints = checkNotNull(cardTierConstraints);
    this.cardStyleConstraints = checkNotNull(cardStyleConstraints);
    this.initialSkill = checkNotNull(initialSkill);
  }

  public ImmutableList<MoveCard> getAvailableCards() {
    return availableCards;
  }

  public ImmutableList<MoveCard> getStrikingCards() {
    return strikingCards;
  }

  public ImmutableList<MoveCard> getGrapplingCards() {
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

  public RangeConstraint getCardTierConstraint(Tier tier) {
    return cardTierConstraints.getOrDefault(tier, RangeConstraint.UNCONSTRAINED);
  }

  public RangeConstraint getCardStyleConstraint(Style style) {
    return cardStyleConstraints.getOrDefault(style, RangeConstraint.UNCONSTRAINED);
  }

  public int getInitialSkill(Skill skill) {
    return initialSkill.getOrDefault(skill, 0);
  }
}
