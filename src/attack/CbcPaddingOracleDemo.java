package attack;

import crypto.AesCbcService;
import crypto.CryptoKeyManager;
import crypto.NonceManager;
import util.HexUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Démonstration académique de l'attaque par Oracle de Padding (Vaudenay 2002) sur AES-CBC.
 *
 * Principe général :
 * Texte chiffré modifié ──> Déchiffrement ──> Vérification du padding PKCS#5 ──> Observation de la réponse
 *
 * Si le serveur / système indique (par un code d'erreur HTTP 500 vs 200, ou par différence de temps)
 * que le padding PKCS#5 est invalide ou valide, un attaquant peut deviner le clair octet par octet
 * en 256 essais maximum par octet, SANS JAMAIS CONNAÎTRE LA CLÉ SECRÈTE !
 */
public class CbcPaddingOracleDemo {

    public static class PaddingOracleResult {
        public final String originalPlaintext;
        public final byte[] originalCiphertext;
        public final byte[] originalIv;
        public final int attemptsCount;
        public final byte recoveredByte;
        public final byte originalLastByte;
        public final boolean attackSuccess;
        public final String technicalExplanation;

        public PaddingOracleResult(String originalPlaintext,
                                   byte[] originalCiphertext,
                                   byte[] originalIv,
                                   int attemptsCount,
                                   byte recoveredByte,
                                   byte originalLastByte,
                                   boolean attackSuccess,
                                   String technicalExplanation) {
            this.originalPlaintext = originalPlaintext;
            this.originalCiphertext = originalCiphertext;
            this.originalIv = originalIv;
            this.attemptsCount = attemptsCount;
            this.recoveredByte = recoveredByte;
            this.originalLastByte = originalLastByte;
            this.attackSuccess = attackSuccess;
            this.technicalExplanation = technicalExplanation;
        }
    }

    /**
     * Simule l'Oracle côté serveur : renvoie true si le déchiffrement possède un padding PKCS#5 valide,
     * false si une BadPaddingException est levée.
     */
    public static boolean paddingOracle(byte[] ciphertext, SecretKey key, byte[] iv) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
            cipher.doFinal(ciphertext);
            return true; // Padding valide (ex: code HTTP 200 ou pas d'erreur padding)
        } catch (BadPaddingException e) {
            return false; // Padding invalide détecté par le serveur
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Exécute une démonstration locale retrouvant le dernier octet du clair grâce à l'Oracle de padding.
     */
    public PaddingOracleResult executeDemo(String plaintext) {
        if (plaintext == null || plaintext.isEmpty()) {
            plaintext = "MOT_DE_PASSE_SECRET_BANCAIRE";
        }

        SecretKey key = CryptoKeyManager.generateKey(128);
        byte[] iv = NonceManager.generateCbcIv();
        AesCbcService cbc = new AesCbcService();

        byte[] ptBytes = plaintext.getBytes(StandardCharsets.UTF_8);
        var enc = cbc.encrypt(ptBytes, key, iv);
        byte[] cipher = enc.getCiphertext();

        // On cible le dernier bloc de 16 octets
        int totalBlocks = cipher.length / 16;
        int lastBlockIdx = (totalBlocks - 1) * 16;
        int prevBlockIdx = (totalBlocks - 2) * 16;

        byte[] prevBlock = (totalBlocks > 1)
                ? Arrays.copyOfRange(cipher, prevBlockIdx, prevBlockIdx + 16)
                : Arrays.copyOf(iv, 16);

        byte[] lastCipherBlock = Arrays.copyOfRange(cipher, lastBlockIdx, lastBlockIdx + 16);

        // Simulation de l'attaque : retrouver l'octet intermédiaire I[15] = AES_Inv(C_last)[15]
        // On modifie l'octet C'_prev[15] jusqu'à ce que l'oracle réponde TRUE (padding 0x01 obtenu)
        byte[] modifiedPrev = Arrays.copyOf(prevBlock, 16);
        int attempts = 0;
        byte recoveredByte = 0;
        boolean success = false;

        // Attaque ciblée : on teste les 256 valeurs d'octets possibles pour forcer un padding 0x01
        for (int guess = 0; guess < 256; guess++) {
            attempts++;
            modifiedPrev[15] = (byte) guess;

            // Construction du message de test envoyé à l'oracle (bloc modifié + bloc cible)
            byte[] testPayload;
            byte[] testIv;
            if (totalBlocks > 1) {
                testIv = iv;
                testPayload = new byte[cipher.length];
                System.arraycopy(cipher, 0, testPayload, 0, cipher.length);
                System.arraycopy(modifiedPrev, 0, testPayload, prevBlockIdx, 16);
            } else {
                testIv = modifiedPrev;
                testPayload = lastCipherBlock;
            }

            if (paddingOracle(testPayload, key, testIv)) {
                // Si l'octet original donnait déjà un padding valide, on évite le faux positif trivial
                if (guess == (prevBlock[15] & 0xFF) && attempts == 1) {
                    continue;
                }
                // Condition vérifiée : I[15] ^ guess = 0x01 => I[15] = guess ^ 0x01
                byte intermediateByte = (byte) (guess ^ 0x01);
                // Le clair original était P[15] = I[15] ^ C_prev[15]
                recoveredByte = (byte) (intermediateByte ^ prevBlock[15]);
                success = true;
                break;
            }
        }

        byte origLast = ptBytes[ptBytes.length - 1];

        String explanation = "DÉMONSTRATION DE L'ATTAQUE PAR ORACLE DE PADDING (VAUDENAY) :\n" +
                "====================================================================\n" +
                "Principe : Texte chiffré modifié ──> Déchiffrement ──> Vérification Padding ──> Réponse Oracle\n\n" +
                "• Texte clair testé : \"" + plaintext + "\"\n" +
                "• Nombre d'essais pour trouver l'octet : " + attempts + " requêtes (max 256 au lieu de 2^128 !)\n" +
                "• Octet original déduit par l'Oracle  : 0x" + String.format("%02X", recoveredByte) +
                " (Caractère : '" + (char) recoveredByte + "')\n" +
                "• Octet réel en clair                 : 0x" + String.format("%02X", origLast) + "\n" +
                "• Succès de la déduction              : " + (success ? "CONFIRMÉ" : "ÉCHEC") + "\n\n" +
                "CONCLUSION SCIENTIFIQUE :\n" +
                "La vulnérabilité ne provient pas de l'algorithme AES, mais du mode CBC avec padding PKCS#5\n" +
                "associé à un canal auxiliaire révélant la validité du bourrage.\n" +
                "AES-GCM est totalement immunisé car il n'utilise aucun padding et vérifie le Tag MAC avant tout traitement.";

        return new PaddingOracleResult(plaintext, cipher, iv, attempts, recoveredByte, origLast, success, explanation);
    }
}
