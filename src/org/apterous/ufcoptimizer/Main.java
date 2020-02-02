package org.apterous.ufcoptimizer;

import com.google.common.collect.ImmutableList;

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
public class Main {

  public static void main(String[] args) throws IOException {
    ImmutableList<Card> cards =
        new CardFileParser(FileSystems.getDefault().getPath(args[0])).load();

    Selection bestSelection = Solver.getBestSelection(cards, new Random(12989));

    System.out.println(bestSelection);
    System.out.println(bestSelection.getDescription());
    System.out.println(bestSelection.getNaughtiness());
    System.out.println(bestSelection.getLongDescription());
  }
}