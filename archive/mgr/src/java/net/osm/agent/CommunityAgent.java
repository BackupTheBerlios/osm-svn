
package net.osm.agent;

import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JTabbedPane;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.omg.CORBA.ORB;
import org.omg.CosNotification.EventType;
import org.omg.CommunityFramework.Community;
import org.omg.CommunityFramework.CommunityHelper;
import org.omg.CommunityFramework.MembershipModel;
import org.omg.CommunityFramework.RecruitmentStatus;
import org.omg.Session.UserIterator;
import org.omg.Session.WorkspaceHelper;
import org.omg.Session.UsersHolder;
import org.omg.Session.User;
import org.omg.Session.UserHelper;
import org.omg.CommunityFramework.Recognizes;
import org.omg.CommunityFramework.RecognizesHelper;

import net.osm.agent.WorkspaceAgent;
import net.osm.agent.MembershipModelAgent;
import net.osm.agent.AgentIterator;
import net.osm.agent.UserAgent;
import net.osm.agent.RoleAgent;
import net.osm.agent.NotFoundException;
import net.osm.agent.Agent;
import net.osm.agent.LinkCollection;
import net.osm.agent.util.Collection;
import net.osm.agent.util.Sequence;
import net.osm.audit.RemoteEvent;
import net.osm.util.IconHelper;
import net.osm.shell.Entity;
import net.osm.shell.MGR;
import net.osm.shell.View;
import net.osm.shell.Simulator;
import net.osm.shell.StaticFeature;
import net.osm.shell.ActiveFeature;
import net.osm.shell.TablePanel;
import net.osm.shell.GenericAction;
import net.osm.shell.ScrollView;
import net.osm.util.ExceptionHelper;

/**
 * A <code>CommunityAgent</code> is a class combining a formal model of 
 * membership above <code>WorkspaceAgent</code>.  As a WorkspaceAgent, 
 * a CommunityAgent is a container of AbstractResourceAgent instances. 
 * As a Membership, a CommunityAgent exposes a MembershipModel detailing 
 * the allowable business roles and group constraints applicable to 
 * associated UserAgent instances.
 *
 * @author Stephen McConnell
 */

public class CommunityAgent extends WorkspaceAgent implements Simulator
{

    //=========================================================================
    // static
    //=========================================================================

    private static final String path = "net/osm/agent/image/community.gif";
    private static final ImageIcon icon = IconHelper.loadIcon( path );

    private static final EventType[] removals = new EventType[0];
    private static final EventType[] additions = new EventType[]
    { 
	  new EventType("org.omg.session","recruitment")
    };


    //=========================================================================
    // state
    //=========================================================================

    protected Community community;

    private MembershipModelAgent model;

    private List propertyPanelList;

    private LinkCollection membersList;

    private LinkTableModel membersModel;

    private List features;
    private boolean recruitment = false;
    private Action openMembershipAction;
    private Action closeMembershipAction;
    private List actions;

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

        if( community == null ) throw new Exception("CommunityAgent, primary resource has not been declared.");
        addView( 
            new ScrollView( 
               new TablePanel( this, "Members", getMembersModel(), 
			new LinkColumnModel(
			  getShell().getDefaultFont()
			)
		   )
            )
        );

        //
        // set the recruitment status
	  //

	  try
	  {
            recruitment = ( community.recruitment_status() == RecruitmentStatus.OPEN_MEMBERSHIP );
        }
        catch( Throwable e )
        {
            ExceptionHelper.printException("Unable to resolve recruitment status.", e );
	      recruitment = false;
        }

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
	//	ExceptionHelper.printException(
	//	  "AbstractResourceAgent, Unable to extend subscription.", e, this, true );
	  //}

	  //
	  // add an action to handle community open and close
	  //
 
        openMembershipAction = new GenericAction( "Open Membership", this, "openMembership", true );
        closeMembershipAction = new GenericAction( "Close Membership", this, "closeMembership", false );
	  openMembershipAction.setEnabled( !recruitment );
        closeMembershipAction.setEnabled( recruitment );

        setIcon( icon, Entity.SMALL );

    }

    //=========================================================================
    // Agent
    //=========================================================================

   /**
    * The <code>getType</code> method returns a human-friendly name of the entity.
    */
    public String getType( )
    {
	  return "Community";
    }

   /**
    * Set the resource that this agent will wrap. The implementation verifies
    * that the type of object passed to the operation is an instance of a 
    * org.omg.CommunityFramework.Community object reference.  If the type is 
    * not derived from this base, the implementation will throw an exception.
    * @param value the community to assign
    * @exception RuntimeException if the type of object supplied is not a Community
    */
    public void setPrimary( Object value ) 
    {
	  super.setPrimary( value );
	  try
	  {
	      this.community = CommunityHelper.narrow( (org.omg.CORBA.Object) value );
        }
        catch( Exception e )
        {
            throw new RuntimeException("Bad argument supplied to constructor.", e );
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
		String type = event.getType();
		if( type.equals("recruitment") )
		{
		    recruitment = event.getProperty("recruitment").extract_boolean();
		    putValue("recruitment", new Boolean( recruitment ) );
	          openMembershipAction.setEnabled( !recruitment );
                closeMembershipAction.setEnabled( recruitment );
		}
	      else
	      {
	          super.remoteChange( event );
	      }
	  }
    }

    //=========================================================================
    // CommunityAgent
    //=========================================================================

   /**
    * A Community is constrained by a MembershipModelAgent, associable through the 
    * <code>getModel</code> method, that declares privacy constraints and a role
    * heirachy to which members of the community are associated.
    */
    public Entity getModel( )
    {
	  if( this.model != null ) return this.model;
	  try
	  {
            this.model = (MembershipModelAgent) getResolver().resolve( this.community.model() );
	      return this.model;
        }
        catch( Throwable e )
        {
		final String error = "unable to resolve membership model";
		throw new RuntimeException( error, e );
        }
    }

   /**
    * Returns the recruitment status, indicating if new members can be added or removed
    * from the membership.  The default implementation disallows members association or
    * retraction when the a community is closed.
    * @return boolean true if the recruitment status is open or false if the 
    *     recruitment status is closed.
    */
    public boolean getOpen()
    {
	  return recruitment;
    }

    public void openMembership()
    {
	  try
	  {
            community.set_recruitment_status( RecruitmentStatus.OPEN_MEMBERSHIP );
	  }
	  catch( Exception e )
	  {
		throw new RuntimeException(
		  "UserAgent.  Remote exception while attempting to open the membership.", e );
	  }
    }

    public void closeMembership()
    {
	  try
	  {
            community.set_recruitment_status( RecruitmentStatus.CLOSED_MEMBERSHIP );
	  }
	  catch( Exception e )
	  {
		throw new RuntimeException(
		  "UserAgent.  Remote exception while attempting to close the membership.", e );
	  }
    }

   /**
    * Returns an iterator of the members of the community, where each member is 
    * represented as an instance of UserAgent (or a instance derived from UserAgent).
    * @return <code>AgentIterator</code> of UserAgent instances associated as 
    *      members of the <code>Membership</code>.
    */
    public List getMembers()
    {
	  if( membersList != null ) return membersList;
        try
	  {
            membersList = new LinkCollection( 
                getLogger().getChildLogger("members"), 
		    getOrb(), getResolver(), community, audit, 
		    RecognizesHelper.type(), Recognizes.class );
            return membersList;
	  }
	  catch( Exception e )
	  {
		final String error = "unable to resolve community members";
            if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
		throw new RuntimeException( error, e );
	  }
    }

    private LinkTableModel getMembersModel()
    {
        if( membersModel != null ) return membersModel;
	  try
	  {
            membersModel = new LinkTableModel( (LinkCollection) getMembers( ) );
	      return membersModel;
	  }
	  catch( Exception e )
	  {
		final String error = "unable to resolve members table model";
		throw new RuntimeException( error, e );
	  }
    }

   /**
    * Returns an iterator of the members of the community that are associated under a 
    * supplied role.
    * @param role the role label
    * @return <code>AgentIterator</code> of UserAgent instances where each instance is 
    * 	associated to the membership under the supplied role
    * @osm.warning the list returned from this operation will not reflect changes to the 
    *   state of the primary community
    */
    public List getMembersInRole( String role )
    {
        try
        {
		UsersHolder holder = new UsersHolder();
		UserIterator iterator = community.list_members_using( role, 0, holder );
		return new Collection( orb, getResolver(), iterator );
        }
        catch( Exception e )
        {
            throw new RuntimeException( "User iterator invalid.", e );
        }
    }

   /**
    * The getMemberRoles method returns a an iterator of all roles 
    * associated with the supplied user.
    * @param  user a member
    * @exception PrivacyConflictException if there is a privacy policy conflict
    */
    public List getMemberRoles( UserAgent user )
    {
	  User u = UserHelper.narrow( (org.omg.CORBA.Object) user.getPrimary() );
	  
        String[] labels = null;
	  try
	  {
	      labels = community.get_member_roles( u );
	  }
	  catch( Exception e )
	  {
		throw new RuntimeException(
		"Unexpected remote exception while retrieving member's roles.", e );
	  }

	  RoleAgent[] roles = new RoleAgent[ labels.length ];
	  for( int i=0; i<labels.length; i++ )
        {
		try
	      {
                roles[i] = ((MembershipModelAgent)getModel()).getRole().getRole( labels[i] );
		}
		catch( NotFoundException nf )
		{
		    throw new RuntimeException(
			"Unexpected exception while retrieving role associations.", nf );
		}
        }
	  return new Sequence( getResolver(), roles );
    }

   /**
    * Returns true if the community has reached quorum based on the role policy.
    * @return boolean true if the community has reached quorum
    */
    public boolean getQuorum()
    {
        try
        {
		return community.quorum_status();
        }
        catch( Exception e )
        {
            throw new RuntimeException( 
		  "Unexpected exception while resolving quorum.", e );
        }
    }

    //=========================================================================
    // Entity
    //=========================================================================

   /**
    * Returns a list of Action instances to be installed as 
    * action menu items within the desktop when the entity 
    * is selected.
    */
    public List getActions( )
    {
	  if( actions != null ) return actions;
	  actions = super.getActions();
	  actions.add( openMembershipAction );
	  actions.add( closeMembershipAction );
        return actions;
    }

   /**
    * The <code>getStandardViews</code> operation returns a sequence of panels 
    * representing different views of the content and/or associations maintained by
    * the agent.
    */
    public List getPropertyPanels()
    {
        if( propertyPanelList == null )
        {
		propertyPanelList = super.getPropertyPanels();
            propertyPanelList.add( 
		  new ScrollView( 
                new TablePanel( this, "Members", getMembersModel(), 
			new LinkColumnModel(
			  getShell().getDefaultFont()
			)
		    )
	        )
            );
        }
        return propertyPanelList;
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
            list.add( new ActiveFeature( this, "recruitment", "getOpen", "recruitment" ));
	      list.add( new StaticFeature("quorum", "" + getQuorum() ));
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
        if( membersList != null ) membersList.dispose();
        if( model != null ) model.dispose();
	  super.dispose();
    }

}
