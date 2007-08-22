// Thu Nov 23 07:22:01 CET 2000

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
Starting a CollaborationProcessor is enabled through the start or initialize operation. These actions
cause the establishment of an initial active state and active-state path. Actions such as
SimpleTransition enable modification of the active-state-path leading to the potential exposure of
a TerminalTransition action. Once a TerminalTransition action has been fired, the hosting
processor enters a closed and completed state (refer ProcessState). A CollaborationProcessor
implementation signals this change though modification of the state attribute on the inherited
Processor interface (and corresponding structured event). This attribute returns a StateDescriptor
which itself contains the Completion valuetype declared under the CollaborationModel
TerminalTransition (indicating Success or Failure of the process).
*/

public class TerminalTransition
implements Transitional, StreamableValue, ValueFactory
{
    
    //==========================================================
    // state
    //==========================================================
    
    /**
    * Declaration of processor termination the hosting processor will expose the
    * Completion result instance, indicating the success or failure of the process.
    */
    public Completion result;
  
    //==========================================================
    // constructors
    //==========================================================

    /**
    * Null argument constructor used during stream internalization.
    */
    public TerminalTransition() {}
  
   /**
    * Creates a TerminalTransition based on a supplied configuration.
    */
    public TerminalTransition( Configuration config ) 
    {
        try
	  {
		result = new Completion( config );
	  }
	  catch( Throwable e )
	  {
		throw new RuntimeException(
              "unable to create new configured terminal transition", e );
	  }
    }

    //==========================================================
    // implementation
    //==========================================================
    
    public TypeCode _type()
    {
        return org.omg.CollaborationFramework.TerminalTransitionHelper.type();
    }
    
    public void _read(InputStream is)
    {
        result = CompletionHelper.read( is);
    }
    
    public void _write(OutputStream os)
    {
        CompletionHelper.write( os, result);
    }
    
    static final String[] _ids_list = { 
        "IDL:omg.org/CollaborationFramework/TerminalTransition:1.0",
    };
    
    public String[] _truncatable_ids() { return _ids_list; }

    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new TerminalTransition() );
    }
}
