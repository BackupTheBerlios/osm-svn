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

import java.util.Arrays;

import net.dpml.lang.AbstractDirective;

/**
 * The ProcessDirective describes a process in terms of dependent processes, 
 * consumed products, post-execution validation processes, execution parameters, 
 * the process implmentation class, the process name, it's implicit execution 
 * status, and an optional output product identifier.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ProcessDirective extends AbstractDirective
{
    private final String m_name;
    private final String m_classname;
    private final boolean m_implicit;
    private final String m_production;
    private final String[] m_dependencies;
    private final InputDirective[] m_inputs;
    private final String[] m_validators;
    private final DataDirective m_data;
    
   /**
    * Creation of a new process directive.
    * @param name the process name
    * @param classname the processor classname
    * @param implicit the implicit flag
    * @param production the output type
    * @param dependencies the set of dependent processor names
    * @param inputs the set of input resources
    * @param validators the set of validation processes
    * @param data the processor data
    */
    public ProcessDirective( 
      final String name, final String classname, boolean implicit, String production,
      final String[] dependencies, InputDirective[] inputs, String[] validators, 
      DataDirective data )
    {
        if( null == name )
        {
            throw new NullPointerException( "name" );
        }
        if( null == classname )
        {
            throw new NullPointerException( "classname" );
        }
        if( null == dependencies )
        {
            throw new NullPointerException( "dependencies" );
        }
        if( null == inputs )
        {
            throw new NullPointerException( "inputs" );
        }
        if( null == validators )
        {
            throw new NullPointerException( "validators" );
        }
        m_name = name;
        m_classname = classname;
        m_implicit = implicit;
        m_production = production;
        m_dependencies = dependencies;
        m_inputs = inputs;
        m_validators = validators;
        m_data = data;
    }
    
   /**
    * Get the product name.
    * @return the product name
    */
    public String getName()
    {
        return m_name;
    }

    
   /**
    * Get the processor classname.
    * @return the classname
    */
    public String getClassname()
    {
        return m_classname;
    }
    
   /**
    * Get the value of the implicit flag.
    * @return the implicit status
    */
    public boolean isImplicit()
    {
        return m_implicit;
    }
    
   /**
    * Get the id of the prioduced product (possibly null).
    * @return the produced product id
    */
    public String getProductionID()
    {
        return m_production;
    }
    
   /**
    * Get the array of dependent process names.
    * @return the dependent process names
    */
    public String[] getDependencies()
    {
        return m_dependencies;
    }

   /**
    * Get the array of input descriptors.
    * @return the input descriptor array
    */
    public InputDirective[] getInputDirectives()
    {
        return m_inputs;
    }

   /**
    * Get the array of validation process names.
    * @return the validation process names
    */
    public String[] getValidators()
    {
        return m_validators;
    }

   /**
    * Get the default data for the process.
    * @return the default data directive
    */
    public DataDirective getDataDirective()
    {
        return m_data;
    }

   /**
    * Compare this object with another for equality.
    * @param other the other object
    * @return true if equal
    */
    public boolean equals( Object other )
    {
        if( super.equals( other ) && ( other instanceof ProcessDirective ) )
        {
            ProcessDirective object = (ProcessDirective) other;
            if( !m_name.equals( object.m_name ) )
            {
                return false;
            }
            else if( !m_classname.equals( object.m_classname ) )
            {
                return false;
            }
            else if( m_implicit != object.m_implicit )
            {
                return false;
            }
            else if( !equals( m_production, object.m_production ) )
            {
                return false;
            }
            else if( !Arrays.equals( m_dependencies, object.m_dependencies ) )
            {
                return false;
            }
            else if( !Arrays.equals( m_inputs, object.m_inputs ) )
            {
                return false;
            }
            else if( !Arrays.equals( m_validators, object.m_validators ) )
            {
                return false;
            }
            else
            {
                return equals( m_data, object.m_data );
            }
        }
        else
        {
            return false;
        }
    }
    
   /**
    * Compute the hash value.
    * @return the hashcode value
    */
    public int hashCode()
    {
        int hash = super.hashCode();
        hash ^= hashValue( m_name );
        hash ^= hashValue( m_classname );
        if( m_implicit )
        {
            hash ^= 35;
        }
        hash ^= hashValue( m_production );
        hash ^= hashArray( m_dependencies );
        hash ^= hashArray( m_inputs );
        hash ^= hashArray( m_validators );
        hash ^= hashValue( m_data );
        return hash;
    }
}
