/*
 */
package net.osm.hub.processor;

import java.io.File;
import org.apache.avalon.framework.component.Component;

/**
 * ProcessorContext is an object passed as an argument during the contextualize
 * phase of a component lifecycle. 
 *
 * @author Stephen McConnell <mcconnell@osm.net>
 */

public class AppliancePathContext implements Component 
{

     private File appliancePath;

    /**
     * Creation of a new ApplianceContext using a File containing the 
     * path of the directory containing the appliace implementation jar
     * files.
     */
     public AppliancePathContext( File path )
     {
         this.appliancePath = path;
     }

    /**
     * Returns the directory containing the appliance jar files.
     */
     public File getApplianceDirectory()
     {
        return appliancePath;
     }
}



