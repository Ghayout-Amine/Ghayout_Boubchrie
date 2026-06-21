package ma.ump.fso.datamining.util;

import java.util.Locale;

public final class NormaliseurClasse {

    private NormaliseurClasse() {
    }

    public static String normaliser(String classe) {
        if (classe == null) {
            return "";
        }
        return classe.replace("\"", "").trim().toLowerCase(Locale.ROOT);
    }

    public static boolean estInconnue(String classe) {
        return "inconnu".equals(normaliser(classe));
    }
}
