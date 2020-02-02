package org.apterous.ufcoptimizer;

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
