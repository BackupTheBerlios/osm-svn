/**
 * License: etc/LICENSE.TXT
 * Copyright: Copyright (C) The Apache Software Foundation. All rights reserved.
 * Copyright: OSM SARL 2001-2002, All Rights Reserved.
 * Copyright: 2000 (C) Intalio Inc. All Rights Reserved.
 */

package net.osm.sps;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.activity.Startable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.service.DefaultServiceManager;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.Serviceable;


/**
 * Static utility to process a component through is lifecycle.
 */
public class LifecycleHelper
{

    public static void pipeline( 
      Object object, Logger logger, Context context, Configuration config, 
      ServiceManager manager ) throws Exception
    {
        if( object == null )
        {
            throw new NullPointerException(
              "Illegal null object argument.");
        }

        if( object instanceof LogEnabled ) 
        {
            if( logger == null ) 
            {
                throw new NullPointerException(
                  "Illegal null logger argument.");
            }
            else
            {
                ((LogEnabled)object).enableLogging( logger );
            }
        }

        if( object instanceof Contextualizable )
        {
            if( context == null ) 
            {
                throw new NullPointerException(
                  "Illegal null context argument.");
            }
            else
            {
                ((Contextualizable)object).contextualize( context );
            }
        }

        if( object instanceof Configurable )
        {
            if( config == null ) 
            {
                throw new NullPointerException(
                  "Illegal null configuration argument.");
            }
            else
            {
                ((Configurable)object).configure( config );
            }
        }

        if( object instanceof Serviceable )
        {
            if( manager == null ) 
            {
                throw new NullPointerException(
                  "Illegal null manager argument.");
            }
            else
            {
                ((Serviceable)object).service( manager );
            }
        }

        if( object instanceof Initializable )
        {
            ((Initializable)object).initialize();
        }
    }
}
