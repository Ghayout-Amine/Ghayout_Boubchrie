package ma.ump.fso.datamining.arbre;

import ma.ump.fso.datamining.donnees.SchemaDonnees;
import ma.ump.fso.datamining.modele.Instance;
import ma.ump.fso.datamining.modele.Noeud;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Coeur du projet : implementation from-scratch de l'algorithme ID3 (Quinlan, 1986).
 * <p>
 * Principe : a chaque noeud, choisir l'attribut qui maximise le gain d'information,
 * partitionner les instances, puis construire recursivement les sous-arbres.
 * Les attributs sont categoriels (valeurs encodees en entiers via {@link ma.ump.fso.datamining.donnees.SchemaDonnees}).
 * <p>
 * Jeu de donnees : Nursery (UCI) — 8 attributs, 5 classes de recommandation.
 */
public class ArbreDecisionID3 {

    /** Limite de profondeur pour eviter un arbre trop profond sur 10 368 instances. */
    private static final int PROFONDEUR_MAX = 10;

    /** Nombre minimum d'instances avant d'arreter la recursion (critere d'elagage). */
    private static final int MIN_ECHANTILLONS_PAR_FEUILLE = 2;

    private final Entropie entropie = new Entropie();
    private SchemaDonnees schema;
    private List<Instance> instances;
    private int[][] vecteurs;
    private Noeud racine;

    public void entrainer(List<Instance> instancesEntrainement, SchemaDonnees schema) {
        if (schema == null || schema.getNbAttributs() == 0) {
            throw new IllegalStateException("Schema vide : impossible de construire l'arbre.");
        }
        if (instancesEntrainement == null || instancesEntrainement.isEmpty()) {
            throw new IllegalStateException("Aucune instance d'entrainement.");
        }

        this.schema = schema;
        this.instances = instancesEntrainement;
        this.vecteurs = construireVecteurs(instancesEntrainement);
        validerVecteurs();

        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < instances.size(); i++) {
            indices.add(i);
        }

        List<Integer> attributs = new ArrayList<>();
        for (int i = 0; i < schema.getNbAttributs(); i++) {
            attributs.add(i);
        }

        racine = construireArbre(indices, attributs, 0);
        libererDonneesEntrainement();
    }

    private int[][] construireVecteurs(List<Instance> instancesEntrainement) {
        int[][] resultat = new int[instancesEntrainement.size()][];
        for (int i = 0; i < instancesEntrainement.size(); i++) {
            resultat[i] = instancesEntrainement.get(i).getValeursAttributs();
        }
        return resultat;
    }

    private void validerVecteurs() {
        int tailleAttendue = schema.getNbAttributs();
        for (int i = 0; i < vecteurs.length; i++) {
            if (vecteurs[i] == null || vecteurs[i].length != tailleAttendue) {
                throw new IllegalStateException(
                        "Encodage incoherent a l'instance " + i
                                + " (attendu " + tailleAttendue + " attributs).");
            }
        }
    }

    private int lireValeurAttribut(int indexInstance, int indexAttribut) {
        int[] vecteur = vecteurs[indexInstance];
        if (vecteur == null || indexAttribut < 0 || indexAttribut >= vecteur.length) {
            return 0;
        }
        return vecteur[indexAttribut];
    }

    private void libererDonneesEntrainement() {
        instances = null;
        vecteurs = null;
    }

    /**
     * Construction recursive de l'arbre ID3.
     * Cas d'arret : feuille pure, profondeur max, trop peu d'instances, plus d'attributs, ou gain <= 0.
     */
    private Noeud construireArbre(List<Integer> indices, List<Integer> attributsDisponibles, int profondeur) {
        String classeMajoritaire = classeMajoritaire(indices);

        if (estFeuillePure(indices)) {
            return Noeud.feuille(indices.isEmpty() ? classeMajoritaire : instances.get(indices.get(0)).getClasse());
        }
        if (profondeur >= PROFONDEUR_MAX || indices.size() < MIN_ECHANTILLONS_PAR_FEUILLE
                || attributsDisponibles.isEmpty()) {
            return Noeud.feuille(classeMajoritaire);
        }

        int meilleurAttribut = -1;
        double meilleurGain = -1.0;

        // Etape ID3 : selectionner l'attribut de gain d'information maximal
        for (int attribut : attributsDisponibles) {
            int nbModalites = schema.getNbModalites(attribut);
            double gain = entropie.calculerGain(
                    instances, indices, vecteurs, attribut, nbModalites);
            if (gain > meilleurGain) {
                meilleurGain = gain;
                meilleurAttribut = attribut;
            }
        }

        if (meilleurAttribut < 0 || meilleurGain <= 0.0) {
            return Noeud.feuille(classeMajoritaire);
        }

        String nomAttribut = schema.getNomAttribut(meilleurAttribut);
        int nbModalites = schema.getNbModalites(meilleurAttribut);
        Noeud noeud = Noeud.interne(meilleurAttribut, nomAttribut, nbModalites);

        List<List<Integer>> partitions = new ArrayList<>();
        for (int i = 0; i < nbModalites; i++) {
            partitions.add(new ArrayList<>());
        }
        for (int index : indices) {
            int valeur = lireValeurAttribut(index, meilleurAttribut);
            if (valeur >= 0 && valeur < nbModalites) {
                partitions.get(valeur).add(index);
            }
        }

        List<Integer> attributsRestants = new ArrayList<>(attributsDisponibles);
        attributsRestants.remove(Integer.valueOf(meilleurAttribut));

        for (int valeur = 0; valeur < nbModalites; valeur++) {
            List<Integer> sousIndices = partitions.get(valeur);
            if (sousIndices.isEmpty()) {
                noeud.setEnfant(valeur, Noeud.feuille(classeMajoritaire));
            } else {
                noeud.setEnfant(valeur, construireArbre(sousIndices, attributsRestants, profondeur + 1));
            }
        }

        return noeud;
    }

    private boolean estFeuillePure(List<Integer> indices) {
        if (indices.isEmpty()) {
            return true;
        }
        String premiereClasse = instances.get(indices.get(0)).getClasse();
        for (int index : indices) {
            if (!instances.get(index).getClasse().equals(premiereClasse)) {
                return false;
            }
        }
        return true;
    }

    private String classeMajoritaire(List<Integer> indices) {
        if (indices.isEmpty()) {
            return "inconnu";
        }
        Map<String, Integer> compteur = new HashMap<>();
        for (int index : indices) {
            String classe = instances.get(index).getClasse();
            compteur.merge(classe, 1, Integer::sum);
        }
        return compteur.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("inconnu");
    }

    public String predire(int[] valeurs) {
        if (racine == null) {
            throw new IllegalStateException("L'arbre n'a pas encore ete entraine.");
        }
        if (valeurs == null || valeurs.length != schema.getNbAttributs()) {
            return "inconnu";
        }
        return predireArbre(valeurs);
    }

    private String predireArbre(int[] vecteur) {
        Noeud courant = racine;
        while (!courant.estFeuille()) {
            int valeur = vecteur[courant.getIndexAttribut()];
            courant = courant.getEnfant(valeur);
            if (courant == null) {
                return "inconnu";
            }
        }
        return courant.getEtiquetteFeuille();
    }

    public List<String> getCheminDecision(int[] vecteur) {
        if (racine == null) {
            throw new IllegalStateException("L'arbre n'a pas encore ete entraine.");
        }
        List<String> chemin = new ArrayList<>();
        Noeud courant = racine;
        while (!courant.estFeuille()) {
            int valeur = vecteur[courant.getIndexAttribut()];
            chemin.add(courant.getNomAttribut() + " = "
                    + schema.getLibelleModalite(courant.getIndexAttribut(), valeur));
            courant = courant.getEnfant(valeur);
            if (courant == null) {
                break;
            }
        }
        if (courant != null && courant.estFeuille()) {
            chemin.add("=> " + courant.getEtiquetteFeuille());
        }
        return chemin;
    }

    public Noeud getRacine() {
        return racine;
    }

    public SchemaDonnees getSchema() {
        return schema;
    }

    public boolean estEntraine() {
        return racine != null;
    }
}
