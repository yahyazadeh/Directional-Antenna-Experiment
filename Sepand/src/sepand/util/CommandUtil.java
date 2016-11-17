/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sepand.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.apache.log4j.Logger;

/**
 *
 * @author daniel
 */
public class CommandUtil {

    final static Logger logger = Logger.getLogger(CommandUtil.class);

    public String executeCommand(String path, String command, boolean su, String password) {

        StringBuffer output = new StringBuffer();        
        StringBuffer errorOutput = new StringBuffer();        
        String sudo = "";
        if (su) {
            sudo = "echo '" + password + "' | sudo -S ";
        }
        String[] cmd = {"/bin/sh", "-c", "cd '" + path + "' ; " + sudo + command};

        logger.info("Cmd: " + cmd[2]);

        Process p;
        try {
            p = Runtime.getRuntime().exec(cmd);
            p.waitFor();
            BufferedReader reader
                    = new BufferedReader(new InputStreamReader(p.getInputStream()));

            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

            String line = "";
            while ((line = reader.readLine()) != null) {
                output.append(line + "\n");
                logger.info("Output Line: " + line);
            }
            
            String s = "";
            while ((s = stdError.readLine()) != null) {
                errorOutput.append(s + "\n");
                logger.error("Error Line: " + s);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return output.append(errorOutput).toString();
        
    }
}
