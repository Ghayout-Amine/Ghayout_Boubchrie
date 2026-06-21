package ma.ump.fso.datamining.donnees;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SchemaDonnees {

    private final List<String> nomsAttributs;
    private final List<List<String>> modalitesParAttribut;
    private final Map<Integer, Map<String, Integer>> encodage;

    public SchemaDonnees(List<String> nomsAttributs, List<List<String>> modalitesParAttribut) {
        this.nomsAttributs = List.copyOf(nomsAttributs);
        this.modalitesParAttribut = modalitesParAttribut.stream().map(List::copyOf).toList();
        this.encodage = construireEncodage();
    }

    private Map<Integer, Map<String, Integer>> construireEncodage() {
        Map<Integer, Map<String, Integer>> cartes = new HashMap<>();
        for (int i = 0; i < nomsAttributs.size(); i++) {
            Map<String, Integer> carte = new HashMap<>();
            List<String> modalites = modalitesParAttribut.get(i);
            for (int j = 0; j < modalites.size(); j++) {
                carte.put(modalites.get(j).toLowerCase(), j);
            }
            cartes.put(i, carte);
        }
        return cartes;
    }

    public int getNbAttributs() {
        return nomsAttributs.size();
    }

    public List<String> getNomsAttributs() {
        return nomsAttributs;
    }

    public List<String> getModalites(int indexAttribut) {
        return modalitesParAttribut.get(indexAttribut);
    }

    public int getNbModalites(int indexAttribut) {
        return modalitesParAttribut.get(indexAttribut).size();
    }

    public String getNomAttribut(int indexAttribut) {
        return nomsAttributs.get(indexAttribut);
    }

    public String getLibelleModalite(int indexAttribut, int indexModalite) {
        return modalitesParAttribut.get(indexAttribut).get(indexModalite);
    }

    public int encoder(int indexAttribut, String valeurBrute) {
        if (valeurBrute == null || valeurBrute.isBlank()) {
            return -1;
        }
        String cle = valeurBrute.trim().toLowerCase();
        Integer code = encodage.get(indexAttribut).get(cle);
        if (code == null) {
            throw new IllegalArgumentException(
                    "Valeur inconnue '" + valeurBrute + "' pour l'attribut "
                            + nomsAttributs.get(indexAttribut));
        }
        return code;
    }

    public int[] encoderDepuisMap(Map<String, String> valeurs) {
        int[] codes = new int[nomsAttributs.size()];
        for (int i = 0; i < nomsAttributs.size(); i++) {
            String nom = nomsAttributs.get(i);
            String valeur = valeurs.get(nom);
            if (valeur == null) {
                throw new IllegalArgumentException("Attribut manquant : " + nom);
            }
            codes[i] = encoder(i, valeur);
        }
        return codes;
    }
}
