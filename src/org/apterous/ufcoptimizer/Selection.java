package org.apterous.ufcoptimizer;

import com.google.common.base.Preconditions;

import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/** A mutable representation of the cards actually assigned. */
public class Selection {

  private final Puzzle puzzle;
  private final Card[] cards; // TODO: use two arrays.
  private final MoveType[] moveSlotTypes;

  private final BitSet used;
  private int chemistry;
  private EnumCounter<Skill> skillCounter;
  private int silvers;

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
    this.silvers = 0;
  }

  public Selection(Selection selection) {
    puzzle = selection.puzzle;
    cards = selection.cards.clone();
    moveSlotTypes = selection.moveSlotTypes.clone();

    used = (BitSet) selection.used.clone();
    chemistry = selection.chemistry;
    skillCounter = new EnumCounter<>(selection.skillCounter);
    silvers = selection.silvers;
}

  @Override
  public String toString() {
    return String.format("Chem=%2d; %s; Silv=%d",
        chemistry,
        skillCounter.values().stream()
            .filter(skill -> skillCounter.get(skill) != 0)
            .map(skill -> String.format("%4s=%3d", skill, skillCounter.get(skill)))
            .collect(Collectors.joining("; ")),
        silvers);
  }

  public String getDescription() {
    return Arrays.stream(cards)
        .map(card -> card == null ? "___" : String.format("%03d", card.getIndex()))
        .collect(Collectors.joining(","));
  }

  public String getLongDescription() {
    return IntStream.range(0, cards.length)
        .mapToObj(
            cardIndex -> String.format("%s: %s",
                moveSlotTypes[cardIndex],
                cards[cardIndex] == null ? "___" : cards[cardIndex].toString()))
        .collect(Collectors.joining("\n"))
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
      silvers -= oldCard.getLevel().equals(Level.SILVER) ? 1 : 0;
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
      silvers += newCard.getLevel().equals(Level.SILVER) ? 1 : 0;
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
    return chemistry >= puzzle.getMinimumChemistry() &&
        silvers <= puzzle.getMaximumSilvers();
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
    if (silvers > puzzle.getMaximumSilvers()) {
      naughtiness += (silvers - puzzle.getMaximumSilvers());
    }
    return naughtiness;
  }
}
