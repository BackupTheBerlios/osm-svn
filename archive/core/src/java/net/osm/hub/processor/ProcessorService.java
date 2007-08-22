/*
 */
package net.osm.hub.processor;

import org.omg.CosPersistentState.NotFound;
import org.omg.CollaborationFramework.Processor;
import org.omg.CollaborationFramework.ProcessorCriteria;
import net.osm.hub.gateway.FactoryException;
import net.osm.hub.resource.AbstractResourceService;
import org.apache.avalon.framework.component.Component;


/**
 * Factory interface through which a Workspace reference can be created.
 *
 * @author Stephen McConnell <mcconnell@osm.net>
 */

public interface ProcessorService extends AbstractResourceService
{

   /**
    * Creation of a new Processor object reference.
    * @param name the name of the processor
    * @param criteria the <code>ProcessorCriteria</code> defining 
    *   process constraints and/or parameters
    * @exception FactoryException
    */
    public Processor createProcessor( String name, ProcessorCriteria criteria ) 
    throws FactoryException;

   /**
    * Creation of a new Processor object reference.
    * @param name the name of the processor
    * @param criteria the <code>ProcessorCriteria</code> defining 
    *   process constraints and/or parameters
    * @param label the label identifying the appliance to use to execute the process
    * @exception FactoryException
    */
    public Processor createProcessor( String name, ProcessorCriteria criteria, String label ) 
    throws FactoryException;

   /**
    * Returns a reference to a Processor given a persistent storage object identifier.
    * @param pid Processor persistent identifier
    * @return Processor corresponding to the PID
    * @exception NotFound if the supplied pid does not match a know Processor
    */
    public Processor getProcessorReference( byte[] pid )
    throws NotFound;


}



