
package net.osm.community;

import org.omg.CORBA.ORB;
import org.omg.CORBA.Any;
import org.omg.CORBA.AnyHolder;
import org.omg.CORBA.LocalObject;
import org.omg.CosCollection.AnySequenceHolder;
import org.omg.CORBA.BooleanHolder;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.EventHeader;
import org.omg.CosNotification.FixedEventHeader;
import org.omg.CosNotification.Property;
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
import org.omg.Session.connect_state;
import org.omg.Session.User;
import org.omg.Session.UserIterator;
import org.omg.Session.UserIteratorPOA;
import org.omg.Session.UserIteratorPOATie;
import org.omg.Session.UsersHolder;
import org.omg.Session.UserHelper;
import org.omg.Session.AbstractResource;
import org.omg.Session.AbstractResourceHelper;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.activity.Disposable;

import net.osm.session.linkage.LinkStorage;
import net.osm.session.linkage.LinkStorageHome;
import net.osm.sps.StructuredEventUtilities;
import net.osm.list.LinkedList;
import net.osm.list.Iterator;
import net.osm.list.NoEntry;
import net.osm.sps.PublisherStorage;

/**
 * MembershipProvider is an implementation of a <code>Membership</code>
 * business component functionality.
 */
public class MembershipProvider extends LocalObject
implements Membership, LogEnabled, Contextualizable, Initializable, Disposable
{

    //======================================================================
    // static
    //======================================================================

   /**
    * Name of the event domain classifying structured events.
    */
    public static final String EVENT_DOMAIN = "org.omg.CommunityFramework";

   /**
    * The event type for a 'recruitment event change from a Community.
    */
    public static final EventType recruitmentEventType = 
      new EventType( EVENT_DOMAIN, "recruitment" );

    //======================================================================
    // state
    //======================================================================

   /**
    * The membership storage object.
    * Supplied under the component context.
    */
    private MembershipStorage m_store;

   /**
    * The publisher storage object that handles event distribution.
    * Supplied under the component context.
    */
    private PublisherStorage m_publisher;

   /**
    * Reference to the membership object reference.
    * Supplied under the component context.
    */
    private Membership m_membership;

   /**
    * Reference to the logging channel.
    */
    private Logger m_logger;

   /**
    * Reference to the runtime context.
    */
    private Context m_context;


    //======================================================================
    // LogEnabled
    //======================================================================

   /**
    * Set the logging channel.
    * @param logger the logging channel supplied by the container
    */
    public void enableLogging( Logger logger )
    {
        m_logger = logger;
    }

   /**
    * Return the assigned logging channel.
    * @return Logger the logging channel.
    */
    protected Logger getLogger()
    {
        return m_logger;
    }

    //======================================================================
    // Contextualizable
    //======================================================================

   /**
    * Application of the runtime context by the container.  The implementation
    * receives references to the membership storage, membership object reference,
    * and event publisher from the supplied context object.  Validation of 
    * the context is undertaken in the initialization phase.
    * @param context the runtime context
    */ 
    public void contextualize( Context context )
    {
        m_context = context;
    }

    //======================================================================
    // Initializable
    //======================================================================

   /**
    * Initialization of the component during which required referrences are 
    * resolved.
    * @exception Exception is the supplied context is incomplete
    */ 
    public void initialize( ) throws Exception
    {
        try
        {
            m_store = (MembershipStorage) m_context.get("MEMBERSHIP_STORAGE");
            m_publisher = (PublisherStorage) m_context.get("PUBLISHER_STORAGE");
            m_membership = (Membership) m_context.get("MEMBERSHIP_REFERENCE");
        }
        catch( Throwable e )
        {
            final String error = "Unable to resolve required membership references.";
            throw new CommunityException( error, e );
        }
    }

    //======================================================================
    // Disposable
    //======================================================================

   /**
    * Disposal of the component by its container.
    */ 
    public void dispose( )
    {
    }

    //======================================================================
    // Simulator
    //======================================================================

    /**
     * Access to a valuetype supporting the abstract Model interface.
     */
    public Model model()
    {
        return m_store.model();
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
        return m_store.recruitment();
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
        return membershipCount( m_store.model().role );
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
        try
        {
            RoleStatus status = getRoleStatus( m_store.model().role );
	      return ( status.status == QuorumStatus.QUORUM_VALID );
        }
        catch (Exception e)
        {
            String error = "unexpected exception while resolving community quorum status.";
            throw new CommunityRuntimeException( error, e );
        }

    }

    /**
     * Set the recrutment status to the supplied value.
     * @since  2.1
     * @param   RecruitmentStatus The value to set the recruitment status to
     *   either OPEN_MEMBERSHIP or CLOSED_MEMBERSHIP.
     */
    public void set_recruitment_status( RecruitmentStatus status )
    {
        m_store.recruitment( status );
	  if( status == RecruitmentStatus.OPEN_MEMBERSHIP )
	  {
	      m_publisher.post( newRecruitmentEvent( true ));
	  }
	  else
	  {
	      m_publisher.post( newRecruitmentEvent( false ));
	  }
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
        Role role = locateRole( m_store.model().role, identifier );
	  if( role == null ) throw new RuntimeException("unknown role '" + identifier + "'.");
        return getRoleStatus( role );
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

	  // make sure that membership modification is allowed
	  if( recruitment_status() == RecruitmentStatus.CLOSED_MEMBERSHIP ) 
        {
		throw new RecruitmentConflict( 
               getMembershipReference(), 
               RecruitmentStatus.CLOSED_MEMBERSHIP );
        }

        LinkedList list = m_store.members();
        synchronized( list )
        {
            // check if the supplied user is already a member
            try
            {
	          Recognizes link = locateMember( user );
                return new Member( getMembershipReference() );
            }
            catch( UnknownMember um )
            {
                // continue
            }

            // check if all of the roles are valid 
            // (i.e. the named roles exist and the roles are not abstract)

		Role root = ((MembershipModel)model()).role;
		for( int i=0; i<roles.length; i++ )
            {
		    String name = roles[i];
                Role role = locateRole( root, name );
		    if( role == null ) 
                {
                    throw new UnknownRole( 
		  	    "the role '" + name + "' is unknown.", getMembershipReference() );
                }
		    if( role.is_abstract ) 
                {
                     throw new RoleAssociationConflict( 
			    getMembershipReference(), "abstract role", name );
                }
            }

		// add the user
            try
            {
		    Recognizes link = new Recognizes( user, roles );
		    Member member = new Member( getMembershipReference() );
                user.bind( member );
                list.add( getLinkHome().create( link ) );
		    return member;
            }
            catch (Exception e)
            {
                String s = "failed to add a User to a Membership";
                throw new CommunityRuntimeException(s, e);
            }
        }
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
        LinkedList list = m_store.members();
        synchronized( list )
        {
            LinkStorage store = locateLinkStorage( UserHelper.narrow( member.resource() ) );
            try
            {
                list.remove( store );
                
                // notify the user that it is no
                // longer recognized
                
                try
                {
                    UserHelper.narrow( member.resource() ).release( member );
                }
                catch( Exception e )
                {
                    String warning = "Remote user raised an exception when notifying it " +
                    "of the release of a Recognizes association";
                    if( getLogger().isDebugEnabled() ) 
                    {
                        getLogger().debug(warning, e);
                    }
                }
            }
            catch( Throwable error )
            {
                String problem = "Cannot remove the supplied member.";
                throw new CommunityRuntimeException( problem, error );
            }
        }
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
        throw new org.omg.CORBA.NO_IMPLEMENT();
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
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    /**
     * The is_member operation returns true if the supplied User is a member of the membership.
     * @param user the user to test for membership
     * @exception PrivacyConflict if there is a privacy policy conflict
     */
    public boolean is_member(org.omg.Session.User user)
        throws PrivacyConflict
    {
        // WARNING:  Privacy assessment not implemented!
        try
        {
             locateMember( user );
             return true;
        }
        catch( UnknownMember um )
        {
	      return false;
        }
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
	  // WARNING: Privacy assessment not implemented!

        try
        {
            Recognizes link = locateMember( user );
	      for( int i=0; i<link.roles.length; i++ )
            {
                if( link.roles[i].equals( role ) ) 
                {
                    return true;
                }
            }
  	      return false;
        }
        catch( UnknownMember um )
        {
	      return false;
        }
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
        try
        {
            Recognizes link = locateMember( user );
	      return link.roles;
        }
        catch( UnknownMember um )
        {
	      return new String[0];
        }
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
	  return list_members_using( null, max_number, users );
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
	  UserIterator userIterator = null;
        LinkedList list = m_store.members();
        synchronized( list )
        {
	      try
	      {
		    Iterator iterator = list.iterator();
	          UserIteratorDelegate delegate = new UserIteratorDelegate( iterator, role );
	          UserIteratorPOA servant = new UserIteratorPOATie( delegate );
		    userIterator = servant._this();  //### check - old version has an orb arg

		    AnySequenceHolder anysHolder = new AnySequenceHolder();
		    BooleanHolder booleanHolder = new BooleanHolder();
		    if( userIterator.retrieve_next_n_elements( max_number, anysHolder, booleanHolder ) )
		    {
			  int k = anysHolder.value.length;
			  User[] userSequence = new User[k];
		        for( int i=0; i<k; i++ )
			  {
				userSequence[i] = UserHelper.extract( anysHolder.value[i]);
		        }
			  users.value = userSequence;
		    }
		    else
		    {
		        users.value = new User[0];
		    }
		}
		catch( Exception e )
		{
		}
        }
	  return userIterator;
    }


   /**
    * Creation of a new recruitment StructuredEvent.
    *
    * @param source Community that the connected event is from
    * @param recruitment boolean state of the commuity
    * @return 'connected' structured event
    */
    private StructuredEvent newRecruitmentEvent( boolean recruitment )
    {
        Property sourceProp = createSourceProperty( );
        Any b = ORB.init().create_any();
        b.insert_boolean( recruitment );
        Property recruitmentProp = new Property( "recruitment", b );
        return StructuredEventUtilities.createEvent( recruitmentEventType, 
		new Property[]
		{ 
			StructuredEventUtilities.timestamp( ), 
			sourceProp, 
			recruitmentProp 
		}
	  );
    }

   /**
    * Returns the Recognizes link associating a User to the Membership.
    * @param user the User object reference to be used as a search key
    * @return the link holding the reference to the User and its associated roles names
    */
    protected Recognizes locateMember( User user ) throws UnknownMember
    {
        LinkStorage s = locateLinkStorage( user );
        return (Recognizes) s.link();
    }

   /**
    * Returns the LinkStorage link associating a User to the Membership.
    * @param user the User object reference to be used as a search key
    * @return the link storage
    * @exception UnknownMember if the user is not associated to the membership
    */
    private LinkStorage locateLinkStorage( User user ) throws UnknownMember
    {
        LinkedList list = m_store.members();
        synchronized( list )
        {
	      try
	      {
		    Iterator iterator = list.iterator();
		    while( iterator.has_next() )
		    {
			  LinkStorage link = (LinkStorage) iterator.next();
			  if( link.link().resource()._is_equivalent( user ))
                    {
				return link;
                    }
		    }
		}
		catch( Exception e )
		{
		    String error = "Unexpected exception while locating a LinkStorage association.";
		    if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
                throw new CommunityRuntimeException( error, e );
		}
		
            throw new UnknownMember();
	  }
    }


   /**
    * Locate a role within the supplied role heirachy given the supplied role name.
    * If the role is not found the methods returns null.
    * @param role root of the role heirachy within which a search is scoped
    * @param name the role label to locate.
    */
    protected Role locateRole( Role role, String name )
    {
	  if( role == null ) return null;
        if( role.label.equals( name )) return role;
	  Role[] roles = role.getRoles();
        for( int i=0; i< roles.length; i++ )
        {
		Role r = locateRole( roles[i], name );
		if( r != null ) return r;
        }
	  return null;
    }
  
   /**
    * Returns a <code>RoleStatus</code> valuetype for the supplied role.  A role is 
    * considered to have reached quorum if (a) the number of members associated under
    * the role equals or exceeds the quorum level, and (b) all subsidary roles within 
    * the role have also reached quorum.
    * @param role the role against which the assesment will be applied
    * @return RoleStatus containing the role label, membership count and quorum status
    */
    protected RoleStatus getRoleStatus( Role role )
    {
        if( role == null ) throw new RuntimeException("Null value supplied as role argument.");

	  RoleStatus status = new RoleStatus();
        try
        {
             // RoleStatus is a valuetype containing the following:
             // 1. role label
             // 2. membership count of members associated under the role
             // 3. a quorum status enumeration

		 status.identifier = role.label;
	       status.count = membershipCount( role );
		 int base = status.count._static;	

		 RolePolicy policy = role.getPolicy();
		 if( policy.policy != QuorumPolicy.SIMPLE ) base = status.count.active;
		 if( base >= policy.quorum ) 
		 {
		    //
		    // this role is provisionally at quorum providing all of its 
                // subsidary roles have reached quorum
		    //

		    boolean pending = false;
		    for( int i=0; i<role.getRoles().length; i++ )
		    {
		        RoleStatus s = getRoleStatus( role.roles[i] );
			  if( s.status != QuorumStatus.QUORUM_VALID ) pending = true;
		    }
		    if( pending ) 
		    {
		        status.status = QuorumStatus.QUORUM_PENDING;
		    }
		    else
		    {
		        status.status = QuorumStatus.QUORUM_VALID;
		    }
             }
             else
             {

		    //
		    // this role has not reached quorum because the number of members 
		    // associated is less than the quorum value
		    //

		    status.status = QuorumStatus.QUORUM_PENDING;
             }
        }
	  catch( Exception e )
	  {
            String error = "Unexpected exception while resolving quorum status for the role '" + 
		  role.label + "'.";
		throw new RuntimeException( error, e );
        }

	  return status;
    }


   /**
    * Returns a MembershipCount valuetype for a suppplied role.
    */
    private MembershipCount membershipCount( Role role )
    {
	  try
	  {
            UserIterator iterator = list_members_using( role.label, 0, new UsersHolder() );
	      AnyHolder holder = new AnyHolder();
	      BooleanHolder more = new BooleanHolder();
	      int j = 0; // member count
	      int k = 0; // connected member count
            while( iterator.retrieve_element_set_to_next( holder, more ) )
            {
		    j++;
		    User user = UserHelper.extract( holder.value );
		    if( user.connectstate() == connect_state.connected ) k++;
            }
            return new MembershipCount( j, k );
        }
        catch( Exception e )
        {
		String error = "Unexpected exception while counting membership.";
            if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
            throw new RuntimeException( error, e );
        }
    }

   /**
    * Returns the object reference for this delegate.
    * @return Community the object referenced for the delegate
    */
    protected Membership getMembershipReference( )
    {
        return m_membership;
    }

   /**
    * Internal utility to a structured event property containing 
    * a membership reference wrapped within an Any with the property
    * name of 'source'.
    * @return Property the source mambership contained within a event Property
    */
    protected Property createSourceProperty( )
    {
        final Any any = ORB.init().create_any();
        MembershipHelper.insert( any, m_membership );
        return new Property( "source", any );
    }

    private AbstractResource getAbstractResource()
    {
        return AbstractResourceHelper.narrow( (org.omg.CORBA.Object) m_membership );
    }

    private LinkStorageHome getLinkHome()
    {
        try
        {
            return ( LinkStorageHome ) m_store.get_storage_home().get_catalog().find_storage_home( 
			"PSDL:osm.net/session/linkage/LinkStorageHomeBase:1.0" );
        }
        catch( Throwable e )
        {
            final String error = "Unable to resolve link storage home.";
            throw new CommunityRuntimeException( error, e );
        }
    }


}
