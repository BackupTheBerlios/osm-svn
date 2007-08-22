
package net.osm.agent;


import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.omg.CORBA.Any;
import org.omg.CORBA.AnyHolder;
import org.omg.CORBA.BooleanHolder;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.ORB;
import org.omg.Session.Workspace;
import org.omg.Session.WorkspaceHelper;
import org.omg.Session.AbstractResource;
import org.omg.Session.AbstractResourcesHolder;
import org.omg.Session.AbstractResourceIteratorHolder;
import org.omg.Session.AbstractResourceHelper;
import org.omg.Session.AccessedBy;
import org.omg.Session.AccessedByHelper;
import org.omg.Session.ComposedOfHelper;
import org.omg.Session.ComposedOf;
import org.omg.Session.CollectsHelper;
import org.omg.Session.Collects;
import org.omg.CommunityFramework.ResourceFactory;
import org.omg.CommunityFramework.ResourceFactoryHelper;
import org.omg.CommunityFramework.ResourceFactoryProblem;
import org.omg.CommunityFramework.Criteria;
import org.omg.CollaborationFramework.ProcessorCriteria;
import org.omg.CosNaming.NameComponent;
import org.omg.CosLifeCycle.NoFactory;

import net.osm.agent.Agent;
import net.osm.agent.AgentIterator;
import net.osm.agent.CriteriaAgent;
import net.osm.agent.util.Collection;
import net.osm.agent.ProcessorCriteriaAgent;
import net.osm.agent.util.SequenceIterator;
import net.osm.agent.AbstractResourceAgent;
import net.osm.agent.LinkCollection;
import net.osm.shell.Entity;
import net.osm.shell.GenericAction;
import net.osm.shell.TablePanel;
import net.osm.shell.View;
import net.osm.shell.ScrollView;
import net.osm.util.ExceptionHelper;
import net.osm.util.IconHelper;

/**
 * WorkspaceAgent is a an agent encapsulating a remote reference to a Workspace.
 * @author Stephen McConnell
 */
public class WorkspaceAgent extends AbstractResourceAgent implements CriteriaActionHandler
{

    //=========================================================================
    // static
    //=========================================================================

    private static final String path = "net/osm/agent/image/workspace.gif";
    private static final ImageIcon icon = IconHelper.loadIcon( path );

    //=========================================================================
    // state
    //=========================================================================

    protected Workspace workspace;

    protected ResourceFactory factory;

    private CriteriaAgent[] criteria;

    private List processorCriteria;
    private List resourceCriteria;

    private List list;

    private View contentView;

    private List createList;

    private View templateView;
    private CriteriaTableModel criteriaModel;

    private LinkCollection containsCollection;
    private LinkTableModel containsTable;

    private LinkCollection accessedByCollection;
    private LinkTableModel accessedByTable;

    private List actions;

    private LinkCollection subWorkspaceList;

    //=========================================================================
    // Initializable
    //=========================================================================

    public void initialize()
    throws Exception
    {
        super.initialize();

        //
        // Populate primary views.
	  //

        addView( getContentsView() );

	  //
	  // set the icon
	  //

        setIcon( icon, Entity.SMALL );
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
		// new resource into this workspace
		//

            AbstractResourceAgent agent = (AbstractResourceAgent) getResolver().resolve( factoryResource );
            if( agent instanceof ProcessorAgent ) 
		{
		    UserAgent.getUserAgent().createTask( 
		      criteria.getName(), (ProcessorAgent) agent );
		}
		else
	      {
		    addResource( agent );
		}
	  }
	  catch( Throwable e )
	  {
		final String error = "unable to instantiate an agent";
		throw new RuntimeException( error, e );
	  }
    }

    //=========================================================================
    // WorkspaceAgent
    //=========================================================================

   /**
    * Adds a resource to the Workspace.
    * @param agent the agent to add to the workspace
    */
    public void addResource( AbstractResourceAgent agent )
    {
        try
	  {
            workspace.add_contains_resource( agent.getAbstractResource() );
	  }
	  catch( Exception e )
	  {
            ExceptionHelper.printException("Unable to add resource to workspace.", e );
	  }
    }

   /**
    * Removes a resource from the Workspace.
    * @param agent the agent to remove from the workspace
    */
    public void removeResource( AbstractResourceAgent agent )
    {
        try
	  {
		System.out.println("removing resource: " + agent );
            workspace.remove_contains_resource( agent.getAbstractResource() );
	  }
	  catch( Exception e )
	  {
            ExceptionHelper.printException("Unable to add resource to workspace.", e );
	  }
    }

   /**
    * Creates a sub-workspace contained by and part of the structure of this workspace.
    */
    public void createWorkspace()
    {
        try
	  {
            workspace.create_subworkspace("Untitled Workspace", new org.omg.Session.User[0] );
	  }
	  catch( Exception e )
	  {
            ExceptionHelper.printException("Unable to create workspace", e, this );
	  }
    }

   /**
    * Get the factory exposed by the primary Workspace.
    * @returns ResourceFactory
    */
    private ResourceFactory getFactory() throws NoFactory
    {
        if( factory == null ) try
	  {
	      org.omg.CORBA.Object[] factories = 
              workspace.find_factories( new NameComponent[0] );
		factory = ResourceFactoryHelper.narrow( factories[0] );
	  }
	  catch( org.omg.CORBA.NO_PERMISSION e )
	  {
		throw new NoFactory();
	  }
	  catch( NoFactory noFactory )
	  {
		throw new NoFactory();
	  }
	  catch( Exception e )
	  {
            throw new RuntimeException(
              "WorkspaceAgent, Unable to resolve factories.", e );
        }
        return factory;
    }

    public List getServices()
    {
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
    * Returns a list of <code>CriteriaAgent</code> instances available though the 
    * primary object factory interface.
    * @return List list of <code>CriteriaAgent</code> instances 
    */
    public List getFactoryCriteria()
    {
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
            throw new RuntimeException(
              "Failed to resolve supported criteria.", e );
        }

        return new LinkedList( Arrays.asList( criteria ) );
    }

    private CriteriaTableModel getCriteriaModel()
    {
        if( criteriaModel != null ) return criteriaModel;
	  criteriaModel = new CriteriaTableModel( getFactoryCriteria() );
	  return criteriaModel;
    }

    public List getContents( )
    {
	  if( containsCollection != null ) return containsCollection;
	  try
	  {
            containsCollection = 
              new LinkCollection( 
		    getLogger().getChildLogger("contains"), 
		    getOrb(), getResolver(), workspace, audit, 
		    CollectsHelper.type(), Collects.class );
		return containsCollection;
	  }
	  catch( Exception e )
	  {
		final String error = "unable to create contains collection";
            if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
		throw new RuntimeException( error, e );
	  }
    }

    public List getAccessedBy( )
    {
	  if( accessedByCollection != null ) return accessedByCollection;
	  try
	  {
            accessedByCollection = 
              new LinkCollection( 
		    getLogger().getChildLogger("accessedby"), getOrb(), getResolver(), workspace, audit, 
		    AccessedByHelper.type(), AccessedBy.class );
		return accessedByCollection;
	  }
	  catch( Exception e )
	  {
		final String error = "unable to create new accessed by collection";
            if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
		throw new RuntimeException( error, e );
	  }
    }

    private LinkTableModel getAccessedByTable()
    {
        if( accessedByTable != null ) return accessedByTable;
	  accessedByTable = new LinkTableModel( (LinkCollection) getAccessedBy( ) );
	  return accessedByTable;
    }

    private LinkTableModel getContainsTable()
    {
        if( containsTable != null ) return containsTable;
        containsTable = new LinkTableModel( (LinkCollection) getContents() );	
	  return containsTable;
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
	      this.workspace = WorkspaceHelper.narrow( (org.omg.CORBA.Object) value );
        }
        catch( Exception local )
        {
            throw new RuntimeException( 
		  "WorkspaceAgent. Bad primary object reference.", local );
        }
    }

   /**
    * The <code>getType</code> method returns a human-friendly name of the entity.
    */
    public String getType( )
    {
	  return "Workspace";
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
    * of the workspace. 
    * @return List the navigatable content
    */
    public List getChildren( )
    {
	  if( subWorkspaceList != null ) return subWorkspaceList;
	  try
	  {
            subWorkspaceList = 
              new LinkCollection( 
		    getLogger().getChildLogger("contains"), 
		    getOrb(), getResolver(), workspace, audit, 
		    CollectsHelper.type(), Collects.class, WorkspaceHelper.id()
              );
		return subWorkspaceList;
	  }
	  catch( Exception e )
	  {
		final String error = "unable to create children collection";
            if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
		throw new RuntimeException( error, e );
	  }
    }

   /**
    * The <code>getPropertyPanels</code> operation returns a sequence of panels 
    * representing different views of the content and/or associations maintained by
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
            list.add( 
              new ScrollView( 
                new ContainsTablePanel( this, "Resources", getContainsTable(), 
			new LinkColumnModel(
			  getShell().getDefaultFont()
			)
		    )
              )
            );
		list.add( 
		  new ScrollView( 
                new TablePanel( 
			this, "Access", getAccessedByTable(), 
 			new LinkColumnModel(
			  getShell().getDefaultFont()
			)
		    )
		  )
		);
		list.add( getTemplatesView() );
	  }
        return list;
    }

    public View getContentsView()
    {
        if( contentView == null )
        {	
	      contentView = new ScrollView( 
                new ContainsTablePanel( this, "Resources", getContainsTable(),
 			new LinkColumnModel(
			  getShell().getDefaultFont()
			)
                )
            );
        }
        return contentView;
    }

    public View getTemplatesView()
    {
        if( templateView == null )
        {	
	      templateView = new ScrollView( 
                new TablePanel( 
			this, "Services", getCriteriaModel(), 
			new CriteriaColumnModel( 
			  getShell().getDefaultFont()
			)
		    )
            );
        }
        return templateView;
    }

    //=========================================================================
    // Disposable
    //=========================================================================

   /**
    * The <code>dispose</code> method is invoked prior to removal of the 
    * agent.  The implementation handles cleaning-up of state members.
    */
    public void dispose()
    {
        if( containsCollection != null ) containsCollection.dispose();
        if( accessedByCollection != null ) accessedByCollection.dispose();
        if( subWorkspaceList != null ) subWorkspaceList.dispose();
	  super.dispose();
    }
}
