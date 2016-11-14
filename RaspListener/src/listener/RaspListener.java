/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package listener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import org.apache.log4j.Logger;

/**
 *
 * @author Daniel
 */
public class RaspListener {

    final static Logger logger = Logger.getLogger(RaspListener.class);

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        if (args.length != 2) {
            System.err.println("Usage: java -jar RaspListener.jar <PortNumber> <FilePath>");
            System.exit(1);
        }

        final int myPort = Integer.parseInt(args[0]);
        try {
            ServerSocket ssock = new ServerSocket(myPort);
            logger.info("Port " + myPort + " has been opened.");

            while (true) {
                Socket sock = ssock.accept();
                logger.info("Client" + sock.getInetAddress() + " has made socket connection to the " + myPort + " port");

                byte[] buf = new byte[4096];
                InputStream ins = sock.getInputStream();

                File file = new File(args[1]);
                if (!file.exists()) {
                    file.createNewFile();
                    logger.info("New file <" + args[1] + "> has been created.");
                }
                FileOutputStream fos = new FileOutputStream(file, true);
                logger.info("File <" + args[1] + "> has been opend.");

                int c;
                while ((c = ins.read(buf)) >= 0) {
                    if (c > 0) {
                        fos.write(buf, 0, c);
                    }
                }
                fos.flush();
                fos.close();
                logger.info("File <" + args[1] + "> has been closed.");

                sock.close();
                logger.info("Client" + sock.getInetAddress() + "'s connection on the " + myPort + " port has been closed.");
            }
        } catch (BindException ex) {
            logger.error(ex);
        } catch (Exception ex) {
            logger.error("An exception occured when trying to listen on " + myPort + " port: " + ex);
        }
    }

}
