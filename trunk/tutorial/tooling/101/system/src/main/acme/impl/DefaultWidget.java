
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
            return "I'm a plain old widget.";
        }
        else
        {       
            return "I'm a " + color + " widget.";
        }
    }
}
