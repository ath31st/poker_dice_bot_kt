package util;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DiceUtil {
  public static int generateRandomInteger(int min, int max) {
    SecureRandom random = new SecureRandom();
    random.setSeed(new Date().getTime());
    return random.nextInt((max - min) + 1) + min;
  }

  public static int[] roll5d6() {
    int[] arr = new int[5];
    for (int i = 0; i < arr.length; i++) {
      arr[i] = generateRandomInteger(1, 6);
    }
    Arrays.sort(arr);
    return arr;
  }

  public static void reroll(int[] firstRoll, int[] reroll) {
    Arrays.sort(reroll);
    int point = 0;
    for (int k : reroll) {
      for (int j = point; j < firstRoll.length; j++) {
        point++;
        if (firstRoll[j] == k) {
          firstRoll[j] = generateRandomInteger(1, 6);
          break;
        }
      }
    }
    Arrays.sort(firstRoll);
  }

  public static boolean isPoker(int[] dices) {
    int[] temp = new int[5];
    Arrays.fill(temp, dices[0]);
    return Arrays.equals(temp, dices);
  }

  public static boolean isFullHouse(int[] dices) {
    Map<Integer, Long> map = Arrays.stream(dices).boxed().collect(Collectors.groupingBy(x -> x, Collectors.counting()));
    return map.size() == 2 && map.get(dices[0]) > 1 && map.get(dices[dices.length - 1]) > 1;
  }

  public static boolean isLargeStraight(int[] dices) {
    int[] largeStraight = new int[]{2, 3, 4, 5, 6};
    return Arrays.equals(dices, largeStraight);
  }

  public static boolean isSmallStraight(int[] dices) {
    int[] smallStraight = new int[]{1, 2, 3, 4, 5};
    return Arrays.equals(dices, smallStraight);
  }

  public static boolean isSequence(int[] dices, int seq) {
    int maxCount = 0;
    int count = 1;
    for (int i = 1; i < dices.length; i++) {
      if (dices[i - 1] == dices[i]) {
        count++;
        if (maxCount < count) maxCount = count;
      } else {
        count = 1;
      }
    }
    return maxCount == seq;
  }

  public static int sequenceScore(int[] dices, int seq) {
    int repeateNumber = 0;
    int maxCount = 0;
    int count = 1;
    for (int i = 1; i < dices.length; i++) {
      if (dices[i - 1] == dices[i]) {
        count++;
        if (maxCount < count) {
          repeateNumber = dices[i];
          maxCount = count;
        }
      } else {
        count = 1;
      }
    }
    return repeateNumber * seq;
  }

  public static boolean isTwoPair(int[] dices) {
    int[] counts = new int[6];
    int pairs = 0;
    for (int dice : dices) {
      counts[dice - 1]++;
    }
    for (int value : counts) {
      if (value == 2) {
        pairs++;
      }
    }
    return pairs == 2;
  }

  public static int scoreTwoPair(int[] dices) {
    Map<Integer, Long> map = Arrays.stream(dices).boxed().collect(Collectors.groupingBy(x -> x, Collectors.counting()));
    return map.entrySet()
        .stream()
        .filter(x -> x.getValue() > 1)
        .flatMapToInt(x -> IntStream.of(x.getKey()))
        .sum() * 2;
  }

  public static int customComparator(RoundResult r1, RoundResult r2) {
    if (r1.getPriority() < r2.getPriority()) {
      return 1;
    } else if (r1.getPriority() == r2.getPriority()) {
      return Integer.compare(r2.getScore(), r1.getScore());
    } else {
      return -1;
    }
  }

}
