/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sepand.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import org.apache.log4j.Logger;

/**
 *
 * @author daniel
 */
public class RaspListener {

    final static Logger logger = Logger.getLogger(RaspListener.class);
    private boolean done = false;

    public void start(int myPort, String filePath) {

        while (true & !done) {
            try {
                ServerSocket ssock = new ServerSocket(myPort);
                logger.info("Port " + myPort + " has been opened.");

                Socket sock = ssock.accept();
                logger.info("Client" + sock.getInetAddress() + " has made socket connection to the " + myPort + " port");

                byte[] buf = new byte[4096];
                InputStream ins = sock.getInputStream();

                File file = new File(filePath);
                if (!file.exists()) {
                    file.createNewFile();
                    logger.info("New file <" + filePath + "> has been created.");
                }
                FileOutputStream fos = new FileOutputStream(file, true);
                logger.info("File <" + filePath + "> has been opend.");

                int c;
                while ((c = ins.read(buf)) >= 0 & !done) {
                    if (c > 0) {
                        fos.write(buf, 0, c);
                    }
                }
                fos.flush();
                fos.close();
                logger.info("File <" + filePath + "> has been closed.");

                sock.close();
                logger.info("Client" + sock.getInetAddress() + "'s connection on the " + myPort + " port has been closed.");

            } catch (BindException ex) {
                logger.error(ex);
                done = true;
            } catch (SocketException ex) {
                logger.error("SocketException on " + myPort + " port: " + ex);
            } catch (Exception ex) {
                logger.error("An exception occured when trying to listen on " + myPort + " port: " + ex);
                done = true;
            }
        }
    }
    
    public void finish(boolean done) {
        this.done = done;
    }

}
