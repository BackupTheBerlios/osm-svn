/*
 */
package net.osm.factory;

import org.apache.avalon.framework.component.Component;

import net.osm.adapter.Adaptive;

/**
 * Interface supporting creation of new adaptive object references.
 *
 * @author Stephen McConnell <mcconnell@osm.net>
 */
public interface FactoryService extends Component
{

    public static final String FACTORY_SERVICE_KEY = "FACTORY_SERVICE_KEY";

   /**
    * Returns an object refererence to the factory.
    * @return Factory the factory object reference.
    */
    public Factory getFactory();

    /**
     * Creates a new object reference
     * @param  arguments an array of arguments 
     * @return  Adaptive an adaptive object reference
     * @exception  UnrecognizedCriteria if the arguments established by the
     *   adapter implementation is unknown to the factory
     * @exception  InvalidCriteria if the arguments created by the 
     *   implementation is recognized but rejected as invalid
     */
    public Adaptive create( Argument[] arguments ) 
    throws UnrecognizedCriteria, InvalidCriteria, CreationException;

}



