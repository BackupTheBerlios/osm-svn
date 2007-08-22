
package net.osm.entity;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.avalon.phoenix.Block;
import org.apache.avalon.phoenix.BlockContext;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.activity.Startable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.DefaultComponentManager;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.AvalonFormatter;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.LogKitLogger;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.excalibur.component.ExcaliburComponentManager;

import org.omg.CORBA_2_3.ORB;
import org.omg.CORBA.portable.ObjectImpl;

import net.osm.orb.ORBService;
import net.osm.shell.Entity;
import net.osm.shell.EntityListener;
import net.osm.shell.EntityEvent;


/**
 * ResourceFactory provides support for the creation of <code>Entity</code> 
 * instances based on CORBA object references.
 * @author Stephen McConnell
 */

public class ResourceFactory extends AbstractLogEnabled implements EntityFactory, EntityListener
{

    //=================================================================
    // state
    //=================================================================

    private ComponentManager manager;
    private Configuration configuration;
    private String role;
    private final Hashtable derived = new Hashtable();
    private final Hashtable entities = new Hashtable();
    private final URL[] urls = new URL[0];
    private Class base;

    //=================================================================
    // constructor
    //=================================================================

   /**
    * Creation of a new <code>EntityFactory</code> that will serve object 
    * creation requests using the supplied class, application context and 
    * configuration.
    */
    public ResourceFactory( final Logger logger, final ComponentManager manager, 
      final Configuration config )
    {
        this.manager = manager;
	  this.configuration = config;

        try
	  {
            this.role = config.getAttribute("role");
	  }
	  catch( Exception e )
	  {
		final String error = "missing role attribute on element '" + config.getName() + "'";
		throw new RuntimeException( error, e );
	  }
	  final Configuration[] children = config.getChildren();
        for( int i=0; i<children.length; i++ )
	  {
		final Configuration child = children[i];
		add( new ResourceFactory( logger, manager, child ) );
	  }

	  String path = null;
	  try
	  {
		path = config.getAttribute("class");
	  }
	  catch( Throwable e ){}
	  if( path == null )
	  {
		enableLogging( logger );
        }
        else try
	  {
	      final URLClassLoader loader = new URLClassLoader( urls,
	        Thread.currentThread().getContextClassLoader());
	      base = loader.loadClass( path );
		enableLogging( logger.getChildLogger( getCatagory( base )));
		if(getLogger().isInfoEnabled()) getLogger().info(
		  "new factory for " + path );
	  }
	  catch( Throwable e )
	  {
		final String error = "unable to create factory for " + config.getName();
		throw new RuntimeException( error, e );
	  }
    }

    // return the name for the logger for instances created by this factory

    private String getLoggerTitle( final Object object )
    {
	  return "" + System.identityHashCode( object );
    }

    // return the name for the factory logger

    private String getCatagory( final Class main )
    {
        return main.getName().substring( base.getName().lastIndexOf(".") + 1, 
	    base.getName().length() ).trim().toLowerCase();
    }

   /**
    * Return the interface that this factory handles.
    */
    public String getRole()
    {
        return this.role;
    }

   /**
    * Add a derived type to this node.
    */
    protected void add( final ResourceFactory node )
    {
        derived.put( node.getRole(), node );
    }

   /**
    * Remove a node from the list of children.
    */
    protected void remove( final ResourceFactory node )
    {
        derived.remove( node.getRole() );
    }

   /**
    * Replace a node in the list of children.
    */
    protected void replace( final ResourceFactory node )
    {
        remove( node );
        add( node );
    }

   /**
    * Returns a factory capable of supporting a primary object.
    * @param object the primary object
    */
    public ResourceFactory resolve( final ObjectImpl object )
    {
        if( object == null ) throw new NullPointerException(
	    "null object argument to resolve");
        boolean test = object._is_a( role );
	  if( !test ) return null;
        final Iterator iterator = derived.values().iterator();
	  while( iterator.hasNext() )
        {
	      final ResourceFactory child = (ResourceFactory) iterator.next();
		final ResourceFactory target = child.resolve( object );
		if( target != null ) return target;
	  }
	  return this;
    }

   /**
    * Returns an existing entity using a supplied primary object as a key.
    * @param key the primary object
    * @return Entity the entity matching the supplied primary object key
    *   (null if the kley does not match an entry)
    */
    public Entity lookup( final Object key )
    {
        if( !( key instanceof org.omg.CORBA.Object )) return null;
        synchronized( entities )
        {
            Iterator iterator = entities.keySet().iterator();
		while( iterator.hasNext() )
		{
                org.omg.CORBA.Object entry = (org.omg.CORBA.Object) iterator.next();
		    if( entry._is_equivalent( (org.omg.CORBA.Object) key ) )
		    {
			  Entity entity = (Entity) entities.get( entry );
                    if(getLogger().isDebugEnabled()) getLogger().debug( 
                      "located: " + System.identityHashCode( entity ));
			  return entity;
		    }
		}
		return null;
        }
    }

   /**
    * Create of a new <code>Entity</code> instance wrapping the supplied
    * object.
    * @param object the object to wrap
    * @exception Exception
    */
    public Entity newInstance( final Object object )
    {
        if( isAbstract() ) throw new RuntimeException("abstract factory");

        synchronized( entities )
        {
            try
	      {
	          final Entity entity = (Entity) base.newInstance();
                setupLogger( entity, getLoggerTitle( entity )  );
                if( entity instanceof Contextualizable ) 
                 ((Contextualizable)entity).contextualize(
		        new EntityContext( object ));
                if( entity instanceof Composable ) ((Composable)entity).compose( manager );
                if( entity instanceof Configurable ) ((Configurable)entity).configure( 
			configuration.getChild("configuration") );
                if( entity instanceof Initializable )((Initializable)entity).initialize();
                if( entity instanceof Startable ) ((Startable)entity).start();

		    entities.put( object, entity );
                if(getLogger().isDebugEnabled()) getLogger().debug( 
                  "created: " + System.identityHashCode( entity ));
		    entity.addEntityListener( this );
                return entity;
	      }
	      catch( Throwable e )
	      {
		    final String error = "unable to create instance of " + base.getName();
                if( getLogger().isErrorEnabled() ) getLogger().error( error );
	    	    throw new RuntimeException( error, e );
	      }
        }
    }

   /**
    * Method invoked when an an entity has been disposed of. 
    * @param event the disposal event
    */
    public void notifyEntityDisposal( final EntityEvent event )
    {
	  final Entity entity = event.getEntity();
	  synchronized( entities )
	  {
		entities.values().remove( event.getEntity() );
	  }
    }

   /**
    * Tests if this factory represents an abstract type.
    * @return boolean true if this factory is abstract
    */
    public boolean isAbstract()
    {
        return ( base == null );
    }

   /**
    * Disposes of resources consumed by the factory.
    */
    public void dispose()
    {
	  if( getLogger().isDebugEnabled() ) getLogger().debug( "disposal of factory " + role );
        final Iterator factories = derived.values().iterator();
	  while( factories.hasNext() )
        {
		ResourceFactory factory = (ResourceFactory) factories.next();
	      factories.remove();		
		factory.dispose();
	  }
	  final Iterator iterator = entities.values().iterator();
        while( iterator.hasNext() )
	  {
		try
		{
		    final Entity entity = (Entity) iterator.next();
		    iterator.remove();
		    entity.removeEntityListener( this );
		    entity.dispose();
		}
		catch( Exception e )
	      {
		    final String warning = "unexpected exception while disposing of an entity";
	          if( getLogger().isWarnEnabled() ) getLogger().warn( warning, e );
	      }
	  }
        this.manager = null;
	  this.base = null;
	  if( getLogger().isDebugEnabled() ) getLogger().debug( "disposal complete " + role );
	  this.role = null;
    }
}

