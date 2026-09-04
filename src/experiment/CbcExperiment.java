package experiment;

import crypto.AesCbcService;
import crypto.CryptoKeyManager;
import crypto.CryptoResult;
import crypto.NonceManager;
import util.BenchmarkUtils;

import javax.crypto.SecretKey;

/**
 * Expérience contrôlée sur le chiffrement et déchiffrement AES-CBC.
 */
public class CbcExperiment implements Experiment {

    private final String id;
    private final int keySizeBits;
    private final byte[] messageData;
    private final AesCbcService cbcService;

    public CbcExperiment(String id, int keySizeBits, byte[] messageData) {
        this.id = id;
        this.keySizeBits = keySizeBits;
        this.messageData = messageData;
        this.cbcService = new AesCbcService();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getTitle() {
        return "Caractérisation du chiffrement/déchiffrement AES-CBC";
    }

    @Override
    public String getObjective() {
        return "Mesurer les temps d'exécution réels de chiffrement et déchiffrement en mode CBC, et vérifier la réversibilité du déchiffrement.";
    }

    @Override
    public String getHypothesis() {
        return "Le mode CBC garantit la confidentialité mais requiert un IV aléatoire de 128 bits et n'offre aucune authentification native.";
    }

    @Override
    public ExperimentResult run() {
        SecretKey key = CryptoKeyManager.generateKey(keySizeBits);
        byte[] iv = NonceManager.generateCbcIv();

        // Chiffrement
        CryptoResult encResult = cbcService.encrypt(messageData, key, iv);
        // Déchiffrement
        CryptoResult decResult = cbcService.decrypt(encResult.getCiphertext(), key, iv);

        double throughputMBs = BenchmarkUtils.computeThroughputMBs(messageData.length, encResult.getEncryptionTimeNs());
        boolean matched = decResult.isSuccess() && decResult.getPlaintext() != null &&
                java.util.Arrays.equals(messageData, decResult.getPlaintext());

        String interpretation = "Chiffrement et déchiffrement exécutés avec succès. Les temps cryptographiques ont été capturés isolément via System.nanoTime().";
        String conclusion = "Dans cet environnement d'exécution, AES-CBC a traité " + messageData.length +
                " octets avec un débit mesuré de " + String.format("%.2f", throughputMBs) + " Mo/s. Aucun contrôle d'intégrité n'est assuré nativement.";

        return new ExperimentResult(
                id,
                "AES",
                "CBC",
                messageData.length,
                keySizeBits,
                iv.length,
                0,
                encResult.getEncryptionTimeNs(),
                decResult.getDecryptionTimeNs(),
                throughputMBs,
                "NOT_APPLICABLE",
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
