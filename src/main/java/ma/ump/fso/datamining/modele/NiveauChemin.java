package ma.ump.fso.datamining.modele;

public class NiveauChemin {

    private final String attribut;
    private final String valeurAttribut;

    public NiveauChemin(String attribut, String valeurAttribut) {
        this.attribut = attribut;
        this.valeurAttribut = valeurAttribut;
    }

    public String getAttribut() {
        return attribut;
    }

    public String getValeurAttribut() {
        return valeurAttribut;
    }
}
