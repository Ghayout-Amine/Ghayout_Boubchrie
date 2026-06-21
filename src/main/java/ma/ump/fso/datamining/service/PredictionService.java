package ma.ump.fso.datamining.service;

import ma.ump.fso.datamining.arbre.ArbreDecisionID3;
import ma.ump.fso.datamining.arbre.VisualiseurArbre;
import ma.ump.fso.datamining.donnees.SchemaDonnees;
import ma.ump.fso.datamining.modele.EtapeArbreVisuel;
import ma.ump.fso.datamining.modele.NiveauChemin;
import ma.ump.fso.datamining.modele.ResultatPrediction;
import ma.ump.fso.datamining.util.NormaliseurClasse;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Service de prediction : encode les valeurs saisies, parcourt l'arbre ID3
 * et retourne la classe predite ainsi que le chemin de decision (interpretabilite).
 */
@Service
public class PredictionService {
    private final ApprentissageService apprentissageService;
    private final VisualiseurArbre visualiseurArbre = new VisualiseurArbre();

    private Map<String, String> dernieresValeurs = new LinkedHashMap<>();
    private ResultatPrediction dernierePrediction;
    private List<NiveauChemin> derniersCheminNiveaux;

    public PredictionService(ApprentissageService apprentissageService) {
        this.apprentissageService = apprentissageService;
    }

    public String predire(Map<String, String> valeursAttributs) {
        if (!apprentissageService.estEntraine()) {
            throw new IllegalStateException("Veuillez d'abord construire l'arbre.");
        }
        SchemaDonnees schema = apprentissageService.getSchema();
        int[] vecteur = schema.encoderDepuisMap(valeursAttributs);
        return NormaliseurClasse.normaliser(apprentissageService.getArbre().predire(vecteur));
    }

    public List<String> getCheminDecision(Map<String, String> valeursAttributs) {
        if (!apprentissageService.estEntraine()) {
            throw new IllegalStateException("Veuillez d'abord construire l'arbre.");
        }
        SchemaDonnees schema = apprentissageService.getSchema();
        return apprentissageService.getArbre().getCheminDecision(schema.encoderDepuisMap(valeursAttributs));
    }

    public EtapeArbreVisuel getCheminArbre(int[] vecteur) {
        verifierEntrainement();
        return visualiseurArbre.extraireCheminDepuisVecteur(
                vecteur, apprentissageService.getArbre(), apprentissageService.getSchema());
    }

    public List<NiveauChemin> getCheminVisuel(int[] vecteur) {
        return visualiseurArbre.aplatirChemin(getCheminArbre(vecteur));
    }

    public List<NiveauChemin> getCheminVisuel(EtapeArbreVisuel chemin) {
        return visualiseurArbre.aplatirChemin(chemin);
    }

    public ResultatPrediction predireComplet(Map<String, String> valeursAttributs) {
        verifierEntrainement();
        if (valeursAttributs == null || valeursAttributs.isEmpty()) {
            throw new IllegalArgumentException("Veuillez renseigner tous les attributs avant la prediction.");
        }

        SchemaDonnees schema = apprentissageService.getSchema();
        for (String nom : schema.getNomsAttributs()) {
            String valeur = valeursAttributs.get(nom);
            if (valeur == null || valeur.isBlank()) {
                throw new IllegalArgumentException("Attribut manquant : " + nom);
            }
        }

        ArbreDecisionID3 arbre = apprentissageService.getArbre();
        int[] vecteur = schema.encoderDepuisMap(valeursAttributs);
        String classe = nettoyerClasse(arbre.predire(vecteur));
        EtapeArbreVisuel chemin = visualiseurArbre.extraireCheminDepuisVecteur(vecteur, arbre, schema);
        return new ResultatPrediction(classe, chemin);
    }

    public void enregistrerDernierePrediction(Map<String, String> valeurs, ResultatPrediction resultat) {
        this.dernieresValeurs = new LinkedHashMap<>(valeurs);
        this.dernierePrediction = resultat;
        this.derniersCheminNiveaux = visualiseurArbre.aplatirChemin(resultat.getCheminArbre());
    }

    public void chargerDernierePrediction(Model uiModel) {
        if (dernierePrediction == null) {
            return;
        }
        uiModel.addAttribute("valeursSaisies", dernieresValeurs);
        uiModel.addAttribute("classe", dernierePrediction.getClasse());
        uiModel.addAttribute("cheminArbre", dernierePrediction.getCheminArbre());
        uiModel.addAttribute("cheminNiveaux", derniersCheminNiveaux);
    }

    private String nettoyerClasse(String classe) {
        if (classe == null || classe.isBlank()) {
            return "inconnu";
        }
        return NormaliseurClasse.normaliser(classe);
    }

    public VisualiseurArbre.NoeudApercu getApercuArbreComplet() {
        return getApercuArbre();
    }

    public VisualiseurArbre.NoeudApercu getApercuArbre() {
        verifierEntrainement();
        ArbreDecisionID3 arbre = apprentissageService.getArbre();
        return visualiseurArbre.extraireApercu(arbre.getRacine(), arbre.getSchema());
    }

    public VisualiseurArbre.NoeudApercu getApercuArbreRapport() {
        verifierEntrainement();
        ArbreDecisionID3 arbre = apprentissageService.getArbre();
        return visualiseurArbre.extraireApercuLimite(arbre.getRacine(), arbre.getSchema(), 3);
    }

    public String getArbreHtmlCouleur() {
        verifierEntrainement();
        return visualiseurArbre.formaterArbreHtmlCouleur(getApercuArbre());
    }

    public List<String> getFeuillesApercu() {
        verifierEntrainement();
        return visualiseurArbre.collecterFeuillesDepuisNoeud(apprentissageService.getArbre().getRacine());
    }

    private void verifierEntrainement() {
        if (!apprentissageService.estEntraine()) {
            throw new IllegalStateException("Veuillez d'abord construire l'arbre.");
        }
    }
}
