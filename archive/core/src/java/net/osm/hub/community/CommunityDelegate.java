

package net.osm.hub.community;

import org.apache.avalon.framework.context.Context;

import org.omg.CORBA.ORB;
import org.omg.CORBA.Any;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.AnyHolder;
import org.omg.CORBA.BooleanHolder;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.PortableServer.POA;
import org.omg.CosTime.TimeService;
import org.omg.CosLifeCycle.NotRemovable;
import org.omg.CosCollection.AnySequenceHolder;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.EventHeader;
import org.omg.CosNotification.FixedEventHeader;
import org.omg.CosNotification.Property;
import org.omg.CommunityFramework.AttemptedCeilingViolation;
import org.omg.CommunityFramework.AttemptedExclusivityViolation;
import org.omg.CommunityFramework.Community;
import org.omg.CommunityFramework.CommunityOperations;
import org.omg.CommunityFramework.CommunityHelper;
import org.omg.CommunityFramework.Member;
import org.omg.CommunityFramework.MembershipModel;
import org.omg.CommunityFramework.MembershipRejected;
import org.omg.CommunityFramework.MembershipCount;
import org.omg.CommunityFramework.Model;
import org.omg.CommunityFramework.PrivacyConflict;
import org.omg.CommunityFramework.RecruitmentConflict;
import org.omg.CommunityFramework.RecruitmentStatus;
import org.omg.CommunityFramework.RoleAssociationConflict;
import org.omg.CommunityFramework.RoleStatus;
import org.omg.CommunityFramework.UnknownRole;
import org.omg.CommunityFramework.UnknownMember;
import org.omg.CommunityFramework.Recognizes;
import org.omg.CommunityFramework.RecognizesHelper;
import org.omg.CommunityFramework.Role;
import org.omg.CommunityFramework.QuorumPolicy;
import org.omg.CommunityFramework.QuorumStatus;
import org.omg.CommunityFramework.RolePolicy;
import org.omg.CommunityFramework.CommunitySingleton;
import org.omg.Session.User;
import org.omg.Session.UserHelper;
import org.omg.Session.UsersHolder;
import org.omg.Session.UserIterator;
import org.omg.Session.UserIteratorPOA;
import org.omg.Session.UserIteratorPOATie;
import org.omg.Session.Link;
import org.omg.Session.LinkIterator;
import org.omg.Session.LinkIteratorPOA;
import org.omg.Session.LinkIteratorPOATie;
import org.omg.Session.LinkIterator;
import org.omg.Session.LinksHolder;
import org.omg.Session.ResourceUnavailable;
import org.omg.Session.ProcessorConflict;
import org.omg.Session.SemanticConflict;
import org.omg.Session.connect_state;

import net.osm.hub.gateway.CannotTerminate;
import net.osm.hub.pss.UserStorage;
import net.osm.hub.pss.CommunityStorage;
import net.osm.hub.pss.LinkStorage;
import net.osm.hub.resource.StructuredEventUtilities;
import net.osm.hub.resource.LinkIteratorDelegate;
import net.osm.hub.user.UserIteratorDelegate;
import net.osm.hub.workspace.WorkspaceDelegate;
import net.osm.list.LinkedList;
import net.osm.list.Iterator;
import net.osm.list.NoEntry;

/**
 * CommunityDelegate implements the Community type and links many Users to to the
 * Community under a common MembershipPolicy.
 */

public class CommunityDelegate extends WorkspaceDelegate implements CommunityOperations
{
    
    //=======================================================================
    // state
    //=======================================================================

   /**
    * Storage object for this Community.
    */
    private CommunityStorage store;
    
   /**
    * Object reference to this Community.
    */
    private Community m_community;

   /**
    * The event type for a 'recruitment' change event.
    */
    public static final EventType recruitmentEventType = CommunitySingleton.recruitmentEventType;
     
    //=======================================================================
    // Initializable
    //=======================================================================
    
   /**
    * Signal completion of contextualization phase and readiness to serve requests.
    * @exception Exception if the servant cannot complete normal initialization
    */
    public void initialize()
    throws Exception
    {
        super.initialize();
        if( getLogger().isDebugEnabled() ) getLogger().debug( "initialization (community)" );
        this.store = (CommunityStorage) getContext().getStorageObject();
        setCommunityReference( CommunityHelper.narrow( 
          getManager().getReference( store.get_pid(), CommunityHelper.id() )));
    }

    //==================================================
    // Vulnerable
    //==================================================
        
   /**
    * Destroys the persistent identity of this object. An
    * implementation is responsible for disposing of references
    * to external resources on completion of termiation actions.
    */
    
    public void terminate( ) throws CannotTerminate
    {
        synchronized( store )
        {
            if( !expendable() ) throw new CannotTerminate("resource is in use");
            if( getLogger().isDebugEnabled() ) getLogger().debug("[COM] terminate ");
            try
            {
                //
                // retract reference to members of the Community
                //
                
                LinkedList list = store.members();
                retractEveryLink( getCommunityReference(), list, "recognizes", new Member( getCommunityReference()));
            }
            catch (Exception e)
            {
                if( getLogger().isErrorEnabled() ) getLogger().error("Unexpected termination exception", e );
                e.printStackTrace();
            }
            super.terminate();
        }
    }
    
   /**
    * Clean up state members.
    */
    
    public synchronized void dispose()
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug("[COM] dispose ");
        this.store = null;
        this.m_community = null;
        super.dispose();
    }
    
    // ==========================================================================
    // Model
    // ==========================================================================
    
    public Model model()
    {
        return store.model();
    }

    // ==========================================================================
    // Membership
    // ==========================================================================
    
   /**
    * Returns the RecruitmentStatus of the Membership.
    * 
    * @return  RecruitmentStatus The value returned is one 
    * of the enumeration values OPEN_MEMBERSHIP or CLOSED_MEMBERSHIP.  
    */
    public RecruitmentStatus recruitment_status()
    {
        return store.recruitment();
    }

   /**
    * Set the recrutment status to the supplied value.
    * 
    * @param  RecruitmentStatus The value to set the recruitment status to
    *   (either OPEN_MEMBERSHIP or CLOSED_MEMBERSHIP).  
    */
    public void set_recruitment_status( RecruitmentStatus status  )
    {
        store.recruitment( status );
	  if( status == RecruitmentStatus.OPEN_MEMBERSHIP )
	  {
	      post( newRecruitmentEvent( true ));
	  }
	  else
	  {
	      post( newRecruitmentEvent( false ));
	  }
    }

   /**
    * Creation of a new recruitment StructuredEvent.
    *
    * @param source Community that the connected event is from
    * @param recruitment boolean state of the commuity
    * @return 'connected' structured event
    */
    public StructuredEvent newRecruitmentEvent( boolean recruitment )
    {
        Property sourceProp = super.createSourceProperty();
        Any b = orb.create_any();
        b.insert_boolean( recruitment );
        Property recruitmentProp = new Property( "recruitment", b );
        return StructuredEventUtilities.createEvent( orb, recruitmentEventType, 
		new Property[]
		{ 
			StructuredEventUtilities.timestamp( orb ), 
			sourceProp, 
			recruitmentProp 
		}
	  );
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
    * @osm.warning not implemented yet
    */
    public MembershipCount membership_count()
    {

        return membershipCount( this.store.model().role );

        /*
	  // NOTE: This implementatiion should be improved by establishing a 
        // event subscription between a membership and its members so that 
        // the membership is notified of changes to the connected state of
        // the user - enabling updating of the membership count and quorum
        // status.

	  try
	  {
	      LinkedList members = store.members();
	      synchronized( members )
            {
                int m = 0;
                int n = members.size();
	          Iterator iterator = members.iterator();
	          while( iterator.has_next() )
                {
		        try
		        {
		            LinkStorage link = (LinkStorage) iterator.next();
			      User user = UserHelper.narrow( link.link().resource() );
			      if( user.connectstate() == connect_state.connected ) m++;
		        }
		        catch( Exception e )
		        {
		        }
                }
                return new MembershipCount( n, m );
            }
        }
        catch( Exception e )
        {
            throw new RuntimeException("Unexpected exception while resolving member count.", e );
        }
        */

    }

   /**
    * Returns true if all roles defined within the associated MembershipPolicy 
    * have met quorum - that is to say that for each role, the number of 
    * member instances associated with that role, is equal to or exceed the 
    * quorum value defined under the RolePolicy associated with the given 
    * role (refer RolePolicy).
    * 
    * @return  boolean true if the membership has reached quorum 
    */
    public boolean quorum_status()
    {
        try
        {
            RoleStatus status = getRoleStatus( store.model().role );
	      return ( status.status == QuorumStatus.QUORUM_VALID );
        }
        catch (Exception e)
        {
            String error = "unexpected exception while resolving community quorum status.";
            if( getLogger().isErrorEnabled() ) getLogger().error( error, e);
            throw new org.omg.CORBA.INTERNAL( error );
        }
    }

   /**
    * Quorum status relating to individual roles is available through the 
    * <code>get_quorum_status</code> operation.  The identifier argument 
    * corresponds to identify of a role exposed within a MembershipModel. 
    * 
    * @return  RoleStatus a valuetype containing the role status, count and 
    * role identifier.
    * @param  String identifier - string identifying the role
    */
    public RoleStatus get_quorum_status(String identifier)
    {
        Role role = locateRole( store.model().role, identifier );
	  if( role == null ) throw new RuntimeException("unknown role '" + identifier + "'.");
        return getRoleStatus( role );
    }

   /**
    * The join operation allows a client to associate a User reference with 
    * a Membership under a set of declared business roles (refer 
    * MembershipPolicy).  The join operation returns a Member instance to 
    * be maintained by the User instance.
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
    throws AttemptedCeilingViolation, AttemptedExclusivityViolation, RecruitmentConflict, 
	RoleAssociationConflict, MembershipRejected, UnknownRole
    {

	  // make sure that membership modification is allowed
	  if( recruitment_status() == RecruitmentStatus.CLOSED_MEMBERSHIP ) 
		throw new RecruitmentConflict( getCommunityReference(), RecruitmentStatus.CLOSED_MEMBERSHIP );

        LinkedList list = store.members();
        synchronized( list )
        {
            // check if the supplied user is already a member
	      Recognizes link = locateMember( user );
	      if( link != null ) return new Member( getCommunityReference() );

            // check if all of the roles are valid 
            // (i.e. the named roles exist and the roles are not abstract)

		Role root = ((MembershipModel)model()).role;
		for( int i=0; i<roles.length; i++ )
            {
		    String name = roles[i];
                Role role = locateRole( root, name );
		    if( role == null ) throw new UnknownRole( 
			"the role '" + name + "' is unknown.", getCommunityReference() );
		    if( role.is_abstract ) throw new RoleAssociationConflict( 
			getCommunityReference(), "abstract role", name );
            }

		// add the user
            try
            {
		    link = new Recognizes( user, roles );
		    Member member = new Member( getCommunityReference() );
                user.bind( member );
                addLink( getCommunityReference(), list, link );
		    return member;
            }
            catch (Exception e)
            {
                String s = "failed to add a User to a Community";
                if( getLogger().isErrorEnabled() ) getLogger().error(s,e);
                throw new org.omg.CORBA.INTERNAL(s);
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
    public void leave(Member member)
    throws RecruitmentConflict, UnknownMember
    {
        LinkedList list = store.members();
        synchronized( list )
        {
            try
            {
                releaseLink( getCommunityReference(), list, member );
                
                // notify the user that it is no
                // longer recognized
                
                try
                {
                    User user = UserHelper.narrow( member.resource() );
			  user.release( new Member( getCommunityReference() ));
                }
                catch( Exception e )
                {
                    String s = "Remote user raised an exception when notifying it " +
                    "of the release of a Recognizes association";
                    if( getLogger().isDebugEnabled() ) getLogger().debug(s, e);
                }
            }
            catch( NoEntry noEntry )
            {
                String problem = "Cannot remove the supplied member.";
                if( getLogger().isErrorEnabled() ) getLogger().error( problem, noEntry );
                throw new org.omg.CORBA.INTERNAL( problem );
            }
        }
    }


   /**
    * The add_roles operation enables the addition of business roles attributed to a Member.
    * @exception  UnknownMember if the supplied Member is unknown within the scope 
    * of the Membership.
    * @exception  RoleAssociationConflict may be invoked if the current RecruitmentStatus 
    * is a CLOSED_MEMBERSHIP
    * @exception  UnknownRole if a role references in the roles argument is unknown 
    * within the scope of the Membership
    */
    public void add_roles(Member member, String[] roles)
    throws UnknownMember, RoleAssociationConflict, UnknownRole
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }


   /**
    * The remove_roles operation disassociated an existing business roles attributed 
    * to a Member.
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
    public void remove_roles( Member member, String[] roles)
    throws UnknownRole, UnknownMember, org.omg.CommunityFramework.CannotRemoveRole
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }


   /**
    * The is_member operation returns true if the supplied User is a member of the membership.
    * @exception  PrivacyConflict if there is a privacy policy conflict
    */
    public boolean is_member( User user )
    throws PrivacyConflict
    {
        // WARNING:  Privacy assessment not implemented!
	  if( locateMember( user ) != null ) return true;
	  return false;
    }

   /**
    * The has_role operation returns true if the supplied User is associated 
    * to the Membership under a role corresponding to the supplied identifier.
    * @exception  PrivacyConflict if there is a privacy policy conflict
    */
    public boolean has_role( User user, String role )
    throws PrivacyConflict
    {
	  // WARNING: Privacy assessment not implemented!
        Recognizes link = locateMember( user );
	  if( link == null ) return false;
	  for( int i=0; i<link.roles.length; i++ )
        {
            if( link.roles[i].equals( role ) ) return true;
        }
	  return false;
    }


   /**
    * The get_member_roles operation returns the sequence of all role identifiers 
    * associated with the supplied user.
    * @param  user User to assess in terms of attributed roles
    * @exception PrivacyConflict if there is a privacy policy conflict
    * @osm.warning policy assessment not implemented
    */
    public String[] get_member_roles( User user )
    throws PrivacyConflict
    {
        Recognizes link = locateMember( user );
	  if( link == null ) return new String[0];
	  return link.roles;
    }


   /**
    * The list_members operation returns an iterator of all User instances 
    * associated with the Membership.  The max_number argument constrains 
    * the maximum number of User instances to include in the returned list sequence.
    * @param  max_number int - the maximum number of User instances to include
    *         in the returned UserHolder sequence
    * @param  list UsersHolder - a sequence of user instances
    * @exception <code>PrivacyConflict</code> if there is a privacy policy conflict
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
    *         the returned UsersHolder sequence.
    * @param  list UsersHolder a sequence of User instances
    * @exception PrivacyConflict if there is a privacy policy conflict
    * @osm.warning Privacy assessment not implemented
    */
    public UserIterator list_members_using( String role, int max_number, UsersHolder users )
    throws PrivacyConflict
    {
	  UserIterator userIterator = null;
        LinkedList list = store.members();
        synchronized( list )
        {
	      try
	      {
		    Iterator iterator = list.iterator();
	          UserIteratorDelegate delegate = new UserIteratorDelegate( orb, iterator, role );
	          UserIteratorPOA servant = new UserIteratorPOATie( delegate );
		    userIterator = servant._this( orb);

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


    // ==========================================================================
    // AbstractResource operation override
    // ==========================================================================
    
   /**
    *  The most derived type that this <code>AbstractResource</code> represents.
    * @return the IDL typecode for a <code>Community</code>
    */
    public TypeCode resourceKind()
    {
        getLogger().info("resourceKind");
        touch( store );
        return CommunityHelper.type();
    }
    

   /**
    * Extension of the Workspace bind operation to support reception of the
    * notification of association dependencies from external resources on this
    * Commuinity.
    *
    * @param link Link notification of an association dependency
    */
    public synchronized void bind(Link link)
    throws ResourceUnavailable, ProcessorConflict, SemanticConflict
    {
        
        synchronized( store )
        {
            
            if( getLogger().isDebugEnabled() ) getLogger().debug("[COM] bind: " + link.getClass().getName());
            touch( store );
            
            if( link instanceof Recognizes )
            {
                
                // Notification to this Community that a User is being added to
                // to the set of members.
                
                LinkedList list = store.members();
                synchronized( list )
                {
                    if( !containsLink( list, link ))
                    {
                        addLink( getCommunityReference(), list, link );
                    }
                    else
                    {
                        throw new SemanticConflict();
                    }
                }
            }
            else
            {
                super.bind( link );
            }
        }
    }

   /**
    * Replaces an existing dependecy with another.
    *
    * @param  old the Link to replace
    * @param  new the replacement Link
    * @exception  <code>ResourceUnavailable</code> if the resource cannot accept the new link binding
    * @exception  <code>SemanticConflict</code> if the resource cannot accept the link binding due to a cardinality constraint.
    */
    public synchronized void replace(Link old, Link _new)
    throws ResourceUnavailable, ProcessorConflict, SemanticConflict
    {
        synchronized( store )
        {
            if( getLogger().isDebugEnabled() ) getLogger().debug("[COM] replace");
            touch( store );
            if (( old instanceof Recognizes) && ( _new instanceof Recognizes ))
            {
                // client is requesting the replacement of an exiting Recognizes
                // association
                
                try
                {
                    replaceLink( getCommunityReference(), store.members(), old, _new );
                }
                catch (Exception e)
                {
                    String s = "unexpected exception while resolving 'Recognizes' association";
                    if( getLogger().isErrorEnabled() ) getLogger().error(s,e);
                    throw new org.omg.CORBA.INTERNAL(s);
                }
            }
            else
            {
                super.replace( old, _new );
            }
        }
    }
    
   /**
    * Releases an existing dependecy.
    * @param link Link to retract
    */
    public synchronized void release(Link link)
    {
        synchronized( store )
        {
            
            getLogger().info("[COM] release " + link.getClass().getName());
            touch( store );
            
            if( link instanceof Recognizes )
            {
                
                // Notification of the retraction of a Recognizes dependency.
                
                try
                {
                    LinkedList list = store.members();
                    releaseLink( getCommunityReference(), list, link );
                }
                catch( Exception e)
                {
                    String s = "failed to release Recognizes association";
                    if( getLogger().isErrorEnabled() ) getLogger().error( s, e );
                    throw new org.omg.CORBA.INTERNAL( s );
                }
            }
            else
            {
                super.release( link );
            }
        }
    }
    
   /**
    * Expansion of links maintained by this Community.
    * @param type the base IDL type of a Link associations to be returned
    * @param max_number the number of lik instances to be packed into the links holder
    * @param links a holder of a sequence of link instances
    * @return iterator of Link instances
    */
    public LinkIterator expand(org.omg.CORBA.TypeCode type, int max_number, LinksHolder links)
    {
        getLogger().info("[COM] expand");
        LinkIterator linkIterator = null;

	  if( type.equals( RecognizesHelper.type() ))
        {
            LinkedList members = store.members();
            synchronized( members )
            {
                // prepare resource sequence
                links.value = create_link_sequence( members, max_number ); 
		    touch( store );
                LinkIteratorDelegate delegate = new LinkIteratorDelegate( orb, members, type );
	          LinkIteratorPOA servant = new LinkIteratorPOATie( delegate );
		    return servant._this( orb);
            }
        }
        else
        {
            return super.expand( type, max_number, links );
        }
    }
    
    // ==========================================================================
    // Internal methods
    // ==========================================================================

   /**
    * Set the object reference to be returned for this delegate.
    * @param workspace the object reference for the community
    */
    protected void setCommunityReference( Community community )
    {
        m_community = community;
        setWorkspaceReference( community );
    }

   /**
    * Returns the object reference for this delegate.
    * @return Community the object referenced for the delegate
    */
    protected Community getCommunityReference( )
    {
        return m_community;
    }

   /**
    * Returns the Recognizes link associating a User to the Membership.
    * @param user the User object reference to be used as a search key
    * @return the link holding the reference to the User and its associated roles names
    */
    protected Recognizes locateMember( User user ) throws RuntimeException
    {
        LinkedList list = store.members();
        synchronized( list )
        {
	      try
	      {
		    Iterator iterator = list.iterator();
		    while( iterator.has_next() )
		    {
			  LinkStorage link = (LinkStorage) iterator.next();
			  Recognizes recognizes = (Recognizes) link.link();
			  if( recognizes.resource().get_key().equal( user.get_key() ))
				return recognizes;
		    }
		}
		catch( Exception e )
		{
		    String error = "Unexpected exception while locating a Recognizes association.";
		    if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
                throw new RuntimeException( error, e );
		}
		return null;
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
    protected MembershipCount membershipCount( Role role )
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
}
