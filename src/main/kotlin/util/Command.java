package util;

public enum Command {
  START("!poker"),
  ROLL("!roll"),
  REROLL("!reroll"),
  PASS("!pass"),
  FINISH("!finish"),
  HELP("!help"),
  COMBINATION("!combo"),
  STATISTICS("!stat");

  public final String value;

  Command(String value) {
    this.value = value;
  }
}
