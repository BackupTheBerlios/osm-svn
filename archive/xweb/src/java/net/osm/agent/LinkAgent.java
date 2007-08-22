
package net.osm.agent;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.omg.Session.Link;

public class LinkAgent extends ValueAgent implements Agent
{

   /**
    * The object reference to the Link that this agents 
    * represents.
    */
    protected Link link;

   /**
    * Cache reference to the target object reference.
    */
    protected AbstractResourceAgent target;

    //=========================================================================
    // Constructor
    //=========================================================================

    public LinkAgent(  )
    {
    }

    public LinkAgent( Object object )
    {
	  super( object );
        try
        {
            this.link = (Link) object;
        }
	  catch( Throwable t )
        {
		throw new RuntimeException(
		"LinkAgent/setReference - bad type.");
        }
    }


    //=========================================================================
    // Atrribute setters
    //=========================================================================

   /**
    * Set the resource that is to be presented.
    */
    public void setReference( Object value ) 
    {
	  super.setReference( value );
	  try
	  {
		this.link = (Link) value;
        }
	  catch( Throwable t )
        {
		throw new RuntimeException(
		"LinkAgent/setReference - bad type.");
        }
    }

    //=========================================================================
    // Getter methods
    //=========================================================================

   /**
    * Returns the target of the link.
    */

    public AbstractResourceAgent getTarget( )
    {
	  if( target != null ) return target;
	  try
        {
	      target = (AbstractResourceAgent) AgentServer.getAgentService().resolve( link.resource() );
		return target;
	  }
	  catch( Throwable e )
        {
            throw new CascadingRuntimeException( "LinkAgent:getTarget", e );
        }
    }

   /**
    * Returns the name of the resource.
    */

    public String getName()
    {
	  try
        {
            return getTarget().getName();
	  }
	  catch( Throwable e )
        {
            throw new CascadingRuntimeException( "LinkAgent:getName", e );
        }
    }


   /**
    * Return the IOR of the link's target object reference.
    */

    public String getIor( ) 
    {
	  try
        {
            return getTarget().getIor();
 	  }
	  catch( Throwable e )
        {
            throw new CascadingRuntimeException( "LinkAgent:getIor", e );
        }
    }


}
