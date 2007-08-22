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
import org.omg.Session.User;
import org.omg.Session.UserHelper;

/**
* Recognizes is a type of Privilege link (refer Task and Session) that defines 
* relationship between a Membership and a User.  Member is the 
* inverse association of Recognizes that associates a User with a Membership.  
* A Recognizes instance, held by a Membership implementation 
* references the participating User.  The inverse relationship, held by an 
* implementation of User (called Member), contains a reference to the target 
* Membership.  
*/

public class Recognizes
    implements Privilege, StreamableValue, ValueFactory
{
    
    //==========================================================
    // static
    //==========================================================
    
   /**
    * Return the truncatable ids
    */
    static final String[] _ids_list = { 
        "IDL:omg.org/CommunityFramework/Recognizes:1.0",
    };

    //==========================================================
    // state
    //==========================================================

   /**
    * The User that this association refers to.
    */
    public User resource_state;
    
   /**
    * A sequnce of role names that the User is associated under 
    * within the hosting Membership.
    */
    public String[] roles;
    
    //==========================================================
    // constructors
    //==========================================================

    /**
    * Default constructor used during internalization of a Recognizes instance from a 
    * valuetype stream.
    */
    public Recognizes() 
    {
    }

   /**
    * Construct a new <code>Recognizes</code> link based on a supplied 
    * <code>User</code>.
    * @param resource the <code>User</code> that this hosting Membership instance
    * is associated to.
    * @exception <code>IllegalArgumentException</code> 
    * if resource is <code>null</code>.
    */
    public Recognizes( User resource, String[] roles ) 
    {
	  if (resource == null) throw new IllegalArgumentException("null User reference");
	  if (roles == null) throw new IllegalArgumentException("null roles reference");
        this.resource_state = resource;
        this.roles = roles;
    }

    //==========================================================
    // Recognizes
    //==========================================================

    /**
     * The resource operation returns the <code>AbstractResource</code> reference 
     * corresponding to a User instance.
     * @return AbstractResource corresponding to a User reference.
     */   
    public AbstractResource resource( ) {
        return resource_state;
    }

   /**
    * Return the value TypeCode
    */
    public TypeCode _type()
    {
        return RecognizesHelper.type();
    }
    
   /**
    * Unmarshal the value into an InputStream
    */
    public void _read(InputStream is)
    {
        resource_state = UserHelper.read(is);
        roles = LabelsHelper.read(is);
    }
    
   /**
    * Marshal the value into an OutputStream
    */
    public void _write(OutputStream os)
    {
        UserHelper.write(os, resource_state);
        LabelsHelper.write(os, roles);
    }
    
   /**
    * Return the truncatable ids
    */
    public String[] _truncatable_ids() { return _ids_list; }

   /**
    * Recognizes factory.
    */
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new Recognizes() );
    }

}
