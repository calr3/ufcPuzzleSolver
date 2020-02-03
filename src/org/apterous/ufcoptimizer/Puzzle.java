package org.apterous.ufcoptimizer;

import com.google.common.collect.ImmutableList;
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
  private final int minimumHeadMovement;
  private final int minimumThrowSkill;
  private final int maximumSilvers;
  private final int initialHeadMovement;
  private final int initialThrowSkill;

  public Puzzle(
      ImmutableList<Card> availableCards,
      Weight fighterWeight,
      Style fighterStyle,
      ImmutableMultiset<MoveType> moveSlots,
      int minimumChemistry,
      int minimumHeadMovement, int minimumThrowSkill, int maximumSilvers,
      int initialHeadMovement, int initialThrowSkill) {
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
    this.minimumHeadMovement = minimumHeadMovement;
    this.minimumThrowSkill = minimumThrowSkill;
    this.maximumSilvers = maximumSilvers;
    this.initialHeadMovement = initialHeadMovement;
    this.initialThrowSkill = initialThrowSkill;
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

  public int getMinimumHeadMovement() {
    return minimumHeadMovement;
  }

  public int getMinimumThrowSkill() {
    return minimumThrowSkill;
  }

  public int getMaximumSilvers() {
    return maximumSilvers;
  }

  public int getInitialHeadMovement() {
    return initialHeadMovement;
  }

  public int getInitialThrowSkill() {
    return initialThrowSkill;
  }
}
