// Thu Nov 23 07:22:01 CET 2000

package org.omg.CollaborationFramework;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.ValueBase;

/**
 * Interface implemented by classes acting in the role of a usage description.
 * The ProcessorModel valuetype defines a set of usage (input and output) towards its controlling
 * Task. These declarations are expressed as a set of UsageDescriptor instances (equivalent to the
 * declaration of argument parameters). Collectively, the set of UsageDescriptor instances declare
 * the naming convention to be applied to tagged Usage links held by the co-ordinating Task. Usage
 * declarations are defined through the valuetypes InputDescriptor and OutputDescriptor. Both
 * valuetypes contain the declaration of a tag name (corresponding to the usage tag string) and a
 * type field containing a TypeCode value. The OutputDescriptor contains an additional required
 * field that if true, states that the link must exist or be supplied. If false, the input 
 * declaration can be considered as an optional argument.
 */

public interface UsageDescriptor extends ValueBase
{

      /**
      * Return the TypeCode of the resource required under this usage descriptor.
      * @return TypeCode
      */
	public TypeCode getType();

      /**
      * Return the TypeCode identifier as a String.
      * @return String
      */
	public String getID();

      
      /**
      * Return the tag name that this usage descriptor is defining.
      * @return String corresponding to the usage tag
      */
	public String getTag();

}
