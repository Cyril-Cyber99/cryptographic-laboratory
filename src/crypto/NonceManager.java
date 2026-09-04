package crypto;

import java.security.SecureRandom;

/**
 * Gestionnaire pour la génération des vecteurs d'initialisation (IV) et des nonces cryptographiques.
 * Garantit l'usage d'un générateur pseudo-aléatoire cryptographiquement sûr (CSPRNG).
 *
 * Distinctions scientifiques fondamentales :
 * - En AES-CBC : L'IV doit être impérativement imprévisible (uniformément aléatoire de 128 bits).
 * - En AES-GCM : Le Nonce doit être unique par clé (96 bits recommandé par le NIST SP 800-38D).
 */
public final class NonceManager {

    public static final int CBC_IV_LENGTH_BYTES = 16;   // 128 bits (taille de bloc AES)
    public static final int GCM_NONCE_LENGTH_BYTES = 12; // 96 bits (standard optimal NIST SP 800-38D)

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private NonceManager() {
    }

    /**
     * Génère un vecteur d'initialisation aléatoire de 16 octets (128 bits) pour AES-CBC.
     */
    public static byte[] generateCbcIv() {
        byte[] iv = new byte[CBC_IV_LENGTH_BYTES];
        SECURE_RANDOM.nextBytes(iv);
        return iv;
    }

    /**
     * Génère un nonce aléatoire standard de 12 octets (96 bits) pour AES-GCM.
     */
    public static byte[] generateGcmNonce() {
        byte[] nonce = new byte[GCM_NONCE_LENGTH_BYTES];
        SECURE_RANDOM.nextBytes(nonce);
        return nonce;
    }

    /**
     * Génère un tableau d'octets aléatoires de taille arbitraire.
     */
    public static byte[] generateRandomBytes(int length) {
        if (length < 0) {
            throw new IllegalArgumentException("La longueur ne peut être négative");
        }
        byte[] bytes = new byte[length];
        SECURE_RANDOM.nextBytes(bytes);
        return bytes;
    }
}
