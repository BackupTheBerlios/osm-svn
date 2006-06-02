/*
/*
 * Copyright 2006 Stephen J. McConnell
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

package net.osm.process.info;

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;

import net.dpml.lang.DuplicateKeyException;

import net.dpml.util.DecodingException;
import net.dpml.util.DOM3DocumentBuilder;
import net.dpml.util.ElementHelper;

import org.w3c.dom.Element;
import org.w3c.dom.Document;

/**
 * Utility class supporting workspace decoding.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class WorkspaceDecoder
{
    private static final DOM3DocumentBuilder DOCUMENT_BUILDER = new DOM3DocumentBuilder();
    
   /**
    * Construct a workspace from XML source.
    * @param source the XML source file
    * @return the layout directive
    * @exception IOException if an IO exception occurs
    */
    public WorkspaceDirective build( File source ) throws IOException
    {
        if( null == source )
        {
            throw new NullPointerException( "source" );
        }
        if( !source.exists() )
        {
            throw new FileNotFoundException( source.toString() );
        }
        if( source.isDirectory() )
        {
            final String error = 
              "File ["
              + source 
              + "] references a directory.";
            throw new IllegalArgumentException( error );
        }
        try
        {
            final Element root = getRootElement( source );
            return buildWorkspaceDirective( root );
        }
        catch( IOException e )
        {
            throw e;
        }
        catch( Exception e )
        {
            final String error = 
              "Unexpected error while attempting to load project layout."
              + "File: '" + source + "'";
            IOException ioe = new IOException( error );
            ioe.initCause( e );
            throw ioe;
        }
    }
    
    private Element getRootElement( File source ) throws IOException
    {
        File file = source.getCanonicalFile();
        final Document document = DOCUMENT_BUILDER.parse( file.toURI() );
        return document.getDocumentElement();
    }
    
   /**
    * Build a layout using an XML element.
    * @param base the base directory
    * @param element the module element
    * @return the library directive
    * @exception Exception if an exception occurs
    */
    private WorkspaceDirective buildWorkspaceDirective( Element element ) throws DecodingException, DuplicateKeyException
    {
        String name = ElementHelper.getAttribute( element, "name" );
        InfoDirective info = buildInfoDirective( ElementHelper.getChild( element, "info" ) );
        String path = ElementHelper.getAttribute( element, "path" );
        Element[] children = ElementHelper.getChildren( element );
        ProductDirective[] products = new ProductDirective[ children.length ];
        for( int i=0; i<children.length; i++ )
        {
            Element child = children[i];
            products[i] = buildProductDirective( child );
        }
        return new WorkspaceDirective( name, info, path, products );
    }
    
    private ProductDirective buildProductDirective( Element element ) throws DecodingException, DuplicateKeyException
    {
        String tag = element.getTagName();
        if( "dir".equals( tag ) )
        {
            return buildDirectoryDirective( element );
        }
        else if( "file".equals( tag ) )
        {
            return buildFileDirective( element );
        }
        else if( "collection".equals( tag ) )
        {
            return buildCollectionDirective( element );
        }
        else if( "workspace".equals( tag ) )
        {
            return buildWorkspaceDirective( element );
        }
        else
        {
            final String error = 
              "Element tag name ["
              + tag 
              + "] is not recognized.";
            throw new DecodingException( element, error );
        }
    }
    
    private DirectoryDirective buildDirectoryDirective( Element element )
    {
        String name = ElementHelper.getAttribute( element, "name" );
        String path = ElementHelper.getAttribute( element, "path" );
        InfoDirective info = buildInfoDirective( ElementHelper.getChild( element, "info" ) );
        return new DirectoryDirective( name, info, path );
    }
    
    private FileDirective buildFileDirective( Element element )
    {
        String name = ElementHelper.getAttribute( element, "name" );
        String type = ElementHelper.getAttribute( element, "type" );
        String base = ElementHelper.getAttribute( element, "base" );
        InfoDirective info = buildInfoDirective( ElementHelper.getChild( element, "info" ) );
        return new FileDirective( name, info, type, base );
    }
    
    private CollectionDirective buildCollectionDirective( Element element )
    {
        throw new UnsupportedOperationException( "buildCollectionDirective" );
    }

    private InfoDirective buildInfoDirective( Element element )
    {
        String title = ElementHelper.getAttribute( element, "title" );
        Element elem = ElementHelper.getChild( element, "description" );
        String description = ElementHelper.getValue( elem );
        return new InfoDirective( title, description );
    }
}
