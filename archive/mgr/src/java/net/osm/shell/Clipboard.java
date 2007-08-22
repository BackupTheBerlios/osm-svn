/* 
 * Shell.java
 */

package net.osm.shell;


/**
 * The <code>Clipboard</code> interface defines the operations
 * through which cut and copy operations can transfer an object
 * to the clipboard state, and access that object for purpose of
 * executing a paste operation.
 *
 * @author Stephen McConnell
 */

public interface Clipboard
{

   /**
    * Place a copy of an object into the cliboard buffer.
    * @param object the entity to place on the clipboard
    */
    public void putScrap( Object[] object );

   /**
    * Returns the current clipboard object.
    * @return the current (possibly null) clipboard object
    */
    public Object[] getScrap( );

}
