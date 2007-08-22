// Tue Dec 19 08:53:39 CET 2000

package org.omg.CollaborationFramework;

import org.omg.CORBA.portable.ValueBase;

/**
* Container of a boolean value indicating the success (true) or 
* failure (false) of a processor.
*/

public class ResultClass implements ValueBase
{

   /**
    * The logical success (true) or failure (false) value.
    */
    public boolean value;

   /**
    * Constructor of a ResultClass with a supplied boolean value.
    */
    public ResultClass(boolean initial) { value = initial; }

    // openorb compile bug requires this constructor

    public ResultClass(int i) 
    { 
	  value = true; 
    }
    
    //
    // implementation of ValueBase
    //
    
    private static final String[] _ids = { 
        ResultClassHelper.id()
    };
    
    public String[] _truncatable_ids() { return _ids; }

}
