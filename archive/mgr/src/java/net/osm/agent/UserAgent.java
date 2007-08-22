
package net.osm.agent;

import java.util.Arrays;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import javax.swing.ImageIcon;
import javax.swing.Icon;
import javax.swing.Action;
import java.awt.event.ActionEvent;
import java.security.cert.CertPath;

import org.omg.CORBA.Any;
import org.omg.CORBA.AnyHolder;
import org.omg.CORBA.BooleanHolder;
import org.omg.CORBA.ORB;
import org.omg.CosLifeCycle.NoFactory;
import org.omg.CosNotification.EventType;
import org.omg.CosNaming.NameComponent;
import org.omg.Session.AbstractResource;
import org.omg.Session.Workspace;
import org.omg.Session.WorkspacesHolder;
import org.omg.Session.WorkspaceIteratorHolder;
import org.omg.Session.TasksHolder;
import org.omg.Session.TaskIteratorHolder;
import org.omg.Session.SystemMessage;
import org.omg.Session.SystemMessageHelper;
import org.omg.Session.SystemMessagesHolder;
import org.omg.Session.SystemMessageIterator;
import org.omg.Session.Desktop;
import org.omg.Session.User;
import org.omg.Session.Task;
import org.omg.Session.UserHelper;
import org.omg.Session.connect_state;
import org.omg.Session.Owns;
import org.omg.Session.OwnsHelper;
import org.omg.Session.Accesses;
import org.omg.Session.AccessesHelper;
import org.omg.Session.WorkspaceHelper;
import org.omg.CommunityFramework.Criteria;
import org.omg.CommunityFramework.ResourceFactoryProblem;
import org.omg.CommunityFramework.Criteria;
import org.omg.CommunityFramework.GenericResource;
import org.omg.CommunityFramework.GenericResourceHelper;
import org.omg.CommunityFramework.GenericCriteria;

import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.Component;

import net.osm.hub.home.ResourceFactory;
import net.osm.hub.home.ResourceFactoryHelper;
import net.osm.agent.AbstractResourceAgent;
import net.osm.agent.LinkCollection;
import net.osm.agent.util.Collection;
import net.osm.shell.vault.VaultEntity;
import net.osm.audit.RemoteEvent;
import net.osm.shell.View;
import net.osm.shell.Feature;
import net.osm.shell.GenericAction;
import net.osm.shell.TablePanel;
import net.osm.shell.StaticFeature;
import net.osm.shell.ActiveFeature;
import net.osm.shell.ScrollView;
import net.osm.shell.TabbedView;
import net.osm.shell.Service;
import net.osm.pki.pkcs.PKCS10;
import net.osm.pki.pkcs.PKCS10Wrapper;
import net.osm.pki.pkcs.PKCS10Helper;
import net.osm.util.ExceptionHelper;
import net.osm.util.IconHelper;
import net.osm.vault.Vault;

/**
 * UserAgentis a an agent encapsulating a remote reference to a User.
 * @author Stephen McConnell
 */
public class UserAgent extends AbstractResourceAgent 
implements CriteriaActionHandler
{

    //=========================================================================
    // static
    //=========================================================================

    private static final String path = "net/osm/agent/image/user.gif";
    private static final ImageIcon icon = IconHelper.loadIcon( path );

    private static final EventType[] removals = new EventType[0];
    private static final EventType[] additions = new EventType[]
    { 
	  new EventType("org.omg.session","property"),
	  new EventType("org.omg.session","connected"),
	  new EventType("org.omg.session","enqueue"),
	  new EventType("org.omg.session","dequeue"),
    };

    private static UserAgent principalUserAgent;
    public static UserAgent getUserAgent()
    {
        return principalUserAgent;
    }

    //=========================================================================
    // state
    //=========================================================================

   /**
    * The primary User object reference that this UserAgent represents.
    */
    protected User user;

   /**
    * Cached reference to the user's desktop.
    */
    protected DesktopAgent desktop;

   /**
    * Cached list of criteria extracted from the User's factory.
    */
    private CriteriaAgent[] criteria;

   /**
    * Cached reference to the User's factory.
    */
    private ResourceFactory factory;

   /**
    * Connected state of the user.
    */
    private boolean connected = false;

    private List list;
    private List features;
    private ScrollView tasks;
    private ScrollView workspaces;
    private ScrollView messages;
    private LinkCollection tasksList;
    private LinkCollection workspacesList;
    private TabbedView primaryView;

    private LinkTableModel workspacesModel; 
    private CriteriaTableModel criteriaModel;
    private TasksTableModel tasksModel;
    private MessagesTableModel messagesModel;

    Action userConnectAction;
    Action userDisconnectAction;

    private LinkTableModel containsTable;
    private View contentView;
    private CertPath certificatePath;
    private Vault vault;
    private List toolsList;
    private UserAgent self;
    private ComponentManager manager;

    private List actions;
    private List processorCriteria;
    private List resourceCriteria;

    //=========================================================================
    // Local constructor
    //=========================================================================

   /**
    * Default constructor.
    */
    public UserAgent( )
    {
	  super();
    }

    //=========================================================================
    // Agent
    //=========================================================================

   /**
    * Set the resource that is to be presented.
    */
    public void setPrimary( Object value ) 
    {
	  super.setPrimary( value );
	  try
        {
 	      this.user = UserHelper.narrow( (org.omg.CORBA.Object) value );
        }
        catch( Throwable e )
        {
		final String error = "supplied primary type missmatch";
		if( getLogger().isErrorEnabled() ) getLogger().error( error + 
		  "\n\tSUPPLIED: " + value.getClass().getName() +
		  "\n\tREQUIRED: " + UserHelper.id(), e );
            throw new RuntimeException( error, e );
        }
    }

   /**
    * Returns the object reference for the user.
    */
    public User getUser()
    {
        return this.user;
    }

   /**
    * The <code>getType</code> method returns a human-friendly name of the entity.
    */
    public String getType( )
    {
	  return "User";
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
        super.compose( manager );
        this.manager = manager;
    }

    //================================================================
    // Initializable
    //================================================================
    
   /**
    * Initialization is invoked by the framework following instance creation
    * and contextualization.  
    */
    public void initialize()
    throws Exception
    {        

        super.initialize();

        principalUserAgent = this;

        //
        // get the user's connected state
        //

        try
	  {
            connected = ( user.connectstate() == connect_state.connected );
 	  }
	  catch( org.omg.CORBA.NO_PERMISSION e )
	  {
		final String warning = "server denied connection state access";
		if( getLogger().isWarnEnabled() ) getLogger().warn( warning, e );
	  }
	  catch( Throwable e )
	  {
		final String warning = "unable to resolve user connected state";
		if( getLogger().isWarnEnabled() ) getLogger().warn( warning, e );
	  }

	  //
	  // add an action to handle user connection and disconnection
	  //
 
        userConnectAction = new GenericAction( "Connect", this, "connect", true );
        userDisconnectAction = new GenericAction( "Disconnect", this, "disconnect", false );
	  userConnectAction.setEnabled( !connected );
        userDisconnectAction.setEnabled( connected );

        //
        // Populate primary views.
	  //

        addView( getContentsView() );
        addView( getMessagesView() );
        addView( getTasksView() );

        //
        // set event subscriptions
	  //

	  //try
	  //{
        //    if( this.adapter != null ) 
	//	{
	//	    adapter.update( additions, removals );
	//	}
	  //}
	  //catch( Exception e )
	  //{
	//	final String warning = "unable to update event subscription";
	//	if( getLogger().isWarnEnabled() ) getLogger().warn( warning, e );
	  //}

    }

    //=========================================================================
    // CriteriaActionHandler
    //=========================================================================

    public void handleCriteriaCallback( ActionEvent event, CriteriaAgent criteria ) 
    throws ResourceFactoryProblem
    {
        if( event == null )
	  {
		final String error = "null event argument";
	      throw new NullPointerException( error );
	  }

        if( criteria == null )
	  {
		final String error = "null criteria argument";
	      throw new NullPointerException( error );
	  }

	  ResourceFactory factory = null;
	  AbstractResource factoryResource = null;
        try
	  {
		factory = getFactory();
            factoryResource = factory.create( 
	        criteria.getName(), criteria.getCriteria() 
            );
	  }
	  catch( NoFactory nf )
	  {
		final String error = "failed to locate factory";
		throw new RuntimeException( error, nf );
        }

        try
	  {
		//
		// if the resource created by the factory is a processor, then 
		// publish a task attached to the processor, otherwise, put the 
		// new resource into the desktop
		//

            AbstractResourceAgent agent = (AbstractResourceAgent) getResolver().resolve( factoryResource );
            if( agent instanceof ProcessorAgent ) 
		{
		    createTask( criteria.getName(), (ProcessorAgent) agent );
		}
		else
	      {
		    getDesktop().addResource( agent );
		}
	  }
	  catch( Throwable e )
	  {
		final String error = "unable to instantiate an agent";
		throw new RuntimeException( error, e );
	  }
    }


    //=========================================================================
    // RemoteEventListener
    //=========================================================================

   /**
    * Method invoked when an an event has been received from a 
    * remote source signalling a state change in the source
    * object.
    */
    public void remoteChange( RemoteEvent event )
    {
	  if( event.getDomain().equals("org.omg.session") )
	  {
	      if( event.getType().equals("connected") )
		{
		    connected = event.getProperty("connected").extract_boolean();
		    putValue("connected", new Boolean( connected ) );
	          userConnectAction.setEnabled( !connected );
                userDisconnectAction.setEnabled( connected );
		    return;
	      }
	  }
        super.remoteChange( event );
    }

    //=========================================================================
    // UserAgent
    //=========================================================================

   /**
    * Returns the renamable state of the Entity.
    * @return boolean true if this entity is renamable.
    */
    public boolean renameable()
    {
	  try
	  {
            return this.equals( manager.lookup("USER"));
	  }
	  catch( Throwable e )
	  {
		return false;
	  }
    }

   /**
    * Returns the removable state of the Entity.
    * @return boolean true if this entity is removable.
    */
    public boolean removable()
    {
        return false;
    }

   /**
    * Action handler that redirects a create workpace requet to the User's 
    * Desktop.
    */
    public void createWorkspace()
    {
        getDesktop().createWorkspace();
    }

   /**
    * Adds a resource to the Desktop of this User.
    * @param agent the agent to add to the user's desktop
    */
    public void addResource( AbstractResourceAgent agent )
    {
        getDesktop().addResource( agent );
    }

   /**
    * Returns the connected state of the user.
    * @return boolean - true if the user is connected otherwise false
    */
    public boolean getConnected()
    {
        return connected;
    }

   /**
    * Change the connected state of the user to true.
    */
    public void connect()
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug( "connect" );
	  try
	  {
            user.connect();
	  }
	  catch( Exception e )
	  {
		final String error = "remote exception while attempting to connect the user";
		if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
		throw new RuntimeException( error, e );
	  }
    }

   /**
    * Change the connected state of the user to true.
    */
    public void disconnect()
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug( "disconnect" );
	  try
	  {
            user.disconnect();
	  }
	  catch( Exception e )
	  {
		final String error = "remote exception while attempting to disconnect the user";
		if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
		throw new RuntimeException( error, e );
	  }
    }

   /**
    * Returns the user's desktop.
    * @return DesktopAgent - the user's desktop.
    */
    public DesktopAgent getDesktop()
    {
	  if( desktop != null ) return desktop;
	  Desktop d = null;
	  try
	  {
	      d = user.get_desktop();
	  }
	  catch( Throwable e )
	  {
		final String error = "remote exception while attempting resolve desktop";
		if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
		throw new RuntimeException( error, e );
	  }

	  try
	  {
		desktop = (DesktopAgent) getResolver().resolve( d );
            return desktop;
        }
        catch( Throwable e )
        {
		final String error = "local exception while attempting to create desktop agent";
		if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
		throw new RuntimeException( error, e );
        }
    }

   /**
    * Returns a active list of tasks owned by the primary user.
    * @return <code>List</code> of TaskAgent instances 
    */
    public List getTasks()
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug("getTasks");
	  if( tasksList != null ) return tasksList;
	  try
	  {
            tasksList = new LinkCollection( 
		  getLogger().getChildLogger("tasks"), 
	        getOrb(), getResolver(), user, audit, 
		  OwnsHelper.type(), Owns.class );
            return tasksList;
	  }
	  catch( Exception e )
	  {
		final String error = "unable to resolve a task list";
		if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
		throw new RuntimeException( error, e );
	  }
    }

    private LinkTableModel getTasksModel()
    {
        if( tasksModel != null ) return tasksModel;
	  tasksModel = new TasksTableModel( (LinkCollection) getTasks() );
	  return tasksModel;
    }

   /**
    * Returns a active list of workspaces accessed by the primary user.
    * @return <code>List</code> of WorkspaceAgent instances 
    */
    public List getWorkspaces()
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug("getWorkspaces");
	  if( workspacesList != null ) return workspacesList;
	  try
	  {
            workspacesList = new LinkCollection( 
		  getLogger().getChildLogger("accesses"),
		  getOrb(), getResolver(), user, audit, 
		  AccessesHelper.type(), Accesses.class );
		return workspacesList;
	  }
	  catch( Exception e )
	  {
		final String error = "unable to resolve a workspace list";
		if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
		throw new RuntimeException( error, e );
	  }
    }

   /**
    * Utility method to return the collection of workspaces contained in the 
    * desktop in the form of a table model.
    */
    private LinkTableModel getWorkspacesModel()
    {
        if( workspacesModel != null ) return workspacesModel;
	  workspacesModel = new LinkTableModel( (LinkCollection) getWorkspaces() );
	  return workspacesModel;
    }

   /**
    * Returns an list of messages queued against the primary user.
    * @return <code>List</code> of SystemMessageAgent instances 
    * @osm.warning the list returned from this operation will not reflect 
    *   changes to the state of the primary user
    */
    public List getMessages()
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug("getMessages");
        try
        {
		SystemMessageIterator messages = user.get_messages( 0, new SystemMessagesHolder() );
		MessageCollection messageCollection = new MessageCollection( 
		  getLogger().getChildLogger("messages"), 
		  orb, getResolver(), user, audit, messages );
	      return messageCollection;
        }
        catch( Throwable e )
        {
		final String error = "unable to resolve a message collection";
		if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
		throw new RuntimeException( error, e );
        }
    }

    private MessagesTableModel getMessagesModel()
    {
        if( messagesModel != null ) return messagesModel;
	  messagesModel = new MessagesTableModel( getMessages() );
	  return messagesModel;
    }

   /**
    * Queues a message against the user.
    * @param message - the message to queue against the user 
    */
    public void doEnqueue( SystemMessage message )
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug( "enque" );
        try
        {
		user.enqueue( message );
        }
        catch( Exception e )
        {
		final String error = "unable to enqueue message";
		if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
		throw new RuntimeException( error, e );
        }
    }

   /**
    * Creation and assignment of a new Task to the User.
    * @param name the name of the task
    * @param processor the processor to assign to the task
    */
    public TaskAgent createTask( String name, ProcessorAgent processor )
    {
        return createTask( name, processor, null );
    }

   /**
    * Creation and assignment of a new Task to the User.
    * @param name the name of the task
    * @param processor the processor to assign to the task
    * @param data the initial task input argument
    */
    public TaskAgent createTask( String name, ProcessorAgent processor, AbstractResourceAgent data )
    {

        if( getLogger().isDebugEnabled() ) getLogger().debug( "createTask" );

	  TaskAgent agent = null;
	  Task task = null;
        try
	  {
	      AbstractResource input = null;
	      if( data != null ) input = data.getAbstractResource();
            task = user.create_task( name, processor.getProcessor(), input );
            agent = (TaskAgent) getResolver().resolve( task );
		return agent;
	  }
	  catch( Throwable e )
	  {
		final String error = "Unable to create task.";
		if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
		if( agent != null )
		{
		    try
		    {
			  agent.remove();
		    }
		    catch( Throwable ignorable )
		    {
		    }
		    finally
		    {
			  agent.dispose();
		    }
		}
		else if( task != null )
		{
		    try
		    {
			  task.remove();
		    }
		    catch( Throwable ignorable )
		    {
		    }
		}
		throw new RuntimeException( error, e );
	  }
    }

    public List getServices()
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug( "getServices" );

        if( processorCriteria != null ) return processorCriteria;
        processorCriteria = new LinkedList();
	  Iterator iterator = getFactoryCriteria().iterator();
        while( iterator.hasNext() )
        {
		Object next = iterator.next();
		if( next instanceof ProcessorCriteriaAgent ) processorCriteria.add( next );
        }
        return processorCriteria;
    }

    public List getResourceServices()
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug( "getResourceServices" );

        if( resourceCriteria != null ) return resourceCriteria;
        resourceCriteria = new LinkedList();
	  Iterator iterator = getFactoryCriteria().iterator();
        while( iterator.hasNext() )
        {
		Object next = iterator.next();
		if( !( next instanceof ProcessorCriteriaAgent )) resourceCriteria.add( next );
        }
        return resourceCriteria;
    }

   /**
    * Returns a list of <code>CriteriaAgent</code> instances availble though the 
    * primary object's factory interface.
    * @return List list of <code>CriteriaAgent</code> instances 
    * @osm.warning the list returned from this operation is not maintained
    */
    public List getFactoryCriteria()
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug( "getFactoryCriteria" );
	  if( criteria == null ) try
	  {
		Criteria[] array = getFactory().supporting();
		criteria = new CriteriaAgent[ array.length ];
	      for( int i=0; i<array.length; i++ )
	      {
		    CriteriaAgent c = (CriteriaAgent) getResolver().resolve( (Criteria) array[i] );
		    c.setCallback( this );
		    criteria[i] = c;
	      }
	  }
	  catch( NoFactory noFactory )
	  {
		return new LinkedList();
	  }
	  catch( Exception e )
	  {
		final String error = "unable to resolve supported factory criteria";
		if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
		throw new RuntimeException( error, e );
        }
        return new LinkedList( Arrays.asList( criteria ) );
    }

    private CriteriaTableModel getCriteriaModel()
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug( "getting criteria model" );
        if( criteriaModel != null ) return criteriaModel;
	  criteriaModel = new CriteriaTableModel( getFactoryCriteria() );
	  return criteriaModel;
    }

   /**
    * Get the factory exposed by the primary User.
    * @returns ResourceFactory
    */
    private ResourceFactory getFactory() throws NoFactory
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug( "getFactory" );
        if( factory != null ) return factory;
        try
	  {
	      org.omg.CORBA.Object[] factories = 
              user.find_factories( new NameComponent[0] );
		factory = ResourceFactoryHelper.narrow( factories[0] );
		return factory;
	  }
	  catch( org.omg.CORBA.NO_PERMISSION e )
	  {
		final String warning = "unable to access factory";
		if( getLogger().isWarnEnabled() ) getLogger().warn( warning, e );
		throw new NoFactory();
	  }
	  catch( NoFactory noFactory )
	  {
		throw new NoFactory();
	  }
	  catch( Exception e )
	  {
		final String error = "unable to resolve factory reference";
		if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
		throw new RuntimeException( error, e );
        }
    }

   /**
    * Method called to initate a certificate certification request object.
    * @osm.warning error handling needs upgrading
    */
    public void certify()
    {
        //if( shell != null ) shell.setMessage("Creating PKCS#10 certificate request");

        //
        // start a thread to construct the PKCS10 object
        //
        
        Thread thread = new Thread(
        new Runnable() {
            public void run()
            {
                try
                {
		        PKCS10 request = new PKCS10Wrapper( ((VaultEntity)vault).getSubject() );
	              certify( request );
                }
                catch (Exception e)
                {
			  final String warning =  "failed to initalize PKCS10 instance";
			  if( getLogger().isWarnEnabled() ) getLogger().warn( warning, e );
                    throw new RuntimeException( warning, e );
                }
            }
        }
        );
        thread.start();
    }


   /**
    * Method called to initate a certificate certification request wrapped within 
    * a GenericResource.
    */
    private void certify( PKCS10 request )
    {
        System.out.println("handle certification in user");
        try
        {
		//
		// create a task to handle this request
		//

		ResourceFactory factory = getFactory();
		Criteria r = factory.criterion( "certificate-request" );
		GenericResource generic = GenericResourceHelper.narrow( getFactory().create( 
              "PKCS10 Certification Request", r ) );
		generic.set_value( request );
		GenericResourceAgent pkcs10 = (GenericResourceAgent) getResolver().resolve( generic ); 
		getDesktop().addResource( pkcs10 );

           /*
            Criteria c = factory.criterion( "certification" );
            AbstractResource p = getFactory().create( "Certification Process", c );
		ProcessorAgent processor = (ProcessorAgent) getResolver().resolve( p );
            TaskAgent task = createTask( "Certification Request", processor );
		task.addConsumed( pkcs10, "pkcs10" );

		//
		// Need to figure out how we handle task completion and provide
		// local agent support for reception of completion events.
	      // In this case, we need to be able to associate the action of 
	      // updating the vault with the certificate that is sent back from 
            // the PKI server - this also raises the question - should we be 
		// creating the PKCS10 resource and declaring actions e.g. 
		// "submit-cert-request" for instances of that type instead of 
		// doing the task generation (or both) ?
		// 

		if( processor.verify() ) task.start();
            */

	  }
	  catch( Exception e )
	  {
	      final String warning =  "failed to create a new PKCS10 certificate request";
		if( getLogger().isWarnEnabled() ) getLogger().warn( warning, e );
            throw new RuntimeException( warning, e );
	  }
    }

    //=========================================================================
    // Entity
    //=========================================================================

   /**
    * Test if this entity if a leaf or a composite
    * @return boolean true if this is a leaf entity
    */
    public boolean isaLeaf( )
    {
        return false;
    }

   /**
    * Returns a list of entities that represents the navigatable content
    * of the target entity. 
    * @return List the navigatable content
    */
    public List getChildren( )
    {
        return getDesktop().getChildren( );
    }

   /**
    * Returns a list of Action instances to be installed as 
    * action menu items within the desktop when the entity 
    * is selected.
    */
    public List getActions( )
    {
	  if( actions != null ) return actions;
	  actions = super.getActions();
	  actions.add( userConnectAction );
	  actions.add( userDisconnectAction );
        return actions;
    }

   /**
    * Returns the icon representing the entity.
    * @param size a value of LARGE or SMALL
    * @return Icon iconic representation of the entity
    * @osm.note only small icons are currently implemented
    */
    public Icon getIcon( int size )
    {
        return icon;
    }

    public View getView()
    {
        if( primaryView == null ) primaryView = new TabbedView( this );
        return primaryView;
    }

   /**
    * The <code>getPropertyPanels</code> operation returns a sequence of panels 
    * representing secondary views of the content and/or associations maintained by
    * and agent.
    */
    public List getPropertyPanels()
    {
	  //
	  // get all of the supertype panels
	  // together with the local panels
        //

	  if( list == null )
        {
	      list = super.getPropertyPanels();
	      list.add( getWorkspacesView() );
            list.add( 
              new ScrollView( 
                new TablePanel( 
			this, "Services", getCriteriaModel(), 
			new CriteriaColumnModel( 
			  getShell().getDefaultFont() 
			) 
		    )
              )
            );
	  }
        return list;
    }

    public View getContentsView()
    {
        if( contentView == null )
        {	
	      contentView = new ScrollView( 
                new DesktopTablePanel( 
			this, getDesktop(), "Resources", getContainsTable(), 
			new LinkColumnModel( getShell().getDefaultFont() )
		    )
            );
        }
        return contentView;
    }

    private LinkTableModel getContainsTable()
    {
        if( containsTable != null ) return containsTable;
	  containsTable = new LinkTableModel( (LinkCollection) getDesktop().getContents() );
	  return containsTable;
    }

    public View getTasksView()
    {
        if( tasks == null )
        {	
	      tasks = new ScrollView( 
              new TasksTablePanel( this, "Tasks", 
		    getTasksModel(),
		    new TasksColumnModel(
			  getShell().getDefaultFont()
	          )
              )
            );
        }
        return tasks;
    }

    public View getWorkspacesView()
    {
        if( workspaces == null )
        {	
		workspaces = new ScrollView( 
              new TablePanel( 
		    this, "Access", getWorkspacesModel(), 
		    new LinkColumnModel( getShell().getDefaultFont() )
		  )
		);
        }
        return workspaces;
    }

    public View getMessagesView()
    {
        if( messages == null )
        {	
	      messages = new ScrollView( 
	        new MessagesTablePanel( 
			this, "Messages", getMessagesModel(), 
			new MessageColumnModel( getShell().getDefaultFont() )
		  )
            );
        }
        return messages;
    }

   /**
    * Returns a list of <code>Features</code> instances to be presented under 
    * the Properties dialog features panel.
    * @return List the list of <code>Feature</code> instances
    */
    public List getFeatures()
    {
        if( features != null ) return features;

        List list = super.getFeatures();
	  try
	  {
            list.add( new ActiveFeature( this, "connected", "getConnected", "connected" ));
        }
	  catch( Exception e )
	  {
	      return list;
	  }
	  this.features = list;
	  return features;
    }

   /**
    * The <code>dispose</code> method is invoked prior to removal of the 
    * agent.  The implementation handles cleaning-up of state members.
    */
    public void dispose()
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug("dispose");

        try
        {
            getDesktop().dispose();
        }
        catch( Throwable anything ){}

        try
        {
            if( tasksList != null ) tasksList.dispose();
        }
        catch( Throwable anything ){}

        try
        {
            if( workspacesList != null ) workspacesList.dispose();
        }
        catch( Throwable anything ){}

        list = null;
        features = null;
        tasks = null;
        workspaces = null;
        messages = null;
        tasksList = null;
        workspacesList = null;
        primaryView = null;
        workspacesModel = null;
        criteriaModel = null;
        tasksModel = null;
        messagesModel = null;
        userConnectAction = null;
        userDisconnectAction = null;
        containsTable = null;
        contentView = null;
        certificatePath = null;
        vault = null;
        toolsList = null;
        self = null;
        manager = null;
        actions = null;
        processorCriteria = null;
        resourceCriteria = null;

	  super.dispose();

    }
}
