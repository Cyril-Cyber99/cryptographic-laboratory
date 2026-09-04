package crypto;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Service cryptographique pour le mode AES-CBC (Cipher Block Chaining).
 * Utilise la transformation standard "AES/CBC/PKCS5Padding".
 *
 * Propriétés théoriques :
 * - Fournit la confidentialité (IND-CPA sous réserve d'un IV aléatoire unique).
 * - Ne fournit AUCUNE intégrité ni authentification intégrée.
 * - Vulnérable à la malléabilité et aux attaques par retournement de bits (Bit-Flipping).
 */
public class AesCbcService {

    public static final String TRANSFORMATION_CBC = "AES/CBC/PKCS5Padding";
    public static final String TRANSFORMATION_CBC_NO_PAD = "AES/CBC/NoPadding";

    /**
     * Chiffre un texte clair avec AES-CBC.
     */
    public CryptoResult encrypt(byte[] plaintext, SecretKey key, byte[] iv) {
        if (plaintext == null || key == null || iv == null) {
            throw new IllegalArgumentException("Les paramètres plaintext, key et iv ne peuvent être nuls");
        }
        if (iv.length != 16) {
            throw new IllegalArgumentException("L'IV pour AES-CBC doit faire exactement 16 octets (128 bits)");
        }

        long startNs = System.nanoTime();
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION_CBC);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
            byte[] ciphertext = cipher.doFinal(plaintext);
            long durationNs = System.nanoTime() - startNs;

            return new CryptoResult(
                    "CBC",
                    plaintext,
                    ciphertext,
                    key.getEncoded(),
                    iv,
                    null,
                    null,
                    durationNs,
                    0,
                    false, // CBC ne fournit pas d'authentification native
                    true,
                    null
            );
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                 InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            long durationNs = System.nanoTime() - startNs;
            return new CryptoResult("CBC", plaintext, null, key.getEncoded(), iv, null, null,
                    durationNs, 0, false, false, e.getMessage());
        }
    }

    /**
     * Déchiffre un texte chiffré avec AES-CBC et padding PKCS5.
     */
    public CryptoResult decrypt(byte[] ciphertext, SecretKey key, byte[] iv) {
        if (ciphertext == null || key == null || iv == null) {
            throw new IllegalArgumentException("Paramètres nuls interdits");
        }

        long startNs = System.nanoTime();
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION_CBC);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
            byte[] decrypted = cipher.doFinal(ciphertext);
            long durationNs = System.nanoTime() - startNs;

            return new CryptoResult(
                    "CBC",
                    decrypted,
                    ciphertext,
                    key.getEncoded(),
                    iv,
                    null,
                    null,
                    0,
                    durationNs,
                    false, // Aucune garantie d'intégrité cryptographique
                    true,
                    null
            );
        } catch (BadPaddingException e) {
            long durationNs = System.nanoTime() - startNs;
            return new CryptoResult("CBC", null, ciphertext, key.getEncoded(), iv, null, null,
                    0, durationNs, false, false, "Erreur de Padding (PKCS#5 invalide) : " + e.getMessage());
        } catch (Exception e) {
            long durationNs = System.nanoTime() - startNs;
            return new CryptoResult("CBC", null, ciphertext, key.getEncoded(), iv, null, null,
                    0, durationNs, false, false, e.getMessage());
        }
    }

    /**
     * Déchiffre sans validation de padding (AES/CBC/NoPadding) pour analyser la propagation
     * exacte des altérations d'octets lors des attaques de bit-flipping sur des blocs complets multiples de 16 octets.
     */
    public byte[] decryptNoPadding(byte[] ciphertext, SecretKey key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION_CBC_NO_PAD);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
        return cipher.doFinal(ciphertext);
    }
}
