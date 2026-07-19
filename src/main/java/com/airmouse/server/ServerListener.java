package com.airmouse.server;

/**
 * Callback interface for Server → UI communication.
 * Allows the server to notify the UI of connection state changes.
 */
public interface ServerListener {

    void onServerStarted(String ip, int port);

    void onClientConnected(String clientAddress);

    void onClientDisconnected();
    
    void onLogMessage(String message);
}
