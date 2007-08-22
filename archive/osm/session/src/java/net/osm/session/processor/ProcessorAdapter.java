
package net.osm.session.processor;

import org.apache.avalon.framework.activity.Startable;
import org.apache.avalon.framework.activity.Suspendable;

/**
 * An adapter providing EJB style access to a <code>Processor</code>.
 */
public interface ProcessorAdapter extends AbstractProcessorAdapter, Startable, Suspendable
{
}
