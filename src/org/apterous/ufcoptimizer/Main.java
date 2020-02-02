package org.apterous.ufcoptimizer;

import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.stream.Collectors.toList;

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

  private static boolean accept(double oldNaughtiness, double newNaughtiness, Random random) {
    if (newNaughtiness < oldNaughtiness) {
      return true;
    }

    double worseness = newNaughtiness - oldNaughtiness;
    if (worseness <= 1) {
      return random.nextDouble() < .01;
    } else if (worseness <= 2) {
      return random.nextDouble() < .005;
    }

    return false;
  }

  public static void main(String[] args) throws IOException {
    ImmutableList<Card> cards;
    try (Stream<String> lines = Files.lines(FileSystems.getDefault().getPath("C:\\Users\\Charlie\\Downloads\\ufc cards - Sheet1 (3).csv"))) {
      cards = lines.skip(1).map(Main::parseCard).collect(toImmutableList());
    }
    List<Card> strikingCards = cards.stream().filter(card -> card.getMoveType().isStriking()).collect(toList());
    List<Card> grapplingCards = cards.stream().filter(card -> !card.getMoveType().isStriking()).collect(toList());

    Random random = new Random(12989);
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
        System.out.printf("%08d: %8.5g [%s] %s [%s] -> [%s]    [%s]%n",
            grind,
            newNaughtiness,
            selection,
            newLowestEver ? "**" : accept ? " *" : "  ",
            oldCard == null ? "                  " : oldCard,
            newCard == null ? "                  " : newCard,
            selection.getDescription());
      }

      if (accept) {
        oldNaughtiness = newNaughtiness;
//        newCard.setUsed(true);
//        if (oldCard != null) {
//          oldCard.setUsed(false);
//        }

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

    System.out.println(bestEver);
    System.out.println(bestEver.getDescription());
    System.out.println(bestEver.getNaughtiness());
    System.out.println(bestEver.getLongDescription());
  }

  private static int INDEX = 0;

  private static Card parseCard(String line) {
    String[] parts = line.split(",");
    return new Card(
        INDEX++,
        parseWeight(parts[1]),
        parseStyle(parts[2]),
        parseType(parts[3]),

        parseSkill(parts[5]),
        parseSkill(parts[6]),
        parseLevel(parts[7])
   );
  }

  private static Weight parseWeight(String raw) {
    if (raw.equals("LW")) {
      return Weight.LW;
    } else if (raw.equals("MW")) {
      return Weight.MW;
    } else if (raw.equals("HW")) {
      return Weight.HW;
    } else if (raw.equals("BW")) {
      return Weight.BW;
    }

    throw new IllegalArgumentException("Bad weight " + raw);
  }

  private static MoveType parseType(String raw) {
    for (MoveType moveType : MoveType.values()) {
      if (moveType.name().equalsIgnoreCase(raw)) {
        return moveType;
      }
    }

    throw new IllegalArgumentException("Bad type " + raw);
  }

  private static Style parseStyle(String raw) {
    if (raw.equals("Bal")) {
      return Style.BALANCED;
    } else if (raw.equals("Gra")) {
      return Style.GRAPPLER;
    } else if (raw.equals("Bra")) {
      return Style.BRAWLER;
    } else if (raw.equals("SPC")) {
      return Style.SPECIALIST;
    } else if (raw.equals("Str")) {
      return Style.STRIKER;
    }

    throw new IllegalArgumentException("Bad style " + raw);
  }

  private static int parseSkill(String raw) {
    return raw.isEmpty() ? 0 : Integer.parseInt(raw);
  }

  private static Level parseLevel(String raw) {
    return Arrays.stream(Level.values())
        .filter(level -> level.name().charAt(0) == raw.charAt(0))
        .findAny()
        .orElseThrow();
  }
}