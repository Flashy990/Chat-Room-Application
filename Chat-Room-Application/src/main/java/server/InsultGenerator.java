package server;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Generates random insults from a predefined list.
 */
public class InsultGenerator {
    private List<String> insults;
    private Random random;

    /**
     * Constructs an InsultGenerator with a predefined list of insults.
     */
    public InsultGenerator() {
        insults = Arrays.asList(
                "You ignorant buffoon.",
                "You half-witted dunce.",
                "You obtuse knave.",
                "You pompous fool.",
                "You daft imbecile."
        );
        random = new Random();
    }

    /**
     * Generates a random insult.
     *
     * @return A random insult string.
     */
    public String generateInsult() {
        int index = random.nextInt(insults.size());
        return insults.get(index);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InsultGenerator that = (InsultGenerator) o;

        return Objects.equals(insults, that.insults);
    }

    @Override
    public int hashCode() {
        return insults != null ? insults.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "InsultGenerator{" +
                "insults=" + insults +
                '}';
    }
}