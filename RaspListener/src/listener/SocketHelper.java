/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package listener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.Socket;
import org.apache.log4j.Logger;

/**
 *
 * @author Daniel
 */
public class SocketHelper {
    
    final static Logger logger = Logger.getLogger(SocketHelper.class);

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
            logger.info("New file <" + filePath + "> has been created.");
        }
        fos = new FileOutputStream(file, true);
        logger.info("File <" + filePath + "> has been opend.");

        int c;
        while ((c = ins.read(buf)) >= 0) {
            if (c > 0) {
                fos.write(buf, 0, c);
            }
        } 
        fos.flush();
        fos.close();
        logger.info("File <" + filePath + "> has been closed.");
    }
}
