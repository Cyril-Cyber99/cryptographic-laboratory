package attack;

import crypto.AesCbcService;
import crypto.CryptoKeyManager;
import crypto.NonceManager;
import util.HexUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Démonstration pédagogique contrôlée de l'attaque par retournement de bits (Bit-Flipping) sur AES-CBC.
 *
 * Principes cryptographiques :
 * En mode CBC : P[i] = AES_Decrypt(C[i], K) XOR C[i-1].
 * Par conséquent, modifier un bit dans C[i-1] :
 * 1. Détruit complètement le bloc déchiffré P[i-1] (avalanche du déchiffrement AES).
 * 2. Modifie PRÉCISÉMENT le bit correspondant dans P[i] sans déclencher d'alerte cryptographique !
 *
 * AVERTISSEMENT : Démonstration strictement confinée à l'apprentissage académique.
 */
public class CbcBitFlippingDemo {

    public static class BitFlippingResult {
        public final String originalPlaintext;
        public final String tamperedPlaintext;
        public final byte[] originalCiphertext;
        public final byte[] tamperedCiphertext;
        public final int byteIndexModified;
        public final byte maskApplied;
        public final boolean exceptionRaised;
        public final String technicalExplanation;

        public BitFlippingResult(String originalPlaintext,
                                 String tamperedPlaintext,
                                 byte[] originalCiphertext,
                                 byte[] tamperedCiphertext,
                                 int byteIndexModified,
                                 byte maskApplied,
                                 boolean exceptionRaised,
                                 String technicalExplanation) {
            this.originalPlaintext = originalPlaintext;
            this.tamperedPlaintext = tamperedPlaintext;
            this.originalCiphertext = originalCiphertext;
            this.tamperedCiphertext = tamperedCiphertext;
            this.byteIndexModified = byteIndexModified;
            this.maskApplied = maskApplied;
            this.exceptionRaised = exceptionRaised;
            this.technicalExplanation = technicalExplanation;
        }
    }

    /**
     * Exécute une altération de bit contrôlée sur un message CBC de 32 octets (2 blocs complets de 16 octets).
     */
    public BitFlippingResult executeDemo(String plaintext) {
        // Message pédagogique par défaut calibré sur 2 blocs de 16 octets (32 octets)
        if (plaintext == null || plaintext.length() < 32) {
            plaintext = "ADMIN=FALSE;ID=100;ROLE=STUDENT;"; // 32 caractères
        }
        byte[] ptBytes = Arrays.copyOf(plaintext.getBytes(StandardCharsets.UTF_8), 32);

        SecretKey key = CryptoKeyManager.generateKey(128);
        byte[] iv = NonceManager.generateCbcIv();
        AesCbcService cbc = new AesCbcService();

        try {
            // Chiffrement initial de blocs sans padding pour cibler exactement le bloc 0 et 1
            javax.crypto.Cipher encCipher = javax.crypto.Cipher.getInstance("AES/CBC/NoPadding");
            encCipher.init(javax.crypto.Cipher.ENCRYPT_MODE, key, new javax.crypto.spec.IvParameterSpec(iv));
            byte[] origCipher = encCipher.doFinal(ptBytes);

            // Altération dans le premier bloc de ciphertext (C[0], index 6 par exemple)
            // On veut changer 'FALSE' (à l'offset 6 du 1er bloc) ou un octet cible dans le bloc 1
            // Dans CBC, altérer C[0] à l'index k altère P[1] à l'index k !
            int targetIndexInC0 = 6; // index 6 dans le premier bloc C[0]
            byte[] tamperedCipher = Arrays.copyOf(origCipher, origCipher.length);

            // On applique un masque XOR
            byte diffMask = (byte) 0x20; // Inversion d'un bit
            tamperedCipher[targetIndexInC0] ^= diffMask;

            // Déchiffrement du ciphertext altéré
            byte[] decrypted = cbc.decryptNoPadding(tamperedCipher, key, iv);
            String decStr = new String(decrypted, StandardCharsets.UTF_8);

            String explanation = "DÉMONSTRATION BIT-FLIPPING CBC :\n" +
                    "- Octet modifié dans Ciphertext : Index " + targetIndexInC0 + " (Bloc 1)\n" +
                    "- Conséquence sur Bloc 1 déchiffré : Déchiffrement corrompu en bruit pseudo-aléatoire.\n" +
                    "- Conséquence sur Bloc 2 déchiffré : L'octet à l'index " + targetIndexInC0 +
                    " du 2ème bloc a subi le masque XOR exact sans lever d'erreur !\n" +
                    "- Conclusion : AES-CBC seul ne détecte aucune falsification active. Un HMAC ou le passage à GCM est obligatoire.";

            return new BitFlippingResult(
                    new String(ptBytes, StandardCharsets.UTF_8),
                    decStr,
                    origCipher,
                    tamperedCipher,
                    targetIndexInC0,
                    diffMask,
                    false,
                    explanation
            );
        } catch (Exception e) {
            return new BitFlippingResult(
                    plaintext,
                    null,
                    null,
                    null,
                    -1,
                    (byte) 0,
                    true,
                    "Échec d'exécution : " + e.getMessage()
            );
        }
    }
}
