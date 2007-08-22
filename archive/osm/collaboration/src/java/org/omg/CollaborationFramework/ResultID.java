// Tue Dec 19 09:08:58 CET 2000

package org.omg.CollaborationFramework;

import org.omg.CORBA.portable.ValueBase;

/**
* Container of a int value indicating an implementation specific result code.
*/

public class ResultID implements ValueBase
{

   /**
    * Implementation specific result code.
    */
    public int value;

   /**
    * ResultID constructor based on supplied code.
    */
    public ResultID(int initial) { value = initial; }
    
    private static final String[] _ids = { 
        ResultIDHelper.id()
    };
    
    public String[] _truncatable_ids() { return _ids; }
}
