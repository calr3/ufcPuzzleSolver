package org.apterous.ufcoptimizer;

import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static org.apterous.ufcoptimizer.MoveType.ARM;
import static org.apterous.ufcoptimizer.MoveType.CLINCH;
import static org.apterous.ufcoptimizer.MoveType.GROUND;
import static org.apterous.ufcoptimizer.MoveType.LEG;
import static org.apterous.ufcoptimizer.MoveType.SUBMISSION;
import static org.apterous.ufcoptimizer.MoveType.TAKEDOWN;

public class Selection {

  private static final int TARGET_CHEMISTRY = 75;
  private static final int TARGET_HEAD_MOVEMENT = 100;
  private static final int TARGET_THROW_SKILL = 95;
  private static final int TARGET_SILVERS = 1;

  private static final MoveType[] STYLE_TARGETS = {
      ARM, ARM, ARM,
      LEG, LEG, LEG,
      CLINCH, CLINCH,
      TAKEDOWN, TAKEDOWN,
      SUBMISSION, SUBMISSION,
      GROUND, GROUND,
  };
  public static final int TOTAL_SLOTS = STYLE_TARGETS.length;
  public static final int STRIKING_SLOTS =
      (int) Arrays.stream(STYLE_TARGETS).filter(MoveType::isStriking).count();
  public static final int GRAPPLING_SLOTS = TOTAL_SLOTS - STRIKING_SLOTS;

  private final Card[] cards;
  private final BitSet used;

  private int chemistry = 0;
  private int headMovement = 75;  // Default value. Ceck
  private int throwSkill = 74;  // Default value
  private int silvers = 0;

  public Selection(List<Card> availableCards) {
    cards = new Card[STYLE_TARGETS.length];
    used = new BitSet(availableCards.size());
  }

  public Selection(Selection selection) {
    cards = selection.cards.clone();
    used = (BitSet) selection.used.clone();
    chemistry = selection.chemistry;
    headMovement = selection.headMovement;
    throwSkill = selection.throwSkill;
    silvers = selection.silvers;
  }

  public void verifyStartingState() {
    if (chemistry != 76) {
      throw new IllegalArgumentException("nope 1");
    }
    if (throwSkill != 95) {
      throw new IllegalArgumentException("nope 2: " + throwSkill);
    }
    if (headMovement != 100) {
      throw new IllegalArgumentException("nope 3");
    }
    if (silvers != 2) {
      throw new IllegalArgumentException("nope 4");
    }
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
    return Arrays.stream(cards)
        .map(card -> card == null ? "___" : card.toString())
        .collect(Collectors.joining("\n"))
        + "\n\n"
        + (isSolved() ? "Solved" : "Unsolved");
  }

  public Card setStriking(int index, Card newCard) {
    if (index < 0 || index >= STRIKING_SLOTS || (newCard != null && !newCard.getMoveType().isStriking())) {
      throw new IllegalArgumentException();
    }
    return set(index, newCard);
  }

  public Card setGrappling(int index, Card newCard) {
    if (index < 0 || index >= GRAPPLING_SLOTS || (newCard != null && newCard.getMoveType().isStriking())) {
      throw new IllegalArgumentException(String.format("Can't set %s to %s", newCard, index));
    }
    return set(STRIKING_SLOTS + index, newCard);
  }

  private Card set(int index, Card newCard) {
    Card oldCard = cards[index];
    cards[index] = newCard;

    if (oldCard != null) {
      if (!isUsed(oldCard)) {
        throw new IllegalArgumentException();
      }
      chemistry -= oldCard.getChemistryAt(STYLE_TARGETS[index]);
      headMovement -= oldCard.getHeadMovement();
      throwSkill -= oldCard.getThrowSkill();
      silvers -= oldCard.getLevel().equals(Level.SILVER) ? 1 : 0;
      setUsed(oldCard,false);
    }

    if (newCard != null) {
      if (isUsed(newCard)) {
        throw new IllegalArgumentException();
      }
      chemistry += newCard.getChemistryAt(STYLE_TARGETS[index]);
      headMovement += newCard.getHeadMovement();
      throwSkill += newCard.getThrowSkill();
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
    return chemistry >= TARGET_CHEMISTRY &&
        headMovement >= TARGET_HEAD_MOVEMENT &&
        throwSkill >= TARGET_THROW_SKILL &&
        silvers <= TARGET_SILVERS;
  }

  public double getNaughtiness() {
    double naughtiness = 0;
    if (chemistry < TARGET_CHEMISTRY) {
      naughtiness += TARGET_CHEMISTRY - chemistry;
    }
    if (headMovement < TARGET_HEAD_MOVEMENT) {
      naughtiness += TARGET_HEAD_MOVEMENT - headMovement;
    }
    if (throwSkill < TARGET_THROW_SKILL) {
      naughtiness += TARGET_THROW_SKILL - throwSkill;
    }
    if (silvers > TARGET_SILVERS) {
      naughtiness += 3 * (silvers - TARGET_SILVERS);
    }
    return naughtiness;
  }
}
