package chat.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteClient extends Remote {
  void speak(String from, String message) throws RemoteException;

  void clientChangeNotification(String from, String notification) throws RemoteException;
}
