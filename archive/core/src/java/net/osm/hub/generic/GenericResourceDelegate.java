

package net.osm.hub.generic;

import java.io.Serializable;

import org.apache.avalon.framework.context.Context;

import org.omg.CORBA.ORB;
import org.omg.CORBA.Any;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.PortableServer.POA;
import org.omg.CosLifeCycle.NotRemovable;

import org.omg.CommunityFramework.LockedResource;
import org.omg.CommunityFramework.GenericResource;
import org.omg.CommunityFramework.GenericResourceOperations;
import org.omg.CommunityFramework.GenericResourceHelper;
import org.omg.CommunityFramework.GenericTypeConflict;

import net.osm.hub.gateway.Manager;
import net.osm.hub.pss.GenericStorage;
import net.osm.hub.gateway.CannotTerminate;
import net.osm.hub.resource.AbstractResourceDelegate;

/**
* GenericResourceDelegate is a type of AbstractResource that exposes operations through 
* which values (in the form of an any) can be attributed to the resource in an 
* interoperable manner.  Instances of GenericResourceDelegate are created through a 
* ResourceFactory using an instance of GenericCriteria as the criteria argument.
*/

public class GenericResourceDelegate extends AbstractResourceDelegate implements GenericResourceOperations
{

    //=================================================================
    // state
    //=================================================================
 
   /**
    * Storage object representing this GenericResource.
    */
    private GenericStorage store;

   /**
    * Object reference to this GenericResource.
    */
    private GenericResource m_generic;
     
    //=======================================================================
    // Initializable
    //=======================================================================
    
   /**
    * Signal completion of contextualization phase and readiness to serve requests.
    * @exception Exception if the servant cannot complete normal initialization
    */
    public void initialize()
    throws Exception
    {
        super.initialize();
        if( getLogger().isDebugEnabled() ) getLogger().debug( "initialize (generic)" );
        this.store = (GenericStorage) super.getContext().getStorageObject();
        setGenericReference( GenericResourceHelper.narrow( 
          getManager().getReference( store.get_pid(), GenericResourceHelper.id() )));
    }

    //=================================================================
    // Disposable 
    //=================================================================

   /**
    * Clean up state members.
    */ 
    public synchronized void dispose()
    {
	  if( getLogger().isDebugEnabled() ) getLogger().debug("dispose (generic)");
        this.store = null;
        this.m_generic = null;
	  super.dispose();
    }

    // ==========================================================================
    // GenericResource implementation
    // ==========================================================================

   /**
    * Returns the IDL identifier of the type of value that can be 
    * contained within the generic resource instance.
    * @osm.note  this operation has been added to address type management
    * corrections to the interface
    */
    public String constraint()
    {
        return store.identifier();
    }

   /**
    * Returns the instance of Seializable contained by this resource.
    */
    public Serializable value()
    {
        getLogger().info("value");
        touch( store );
        return store.value();
    }

   /**
    * Types derived from Generic can be locked to prevent modification
    * of the value attribute.  The locked atrribute supports setting and getting 
    * of the locked state of the resource.
    * 
    * @return  true if locked.
    */
    public boolean locked()
    {
        getLogger().info("locked");
        touch( store );
        return store.locked();
    }

   /**
    * Types derived from Generic can be locked to prevent modification
    * of the value attribute.  The locked atrribute supports setting and getting 
    * of the locked state of the resource.
    * 
    * @return  true if locked.
    */
    public void locked( boolean value )
    {
        getLogger().info("set locked");
        store.locked( value );
        modify( store );
    }

   /**
    * Sets the template state of the resource.
    */
    public boolean template()
    {
        touch( store );
        return store.template();
    }

   /**
    * Sets the template state of the resource.
    */
    public void template( boolean value )
    {
        getLogger().info("set template " + value );
        store.template( value );
        modify( store );
    }

   /**
    * Sets the value of the Generic resource.
    * @param  value org.omg.CORBA.Any a reference to a CORBA Any to be assigned as the 
    * value of the resource.
    * @exception  LockedResource if invoked under a locked state.
    */
    public void set_value( Serializable value )
    throws LockedResource, GenericTypeConflict
    {
        getLogger().info("set_value");
	  if( store.locked() ) throw new LockedResource();
        
        // make sure that the supplied value is an instance of the 
	  // type declared by the constraint IDL identifier

        ValueFactory factory = ((org.omg.CORBA_2_3.ORB)orb).lookup_value_factory( store.identifier() );
	  if( factory.getClass().isInstance( value ) )
	  {
	      store.value( value );
		modify( store );
	  }
	  else
	  {
		throw new GenericTypeConflict( store.identifier() );
	  }
    }
    
    // ==========================================================================
    // AbstractResource operation override
    // ==========================================================================
    
   /**
    *  The most derived type that this <code>AbstractResource</code> represents.
    */
    
    public TypeCode resourceKind()
    {
        getLogger().info("resourceKind");
        touch( store );
        return GenericResourceHelper.type();
    }

    // ==========================================================================
    // utilities
    // ==========================================================================

   /**
    * Set the object reference to be returned for this delegate.
    * @param workspace the object reference for the workspace
    */
    protected void setGenericReference( GenericResource generic )
    {
        m_generic = generic;
        setReference( generic );
    }

   /**
    * Returns the object reference for this delegate.
    * @return Generic the object referenced for the delegate
    */
    protected GenericResource getGenericReference( )
    {
        return m_generic;
    }

}
