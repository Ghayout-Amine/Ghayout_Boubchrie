package ma.ump.fso.datamining.modele;

public class MetriquesClasse {

    private final String classe;
    private final double precision;
    private final double rappel;
    private final double f1;
    private final int support;

    public MetriquesClasse(String classe, double precision, double rappel, double f1, int support) {
        this.classe = classe;
        this.precision = precision;
        this.rappel = rappel;
        this.f1 = f1;
        this.support = support;
    }

    public String getClasse() {
        return classe;
    }

    public double getPrecision() {
        return precision;
    }

    public double getRappel() {
        return rappel;
    }

    public double getF1() {
        return f1;
    }

    public int getSupport() {
        return support;
    }
}
