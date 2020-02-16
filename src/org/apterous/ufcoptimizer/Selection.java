package org.apterous.ufcoptimizer;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

/** A mutable representation of the cards actually assigned. */
public class Selection {

  private final Puzzle puzzle;
  private final Card[] cards; // TODO: use two arrays.
  private final MoveType[] moveSlotTypes;

  private final BitSet used;
  private int chemistry;
  private final EnumCounter<Skill> skillCounter;
  private final EnumCounter<Tier> cardTierCounter;
  private final EnumCounter<Style> cardStyleCounter;

  public Selection(Puzzle puzzle) {
    this.puzzle = Preconditions.checkNotNull(puzzle);
    this.cards = new Card[puzzle.getMoveSlotCount()];
    // Order can be arbitrary as long as striking slots are first.
    this.moveSlotTypes =
        puzzle.getMoveSlots().stream()
            .sorted((slotA, slotB) -> Boolean.compare(slotB.isStriking(), slotA.isStriking()))
            .collect(toList())
            .toArray(new MoveType[0]);

    this.used = new BitSet(puzzle.getAvailableCards().size());
    this.chemistry = 0;
    this.skillCounter = new EnumCounter<>(Skill.values(), puzzle::getInitialSkill);
    this.cardTierCounter = new EnumCounter<>(Tier.values(), tier -> 0);
    this.cardStyleCounter = new EnumCounter<>(Style.values(), style -> 0);
  }

  public Selection(Selection selection) {
    puzzle = selection.puzzle;
    cards = selection.cards.clone();
    moveSlotTypes = selection.moveSlotTypes.clone();

    used = (BitSet) selection.used.clone();
    chemistry = selection.chemistry;
    skillCounter = new EnumCounter<>(selection.skillCounter);
    cardTierCounter = new EnumCounter<>(selection.cardTierCounter);
    cardStyleCounter = new EnumCounter<>(selection.cardStyleCounter);
  }

  @Override
  public String toString() {
    return
        ImmutableList.<String>builder()
            .add(String.format("CHEM=%2d", chemistry))
            .addAll(
                skillCounter.values().stream()
                    .filter(skill -> !puzzle.getSkillConstraint(skill).acceptsAnything())
                    .map(skill -> String.format("%4s=%3d", skill, skillCounter.get(skill)))
                    .collect(toList()))
            .addAll(
                cardTierCounter.values().stream()
                    .filter(tier -> !puzzle.getCardTierConstraint(tier).acceptsAnything())
                    .map(tier -> String.format("%6s=%2d", tier, cardTierCounter.get(tier)))
                    .collect(toList()))
            .addAll(
                cardStyleCounter.values().stream()
                    .filter(style -> !puzzle.getCardStyleConstraint(style).acceptsAnything())
                    .map(style -> String.format("%3s=%2d", style, cardStyleCounter.get(style)))
                    .collect(toList()))
            .build()
            .stream()
            .collect(joining("; "));
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
    Preconditions.checkArgument(index >= 0);
    Preconditions.checkArgument(index < puzzle.getStrikingSlotCount());
    Preconditions.checkArgument(newCard == null || newCard.getMoveType().isStriking());
    return set(index, newCard);
  }

  public Card setGrappling(int index, Card newCard) {
    Preconditions.checkArgument(index >= 0);
    Preconditions.checkArgument(index < puzzle.getGrapplingSlotCount());
    Preconditions.checkArgument(newCard == null || !newCard.getMoveType().isStriking());
    return set(puzzle.getStrikingSlotCount() + index, newCard);
  }

  // This method is performance-sensitive; it's the main workhorse that solvers
  // will use to iterate and explore the space.
  private Card set(int index, Card newCard) {
    Card oldCard = cards[index];
    cards[index] = newCard;

    if (oldCard != null) {
      Preconditions.checkArgument(isUsed(oldCard));
      chemistry -= oldCard.getChemistryInSlot(puzzle, moveSlotTypes[index]);
      for (Skill skill : skillCounter.values()) {
        skillCounter.add(skill, -oldCard.getSkillModifier(skill));
      }
      cardTierCounter.add(oldCard.getTier(), -1);
      cardStyleCounter.add(oldCard.getStyle(), -1);
      setUsed(oldCard,false);
    }

    if (newCard != null) {
      Preconditions.checkArgument(!isUsed(newCard));
      chemistry += newCard.getChemistryInSlot(puzzle, moveSlotTypes[index]);
      for (Skill skill : skillCounter.values()) {
        skillCounter.add(skill, newCard.getSkillModifier(skill));
      }
      cardTierCounter.add(newCard.getTier(), 1);
      cardStyleCounter.add(newCard.getStyle(), 1);
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
    for (Style style : cardStyleCounter.values()) {
      if (!puzzle.getCardStyleConstraint(style).isSatisfiedBy(
          cardStyleCounter.get(style))) {
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
    for (Style style : cardStyleCounter.values()) {
      // TODO: use the negative values more flexibly.
      naughtiness += Math.max(0,
          puzzle.getCardStyleConstraint(style).getSatisfactionDistance(
              cardStyleCounter.get(style)));
    }
    return naughtiness;
  }
}
