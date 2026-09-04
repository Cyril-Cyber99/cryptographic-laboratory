package experiment;

import util.FormatUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Modèle de résultat expérimental scientifique.
 * Contient l'ensemble des données d'un protocole expérimental complet et permet l'export CSV standard.
 */
public class ExperimentResult {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String experimentId;
    private final String timestamp;
    private final String algorithm;
    private final String mode;
    private final int messageSize;
    private final int keySize;
    private final int ivOrNonceSize;
    private final int aadSize;
    private final long encryptionTimeNs;
    private final long decryptionTimeNs;
    private final double throughputMBs;
    private final String authenticationStatus;
    private final String attackType;
    private final String result;

    // Métadonnées scientifiques
    private final String title;
    private final String objective;
    private final String hypothesis;
    private final String interpretation;
    private final String conclusion;

    public ExperimentResult(String experimentId,
                            String algorithm,
                            String mode,
                            int messageSize,
                            int keySize,
                            int ivOrNonceSize,
                            int aadSize,
                            long encryptionTimeNs,
                            long decryptionTimeNs,
                            double throughputMBs,
                            String authenticationStatus,
                            String attackType,
                            String result,
                            String title,
                            String objective,
                            String hypothesis,
                            String interpretation,
                            String conclusion) {
        this.experimentId = experimentId;
        this.timestamp = LocalDateTime.now().format(ISO_FORMATTER);
        this.algorithm = algorithm;
        this.mode = mode;
        this.messageSize = messageSize;
        this.keySize = keySize;
        this.ivOrNonceSize = ivOrNonceSize;
        this.aadSize = aadSize;
        this.encryptionTimeNs = encryptionTimeNs;
        this.decryptionTimeNs = decryptionTimeNs;
        this.throughputMBs = throughputMBs;
        this.authenticationStatus = authenticationStatus;
        this.attackType = attackType;
        this.result = result;
        this.title = title;
        this.objective = objective;
        this.hypothesis = hypothesis;
        this.interpretation = interpretation;
        this.conclusion = conclusion;
    }

    public String getExperimentId() {
        return experimentId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public String getMode() {
        return mode;
    }

    public int getMessageSize() {
        return messageSize;
    }

    public int getKeySize() {
        return keySize;
    }

    public int getIvOrNonceSize() {
        return ivOrNonceSize;
    }

    public int getAadSize() {
        return aadSize;
    }

    public long getEncryptionTimeNs() {
        return encryptionTimeNs;
    }

    public long getDecryptionTimeNs() {
        return decryptionTimeNs;
    }

    public double getThroughputMBs() {
        return throughputMBs;
    }

    public String getAuthenticationStatus() {
        return authenticationStatus;
    }

    public String getAttackType() {
        return attackType;
    }

    public String getResult() {
        return result;
    }

    public String getTitle() {
        return title;
    }

    public String getObjective() {
        return objective;
    }

    public String getHypothesis() {
        return hypothesis;
    }

    public String getInterpretation() {
        return interpretation;
    }

    public String getConclusion() {
        return conclusion;
    }

    /**
     * Génère la ligne CSV correspondante aux spécifications du projet.
     */
    public String toCsvRow() {
        return String.format("%s,%s,%s,%s,%d,%d,%d,%d,%d,%d,%.2f,%s,%s,%s",
                escapeCsv(experimentId),
                escapeCsv(timestamp),
                escapeCsv(algorithm),
                escapeCsv(mode),
                messageSize,
                keySize,
                ivOrNonceSize,
                aadSize,
                encryptionTimeNs,
                decryptionTimeNs,
                throughputMBs,
                escapeCsv(authenticationStatus),
                escapeCsv(attackType),
                escapeCsv(result)
        );
    }

    private String escapeCsv(String val) {
        if (val == null) return "";
        if (val.contains(",") || val.contains("\"") || val.contains("\n")) {
            return "\"" + val.replace("\"", "\"\"") + "\"";
        }
        return val;
    }

    /**
     * Produit la fiche scientifique académique complète.
     */
    public String toAcademicReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("--------------------------------------------------------------------------------\n");
        sb.append("FICHE EXPÉRIMENTALE : ").append(experimentId).append(" — ").append(title).append("\n");
        sb.append("Date & Heure : ").append(timestamp).append("\n");
        sb.append("--------------------------------------------------------------------------------\n");
        sb.append("OBJECTIF       : ").append(objective).append("\n\n");
        sb.append("HYPOTHÈSE      : ").append(hypothesis).append("\n\n");
        sb.append("PARAMÈTRES     : Algorithme=").append(algorithm).append("-").append(mode)
                .append(", Clé=").append(keySize).append(" bits")
                .append(", IV/Nonce=").append(ivOrNonceSize * 8).append(" bits")
                .append(", Taille Message=").append(FormatUtils.formatSize(messageSize))
                .append(", AAD=").append(aadSize).append(" octets\n\n");
        sb.append("MESURES RÉELLES:\n");
        sb.append("  • Temps de chiffrement : ").append(FormatUtils.formatDurationNs(encryptionTimeNs)).append("\n");
        sb.append("  • Temps de déchiffrement: ").append(FormatUtils.formatDurationNs(decryptionTimeNs)).append("\n");
        sb.append("  • Débit mesuré         : ").append(FormatUtils.formatThroughput(throughputMBs)).append("\n");
        sb.append("  • Statut Authenticité  : ").append(authenticationStatus).append("\n");
        sb.append("  • Type d'attaque testé : ").append(attackType).append("\n");
        sb.append("  • Résultat d'intégrité : ").append(result).append("\n\n");
        sb.append("INTERPRÉTATION : ").append(interpretation).append("\n\n");
        sb.append("CONCLUSION     : ").append(conclusion).append("\n");
        sb.append("--------------------------------------------------------------------------------\n");
        return sb.toString();
    }
}
