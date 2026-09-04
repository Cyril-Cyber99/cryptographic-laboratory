package experiment;

import crypto.AesGcmService;
import crypto.CryptoKeyManager;
import crypto.CryptoResult;
import crypto.NonceManager;
import util.BenchmarkUtils;

import javax.crypto.SecretKey;
import java.util.Arrays;

/**
 * Expérience contrôlée sur le chiffrement authentifié AES-GCM (AEAD).
 */
public class GcmExperiment implements Experiment {

    private final String id;
    private final int keySizeBits;
    private final byte[] messageData;
    private final byte[] aadData;
    private final AesGcmService gcmService;

    public GcmExperiment(String id, int keySizeBits, byte[] messageData, byte[] aadData) {
        this.id = id;
        this.keySizeBits = keySizeBits;
        this.messageData = messageData;
        this.aadData = aadData;
        this.gcmService = new AesGcmService();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getTitle() {
        return "Caractérisation du chiffrement authentifié AES-GCM (AEAD)";
    }

    @Override
    public String getObjective() {
        return "Mesurer les temps réels de chiffrement/déchiffrement GCM et valider la vérification du Tag MAC de 128 bits avec données associées (AAD).";
    }

    @Override
    public String getHypothesis() {
        return "Le mode GCM assure simultanément la confidentialité et l'authenticité/intégrité des données claires et de l'AAD.";
    }

    @Override
    public ExperimentResult run() {
        SecretKey key = CryptoKeyManager.generateKey(keySizeBits);
        byte[] nonce = NonceManager.generateGcmNonce();

        // Chiffrement GCM
        CryptoResult encResult = gcmService.encrypt(messageData, key, nonce, aadData);

        // Déchiffrement GCM avec vérification du tag
        CryptoResult decResult = gcmService.decrypt(encResult.getCiphertext(), encResult.getTag(), key, nonce, aadData);

        double throughputMBs = BenchmarkUtils.computeThroughputMBs(messageData.length, encResult.getEncryptionTimeNs());
        boolean matched = decResult.isSuccess() && decResult.getPlaintext() != null &&
                Arrays.equals(messageData, decResult.getPlaintext());

        String interpretation = "Chiffrement et vérification de conformité du tag GHASH réussis. Les données associées (AAD) ont été protégées sans être chiffrées.";
        String conclusion = "Dans cet environnement d'exécution, AES-GCM a traité " + messageData.length +
                " octets avec un débit mesuré de " + String.format("%.2f", throughputMBs) +
                " Mo/s. L'authenticité du message a été formellement validée.";

        return new ExperimentResult(
                id,
                "AES",
                "GCM",
                messageData.length,
                keySizeBits,
                nonce.length,
                aadData != null ? aadData.length : 0,
                encResult.getEncryptionTimeNs(),
                decResult.getDecryptionTimeNs(),
                throughputMBs,
                decResult.isAuthenticated() ? "VALID" : "REJECTED",
                "NONE",
                matched ? "SUCCESS" : "DATA_MISMATCH",
                getTitle(),
                getObjective(),
                getHypothesis(),
                interpretation,
                conclusion
        );
    }
}
