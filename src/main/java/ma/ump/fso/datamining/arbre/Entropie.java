package ma.ump.fso.datamining.arbre;

import ma.ump.fso.datamining.modele.Instance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Calcul de l'entropie de Shannon et du gain d'information (formules du chapitre 5 — cours Data Mining).
 * Formule : H(S) = -Σ p_i · log2(p_i) ; Gain(S,A) = H(S) - H(S|A).
 */
public class Entropie {

    /** Entropie d'un sous-ensemble d'instances (mesure de l'impurete des classes). */
    public double calculerEntropie(List<Instance> instances, List<Integer> indices) {
        if (indices.isEmpty()) {
            return 0.0;
        }

        Map<String, Integer> compteur = new HashMap<>();
        for (int index : indices) {
            String classe = instances.get(index).getClasse();
            compteur.merge(classe, 1, Integer::sum);
        }

        double entropie = 0.0;
        int total = indices.size();
        for (int count : compteur.values()) {
            if (count > 0) {
                double probabilite = (double) count / total;
                entropie -= probabilite * (Math.log(probabilite) / Math.log(2));
            }
        }
        return entropie;
    }

    /** Gain d'information apporte par un attribut categoriel (difference entropie avant/apres partition). */
    public double calculerGain(List<Instance> instances, List<Integer> indices,
                               int[][] vecteurs, int indexAttribut, int nbValeurs) {
        if (indices.isEmpty()) {
            return 0.0;
        }

        double entropieInitiale = calculerEntropie(instances, indices);
        List<List<Integer>> partitions = new ArrayList<>();
        for (int i = 0; i < nbValeurs; i++) {
            partitions.add(new ArrayList<>());
        }

        for (int index : indices) {
            int[] vecteur = vecteurs[index];
            if (vecteur == null || indexAttribut < 0 || indexAttribut >= vecteur.length) {
                continue;
            }
            int valeur = vecteur[indexAttribut];
            if (valeur >= 0 && valeur < nbValeurs) {
                partitions.get(valeur).add(index);
            }
        }

        int total = indices.size();
        double entropieConditionnelle = 0.0;
        for (List<Integer> partition : partitions) {
            if (!partition.isEmpty()) {
                entropieConditionnelle += ((double) partition.size() / total)
                        * calculerEntropie(instances, partition);
            }
        }

        return entropieInitiale - entropieConditionnelle;
    }
}
