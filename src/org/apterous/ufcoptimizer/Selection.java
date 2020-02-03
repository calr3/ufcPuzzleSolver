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
  private int headMovement;
  private int throwSkill;
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
    this.headMovement = puzzle.getInitialSkill(Skill.HVMT);
    this.throwSkill = puzzle.getInitialSkill(Skill.THRW);
    this.silvers = 0;
  }

  public Selection(Selection selection) {
    puzzle = selection.puzzle;
    cards = selection.cards.clone();
    moveSlotTypes = selection.moveSlotTypes.clone();

    used = (BitSet) selection.used.clone();
    chemistry = selection.chemistry;
    headMovement = selection.headMovement;
    throwSkill = selection.throwSkill;
    silvers = selection.silvers;
}

  @Override
  public String toString() {
    return String.format("Chem=%2d; Hvmt=%3d; Thrw=%3d; Silv=%d",
        chemistry, headMovement, throwSkill, silvers);
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

  private Card set(int index, Card newCard) {
    Card oldCard = cards[index];
    cards[index] = newCard;

    if (oldCard != null) {
      if (!isUsed(oldCard)) {
        throw new IllegalArgumentException();
      }
      chemistry -= oldCard.getChemistryInSlot(puzzle, moveSlotTypes[index]);
      headMovement -= oldCard.getSkillModifier(Skill.HVMT);
      throwSkill -= oldCard.getSkillModifier(Skill.THRW);
      silvers -= oldCard.getLevel().equals(Level.SILVER) ? 1 : 0;
      setUsed(oldCard,false);
    }

    if (newCard != null) {
      if (isUsed(newCard)) {
        throw new IllegalArgumentException();
      }
      chemistry += newCard.getChemistryInSlot(puzzle, moveSlotTypes[index]);
      headMovement += newCard.getSkillModifier(Skill.HVMT);
      throwSkill += newCard.getSkillModifier(Skill.THRW);
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
    return chemistry >= puzzle.getMinimumChemistry() &&
        headMovement >= puzzle.getMinimumHeadMovement() &&
        throwSkill >= puzzle.getMinimumThrowSkill() &&
        silvers <= puzzle.getMaximumSilvers();
  }

  public double getNaughtiness() {
    double naughtiness = 0;
    if (chemistry < puzzle.getMinimumChemistry()) {
      naughtiness += puzzle.getMinimumChemistry() - chemistry;
    }
    if (headMovement < puzzle.getMinimumHeadMovement()) {
      naughtiness += puzzle.getMinimumHeadMovement() - headMovement;
    }
    if (throwSkill < puzzle.getMinimumThrowSkill()) {
      naughtiness += puzzle.getMinimumThrowSkill() - throwSkill;
    }
    if (silvers > puzzle.getMaximumSilvers()) {
      naughtiness += (silvers - puzzle.getMaximumSilvers());
    }
    return naughtiness;
  }
}
