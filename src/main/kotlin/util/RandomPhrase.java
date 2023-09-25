package util;

import java.util.ArrayList;
import java.util.List;
import lombok.experimental.UtilityClass;

@UtilityClass
public class RandomPhrase {
  private static final List<String> autoPass = new ArrayList<>();
  private static final List<String> rollDices = new ArrayList<>();
  private static final List<String> rerollDices = new ArrayList<>();
  private static final List<String> pass = new ArrayList<>();

  static {
    autoPass.add("Угадайте, что снится %s? Автоматический пропуск хода! Принесите ему(ей) одеяло");
    autoPass.add("%s больше не наливать! Штрафной автоматический пропуск хода! И заберите у него(нее) бутылку");
    autoPass.add("%s ушел(шла) в себя! Что ж, это автоматический пропуск хода! Доиграем без него(нее)!");
    autoPass.add("Кто-нибудь видел %s? Негоже заставлять других ждать. Автоматический пропуск хода!");
    autoPass.add("Сейчас бы не доигрывать раунды, да, %s? Автоматический пропуск хода!");
    autoPass.add(" По~~матросил~~ролил(а) и бросил(а) %s? Не надо так. Автоматический пропуск хода!");

    rollDices.add("%s ловко бросает кости %s");
    rollDices.add("%s сегодня, определенно, везет %s");
    rollDices.add("%s hehe, boy %s");
    rollDices.add("%s совершает героический бросок костей %s");
    rollDices.add("Поглядите на %s, до чего же хорош его(ее) бросок %s");
    rollDices.add("Ведите летописца, %s выбросил легендарные %s");

    rerollDices.add("%s с надеждой перебрасывает кости %s\nПолучилось %s");
    rerollDices.add("%s дует на кости %s перед перебросом\nПолучилось %s");
    rerollDices.add("%s недовольно смотрит на %s\nПолучилось %s");
    rerollDices.add("%s мечтает, чтобы вместо %s были другие кости\nПолучилось %s");
    rerollDices.add("%s тратит остатки удачи на %s\nПолучилось %s");

    pass.add("%s с ухмылкой пропускает ход");
    pass.add("%s загадочно потирает руки");
    pass.add("%s держит покерфейс");
    pass.add("%s наверняка рассчитывает на победу!");
    pass.add("%s что-то задумал(а)");
    pass.add("%s чешет левую пятку, перебрасывать не будет");
  }

  public static String getAutoPassPhrase() {
    int index = DiceUtil.generateRandomInteger(0, autoPass.size() - 1);
    return autoPass.get(index);
  }

  public static String getRollDicesPhrase() {
    int index = DiceUtil.generateRandomInteger(0, rollDices.size() - 1);
    return rollDices.get(index);
  }

  public static String getPassPhrase() {
    int index = DiceUtil.generateRandomInteger(0, pass.size() - 1);
    return pass.get(index);
  }

  public static String getRerollPhrase() {
    int index = DiceUtil.generateRandomInteger(0, rerollDices.size() - 1);
    return rerollDices.get(index);
  }
}
