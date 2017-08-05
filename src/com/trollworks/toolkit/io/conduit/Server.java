/*
 * Copyright (c) 1998-2017 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, version 2.0. If a copy of the MPL was not distributed with
 * this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, version 2.0.
 */

package com.trollworks.toolkit.io.conduit;

import com.trollworks.toolkit.io.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

class Server extends Thread {
    private ServerSocket      mServerSocket;
    private ArrayList<Client> mClients;
    private Object            mSendLock;
    private int               mClientCounter;

    /**
     * Creates a new conduit message server.
     *
     * @param socketAddress The socket address to attach to.
     * @throws IOException if the server socket cannot be created.
     */
    Server(InetSocketAddress socketAddress) throws IOException {
        super(Conduit.class.getSimpleName() + '$' + Server.class.getSimpleName() + '@' + socketAddress);
        setPriority(NORM_PRIORITY);
        setDaemon(true);
        mServerSocket = new ServerSocket(socketAddress.getPort(), 0, socketAddress.getAddress());
        mClients = new ArrayList<>();
        mSendLock = new Object();
    }

    /** @return The next client counter. */
    int getNextClientCounter() {
        return ++mClientCounter;
    }

    /** @return The server socket. */
    ServerSocket getServerSocket() {
        return mServerSocket;
    }

    /** Handles accepting new incoming connections and starting client processors for each. */
    @Override
    public void run() {
        try {
            while (true) {
                @SuppressWarnings("resource")
                Socket socket = mServerSocket.accept();
                try {
                    Client client = new Client(this, socket);
                    client.setDaemon(true);
                    client.start();
                    synchronized (mClients) {
                        mClients.add(client);
                    }
                } catch (IOException ioe) {
                    // The client died an early death... ignore it.
                    socket.close();
                }
            }
        } catch (Exception exception) {
            shutdown();
        }
        Client[] clients;
        synchronized (mClients) {
            clients = mClients.toArray(new Client[0]);
            mClients.clear();
        }
        for (Client element : clients) {
            element.shutdown();
        }
    }

    /**
     * Removes a client from the list of clients being served.
     *
     * @param client The client to remove.
     */
    void remove(Client client) {
        synchronized (mClients) {
            mClients.remove(client);
        }
    }

    /**
     * Sends a message to all connected clients.
     *
     * @param msg The message to send.
     */
    void send(ConduitMessage msg) {
        Client[] clients;
        synchronized (mClients) {
            clients = mClients.toArray(new Client[0]);
        }
        synchronized (mSendLock) {
            for (Client element : clients) {
                element.send(msg);
            }
        }
    }

    /** Shuts down this communication server. */
    synchronized void shutdown() {
        try {
            mServerSocket.close();
        } catch (Exception exception) {
            Log.error(exception);
        }
    }

}
