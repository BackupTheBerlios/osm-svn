// Thu Nov 23 07:22:02 CET 2000

package org.omg.CollaborationFramework;

import java.util.LinkedList;
import java.io.Serializable;

import org.apache.avalon.framework.configuration.Configuration;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.CommunityFramework.Role;
import org.omg.CommunityFramework.RoleHelper;

/**
 * EngagementModel extends ProcessorModel through the addition of three values, a Role used to
 * qualify the engagement context, a declaration of the maximum lifetime of an Engagement
 * process, and a value indicating if the engagement has a unilateral implication on the members of
 * an associated Encounter.
 */

public class EngagementModel
extends ProcessorModel
{
    
    //
    //  state members
    //
    
   /**
    * The value of quorum under this Role indicates the number of engagements
    * required following which engagement is considered as binding.
    */

    public Role role;

   /**
    * The maximum lifetime of the process commencing on transition of the
    * process to a running state. A zero, negative or null value is equivalent to
    * no constraint on process lifetime. 
    */
    public Duration lifetime;

   /**
    * If true, the process of engagement shall be considered as binding on all
    * members. If false, then the act of engagement is considered as binding
    * on members that have actively engaged. Members that have not
    * invoked the engage operation shall not be considered as bound to the
    * engagement. 
    */
    public boolean unilateral;
    

    // 
    // constructors
    //

   /**
    * Null argument constructor used during stream internalization.
    */
    public EngagementModel( ){}

   /**
    * Creates a EngagementModel based on a supplied DOM element.
    */
    public EngagementModel( Configuration config )
    {
	  super( config );
        try
	  {
		unilateral = config.getAttributeAsBoolean("unilateral",true);
		lifetime = new Duration( config.getAttributeAsLong("duration",(120*60)));
		role = new Role( config.getChild("role") );
	  }
	  catch( Throwable e )
	  {
		throw new RuntimeException(
              "unable to create new configured simple transition", e );
	  }
    }

    //
    // implementation of Streamable
    //
    
    public TypeCode _type()
    {
        return EngagementModelHelper.type();
    }
    
    public void _read(InputStream is)
    {
        super._read(is);
        role = RoleHelper.read(is);
        lifetime = DurationHelper.read(is);
        unilateral = is.read_boolean();
    }
    
    public void _write(OutputStream os)
    {
        super._write(os);
        RoleHelper.write(os, role);
        DurationHelper.write(os, lifetime);
        os.write_boolean(unilateral);
    }
    
    //
    // implementation of ValueBase
    //
    
    private static final String[] _ids_list = { 
        "IDL:omg.org/CollaborationFramework/EngagementModel:1.0",
    };
    
    public String[] _truncatable_ids() { return _ids_list; }

    //
    // implementation of ValueFactory
    //
    
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new EngagementModel() );
    }

}
