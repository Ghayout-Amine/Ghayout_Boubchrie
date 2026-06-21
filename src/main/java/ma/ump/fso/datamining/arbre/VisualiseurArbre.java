package ma.ump.fso.datamining.arbre;

import ma.ump.fso.datamining.donnees.SchemaDonnees;
import ma.ump.fso.datamining.modele.EtapeArbreVisuel;
import ma.ump.fso.datamining.modele.NiveauChemin;
import ma.ump.fso.datamining.modele.Noeud;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Visualisation de l'arbre ID3 : apercu interactif dans l'interface et export HTML colore.
 * Utilise par les pages Apprentissage et Prediction pour expliquer les decisions du modele.
 */
public class VisualiseurArbre {

    public EtapeArbreVisuel extraireCheminDepuisVecteur(int[] vecteur, ArbreDecisionID3 arbre, SchemaDonnees schema) {
        return extraireChemin(vecteur, arbre, schema);
    }

    public EtapeArbreVisuel extraireChemin(int[] vecteur, ArbreDecisionID3 arbre, SchemaDonnees schema) {
        if (!arbre.estEntraine()) {
            return null;
        }

        Noeud courant = arbre.getRacine();

        List<String> attributs = new ArrayList<>();
        List<String> valeurs = new ArrayList<>();

        while (courant != null && !courant.estFeuille()) {
            int valeur = vecteur[courant.getIndexAttribut()];
            attributs.add(courant.getNomAttribut());
            valeurs.add(schema.getLibelleModalite(courant.getIndexAttribut(), valeur));
            courant = courant.getEnfant(valeur);
        }

        String classeFeuille = courant != null && courant.estFeuille()
                ? nettoyerClasse(courant.getEtiquetteFeuille())
                : "inconnu";

        EtapeArbreVisuel resultat = EtapeArbreVisuel.feuille(classeFeuille);
        for (int i = attributs.size() - 1; i >= 0; i--) {
            resultat = EtapeArbreVisuel.interne(attributs.get(i), valeurs.get(i), resultat);
        }
        return resultat;
    }

    public List<NiveauChemin> aplatirChemin(EtapeArbreVisuel etape) {
        List<NiveauChemin> niveaux = new ArrayList<>();
        EtapeArbreVisuel courant = etape;
        while (courant != null && !courant.estFeuille()) {
            niveaux.add(new NiveauChemin(courant.getAttribut(), courant.getValeurAttribut()));
            courant = courant.getSuite();
        }
        return niveaux;
    }

    public List<String> collecterFeuillesDepuisNoeud(Noeud noeud) {
        return collecterFeuilles(noeud);
    }

    public List<String> collecterFeuilles(Noeud noeud) {
        Set<String> feuilles = new LinkedHashSet<>();
        collecterFeuillesRec(noeud, feuilles);
        return new ArrayList<>(feuilles);
    }

    private void collecterFeuillesRec(Noeud noeud, Set<String> feuilles) {
        if (noeud == null) {
            return;
        }
        if (noeud.estFeuille()) {
            feuilles.add(nettoyerClasse(noeud.getEtiquetteFeuille()));
            return;
        }
        for (int valeur = 0; valeur < noeud.getNbValeurs(); valeur++) {
            collecterFeuillesRec(noeud.getEnfant(valeur), feuilles);
        }
    }

    public NoeudApercu extraireApercu(Noeud noeud, SchemaDonnees schema) {
        return extraireApercuRecursif(noeud, schema, 0, Integer.MAX_VALUE);
    }

    /**
     * Apercu limite en profondeur pour captures rapport (ex. 3 niveaux visibles).
     */
    public NoeudApercu extraireApercuLimite(Noeud noeud, SchemaDonnees schema, int profondeurMax) {
        return extraireApercuRecursif(noeud, schema, 0, profondeurMax);
    }

    public String formaterArbreHtmlCouleur(NoeudApercu racine) {
        if (racine == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html lang=\"fr\"><head><meta charset=\"UTF-8\">");
        sb.append("<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">");
        sb.append("<title>Arbre ID3 — Ghayout &amp; Boubechrie</title>");
        sb.append("<style>").append(STYLES_ARBRE_EXPORT).append("</style></head><body>");
        sb.append("<div class=\"page-export\">");
        sb.append("<header class=\"entete-export\">");
        sb.append("<h1>Arbre de decision ID3</h1>");
        sb.append("<p class=\"sous-titre\">Dataset Nursery (UCI) — arbre complet (jusqu'a 10 niveaux)</p>");
        sb.append("<p class=\"legende\">Noeud gris = attribut · Branches rouges = modalite · Feuilles = classes colorees</p>");
        sb.append("<div class=\"badges-legende\">");
        sb.append(badgeLegende("not_recom")).append(badgeLegende("recommend")).append(badgeLegende("very_recom"));
        sb.append(badgeLegende("priority")).append(badgeLegende("spec_prior"));
        sb.append("</div></header>");
        sb.append("<div class=\"arbre-zoom-panel\" id=\"zoomPanel\" data-zoom-default=\"1\">");
        sb.append("<div class=\"arbre-zoom-toolbar\">");
        sb.append("<button type=\"button\" class=\"btn-zoom\" id=\"btnOut\" title=\"Reduire\">&minus;</button>");
        sb.append("<span class=\"arbre-zoom-label\" id=\"zoomLabel\">100%</span>");
        sb.append("<button type=\"button\" class=\"btn-zoom\" id=\"btnIn\" title=\"Agrandir\">+</button>");
        sb.append("<button type=\"button\" class=\"btn-zoom btn-zoom-reset\" id=\"btnReset\" title=\"Taille par defaut\">Defaut</button>");
        sb.append("</div>");
        sb.append("<div class=\"zone-scroll\" id=\"conteneur\"><div class=\"arbre-zoom-canvas\" id=\"canvas\"><div class=\"arbre-cours-diagram\">");
        genererNoeudHtmlRec(racine, true, sb);
        sb.append("</div></div></div></div></div>");
        sb.append("<script>").append(SCRIPT_ZOOM_EXPORT).append("</script>");
        sb.append("</body></html>");
        return sb.toString();
    }

    private void genererNoeudHtmlRec(NoeudApercu noeud, boolean estRacine, StringBuilder sb) {
        if (noeud == null) {
            return;
        }
        sb.append("<div class=\"tree-cell\">");
        if ("feuille".equals(noeud.getType())) {
            sb.append("<div class=\"tree-leaf leaf-").append(echapperHtml(noeud.getLibelle())).append("\">")
                    .append(echapperHtml(noeud.getLibelle())).append("</div>");
        } else {
            sb.append("<div class=\"tree-interne\">");
            sb.append("<div class=\"tree-node").append(estRacine ? " racine" : "").append("\">")
                    .append(echapperHtml(noeud.getLibelle())).append("</div>");
            sb.append("<div class=\"tree-stem\"></div>");
            sb.append("<div class=\"tree-fork\">");
            for (BrancheApercu branche : noeud.getBranches()) {
                if (branche.getEnfant() == null) {
                    continue;
                }
                sb.append("<div class=\"tree-branch\">");
                sb.append("<div class=\"tree-branch-stem\"></div>");
                sb.append("<span class=\"branch-name\">").append(echapperHtml(branche.getValeur())).append("</span>");
                sb.append("<div class=\"branch-line\"></div>");
                genererNoeudHtmlRec(branche.getEnfant(), false, sb);
                sb.append("</div>");
            }
            sb.append("</div></div>");
        }
        sb.append("</div>");
    }

    private String echapperHtml(String texte) {
        if (texte == null) {
            return "";
        }
        return texte.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private String badgeLegende(String cls) {
        return "<span class=\"tree-leaf leaf-" + echapperHtml(cls) + "\">" + echapperHtml(cls) + "</span>";
    }

    private static final String STYLES_ARBRE_EXPORT = """
            *{box-sizing:border-box}
            body{font-family:Segoe UI,Arial,sans-serif;margin:0;background:#f3f4f6;color:#111}
            .page-export{max-width:100%;padding:1.5rem}
            .entete-export{text-align:center;margin-bottom:1.25rem;background:#fff;border:1px solid #e5e7eb;border-radius:8px;padding:1.25rem 1rem}
            h1{font-size:1.35rem;margin:0 0 0.35rem}
            .sous-titre{margin:0 0 0.5rem;color:#4b5563;font-size:0.95rem}
            .legende{font-size:0.88rem;color:#6b7280;margin:0 0 0.75rem}
            .badges-legende{display:flex;flex-wrap:wrap;justify-content:center;gap:0.4rem}
            .arbre-zoom-panel{background:#fff;border:1px solid #e5e7eb;border-radius:8px;padding:1rem}
            .arbre-zoom-toolbar{display:flex;align-items:center;justify-content:center;gap:0.5rem;margin-bottom:0.75rem;flex-wrap:wrap}
            .btn-zoom{background:#e5e7eb;color:#1f2937;border:1px solid #d1d5db;padding:0.35rem 0.85rem;border-radius:4px;cursor:pointer;font-weight:bold;font-size:1rem;line-height:1.2}
            .btn-zoom:hover{background:#d1d5db}
            .btn-zoom-reset{font-size:0.85rem;font-weight:600}
            .arbre-zoom-label{min-width:3.5rem;text-align:center;font-weight:bold;color:#1e3a5f}
            .zone-scroll{overflow:auto;max-height:85vh;text-align:center}
            .arbre-zoom-canvas{display:inline-block;transform:none;transform-origin:top center;padding:1.5rem 2rem 2rem}
            .arbre-cours-diagram{display:inline-block;text-align:center}
            .tree-cell,.tree-interne{display:flex;flex-direction:column;align-items:center;width:max-content;margin:0 auto}
            .tree-node{background:#6b7280;color:#fff;padding:0.5rem 0.85rem;border-radius:6px;font-weight:bold;font-size:0.85rem;max-width:11rem;white-space:normal;text-align:center;line-height:1.25}
            .tree-node.racine{background:#4b5563;font-size:1rem}
            .tree-stem,.tree-branch-stem,.branch-line{width:2px;height:22px;background:#2563eb}
            .tree-fork{display:flex;justify-content:center;gap:2.5rem;border-top:2px solid #2563eb;width:max-content;margin:0 auto;padding-top:0}
            .tree-branch-stem{margin-top:-2px}
            .tree-branch{display:flex;flex-direction:column;align-items:center;padding:0 0.75rem}
            .branch-name{color:#dc2626;font-weight:bold;font-size:0.85rem;margin-bottom:0.25rem;text-transform:lowercase}
            .tree-leaf{font-weight:bold;padding:0.45rem 0.85rem;border-radius:6px;border:2px solid transparent;text-transform:uppercase;font-size:0.85rem;white-space:nowrap}
            .leaf-not_recom{background:#fee2e2;color:#b91c1c;border-color:#fca5a5}
            .leaf-recommend{background:#dbeafe;color:#1d4ed8;border-color:#93c5fd}
            .leaf-very_recom{background:#d1fae5;color:#047857;border-color:#6ee7b7}
            .leaf-priority{background:#fef3c7;color:#b45309;border-color:#fcd34d}
            .leaf-spec_prior{background:#ede9fe;color:#6d28d9;border-color:#c4b5fd}
            @media print{.page-export{padding:0}.zone-scroll{overflow:visible;border:none}.arbre-zoom-toolbar{display:none}}
            """;

    private static final String SCRIPT_ZOOM_EXPORT = """
            (function(){
              var canvas=document.getElementById('canvas');
              var conteneur=document.getElementById('conteneur');
              var label=document.getElementById('zoomLabel');
              var zoom=1, minZoom=0.15, maxZoom=1.5, pas=0.1;
              function appliquer(){canvas.style.zoom=zoom;label.textContent=Math.round(zoom*100)+'%';}
              function ajuster(){
                zoom=1; appliquer();
                var lA=canvas.scrollWidth, lZ=conteneur.clientWidth-48;
                if(lA>0&&lZ>0&&lA>lZ) zoom=Math.max(minZoom,Math.min(1,lZ/lA));
                appliquer();
              }
              document.getElementById('btnIn').onclick=function(){zoom=Math.min(maxZoom,+(zoom+pas).toFixed(2));appliquer();};
              document.getElementById('btnOut').onclick=function(){zoom=Math.max(minZoom,+(zoom-pas).toFixed(2));appliquer();};
              document.getElementById('btnReset').onclick=function(){ajuster();};
              requestAnimationFrame(function(){requestAnimationFrame(ajuster);});
            })();
            """;

    private NoeudApercu extraireApercuRecursif(Noeud noeud, SchemaDonnees schema, int profondeur, int profondeurMax) {
        if (noeud == null) {
            return null;
        }
        if (noeud.estFeuille()) {
            return NoeudApercu.feuille(nettoyerClasse(noeud.getEtiquetteFeuille()));
        }
        if (profondeur >= profondeurMax) {
            return NoeudApercu.feuille("sous-arbre");
        }

        NoeudApercu apercu = NoeudApercu.interne(noeud.getNomAttribut());
        for (int valeur = 0; valeur < noeud.getNbValeurs(); valeur++) {
            apercu.ajouterBranche(
                    schema.getLibelleModalite(noeud.getIndexAttribut(), valeur),
                    extraireApercuRecursif(noeud.getEnfant(valeur), schema, profondeur + 1, profondeurMax));
        }
        return apercu;
    }

    private String nettoyerClasse(String classe) {
        if (classe == null) {
            return "inconnu";
        }
        return classe.replace("\"", "").trim();
    }

    public static class BrancheApercu {
        private final String valeur;
        private final NoeudApercu enfant;

        public BrancheApercu(String valeur, NoeudApercu enfant) {
            this.valeur = valeur;
            this.enfant = enfant;
        }

        public String getValeur() {
            return valeur;
        }

        public NoeudApercu getEnfant() {
            return enfant;
        }
    }

    public static class NoeudApercu {
        private final String libelle;
        private final String type;
        private final List<BrancheApercu> branches = new ArrayList<>();

        private NoeudApercu(String libelle, String type) {
            this.libelle = libelle;
            this.type = type;
        }

        public static NoeudApercu interne(String attribut) {
            return new NoeudApercu(attribut, "interne");
        }

        public static NoeudApercu feuille(String classe) {
            return new NoeudApercu(classe, "feuille");
        }

        public void ajouterBranche(String valeur, NoeudApercu enfant) {
            branches.add(new BrancheApercu(valeur, enfant));
        }

        public String getLibelle() {
            return libelle;
        }

        public String getType() {
            return type;
        }

        public List<BrancheApercu> getBranches() {
            return branches;
        }
    }
}
