
package org.omg.CollaborationFramework;

import java.io.Serializable;

import org.apache.avalon.framework.configuration.Configuration;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ValueFactory;

/**
SimpleTransition is Transitional that enables a state transition from the current active state to a
State declared under by the SimpleTransition target value. A successful invocation of apply or
apply_arguments on CollaborationProcessor will result in the change of the CollaborationProcessor
active state to the state referenced by the target value.
*/

public class SimpleTransition
implements Transitional, StreamableValue, ValueFactory
{
    
    //=========================================================================
    // state
    //=========================================================================
    
    /**
    * The state to be established as the active state of the CollaborationProcessor
    * @see CollaborationProcessor#active_state
    */
    public State target;
        
    //=========================================================================
    // constructor
    //=========================================================================

    /**
    * Null argument constructor used during stream internalization.
    */
    public SimpleTransition() {
    }
    
   /**
    * Creates a SimpleTransition based on a supplied DOM element.
    */
    public SimpleTransition( CollaborationModel model, Configuration conf ) 
    {
        try
	  {
		target = model.lookupState( conf.getAttribute("target") );
	  }
	  catch( Throwable e )
	  {
		throw new RuntimeException(
              "unable to create new configured simple transition", e );
	  }
    }

    //=========================================================================
    // implementation
    //=========================================================================
    
    public TypeCode _type()
    {
        return SimpleTransitionHelper.type();
    }
    
    public void _read(InputStream _is)
    {
        target = StateHelper.read(_is);
    }
    
    public void _write(OutputStream _os)
    {
        StateHelper.write(_os, target);
    }
        
    static final String[] _ids_list = { 
        "IDL:omg.org/CollaborationFramework/SimpleTransition:1.0",
    };
    
    public String[] _truncatable_ids() { return _ids_list; }

    //
    // implementation of ValueFactory
    //
    
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new SimpleTransition() );
    }    
}
