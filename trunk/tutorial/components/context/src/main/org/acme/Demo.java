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
    //------------------------------------------------------------------
    // context
    //------------------------------------------------------------------

   /**
    * The internal context interface through which the component declares 
    * its operational prerequisited.
    */
    public interface Context
    {
       /**
        * Return a string describing an activity that our object should 
        * perform. An activity is a word such as "painting" or "coloring" 
        * or any other color related activity you can think of.  The component
        * implementation will construct a phrase using this word as the operative
        * activity.
        *
        * @return the activity verb
        */
        String getActivity();
        
       /**
        * When constructing a phrase the implementation uses a owner to 
        * distringuish the ownership of the subject to which it is applying 
        * an activity. The value returned by this method could be a user's name
        * or an alias such as "batman".
        *
        * @return the owner's name
        */
        String getOwner();
        
       /**
        * The object implementation applies an activity to an owners object.  The
        * name of the object is provided in the form of a target.  A target could
        * be a house, a bike, a car, or whatever object appeals to the manager of 
        * the object.
        *
        * @return the name of the owner's target to which the activity will
        *   be applied
        */
        String getTarget();
        
       /**
        * Returns the color to be used during construction of the activity statement.
        * 
        * @return the color value
        */
        String getColor();
    }
    
    //------------------------------------------------------------------
    // constructor
    //------------------------------------------------------------------
    
   /**
    * Creation of a new object using a supplied logging channel.
    * @param logger the logging channel
    * @param context the deployment context
    */
    public Demo( final Logger logger, final Context context )
    {
        if( logger.isLoggable( Level.INFO ) )
        {
            final String owner = context.getOwner();
            final String activity = context.getActivity();
            final String target = context.getTarget();
            final String color = context.getColor();
            final String message = 
                activity 
                + " " 
                + owner 
                + "'s " 
                + target 
                + " " 
                + color 
                + ".";
            logger.info( message );
        }
    }
}
