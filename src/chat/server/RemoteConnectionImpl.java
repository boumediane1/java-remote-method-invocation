package chat.server;

import chat.remote.RemoteClient;
import chat.remote.RemoteConnection;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RemoteConnectionImpl extends UnicastRemoteObject implements RemoteConnection {
  private String name;
  private RemoteClient client;
  private Server server;

  public RemoteConnectionImpl(String name, RemoteClient client, Server server)
      throws RemoteException {
    this.name = name;
    this.client = client;
    this.server = server;
  }

  @Override
  public void say(String message) throws RemoteException {
    server.broadcastMessage(message, name);
  }

  @Override
  public void logoff() throws RemoteException {
    server.removeClient(name);

    try {
      client.speak("Server", "Good bye!");
    } catch (RemoteException e) {
      // nothing to do as the client is gone anyway
    }
  }
}
