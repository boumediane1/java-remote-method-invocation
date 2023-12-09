package chat.server;

import chat.remote.RemoteClient;

public interface Server {
  boolean addClient(String clientName, RemoteClient client);

  boolean removeClient(String name);

  String[] getClientNames();

  RemoteClient getClient(String clientName);

  void broadcastMessage(String message, String from);

  boolean targetMessage(String message, String from, String to);
}
