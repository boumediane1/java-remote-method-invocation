package chat.client;

import chat.remote.RemoteClient;
import chat.remote.RemoteConnection;
import chat.remote.RemoteServer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ClientImpl extends UnicastRemoteObject implements RemoteClient {
  private RemoteServer chatServer;
  private RemoteConnection chatConnection;
  private String clientName;

  public ClientImpl(String serverUrl, String clientName)
      throws RemoteException, MalformedURLException, NotBoundException {

    chatServer = (RemoteServer) Naming.lookup(serverUrl);

    chatConnection = chatServer.logon(clientName, this);

    if (chatConnection == null) {
      throw new RemoteException("Cannot use given name");
    }

    this.clientName = clientName;
  }

  public static void main(String[] args) throws IOException, NotBoundException {
    if (args.length < 2) {
      System.out.println(
          "You must give the URL of the chat server and the name of the client as arguments");
      System.out.println("i.e. rmi://clio.unice.fr/Server bob");
      System.exit(1);
    }

    ClientImpl client = new ClientImpl(args[0], args[1]);
    client.startClient();
  }

  public void startClient() throws IOException {
    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

    while (true) {
      String message = in.readLine();
      if (message == null || message.isEmpty()) break;
      chatConnection.say(message);
    }

    chatConnection.logoff();
    System.exit(0);
  }

  @Override
  public void speak(String from, String message) throws RemoteException {
    System.out.println(from + ": " + message);
  }

  @Override
  public void clientChangeNotification(String from, String notification) throws RemoteException {
    System.out.println(from + ": " + notification);
  }
}
