package ma.ump.fso.datamining.donnees;

import java.util.List;

/**
 * Definition du jeu de donnees Nursery (UCI Machine Learning Repository).
 * 8 attributs categoriels, 5 classes cibles, toutes les valeurs sont des chaines de caracteres.
 * Fichiers bruts dans le dossier {@code donnees/} a la racine du projet.
 */
public final class SchemaNursery {
    private static final List<String> CLASSES = List.of(
            "not_recom", "recommend", "very_recom", "priority", "spec_prior"
    );

    private static final SchemaDonnees SCHEMA = new SchemaDonnees(
            List.of(
                    "parents", "has_nurs", "form", "children",
                    "housing", "finance", "social", "health"
            ),
            List.of(
                    List.of("usual", "pretentious", "great_pret"),
                    List.of("proper", "less_proper", "improper", "critical", "very_crit"),
                    List.of("complete", "completed", "incomplete", "foster"),
                    List.of("1", "2", "3", "more"),
                    List.of("convenient", "less_conv", "critical"),
                    List.of("convenient", "inconv"),
                    List.of("nonprob", "slightly_prob", "problematic"),
                    List.of("recommended", "priority", "not_recom")
            )
    );

    private SchemaNursery() {
    }

    public static SchemaDonnees getSchema() {
        return SCHEMA;
    }

    public static List<String> getClasses() {
        return CLASSES;
    }
}
