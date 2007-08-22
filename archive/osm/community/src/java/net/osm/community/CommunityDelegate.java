

package net.osm.community;

import java.util.Vector;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceException;

import org.omg.CORBA.ORB;
import org.omg.CORBA.Any;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CORBA.BooleanHolder;
import org.omg.CosCollection.AnySequenceHolder;
import org.omg.CosObjectIdentity.IdentifiableObject;
import org.omg.CosTime.TimeService;
import org.omg.CosNaming.NameComponent;
import org.omg.CosLifeCycle.NoFactory;
import org.omg.CosLifeCycle.NotRemovable;
import org.omg.NamingAuthority.AuthorityId;
import org.omg.PortableServer.POA;
import org.omg.Session.LinkIterator;
import org.omg.Session.AbstractResource;
import org.omg.Session.AbstractResourceHelper;
import org.omg.Session.AbstractResourceIteratorPOA;
import org.omg.Session.AbstractResourceIteratorPOATie;
import org.omg.Session.AbstractResourceIteratorHelper;
import org.omg.Session.LinkIterator;
import org.omg.Session.LinkIteratorPOA;
import org.omg.Session.LinkIteratorPOATie;
import org.omg.Session.AbstractResourcesHolder;
import org.omg.Session.AbstractResourceIterator;
import org.omg.Session.AbstractResourceIteratorHolder;
import org.omg.Session.User;
import org.omg.Session.Link;
import org.omg.Session.LinksHolder;
import org.omg.Session.ResourceUnavailable;
import org.omg.Session.ProcessorConflict;
import org.omg.Session.SemanticConflict;
import org.omg.Session.AccessedByHelper;
import org.omg.Session.AccessedBy;
import org.omg.Session.CollectedBy;
import org.omg.Session.ComposedOf;
import org.omg.Session.ComposedOfHelper;
import org.omg.Session.Collects;
import org.omg.Session.CollectsHelper;
import org.omg.Session.WorkspaceHelper;
import org.omg.Session.Workspace;
import org.omg.Session.WorkspaceOperations;
import org.omg.Session.AdministeredBy;
import org.omg.Session.Administers;
import org.omg.Session.IsPartOf;
import org.omg.Session.ContainmentHelper;
import org.omg.Session.UsersHolder;
import org.omg.Session.UserIterator;
import org.omg.CommunityFramework.Membership;
import org.omg.CommunityFramework.MembershipModel;
import org.omg.CommunityFramework.MembershipHelper;
import org.omg.CommunityFramework.RecruitmentStatus;
import org.omg.CommunityFramework.MembershipCount;
import org.omg.CommunityFramework.RoleStatus;
import org.omg.CommunityFramework.Member;
import org.omg.CommunityFramework.AttemptedCeilingViolation;
import org.omg.CommunityFramework.AttemptedExclusivityViolation;
import org.omg.CommunityFramework.RecruitmentConflict;
import org.omg.CommunityFramework.RoleAssociationConflict;
import org.omg.CommunityFramework.MembershipRejected;
import org.omg.CommunityFramework.UnknownRole;
import org.omg.CommunityFramework.PrivacyConflict;
import org.omg.CommunityFramework.CannotRemoveRole;
import org.omg.CommunityFramework.UnknownMember;
import org.omg.CommunityFramework.Role;
import org.omg.CommunityFramework.RolePolicy;
import org.omg.CommunityFramework.QuorumStatus;
import org.omg.CommunityFramework.QuorumPolicy;
import org.omg.CommunityFramework.Recognizes;
import org.omg.CommunityFramework.Model;


import org.apache.pss.ActivatorService;
import org.apache.pss.util.Incrementor;
import org.apache.time.TimeUtils;

import net.osm.adapter.Adapter;
import net.osm.session.CannotTerminate;
import net.osm.session.user.UserService;
import net.osm.session.resource.AbstractResourceDelegate;
import net.osm.session.workspace.WorkspaceDelegate;
import net.osm.session.linkage.LinkIteratorDelegate;
import net.osm.session.linkage.LinkStorage;
import net.osm.list.LinkedList;
import net.osm.list.Iterator;
import net.osm.list.NoEntry;

/**
 */
public class CommunityDelegate extends WorkspaceDelegate implements CommunityOperations
{

    //======================================================================
    // state
    //======================================================================
    
   /**
    * Storage object representing this Community.
    */
    private CommunityStorage m_store;

   /**
    * User service reference.
    */
    private UserService m_userService;
   
   /**
    * User service reference.
    */
    private CommunityService m_communityService;

   /**
    * Object reference to this Community.
    */
    private Community m_community;

   /**
    * Reference to the membership provider.
    */
    private MembershipProvider m_membership;

    //=======================================================================
    // Serviceable
    //=======================================================================

    /**
     * Pass the <code>ServiceManager</code> to the <code>Composable</code>.
     * The <code>Serviceable</code> implementation uses the supplied
     * <code>ServiceManager</code> to acquire the components it needs for
     * execution.
     * @param controller the <code>ServiceManager</code> for this delegate
     * @exception ServiceException
     */
    public void service( ServiceManager manager )
    throws ServiceException
    {
	  super.service( manager );

        m_userService = (UserService) manager.lookup( 
          UserService.USER_SERVICE_KEY );

        m_communityService = (CommunityService) manager.lookup(
          CommunityService.COMMUNITY_SERVICE_KEY );
    }

    //=======================================================================
    // Initializable
    //=======================================================================
    
   /**
    * Initialization of the delagate by its coontainer.
    * @exception Exception if the servant cannot complete normal initialization
    */
    public void initialize()
    throws Exception
    {
        super.initialize();

        m_store = (CommunityStorage) super.getStorageObject();
	  setCommunityReference( 
          CommunityHelper.narrow( 
            m_communityService.getCommunityReference( m_store ) ) );

        m_membership = new MembershipProvider();
        m_membership.enableLogging( getLogger().getChildLogger("membership") );
        DefaultContext context = new DefaultContext();
        context.put("MEMBERSHIP_STORAGE", m_store.membership() );
        context.put("PUBLISHER_STORAGE", m_store.publisher() );
        context.put("MEMEBRSHIP_REFERENCE", getCommunityReference() );
        context.makeReadOnly();
        m_membership.contextualize( context );
        m_membership.initialize();
    }
    
    //=======================================================================
    // Disposable
    //=======================================================================

   /**
    * Clean up state members.
    */ 
    
    public synchronized void dispose()
    {
        m_store = null;
        m_community = null;
	  super.dispose();
    }

    // ==========================================================================
    // Community
    // ==========================================================================
    
   /**
    * Returns a <code>CommunityAdapter</code>.
    * @return Adapter an instance of <code>CommunityAdapter</code>.
    */
    public Adapter get_adapter()
    {
        return new CommunityValue( m_community );
    }

    //======================================================================
    // Simulator
    //======================================================================

    /**
     * Access to a valuetype supporting the abstract Model interface.
     */
    public Model model()
    {
        return m_membership.model();
    }

    //======================================================================
    // Membership
    //======================================================================

    /**
     * Returns the RecruitmentStatus of the Membership.
     * 
     * @return  RecruitmentStatus The value returned is one 
     * of the enumeration values OPEN_MEMBERSHIP or CLOSED_MEMBERSHIP.  
     */
    public RecruitmentStatus recruitment_status()
    {
        return m_membership.recruitment_status();
    }

    /**
     * Supports access to the number of associated Member instances.  
     * The valuetype MembershipCount contains two values, the number of 
     * Member instances associated to the Membership (static field), and the 
     * number of Member instances referencing connected Users at the time 
     * of invocation (refer Task and Session, User, Connected State).
     * 
     * @return  MembershipCount a struct containing (a) the number of 
     * associated, and (b) connected users.
     */
    public MembershipCount membership_count()
    {
        return m_membership.membership_count();
    }

    /**
     * Returns true if all roles defined within the associated MembershipPolicy 
     * have met quorum – that is to say that for each role, the number of 
     * member instances associated with that role, equal or exceed the 
     * quorum value defined under the RolePolicy associated with the given 
     * role (refer RolePolicy).
     * 
     * @return  boolean true if the membership has reached quorum 
     */
    public boolean quorum_status()
    {
        return m_membership.quorum_status();
    }

    /**
     * Set the recrutment status to the supplied value.
     * @since  2.1
     * @param   RecruitmentStatus The value to set the recruitment status to
     *   either OPEN_MEMBERSHIP or CLOSED_MEMBERSHIP.
     */
    public void set_recruitment_status( RecruitmentStatus status )
    {
        m_membership.set_recruitment_status( status );
    }

    /**
     * Quorum status relating to individual roles is available through the 
     * <code>get_quorum_status</code> operation.  The identifier argument 
     * corresponds to identify of a role exposed within a MembershipModel. 
     * 
     * @return  RoleStatus a valurtype containing the role status, count and 
     * role identifier.
     * @param  String identifier - string identifying the role
     */
    public RoleStatus get_quorum_status( String identifier )
    {
        return m_membership.get_quorum_status( identifier );
    }


    /**
     * The join operation allows a client to associate a User reference with 
     * a Membership under a set of declared business roles (refer 
     * MembershipPolicy).  The join operation returns a Member instance to 
     * be maintained by the User instance.
     * 
     * @see  org.omg.CommunityFramework.MembershipPolicy
     * @return  Member a role association containing the user
     * @param  user the User to be associated with the Membership
     * @param  roles a sequence os String instances representing the 
     * roles to attribute to the user
     * @exception  AttemptedCeilingViolation
     * if the number of Users associated unde the supplied role exceeds 
     * the maximum allowed under the associated RolePolicy
     * @exception  AttemptedExclusivityViolation if a user attempts to join 
     * more than once using the same User identity
     * @exception  RecruitmentConflict if the current RecruitmentStatus is 
     * CLOSED_MEMBERSHIP
     * @exception  RoleAssociationConflict if a supplied role name is an 
     * abstract role
     * @exception  MembershipRejected if the implementation chooses to disallow 
     * user association for an implementation or policy reason
     * @exception  UnknownRole if a role references in the roles argument is unknown 
     * within the scope of the Membership
     */
    public Member join( User user, String[] roles )
        throws AttemptedCeilingViolation, AttemptedExclusivityViolation, 
        RecruitmentConflict, RoleAssociationConflict, MembershipRejected, 
        UnknownRole
    {
        return m_membership.join( user, roles );
    }

    /**
     * The leave operation disassociates a Member from a Membership.  
     * @param  member the Member association to retract from the Membership
     * @exception  RecruitmentConflict if the current RecruitmentStatus is 
     * CLOSED_MEMBERSHIP
     * @exception  UnknownMember if a Member references in the member argument is unknown 
     * within the scope of the Membership
     */
    public void leave( Member member )
        throws RecruitmentConflict, UnknownMember
    {
        m_membership.leave( member );
    }


    /**
     * The add_roles operation enables the addition of business roles attributed to a Member.
     * 
     * @exception  UnknownMember if the supplied Member is unknown within the scope 
     * of the Membership.
     * @exception  RoleAssociationConflict may be invoked if the current RecruitmentStatus 
     * is a CLOSED_MEMBERSHIP
     * @exception  UnknownRole if a role references in the roles argument is unknown 
     * within the scope of the Membership
     */
    public void add_roles( Member member, String[] roles)
        throws UnknownMember, RoleAssociationConflict, UnknownRole
    {
        m_membership.add_roles( member, roles );
    }

    /**
     * The remove_roles operation disassociated an existing business roles attributed 
     * to a Member.
     * 
     * @param  member Member - the association defining the connection 
     * of the User to the Membership
     * @param  roles String[] - declaring the names of the roles to which 
     * this User should be associated under this Membership
     * @exception  UnknownRole if the supplied Role label is unknown within the scope 
     * of the Membership.
     * @exception  UnknownMember if the supplied Member is unknown within the scope 
     * of the Membership.
     * @exception  CannotRemoveRole if the supplied Role label references a role 
     * cannot be removed.
     */
    public void remove_roles(Member member, String[] roles)
        throws UnknownRole, UnknownMember, CannotRemoveRole
    {
        m_membership.remove_roles( member, roles );
    }

    /**
     * The is_member operation returns true if the supplied User is a member of the membership.
     * @param user the user to test for membership
     * @exception PrivacyConflict if there is a privacy policy conflict
     */
    public boolean is_member(org.omg.Session.User user)
        throws PrivacyConflict
    {
        return m_membership.is_member( user );
    }

    /**
     * The has_role operation returns true if the supplied User is associated 
     * to the Membership under a role corresponding to the supplied identifier.
     * 
     * @exception  PrivacyConflict if there is a privacy policy conflict
     */
    public boolean has_role( User user, String role )
        throws PrivacyConflict
    {
        return m_membership.has_role( user, role );
    }


    /**
     * The get_member_roles operation returns the sequence of all role identifiers 
     * associated with the supplied user.
     * @param  user User to assess in terms of attributed roles
     * @exception  PrivacyConflict if there is a privacy policy conflict
     */
    public String[] get_member_roles( User user )
        throws PrivacyConflict
    {
        return m_membership.get_member_roles( user );
    }


    /**
     * The list_members operation returns an iterator of all User instances 
     * associated with the Membership.  The max_number argument constrains 
     * the maximum number of User instances to include in the returned list sequence.
     * @param  max_number int - the maximum number of User instances to include
     * in the returned UserHolder sequence
     * @param  list UsersHolder - a sequence of user instances
     * @exception  PrivacyConflict if there is a privacy policy conflict
     */
    public UserIterator list_members(int max_number, UsersHolder users )
        throws PrivacyConflict
    {
        return m_membership.list_members( max_number, users );
    }

    /**
     * The list_members_using operation returns an iterator of 
     * all User instances associated with the Membership under a supplied 
     * role.  The max_number argument constrains the maximum number 
     * of Member instances to include in the returned list sequence.
     * @param  role String containign the label of the role
     * @param  max_number int defining the maximum number of user to include in 
     * the returned UsersHolder sequence.
     * @param  list UsersHolder a sequence of User instances
     * @exception  PrivacyConflict if there is a privacy policy conflict
     */
    public org.omg.Session.UserIterator list_members_using(String role, int max_number, UsersHolder users )
        throws PrivacyConflict
    {
        return m_membership.list_members_using( role, max_number, users );
    }

    // ==========================================================================
    // utilities
    // ==========================================================================

   /**
    * Set the object reference to be returned for this delegate.
    * @param workspace the object reference for the workspace
    */
    protected void setCommunityReference( Community community )
    {
        m_community = community;
        super.setWorkspaceReference( community );
    }

   /**
    * Returns the object reference for this delegate.
    * @return Desktop the object referenced for the delegate
    */
    protected Community getCommunityReference( )
    {
        return m_community;
    }

}
