package attack;

import crypto.AesCbcService;
import crypto.CryptoKeyManager;
import crypto.NonceManager;
import util.HexUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Démonstration pédagogique de la manipulation du Vecteur d'Initialisation (IV) en mode AES-CBC.
 *
 * Principes cryptographiques :
 * Lors du déchiffrement du premier bloc :
 * P1 = AES_Inv(C1, K) XOR IV
 *
 * Si l'IV n'est pas authentifié par un MAC externe et transite sur un canal non intègre :
 * L'adversaire remplace IV par IV' = IV XOR Delta.
 * Le destinataire déchiffre :
 * P'1 = AES_Inv(C1, K) XOR IV' = (AES_Inv(C1, K) XOR IV) XOR Delta = P1 XOR Delta !
 *
 * Propriété remarquable :
 * Contrairement à la modification d'un bloc de ciphertext qui corrompt le bloc courant en bruit aléatoire,
 * LA MANIPULATION DE L'IV PERMET D'ALTÉRER DIRECTEMENT LE PREMIER BLOC SANS CORROMPRE AUCUN AUTRE BLOC !
 */
public class CbcIvManipulationDemo {

    public static class IvManipulationResult {
        public final String originalPlaintext;
        public final String tamperedPlaintext;
        public final byte[] originalIv;
        public final byte[] tamperedIv;
        public final byte[] ciphertext;
        public final int byteIndexModified;
        public final boolean success;
        public final String technicalExplanation;

        public IvManipulationResult(String originalPlaintext,
                                   String tamperedPlaintext,
                                   byte[] originalIv,
                                   byte[] tamperedIv,
                                   byte[] ciphertext,
                                   int byteIndexModified,
                                   boolean success,
                                   String technicalExplanation) {
            this.originalPlaintext = originalPlaintext;
            this.tamperedPlaintext = tamperedPlaintext;
            this.originalIv = originalIv;
            this.tamperedIv = tamperedIv;
            this.ciphertext = ciphertext;
            this.byteIndexModified = byteIndexModified;
            this.success = success;
            this.technicalExplanation = technicalExplanation;
        }
    }

    public IvManipulationResult executeDemo(String plaintext) {
        if (plaintext == null || plaintext.length() < 16) {
            plaintext = "ADMIN=FALSE;ACCES=RESTREINT;";
        }
        byte[] ptBytes = plaintext.getBytes(StandardCharsets.UTF_8);

        SecretKey key = CryptoKeyManager.generateKey(128);
        byte[] originalIv = NonceManager.generateCbcIv();
        AesCbcService cbc = new AesCbcService();

        // Chiffrement initial
        var enc = cbc.encrypt(ptBytes, key, originalIv);
        byte[] cipher = enc.getCiphertext();

        // Manipulation chirurgicale de l'IV
        // On veut transformer "ADMIN=FALSE" en "ADMIN=TRUE "
        // À l'index 6 : 'F' (0x46) -> 'T' (0x54) : Delta = 0x46 ^ 0x54 = 0x12
        byte[] tamperedIv = Arrays.copyOf(originalIv, originalIv.length);
        int targetIdx = 6;
        byte delta = (byte) ('F' ^ 'T');
        tamperedIv[targetIdx] ^= delta;

        // Déchiffrement avec l'IV manipulé
        var dec = cbc.decrypt(cipher, key, tamperedIv);
        String decStr = new String(dec.getPlaintext(), StandardCharsets.UTF_8);

        String explanation = "DÉMONSTRATION — MANIPULATION DU VECTEUR D'INITIALISATION (IV CBC) :\n" +
                "====================================================================\n" +
                "Formule formelle : P'1 = D_K(C1) XOR IV' = P1 XOR (IV XOR IV')\n\n" +
                "• Message clair émis       : \"" + plaintext + "\"\n" +
                "• IV Original (128 bits)   : " + HexUtils.bytesToSpacedHex(originalIv) + "\n" +
                "• IV Modifié par l'attaquant: " + HexUtils.bytesToSpacedHex(tamperedIv) + "\n" +
                "• Message déchiffré reçu   : \"" + decStr + "\"\n\n" +
                "OBSERVATION CRITIQUE :\n" +
                "1. Aucune exception cryptographique n'a été levée.\n" +
                "2. Aucun bloc n'a été corrompu en bruit aléatoire (contrairement au bit-flipping de ciphertext).\n" +
                "3. Le premier bloc a été altéré de manière chirurgicale sans que le récepteur ne puisse s'en apercevoir.\n" +
                "Conclusion : L'IV de CBC DOIT impérativement être intègre et protégé par un MAC (ou remplacé par GCM).";

        return new IvManipulationResult(plaintext, decStr, originalIv, tamperedIv, cipher, targetIdx, true, explanation);
    }
}
