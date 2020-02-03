package org.apterous.ufcoptimizer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultiset;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.Random;

/**
 * The entry point for the application.
 *
 * <p>This application solves the Challenge puzzles in EA's game UFC 3 for
 * PlayStation 4 and XBox One.
 *
 * <p>The puzzle it solves is currently hard-coded. This will be improved in a
 * future version.
 *
 * <p>The cards used are loaded from a file, in a rather clunky CSV format.
 * This will be improved in a future version.
 */
public final class Main {

  public static void main(String[] args) throws IOException {
    ImmutableList<Card> cards =
        new CardFileParser(FileSystems.getDefault().getPath(args[0])).load();

    Puzzle puzzle =
        new Puzzle(
            cards,
            Weight.BW,
            Style.BALANCED,
            ImmutableMultiset.<MoveType>builder()
                .addCopies(MoveType.ARM, 3)
                .addCopies(MoveType.LEG, 3)
                .addCopies(MoveType.CLINCH, 2)
                .addCopies(MoveType.TAKEDOWN, 2)
                .addCopies(MoveType.SUBMISSION, 2)
                .addCopies(MoveType.GROUND, 2)
                .build(),
            75,
            100,
            95,
            1,
            75,
            74);

    Selection bestSelection = Solver.getBestSelection(puzzle, new Random(129189));

    System.out.println(bestSelection);
    System.out.println(bestSelection.getDescription());
    System.out.println(bestSelection.getNaughtiness());
    System.out.println(bestSelection.getLongDescription());
  }

  private Main() {}  // Not for instantiation.
}