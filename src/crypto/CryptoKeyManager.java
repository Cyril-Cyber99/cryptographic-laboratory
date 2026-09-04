package crypto;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Gestionnaire cryptographique pour la génération et l'encapsulation de clés AES.
 * Utilise strictement SecureRandom (CSPRNG) et les primitives de Java Cryptography Architecture (JCA).
 */
public final class CryptoKeyManager {

    private static final String ALGORITHM = "AES";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private CryptoKeyManager() {
    }

    /**
     * Génère une clé secrète AES de taille spécifiée (128, 192 ou 256 bits).
     */
    public static SecretKey generateKey(int keySizeBits) {
        if (keySizeBits != 128 && keySizeBits != 192 && keySizeBits != 256) {
            throw new IllegalArgumentException("Taille de clé AES invalide : " + keySizeBits + " (valeurs autorisées : 128, 192, 256)");
        }
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
            keyGen.init(keySizeBits, SECURE_RANDOM);
            return keyGen.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algorithme AES indisponible dans l'environnement Java", e);
        }
    }

    /**
     * Reconstruit une SecretKey à partir d'un tableau d'octets brut.
     */
    public static SecretKey loadKeyFromBytes(byte[] keyBytes) {
        if (keyBytes == null || (keyBytes.length != 16 && keyBytes.length != 24 && keyBytes.length != 32)) {
            throw new IllegalArgumentException("Longueur d'octets de clé AES invalide (attendu 16, 24 ou 32 octets)");
        }
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }
}
