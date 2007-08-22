
package net.osm.shell;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Vector;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Enumeration;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.Action;
import javax.swing.AbstractAction;

import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.activity.Initializable;

import net.osm.entity.EntityService;
import net.osm.entity.EntityContext;
import net.osm.shell.Feature;
import net.osm.shell.ContextListener;
import net.osm.shell.ContextEvent;
import net.osm.shell.Panel;
import net.osm.shell.Shell;
import net.osm.shell.ActionHandler;
import net.osm.shell.TabbedView;
import net.osm.shell.ScrollView;
import net.osm.shell.View;
import net.osm.util.ListListener;
import net.osm.util.ListEvent;
import net.osm.util.ExceptionHelper;
import net.osm.util.IconHelper;

/**
 * Default implementation of an Entity.
 */
public abstract class DefaultEntity extends AbstractAction implements ActionHandler, Entity, LogEnabled, Contextualizable, Composable, Initializable
{

    //=========================================================================
    // static
    //=========================================================================

   /**
    * Default small icon path.
    */
    private static final String path = "net/osm/shell/image/item.gif";

   /**
    * Default small icon.
    */
    private Icon icon = IconHelper.loadIcon( path );

   /**
    * Internal debugging policy.
    */
    private static final boolean trace = false;

    //=========================================================================
    // state
    //=========================================================================

   /**
    * Base Logger instance.
    */
    private Logger m_logger;

   /**
    * The runtime context for this entity.
    */
    EntityContext context;

   /**
    * Initialized state - when true, actions are sealed.
    */
    private boolean initialized = false;

   /**
    * Disposed state - when true no value resolution is performed.
    */
    private boolean disposed = false;

   /**
    * List of actions.
    */
    private final LinkedList actionActions = new LinkedList();

   /**
    * List of tools.
    */
    private final LinkedList toolActions = new LinkedList();

   /**
    * Default rename action.
    */
    private Action renameAction;

   /**
    * Default remove action.
    */
    private Action removeAction;

   /**
    * Name of the entity.
    * @see #getName
    * @see #setName
    */
    private String name = "Untitled Entity";

   /**
    * List of primary views.
    * @see #addView
    */
    private final LinkedList primaryViews = new LinkedList();

   /**
    * Default view of the entity.
    */
    private View defaultView;

   /**
    * Supplimentary Property panels to be added to the Properties 
    * dialog.  The list containing <code>Component</code> instances 
    * will be used as the basis for construction of a set of views of
    * and entity presented as tabs within a Property Dialog. 
    * This class provides support for a default general property table 
    * view derived from the property names and values returned from the 
    * <code>getProperties</code> method.
    */
    private final LinkedList propertyPanels = new LinkedList();

   /**
    * A list of features of the entity.
    */
    private final List features = new LinkedList();

    private EntityService resolver;

    private Shell shell;

   /**
    * List of entity listeners.
    */
    private final LinkedList entityListeners = new LinkedList();

    private final List children = new LinkedList();

    //=========================================================================
    // Constructor
    //=========================================================================

    public DefaultEntity()
    {
        this( "Untitled" );
    }

    public DefaultEntity( String name )
    {
	  super( name );
	  this.name = name;
	  putValue("name", name );
    }

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
        m_logger = logger;
    }

    /**
     * Returns the current logging channel.
     * @return the Logger
     */
    protected final Logger getLogger()
    {
        return m_logger;
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
        Logger logger = m_logger;

        if( null != subCategory )
        {
            logger = m_logger.getChildLogger( subCategory );
        }

        setupLogger( component, logger );
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

    public void contextualize( Context context )
    throws ContextException
    {
        try
	  {
		final EntityContext c = (EntityContext) context;
        }
	  catch( Throwable e )
	  {
		throw new ContextException("cannot contextualize entity", e );
	  }
    }

    //=========================================================
    // Composable implementation
    //=========================================================

    public void compose( ComponentManager manager )
    throws ComponentException
    {
	  try
	  {
		resolver = (EntityService) manager.lookup("RESOLVER");
		shell = (Shell) manager.lookup("SHELL");
        }
        catch( Exception e )
        {
		throw new ComponentException("unexpected exception during composition phase", e );
        }
    }

    public EntityService getResolver()
    {
        return resolver;
    }

    public Shell getShell()
    {
        return shell;
    }

    //================================================================
    // Initializable
    //================================================================

   /**
    * Initialization is invoked by the framework following instance creation.
    * Once initiization has been invoked, all actions are sealed.
    * @exception Exception general exception
    */
    public void initialize()
    throws Exception
    {        
        initialized = true;
        features.add( new StaticFeature("class", getClass().getName() ));
        putValue("status","initialize");
    }

   /**
    * Returns the disposed state of the entity.
    */
    public boolean isDisposed()
    {
        return disposed;
    }

    //==========================================================
    // ActionListener
    //==========================================================

   /**
    * Establishes the modal dialog.
    */
    public void actionPerformed( ActionEvent event )
    {
    }

    //=========================================================================
    // EntityHandler
    //=========================================================================

   /**
    * Adds a <code>EntityListener</code>.
    */
    public void addEntityListener( EntityListener listener )
    {
	  if( listener == null ) throw new NullPointerException(
	     "attempting to add a null listener");
	  synchronized( entityListeners )
	  {
            entityListeners.add( listener );
	  }
    }

   /**
    * Removes a <code>ListListener</code>.
    */
    public void removeEntityListener( EntityListener listener )
    {
	  if( listener == null ) return;
	  synchronized( entityListeners )
	  {
            entityListeners.remove( listener );
	  }
    }

   /**
    * Proceses entity events on this handler.
    */
    private synchronized void fireEntityEvent( EntityEvent event )
    {
        synchronized( entityListeners )
	  {
	      Iterator iterator = entityListeners.iterator();
            while( iterator.hasNext() ) 
            {
		    EntityListener listener = null;
		    try
		    {
                    listener = (EntityListener) iterator.next();
			  iterator.remove();
	  	        listener.notifyEntityDisposal( event );
                }
		    catch( Exception e )
		    {
			  final String warning = "failed to notify a EntityListener, listener: ";
			  if( getLogger().isWarnEnabled() ) getLogger().warn( 
			    warning + listener );
		    }
	      }
	  }
    }

    //=========================================================================
    // ActionHandler
    //=========================================================================

   /**
    * Returns a list of Action instances to be installed as 
    * action menu items within the desktop when the entity 
    * is selected.
    * @return List the list of actions exposed by the entity
    */
    public List getActions( )
    {
        return actionActions;
    }

    //=========================================================================
    // Entity
    //=========================================================================

   /**
    * Returns the name of the <code>Entity</code> as a <code>String</code>.
    */
    public String getName()
    {
        return this.name;
    }

   /**
    * Returns the renamable state of the Entity.
    * @return boolean true if this entity is renamable.
    */
    public boolean renameable()
    {
        return true;
    }

   /**
    * Returns the removable state of the Entity.
    * @return boolean true if this entity is removable.
    */
    public boolean removable()
    {
        return true;
    }

   /**
    * Returns the <code>Action</code> object that will be handle
    * an action event signalling removal of the instance.
    * @return Action an <code>Action</code> that will remove the 
    * <code>Entity</code> 
    */
    public Action getRemoveAction()
    {
        if( removeAction != null ) return removeAction;
        removeAction = new RemoveAction( "Delete Entity", this );
        return removeAction;
    }

   /**
    * Set the name of the entity.
    *
    * @param name the new entity name
    */
    public void setName( String name )
    {
        if( name == null ) throw new RuntimeException(
  	    "DefaultEntity. Null name argument");
        if( name.length() < 1 ) throw new RuntimeException(
  	    "DefaultEntity. Bad name argument");

	  try
	  {
            if( name.equals( this.name )) return;
            this.name = name;
            putValue( "name", name );
            putValue( Action.NAME, getName() );
        }
	  catch( Exception e )
	  {
		ExceptionHelper.printException( 
              "DefaultEntity, unexpected exception while setting name.", e, this, trace );
		e.printStackTrace();
	  }
    }

   /**
    * Sets the icon representing the entity.
    * @param Icon iconic representation of the entity
    * @param size a value of LARGE or SMALL
    * @osm.warning current implementation ignores the size argument
    */
    public void setIcon( Icon icon, int size )
    {
        this.icon = icon;
    }

   /**
    * Returns the icon representing the entity.
    * @param size a value of LARGE or SMALL
    * @return Icon iconic representation of the entity
    * @osm.warning current implementation ignores the size argument
    */
    public Icon getIcon( int size )
    {
        return icon;
    }

   /**
    * Returns a list of named <code>Component</code> instances to be added 
    * to the Properties dialog.
    */
    public List getPropertyPanels()
    {
        return propertyPanels;
    }

   /**
    * Return the default <code>View</code> of this <code>Entity</code>
    */
    public View getView()
    {
        return new TabbedView( this );
    }

   /**
    * Returns list of the primary views of this <code>Entity</code>. 
    * The default implementation returns an empty list.  Classes specializing
    * <code>DefaultEntity</code> should use the <code>addView</code> method
    * during initialization to populate the list as required.
    * @return List list of <code>View</code> instances.
    */
    public List getViews()
    {
	  return primaryViews;
    }

   /**
    * Returns a list of <code>Features</code> instances to be added 
    * to the Properties dialog features panel.
    * @return List the list of <code>Feature</code> instances
    */
    public List getFeatures()
    {
        return features;
    }

   /**
    * Test if this entity if a leaf or a composite
    * @return boolean true if this is a leaf node
    */
    public boolean isaLeaf( )
    {
        return true;
    }

   /**
    * Returns a list of entities that represents the navigatable content
    * of the target entity. 
    * @return List the navigatable content
    */
    public List getChildren( )
    {
        return children;
    }

    //=========================================================================
    // Disposable
    //=========================================================================

    public synchronized void dispose()
    { 
        if( isDisposed() )
        {
		final String warning = "requesting disposal of a pre-disposed entity";
		if( getLogger().isWarnEnabled() ) getLogger().warn( warning );
	  }
	  final String info = "disposing of the entity";
        if( getLogger().isDebugEnabled() ) getLogger().debug( info );
	  fireEntityEvent( new EntityEvent( this ) );

	  //
	  // items and views linked to this entity should be listening for
	  // the disposal of this entity and handling their own disposal 
        // WARNING: not implemented yet
        //

	  disposed = true;
        putValue("status","dispose");
        renameAction = null;
        removeAction = null;
        defaultView = null;
    }

    //=========================================================================
    // implementation
    //=========================================================================

   /**
    * Add a view to the list of primary views.
    */
    public void addView( View view ) throws IllegalArgumentException, IllegalStateException
    {
	  if( view == null ) throw new IllegalArgumentException(
	    "DefaultEntity. Illegal attempt to add a null view to the primary view list.");

        primaryViews.add( view );
    }

    //=============================================================
    // Object (override)
    //=============================================================

    public String toString()
    {
        return getClass().getName() + 
		"[" +  
		"id=" + System.identityHashCode( this ) + " " +
		"name=" + name + " " +
            "]";
    }

    public boolean equals( Object object )
    {
        if( object == null ) return false;
	  if( !object.getClass().equals( this.getClass() ) ) return false;
        return ( System.identityHashCode( this ) == System.identityHashCode( object ));
    }

    private boolean equivalent( Entity entity )
    {
        if( entity == null ) return false;
	  if( !entity.getClass().equals( this.getClass() ) ) return false;
        return ( System.identityHashCode( this ) == System.identityHashCode( entity ));
    }

}
