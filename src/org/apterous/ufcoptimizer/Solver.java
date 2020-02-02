package org.apterous.ufcoptimizer;

import java.util.Random;

/** The core solving engine. */
final class Solver {

  /** Solves the puzzle, using stochastic gradient descent. */
  static Selection getBestSelection(Puzzle puzzle, Random random) {
    Selection selection = new Selection(puzzle);

    Selection bestEver = new Selection(selection);
    double lowestEverNaughtiness = bestEver.getNaughtiness();

    double oldNaughtiness = selection.getNaughtiness();
    // TODO: puzzle hard-coding (12000).
    for (int grind = 0; grind < 1_000_000 && !selection.isSolved(); ++grind) {
      // Put a random card in a random slot.
      int targetIndex = random.nextInt(puzzle.getMoveSlotCount());
      Card newCard, oldCard;
      if (targetIndex < puzzle.getStrikingSlotCount()) {
        newCard = selection.getRandomUnused(puzzle.getStrikingCards(), random);
        oldCard = selection.setStriking(targetIndex, newCard);
      } else {
        newCard = selection.getRandomUnused(puzzle.getGrapplingCards(), random);
        oldCard = selection.setGrappling(
            targetIndex - puzzle.getStrikingSlotCount(), newCard);
    }

      // Evaluate the new fitness against the old one.
      double newNaughtiness = selection.getNaughtiness();
      boolean newLowestEver = newNaughtiness < lowestEverNaughtiness;
      boolean accept = accept(oldNaughtiness, newNaughtiness, random);

      // Print some progress info.
      if (newLowestEver) {
        System.out.printf("%08d: %8.5g [%s]    [%s] -> [%s]    [%s]%n",
            grind,
            newNaughtiness,
            selection,
            oldCard == null ? "                  " : oldCard,
            newCard == null ? "                  " : newCard,
            selection.getDescription());
      }

      // If we like the new state, stick with it. Otherwise roll it back.
      if (accept) {
        oldNaughtiness = newNaughtiness;
        if (newNaughtiness < lowestEverNaughtiness) {
          lowestEverNaughtiness = newNaughtiness;
          bestEver = new Selection(selection);
        }
      } else {
        if (targetIndex < puzzle.getStrikingSlotCount()) {
          selection.setStriking(targetIndex, oldCard);
        } else {
          selection.setGrappling(targetIndex - puzzle.getStrikingSlotCount(), oldCard);
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
  // TODO: puzzle hard-coding (might be too rigid).
  private static boolean accept(double oldNaughtiness, double newNaughtiness, Random random) {
    double worseness = newNaughtiness - oldNaughtiness;

    return (worseness < 0) ||
           (worseness <= 1 && random.nextDouble() < .01) ||
           (worseness <= 2 && random.nextDouble() < .005);
  }
}
