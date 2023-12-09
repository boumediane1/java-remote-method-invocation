package chat.server;

import chat.remote.RemoteClient;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ServerImpl implements Server {
  private final Map<String, RemoteClient> clients;

  public ServerImpl() {
    clients = new HashMap<>();
  }

  public ServerImpl(Map<String, RemoteClient> clients) {
    this.clients = clients;
  }

  @Override
  public boolean addClient(String clientName, RemoteClient client) {
    synchronized (clients) {
      if (clients.get(clientName) != null) return false;
      clients.put(clientName, client);
    }

    notifyAllClients(clientName, "logged on");
    return true;
  }

  @Override
  public boolean removeClient(String clientName) {
    synchronized (clients) {
      if (clients.remove(clientName) == null) return false;
    }

    notifyAllClients(clientName, "logged off");
    return true;
  }

  @Override
  public String[] getClientNames() {
    synchronized (clients) {
      return clients.keySet().toArray(new String[0]);
    }
  }

  @Override
  public RemoteClient getClient(String clientName) {
    synchronized (clients) {
      return clients.get(clientName);
    }
  }

  @Override
  public void broadcastMessage(String message, String from) {
    new RunnableClientCaller(entriesIterator(), new ClientSpeakCall(message, from));
  }

  @Override
  public boolean targetMessage(String message, String from, String to) {
    RemoteClient sender = getClient(from);
    RemoteClient receiver = getClient(to);

    if (receiver == null) return false;

    message = "[" + message + "]";

    try {
      receiver.speak(from, message);
    } catch (RemoteException e) {
      System.out.println("Dropping " + to + " due to remote error");
      removeClient(to);
      return false;
    }

    try {
      sender.speak(from, message);
    } catch (RemoteException e) {
      System.out.println("Dropping " + from + " due to remove error");
      removeClient(from);
      return false;
    }

    return true;
  }

  private Iterator<Map.Entry<String, RemoteClient>> entriesIterator() {
    return cloneClients().entrySet().iterator();
  }

  private Map<String, RemoteClient> cloneClients() {
    synchronized (clients) {
      return Map.copyOf(clients);
    }
  }

  private void notifyAllClients(String from, String notification) {
    new RunnableClientCaller(
        entriesIterator(), new ClientChangeNotificationCall(notification, from));
  }

  private interface ClientCall {
    void doCall(String clientName, RemoteClient client) throws RemoteException;
  }

  private record ClientChangeNotificationCall(String notification, String from)
      implements ClientCall {

    @Override
    public void doCall(String clientName, RemoteClient client) throws RemoteException {
      client.clientChangeNotification(clientName, notification);
    }
  }

  private record ClientSpeakCall(String message, String from) implements ClientCall {

    @Override
    public void doCall(String clientName, RemoteClient client) throws RemoteException {
      client.speak(from, message);
    }
  }

  private class RunnableClientCaller implements Runnable {
    private final Iterator<Map.Entry<String, RemoteClient>> iterator;
    private final ClientCall clientCall;

    public RunnableClientCaller(
        Iterator<Map.Entry<String, RemoteClient>> iterator, ClientCall clientCall) {
      this.iterator = iterator;
      this.clientCall = clientCall;
      Thread thread = new Thread(this);
      thread.start();
    }

    @Override
    public void run() {
      while (iterator.hasNext()) {
        Map.Entry<String, RemoteClient> clientEntry = iterator.next();

        String clientName = clientEntry.getKey();
        RemoteClient client = clientEntry.getValue();

        try {
          clientCall.doCall(clientName, client);
        } catch (RemoteException e) {
          removeClient(clientName);
        }
      }
    }
  }
}
