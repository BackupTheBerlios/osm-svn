
package net.osm.agent.workspace;


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
import org.omg.Session.AccessedByHelper;
import org.omg.Session.ComposedOfHelper;
import org.omg.Session.ComposedOf;
import org.omg.CommunityFramework.ResourceFactory;
import org.omg.CommunityFramework.ResourceFactoryHelper;
import org.omg.CommunityFramework.Criteria;
import org.omg.CollaborationFramework.ProcessorCriteria;
import org.omg.CosNaming.NameComponent;
import org.omg.CosLifeCycle.NoFactory;

import net.osm.shell.Utility;
import net.osm.agent.Agent;
import net.osm.agent.AgentIterator;
import net.osm.agent.CriteriaAgent;
import net.osm.agent.ProcessorCriteriaAgent;
import net.osm.agent.util.Collection;
import net.osm.agent.util.SequenceIterator;
import net.osm.agent.AbstractResourceAgent;
import net.osm.agent.LinkCollection;
import net.osm.shell.base.Item;
import net.osm.shell.base.View;
import net.osm.shell.panel.DefaultPanel;
import net.osm.shell.panel.DefaultItem;
import net.osm.shell.panel.FlatPanel;
import net.osm.shell.panel.NullPanel;
import net.osm.shell.panel.GenericAction;
import net.osm.shell.view.ScrollView;
import net.osm.shell.control.MenuBar;

import net.osm.util.ExceptionHelper;

public class WorkspaceAgent extends AbstractResourceAgent
{

    //=========================================================================
    // Members
    //=========================================================================

    private static final String path = "net/osm/agent/image/workspace.gif";
    private static final ImageIcon icon = Utility.loadIcon( path );

    protected Workspace workspace;

    protected ResourceFactory factory;

    private CriteriaAgent[] criteria;

    private List list;

    private View contentView;

    private View templateView;

    private List createList;

    private LinkCollection containsCollection;

    //=========================================================================
    // Constructor
    //=========================================================================

    public WorkspaceAgent( )
    {
	  super();
    }

    public WorkspaceAgent( ORB orb, Workspace reference )
    {
        super( orb, reference );
	  setPrimary( reference );
    }

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
            throw new RuntimeException( "Bad primary object reference.", local );
        }
    }

    //=========================================================================
    // Initializable
    //=========================================================================

    public void initialize()
    throws Exception
    {

        //
	  // add all of the criteria instances from the workspaces factory
	  // to the list of actions
	  //

	  try
	  {
	      Iterator iterator = getFactoryCriteria().iterator();
            while( iterator.hasNext() )
	      {
		    addAction( (Action) iterator.next(), MenuBar.ACTION_MENU_GROUP );
            }
	  }
	  catch( org.omg.CORBA.NO_PERMISSION e )
	  {
	  }

	  //
	  // add an action to create a new sub-workspace
	  //

	  Action createSubWorkspace = new GenericAction("Workspace", this, "createWorkspace" );
        addAction( createSubWorkspace, MenuBar.CREATE_MENU_GROUP );

	  Action testAction = new GenericAction( "Contains Test", this, "containsTest" );
	  addAction( testAction, MenuBar.ACTION_MENU_GROUP );

        super.initialize();
    }

    public void containsTest()
    {
        if( containsCollection != null ) containsCollection.test();
    }

    //=========================================================================
    // WorkspaceAgent
    //=========================================================================

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

    public List getContents()
    {
	  if( containsCollection != null ) return containsCollection;
	  try
	  {
            containsCollection = 
              new LinkCollection( service, workspace, adapter, 
		  ComposedOfHelper.type(), ComposedOf.class );
	  }
	  catch( Exception e )
	  {
		containsCollection = new LinkCollection( this, ComposedOf.class );
	  }
        finally
	  {
            return containsCollection;
	  }
    }

    public List getFactoryCriteria()
    {
        if( factory == null ) try
	  {
	      org.omg.CORBA.Object[] factories = 
              workspace.find_factories( new NameComponent[0] );
		factory = ResourceFactoryHelper.narrow( factories[0] );
	  }
	  catch( org.omg.CORBA.NO_PERMISSION e )
	  {
		return new LinkedList();
	  }
	  catch( NoFactory noFactory )
	  {
		return new LinkedList();
	  }
	  catch( Exception e )
	  {
            throw new RuntimeException(
              "WorkspaceAgent, Unable to resolve factories.", e );
        }

	  if( criteria == null ) try
	  {
		Criteria[] array = factory.supporting();
		criteria = new CriteriaAgent[ array.length ];
	      for( int i=0; i<array.length; i++ )
	      {
		    if( array[i] instanceof ProcessorCriteria )
		    {
		        criteria[i] = new ProcessorCriteriaAgent( 
			    (ProcessorCriteria) array[i] );
		    }
		    else
	          {
		        criteria[i] = new CriteriaAgent( array[i] );
		    }
	      }
	  }
	  catch( Exception e )
	  {
            throw new RuntimeException(
              "Failed to resolve supported criteria.", e );
        }

        return new LinkedList( Arrays.asList( criteria ) );
    }

    public List getContents( TypeCode type )
    {
        AbstractResourcesHolder holder = new AbstractResourcesHolder();
	  AbstractResourceIteratorHolder iteratorHolder = 
          new AbstractResourceIteratorHolder();

        try
        {
		workspace.list_resources_by_type( type, 0, holder, iteratorHolder );
        }
        catch( Exception remote )
        {
            throw new RuntimeException( 
			"Remote exception while expanding iterator.", remote );
        }

        try
        {
		return new Collection( orb, iteratorHolder.value );
        }
        catch( Exception local )
        {
            throw new RuntimeException( 
			"Local exception while establishing list.", local );
        }
    }

    public List getAccessedBy( )
    {
        return getLinks( AccessedByHelper.type() );
    }

    //=========================================================================
    // Entity implementation
    //=========================================================================

    public Item getItem( )
    {
	  return new DefaultItem( this, "role", icon, getContents() );
    }

    public View getView()
    {
        return getContentsView();
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

		//
		// WARNING:
		// Bug in getAccessedBy
	      // list.add( new ListView( this, "Authorized", this.getAccessedBy() ) );
		//

		list.add( getTemplatesView() );
		list.add( new ScrollView( new FlatPanel( this, "Contents", "getContents" ) ) );
		list.add( new ScrollView( new FlatPanel( this, "Access", "getAccessedBy" ) ) );
	  }
        return list;
    }

    public View getContentsView()
    {
        if( contentView == null )
        {	
	      contentView = new ScrollView( new FlatPanel( this, "Contents", "getContents" ));
        }
        return contentView;
    }

    public View getTemplatesView()
    {
        if( templateView == null )
        {	
	      templateView = new ScrollView( new FlatPanel( this, "Templates", "getFactoryCriteria" ) );
        }
        return templateView;
    }

}
