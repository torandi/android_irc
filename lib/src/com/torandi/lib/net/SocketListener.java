package com.torandi.lib.net;

public interface SocketListener {
    public void dataRecived(String data, Socket sck);
    /**
     * Called when a new client connects. The client is already accepted when
     * this method is called.
     * @param client
     * @param srvr
     */
    public void newClient(Socket client, ServerSocket srvr);
    public void connectionClosed(Socket sck);
}
