
package net.osm.agent;

import org.omg.CORBA.ORB;
import org.omg.CommunityFramework.Control;
import org.omg.CORBA.portable.ValueBase;

import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;

import net.osm.orb.ORBService;
import net.osm.entity.EntityContext;

/**
 * Abstract base class for agents encapsulating valuetypes.
 */
public abstract class ValueAgent extends AbstractAgent implements Contextualizable
{

   /**
    * The object reference to the valuetype that this agents 
    * represents.
    */
    protected ValueBase valuebase;

   /**
    * Cache reference to kind IDL string.
    */
    protected String kind;

   /**
    * Runtime ORB.
    */
    private ORB orb;

    //=========================================================================
    // Constructor
    //=========================================================================

    public ValueAgent(  )
    {
    }

    public ValueAgent( Object object )
    {
	  if( object == null ) throw new RuntimeException(
		"ValueAgent - null reference supplied to agent constructor");
        try
        {
            this.valuebase = (ValueBase) object;
        }
	  catch( Throwable t )
        {
		throw new RuntimeException(
		"ValueAgent/setPrimary - bad type.");
        }
    }

    //=================================================================
    // Contextualizable
    //=================================================================

    public void contextualize( Context context ) throws ContextException
    {
	  super.contextualize( context );
	  setPrimary( ((EntityContext)context).getPrimary() );
    }

    //=========================================================
    // Composable implementation
    //=========================================================

    public void compose( ComponentManager manager )
    throws ComponentException
    {
        super.compose( manager );
	  try
	  {
		orb = ((ORBService) manager.lookup("ORB")).getOrb();
        }
        catch( Exception e )
        {
		throw new ComponentException("unexpected exception during composition phase", e );
        }
    }

    public ORB getOrb()
    {
        return orb;
    }

    //=========================================================================
    // Atrribute setters
    //=========================================================================

   /**
    * Set the valuetype that is to be presented.
    */
    public void setPrimary( Object value ) 
    {
	  if( value == null ) throw new RuntimeException(
		"ValueAgent/setPrimary - null value supplied.");
	  try
	  {
		this.valuebase = (ValueBase) value;
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
	  if( kind != null ) return kind;
	  try
        {
            kind = valuebase._truncatable_ids()[0];
		return kind;
	  }
	  catch( Throwable e )
        {
            throw new RuntimeException( "ValueAgent:getKind", e );
        }
    }
}
