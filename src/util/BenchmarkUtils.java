package util;

import java.util.Arrays;

/**
 * Utilitaires pour le calcul des grandeurs statistiques issues des mesures de benchmark.
 * Garantit un traitement rigoureux sans approximation empirique infondée.
 */
public final class BenchmarkUtils {

    private BenchmarkUtils() {
    }

    /**
     * Calcule la moyenne arithmétique d'une série de valeurs en nanosecondes.
     */
    public static double computeMean(long[] values) {
        if (values == null || values.length == 0) {
            return 0.0;
        }
        long sum = 0;
        for (long v : values) {
            sum += v;
        }
        return (double) sum / values.length;
    }

    /**
     * Calcule la valeur minimale.
     */
    public static long computeMin(long[] values) {
        if (values == null || values.length == 0) {
            return 0;
        }
        long min = values[0];
        for (long v : values) {
            if (v < min) {
                min = v;
            }
        }
        return min;
    }

    /**
     * Calcule la valeur maximale.
     */
    public static long computeMax(long[] values) {
        if (values == null || values.length == 0) {
            return 0;
        }
        long max = values[0];
        for (long v : values) {
            if (v > max) {
                max = v;
            }
        }
        return max;
    }

    /**
     * Calcule la médiane d'une série d'échantillons.
     */
    public static double computeMedian(long[] values) {
        if (values == null || values.length == 0) {
            return 0.0;
        }
        long[] copy = Arrays.copyOf(values, values.length);
        Arrays.sort(copy);
        int mid = copy.length / 2;
        if (copy.length % 2 == 0) {
            return (copy[mid - 1] + copy[mid]) / 2.0;
        } else {
            return copy[mid];
        }
    }

    /**
     * Calcule l'écart-type (dispersion par rapport à la moyenne).
     */
    public static double computeStdDev(long[] values, double mean) {
        if (values == null || values.length <= 1) {
            return 0.0;
        }
        double sumSq = 0.0;
        for (long v : values) {
            double diff = v - mean;
            sumSq += diff * diff;
        }
        return Math.sqrt(sumSq / (values.length - 1));
    }

    /**
     * Calcule le débit effectif en Mégaoctets par seconde (Mo/s) à partir de la taille et de la durée moyenne.
     * Débit (Mo/s) = (sizeBytes / (1024 * 1024)) / (durationNanos / 1_000_000_000.0)
     */
    public static double computeThroughputMBs(int sizeBytes, double durationNanos) {
        if (durationNanos <= 0 || sizeBytes <= 0) {
            return 0.0;
        }
        double sizeMB = (double) sizeBytes / (1024.0 * 1024.0);
        double durationSeconds = durationNanos / 1_000_000_000.0;
        return sizeMB / durationSeconds;
    }
}
