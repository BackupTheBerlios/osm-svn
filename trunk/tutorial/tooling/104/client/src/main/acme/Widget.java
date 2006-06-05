
package acme;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Widget extends Remote
{
    void process( String color ) throws RemoteException;
}
