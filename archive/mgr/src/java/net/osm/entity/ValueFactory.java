
package net.osm.entity;

import java.net.URL;
import java.net.URLClassLoader;

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

import org.omg.CORBA.ORB;

import net.osm.orb.ORBService;
import net.osm.shell.Entity;


/**
 * ValueFactory provides support for the creation of <code>Entity</code> 
 * instances based on CORBA valuetypes.
 * @author Stephen McConnell
 */

public class ValueFactory extends AbstractLogEnabled implements EntityFactory, Disposable
{

    //=================================================================
    // state
    //=================================================================

    private String catagory;
    private String role;
    private ComponentManager manager;
    private Configuration configuration;
    private Class base;
    private final URL[] urls = new URL[0];

    //=================================================================
    // constructor
    //=================================================================

   /**
    * Creation of a new <code>ValueFactory</code> supporting the creation
    * of new Entity instances.
    */
    public ValueFactory( Logger logger, ComponentManager manager, Configuration config )
    {
        if( manager == null ) throw new NullPointerException("null manager argument");
        if( config == null ) throw new NullPointerException("null configuration argument");
        if( logger == null ) throw new NullPointerException("null logger argument");

        this.catagory = config.getName();
        this.manager = manager;
	  this.configuration = config;

        try
	  {
            this.role = config.getAttribute("role");
	  }
	  catch( Throwable e )
	  {
		final String error = "missing role attribute on element '" + config.getName() + "'";
		throw new RuntimeException( error, e );
	  }

        String path = null;
        try
	  {
            path = config.getAttribute("class");
	  }
	  catch( Throwable e )
	  {
		final String error = "missing class attribute on element '" + config.getName() + "'";
		throw new RuntimeException( error, e );
	  }

	  try
	  {
	      URLClassLoader loader = new URLClassLoader( urls,
	        Thread.currentThread().getContextClassLoader());
	      base = loader.loadClass( path );
		enableLogging( logger.getChildLogger( getCatagory( base )));
            if( getLogger().isInfoEnabled() ) getLogger().info( 
		  "new value factory for " + path );
	  }
	  catch( Throwable e )
	  {
		final String error = "unable to create value factory for " + config.getName();
		throw new RuntimeException( error, e );
	  }

    }

    private String getCatagory( Class main )
    {
        return main.getName().substring( base.getName().lastIndexOf(".") 
		+ 1, base.getName().length() ).trim().toLowerCase();
    }

    private String getLoggerTitle( Object object )
    {
	  return "" + System.identityHashCode( object );
    }

   /**
    * Create of a new <code>Entity</code> instance wrapping the supplied
    * object.
    * @param object the object to wrap
    * @exception Exception
    */
    public Entity newInstance( Object object )
    {
        try
	  {
	      final Entity entity = (Entity) base.newInstance();
            setupLogger( entity, getLoggerTitle( entity ) );
            if(getLogger().isDebugEnabled()) getLogger().debug( 
              "created: " + System.identityHashCode( entity ));
            if( entity instanceof Contextualizable ) 
              ((Contextualizable)entity).contextualize( new EntityContext( object ));
            if( entity instanceof Composable ) ((Composable)entity).compose( manager );
            if( entity instanceof Configurable ) ((Configurable)entity).configure( 
			configuration.getChild("configuration") );
            if( entity instanceof Initializable )((Initializable)entity).initialize();
            if( entity instanceof Startable ) ((Startable)entity).start();

            return entity;
	  }
	  catch( Throwable e )
	  {
		final String error = "unable to create new value instance for ";
		throw new RuntimeException( error + base.getName(), e );
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
        this.catagory = null;
        this.role = null;
        this.manager = null;
	  this.base = null;
    }
}

