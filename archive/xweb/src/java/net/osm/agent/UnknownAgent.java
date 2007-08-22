
package net.osm.agent;

import org.omg.CORBA.ORB;

public class UnknownAgent extends AbstractAgent
{

    //=========================================================================
    // Constructor
    //=========================================================================

    public UnknownAgent( )
    {
	  super();
    }

    public String getUrl( ) 
    {
	  return null;
    }

    public String getKind( ) 
    {
	  return "IDL:/unknown:0";
    }


}
