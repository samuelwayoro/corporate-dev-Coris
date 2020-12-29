/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.flexcube;

import org.patware.action.file.FlatFileWriter;
import org.patware.utils.Utility;

/**
 *
 * @author Patrick Augou
 */
public class ClearingFlexCubeEBJWriter extends FlatFileWriter {

    private CPTENVFlexCubeWriter cptenvfcw;
    private ChequeAllerFlexCubeEBJWriter cafcw; //DAY
    private ChequeAllerRejeteFlexCubeEBJWriter carfcw; //RJA
    private CPTANLFlexCubeWriter cptanlfcw;
//    private VirementRetourFlexCubeWriter vrfcw;
    private ChequeRetourFlexCubeWriter crfcw;
    private ChequeRetourRejeteFlexCubeWriter crrfcw;
    private VirementAllerFlexCubeWriter vafcw;
    private EffetRetourFlexCubeWriter erfcw;

    public ClearingFlexCubeEBJWriter() {
        setDescription("Generation des fichiers vers le SIB");

        cptenvfcw = new CPTENVFlexCubeWriter();
        cafcw = new ChequeAllerFlexCubeEBJWriter();
        carfcw = new ChequeAllerRejeteFlexCubeEBJWriter();
        cptanlfcw = new CPTANLFlexCubeWriter();
//        vrfcw = new VirementRetourFlexCubeWriter();

        crfcw = new ChequeRetourFlexCubeWriter();
        crrfcw = new ChequeRetourRejeteFlexCubeWriter();
        vafcw = new VirementAllerFlexCubeWriter();
        erfcw = new EffetRetourFlexCubeWriter();
    }

    @Override
    public void execute() throws Exception {
        super.execute();
        String dateValeur = Utility.getParam("DATEVALEUR_ALLER");
        String[] param1 = (String[]) getParametersMap().get("param1");
        if (param1 != null && param1.length > 0) {
            dateValeur = param1[0];
        }
        System.out.println("Date Valeur Credit = " + dateValeur);
        String dateCompensation = "";
        param1 = (String[]) getParametersMap().get("param2");
        if (param1 != null && param1.length > 0) {
            dateCompensation = param1[0];
        }
        System.out.println("Date Compensation = " + dateCompensation);

        

        cafcw.setParametersMap(getParametersMap());
        cafcw.setDescription("Generation Fichier DAY");
        cafcw.execute();
        setDescription(getDescription() + "\n" + cafcw.getDescription());

        carfcw.setParametersMap(getParametersMap());
        carfcw.setDescription("Generation Fichier RJA");
        carfcw.execute();
        setDescription(getDescription() + "\n" + carfcw.getDescription());

        cptanlfcw.setParametersMap(getParametersMap());
        cptanlfcw.setDescription("Generation Fichier CPTANL");
        cptanlfcw.execute();
        setDescription(getDescription() + "\n" + cptanlfcw.getDescription());

        cptenvfcw.setParametersMap(getParametersMap());
        cptenvfcw.setDescription("Generation Fichier ENV");
        cptenvfcw.execute();
        setDescription(getDescription() + "\n" + cptenvfcw.getDescription());

//        vrfcw.setParametersMap(getParametersMap());
//        vrfcw.setDescription("Generation Fichier VMTR");
//        vrfcw.execute();
//        setDescription(getDescription() + "\n"+ vrfcw.getDescription());

        dateValeur = Utility.getParam("DATEVALEUR_RETOUR");
        param1 = (String[]) getParametersMap().get("param3");
        if (param1 != null && param1.length > 0) {
            dateValeur = param1[0];
        }
        System.out.println("Date Valeur Debit = " + dateValeur);

        getParametersMap().put("param1", getParametersMap().get("param3"));

        crfcw.setParametersMap(getParametersMap());
        crfcw.setDescription("Generation Fichier RETBJ");
        crfcw.execute();
        setDescription(getDescription() + "\n" + crfcw.getDescription());

        crrfcw.setParametersMap(getParametersMap());
        crrfcw.setDescription("Generation Fichier RJTRET");
        crrfcw.execute();
        setDescription(getDescription() + "\n" + crrfcw.getDescription());

        vafcw.setParametersMap(getParametersMap());
        vafcw.setDescription("Generation Fichier VMTA");
        vafcw.execute();
        setDescription(getDescription() + "\n" + vafcw.getDescription());

        erfcw.setParametersMap(getParametersMap());
        erfcw.setDescription("Generation Fichier EFFT");
        erfcw.execute();
        setDescription(getDescription() + "\n" + erfcw.getDescription());



    }
}
