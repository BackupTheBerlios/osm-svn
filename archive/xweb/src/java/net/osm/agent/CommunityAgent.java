
package net.osm.agent;

import org.omg.CORBA.ORB;
import org.omg.CommunityFramework.Community;
import org.omg.CommunityFramework.CommunityHelper;
import org.omg.CommunityFramework.MembershipModel;
import org.omg.CommunityFramework.RecruitmentStatus;
import org.omg.Session.UserIterator;
import org.omg.Session.UsersHolder;
import org.omg.Session.User;
import org.omg.Session.UserHelper;

import org.apache.avalon.framework.CascadingRuntimeException;

import net.osm.agent.iterator.CollectionIterator;
import net.osm.agent.iterator.SequenceIterator;

/**
 * A <code>CommunityAgent</code> is a class combining a formal model of 
 * membership above <code>WorkspaceAgent</code>.  As a WorkspaceAgent, 
 * a CommunityAgent is a container of AbstractResourceAgent instances. 
 * As a Membership, a CommunityAgent exposes a MembershipModel detailing 
 * the allowable business roles and group constraints applicable to 
 * associated UserAgent instances.
 */

public class CommunityAgent extends WorkspaceAgent
{

    //=========================================================================
    // Members
    //=========================================================================

    protected Community community;

    private MembershipModel model;

    //=========================================================================
    // Constructor
    //=========================================================================

   /**
    * Default <code>CommunityAgent</code> constructor.
    */
    public CommunityAgent( )
    {
	  super();
    }

   /**
    * Creation of a new <code>CommunityAgent</code> instance based on a supplied
    * ORB and <code>Community</code> object reference.
    * @param orb object request broker
    * @param reference object reference of a remotely located Community instance
    */

    public CommunityAgent( ORB orb, Community reference )
    {
	  super( orb, reference );
	  this.community = reference;
    }

    //=========================================================================
    // Setter methods
    //=========================================================================

   /**
    * Set the resource that this agent will wrap. The implementation verifies
    * that the type of object passed to the operation is an instance of a 
    * org.omg.CommunityFramework.Community object reference.  If the type is 
    * not derived from this base, the implementation will throw an exception.
    * @param value the community to assign
    * @throws CascadingRuntimeException if the type of object supplied is not a Community
    */
    public void setReference( Object value ) 
    {
	  super.setReference( value );
	  try
	  {
	      this.community = CommunityHelper.narrow( (org.omg.CORBA.Object) value );
        }
        catch( Exception e )
        {
            throw new CascadingRuntimeException("Bad argument supplied to constructor.", e );
        }
    }

    //=========================================================================
    // Getter methods
    //=========================================================================

   /**
    * A Community is constrained by a MembershipModel, associable through the 
    * <code>getModel</code> method, that declares privacy constraints and a role
    * heirachy to which members of the community are associated.
    */
    public MembershipModelAgent getModel( )
    {
	  try
	  {
	      if( this.model == null ) this.model = (MembershipModel) this.community.model();
	      return new MembershipModelAgent( this.model );
        }
        catch( Throwable e )
        {
            return null;
        }
    }

   /**
    * Returns the recruitment status, indicating if new members can be added or removed
    * from the membership.  The default implementation disallows members association or
    * retraction when the a community is closed.
    * @returns boolean true if the recruitment status is open or false if the 
    *     recruitment status is closed.
    */
    public boolean getOpen()
    {
	  try
	  {
            return ( community.recruitment_status() == RecruitmentStatus.OPEN_MEMBERSHIP );
        }
        catch( Throwable e )
        {
            return false;
        }
    }

   /**
    * Returns an iterator of the members of the community, where each member is 
    * represented as an instance of UserAgent (or a instance derived from UserAgent).
    * @returns <code>AgentIterator</code> of UserAgent instances associated as 
    *      members of the <code>Membership</code>.
    */

    public AgentIterator getMembers()
    {
        return getMembersInRole( null );
    }

   /**
    * Returns an iterator of the members of the community that are associated under a 
    * supplied role.
    * @param role the role label
    * @returns <code>AgentIterator</code> of UserAgent instances where each instance is 
    * 	associated to the membership under the supplied role
    */
    public AgentIterator getMembersInRole( String role )
    {
        try
        {
		UsersHolder holder = new UsersHolder();
		UserIterator iterator = community.list_members_using( role, 0, holder );
		return new CollectionIterator( orb, iterator );
        }
        catch( Exception e )
        {
            throw new CascadingRuntimeException( "User iterator invalid.", e );
        }
    }

   /**
    * The getMemberRoles method returns a an iterator of all roles 
    * associated with the supplied user.
    * @param  user a member
    * @throws PrivacyConflictException if there is a privacy policy conflict
    */
    public AgentIterator getMemberRoles( UserAgent user )
    {
	  User u = UserHelper.narrow( (org.omg.CORBA.Object) user.getReference() );
	  
        String[] labels = null;
	  try
	  {
	      labels = community.get_member_roles( u );
	  }
	  catch( Exception e )
	  {
		throw new CascadingRuntimeException("Unexpected remote exception while retrieving member's roles.", e );
	  }

	  RoleAgent[] roles = new RoleAgent[ labels.length ];
	  for( int i=0; i<labels.length; i++ )
        {
		try
	      {
                roles[i] = getModel().getRole().getRole( labels[i] );
		}
		catch( NotFoundException nf )
		{
		    throw new CascadingRuntimeException("Unexpected exception while retrieving role associations.", nf );
		}
        }
	  return new SequenceIterator( roles );
    }

   /**
    * Returns true if the community has reached quorum based on the role policy.
    * @returns boolean true if the community has reached quorum
    */
    public boolean getQuorum()
    {
        try
        {
		return community.quorum_status();
        }
        catch( Exception e )
        {
            throw new CascadingRuntimeException( "Unexpected exception while resolving quorum.", e );
        }
    }
}
