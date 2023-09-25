package util;

import static bot.farm.pd.util.Combination.FULL_HOUSE;
import static bot.farm.pd.util.Combination.LARGE_STRAIGHT;
import static bot.farm.pd.util.Combination.PAIR;
import static bot.farm.pd.util.Combination.POKER;
import static bot.farm.pd.util.Combination.SET;
import static bot.farm.pd.util.Combination.SMALL_STRAIGHT;
import static bot.farm.pd.util.Combination.SQUARE;
import static bot.farm.pd.util.Combination.TWO_PAIR;
import static bot.farm.pd.util.Command.FINISH;
import static bot.farm.pd.util.Command.PASS;
import static bot.farm.pd.util.Command.REROLL;
import static bot.farm.pd.util.Command.ROLL;
import static bot.farm.pd.util.Command.START;

public enum MessageEnum {
  TABLE_BUSY("Извините, игровой стол сейчас занят"),
  START_ROUND("%s начинает новый раунд покера с костями!"),
  ACTIVITY_FOR_GAME("за игрой"),
  ACTIVITY_CLEANING_TABLE("уборку игрового стола"),
  FINISH_ROUND("%s досрочно завершает раунд, результаты будут аннулированы"),
  TIME_EXPIRED("Время раунда подошло к концу"),
  HELP("Для игры вам понадобится:\n" +
      "**0.** Знать выигрышные комбинации. Подсмотреть подсказку можно командой **"
      + Command.COMBINATION.value + "**.\n" +
      "**1.** Ввести команду **" + START.value + "**.\n" +
      "**2.** После ввода комнды бот объявит о начале раунда. Время раунда ограничено 5 минутами.\n" +
      "**3.** Игроки могут присоединяться к раунду командой **" + ROLL.value + "**, пока остались незавершенные действия у участников раунда.\n" +
      "**4.** Во время раунда каждый игрок должен совершить минимум два действия:\n" +
      "\tа) бросить кости с помощью команды **" + ROLL.value + "** (обязательное действие)\n" +
      "\tб) перебросить выбранные кости командой **" + REROLL.value + "** с указанием кубиков через пробел(ы) (например, " + REROLL.value + " 1 2  4)\n" +
      "\tв) пропустить этап переброски, если вас все устраивает, можно командой **" + PASS.value + "**\n" +
      "**5.** Раунд завершается после того, как все игроки выполнят свои действия с кубиками.\n" +
      "**6.** Раунд автоматически завершается по времени, если игроки не закончили свои действия. Для таких игроков будет проведен автоматический пропуск хода.\n" +
      "**7.** В случае форс-мажора (рожает кошка, игрок афк, *'нет сил приятель, душа болит, быть может пуля их повторит...'*) игрок, " +
      "запустивший раунд, может досрочно его прекратить командой **" + FINISH.value + "**. Если игрок-инициатор и есть то самое слабое " +
      "звено в ваший партии, вы можете потратить оставшееся время раунда на проклятия в его сторону.\n" +
      "**Примечание.** В одном текстовом канале одномоментно может быть запущена только одна игра. Дождитесь окончания текущей партии или перейдите " +
      "в другой текстовый канал для запуска раунда."),

  COMBINATION("Комбинации:\n" +
      POKER.value + " - 5 одинаковых костей.\n" +
      SQUARE.value + " - 4 кости выпали одной и той же стороной.\n" +
      FULL_HOUSE.value + " - две кости одного, три кости другого номинала.\n" +
      LARGE_STRAIGHT.value + " - все 5 костей идут по порядку возрастания начиная с 2\n" +
      SMALL_STRAIGHT.value + " - все 5 костей идут по порядку возрастания начиная с 1\n" +
      SET.value + " - 3 кости с одинаковыми номиналами.\n" +
      TWO_PAIR.value + " - две кости одного номинала и еще две кости другого.\n" +
      PAIR.value + " - две кости ложатся одной и той же стороной.\n" +
      "**Примечание.** В случае, когда у игроков одинаковые комбинации, например 2 покера, победителем считается тот, " +
      "у кого комбинация в сумме дает больше очков.\n");

  public final String value;

  MessageEnum(String value) {
    this.value = value;
  }
}
