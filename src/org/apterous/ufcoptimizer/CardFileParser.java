package org.apterous.ufcoptimizer;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.MoreCollectors.onlyElement;

/** Parse the file at a given path into a list of cards. */
final class CardFileParser {

  private final Path filePath;

  /** Construct a new instance to parse the given path. */
  CardFileParser(Path filePath) {
    this.filePath = Preconditions.checkNotNull(filePath);
  }

  /**
   * Returns the list of cards in the order they occur in the file.
   *
   * <p>Indices are contiguous starting from 0.
   */
  ImmutableList<Card> load() throws IOException {
    AtomicInteger index = new AtomicInteger(0);
    try (Stream<String> lines = Files.lines(filePath)) {
      return lines
          .skip(1)
          .map(line -> parseCard(index.incrementAndGet(), line))
          .collect(toImmutableList());
    }
  }

  private Card parseCard(int index, String line) {
    String[] parts = line.split(",");
    return new Card(
        index,
        parseWeight(parts[1]),
        parseStyle(parts[2]),
        parseType(parts[3]),
        ImmutableMap.of(
            Skill.HVMT, parseSkill(parts[5]),
            Skill.THRW, parseSkill(parts[6])),
        parseTier(parts[7])
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

  private static Tier parseTier(String raw) {
    return Arrays.stream(Tier.values())
        .filter(tier -> tier.name().charAt(0) == raw.charAt(0))
        .collect(onlyElement());
  }
}
