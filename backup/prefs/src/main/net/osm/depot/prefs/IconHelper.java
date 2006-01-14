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

package net.dpml.depot.prefs;

import java.net.URL;

import javax.swing.JLabel;
import javax.swing.ImageIcon;

/**
 * Utility class for loading image icons.
 */
public class IconHelper
{
    public static JLabel createImageIconJLabel( 
      ClassLoader classloader, String path, String description, String text )
    {
        ImageIcon icon = createImageIcon( classloader, path, description );
        JLabel label = new JLabel( text, icon, JLabel.LEFT );
        return label;
    }

    public static ImageIcon createImageIcon( ClassLoader classloader, String path, String description )
    {
        URL url = classloader.getResource( path );
        if( null != url )
        {
            return new ImageIcon( url );
        }
        else
        {
            final String error =
              "Supplied image icon resource path is unknown ["
              + path 
              + "].";
            throw new IllegalArgumentException( error );
        }
    }
}
