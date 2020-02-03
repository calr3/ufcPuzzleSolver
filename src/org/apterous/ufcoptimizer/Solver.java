package org.apterous.ufcoptimizer;

import javax.annotation.concurrent.Immutable;
import java.util.Random;

import static com.google.common.base.Preconditions.checkNotNull;

/** The core solving engine. */
@Immutable
final class Solver {

  @Immutable
  static final class SolverConfig {

    private final int maximumIterations;

    SolverConfig(int maximumIterations) {
      this.maximumIterations = maximumIterations;
    }
  }

  private final SolverConfig solverConfig;
  private final Puzzle puzzle;

  Solver(SolverConfig solverConfig, Puzzle puzzle) {
    this.solverConfig = checkNotNull(solverConfig);
    this.puzzle = checkNotNull(puzzle);
  }

  /** Solves the puzzle, using stochastic gradient descent. */
  Selection getBestSelection(Random random) {
    Selection selection = new Selection(puzzle);

    Selection bestEver = new Selection(selection);
    double lowestEverNaughtiness = bestEver.getNaughtiness();

    double oldNaughtiness = selection.getNaughtiness();
    for (int grind = 0; grind < solverConfig.maximumIterations && !selection.isSolved(); ++grind) {
      // Put a random card in a random slot.
      // TODO: support selecting null as newCard.
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
        System.out.printf("%08d: %8.5g [%s]    [%-24s] -> [%-24s]    [%s]%n",
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
