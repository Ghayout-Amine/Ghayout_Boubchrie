package ma.ump.fso.datamining.modele;

import java.util.Collections;
import java.util.List;

public class ResultatEvaluation {

    private final double exactitude;
    private final double precisionMacro;
    private final double rappelMacro;
    private final double f1Macro;
    private final int nbInstances;
    private final List<String> classes;
    private final int[][] matrice;
    private final List<MetriquesClasse> metriquesParClasse;

    public ResultatEvaluation(double exactitude, double precisionMacro, double rappelMacro, double f1Macro,
                              int nbInstances, List<String> classes, int[][] matrice,
                              List<MetriquesClasse> metriquesParClasse) {
        this.exactitude = exactitude;
        this.precisionMacro = precisionMacro;
        this.rappelMacro = rappelMacro;
        this.f1Macro = f1Macro;
        this.nbInstances = nbInstances;
        this.classes = classes;
        this.matrice = matrice;
        this.metriquesParClasse = metriquesParClasse;
    }

    public double getExactitude() {
        return exactitude;
    }

    public double getPrecisionMacro() {
        return precisionMacro;
    }

    public double getRappelMacro() {
        return rappelMacro;
    }

    public double getF1Macro() {
        return f1Macro;
    }

    public int getNbInstances() {
        return nbInstances;
    }

    public List<String> getClasses() {
        return Collections.unmodifiableList(classes);
    }

    public int[][] getMatrice() {
        return matrice;
    }

    public List<MetriquesClasse> getMetriquesParClasse() {
        return Collections.unmodifiableList(metriquesParClasse);
    }

    public int getValeurMatrice(int ligne, int colonne) {
        return matrice[ligne][colonne];
    }
}
