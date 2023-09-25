package util;

public enum Combination {
  POKER("Покер", 9),
  SQUARE("Каре", 8),
  FULL_HOUSE("Фулл-хаус", 7),
  LARGE_STRAIGHT("Большой стрейт", 6),
  SMALL_STRAIGHT("Малый стрейт", 5),
  SET("Сет", 4),
  TWO_PAIR("Две пары", 3),
  PAIR("Пара", 2),
  NOTHING("Ничего", 1);

  public final String value;
  public final int priority;

  Combination(String value, int priority) {
    this.value = value;
    this.priority = priority;
  }
}
