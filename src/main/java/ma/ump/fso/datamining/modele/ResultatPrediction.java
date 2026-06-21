package ma.ump.fso.datamining.modele;

public class ResultatPrediction {

    private final String classe;
    private final EtapeArbreVisuel cheminArbre;

    public ResultatPrediction(String classe, EtapeArbreVisuel cheminArbre) {
        this.classe = classe;
        this.cheminArbre = cheminArbre;
    }

    public String getClasse() {
        return classe;
    }

    public EtapeArbreVisuel getCheminArbre() {
        return cheminArbre;
    }
}
