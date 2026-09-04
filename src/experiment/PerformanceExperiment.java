package experiment;

import crypto.AesCbcService;
import crypto.AesGcmService;
import crypto.CryptoKeyManager;
import crypto.NonceManager;
import util.BenchmarkUtils;

import javax.crypto.SecretKey;
import java.util.ArrayList;
import java.util.List;

/**
 * Moteur expérimental de benchmark scientifique comparant AES-CBC et AES-GCM.
 *
 * Protocole expérimental rigoureux :
 * 1. Warm-up (échauffement JIT préalable sans enregistrement des mesures).
 * 2. Génération de tampons aléatoires de tailles définies : 1 Ko, 10 Ko, 100 Ko, 1 Mo, 5 Mo, 10 Mo.
 * 3. Répétition de N mesures par opération.
 * 4. Mesure exclusive du temps cryptographique avec System.nanoTime() (aucun artefact Swing).
 * 5. Extraction statistique : Moyenne, Minimum, Maximum, Médiane, Écart-type, Débit (Mo/s).
 */
public class PerformanceExperiment {

    public static final int[] STANDARD_SIZES = {
            1 * 1024,          // 1 KB
            10 * 1024,         // 10 KB
            100 * 1024,        // 100 KB
            1024 * 1024,       // 1 MB
            5 * 1024 * 1024,   // 5 MB
            10 * 1024 * 1024   // 10 MB
    };

    public static class OperationMetrics {
        public final String mode;
        public final String operation; // "ENCRYPT" ou "DECRYPT"
        public final int sizeBytes;
        public final int repetitions;
        public final double meanNanos;
        public final long minNanos;
        public final long maxNanos;
        public final double medianNanos;
        public final double stdDevNanos;
        public final double throughputMBs;

        public OperationMetrics(String mode, String operation, int sizeBytes, int repetitions,
                                double meanNanos, long minNanos, long maxNanos,
                                double medianNanos, double stdDevNanos, double throughputMBs) {
            this.mode = mode;
            this.operation = operation;
            this.sizeBytes = sizeBytes;
            this.repetitions = repetitions;
            this.meanNanos = meanNanos;
            this.minNanos = minNanos;
            this.maxNanos = maxNanos;
            this.medianNanos = medianNanos;
            this.stdDevNanos = stdDevNanos;
            this.throughputMBs = throughputMBs;
        }
    }

    public static class BenchmarkSuiteResult {
        public final List<OperationMetrics> cbcEncryptMetrics = new ArrayList<>();
        public final List<OperationMetrics> cbcDecryptMetrics = new ArrayList<>();
        public final List<OperationMetrics> gcmEncryptMetrics = new ArrayList<>();
        public final List<OperationMetrics> gcmDecryptMetrics = new ArrayList<>();
        public final List<ExperimentResult> experimentResults = new ArrayList<>();
        public final String executionSummary;

        public BenchmarkSuiteResult(String executionSummary) {
            this.executionSummary = executionSummary;
        }
    }

    public interface BenchmarkProgressListener {
        void onProgress(String currentTask, int percent);
    }

    /**
     * Exécute la suite complète de benchmark avec warm-up et tailles calibrées.
     */
    public BenchmarkSuiteResult runBenchmark(int[] sizes, int repetitions, int keySizeBits, BenchmarkProgressListener listener) {
        if (sizes == null || sizes.length == 0) {
            sizes = STANDARD_SIZES;
        }
        if (repetitions <= 0) {
            repetitions = 5;
        }

        AesCbcService cbc = new AesCbcService();
        AesGcmService gcm = new AesGcmService();
        SecretKey key = CryptoKeyManager.generateKey(keySizeBits);

        // 1. Phase de Warm-Up JIT (50 itérations sur 64 Ko pour déclencher la compilation native JIT C2)
        if (listener != null) listener.onProgress("Phase d'échauffement JIT (Warm-up)...", 5);
        byte[] warmUpData = NonceManager.generateRandomBytes(64 * 1024);
        byte[] cbcIv = NonceManager.generateCbcIv();
        byte[] gcmNonce = NonceManager.generateGcmNonce();

        for (int w = 0; w < 30; w++) {
            var rCbc = cbc.encrypt(warmUpData, key, cbcIv);
            cbc.decrypt(rCbc.getCiphertext(), key, cbcIv);
            var rGcm = gcm.encrypt(warmUpData, key, gcmNonce, null);
            gcm.decrypt(rGcm.getCiphertext(), rGcm.getTag(), key, gcmNonce, null);
        }

        BenchmarkSuiteResult suiteResult = new BenchmarkSuiteResult("Benchmark terminé avec succès.");
        int totalSteps = sizes.length * 4;
        int currentStep = 0;

        for (int size : sizes) {
            byte[] data = NonceManager.generateRandomBytes(size);

            // --- CBC ENCRYPT ---
            currentStep++;
            if (listener != null) listener.onProgress("CBC Chiffrement (" + (size / 1024) + " Ko)...", (currentStep * 90) / totalSteps);
            long[] cbcEncTimes = new long[repetitions];
            byte[] lastCbcCipher = null;
            byte[] lastCbcIv = null;
            for (int r = 0; r < repetitions; r++) {
                byte[] iv = NonceManager.generateCbcIv();
                long start = System.nanoTime();
                var res = cbc.encrypt(data, key, iv);
                cbcEncTimes[r] = System.nanoTime() - start;
                lastCbcCipher = res.getCiphertext();
                lastCbcIv = iv;
            }
            OperationMetrics cbcEncMetric = buildMetrics("CBC", "ENCRYPT", size, repetitions, cbcEncTimes);
            suiteResult.cbcEncryptMetrics.add(cbcEncMetric);

            // --- CBC DECRYPT ---
            currentStep++;
            if (listener != null) listener.onProgress("CBC Déchiffrement (" + (size / 1024) + " Ko)...", (currentStep * 90) / totalSteps);
            long[] cbcDecTimes = new long[repetitions];
            for (int r = 0; r < repetitions; r++) {
                long start = System.nanoTime();
                cbc.decrypt(lastCbcCipher, key, lastCbcIv);
                cbcDecTimes[r] = System.nanoTime() - start;
            }
            OperationMetrics cbcDecMetric = buildMetrics("CBC", "DECRYPT", size, repetitions, cbcDecTimes);
            suiteResult.cbcDecryptMetrics.add(cbcDecMetric);

            // --- GCM ENCRYPT ---
            currentStep++;
            if (listener != null) listener.onProgress("GCM Chiffrement (" + (size / 1024) + " Ko)...", (currentStep * 90) / totalSteps);
            long[] gcmEncTimes = new long[repetitions];
            byte[] lastGcmCipher = null;
            byte[] lastGcmTag = null;
            byte[] lastGcmNonce = null;
            for (int r = 0; r < repetitions; r++) {
                byte[] nonce = NonceManager.generateGcmNonce();
                long start = System.nanoTime();
                var res = gcm.encrypt(data, key, nonce, null);
                gcmEncTimes[r] = System.nanoTime() - start;
                lastGcmCipher = res.getCiphertext();
                lastGcmTag = res.getTag();
                lastGcmNonce = nonce;
            }
            OperationMetrics gcmEncMetric = buildMetrics("GCM", "ENCRYPT", size, repetitions, gcmEncTimes);
            suiteResult.gcmEncryptMetrics.add(gcmEncMetric);

            // --- GCM DECRYPT ---
            currentStep++;
            if (listener != null) listener.onProgress("GCM Déchiffrement (" + (size / 1024) + " Ko)...", (currentStep * 90) / totalSteps);
            long[] gcmDecTimes = new long[repetitions];
            for (int r = 0; r < repetitions; r++) {
                long start = System.nanoTime();
                gcm.decrypt(lastGcmCipher, lastGcmTag, key, lastGcmNonce, null);
                gcmDecTimes[r] = System.nanoTime() - start;
            }
            OperationMetrics gcmDecMetric = buildMetrics("GCM", "DECRYPT", size, repetitions, gcmDecTimes);
            suiteResult.gcmDecryptMetrics.add(gcmDecMetric);

            // Enregistrement des fiches expérimentales
            suiteResult.experimentResults.add(new ExperimentResult(
                    "BM-CBC-" + (size / 1024) + "K", "AES", "CBC", size, keySizeBits, 16, 0,
                    (long) cbcEncMetric.meanNanos, (long) cbcDecMetric.meanNanos, cbcEncMetric.throughputMBs,
                    "NOT_APPLICABLE", "BENCHMARK", "SUCCESS",
                    "Benchmark CBC " + (size / 1024) + " Ko",
                    "Mesure du débit CBC", "Performances dépendantes du chaînage séquentiel",
                    "Mesure moyenne sur " + repetitions + " répétitions",
                    "Dans cet environnement expérimental, AES-CBC a produit un débit moyen de " +
                            String.format("%.2f", cbcEncMetric.throughputMBs) + " Mo/s sur une taille de " + (size / 1024) + " Ko."
            ));

            suiteResult.experimentResults.add(new ExperimentResult(
                    "BM-GCM-" + (size / 1024) + "K", "AES", "GCM", size, keySizeBits, 12, 0,
                    (long) gcmEncMetric.meanNanos, (long) gcmDecMetric.meanNanos, gcmEncMetric.throughputMBs,
                    "VALID", "BENCHMARK", "SUCCESS",
                    "Benchmark GCM " + (size / 1024) + " Ko",
                    "Mesure du débit GCM", "Performances dépendantes des instructions d'accélération matérielle (AES-NI / CLMUL)",
                    "Mesure moyenne sur " + repetitions + " répétitions",
                    "Dans cet environnement expérimental, AES-GCM a produit un débit moyen de " +
                            String.format("%.2f", gcmEncMetric.throughputMBs) + " Mo/s sur une taille de " + (size / 1024) + " Ko."
            ));
        }

        if (listener != null) listener.onProgress("Benchmark achevé.", 100);
        return suiteResult;
    }

    private OperationMetrics buildMetrics(String mode, String op, int size, int reps, long[] times) {
        double mean = BenchmarkUtils.computeMean(times);
        long min = BenchmarkUtils.computeMin(times);
        long max = BenchmarkUtils.computeMax(times);
        double median = BenchmarkUtils.computeMedian(times);
        double stdDev = BenchmarkUtils.computeStdDev(times, mean);
        double throughput = BenchmarkUtils.computeThroughputMBs(size, mean);
        return new OperationMetrics(mode, op, size, reps, mean, min, max, median, stdDev, throughput);
    }
}
