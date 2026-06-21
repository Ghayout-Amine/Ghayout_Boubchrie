package ma.ump.fso.datamining.controleur;

import ma.ump.fso.datamining.donnees.SchemaDonnees;
import ma.ump.fso.datamining.donnees.SchemaNursery;
import ma.ump.fso.datamining.modele.AttributFormulaire;
import ma.ump.fso.datamining.modele.ResultatPrediction;
import ma.ump.fso.datamining.service.ApprentissageService;
import ma.ump.fso.datamining.service.PredictionService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Controleur web : interface graphique du mini-projet (consigne de remise).
 * <p>
 * Parcours utilisateur :
 * <ul>
 *   <li>{@code /} — accueil et presentation</li>
 *   <li>{@code /apprentissage} — chargement CSV + construction de l'arbre ID3</li>
 *   <li>{@code /prediction} — saisie d'une candidature et affichage du chemin de decision</li>
 *   <li>{@code /evaluation} — metriques sur le jeu de test (exactitude, F1, matrice de confusion)</li>
 * </ul>
 */
@Controller
public class InterfacePrincipale {

    private final ApprentissageService apprentissageService;
    private final PredictionService predictionService;

    public InterfacePrincipale(ApprentissageService apprentissageService,
                               PredictionService predictionService) {
        this.apprentissageService = apprentissageService;
        this.predictionService = predictionService;
    }

    private void enrichirModeleCommun(Model model) {
        SchemaDonnees schema = apprentissageService.getSchema();
        model.addAttribute("schema", schema);
        model.addAttribute("attributsFormulaire", construireAttributsFormulaire(schema));
        model.addAttribute("classesNursery", SchemaNursery.getClasses());
        model.addAttribute("nbAttributs", schema.getNbAttributs());
        model.addAttribute("entraine", apprentissageService.estEntraine());
    }

    private List<AttributFormulaire> construireAttributsFormulaire(SchemaDonnees schema) {
        List<AttributFormulaire> attributs = new ArrayList<>();
        for (int i = 0; i < schema.getNbAttributs(); i++) {
            attributs.add(new AttributFormulaire(schema.getNomAttribut(i), schema.getModalites(i)));
        }
        return attributs;
    }

    @GetMapping("/")
    public String accueil(Model model) {
        enrichirModeleCommun(model);
        model.addAttribute("message", apprentissageService.getMessageEtat());
        return "index";
    }

    @GetMapping("/apprentissage")
    public String pageApprentissage(Model model) {
        enrichirModeleCommun(model);
        model.addAttribute("message", apprentissageService.getMessageEtat());
        if (apprentissageService.getInstancesEntrainement() != null) {
            model.addAttribute("nbEntrainement", apprentissageService.getInstancesEntrainement().size());
        }
        if (apprentissageService.getInstancesTest() != null) {
            model.addAttribute("nbTest", apprentissageService.getInstancesTest().size());
        }
        if (apprentissageService.estEntraine()) {
            try {
                model.addAttribute("apercuArbre", predictionService.getApercuArbreComplet());
                model.addAttribute("feuillesApercu", predictionService.getFeuillesApercu());
            } catch (Exception ignored) {
                // pas d'apercu si l'arbre n'est pas disponible
            }
        }
        return "apprentissage";
    }

    @PostMapping("/charger-defaut")
    public String chargerDefaut(RedirectAttributes redirectAttributes) {
        try {
            apprentissageService.chargerDonneesParDefaut();
            redirectAttributes.addFlashAttribute("succes", apprentissageService.getMessageEtat());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erreur", "Erreur chargement : " + e.getMessage());
        }
        return "redirect:/apprentissage";
    }

    @PostMapping("/charger-entrainement")
    public String chargerEntrainement(@RequestParam("fichier") MultipartFile fichier,
                                      RedirectAttributes redirectAttributes) {
        try {
            apprentissageService.chargerEntrainement(fichier);
            redirectAttributes.addFlashAttribute("succes", apprentissageService.getMessageEtat());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erreur", "Erreur chargement : " + e.getMessage());
        }
        return "redirect:/apprentissage";
    }

    @PostMapping("/charger-test")
    public String chargerTest(@RequestParam("fichier") MultipartFile fichier,
                              RedirectAttributes redirectAttributes) {
        try {
            apprentissageService.chargerTest(fichier);
            redirectAttributes.addFlashAttribute("succes", apprentissageService.getMessageEtat());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erreur", "Erreur chargement : " + e.getMessage());
        }
        return "redirect:/apprentissage";
    }

    @PostMapping("/entrainer")
    public String entrainer(RedirectAttributes redirectAttributes) {
        try {
            apprentissageService.entrainer();
            redirectAttributes.addFlashAttribute("succes", apprentissageService.getMessageEtat());
        } catch (Exception e) {
            String message = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            if (apprentissageService.estEntraine()) {
                redirectAttributes.addFlashAttribute("erreur",
                        "Erreur apprentissage : " + message
                                + " — l'arbre precedent est conserve.");
            } else {
                redirectAttributes.addFlashAttribute("erreur", "Erreur apprentissage : " + message);
            }
        }
        return "redirect:/apprentissage";
    }

    @GetMapping("/prediction")
    public String pagePrediction(Model model) {
        enrichirModeleCommun(model);
        predictionService.chargerDernierePrediction(model);
        return "prediction";
    }

    @PostMapping("/predire")
    public String predire(@RequestParam Map<String, String> params, RedirectAttributes redirectAttributes) {
        SchemaDonnees schema = apprentissageService.getSchema();
        Map<String, String> valeurs = new LinkedHashMap<>();
        for (String nom : schema.getNomsAttributs()) {
            valeurs.put(nom, params.get(nom));
        }
        try {
            ResultatPrediction resultat = predictionService.predireComplet(valeurs);
            predictionService.enregistrerDernierePrediction(valeurs, resultat);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erreur", e.getMessage());
            redirectAttributes.addFlashAttribute("valeursSaisies", valeurs);
        }
        return "redirect:/prediction";
    }

    @GetMapping(value = "/export/arbre-visual", produces = "text/html;charset=UTF-8")
    public ResponseEntity<String> exporterArbreVisuel() {
        if (!apprentissageService.estEntraine()) {
            return ResponseEntity.ok()
                    .contentType(new MediaType("text", "html", java.nio.charset.StandardCharsets.UTF_8))
                    .body("""
                            <!DOCTYPE html><html lang="fr"><head><meta charset="UTF-8"><title>Export arbre</title></head>
                            <body style="font-family:sans-serif;padding:2rem">
                            <h2>Arbre non disponible</h2>
                            <p>Construisez d'abord l'arbre sur <a href="/apprentissage">/apprentissage</a></p>
                            </body></html>
                            """);
        }
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "html", java.nio.charset.StandardCharsets.UTF_8))
                .body(predictionService.getArbreHtmlCouleur());
    }

    @GetMapping("/export/arbre.html")
    public ResponseEntity<String> telechargerArbreHtml() {
        if (!apprentissageService.estEntraine()) {
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Construisez d'abord l'arbre sur /apprentissage");
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"arbre_id3_couleur.html\"")
                .contentType(new MediaType("text", "html", java.nio.charset.StandardCharsets.UTF_8))
                .body(predictionService.getArbreHtmlCouleur());
    }

    @GetMapping("/evaluation")
    public String pageEvaluation(Model model) {
        enrichirModeleCommun(model);
        model.addAttribute("evaluation", apprentissageService.getDerniereEvaluation());
        if (apprentissageService.getInstancesTest() != null) {
            model.addAttribute("nbTest", apprentissageService.getInstancesTest().size());
        }
        return "evaluation";
    }

    @PostMapping("/evaluer")
    public String evaluer(RedirectAttributes redirectAttributes) {
        try {
            apprentissageService.evaluerSurTest();
            redirectAttributes.addFlashAttribute("succes", apprentissageService.getMessageEtat());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/evaluation";
    }
}
