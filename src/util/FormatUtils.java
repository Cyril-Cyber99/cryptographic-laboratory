package util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Utilitaires pour le formatage scientifique et institutionnel des durées, débits et tailles mémoire.
 */
public final class FormatUtils {

    private static final DecimalFormat DF_2 = new DecimalFormat("0.00", DecimalFormatSymbols.getInstance(Locale.US));
    private static final DecimalFormat DF_4 = new DecimalFormat("0.0000", DecimalFormatSymbols.getInstance(Locale.US));

    private FormatUtils() {
    }

    /**
     * Formate une durée exprimée en nanosecondes dans l'unité la plus lisible (ns, µs, ms, s).
     */
    public static String formatDurationNs(long nanos) {
        if (nanos < 1_000) {
            return nanos + " ns";
        } else if (nanos < 1_000_000) {
            double us = nanos / 1_000.0;
            return DF_2.format(us) + " µs";
        } else if (nanos < 1_000_000_000) {
            double ms = nanos / 1_000_000.0;
            return DF_2.format(ms) + " ms";
        } else {
            double s = nanos / 1_000_000_000.0;
            return DF_4.format(s) + " s";
        }
    }

    /**
     * Formate une taille en octets en format lisible (o, Ko, Mo).
     */
    public static String formatSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " octets";
        } else if (bytes < 1024 * 1024) {
            double kb = bytes / 1024.0;
            return DF_2.format(kb) + " Ko";
        } else {
            double mb = bytes / (1024.0 * 1024.0);
            return DF_2.format(mb) + " Mo";
        }
    }

    /**
     * Formate un débit exprimé en Mo/s (Mégaoctets par seconde).
     */
    public static String formatThroughput(double mbPerSec) {
        return DF_2.format(mbPerSec) + " Mo/s";
    }

    /**
     * Formate un nombre décimal à 2 chiffres après la virgule.
     */
    public static String formatDecimal(double value) {
        return DF_2.format(value);
    }
}
