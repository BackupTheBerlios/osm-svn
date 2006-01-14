/*
 * Copyright 2005 Stephen McConnell
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.osm.editor.composition;

import java.net.URI;

import net.dpml.part.Part;
import net.dpml.part.PartException;

import net.dpml.component.data.ComponentDirective;
import net.dpml.component.control.ClassLoaderManager;

import net.dpml.transit.Logger;
import net.dpml.transit.Transit;
import net.dpml.transit.Repository;

import net.osm.editor.PartEditor;
import net.osm.editor.PartEditorFactory;

/**
 * Edit factory for the composition datatypes. 
 */
public final class CompositionEditorFactory implements PartEditorFactory
{
    private Logger m_logger;
    private ClassLoaderManager m_manager;

    public CompositionEditorFactory( Logger logger )
    {
        m_logger = logger;
        m_manager = loadClassLoaderManager();
    }

    public PartEditor getPartEditor( Part part )
    {
        if( part instanceof ComponentDirective )
        {
            try
            {
                ComponentDirective directive = (ComponentDirective) part;
                return new ComponentDirectiveEditor( m_logger, m_manager, directive );
            }
            catch( PartException e )
            {
                final String error = 
                  "Unable to create component directive editor for: " + part;
                    throw new RuntimeException( error, e );
            }
        }
        else
        {
            final String error = 
              "Part type [" + part.getClass().getName() + "] not supported.";
            throw new IllegalArgumentException( error );
        }
    }

    private ClassLoaderManager loadClassLoaderManager()
    {
        try
        {
            ClassLoader classloader = getClass().getClassLoader();
            URI uri = new URI( "@PART-CONTROLLER-URI@" );
            Repository repository = Transit.getInstance().getRepository();
            return (ClassLoaderManager) repository.getPlugin( classloader, uri, new Object[]{m_logger} );
        }
        catch( Throwable e )
        {
            final String error =
              "Internal error while attempting to establish the composition type manager.";
            throw new RuntimeException( error, e );
        }
    }

}
