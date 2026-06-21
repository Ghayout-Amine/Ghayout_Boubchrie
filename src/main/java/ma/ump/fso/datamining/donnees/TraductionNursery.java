package ma.ump.fso.datamining.donnees;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class TraductionNursery {

    private static final Map<String, String> ATTRIBUTS = Map.of(
            "parents", "Profession des parents",
            "has_nurs", "Creche de l'enfant",
            "form", "Type de famille",
            "children", "Nombre d'enfants",
            "housing", "Logement",
            "finance", "Situation financiere",
            "social", "Conditions sociales",
            "health", "Sante"
    );

    private static final Map<String, Map<String, String>> MODALITES = Map.of(
            "parents", Map.of(
                    "usual", "Ordinaire",
                    "pretentious", "Pretentieux",
                    "great_pret", "Tres pretentieux"
            ),
            "has_nurs", Map.of(
                    "proper", "Convenable",
                    "less_proper", "Peu convenable",
                    "improper", "Inadaptee",
                    "critical", "Critique",
                    "very_crit", "Tres critique"
            ),
            "form", Map.of(
                    "complete", "Complete",
                    "completed", "Completee",
                    "incomplete", "Incomplete",
                    "foster", "Famille d'accueil"
            ),
            "children", Map.of(
                    "1", "1 enfant",
                    "2", "2 enfants",
                    "3", "3 enfants",
                    "more", "Plus de 3"
            ),
            "housing", Map.of(
                    "convenient", "Convenable",
                    "less_conv", "Peu convenable",
                    "critical", "Critique"
            ),
            "finance", Map.of(
                    "convenient", "Convenable",
                    "inconv", "Inconvenante"
            ),
            "social", Map.of(
                    "nonprob", "Sans probleme",
                    "slightly_prob", "Legerement problematique",
                    "problematic", "Problematique"
            ),
            "health", Map.of(
                    "recommended", "Recommandee",
                    "priority", "Prioritaire",
                    "not_recom", "Non recommandee"
            )
    );

    public String libelleAttribut(String code) {
        if (code == null) {
            return "";
        }
        return ATTRIBUTS.getOrDefault(code.toLowerCase(), code);
    }

    public String libelleModalite(String attribut, String modalite) {
        if (attribut == null || modalite == null) {
            return modalite != null ? modalite : "";
        }
        Map<String, String> carte = MODALITES.get(attribut.toLowerCase());
        if (carte == null) {
            return modalite;
        }
        return carte.getOrDefault(modalite.toLowerCase(), modalite);
    }

    public String libelleModalite(SchemaDonnees schema, int indexAttribut, int indexModalite) {
        String attribut = schema.getNomAttribut(indexAttribut);
        String modalite = schema.getLibelleModalite(indexAttribut, indexModalite);
        return libelleModalite(attribut, modalite);
    }

    public String libelleClasse(String code) {
        if (code == null || code.isBlank()) {
            return "Inconnu";
        }
        return switch (code.toLowerCase()) {
            case "not_recom" -> "Non recommande";
            case "recommend" -> "Recommande";
            case "very_recom" -> "Tres recommande";
            case "priority" -> "Priorite";
            case "spec_prior" -> "Priorite speciale";
            case "sous-arbre" -> "Sous-arbre";
            case "inconnu" -> "Inconnu";
            default -> code;
        };
    }

    public static String libelleAttributStatique(String code) {
        return ATTRIBUTS.getOrDefault(code != null ? code.toLowerCase() : "", code);
    }

    public static String libelleModaliteStatique(String attribut, String modalite) {
        if (attribut == null || modalite == null) {
            return modalite;
        }
        Map<String, String> carte = MODALITES.get(attribut.toLowerCase());
        return carte != null ? carte.getOrDefault(modalite.toLowerCase(), modalite) : modalite;
    }

    public static String libelleClasseStatique(String code) {
        return new TraductionNursery().libelleClasse(code);
    }
}
