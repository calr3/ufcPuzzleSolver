package org.apterous.ufcoptimizer;

/** Represents the slot that a move is associated with.
 *
 * <p>Note that moves can be assigned to slots of a different type (at the
 * cost of some chemistry). The only hard constraint is that striking moves
 * must be assigned to striking slots, and likewise for non-striking (i.e.
 * grappling).
 */
public enum MoveType {
  ARM {
    @Override
    public String toString() {
      return "ARM";
    }
  }, LEG {
    @Override
    public String toString() {
      return "LEG";
    }
  }, CLINCH {
    @Override
    public String toString() {
      return "CLN";
    }
  }, TAKEDOWN {
    @Override
    public String toString() {
      return "TKD";
    }
  }, SUBMISSION {
    @Override
    public String toString() {
      return "SUB";
    }
  }, GROUND {
    @Override
    public String toString() {
      return "GRD";
    }
  };

  public boolean isStriking() {
    return this == ARM || this == LEG;
  }
}
