package org.apterous.ufcoptimizer;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Streams;

import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

/** A mutable representation of the cards actually assigned. */
public class Selection {

  // Map from summary skill to the skills that roll up to them. Generally the value of
  // a summary skill is the average, rounded down, of the composite skills. However
  // CHEMISTRY is a special case.
  private final ImmutableListMultimap<SummarySkill, Skill> SKILLS_BY_SUMMARY =
      ImmutableListMultimap.<SummarySkill, Skill>builder()
          .putAll(
              SummarySkill.STRIKING,
              Skill.SPD, Skill.PWR, Skill.FWRK, Skill.ACC, Skill.SWCH, Skill.BLOK, Skill.HVMT)
          .putAll(
              SummarySkill.GRAPPLING,
              Skill.THRW, Skill.CCON, Skill.TOP, Skill.BOT, Skill.TD, Skill.TDD, Skill.SUBO, Skill.SUBD)
          .putAll(
              SummarySkill.STAMINA,
              Skill.GSTA, Skill.SSTA, Skill.END)
          .putAll(
              SummarySkill.HEALTH,
              Skill.TGH, Skill.HART, Skill.CHIN, Skill.BODY, Skill.LEGS)
          .build();

  private final Puzzle puzzle;
  private final MoveCard[] cards; // TODO: use two arrays.
  private final BoostCard[] boostCards;
  private final MoveType[] moveSlotTypes;

  private final BitSet used;
  private int chemistry;
  private final EnumCounter<Skill> skillCounter;
  private final EnumCounter<Tier> cardTierCounter;
  private final EnumCounter<Style> cardStyleCounter;

  public Selection(Puzzle puzzle) {
    this.puzzle = Preconditions.checkNotNull(puzzle);
    this.cards = new MoveCard[puzzle.getMoveSlotCount()];
    this.boostCards = new BoostCard[puzzle.getBoostSlotCount()];
    // Order can be arbitrary as long as striking slots are first.
    this.moveSlotTypes =
        puzzle.getMoveSlots().stream()
            .sorted((slotA, slotB) -> Boolean.compare(slotB.isStriking(), slotA.isStriking()))
            .collect(toList())
            .toArray(new MoveType[0]);

    this.used = new BitSet(puzzle.getCardCount());
    this.chemistry = 0;
    this.skillCounter = new EnumCounter<>(Skill.values(), puzzle::getInitialSkill);
    this.cardTierCounter = new EnumCounter<>(Tier.values(), tier -> 0);
    this.cardStyleCounter = new EnumCounter<>(Style.values(), style -> 0);
  }

  public Selection(Selection selection) {
    puzzle = selection.puzzle;
    cards = selection.cards.clone();
    boostCards = selection.boostCards.clone();
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
            .addAll(
                Arrays.stream(SummarySkill.values())
                    .filter(skill -> !puzzle.getSummarySkillConstraint(skill).acceptsAnything())
                    .map(skill -> String.format("%9s=%3d", skill, getSummarySkillValue(skill)))
                    .collect(toList()))
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
    return Streams.concat(Arrays.stream(cards), Arrays.stream(boostCards))
        .map(card -> card == null ? "___" : String.format("%03d", card.getIndex()))
        .collect(joining(","));
  }

  public String getLongDescription() {
    return Streams.concat(
          IntStream.range(0, cards.length)
              .mapToObj(
                  cardIndex -> String.format("%s: %s",
                      moveSlotTypes[cardIndex],
                      cards[cardIndex] == null ? "___" : cards[cardIndex].toString())),
          Arrays.stream(boostCards)
              .map(card -> String.format("BST: %s", card == null ? "___" : card.toString())))
        .collect(joining("\n"))
        + "\n\n"
        + (isSolved() ? "Solved" : "Unsolved");
  }

  public MoveCard setStriking(int index, MoveCard newCard) {
    Preconditions.checkArgument(index >= 0);
    Preconditions.checkArgument(index < puzzle.getStrikingSlotCount());
    Preconditions.checkArgument(newCard == null || newCard.getMoveType().isStriking());
    return set(index, newCard);
  }

  public MoveCard setGrappling(int index, MoveCard newCard) {
    Preconditions.checkArgument(index >= 0);
    Preconditions.checkArgument(index < puzzle.getGrapplingSlotCount());
    Preconditions.checkArgument(newCard == null || !newCard.getMoveType().isStriking());
    return set(puzzle.getStrikingSlotCount() + index, newCard);
  }

  public BoostCard setBoost(int index, BoostCard newCard) {
    Preconditions.checkArgument(index >= 0);
    Preconditions.checkArgument(index < puzzle.getBoostSlotCount());

    BoostCard oldCard = boostCards[index];
    set(index, newCard, boostCards);
    return oldCard;
  }

  // This method is performance-sensitive; it's the main workhorse that solvers
  // will use to iterate and explore the space.
  private MoveCard set(int index, MoveCard newCard) {
    MoveCard oldCard = cards[index];
    set(index, newCard, cards);

    if (oldCard != null) {
      chemistry -= oldCard.getChemistryInSlot(puzzle, moveSlotTypes[index]);
      cardTierCounter.add(oldCard.getTier(), -1);
      cardStyleCounter.add(oldCard.getStyle(), -1);
    }

    if (newCard != null) {
      chemistry += newCard.getChemistryInSlot(puzzle, moveSlotTypes[index]);
      cardTierCounter.add(newCard.getTier(), 1);
      cardStyleCounter.add(newCard.getStyle(), 1);
    }

    return oldCard;
  }

  // Insert the given card (which may be null) into the given card array,
  // updating counters and state accordingly. Note that this has the
  // side effect of marking the new card as used (if non-null) and any
  // previous card in the same position as unused. (If the new and old
  // cards are actually the same card, the card is still marked as used.)
  private void set(int index, Card newCard, Card[] cards) {
    Card oldCard = cards[index];
    cards[index] = newCard;

    if (oldCard != null) {
      Preconditions.checkArgument(isUsed(oldCard));
      for (Skill skill : skillCounter.values()) {
        skillCounter.add(skill, -oldCard.getSkillModifier(skill));
      }
      cardTierCounter.add(oldCard.getTier(), -1);
      setUsed(oldCard,false);
    }

    if (newCard != null) {
      Preconditions.checkArgument(!isUsed(newCard));
      for (Skill skill : skillCounter.values()) {
        skillCounter.add(skill, newCard.getSkillModifier(skill));
      }
      cardTierCounter.add(newCard.getTier(), 1);
      setUsed(newCard, true);
    }
  }

  public boolean isUsed(Card card) {
    return used.get(card.getIndex());
  }

  public void setUsed(Card card, boolean isUsed) {
    used.set(card.getIndex(), isUsed);
  }

  public <CardT extends Card> CardT getRandomUnused(List<? extends CardT> cards, Random random) {
    while (true) {
      CardT card = cards.get(random.nextInt(cards.size()));
      if (!isUsed(card)) {
        return card;
      }
    }
  }

  public int getSummarySkillValue(SummarySkill summarySkill) {
    // Chemistry is a special case that is not derived from other attributes.
    if (summarySkill.equals(SummarySkill.CHEMISTRY)) {
      return chemistry;
    }

    ImmutableList<Skill> relevantSkills = SKILLS_BY_SUMMARY.get(summarySkill);
    int sum = 0;
    for (Skill skill : relevantSkills) {
      sum += skillCounter.get(skill);
    }
    return sum / relevantSkills.size();
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
    for (SummarySkill summarySkill : SummarySkill.values()) {
      RangeConstraint constraint = puzzle.getSummarySkillConstraint(summarySkill);
      // These constraints are sparse so we try to avoid computing the value
      // unless it's necessary.
      if (!constraint.acceptsAnything() &&
          !constraint.isSatisfiedBy(getSummarySkillValue(summarySkill))) {
        return false;
      }
    }
    return true;
  }

  public double getNaughtiness() {
    double naughtiness = 0;
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
    for (SummarySkill summarySkill : SummarySkill.values()) {
      RangeConstraint constraint = puzzle.getSummarySkillConstraint(summarySkill);
      // These constraints are sparse so we try to avoid computing the value
      // unless it's necessary.
      if (!constraint.acceptsAnything()) {
        naughtiness += Math.max(0,
            constraint.getSatisfactionDistance(getSummarySkillValue(summarySkill)));
      }
    }
    return naughtiness;
  }
}
