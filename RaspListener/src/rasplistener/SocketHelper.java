/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rasplistener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 *
 * @author Daniel
 */
public class SocketHelper {

    Socket sock;
    byte[] buf;
    InputStream ins;
    FileOutputStream fos;
    File file;

    SocketHelper(Socket sock) throws Exception {
        this.sock = sock;
        buf = new byte[4096];
        ins = sock.getInputStream();
    }

    public void wirteToFile(String filePath) throws Exception {
        file = new File(filePath);
        if (!file.exists()) {
            file.createNewFile();
        }
        fos = new FileOutputStream(file, true);

        int c;
        while ((c = ins.read(buf)) >= 0) {
            if (c > 0) {
                fos.write(buf, 0, c);
            }
        } 
        fos.flush();
        fos.close();
        System.out.println("Log:: Finished. Your file in the path " + filePath + " is now ready.");
    }
}
