
package net.osm.gateway.util;

import net.osm.adapter.ServiceAdapter;
import net.osm.chooser.ChooserAdapter;
import net.osm.chooser.ChooserRuntimeException;


/**
 * Utility class supporting chooser related web content.
 */
public class ChooserUtil
{

    //=================================================================
    // ChooserHelper
    //=================================================================

   /**
    * Returns a string describing the breakdown of a chooser in terms
    * of supplied services.  The resulting string is a javascript directive
    * that corresponds to a hierachical breakdown presentation. 
    */

    public static String getChooserJavacript( String root, ServiceAdapter adapter )
    {
        StringBuffer buffer = new StringBuffer();
        packChooserJavacript( buffer, root, adapter );
        return buffer.toString();
    }

    private static void packChooserJavacript( StringBuffer buffer, String root, ServiceAdapter adapter )
    {
        try
        {
            if( adapter instanceof ChooserAdapter )
            {
                ChooserAdapter chooser = (ChooserAdapter) adapter;
                StringBuffer b = new StringBuffer();
                String[] array = chooser.getKey();
                for( int k=0; k<array.length; k++ )
                {
                    b.append( "_" + array[k] );
                }
                String key = b.toString();

                String name = chooser.getName();
                buffer.append( 
                  "    " 
                  + key + " = insFld( " + root 
                  + ", \n        " 
                  + "gFld(\"<span class='tree-node-text'>" 
                  + name
                  + "</span>\", " 
                  + "\n      " 
                  + "\"../" + adapter.getURL() + "\", '"
                  + key + "', \n    \"images/jmenu/folder_16_pad.gif\", \"" 
                  + key + "\"));\n\n" );

                String[] children = chooser.getNames();
                for( int i=0; i<children.length; i++ )
                {
                    packChooserJavacript( buffer, key, chooser.lookup( children[i] ) );
                }
            }
            else
            {
                String name = adapter.getName();
                buffer.append( 
                  "    insDoc( " + root + ",\n        "
                  + "gLnk(0, \"<span class='tree-node-text'>" 
                  + name
                  + "</span>\", \"../" + adapter.getURL() + "\", '"
                  + name + "', \n        " 
                  + "\"images/jmenu/jaxm_profile02.gif\", \"" 
                  + name + "\"));\n\n" );
            }
        }
        catch( Throwable e )
        {
            final String error = "Internal error while preparing services breakdown.";
            throw new ChooserRuntimeException( error, e );
        }
    }
}
