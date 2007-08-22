/**
 */

package net.osm.ins;

import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;

import java.util.Vector;
import java.util.NoSuchElementException;
import java.util.Enumeration;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.*;
import org.omg.PortableServer.POAPackage.ObjectNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;
import org.omg.PortableServer.POAPackage.ServantAlreadyActive;

/**
 * This class is the implementation of BindingIterator.
 */
public class BindingIteratorServant extends BindingIteratorPOA implements LogEnabled
{

    //================================================
    // State members
    //================================================

   /**
    * Reference to the binding list
    */
    private Enumeration _bl;

   /**
    * Log channel.
    */
    Logger log;

    //================================================
    // Constructors
    //================================================
    
   /**
    * Constructor
    */
    public BindingIteratorServant( Logger log, ORB orb, Vector bl ) { 
        
        this.log = log;

        _bl = bl.elements();
        log.debug("elements: " + bl.size());
                        
        ((org.omg.CORBA_2_3.ORB)orb).set_delegate( this );
        try
        {
            _default_POA().activate_object( this );            
        }
        catch ( ServantAlreadyActive ex )
        { 
            log.warn("servant already active");
        }
        catch ( WrongPolicy ex )
        { 
            String error = "wrong policy exception";
            log.error( error, ex );
            throw new RuntimeException( error );
        }
    }

    // ===========================================================
    // Loggable implementation
    // ===========================================================
    
   /**
    * Sets the log channel for this instance.
    */
    public void enableLogging( final Logger logger )
    {
        log = logger;
    }

    //================================================
    // Binding Iterator Implememntation
    //================================================

   /**
    * This operation returns the next binding. 
    * 
    * @return true if there are no more bindings, false is returned.
    */
    public boolean next_one(BindingHolder b) { 
        
        try 
        { 
            b.value = ( Binding ) _bl.nextElement();
            log.debug("next_one: true");
            return true;
        }
        catch ( NoSuchElementException e ) 
            { 
            b.value = new Binding();
            b.value.binding_name = new NameComponent[0];
            b.value.binding_type = BindingType.nobject;
            log.debug("next_one: false");
            return false;
        }
    }

   /**
    * This operation returns at most the requested number of bindings.
    */
    public boolean next_n(int how_many, BindingListHolder bl) 
      { 
        
        log.debug("next_n");
        int max = how_many;
        if ( max == 0 ) return false;
        
        Vector bindings = new Vector();
        
        for ( int i=0; i<max; i++ ) 
            { 
            BindingHolder bh = new BindingHolder();
            
            boolean result = next_one( bh );
            
            if ( !result ) 
            { 
                
                bl.value = new Binding[bindings.size()];
                for ( int k=0; k < bindings.size() ; k++ ) {
                    bl.value[k] = ( Binding ) bindings.elementAt(k);
                }
                if ( bindings.size() == 0 )
                    return false;
                else
                    return true;
            }
            else 
            {
                bindings.addElement(bh.value);
            }    
        }

        bl.value = new Binding[bindings.size()];
        for ( int k=0; k < bindings.size() ; k++ ) 
        {
            bl.value[k] = ( Binding ) bindings.elementAt(k);
        }
        return true;
    }

   /**
    * This operation destroys the iterator.
    */
    public void destroy()
    {
        try
        {            
            _poa().deactivate_object( _object_id() );        
        }
        catch ( ObjectNotActive ex )
        {
            log.warn("attempt to disactivate an inactive object");
        }
        catch ( WrongPolicy ex )
        {
            String error = "wrong policy exception while disactivating the binding iterator";
            log.error( error, ex );
        }
    }
    
}
