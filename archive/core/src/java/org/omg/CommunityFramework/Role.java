// Thu Nov 23 07:22:00 CET 2000

package org.omg.CommunityFramework;

import java.io.Serializable;
import java.util.LinkedList;

import org.apache.avalon.framework.configuration.Configuration;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ValueFactory;

/**
 * Role is a valuetype that declares the notion of a "business role" 
 * of a User.  The state fields label and note inherited from Control 
 * are used to associate a role name and role description.  Role 
 * supplements this information with an additional three state fields, 
 * policy, is_abstract and roles.  The roles field contains a sequence 
 * of role instances through which role hierarchies can be constructed. 
 * The policy field value is RolePolicy valuetype that qualifies the 
 * quorum, ceiling, quorum assessment and quorum policy applicable to 
 * the containing role.  A Role can be declared as an abstract role by 
 * setting the is_abstract state field value to true.  Declaring the 
 * role as abstract disables direct association of a User to the Role 
 * under a Membership.  Instead, members can associate lower-level roles, 
 * thereby implicitly associating themselves with the containing roles.
 * <p>
 * Examples of business role hierarchies include the logical association 
 * of "customer" and "supplier" as roles under a parent named 
 * "signatories".  In this example, both "customer" and "supplier" would 
 * be modeled as Role instances with is_abstract set to false, and 
 * contained within a single Role named "signatories".  By setting the 
 * "signatories" role is_abstract value to true, Members cannot directly 
 * associate to this role.  Instead, Members associating to either 
 * "customer" or "supplier" are implicitly granted "signatory" association. 
 * <p>
 * An implementation is responsible for ensuring the consistency of quorum 
 * and ceiling values across a role hierarchy.
 */

public class Role extends Control
{
    
    //==========================================================
    // static
    //==========================================================
    
   /**
    * Return the truncatable ids
    */
    static final String[] _ids_list = { 
        "IDL:omg.org/CommunityFramework/Role:1.0",
    };

    //==========================================================
    // state
    //==========================================================
    
   /**
    /**
    * A sequence of Role instances that are considered as children relative to the 
    * containing role.  Association of a Member to a child role implicitly associates 
    * the Member with all parent roles.
    */
    public Role[] roles;

    /**
    * Defines policy associated with an instance of RoleContainer or RoleElement.  
    * If null, no direct policy constraint is implied.
    */
    public RolePolicy policy; 
 
   /**
    * If true, Member instances may not be directly associated with the role under 
    * a Membership.  Members may be associated implicitly through association to a 
    * non-abstract sibling.
    */
    public boolean is_abstract; 

    private Role parent;

    //==========================================================
    // constructors
    //==========================================================
    
   /**
    * Default constructor used during internalization of a Role instance from a 
    * valuetype stream.
    */
    public Role( )
    {
    }

   /**
    * Creation of a new Role based on a supplied configuration.
    */
    public Role( Configuration conf )
    {
	  super( conf );
        this.is_abstract = conf.getAttributeAsBoolean("abstract",false);
	  Configuration p = conf.getChild( "rpolicy", false );
        if( p != null )
        {
            this.policy = new RolePolicy( p );
		this.policy.setRole( this );
        }
        Configuration[] children = conf.getChildren("role");
        int n = children.length;
        if( n > 0 ) 
        {
	      this.roles = new Role[n];
	      for( int i=0; i<n; i++ )
            {
		    Role child = new Role( children[i] );
		    child.parent = this;
                this.roles[i] = child;
            }
        }
    }

    /**
    * Creation of a new default role.
    * @param label name of the role
    * @param note description of the role
    * @param is_abstract true if this is an abstract role
    */
    public Role( String label, String note, Role[] children, boolean is_abstract )
    {
        super( label, note );
	  this.roles = children;
	  this.is_abstract = is_abstract;
        this.roles = children;
    }

    //==========================================================
    // impelmenetation
    //==========================================================

    /**
     * getPolicy returns the immediate or first inherited role policy.
     */
     public RolePolicy getPolicy() {
         if( policy != null ) return policy;
	   if( parent != null ) {
		return parent.getPolicy();
	   } else {
		return RolePolicy.getDefaultPolicy();
	   }
     }

   /**
    *Return the sequence of roles.
    */
    public Role[] getRoles( ) 
    {
        if( this.roles == null ) return new Role[0];
	  return this.roles;
    }

   /**
    *Return a role corresponding to the supplied role label.  Returns null if
    *no matching role.
    */
    public Role lookupRole( String label ) 
    {
        if( this.label.equals( label )) return this;
        if( this.roles != null ) {
            for( int i = 0; i< this.roles.length; i++) {
                Role r = ((Role)roles[i]).lookupRole( label );
                if( r != null) return r;
            }
        }
        return null;
    }

   /**
    * Return the value TypeCode
    */
    public TypeCode _type()
    {
        return RoleHelper.type();
    }
    
   /**
    * Unmarshal the value into an InputStream
    */
    public void _read(InputStream is)
    {
        super._read(is);
        policy = RolePolicyHelper.read(is);
        roles = RolesHelper.read(is);
        is_abstract = is.read_boolean();
    }
    
   /**
    * Marshal the value into an OutputStream
    */
    public void _write(OutputStream os)
    {
        super._write(os);
        RolePolicyHelper.write(os, policy);
        RolesHelper.write(os, roles);
        os.write_boolean(is_abstract);
    }
    
   /**
    * Return the truncatable ids
    */
    public String[] _truncatable_ids() { return _ids_list; }

   /**
    * Role factory.
    */
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new Role() );
    }
}
