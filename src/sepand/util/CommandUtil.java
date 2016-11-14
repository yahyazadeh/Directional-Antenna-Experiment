/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sepand.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 *
 * @author daniel
 */
public class CommandUtil {

    public String executeCommand(String path, String command, boolean su, String password) {

        StringBuffer output = new StringBuffer();
        String sudo = "";
        if (su) {
            sudo = "echo '" + password + "' | sudo -kS ";
        }
        String[] cmd = {"/bin/sh", "-c", "cd " + path + " ; " + sudo + command};

        Process p;
        try {
            p = Runtime.getRuntime().exec(cmd);
            p.waitFor();
            BufferedReader reader
                    = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line = "";
            while ((line = reader.readLine()) != null) {
                output.append(line + "\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return output.toString();

    }
}
