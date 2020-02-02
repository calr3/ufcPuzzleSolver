package org.apterous.ufcoptimizer;

public enum Style {
  BALANCED {
    @Override
    public String toString() {
      return "BAL";
    }
  }, STRIKER {
    @Override
    public String toString() {
      return "STR";
    }
  }, GRAPPLER {
    @Override
    public String toString() {
      return "GRA";
    }
  }, BRAWLER {
    @Override
    public String toString() {
      return "BRA";
    }
  }, SPECIALIST {
    @Override
    public String toString() {
      return "SPC";
    }
  };
}
