package chat.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteServer extends Remote {
  RemoteConnection logon(String name, RemoteClient client) throws RemoteException;
}
