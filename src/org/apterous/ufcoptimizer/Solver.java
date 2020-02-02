package org.apterous.ufcoptimizer;

import com.google.common.collect.ImmutableList;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import static java.util.stream.Collectors.toList;

/** The core solving engine. */
final class Solver {

  /** Solves the puzzle, using stochastic gradient descent. */
  static Selection getBestSelection(ImmutableList<Card> cards, Random random) {
    // Split the cards into striking and grappling, and randomize them.
    List<Card> strikingCards =
        cards.stream().filter(card -> card.getMoveType().isStriking()).collect(toList());
    List<Card> grapplingCards =
        cards.stream().filter(card -> !card.getMoveType().isStriking()).collect(toList());
    Collections.shuffle(strikingCards, random);
    Collections.shuffle(grapplingCards, random);

    Selection selection = new Selection(cards);

    Selection bestEver = new Selection(selection);
    double lowestEverNaughtiness = bestEver.getNaughtiness();

    double oldNaughtiness = selection.getNaughtiness();
    for (int grind = 0; grind < 12_000 && !selection.isSolved(); ++grind) {
      int targetIndex = random.nextInt(Selection.TOTAL_SLOTS);

      Card newCard, oldCard;
      if (targetIndex < Selection.STRIKING_SLOTS) {
        newCard = selection.getRandomUnused(strikingCards, random);
        oldCard = selection.setStriking(targetIndex, newCard);
      } else {
        newCard = selection.getRandomUnused(grapplingCards, random);
        oldCard = selection.setGrappling(targetIndex - Selection.STRIKING_SLOTS, newCard);
      }

      double newNaughtiness = selection.getNaughtiness();
      boolean newLowestEver = newNaughtiness < lowestEverNaughtiness;
      boolean accept = accept(oldNaughtiness, newNaughtiness, random);

      if (newLowestEver) {
        System.out.printf("%08d: %8.5g [%s]    [%s] -> [%s]    [%s]%n",
            grind,
            newNaughtiness,
            selection,
            oldCard == null ? "                  " : oldCard,
            newCard == null ? "                  " : newCard,
            selection.getDescription());
      }

      if (accept) {
        oldNaughtiness = newNaughtiness;
        if (newNaughtiness < lowestEverNaughtiness) {
          lowestEverNaughtiness = newNaughtiness;
          bestEver = new Selection(selection);
        }
      } else {
        if (targetIndex < Selection.STRIKING_SLOTS) {
          selection.setStriking(targetIndex, oldCard);
        } else {
          selection.setGrappling(targetIndex - Selection.STRIKING_SLOTS, oldCard);
        }
      }
    }

    return bestEver;
  }

  /**
   * Whether to accept the most recent step.
   *
   * <p>Downhill steps are always allowed; uphill steps are allowed with some probability.
   */
  private static boolean accept(double oldNaughtiness, double newNaughtiness, Random random) {
    double worseness = newNaughtiness - oldNaughtiness;

    return (worseness < 0) ||
           (worseness <= 1 && random.nextDouble() < .01) ||
           (worseness <= 2 && random.nextDouble() < .005);
  }
}
