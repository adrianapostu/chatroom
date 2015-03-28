/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package backend;

/**
 *
 * @author Adrian
 */

import static backend.Client.clientFrame;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import java.net.*;
import java.awt.*;
import java.io.*;


public class Client
{
    static final int PORT = 6969;
    static InetAddress host;
    static Socket socket = null;

    static ClientFrame clientFrame;
    static ClientListener clientListener;
    static Scanner networkInput;
    static PrintWriter networkOutput;
    static NetworkReceiverThread receiverThread;

    public static boolean connected = false;

    public static void main (String[] args)
    {
        clientListener = new ClientListener ();
        clientFrame = new ClientFrame (clientListener);
        clientFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
               int reply = JOptionPane.showConfirmDialog(clientFrame,
                             "Really Quit ?", "Quit", JOptionPane.YES_NO_OPTION);
               if (reply == JOptionPane.YES_OPTION)
                     System.exit(0);
        }
    });
    clientFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    }

    public static boolean connect (String username)
    {
        try {
            host = InetAddress.getLocalHost ();
        } catch (UnknownHostException e) {
            System.out.println ("Local Adress not reachable");
            
            return false;
        }
        try {
            socket = new Socket (host, PORT);
        } catch (IOException e) {
            System.out.println ("Server not reachable");
            
            return false;
        }

        try {
            networkInput = new Scanner (socket.getInputStream ());
            networkOutput = new PrintWriter (socket.getOutputStream (), true);

        } catch (IOException e) {
            System.out.println ("Input / Output stream not working properly");
            
        }
        receiverThread = new NetworkReceiverThread ();
        receiverThread.start ();
        networkOutput.println (username);
        clientFrame.connectedView ();
        clientFrame.responseMessages.append ("==> Hi " + username + "!\n");
        return connected = true;
    }

    public static void send (String message)
    {
        networkOutput.println (message);
    }

    public static boolean disconnect ()
    {
        networkOutput.println ("PLEASEDISCONNECTME");
        try {
            socket.close ();
            System.out.println ("Closing connection");
        } catch (IOException e) {
            System.out.println ("Could not disconnect!");
            
            return false;
        }
        clientFrame.disconnectedView ();
        receiverThread.shouldListen = false;

        connected = false;
        return true;
    }
}

class NetworkReceiverThread extends Thread
{
    public boolean shouldListen = true;

    @Override
    public void run ()
    {
        String response;
        Scanner input = null;
        try {
            input = new Scanner (Client.socket.getInputStream ());
        } catch (IOException e) {
            
        }

        do {
            while (!(input != null && input.hasNext ()) && shouldListen)
                try {
                    sleep (10);
                } catch (InterruptedException e) {
                    
                }

            try {
                response = Client.networkInput.nextLine ();
                Client.clientFrame.responseMessages.append (response + "\n");
            } catch (NoSuchElementException nsee) { }
        } while (Client.connected);

    }
}

class ClientListener implements ActionListener
{
    @Override
    public void actionPerformed (ActionEvent event)
    {
        if (event.getSource () == Client.clientFrame.buttonConnection) {

            if (!Client.connected) {
                String username = Client.clientFrame.userInput.getText ();
                Client.connect (username);
            }

            else
                Client.disconnect ();
        }
        else if (event.getSource () == Client.clientFrame.buttonSend) {
            String message = Client.clientFrame.userInput.getText ();
            if (!message.isEmpty ()) {
                Client.send (message);
                Client.clientFrame.userInput.setText ("");
            }
        }
    }
}

class ClientFrame extends JFrame
{
    JTextField userInput;
    JTextArea responseMessages;
    JPanel buttonsPanel;
    JButton buttonConnection;
    JButton buttonSend;

    public ClientFrame (ActionListener clientListener)
    {
        setSize (100,100);
        setVisible (true);
        setTitle ("ChatRoom");
        userInput = new JTextField ("user input", 20);
        add (userInput, BorderLayout.CENTER);
        responseMessages = new JTextArea (30, 35);
        responseMessages.setWrapStyleWord (true);
        responseMessages.setLineWrap (true);
        add (responseMessages, BorderLayout.NORTH);
        buttonConnection = new JButton ("Connection");
        buttonSend = new JButton ("Send");
        buttonConnection.addActionListener(clientListener);
        buttonSend.addActionListener(clientListener);
        buttonsPanel = new JPanel ();
        buttonsPanel.add (buttonConnection);
        buttonsPanel.add (buttonSend);
        add (buttonsPanel, BorderLayout.NORTH);
        pack ();
        disconnectedView ();
    }

    public void connectedView ()
    {
        pack ();
        userInput.setText ("Enter message to send");
        userInput.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e){
                userInput.setText("");
            }
        });
        responseMessages.setText ("");
        add (responseMessages, BorderLayout.NORTH);
        buttonConnection.setText ("Disconnect");
        remove (buttonsPanel);
        buttonsPanel.add (buttonSend);
        add (buttonsPanel, BorderLayout.SOUTH);
        pack ();
    }

    public void disconnectedView ()
    {
        userInput.setText ("Enter username");
        userInput.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e){
                userInput.setText("");
            }
        });
     
        remove (responseMessages);
        buttonConnection.setText ("Connect");
        remove (buttonsPanel);
        buttonsPanel.remove (buttonSend);
        add (buttonsPanel, BorderLayout.SOUTH);
        pack ();
    }
}