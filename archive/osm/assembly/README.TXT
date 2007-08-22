
Assembly – an open service managemet architecture.
==================================================

This package contains work in progress originating from the Merlin container.  It includes a set of service based on the containerkit abstraction dealing with the defintion of a system kernel, container hierachies, component assembly, and component deployment and decommissioning.  Everything is this package is ALPHA and is subject to changes and restructuring arising from changes in the containerkit package and general evolution of the Assembly packages.

A demonstration of current status is normally possible by invoking the following ant targets:

   ant dist
   ant kernel

This will cause the build of the kernel, container framework, type repository, a set of demo components, and the creation and deployment of a root container holding a combination of component providers and subsidiary containers.

Status
------

03-JUL-2002.
The initialization of the component will  automatically build a component deployment graph based on implicit dependencies and service declarations based on of components available in the classpath, together with explict declarations including in container configurations.

To-Do
-----

The kernel should take advantage of Excalibur LogKit extensions (logging configuration etc.). Functional operation of the container as a node in a container hierachy is still very much work-in-progreess.  

The component instantiation and lifecycle management is not included at this stage.  Before proceeding with actual component instantiation - more playing around is required with the containerkit abstractions.

Context creation is based on the current ContextFactory class.  This needs to be updated in accordance with the specifications for context and context value creation directives.  Also, the registry needs to be updated to provide support for the import of container based context values (refer spec).

Management of defaults using the CascadingConfiguration need to be included.  The original Merlin approach was to include the default configuration information inside the .xinfo file.  The approach taken here eill be to seperate default information into a <classname>.profile document.  

Introduction of component factories will be required as a complement to the <component/> directive.


Stephen McConnell
mcconnell@apache.org

