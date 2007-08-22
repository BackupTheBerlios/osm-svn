
package org.omg.CollaborationFramework;

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
* Launch is a valuetype that contains a mode constraint (one of INITIATOR, RESPONDENT or PARTICIPANT) 
* and a reference to a role that qualifies accessibility of the containing Trigger relative to Members of an 
* associated Encounter.
*/

public class Launch
implements Guard, StreamableValue, ValueFactory
{
    
    //=====================================================================
    // state
    //=====================================================================
    
   /**
    * A value corresponding to one of INITIATOR, RESPONDENT or PARTICIPANT.
    */
    public TriggerMode mode;

   /**
    * If the role value is not null, a client invoking the containing trigger must
    * be associated to the Encounter under a role with a label equal to the role
    * identifier.
    */
    public Role role;
   
    //=====================================================================
    // constructors
    //=====================================================================
    
    /**
    * Null argument constructor used during stream internalization.
    */

    public Launch( ){}

    public Launch( CollaborationModel model, Configuration config )
    {
	  try
	  {
	      String roleLabel = config.getAttribute("role","default");
	      role = model.lookupRole( roleLabel );
	      if( role == null ) throw new RuntimeException("could not resolve role");

	      String modeString = config.getAttribute("mode","PARTICIPANT");
            if( modeString.equals("INITIATOR") )
	      {
                mode = TriggerMode.INITIATOR;
	      }
	      else if( modeString.equals("RESPONDENT") )
	      {
                mode = TriggerMode.RESPONDENT;
	      }
	      else
	      {
                mode = TriggerMode.PARTICIPANT;
	      }

	      if( role == null ) throw new Exception("unresolved role '" + roleLabel + "'");
	  }
	  catch( Throwable e )
	  {
	      throw new RuntimeException("unable to create Launch", e );
	  }
    }

    //=====================================================================
    // implementation
    //=====================================================================
    
    public TypeCode _type()
    {
        return LaunchHelper.type();
    }
    
    public void _read(InputStream is )
    {
        mode = TriggerModeHelper.read( is );
        role = RoleHelper.read( is );
    }
    
    public void _write(OutputStream os)
    {
        TriggerModeHelper.write( os, mode);
        RoleHelper.write( os, role);
    }
        
    static final String[] _ids_list = { 
        "IDL:omg.org/CollaborationFramework/Launch:1.0",
    };
    
    public String[] _truncatable_ids() { return _ids_list; }
    
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new Launch() );
    }

}
