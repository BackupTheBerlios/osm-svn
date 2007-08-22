
package net.osm.agent;

import java.util.List;
import java.util.LinkedList;

import net.osm.shell.DefaultEntity;
import net.osm.shell.Feature;
import net.osm.shell.StaticFeature;

/**
 * The <code>AbstractAgent</code> class is an abstract class that handles basic
 * configuration operations including association of an ORB and principal object.
 * The implementation also provides basic support for creation of a human-friendly
 * type name.
 *
 * @author Stephen McConnell
 */

public abstract class AbstractAgent extends DefaultEntity implements Agent
{
   /**
    * The principal object that this agents represents.
    */
    protected Object object;

   /**
    * Cached reference to human friendly short form of the agent kind.
    */
    protected String type;

   /**
    * List of features.
    */
    private List features;

    //=========================================================================
    // Constructor
    //=========================================================================

    public AbstractAgent()
    {
        super();
    }

    public AbstractAgent( String name )
    {
        super( name );
    }

    //=========================================================================
    // Atrribute setters and getters
    //=========================================================================

   /**
    * Set the primary object that agent represents.
    * @param value principal object that the agent will represent
    */
    public void setPrimary( Object value ) 
    {
	  if( value == null ) throw new RuntimeException(
		"AbstractAgent/setPrimary - null value supplied.");
	  this.object =  value;
    }

   /**
    * Returns the primary object that agent represents.
    */
    public Object getPrimary( ) 
    {
	  return this.object;
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
            throw new RuntimeException( "AbstractAgent:getKind", e );
        }
    }

    //=========================================================================
    // Entity
    //=========================================================================

   /**
    * Returns a list of <code>Features</code> instances to be presented under 
    * the Properties dialog features panel.
    * @return List the list of <code>Feature</code> instances
    */
    public List getFeatures()
    {
        if( features != null ) return features;
        features = super.getFeatures();
        features.add( new StaticFeature( "kind", getKind()) );
        return features;
    }

    //=========================================================================
    // Disposable implementation
    //=========================================================================

   /**
    * The <code>dispose</code> method is invoked prior to disposal of the 
    * agent.  The implementation handles cleaning-up of state members.
    */

    public void dispose()
    {
        this.object = null;
        this.type = null;
        super.dispose();
    }
}
