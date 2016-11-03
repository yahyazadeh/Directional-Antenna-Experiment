/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package listener;

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

        if (args[0].equals("-h")) {
            System.out.println("Usage: java -jar RaspListener.jar <PortNumber> <FilePath>");
            return;
        }

        final int myPort = Integer.valueOf(args[0]);
        ServerSocket ssock;
        try {
            ssock = new ServerSocket(myPort);
            logger.info("Port " + myPort + " has been opened.");

            while (true) {
                Socket sock = ssock.accept();
                logger.info("Someone has made socket connection.");

                SocketHelper sh = new SocketHelper(sock);
                sh.wirteToFile(args[1]);
                sock.close();
                logger.info("socket has been closed.");
            }
        } catch (BindException ex) {
            logger.error(ex);
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

}
