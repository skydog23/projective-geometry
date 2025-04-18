package charlesgunn.math;

import java.util.ArrayList;
import java.util.List;

public class ContinuedFraction {

    /**
     * Computes the first n integers of the continued fraction expansion of a/b.
     *
     * @param a The numerator.
     * @param b The denominator.
     * @param n The number of terms to compute.
     * @return A list of the first n integers of the continued fraction.
     */
    public static List<Integer> cf(double a, double b, int n) {
        if (b == 0) {
            return new ArrayList<>(); // Handle division by zero
        }

        List<Integer> result = new ArrayList<>();
        double quotient = a / b;

        for (int i = 0; i < n; i++) {
            int integerPart = (int) quotient;
            result.add(integerPart);

            double remainder = quotient - integerPart;
            if (remainder == 0) {
                break; // Stop if the remainder is zero
            }

            a = b;
            b = 1.0 / remainder;
            quotient = a / b;
        }

        return result;
    }

    public static void main(String[] args) {
        double a = 22.0;
        double b = 7.0;
        int n = 5;

        List<Integer> continuedFraction = cf(a, b, n);
        System.out.println("Continued fraction of " + a + "/" + b + ": " + continuedFraction);

        a = 123.0;
        b = 47.0;
        n = 10;
        continuedFraction = cf(a, b, n);
        System.out.println("Continued fraction of " + a + "/" + b + ": " + continuedFraction);

        a = 1.41421356; //approx sqrt(2)
        b = 1.0;
        n = 5;
        continuedFraction = cf(a, b, n);
        System.out.println("Continued fraction of " + a + "/" + b + ": " + continuedFraction);
    }
}