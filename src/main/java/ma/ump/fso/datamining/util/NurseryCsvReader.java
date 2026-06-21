package ma.ump.fso.datamining.util;

import ma.ump.fso.datamining.donnees.SchemaDonnees;
import ma.ump.fso.datamining.modele.Instance;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lecture des fichiers CSV du dataset Nursery (UCI).
 * Format attendu : 8 colonnes d'attributs + colonne {@code classe}.
 * Les valeurs textuelles sont converties en indices entiers via le schema.
 */
public final class NurseryCsvReader {
    private NurseryCsvReader() {
    }

    public static List<Instance> lire(InputStream flux, SchemaDonnees schema) throws IOException {
        List<Instance> instances = new ArrayList<>();
        List<String> nomsAttributs = schema.getNomsAttributs();

        try (BufferedReader lecteur = new BufferedReader(new InputStreamReader(flux, StandardCharsets.UTF_8))) {
            String entete = lecteur.readLine();
            if (entete == null) {
                return instances;
            }
            entete = supprimerBom(entete);
            String[] colonnes = entete.split(",");
            Map<String, Integer> indexParNom = new HashMap<>();
            for (int i = 0; i < colonnes.length; i++) {
                indexParNom.put(colonnes[i].trim().toLowerCase(), i);
            }

            while (true) {
                String ligne = lecteur.readLine();
                if (ligne == null) {
                    break;
                }
                ligne = supprimerBom(ligne).trim();
                if (ligne.isEmpty()) {
                    continue;
                }
                String[] champs = ligne.split(",", -1);
                if (champs.length < nomsAttributs.size() + 1) {
                    continue;
                }

                int[] valeurs = new int[nomsAttributs.size()];
                for (int i = 0; i < nomsAttributs.size(); i++) {
                    Integer idx = indexParNom.get(nomsAttributs.get(i).toLowerCase());
                    if (idx == null || idx >= champs.length) {
                        throw new IOException("Colonne manquante : " + nomsAttributs.get(i));
                    }
                    valeurs[i] = schema.encoder(i, champs[idx].trim());
                }

                Integer idxClasse = indexParNom.get("classe");
                if (idxClasse == null || idxClasse >= champs.length) {
                    throw new IOException("Colonne 'classe' manquante dans le CSV Nursery.");
                }
                String classe = NormaliseurClasse.normaliser(champs[idxClasse].trim());
                instances.add(new Instance(valeurs, classe));
            }
        }
        return instances;
    }

    private static String supprimerBom(String ligne) {
        if (ligne != null && !ligne.isEmpty() && ligne.charAt(0) == '\uFEFF') {
            return ligne.substring(1);
        }
        return ligne;
    }
}
