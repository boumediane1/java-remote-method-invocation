package chat.server;

import chat.remote.RemoteClient;
import chat.remote.RemoteConnection;
import chat.remote.RemoteServer;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

public class RemoteServerImpl extends UnicastRemoteObject implements RemoteServer {
  private static final String SERVER_NAME = "Server";
  private Server chatServer;

  public RemoteServerImpl() throws RemoteException {
    chatServer = new ServerImpl();
  }

  public static void main(String[] args) throws RemoteException, MalformedURLException {
    RemoteServer remoteServer = new RemoteServerImpl();
    LocateRegistry.createRegistry(1099);
    Naming.rebind("rmi://localhost:1099/chat", remoteServer);
  }

  private static String getHostName() {
    try {
      return InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException e) {
      return "Unknown";
    }
  }

  @Override
  public RemoteConnection logon(String name, RemoteClient client) throws RemoteException {
    if (name.equals(SERVER_NAME)) {
      client.speak(SERVER_NAME, "Sorry, but the same Server is reserved");
      return null;
    }

    boolean isRegistered = chatServer.addClient(name, client);

    if (!isRegistered) {
      client.speak(SERVER_NAME, "Sorry, but the same " + name + " is already in use");
      return null;
    }

    RemoteConnection connection = new RemoteConnectionImpl(name, client, chatServer);
    client.speak(name, "Welcome");
    return connection;
  }
}
