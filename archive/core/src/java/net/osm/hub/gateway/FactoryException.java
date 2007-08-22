
package net.osm.hub.gateway;

/**
 * FactoryException is an exception thrown by a factory.
 */

public class FactoryException extends Exception
{
   /**
    * Creates new <code>FactoryException</code> without detail message.
    */
    public FactoryException() {
    }

    /**
     * Constructs an <code>FactoryException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public FactoryException(String msg) {
        super(msg);
    }

    /**
     * Constructs an <code>FactoryException</code> with the specified detail message
     * and cause.
     * @param msg the detail message.
     */
    public FactoryException(String msg, Throwable e ) {
        super(msg, e );
    }
}
