package crypto;

import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.util.Arrays;

/**
 * Service cryptographique pour le mode AES-GCM (Galois/Counter Mode).
 * Fournit un chiffrement authentifié avec données associées (AEAD).
 *
 * Propriétés théoriques :
 * - Confidentialité (mode CTR sous-jacent).
 * - Intégrité et Authenticité (multiplicateur GHASH dans GF(2^128) produisant un Tag MAC de 128 bits).
 * - AAD : Données associées authentifiées mais transmises en clair.
 * - Vulnérabilité critique : La réutilisation du Nonce avec la même clé compromet la confidentialité
 *   et permet de dériver la clé d'authentification GHASH.
 */
public class AesGcmService {

    public static final String TRANSFORMATION_GCM = "AES/GCM/NoPadding";
    public static final int TAG_LENGTH_BITS = 128; // 16 octets
    public static final int TAG_LENGTH_BYTES = TAG_LENGTH_BITS / 8;

    /**
     * Chiffre un texte clair avec AES-GCM, en intégrant optionnellement des données associées (AAD).
     */
    public CryptoResult encrypt(byte[] plaintext, SecretKey key, byte[] nonce, byte[] aad) {
        if (plaintext == null || key == null || nonce == null) {
            throw new IllegalArgumentException("Plaintext, key et nonce ne peuvent être nuls");
        }

        long startNs = System.nanoTime();
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION_GCM);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BITS, nonce);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);

            if (aad != null && aad.length > 0) {
                cipher.updateAAD(aad);
            }

            // Java JCA accole le tag de 16 octets à la fin du ciphertext
            byte[] combined = cipher.doFinal(plaintext);
            long durationNs = System.nanoTime() - startNs;

            int cipherLength = combined.length - TAG_LENGTH_BYTES;
            byte[] ciphertextOnly = new byte[cipherLength];
            byte[] tag = new byte[TAG_LENGTH_BYTES];

            System.arraycopy(combined, 0, ciphertextOnly, 0, cipherLength);
            System.arraycopy(combined, cipherLength, tag, 0, TAG_LENGTH_BYTES);

            return new CryptoResult(
                    "GCM",
                    plaintext,
                    ciphertextOnly,
                    key.getEncoded(),
                    nonce,
                    aad,
                    tag,
                    durationNs,
                    0,
                    true, // Mode authentifié nativement
                    true,
                    null
            );
        } catch (Exception e) {
            long durationNs = System.nanoTime() - startNs;
            return new CryptoResult("GCM", plaintext, null, key.getEncoded(), nonce, aad, null,
                    durationNs, 0, false, false, e.getMessage());
        }
    }

    /**
     * Déchiffre un texte chiffré et vérifie le tag d'authentification MAC avec l'AAD.
     */
    public CryptoResult decrypt(byte[] ciphertextOnly, byte[] tag, SecretKey key, byte[] nonce, byte[] aad) {
        if (ciphertextOnly == null || tag == null || key == null || nonce == null) {
            throw new IllegalArgumentException("Paramètres nuls interdits pour le déchiffrement GCM");
        }

        long startNs = System.nanoTime();
        try {
            // Reconstitution du flux combiné ciphertext || tag pour le Cipher Java standard
            byte[] combined = new byte[ciphertextOnly.length + tag.length];
            System.arraycopy(ciphertextOnly, 0, combined, 0, ciphertextOnly.length);
            System.arraycopy(tag, 0, combined, ciphertextOnly.length, tag.length);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION_GCM);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BITS, nonce);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);

            if (aad != null && aad.length > 0) {
                cipher.updateAAD(aad);
            }

            byte[] decrypted = cipher.doFinal(combined);
            long durationNs = System.nanoTime() - startNs;

            return new CryptoResult(
                    "GCM",
                    decrypted,
                    ciphertextOnly,
                    key.getEncoded(),
                    nonce,
                    aad,
                    tag,
                    0,
                    durationNs,
                    true, // Tag vérifié avec succès
                    true,
                    null
            );
        } catch (AEADBadTagException e) {
            long durationNs = System.nanoTime() - startNs;
            return new CryptoResult("GCM", null, ciphertextOnly, key.getEncoded(), nonce, aad, tag,
                    0, durationNs, false, false, "AUTHENTICATION FAILED (Tag invalide ou données altérées)");
        } catch (Exception e) {
            long durationNs = System.nanoTime() - startNs;
            return new CryptoResult("GCM", null, ciphertextOnly, key.getEncoded(), nonce, aad, tag,
                    0, durationNs, false, false, e.getMessage());
        }
    }

    /**
     * Déchiffrement direct avec le buffer combiné (ciphertext || tag).
     */
    public CryptoResult decryptCombined(byte[] combined, SecretKey key, byte[] nonce, byte[] aad) {
        if (combined == null || combined.length < TAG_LENGTH_BYTES) {
            return new CryptoResult("GCM", null, null, key != null ? key.getEncoded() : null, nonce, aad, null,
                    0, 0, false, false, "Données combinées GCM trop courtes pour contenir un tag 128 bits");
        }
        int cipherLen = combined.length - TAG_LENGTH_BYTES;
        byte[] cipherOnly = Arrays.copyOfRange(combined, 0, cipherLen);
        byte[] tag = Arrays.copyOfRange(combined, cipherLen, combined.length);
        return decrypt(cipherOnly, tag, key, nonce, aad);
    }
}
