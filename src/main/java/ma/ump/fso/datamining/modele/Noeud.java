package ma.ump.fso.datamining.modele;

/**
 * Structure d'un noeud de l'arbre de decision.
 * Noeud interne : test sur un attribut ; feuille : classe predite (etiquette).
 */
public class Noeud {

    private int indexAttribut = -1;
    private String nomAttribut;
    private String etiquetteFeuille;
    private Noeud[] enfants;

    public Noeud() {
    }

    public static Noeud feuille(String etiquette) {
        Noeud noeud = new Noeud();
        noeud.etiquetteFeuille = etiquette;
        return noeud;
    }

    public static Noeud interne(int indexAttribut, String nomAttribut, int nbValeurs) {
        Noeud noeud = new Noeud();
        noeud.indexAttribut = indexAttribut;
        noeud.nomAttribut = nomAttribut;
        noeud.enfants = new Noeud[nbValeurs];
        return noeud;
    }

    public boolean estFeuille() {
        return etiquetteFeuille != null;
    }

    public int getIndexAttribut() {
        return indexAttribut;
    }

    public String getNomAttribut() {
        return nomAttribut;
    }

    public String getEtiquetteFeuille() {
        return etiquetteFeuille;
    }

    public Noeud getEnfant(int valeur) {
        if (enfants == null || valeur < 0 || valeur >= enfants.length) {
            return null;
        }
        return enfants[valeur];
    }

    public void setEnfant(int valeur, Noeud enfant) {
        if (enfants != null && valeur >= 0 && valeur < enfants.length) {
            enfants[valeur] = enfant;
        }
    }

    public int getNbValeurs() {
        return enfants == null ? 0 : enfants.length;
    }
}
