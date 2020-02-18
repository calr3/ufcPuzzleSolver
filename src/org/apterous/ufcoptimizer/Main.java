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
    CardFileParser.Cards cards =
        new CardFileParser(
                FileSystems.getDefault().getPath(args[0]),
                FileSystems.getDefault().getPath(args[1]))
            .load();

    // Descriptive constants.
    ImmutableMap<Skill, RangeConstraint> NO_SKILL_CONSTRAINTS = ImmutableMap.of();
    ImmutableMap<Tier, RangeConstraint> NO_TIER_CONSTRAINTS = ImmutableMap.of();
    ImmutableMap<Style, RangeConstraint> NO_STYLE_CONSTRAINTS = ImmutableMap.of();

    ImmutableList<Puzzle> puzzles = ImmutableList.of(
        new Puzzle(
            cards.availableMoves,
            cards.availableBoosts,
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
            6,
            ImmutableMap.of(
                Skill.HVMT, RangeConstraint.min( 100),
                Skill.THRW, RangeConstraint.min(95)),
            ImmutableMap.of(SummarySkill.CHEMISTRY, RangeConstraint.min(75)),
            ImmutableMap.of(
                Tier.SILVER, RangeConstraint.max(1)),
            NO_STYLE_CONSTRAINTS,
            ImmutableMap.<Skill, Integer>builder()
                .put(Skill.SPD, 69)
                .put(Skill.PWR, 69)
                .put(Skill.FWRK, 73)
                .put(Skill.ACC, 69)
                .put(Skill.SWCH, 69)
                .put(Skill.BLOK, 69)
                .put(Skill.HVMT, 75)
                .put(Skill.THRW, 74)
                .put(Skill.CCON, 79)
                .put(Skill.TOP, 79)
                .put(Skill.BOT, 77)
                .put(Skill.TD, 79)
                .put(Skill.TDD, 79)
                .put(Skill.SUBO, 77)
                .put(Skill.SUBD, 75)
                .put(Skill.GSTA, 79)
                .put(Skill.SSTA, 69)
                .put(Skill.END, 79)
                .put(Skill.TGH, 77)
                .put(Skill.HART, 77)
                .put(Skill.CHIN, 75)
                .put(Skill.BODY, 71)
                .put(Skill.LEGS, 71)
                .build()),
        // Pack: UFC 200: Normal. Puzzle 1: Miesha Tate: Normal.
        new Puzzle(
            cards.availableMoves,
            cards.availableBoosts,
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
            6,
            NO_SKILL_CONSTRAINTS,
            ImmutableMap.of(SummarySkill.CHEMISTRY, RangeConstraint.min(25)),
            NO_TIER_CONSTRAINTS,
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
                .put(Skill.GSTA, 71)
                .put(Skill.SSTA, 77)
                .put(Skill.END, 73)
                .put(Skill.TGH, 79)
                .put(Skill.HART, 80)
                .put(Skill.CHIN, 80)
                .put(Skill.BODY, 79)
                .put(Skill.LEGS, 79)
                .build()),
        // Pack: UFC 200: Normal. Puzzle 2: Amanda Nunes: Normal.
        new Puzzle(
            cards.availableMoves,
            cards.availableBoosts,
            Weight.BW,
            Style.GRAPPLER,
            ImmutableMultiset.<MoveType>builder()
                .addCopies(MoveType.ARM, 3)
                .addCopies(MoveType.LEG, 1)
                .addCopies(MoveType.CLINCH, 3)
                .addCopies(MoveType.TAKEDOWN, 4)
                .addCopies(MoveType.SUBMISSION, 2)
                .addCopies(MoveType.GROUND, 2)
                .build(),
            6,
            ImmutableMap.of(Skill.TGH, RangeConstraint.min(90)),
            ImmutableMap.of(SummarySkill.CHEMISTRY, RangeConstraint.min(25)),
            NO_TIER_CONSTRAINTS,
            NO_STYLE_CONSTRAINTS,
            ImmutableMap.<Skill, Integer>builder()
                .put(Skill.SPD, 69)
                .put(Skill.PWR, 69)
                .put(Skill.FWRK, 73)
                .put(Skill.ACC, 69)
                .put(Skill.SWCH, 69)
                .put(Skill.BLOK, 69)
                .put(Skill.HVMT, 69)
                .put(Skill.THRW, 79)
                .put(Skill.CCON, 79)
                .put(Skill.TOP, 79)
                .put(Skill.BOT, 77)
                .put(Skill.TD, 79)
                .put(Skill.TDD, 79)
                .put(Skill.SUBO, 77)
                .put(Skill.SUBD, 75)
                .put(Skill.GSTA, 79)
                .put(Skill.SSTA, 69)
                .put(Skill.END, 79)
                .put(Skill.TGH, 77)
                .put(Skill.HART, 77)
                .put(Skill.CHIN, 75)
                .put(Skill.BODY, 71)
                .put(Skill.LEGS, 71)
                .build()),
        // Pack: UFC 200: Hard. Puzzle 8: Travis Browne: Hard.
        new Puzzle(
            cards.availableMoves,
            cards.availableBoosts,
            Weight.HW,
            Style.BRAWLER,
            ImmutableMultiset.<MoveType>builder()
                .addCopies(MoveType.ARM, 5)
                .addCopies(MoveType.LEG, 4)
                .addCopies(MoveType.CLINCH, 2)
                .addCopies(MoveType.TAKEDOWN, 2)
                .addCopies(MoveType.SUBMISSION, 1)
                .addCopies(MoveType.GROUND, 1)
                .build(),
            6,
            NO_SKILL_CONSTRAINTS,
            ImmutableMap.of(
                SummarySkill.CHEMISTRY, RangeConstraint.min(50),
                SummarySkill.STAMINA, RangeConstraint.min(80)),
            NO_TIER_CONSTRAINTS,
            ImmutableMap.of(
                Style.GRAPPLER, RangeConstraint.min(2)),
            ImmutableMap.<Skill, Integer>builder()
                .put(Skill.SPD, 73)
                .put(Skill.PWR, 80)
                .put(Skill.FWRK, 77)
                .put(Skill.ACC, 73)
                .put(Skill.SWCH, 77)
                .put(Skill.BLOK, 77)
                .put(Skill.HVMT, 77)
                .put(Skill.THRW, 71)
                .put(Skill.CCON, 71)
                .put(Skill.TOP, 69)
                .put(Skill.BOT, 69)
                .put(Skill.TD, 71)
                .put(Skill.TDD, 71)
                .put(Skill.SUBO, 69)
                .put(Skill.SUBD, 69)
                .put(Skill.GSTA, 71)
                .put(Skill.SSTA, 77)
                .put(Skill.END, 73)
                .put(Skill.TGH, 79)
                .put(Skill.HART, 80)
                .put(Skill.CHIN, 80)
                .put(Skill.BODY, 79)
                .put(Skill.LEGS, 79)
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