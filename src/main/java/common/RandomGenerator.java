package common;

import java.util.Random;

public class RandomGenerator {
    /**
     * Genera un número entero aleatorio entre 0 y el valor 2.
     *
     * @return número entero entre 0 y 2.
     */
    public static int generateNumber() {
        Random random = new Random();

        return random.nextInt(3);
    }
}
