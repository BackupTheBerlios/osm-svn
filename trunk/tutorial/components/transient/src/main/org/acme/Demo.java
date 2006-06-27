/*
 * Copyright 2006 Stephen J. McConnell.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.acme;

import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * A minimal component.
 * 
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class Demo
{
   /**
    * Utility interface used to resolve internal parts.
    */
    public interface Context
    {
       /**
        * Return the loop count
        * @return the count
        */
        int getCount();
        
    }
    
   /**
    * Utility interface used to resolve internal parts.
    */
    public interface Parts
    {
       /**
        * Return the gizmo.
        * @return the gizmo
        */
        Gizmo getGizmo();
    }
    
    //------------------------------------------------------------------
    // constructor
    //------------------------------------------------------------------
    
   /**
    * Creation of a new object using a supplied logging channel, context and 
    * internal parts.
    * @param logger the logging channel
    * @param context the deployment context
    */
    public Demo( final Logger logger, final Context context, Parts parts )
    {
        int n = context.getCount();
        for( int i=0; i<n; i++ )
        {
            Gizmo gizmo = parts.getGizmo();
            if( logger.isLoggable( Level.INFO ) )
            {
                int id = System.identityHashCode( gizmo );
                logger.info( " gizmo [" + (i+1) + "] " + id );
            }
        }
    }
}
