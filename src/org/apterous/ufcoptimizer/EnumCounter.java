package org.apterous.ufcoptimizer;

import com.google.common.collect.ImmutableList;

import java.util.Arrays;
import java.util.function.ToIntFunction;

/** A convenient way of mapping the values of an enum to a counter.
 *
 * This is more efficient than a standard Map or EnumMap by avoiding
 * boxing--unboxing overhead, and also caches the values of the enum
 * to avoid repeated calls to {@code .values()}.
 */
final class EnumCounter<E extends Enum<E>> {

  private final ImmutableList<E> values;
  private final int[] counters;

  /**
   * Construct a new counter with the given enum values (which are
   * assumed to be the complete list of values for the enum). Initial
   * values of each counter are determined by the initializer function.
   */
  EnumCounter(E[] enumClass, ToIntFunction<E> initializer) {
    values = ImmutableList.copyOf(enumClass);
    counters = Arrays.stream(enumClass).mapToInt(initializer).toArray();
  }

  /**
   * Constructs a copy of the given {@link EnumCounter}.
   */
  EnumCounter(EnumCounter<E> copyFrom) {
    values = copyFrom.values;
    counters = copyFrom.counters.clone();
  }

  /** Get the values of the enum. May be more efficient than calling {@code .values()}. */
  ImmutableList<E> values() {
    return values;
  }

  /** Get the values of the enum. May be more efficient than calling {@code .values().length}. */
  int size() {
    return counters.length;
  }

  /** Get the value of the counter at the given value. */
  int get(E value) {
    return counters[value.ordinal()];
  }

  /** Increment the value of the counter by the given amount. */
  void add(E value, int addend) {
    counters[value.ordinal()] += addend;
  }
}
