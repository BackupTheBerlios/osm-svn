
package net.osm.entity;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

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
import org.omg.CORBA.portable.ValueBase;
import org.omg.CORBA.portable.ObjectImpl;

import net.osm.orb.ORBService;
import net.osm.shell.Entity;
import net.osm.entity.EntityFactory;
import net.osm.entity.ResourceFactory;
import net.osm.entity.ValueFactory;
import net.osm.entity.EntityContext;

import net.osm.shell.Service;
import net.osm.shell.Shell;

/**
 * The <code>EntityService</code> provides support for new <code>Entity</code>
 * creation by aggregation of a set of factories. 
 * 
 * @author Stephen McConnell
 */
public abstract class EntityServer extends DefaultComponentManager
implements Block, Disposable, LogEnabled, Configurable, Contextualizable, Composable, Initializable, EntityService
{

    //=================================================================
    // state
    //=================================================================

    private Logger logger;
    private Context context;
    private Configuration configuration;

    private final Hashtable factories = new Hashtable(); // sub-types
    private ResourceFactory resource;

    //================================================================
    // LogEnabled
    //================================================================
    
    /**
     * Set the components logger.
     *
     * @param logger the logger
     */
    public void enableLogging( final Logger logger )
    {
        this.logger = logger;
    }

    /**
     * Returns the current logging channel.
     * @return the Logger
     */
    protected final Logger getLogger()
    {
        return logger;
    }

    /**
     * Helper method to setup other components with same logger.
     *
     * @param component the component to pass logger object to
     */
    protected void setupLogger( final Object component )
    {
        setupLogger( component, (String)null );
    }

    /**
     * Helper method to setup other components with logger.
     * The logger has the subcategory of this components logger.
     *
     * @param component the component to pass logger object to
     * @param subCategory the subcategory to use (may be null)
     */
    protected void setupLogger( final Object component, final String subCategory )
    {
        Logger log = logger;

        if( null != subCategory )
        {
            log = logger.getChildLogger( subCategory );
        }

        setupLogger( component, log );
    }

    /**
     * Helper method to setup other components with logger.
     *
     * @param component the component to pass logger object to
     * @param logger the Logger
     */
    protected void setupLogger( final Object component, final Logger logger )
    {
        if( component instanceof LogEnabled )
        {
            ((LogEnabled)component).enableLogging( logger );
        }
    }

    //=================================================================
    // Contextualizable
    //=================================================================

    public void contextualize( Context context ) throws ContextException
    {
        this.context = context;
    }

    //=========================================================
    // Composable implementation
    //=========================================================

    /**
     * Pass the <code>ComponentManager</code> to the <code>Composable</code>.
     * The <code>Composable</code> implementation should use the specified
     * <code>ComponentManager</code> to acquire the components it needs for
     * execution.
     *
     * @param manager The <code>ComponentManager</code> which this
     *                <code>Composer</code> uses.
     * @exception ComponentException
     */
    public void compose( ComponentManager manager )
    throws ComponentException
    {
	  put( "ORB", manager.lookup("ORB") );
	  put( "AUDIT", manager.lookup("AUDIT") );
	  put( "SHELL", manager.lookup("SHELL") );
    }

    //=========================================================
    // Configurable implementation
    //=========================================================
    
   /**
    * Configuration of the runtime environment based on a supplied Configuration arguments.
    *
    * @param config static configuration block.
    * @exception ConfigurationException if the supplied configuration is incomplete or badly formed.
    */
    public void configure( final Configuration config )
    throws ConfigurationException
    {
        this.configuration = config;
    }

    //=========================================================
    // Initializable implementation
    //=========================================================

   /**
    * Initialization is invoked by the framework following configuration.  
    * @osm.note implementation is currently creating the same factory 
    * multiple time for different role names - implementation needs to be 
    * updated to handle multiple roles supported by a single factory
    * @exception Exception
    */
    public void initialize()
    throws Exception
    {
        put( "RESOLVER", (EntityService) this );
        try
	  {
	      resource = new ResourceFactory( getLogger(), this, configuration.getChild("resource") );
	      Configuration[] children = configuration.getChild("values").getChildren();
            for( int i=0; i<children.length; i++ )
	      {
		    Configuration child = children[i];
		    String role = child.getAttribute("role");

		    if( factories.get( role ) == null )
		    {
		       factories.put( role, new ValueFactory( getLogger(), this, child ) );
		    }
	      }
        }
	  catch( Exception e )
	  {
		String error = "unexpected exception while bootstrapping factories";
	      if(getLogger().isErrorEnabled()) getLogger().error( error, e );
		throw new Exception( error, e );
	  }
    }


   /**
    * Creation of a new instance of Entity supporting a supplied primary object.
    */
    public Entity resolve( Object object )
    {

	  if( object == null ) throw new NullPointerException("cannot resolve a null object");
        if(getLogger().isDebugEnabled()) getLogger().debug("resolve: " + object.getClass().getName());

	  //
	  // get the set of interfaces supported by this object
	  // so we can resolve the entity factory to use
        //

        if( object instanceof ObjectImpl )
	  {
		ResourceFactory factory = resource.resolve( (ObjectImpl) object );
		if( factory != null ) 
		{
		    if( factory.isAbstract() )
	          {
		        throw new RuntimeException("resolve returned an abstract factory");
		    }
		    Entity ent = factory.lookup( object );
		    if( ent != null ) return ent;
		    return factory.newInstance( object );
            }
		else
	      {
		    final String warning = "no factory supporting a remote object ";
                if(getLogger().isWarnEnabled()) getLogger().warn( warning );
	  	    throw new RuntimeException( warning + object);
	      }
        }
        else if( object instanceof ValueBase )
	  {
		ValueFactory factory = null;
            ValueBase value = (ValueBase) object;
		String[] ids = value._truncatable_ids();
            for( int i=0; i<ids.length; i++ )
	      {
		    factory = (ValueFactory) factories.get( ids[i] );
		    if( factory != null ) break;
	      }
	      if( factory != null )
		{
		    return factory.newInstance( object );
		}
		else
		{
                if( getLogger().isDebugEnabled() ) getLogger().debug(
		      "no supporting value factory");
	          throw new RuntimeException("unable to provide a factory for " + object );
		}
        }
        else
	  {
		final String error = "don't know how to create entity for ";
		throw new RuntimeException( error + object.getClass().getName() );
	  }
    }

    //=========================================================
    // ComponentManager
    //=========================================================

    public Component lookup( String role ) throws ComponentException
    {
        return super.lookup( role );
    }

   /**
    * Returns a list of Action instances to be installed as tool
    * menu items within the desktop for the lifetime of the service.
    */
    public abstract List getTools( );

    //==========================================================================
    // Disposable
    //==========================================================================

   /**
    * Disposal of this block.
    */  
    public void dispose()
    {
	  final String message = "disposing of the entity server";
   	  if( getLogger().isInfoEnabled() ) getLogger().info( message );
        context = null;
        configuration = null;
	  try
	  {
		resource.dispose();
	  }
	  catch( Throwable e )
	  {
	      final String warning = "unexpected exception while disposing of the root factoryr";
   	      if( getLogger().isWarnEnabled() ) getLogger().warn( warning, e );
	  }
	  Iterator iterator = factories.values().iterator();
	  while( iterator.hasNext() )
	  {
		try
		{
 	  	    Object f = iterator.next();
		    if( f instanceof Disposable ) ((Disposable)f).dispose();
 	      }
            catch( Throwable ve )
	      {
	          final String warning = "unexpected exception while disposing of a value factory";
   	          if( getLogger().isWarnEnabled() ) getLogger().warn( warning, ve );
	      }
        }
	  final String completion = "entity server disposal complete";
   	  if( getLogger().isInfoEnabled() ) getLogger().info( completion );
    }
}