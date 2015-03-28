/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package backend;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 *
 * @author Adrian
 */



public class Server
{
    static final int PORT = 6969;
    static ServerSocket srvSock;

    static class InfoAboutClient
    {
        Socket socket;
        PrintWriter output;
        public String username;

        public InfoAboutClient (Socket socket)
        {
            this.socket = socket;
            try {
                output = new PrintWriter (socket.getOutputStream (), true);
            } catch (IOException e) {
                System.out.println ("There's something wrong with the client's output stream. "
                                  + "Please check again and come back!");
            }

            username = "";
        }
    }
    static List<InfoAboutClient> clients;

    public static void main (String[] args)
    {
        clients = new ArrayList<> ();

        try {
            srvSock = new ServerSocket (PORT);
        } catch (IOException e) {
            System.out.println ("Port " + PORT +" seems busy, "
                              + "please try again later ");
        }

        System.out.println ("==> Server is now ready!");
        for(;;) { 
            Socket socketForNewClient;
            try {
                socketForNewClient = srvSock.accept ();
                addNewClientToChat (socketForNewClient);
            } catch (IOException e) {
                System.out.println ("Client couldn't connect."
                                  + " Please try again later");
            }
        }

    }

    public static void onChatText (String sender, String message)
    {
        String toSend = sender + ": " + message;
        System.out.println ("On Chat Text '" + toSend + "'");
        clients.forEach (client -> client.output.println (toSend));
    }

    public static void sayToAll (String message)
    {
        clients.forEach (client -> client.output.println ("==> " + message));
    }

    public static void addNewClientToChat (Socket socket)
    {
        InfoAboutClient newClient = new InfoAboutClient (socket);

        System.out.println ("==> New client accepted");
        clients.add (newClient);

        new ThreadsThings (newClient).start ();
    }

    public static void removeClientFromChat (InfoAboutClient client)
    {
        sayToAll (client.username + " disconnected");
        clients.remove (client);
    }
}

class ThreadsThings extends Thread
{
    Server.InfoAboutClient clientInfo;
    Scanner networkInput;

    public ThreadsThings (Server.InfoAboutClient client)
    {
        this.clientInfo = client;
    }

    @Override
    public void run ()
    {
        try {
            networkInput = new Scanner (clientInfo.socket.getInputStream ());
        } catch (IOException e) {
            System.out.println ("Could not get input from client");
        }

       for(;;) {

            while (!networkInput.hasNextLine ())
                try {
                    sleep (10);
                } catch (InterruptedException ie) {
                    ie.printStackTrace ();
                }
            String message = networkInput.nextLine ();

            if (message.equals ("PLEASEDISCONNECTME")) {
                try {
                    clientInfo.socket.close ();
                    Server.removeClientFromChat (clientInfo);
                    System.out.println ("==> " + clientInfo.username + " disconnected");
                } catch (IOException ioe) {
                    System.out.println ("There was a problem disconnecting the client. "
                                      + "Please try again in a few seconds. ");
                }
                break;
            }

            else {
                if (clientInfo.username.isEmpty ()) {
                    clientInfo.username = message;
                    System.out.println ("==> " + message + " authenticated");
                    Server.sayToAll (message + " connected");
                }

                else
                    Server.onChatText (clientInfo.username, message);
            }

        }
    }
}
