package util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Gestionnaire d'exportation des données expérimentales au format standard CSV (Comma-Separated Values).
 * Respecte strictement la structure définie dans le protocole académique.
 */
public final class CsvExporter {

    public static final String CSV_HEADER = "experiment_id,date,algorithm,mode,message_size,key_size,iv_or_nonce_size,aad_size,encryption_time_ns,decryption_time_ns,throughput_mb_s,authentication_status,attack_type,result";

    private CsvExporter() {
    }

    /**
     * Enregistre une liste de lignes CSV dans un fichier cible.
     */
    public static void exportToFile(File targetFile, List<String> csvRows) throws IOException {
        if (targetFile == null) {
            throw new IllegalArgumentException("Le fichier cible ne peut être null");
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(targetFile, StandardCharsets.UTF_8))) {
            writer.write(CSV_HEADER);
            writer.newLine();
            if (csvRows != null) {
                for (String row : csvRows) {
                    writer.write(row);
                    writer.newLine();
                }
            }
        }
    }
}
