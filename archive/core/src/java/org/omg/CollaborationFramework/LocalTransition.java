// Thu Nov 23 07:22:01 CET 2000

package org.omg.CollaborationFramework;

import java.io.Serializable;

import org.apache.avalon.framework.configuration.Configuration;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ValueFactory;

/**
LocalTransition enables the possible modification of usage relationships (if the containing Trigger
enables this), and the possibility to reset timeout constraints associated with the containing
Trigger. LocalTransition can be considered as a transition from the current active state to the same
state, where side effects concerning timeout and usage relationships can be declared.
*/

public class LocalTransition
implements Transitional, StreamableValue, ValueFactory 
{
    
    //============================================================
    // state
    //============================================================
    
   /**
    * If true, any timeout conditions established through Triggers 
    * containing Clocks are reset following application of the 
    * containing trigger under a CollaborationProcessor.
    */
    public boolean reset;
    

    //============================================================
    // constructors
    //============================================================

   /**
    * Null argument constructor used during stream internalization.
    */
    public LocalTransition() 
    {
    }

   /**
    * Creates a LocalTransition based on a supplied configuration.
    */
    public LocalTransition( Configuration config )
    {
	  try
	  {
            reset = config.getAttributeAsBoolean( "reset", false );
	  }
	  catch( Exception e)
	  {
	      throw new RuntimeException("unable to create new configured LocalTransition", e );
	  }
    }

    //============================================================
    // implementation
    //============================================================
    
    public TypeCode _type()
    {
        return LocalTransitionHelper.type();
    }
    
    public void _read( InputStream is)
    {
        reset = is.read_boolean();
    }
    
    public void _write(OutputStream os)
    {
        os.write_boolean(reset);
    }
    
    //
    // implementation of ValueBase
    //
    
    static final String[] _ids_list = { 
        "IDL:omg.org/CollaborationFramework/LocalTransition:1.0",
    };
    
    public String[] _truncatable_ids() { return _ids_list; }

    //
    // implementation of ValueFactory
    //
    
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new LocalTransition() );
    }

}
