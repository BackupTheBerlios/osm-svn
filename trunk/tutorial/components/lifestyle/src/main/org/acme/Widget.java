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

import java.awt.Color;

/**
 * A test component.
 * 
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class Widget
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
        * Return the color.
        *
        * @return the color
        */
        Color getColor();
    }
    
    //------------------------------------------------------------------
    // immutable state
    //------------------------------------------------------------------
    
    private final Context m_context;
    
    //------------------------------------------------------------------
    // constructor
    //------------------------------------------------------------------
    
   /**
    * Creation of a new widget.
    * @param context the deployment context
    */
    public Widget( final Context context )
    {
        m_context = context;
    }
    
    //------------------------------------------------------------------
    // services
    //------------------------------------------------------------------
    
   /**
    * Return the color value assigned via the component context.
    * @return the color
    */
    public Color getColor()
    {
        return m_context.getColor();
    }
}
