/*
 */
package net.osm.session.processor;

import net.osm.chooser.ChooserService;
import net.osm.session.resource.AbstractResourceService;

/**
 * Factory interface through which a Processor reference can be created.
 *
 * @author Stephen McConnell <mcconnell@osm.net>
 */

public interface ProcessorService extends AbstractResourceService
{

    public static final String PROCESSOR_SERVICE_KEY = "PROCESSOR_SERVICE_KEY";

   /**
    * Creation of a Processor.
    * @param name the initial name to assign to the processor
    * @param appliance the class name of the appliance to establish as the process logic
    * @return Processor a new Processor object reference
    */
    public Processor createProcessor( String name, String appliance ) 
    throws ProcessorException;

   /**
    * Creation of a new <code>StorageObject</code> representing the 
    * state of a new <code>AbstractResource</code> instance.
    * @param name the name to apply to the new resource
    * @param appliance the class name of the appliance to establish as the process logic
    * @return StorageObject storage object encapsulating the resource state
    */
    public ProcessorStorage createProcessorStorage( String name, String appliance ) throws ProcessorException;

   /**
    * Return a reference to an object as an Processor.
    * @param ProcessorStorage storage object for the processor
    * @return Processor object reference to the processor
    */
    public Processor getProcessorReference( ProcessorStorage store );

}