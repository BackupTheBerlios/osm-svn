
package acme.impl;

import acme.Widget;

public class DefaultWidget implements Widget
{
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
