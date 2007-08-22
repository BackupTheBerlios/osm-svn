
package net.osm.agent;

import javax.swing.ImageIcon;

import org.omg.CORBA.ORB;

import net.osm.util.IconHelper;

/**
 * Utility class used to handle fallback conditions in the event of the 
 * failure of the framework to locate a specific agent type.
 */
public class UnknownAgent extends AbstractAgent 
{

    private static String path = "net/osm/agent/image/resource.gif";
    private static ImageIcon icon = IconHelper.loadIcon( path );

    //=========================================================================
    // Constructor
    //=========================================================================

    public UnknownAgent( )
    {
	  super();
    }

    //=========================================================================
    // implementation
    //=========================================================================

    public String getUrl( ) 
    {
	  return null;
    }

    public String getKind( ) 
    {
	  return "IDL:/unknown:0";
    }
}
