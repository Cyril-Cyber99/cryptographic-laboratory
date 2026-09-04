package test;

import attack.*;
import crypto.*;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Suite de tests de validation cryptographique interne autonome (sans framework externe).
 * Valide les 7 exigences fondamentales du cahier des charges :
 * 1. CBC encrypt -> decrypt = plaintext original
 * 2. GCM encrypt -> decrypt = plaintext original
 * 3. GCM ciphertext modifié -> rejet (AEADBadTagException)
 * 4. GCM tag modifié -> rejet
 * 5. GCM AAD modifiée -> rejet
 * 6. GCM nonce généré -> longueur correcte (12 octets / 96 bits)
 * 7. CBC IV généré -> longueur correcte (16 octets / 128 bits)
 */
public class CryptoValidationTest {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("====================================================================");
        System.out.println("SUITE DE VALIDATION CRYPTOGRAPHIQUE INTERNE (JAVA 17 PURE)");
        System.out.println("====================================================================");

        testCbcRoundtrip();
        testGcmRoundtrip();
        testGcmCiphertextTamperingRejection();
        testGcmTagTamperingRejection();
        testGcmAadTamperingRejection();
        testGcmNonceLength();
        testCbcIvLength();

        System.out.println("====================================================================");
        System.out.println("RÉSULTATS DE VALIDATION : " + testsPassed + " SUCCÈS, " + testsFailed + " ÉCHECS");
        if (testsFailed == 0) {
            System.out.println(">>> TOUS LES TESTS SONT CONFORMES AUX EXIGENCES ACADÉMIQUES.");
        } else {
            System.err.println(">>> DES ANOMALIES ONT ÉTÉ DÉTECTÉES.");
            System.exit(1);
        }
        System.out.println("====================================================================");
    }

    private static void assertTrue(String testName, boolean condition, String details) {
        if (condition) {
            System.out.println("[SUCCÈS] " + testName + " : " + details);
            testsPassed++;
        } else {
            System.err.println("[ÉCHEC]  " + testName + " : " + details);
            testsFailed++;
        }
    }

    // 1. CBC encrypt -> decrypt = original plaintext
    private static void testCbcRoundtrip() {
        AesCbcService cbc = new AesCbcService();
        SecretKey key = CryptoKeyManager.generateKey(128);
        byte[] iv = NonceManager.generateCbcIv();
        byte[] pt = "TEST_SECRET_CBC_PLAINTEXT_MASTER_2026".getBytes(StandardCharsets.UTF_8);

        CryptoResult enc = cbc.encrypt(pt, key, iv);
        CryptoResult dec = cbc.decrypt(enc.getCiphertext(), key, iv);

        boolean match = dec.isSuccess() && Arrays.equals(pt, dec.getPlaintext());
        assertTrue("Test 1 - CBC Chiffrement/Déchiffrement Réversible", match,
                "Plaintext restitué identique au clair original.");
    }

    // 2. GCM encrypt -> decrypt = original plaintext
    private static void testGcmRoundtrip() {
        AesGcmService gcm = new AesGcmService();
        SecretKey key = CryptoKeyManager.generateKey(128);
        byte[] nonce = NonceManager.generateGcmNonce();
        byte[] pt = "TEST_SECRET_GCM_PLAINTEXT_MASTER_2026".getBytes(StandardCharsets.UTF_8);
        byte[] aad = "METADONNEES_AUTH".getBytes(StandardCharsets.UTF_8);

        CryptoResult enc = gcm.encrypt(pt, key, nonce, aad);
        CryptoResult dec = gcm.decrypt(enc.getCiphertext(), enc.getTag(), key, nonce, aad);

        boolean match = dec.isSuccess() && dec.isAuthenticated() && Arrays.equals(pt, dec.getPlaintext());
        assertTrue("Test 2 - GCM Chiffrement/Déchiffrement Réversible & Authentifié", match,
                "Clair exact et tag GHASH validé avec succès.");
    }

    // 3. GCM ciphertext modifié -> rejet
    private static void testGcmCiphertextTamperingRejection() {
        GcmCiphertextTamperingDemo demo = new GcmCiphertextTamperingDemo();
        GcmCiphertextTamperingDemo.TamperingResult res = demo.executeDemo("MESSAGE_IMPORTANT", 3);
        assertTrue("Test 3 - GCM Altération Ciphertext", res.rejected,
                "Rejet strict immédiat (AEADBadTagException).");
    }

    // 4. GCM tag modifié -> rejet
    private static void testGcmTagTamperingRejection() {
        GcmTagTamperingDemo demo = new GcmTagTamperingDemo();
        GcmTagTamperingDemo.TagTamperingResult res = demo.executeDemo("MESSAGE_IMPORTANT", 1);
        assertTrue("Test 4 - GCM Altération Tag MAC", res.rejected,
                "Tag falsifié intercepté et rejeté.");
    }

    // 5. GCM AAD modifiée -> rejet
    private static void testGcmAadTamperingRejection() {
        GcmAadTamperingDemo demo = new GcmAadTamperingDemo();
        GcmAadTamperingDemo.AadTamperingResult res = demo.executeDemo("ROLE=USER", "ROLE=ADMIN", "DONNEE");
        assertTrue("Test 5 - GCM Altération AAD", res.rejected,
                "AAD falsifiée détectée par GHASH et rejetée.");
    }

    // 6. GCM nonce généré -> longueur correcte (12 octets / 96 bits)
    private static void testGcmNonceLength() {
        byte[] nonce = NonceManager.generateGcmNonce();
        boolean valid = nonce != null && nonce.length == NonceManager.GCM_NONCE_LENGTH_BYTES && nonce.length == 12;
        assertTrue("Test 6 - Longueur Normée du Nonce GCM", valid,
                "Longueur exacte de 12 octets (96 bits) conforme NIST SP 800-38D.");
    }

    // 7. CBC IV généré -> longueur correcte (16 octets / 128 bits)
    private static void testCbcIvLength() {
        byte[] iv = NonceManager.generateCbcIv();
        boolean valid = iv != null && iv.length == NonceManager.CBC_IV_LENGTH_BYTES && iv.length == 16;
        assertTrue("Test 7 - Longueur Normée de l'IV CBC", valid,
                "Longueur exacte de 16 octets (128 bits) conforme NIST SP 800-38A.");
    }
}
