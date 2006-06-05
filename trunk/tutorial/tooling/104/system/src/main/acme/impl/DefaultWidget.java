
package acme.impl;

import acme.Widget;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class DefaultWidget extends UnicastRemoteObject implements Widget
{
    public DefaultWidget() throws RemoteException
    {
        super();
    }
    
    public void process( String color )
    {
        String message = buildMessage( color );
        System.out.println( message );
    }

    public String buildMessage( String color )
    {
        if( null == color )
        {
            return "I'm a plain old " + COLOR + " widget named " + NAME + ".";
        }
        else
        {       
            return "I'm a " + color + " widget named " + NAME;
        }
    }

    private static final String NAME = "@NAME@";
    private static final String COLOR = "@COLOR@";
 
}
