/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action;

import static com.jcraft.jsch.Logger.DEBUG;
import static com.jcraft.jsch.Logger.ERROR;
import static com.jcraft.jsch.Logger.FATAL;
import static com.jcraft.jsch.Logger.WARN;

/**
 *
 * @author BOUIKS
 */
public class JSCHLogger implements com.jcraft.jsch.Logger {

    static java.util.Hashtable name = new java.util.Hashtable();

    static {
        name.put(new Integer(DEBUG), "DEBUG: ");
        name.put(new Integer(INFO), "INFO: ");
        name.put(new Integer(WARN), "WARN: ");
        name.put(new Integer(ERROR), "ERROR: ");
        name.put(new Integer(FATAL), "FATAL: ");
    }

    @Override
    public boolean isEnabled(int i) {
        return true;
    }

    @Override
    public void log(int level, String message) {
        System.err.print(name.get(level));
        System.err.println(message);
    }

}
