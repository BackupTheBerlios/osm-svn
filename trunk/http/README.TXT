
OSM HTTP - README
=================

Projects defined here include:

  impl   - the server impl project contains the primary HTTP server 
           class net.dpml.web.server.Server which is an extension of the 
           Jetty 6.X Server implemetnation.  The DPML implementation 
           extends the Jetty class to support Context IOC together with
           a collection of Jetty related components usable as part of 
           a server definition (including http context types and 
           content handlers for static content, servlets, and web-apps).

  war    - an example war file creation project

  server - an example server configuration prepared as a resolvable
           part artifact

  test   - project used to test deployment of the server which is 
           preconfigured to use the example war artifact

  