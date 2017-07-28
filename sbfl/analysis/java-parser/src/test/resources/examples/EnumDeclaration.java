public enum EnumDeclaration {
  /**
   * OFF
   */
  OFF,

  /**
   * ON
   */
  ON;

  public boolean atLeast(
      int a) {
    if (a < 
        50) {
      return true;
    }
    return false;
  }
}