package ma.ump.fso.datamining.service;

import ma.ump.fso.datamining.arbre.ArbreDecisionID3;
import ma.ump.fso.datamining.donnees.SchemaDonnees;
import ma.ump.fso.datamining.donnees.SchemaNursery;
import ma.ump.fso.datamining.evaluation.Evaluateur;
import ma.ump.fso.datamining.modele.Instance;
import ma.ump.fso.datamining.modele.ResultatEvaluation;
import ma.ump.fso.datamining.util.NurseryCsvReader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Gestion du cycle de vie des donnees et de l'apprentissage.
 * <p>
 * Les fichiers CSV par defaut sont dans {@code src/main/resources/donnees/}
 * (10 368 entrainement / 2 592 test, decoupage 80/20 du corpus Nursery).
 * Aucune base SQL : les donnees sont lues depuis des fichiers CSV.
 */
@Service
public class ApprentissageService {
    private final SchemaDonnees schema = SchemaNursery.getSchema();
    private final Evaluateur evaluateur;

    private ArbreDecisionID3 arbre;
    private List<Instance> instancesEntrainement;
    private List<Instance> instancesTest;
    private ResultatEvaluation derniereEvaluation;
    private String messageEtat = "Aucun apprentissage effectue.";

    public ApprentissageService(Evaluateur evaluateur) {
        this.evaluateur = evaluateur;
    }

    /** Charge les jeux entrainement.csv et test.csv fournis avec le projet. */
    public void chargerDonneesParDefaut() throws IOException {        instancesEntrainement = NurseryCsvReader.lire(
                new ClassPathResource("donnees/entrainement.csv").getInputStream(), schema);
        instancesTest = NurseryCsvReader.lire(
                new ClassPathResource("donnees/test.csv").getInputStream(), schema);
        messageEtat = "Dataset Nursery (UCI) charge : "
                + instancesEntrainement.size() + " entrainement, "
                + instancesTest.size() + " test.";
    }

    public void chargerEntrainement(MultipartFile fichier) throws IOException {
        if (fichier == null || fichier.isEmpty()) {
            throw new IllegalStateException("Aucun fichier d'entrainement selectionne.");
        }
        instancesEntrainement = NurseryCsvReader.lire(fichier.getInputStream(), schema);
        if (instancesEntrainement.isEmpty()) {
            throw new IllegalStateException(
                    "Le fichier '" + fichier.getOriginalFilename() + "' est vide ou au mauvais format. "
                            + "Utilisez entrainement.csv (colonnes Nursery + classe).");
        }
        messageEtat = "Fichier d'entrainement charge : " + instancesEntrainement.size() + " instances.";
    }

    public void chargerTest(MultipartFile fichier) throws IOException {
        if (fichier == null || fichier.isEmpty()) {
            throw new IllegalStateException("Aucun fichier de test selectionne.");
        }
        instancesTest = NurseryCsvReader.lire(fichier.getInputStream(), schema);
        if (instancesTest.isEmpty()) {
            throw new IllegalStateException(
                    "Le fichier '" + fichier.getOriginalFilename() + "' est vide ou au mauvais format. "
                            + "Utilisez test.csv (colonnes Nursery + classe).");
        }
        messageEtat = "Fichier de test charge : " + instancesTest.size() + " instances.";
    }

    /** Construit l'arbre ID3 a partir des instances d'entrainement chargees. */
    public void entrainer() {        if (instancesEntrainement == null || instancesEntrainement.isEmpty()) {
            throw new IllegalStateException("Aucune donnee d'entrainement disponible.");
        }

        synchronized (this) {
            ArbreDecisionID3 arbreCandidat = new ArbreDecisionID3();
            arbreCandidat.entrainer(instancesEntrainement, schema);

            this.arbre = arbreCandidat;
            this.derniereEvaluation = null;

            messageEtat = "Arbre ID3 construit sur " + instancesEntrainement.size()
                    + " instances (" + schema.getNbAttributs() + " attributs categoriels).";
            if (instancesTest != null && !instancesTest.isEmpty()) {
                messageEtat += " Allez sur Evaluation pour lancer les tests.";
            } else {
                messageEtat += " Aucun fichier de test charge.";
            }
        }
    }

    public void evaluerSurTest() {
        if (!estEntraine()) {
            throw new IllegalStateException("Construisez d'abord l'arbre.");
        }
        if (instancesTest == null || instancesTest.isEmpty()) {
            throw new IllegalStateException("Aucun fichier de test charge.");
        }
        derniereEvaluation = evaluateur.evaluer(arbre, instancesTest);
        messageEtat = "Evaluation terminee sur " + instancesTest.size() + " instances de test.";
    }

    public SchemaDonnees getSchema() {
        return schema;
    }

    public boolean estEntraine() {
        return arbre != null && arbre.estEntraine();
    }

    public ArbreDecisionID3 getArbre() {
        return arbre;
    }

    public List<Instance> getInstancesEntrainement() {
        return instancesEntrainement;
    }

    public List<Instance> getInstancesTest() {
        return instancesTest;
    }

    public ResultatEvaluation getDerniereEvaluation() {
        return derniereEvaluation;
    }

    public String getMessageEtat() {
        return messageEtat;
    }
}
