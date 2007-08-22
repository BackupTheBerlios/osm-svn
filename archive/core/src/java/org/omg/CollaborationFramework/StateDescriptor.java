// Tue Dec 19 00:44:42 CET 2000

package org.omg.CollaborationFramework;

import java.io.Serializable;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.Session.task_state;
import org.omg.CommunityFramework.Problem;
import org.omg.CommunityFramework.ProblemsHelper;


/**
 * Processor state is accessible through the state attribute. The state attribute returns an instance of
 * StateDescriptor, a valuetype containing an enumeration value of the process state equivalent to
 * the state model defined under Task and Session specification (refer Task and Session, Task
 * Specification). StateDescriptor also contains a state field named problems that exposes any
 * standing problems concerning processor configuration or execution.
 */

public class StateDescriptor
implements StreamableValue, ValueFactory
{
    
    //
    //  state members
    //
    
    /**
    * The state of the processor at a general level (equivilent to the general
    * task state enumeration values.
    */
    public task_state processor_state;

   /**
    * A completion status (SUCCESS or FAILURE and result id).  May be null if 
    * the processor exposing this instance has not completed execution.
    */
    public Completion completion;

   /**
    * A sequence of problems raised by the processor presenting normal execution.
    */
    public Problem[] problems;
    
    /**
    * Null argument constructor used during stream internalization.
    */
    public StateDescriptor(){}

   /**
    * Constructor of a StateDescriptor based on a suplied initial task state value
    * (where task_state is the type of argument used to express processor state).
    */
    public StateDescriptor( task_state state ) 
    {
	  this.processor_state = state;
    }

    /**
    * Constructor of a StateDescriptor based on a suplied initial task state value
    * (where task_state is the type of argument used to express processor state).
    */
    public StateDescriptor( task_state state, Problem problem ) 
    {
        this.processor_state = state;
        problems = new Problem[]{ problem };
    }

    //
    // implementation of Streamable
    //
    
    public TypeCode _type()
    {
        return StateDescriptorHelper.type();
    }
    
    public void _read(InputStream _is)
    {
        processor_state = ProcessorStateHelper.read(_is);
        completion = CompletionHelper.read(_is);
        problems = ProblemsHelper.read(_is);
    }
    
    public void _write(org.omg.CORBA.portable.OutputStream _os)
    {
        ProcessorStateHelper.write(_os, processor_state);
        CompletionHelper.write(_os, completion);
        ProblemsHelper.write(_os, problems);
    }
    
    //
    // implementation of ValueBase
    //
    
    static final String[] _ids_list = { 
        "IDL:omg.org/CollaborationFramework/StateDescriptor:1.0"
    };
    
    public String[] _truncatable_ids() { return _ids_list; }

    //
    // implementation of ValueFactory
    //
    
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new StateDescriptor() );
    }

}
