package chat.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteConnection extends Remote {
  void say(String message) throws RemoteException;

  void logoff() throws RemoteException;
}
