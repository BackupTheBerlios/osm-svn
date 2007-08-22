// Mon Dec 18 05:36:51 CET 2000

package org.omg.CommunityFramework;

import java.io.Serializable;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.Session.AbstractResource;
import org.omg.Session.Privilege;
import org.omg.Session.Link;
import org.omg.Session.UserHelper;

import net.osm.community.CommunityRuntimeException;


/**
* Member is a type of Privilege link (refer Task and Session) that defines 
* relationship between a Membership and a User.  Recognizes is the 
* inverse association of Member that associates a Membership with a Users.  
* A Member instance when held by a Membership implementation 
* references the participating User.  The inverse relationship, held by an 
* implementation of User, contains a reference to the target Membership.  
*/

public class Member
implements Privilege, StreamableValue, ValueFactory
{
    
    //==========================================================
    // static
    //==========================================================
    
   /**
    * Return the truncatable ids
    */
    static final String[] _ids_list = { 
        "IDL:omg.org/CommunityFramework/Member:1.0",
    };

    //==========================================================
    // state
    //==========================================================
    
   /**
    * The reference to a Membership that the User, holding this link is a member of.
    */
    public Membership resource_state;
    
    //==========================================================
    // constructors
    //==========================================================

    /**
    * Default constructor used during internalization of a Member instance from a 
    * valuetype stream.
    */
    public Member() 
    {
    }

   /**
    * Construct a new <code>Member</code> link based on a supplied 
    * <code>Membership</code>.
    * @param resource the <code>Membership</code> that this Member instance
    * is associated to.
    * @exception <code>IllegalArgumentException</code> 
    * if resource is <code>null</code>.
    */
    public Member( Membership resource ) 
    {
	  if (resource == null) throw new IllegalArgumentException("null Membership reference");
        this.resource_state = resource;
    }

    //==========================================================
    // Member
    //==========================================================

    /**
     * The resource operation returns the <code>AbstractResource</code> reference 
     * corresponding to a concrete Membership instance.
     * @return AbstractResource corresponding to a concrete Membership reference.
     */   
    public AbstractResource resource( ) {
        return (AbstractResource) resource_state;
    }

   /**
    * Factory operation through which the inverse link can be created.
    * @param resource the <code>AbstractResource</code> to bind as the 
    * source of the inverse relationship.
    */
    public Link inverse( final AbstractResource resource )
    {
        try
        {
            return new Recognizes( UserHelper.narrow( resource ));
        }
        catch( Throwable e )
        {
            throw new CommunityRuntimeException( 
               "Unexpected error occured while attempting to create an inverse link.",
               e );
        }
    } 



   /**
    * Return the value TypeCode
    */
    public TypeCode _type()
    {
        return MemberHelper.type();
    }
    
   /**
    * Unmarshal the value into an InputStream
    */
    public void _read(InputStream is)
    {
        resource_state = MembershipHelper.read(is);
    }
    
   /**
    * Marshal the value into an OutputStream
    */
    public void _write(OutputStream os)
    {
        MembershipHelper.write(os, resource_state);
    }
    
   /**
    * Return the truncatable ids
    */
    public String[] _truncatable_ids() { return _ids_list; }
    
   /**
    * Member factory.
    */
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new Member() );
    }

}
