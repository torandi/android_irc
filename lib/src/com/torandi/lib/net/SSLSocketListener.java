package com.torandi.lib.net;

import java.io.IOException;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;

public interface SSLSocketListener {
    public void dataRecived(String data, SSLSocket sck);
    /**
     * Called when a new client connects. The client is already accepted when
     * this method is called. Handshake is not done.
     * @param client
     * @param srvr
     */
    public void newClient(SSLSocket client, SSLServerSocket srvr);
    public void connectionClosed(SSLSocket sck, IOException e);
}
