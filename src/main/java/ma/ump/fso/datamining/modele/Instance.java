package ma.ump.fso.datamining.modele;

public class Instance {

    private final int[] valeursAttributs;
    private final String classe;

    public Instance(int[] valeursAttributs, String classe) {
        this.valeursAttributs = valeursAttributs;
        this.classe = classe;
    }

    public int[] getValeursAttributs() {
        return valeursAttributs;
    }

    public String getClasse() {
        return classe;
    }
}
