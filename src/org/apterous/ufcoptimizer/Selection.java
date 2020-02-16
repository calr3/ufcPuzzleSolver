package org.apterous.ufcoptimizer;

import com.google.common.base.Preconditions;

import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.joining;

/** A mutable representation of the cards actually assigned. */
public class Selection {

  private final Puzzle puzzle;
  private final Card[] cards; // TODO: use two arrays.
  private final MoveType[] moveSlotTypes;

  private final BitSet used;
  private int chemistry;
  private EnumCounter<Skill> skillCounter;
  private EnumCounter<Tier> cardTierCounter;

  public Selection(Puzzle puzzle) {
    this.puzzle = Preconditions.checkNotNull(puzzle);
    this.cards = new Card[puzzle.getMoveSlotCount()];
    // Order can be arbitrary as long as striking slots are first.
    this.moveSlotTypes =
        puzzle.getMoveSlots().stream()
            .sorted((slotA, slotB) -> Boolean.compare(slotB.isStriking(), slotA.isStriking()))
            .collect(Collectors.toList())
            .toArray(new MoveType[0]);

    this.used = new BitSet(puzzle.getAvailableCards().size());
    this.chemistry = 0;
    this.skillCounter = new EnumCounter<>(Skill.values(), puzzle::getInitialSkill);
    this.cardTierCounter = new EnumCounter<>(Tier.values(), tier -> 0);
  }

  public Selection(Selection selection) {
    puzzle = selection.puzzle;
    cards = selection.cards.clone();
    moveSlotTypes = selection.moveSlotTypes.clone();

    used = (BitSet) selection.used.clone();
    chemistry = selection.chemistry;
    skillCounter = new EnumCounter<>(selection.skillCounter);
    cardTierCounter = new EnumCounter<>(selection.cardTierCounter);
}

  @Override
  public String toString() {
    return String.format("Chem=%2d; %s; %s",
        chemistry,
        skillCounter.values().stream()
            .filter(skill -> skillCounter.get(skill) != 0)
            .map(skill -> String.format("%4s=%3d", skill, skillCounter.get(skill)))
            .collect(joining("; ")),
        cardTierCounter.values().stream()
            .filter(tier -> cardTierCounter.get(tier) != 0)
            .map(tier -> String.format("%6s=%3d", tier, cardTierCounter.get(tier)))
            .collect(joining("; ")));
  }

  public String getDescription() {
    return Arrays.stream(cards)
        .map(card -> card == null ? "___" : String.format("%03d", card.getIndex()))
        .collect(joining(","));
  }

  public String getLongDescription() {
    return IntStream.range(0, cards.length)
        .mapToObj(
            cardIndex -> String.format("%s: %s",
                moveSlotTypes[cardIndex],
                cards[cardIndex] == null ? "___" : cards[cardIndex].toString()))
        .collect(joining("\n"))
        + "\n\n"
        + (isSolved() ? "Solved" : "Unsolved");
  }

  public Card setStriking(int index, Card newCard) {
    if (index < 0 || index >= puzzle.getStrikingSlotCount() || (newCard != null && !newCard.getMoveType().isStriking())) {
      throw new IllegalArgumentException();
    }
    return set(index, newCard);
  }

  public Card setGrappling(int index, Card newCard) {
    if (index < 0 || index >= puzzle.getGrapplingSlotCount() || (newCard != null && newCard.getMoveType().isStriking())) {
      throw new IllegalArgumentException(String.format("Can't set %s to %s", newCard, index));
    }
    return set(puzzle.getStrikingSlotCount() + index, newCard);
  }

  // This method is performance-sensitive; it's the main workhorse that solvers
  // will use to iterate and explore the space.
  private Card set(int index, Card newCard) {
    Card oldCard = cards[index];
    cards[index] = newCard;

    if (oldCard != null) {
      if (!isUsed(oldCard)) {
        throw new IllegalArgumentException();
      }
      chemistry -= oldCard.getChemistryInSlot(puzzle, moveSlotTypes[index]);
      for (Skill skill : skillCounter.values()) {
        skillCounter.add(skill, -oldCard.getSkillModifier(skill));
      }
      cardTierCounter.add(oldCard.getTier(), -1);
      setUsed(oldCard,false);
    }

    if (newCard != null) {
      if (isUsed(newCard)) {
        throw new IllegalArgumentException();
      }
      chemistry += newCard.getChemistryInSlot(puzzle, moveSlotTypes[index]);
      for (Skill skill : skillCounter.values()) {
        skillCounter.add(skill, newCard.getSkillModifier(skill));
      }
      cardTierCounter.add(newCard.getTier(), 1);
      setUsed(newCard, true);
    }

    return oldCard;
  }

  public boolean isUsed(Card card) {
    return used.get(card.getIndex());
  }

  public void setUsed(Card card, boolean isUsed) {
    used.set(card.getIndex(), isUsed);
  }

  public Card getRandomUnused(List<Card> cards, Random random) {
    while (true) {
      Card card = cards.get(random.nextInt(cards.size()));
      if (!isUsed(card)) {
        return card;
      }
    }
  }

  public boolean isSolved() {
    for (Skill skill : Skill.values()) {
      if (!puzzle.getSkillConstraint(skill).isSatisfiedBy(
          skillCounter.get(skill))) {
        return false;
      }
    }
    for (Tier tier : Tier.values()) {
      if (!puzzle.getCardTierConstraint(tier).isSatisfiedBy(
          cardTierCounter.get(tier))) {
        return false;
      }
    }
    return chemistry >= puzzle.getMinimumChemistry();
  }

  public double getNaughtiness() {
    double naughtiness = 0;
    if (chemistry < puzzle.getMinimumChemistry()) {
      naughtiness += puzzle.getMinimumChemistry() - chemistry;
    }
    for (Skill skill : Skill.values()) {
      // TODO: use the negative values more flexibly.
      naughtiness += Math.max(0,
          puzzle.getSkillConstraint(skill).getSatisfactionDistance(
             skillCounter.get(skill)));
    }
    for (Tier tier : cardTierCounter.values()) {
      // TODO: use the negative values more flexibly.
      naughtiness += Math.max(0,
          puzzle.getCardTierConstraint(tier).getSatisfactionDistance(
              cardTierCounter.get(tier)));
    }
    return naughtiness;
  }
}
