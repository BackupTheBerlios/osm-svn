
package net.osm.agent;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import net.osm.util.IconHelper;

import org.omg.Session.Link;
import org.omg.Session.AbstractResource;
import org.omg.Session.Tagged;

import net.osm.shell.Pointer;
import net.osm.agent.AbstractResourceAgent;
import net.osm.shell.Entity;


/**
 * The LinkAgent is an agent that wraps an instance of Link (a CORBA valuetype 
 * used to represent association between AbstractResource instances).  A LinkAgent
 * exposes the target of the link in the form of an Agent.  Specializations
 * of <code>LinkAgent</code> may expose supplimentary atributes bound to the relationship.
 */

public class LinkAgent extends ValueAgent implements Pointer
{

   /**
    * The object reference to the Link that this agents 
    * represents.
    */
    protected Link link;

   /**
    * Cache reference to the target object reference.
    */
    protected Entity target;

    //=========================================================================
    // Agent
    //=========================================================================

   /**
    * Set the Link valuetype instance that this agent is wrapping.
    */
    public void setPrimary( Object value ) 
    {
	  super.setPrimary( value );
	  try
	  {
		this.link = (Link) value;
		super.setName( link.getClass().getName() );
        }
	  catch( Throwable e )
        {
		throw new RuntimeException(
             "Primary object is not a Link.", e );
        }
    }

    //=========================================================================
    // Getter methods
    //=========================================================================

   /**
    * Returns the target of the link.
    * @return AbstractResourceAgent the agent representing the link target or null if the
    *    target does not exist.
    */
    public Entity getTarget( )
    {
	  if( isDisposed() ) throw new IllegalStateException(
		"LinkAgent cannot access target following link disposal.");
	  if( target != null ) return target;
	  try
        {
	      target = (Entity) getResolver().resolve( link.resource() );
		return target;
	  }
	  catch( Throwable e )
        {
		final String error = "unable to get create the target agent";
		if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
		throw new RuntimeException( error, e );
        }
    }

   /**
    * Returns the target of the link in the form of an AbstractResource.
    * @return AbstractResource the primary object reference representing the link target 
    */
    public AbstractResource getPrimaryTarget( )
    {
	  if( isDisposed() ) throw new IllegalStateException(
		"LinkAgent cannot access primary target following link disposal.");
	  return link.resource();
    }

   /**
    * Returns the name of the resource.
    */
    public String getName()
    {
        return link.getClass().getName();
    }

   /**
    * Return the IOR of the link's target object reference.
    */
    public String getIor( ) 
    {
	  try
        {
		AbstractResourceAgent agent = (AbstractResourceAgent) getTarget();
		if( agent == null ) throw new RuntimeException("Null target agent reference.");
		return agent.getIor();
 	  }
	  catch( Throwable e )
        {
            throw new RuntimeException( 
              "LinkAgent. Unable to resolve target IOR.", e );
        }
    }

    //=========================================================================
    // Entity
    //=========================================================================

   /**
    * Returns the icon representing the entity.
    * @param size a value of LARGE or SMALL
    * @return Icon iconic representation of the entity
    */
    public Icon getIcon( int size )
    {
	  try
        {
		AbstractResourceAgent agent = (AbstractResourceAgent) getTarget();
		if( agent == null ) throw new RuntimeException("Null target agent reference.");
		return agent.getIcon( size );
 	  }
	  catch( Throwable e )
        {
            throw new RuntimeException( 
              "LinkAgent. Unable to get target icon.", e );
        }
    }

    //=========================================================================
    // Object
    //=========================================================================

    public boolean equals( Object object )
    {
        if( super.equals( object ) ) return true;
        if( object instanceof AbstractResourceAgent )
	  {
		return equivalent( (AbstractResourceAgent) object );
        }
        else if( object instanceof org.omg.Session.AbstractResource )
	  {
		return equivalent( (org.omg.Session.AbstractResource) object );
        }
        return false;
    }

    public boolean equivalent( LinkAgent link )
    {
        return equivalent( link.getPrimaryTarget() );
    }

    public boolean equivalent( AbstractResourceAgent agent )
    {
        return equivalent( agent.getAbstractResource() );
    }

    public boolean equivalent( org.omg.Session.AbstractResource resource )
    {
        return link.resource()._is_equivalent( resource );
    }

    public boolean equivalent( Link other )
    {      
        return link.resource()._is_equivalent( other.resource() );
    }
}
