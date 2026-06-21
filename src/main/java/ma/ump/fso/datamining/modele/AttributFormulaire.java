package ma.ump.fso.datamining.modele;

import java.util.List;

public class AttributFormulaire {

    private final String nom;
    private final List<String> modalites;

    public AttributFormulaire(String nom, List<String> modalites) {
        this.nom = nom;
        this.modalites = List.copyOf(modalites);
    }

    public String getNom() {
        return nom;
    }

    public List<String> getModalites() {
        return modalites;
    }
}
