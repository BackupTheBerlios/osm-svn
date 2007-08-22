
package net.osm.agent;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import net.osm.util.IconHelper;

import org.omg.Session.Usage;
import org.omg.Session.Tagged;


/**
 * The UsageAgent is an agent that wraps an instance of Usage (a CORBA valuetype 
 * used to represent usage association between AbstractResource and Task instances).
 */

public class UsageAgent extends LinkAgent 
{

   /**
    * The primary valuetype.
    */
    private Usage usage;

    //=========================================================================
    // Agent
    //=========================================================================

   /**
    * Set the Usage valuetype instance that this agent is wrapping.
    */
    public void setPrimary( Object value ) 
    {
	  super.setPrimary( value );
	  try
	  {
		this.usage = (Usage) value;
        }
	  catch( Throwable e )
        {
		throw new RuntimeException(
             "Primary object is not a Usage.", e );
        }
    }

    //=========================================================================
    // implementation
    //=========================================================================

   /**
    * Returns the target of the link.
    * @return AbstractResourceAgent the agent representing the link target or null if the
    *    target does not exist.
    */
    public String getTag( )
    {
        return usage.tag();
    }

}
