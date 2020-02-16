package org.apterous.ufcoptimizer;

import com.google.common.base.Preconditions;

import java.util.OptionalInt;

/**
 * Immutable description of a range of acceptable integers. May include an upper and/or lower bound.
 */
class RangeConstraint {

  /**
   * Convenience constant for the {@link RangeConstraint} that accepts anything.
   */
  public static final RangeConstraint UNCONSTRAINED =
      new RangeConstraint(OptionalInt.empty(), OptionalInt.empty());

  private final OptionalInt min;
  private final OptionalInt max;

  /**
   * Constructs a new {@link RangeConstraint}. The static constructors are generally more
   * convenient.
   */
  public RangeConstraint(OptionalInt min, OptionalInt max) {
    this.min = Preconditions.checkNotNull(min);
    this.max = Preconditions.checkNotNull(max);
  }

  /**
   * Static constructor for {@link RangeConstraint} that only enforces a minimum.
   */
  static RangeConstraint min(int value) {
    return new RangeConstraint(OptionalInt.of(value), OptionalInt.empty());
  }

  /**
   * Static constructor for {@link RangeConstraint} that only enforces a maximum.
   */
  static RangeConstraint max(int value) {
    return new RangeConstraint(OptionalInt.empty(), OptionalInt.of(value));
  }

  /**
   * Static constructor for {@link RangeConstraint} that enforces an upper and lower bound.
   */
  static RangeConstraint of(int min, int max) {
    return new RangeConstraint(OptionalInt.of(min), OptionalInt.of(max));
  }

  /**
   * Whether the given value passes the constraints.
   */
  boolean isSatisfiedBy(int value) {
    return (!min.isPresent() || value >= min.getAsInt()) &&
        (!max.isPresent() || value <= max.getAsInt());
  }

  /**
   * How close is the given value to passing the constraints. Essentially this measures how far the given value is from
   * the range defined by the constraints.
   *
   * <ul>
   * <li>Positive values indicate failing the constraint. The larger the value, the "worse" the
   * failure.
   * <li>Zero indicates passing the constraint but right at the limit.
   * <li>Negative values indicate passing the constraint. The larger the value, the more comfortably
   * the value is inside the range.
   * </ul>
   */
  int getSatisfactionDistance(int value) {
    if (min.isPresent()) {
      if (max.isPresent()) {
        return Math.max(min.getAsInt() - value, value - max.getAsInt());
      } else {
        return min.getAsInt() - value;
      }
    } else {
      if (max.isPresent()) {
        return value - max.getAsInt();
      } else {
        return 0;
      }
    }
  }
}
