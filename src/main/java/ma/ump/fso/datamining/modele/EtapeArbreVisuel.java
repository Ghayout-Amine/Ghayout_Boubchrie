package ma.ump.fso.datamining.modele;

public class EtapeArbreVisuel {

    private final String attribut;
    private final String valeurAttribut;
    private final EtapeArbreVisuel suite;
    private final String classeFeuille;

    private EtapeArbreVisuel(String attribut, String valeurAttribut, EtapeArbreVisuel suite, String classeFeuille) {
        this.attribut = attribut;
        this.valeurAttribut = valeurAttribut;
        this.suite = suite;
        this.classeFeuille = classeFeuille;
    }

    public static EtapeArbreVisuel interne(String attribut, String valeurAttribut, EtapeArbreVisuel suite) {
        return new EtapeArbreVisuel(attribut, valeurAttribut, suite, null);
    }

    public static EtapeArbreVisuel feuille(String classe) {
        return new EtapeArbreVisuel(null, null, null, classe);
    }

    public boolean estFeuille() {
        return classeFeuille != null;
    }

    public String getAttribut() {
        return attribut;
    }

    public String getValeurAttribut() {
        return valeurAttribut;
    }

    public EtapeArbreVisuel getSuite() {
        return suite;
    }

    public String getClasseFeuille() {
        return classeFeuille;
    }
}
