package common;

import java.util.Random;

public class RandomGenerator {
    public static int generateNumber() {
        Random random = new Random();

        return random.nextInt(3);
    }
}
