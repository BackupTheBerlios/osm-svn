
package acme.impl.test;

import junit.framework.TestCase;

import acme.Widget;
import acme.impl.DefaultWidget;

/**
 * Test the DefaultWidget
 *
 * @author <a href="http://www.osm.net">OSM</a>
 */
public class DefaultWidgetTestCase extends TestCase
{
   /**
    * Test the default message produced when we supply a null value.
    */
    public void testDefaultMessage() throws Exception
    {
        DefaultWidget widget = new DefaultWidget();
        String result = widget.buildMessage( null );
        String expected = "I'm a plain old " + COLOR + " widget named " + NAME + ".";
        assertEquals( "default", expected, result );
        widget.process( null );
    }

   /**
    * Test a non-null value.
    */
    public void testExplicitMessage() throws Exception
    {
        DefaultWidget widget = new DefaultWidget();
        String color = "blue";
        String result = widget.buildMessage( color );
        String expected = "I'm a " + color + " widget named " + NAME;
        assertEquals( "explicit", expected, result );
        widget.process( color );
    }

   /**
    * Test the the DefaultWidget is in fact an instance of Widget.
    */
    public void testImplementsWidget() throws Exception
    {
        DefaultWidget impl = new DefaultWidget();
        try
        {
            Widget widget = (Widget) impl;
        }
        catch( ClassCastException e )
        {
            fail( "DefaultWidget does not implement the Widget service interface." );
        }
    }

    private static final String NAME = "@NAME@";
    private static final String COLOR = "@COLOR@";

}
