/*
 */
package net.osm.session;

import net.osm.chooser.ChooserService;
import org.apache.avalon.framework.component.Component;

/**
 * Interface supporting access to the session finder.
 *
 * @author Stephen McConnell <mcconnell@osm.net>
 */
public interface HomeService extends ChooserService
{

    public static final String SESSION_SERVICE_KEY = "SESSION_SERVICE_KEY";


   /**
    * Returns an object refererence to the session home.
    * @return Home the session home object reference.
    */
    public Home getHome();


}



