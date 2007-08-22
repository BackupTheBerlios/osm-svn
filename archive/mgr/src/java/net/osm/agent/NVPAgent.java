
package net.osm.agent;

import javax.swing.ImageIcon;

import org.apache.avalon.framework.CascadingRuntimeException;

import org.omg.CORBA.Any;
import org.omg.CosLifeCycle.NVP;
import org.omg.CosLifeCycle.NVPHelper;

import net.osm.agent.util.SequenceIterator;
import net.osm.util.IconHelper;

/**
 * The <code>NVPAgent</code> class is an agent class used to represent
 * a named value pair.
 */
public class NVPAgent extends AbstractAgent
{

    private static final String path = "net/osm/agent/image/resource.gif";
    private static final ImageIcon icon = IconHelper.loadIcon( path );


   /**
    * The object reference to the NVP instance that this agent represents.
    */
    protected NVP nvp;


    //=========================================================================
    // Constructor
    //=========================================================================

    public NVPAgent(  )
    {
    }

    public NVPAgent( NVP nvp )
    {
	  super( );
        this.nvp = nvp;
    }

    //=========================================================================
    // Atrribute setters
    //=========================================================================

   /**
    * Set the valuetype that is to be presented.
    */
    public void setPrimary( Object value ) 
    {
	  super.setPrimary( value );
	  try
	  {
		this.nvp = (NVP) value;
        }
	  catch( Throwable t )
        {
		throw new RuntimeException(
		"ValueAgent/setPrimary - bad type.");
        }
    }

    //=========================================================================
    // Getter methods
    //=========================================================================

   /**
    * Returns the resource kind.
    */
    public String getKind( )
    {
	  return NVPHelper.id();
    }

   /**
    * Returns the name of the NVP.
    */
    public String getName( )
    {
	  return nvp.name;
    }

   /**
    * Returns the value contained in the NVP.
    */
    public String getValue( )
    {
	  Any any = nvp.value;
	  try
	  {
	      return any.extract_string();
	  }
	  catch( Exception e )
	  {
		System.err.println("Unsupported type in NVP.");
	      return "";
	  }
    }
}
