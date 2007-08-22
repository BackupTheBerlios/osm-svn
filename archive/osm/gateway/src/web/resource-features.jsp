
<%@ taglib uri="http://home.osm.net/web" prefix="osm" %>

<!--
The resource-features page adds a set of table row declarations to a enclosing
table.  The features are based o featrues exposed by an AbstractResourceAdapter 
instance.
-->


  <tr><td align="left"><p class="entry">domain:</p></td>
    <td><p class="entry"><osm:adapter feature="domain"/></P></tr></td>
  <tr><td align="left"><p class="entry">identity:</p></td>
    <td><p class="entry"><osm:adapter feature="identity"/></P></tr></td>
  <tr><td align="left"><p class="entry">created:</p></td>
    <td><p class="entry"><osm:adapter feature="creationDate"/></P></tr></td>
  <tr><td align="left"><p class="entry">modified:</p></td>
    <td><p class="entry"><osm:adapter feature="modificationDate"/></P></tr></td>
  <tr><td align="left"><p class="entry">access:</p></td>
    <td><p class="entry"><osm:adapter feature="accessDate"/></P></td>
  </tr>


