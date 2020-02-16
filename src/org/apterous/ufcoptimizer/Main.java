package org.apterous.ufcoptimizer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
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

    ImmutableList<Puzzle> puzzles = ImmutableList.of(
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
            ImmutableMap.of(
                Skill.HVMT, RangeConstraint.min( 100),
                Skill.THRW, RangeConstraint.min(95)),
            ImmutableMap.of(
                Tier.SILVER, RangeConstraint.max(1)),
            ImmutableMap.of(),
            ImmutableMap.of(Skill.HVMT, 75, Skill.THRW, 74)),
        // Pack: UFC 200: Normal. Puzzle 1: Miesha Tate: Normal.
        new Puzzle(
            cards,
            Weight.BW,
            Style.BRAWLER,
            ImmutableMultiset.<MoveType>builder()
                .addCopies(MoveType.ARM, 5)
                .addCopies(MoveType.LEG, 4)
                .addCopies(MoveType.CLINCH, 2)
                .addCopies(MoveType.TAKEDOWN, 2)
                .addCopies(MoveType.SUBMISSION, 1)
                .addCopies(MoveType.GROUND, 1)
                .build(),
            25,
            ImmutableMap.of(),
            ImmutableMap.of(),
            ImmutableMap.of(
                Style.SPECIALIST, RangeConstraint.min(2)),
            ImmutableMap.<Skill, Integer>builder()
                .put(Skill.SPD, 77)
                .put(Skill.PWR, 88)
                .put(Skill.FWRK, 78)
                .put(Skill.ACC, 72)
                .put(Skill.SWCH, 77)
                .put(Skill.BLOK, 79)
                .put(Skill.HVMT, 81)
                .put(Skill.THRW, 71)
                .put(Skill.CCON, 74)
                .put(Skill.TOP, 69)
                .put(Skill.BOT, 69)
                .put(Skill.TD, 71)
                .put(Skill.TDD, 71)
                .put(Skill.SUBO, 72)
                .put(Skill.SUBD, 69)
                .build()));

    puzzles.stream()
        .map(puzzle ->
            new Solver(new Solver.SolverConfig(1_000_000), puzzle)
                .getBestSelection(new Random(129189)))
        .forEach(solution -> {
          System.out.println(solution);
          System.out.println(solution.getDescription());
          System.out.println(solution.getNaughtiness());
          System.out.println(solution.getLongDescription());
        });
  }

  private Main() {}  // Not for instantiation.
}