package util;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class StringUtil {
  public static String getIdFromBrackets(String userId) {
    return userId.replaceAll("<@|>", " ").trim().replaceAll("\\s+", " ");
  }

  public static String diamondWrapperForId(Long userId) {
    return "<@" + userId + ">";
  }

  public static Set<String> getPlayersId(String command) {

    return Arrays.stream(command.substring(command.indexOf(" "))
            .replaceAll("<@|>", " ")
            .trim()
            .replaceAll("\\s+", " ")
            .split(" "))
        .collect(Collectors.toSet());
  }

  public static int[] getRerollNumbers(String command) {
    return Arrays.stream(command.substring(command.indexOf(" "))
            .trim()
            .replaceAll("\\s+", " ")
            .split(" "))
        .mapToInt(Integer::valueOf)
        .toArray();
  }

  public static String resultWithBrackets(int[] array) {
    return "[" + Arrays.stream(array).mapToObj(String::valueOf).collect(Collectors.joining("] [")) + "]";
  }
}
