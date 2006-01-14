/*
 * Copyright 2005 Stephen J. McConnell.
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

package org.acme.tutorial;

import java.util.logging.Logger;

/**
 * A minimal component.
 * 
 * @author <a href="http://www.osm.net">Open Service Management</a>
 */
public class SimpleWidget implements Widget
{
    //------------------------------------------------------------------
    // context
    //------------------------------------------------------------------

   /**
    * The internal context interface through which the component declares 
    * its functional requirements and delivery strategy.
    */
    public interface Context
    {
       /**
        * Return a string describing an activity that our widget should 
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
        * The wisget implementation applies an activity to an owners object.  The
        * name of the object is provided in the form of a target.  A target could
        * be a house, a bike, a car, or whatever object appeals to the manager of 
        * the widget.
        *
        * @return the name of the owner's target object to which the activity will
        *   be applied
        */
        String getTarget();
    }

    //------------------------------------------------------------------
    // state
    //------------------------------------------------------------------

    private final Logger m_logger;
    private final Context m_context;

    //------------------------------------------------------------------
    // constructor
    //------------------------------------------------------------------

   /**
    * Creation of a new widget using a supplied logger and context.
    * @param logger the supplied logging channel
    * @param context a context implementation fulfilling the internal Contexzt interface
    */
    public SimpleWidget( Logger logger, Context context  )
    {
        m_logger = logger;
        m_context = context;
    }

    //------------------------------------------------------------------
    // Widget
    //------------------------------------------------------------------

   /**
    * Implementation of the widget service contract during which a 
    * message is logged.
    *
    * @param color applies the supplied colour within the context of a configured 
    *   activity, owner and target object
    */
    public void process( String color )
    {
        String message = buildMessage( color );
        m_logger.info( message );
    }

    //------------------------------------------------------------------
    // internals
    //------------------------------------------------------------------

   /**
    * Utility operation to construct a message.
    * @param color the colour to use
    * @return the string (used by the testcase)
    */
    public String buildMessage( String color )
    {
        String owner = m_context.getOwner();
        String activity = m_context.getActivity();
        String target = m_context.getTarget();

        return activity + " " + owner + "'s " + target + " " + color + ".";
    }
}
