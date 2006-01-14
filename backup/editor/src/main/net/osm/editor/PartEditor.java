/*
 * Copyright (c) 2005 Stephen J. McConnell
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

package net.osm.editor;

import java.awt.Component;
import java.io.IOException;
import java.net.URI;

import javax.swing.tree.TreeNode;

import net.dpml.part.Part;
import net.dpml.part.Component;

/**
 * Interface implemented by part editors.
 *
 * @author <a href="mcconnell@dpml.net">Stephen J. McConnell</a>
 */
public interface PartEditor
{
    Part getPart();

    Component getComponent();

    Component[] getPartPanels();

    TreeNode[] getPartNodes();
}
