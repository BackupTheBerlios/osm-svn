
package net.osm.agent;

import org.apache.avalon.framework.CascadingRuntimeException;

/**
 * The <code>AbstractAgent</code> class is an abstract class that handles basic
 * configuration operations including association of an ORB and principal object.
 * The implementation also provides basic support for creation of a human-friendly
 * type name.
 */

public abstract class AbstractAgent implements Agent
{
   /**
    * The pricipal object that this agents represents.
    */
    protected Object object;

   /**
    * Cached reference to human friendly short form of the agent kind.
    */
    protected String type;

    //=========================================================================
    // Atrribute setters and getters
    //=========================================================================

   /**
    * Set the pricipal object that agent represents.
    * @param value principal object that the agent will represent
    */
    public void setReference( Object value ) 
    {
	  if( value == null ) throw new RuntimeException(
		"AbstractAgent/setReference - null value supplied.");
	  this.object =  value;
    }

   /**
    * Returns the pricipal object that agent represents.
    */
    protected Object getReference( ) 
    {
	  return this.object;
    }

   /**
    * The <code>dispose</code> method is invoked prior to disposal of the 
    * agent.  The implementation handles cleaning-up of state members.
    */

    public void dispose()
    {
        this.object = null;
        this.type = null;
    }

   /**
    * Returns an IDL type identifier of the pricipal object.
    */

    public abstract String getKind( );

   /**
    * The <code>getType</code> method returns the resource kind, a human 
    * friendly representation of an IDL identifier.
    */

    public String getType( )
    {
	  if( type != null ) return type;
	  try
        {
		String k = getKind();
            type = k.substring( k.lastIndexOf("/") + 1, k.lastIndexOf(":")).toLowerCase();
		return type;
	  }
	  catch( Throwable e )
        {
            throw new CascadingRuntimeException( "AbstractAgent:getKind", e );
        }
    }
}
