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
public class Gizmo
{
   /**
    * Gizmo dependencies.
    */
    public interface Context
    {
       /**
        * Return the reference to a Widget service.
        * @return the widget
        */
        Widget getWidget();
    }
    
    private final Context m_context;
    
   /**
    * Creation of a new gizmo.
    * @param context the deployment context
    */
    public Gizmo( Context context )
    {
        m_context = context;
    }
    
   /**
    * Return the wdget resolved via the dployment context.
    * @return the widget
    */
    public Widget getWidget()
    {
        return m_context.getWidget();
    }
}
