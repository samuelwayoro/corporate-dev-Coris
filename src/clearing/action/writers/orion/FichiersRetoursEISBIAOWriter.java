/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.orion;

import clearing.action.writers.*;
import org.patware.action.file.FlatFileWriter;

/**
 *
 * @author Patrick
 */
public class FichiersRetoursEISBIAOWriter extends FlatFileWriter {

    public FichiersRetoursEISBIAOWriter() {
        setDescription("Envoi des Fichiers retour vers le SIB ");
    }

    @Override
    public void execute() throws Exception {
        super.execute();
        
        new ChequeRetourEISSitWriter().execute();
        new EffetRetourSitWriter().execute();
        new VirementRetourSitWriter().execute();
        new ChequeRejeteSitWriter().execute();

    }

   
}
