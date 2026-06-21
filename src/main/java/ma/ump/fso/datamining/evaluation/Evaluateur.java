package ma.ump.fso.datamining.evaluation;

import ma.ump.fso.datamining.arbre.ArbreDecisionID3;
import ma.ump.fso.datamining.donnees.SchemaNursery;
import ma.ump.fso.datamining.modele.Instance;
import ma.ump.fso.datamining.modele.MetriquesClasse;
import ma.ump.fso.datamining.modele.ResultatEvaluation;
import ma.ump.fso.datamining.util.NormaliseurClasse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Evaluation du modele sur le jeu de test independant.
 * Calcule exactitude, precision/rappel/F1 macro et la matrice de confusion (5 classes Nursery).
 * Resultats documentes dans le rapport : exactitude 98,30 % sur 2 592 instances.
 */
@Component
public class Evaluateur {

    public ResultatEvaluation evaluer(ArbreDecisionID3 arbre, List<Instance> instancesTest) {
        if (instancesTest.isEmpty()) {
            List<String> classes = SchemaNursery.getClasses();
            return new ResultatEvaluation(0, 0, 0, 0, 0, classes,
                    new int[classes.size()][classes.size()], List.of());
        }

        List<String> classes = new ArrayList<>(SchemaNursery.getClasses());
        int[][] matrice = new int[classes.size()][classes.size()];
        int[] supportParClasse = new int[classes.size()];
        int correct = 0;

        for (Instance instance : instancesTest) {
            String classeReelle = NormaliseurClasse.normaliser(instance.getClasse());
            int indexReel = classes.indexOf(classeReelle);
            if (indexReel < 0) {
                continue;
            }
            supportParClasse[indexReel]++;

            String prediction = NormaliseurClasse.normaliser(arbre.predire(instance.getValeursAttributs()));
            if (NormaliseurClasse.estInconnue(prediction)) {
                continue;
            }

            int indexPredit = classes.indexOf(prediction);
            if (indexPredit < 0) {
                continue;
            }

            matrice[indexReel][indexPredit]++;
            if (indexReel == indexPredit) {
                correct++;
            }
        }

        double exactitude = (double) correct / instancesTest.size();
        List<MetriquesClasse> metriquesParClasse = calculerMetriquesParClasse(classes, matrice, supportParClasse);
        double precisionMacro = moyenne(metriquesParClasse.stream().mapToDouble(MetriquesClasse::getPrecision).toArray());
        double rappelMacro = moyenne(metriquesParClasse.stream().mapToDouble(MetriquesClasse::getRappel).toArray());
        double f1Macro = moyenne(metriquesParClasse.stream().mapToDouble(MetriquesClasse::getF1).toArray());

        return new ResultatEvaluation(
                exactitude, precisionMacro, rappelMacro, f1Macro,
                instancesTest.size(), classes, matrice, metriquesParClasse);
    }

    private List<MetriquesClasse> calculerMetriquesParClasse(List<String> classes, int[][] matrice, int[] supportParClasse) {
        List<MetriquesClasse> resultat = new ArrayList<>();
        for (int i = 0; i < classes.size(); i++) {
            int vp = matrice[i][i];
            int fp = 0;
            for (int ligne = 0; ligne < classes.size(); ligne++) {
                if (ligne != i) {
                    fp += matrice[ligne][i];
                }
            }
            int support = supportParClasse[i];
            int fn = support - vp;
            double precision = diviser(vp, vp + fp);
            double rappel = diviser(vp, vp + fn);
            double f1 = diviser(2 * precision * rappel, precision + rappel);
            resultat.add(new MetriquesClasse(classes.get(i), precision, rappel, f1, support));
        }
        return resultat;
    }

    private double moyenne(double[] valeurs) {
        if (valeurs.length == 0) {
            return 0.0;
        }
        double somme = 0.0;
        for (double valeur : valeurs) {
            somme += valeur;
        }
        return somme / valeurs.length;
    }

    private double diviser(double numerateur, double denominateur) {
        if (denominateur == 0.0) {
            return 0.0;
        }
        return numerateur / denominateur;
    }
}
