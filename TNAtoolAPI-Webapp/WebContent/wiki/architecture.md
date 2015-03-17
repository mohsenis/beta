Transit Network Analysis Software Tool Architecture
=========

The TNA tool is a web application and therefore has a client-server architecture. The architecture of the TNA tool is displayed in the picture below. 

![](images/architecture.png "TNA Software Tool Architecture")

Client Side Application
---------

The client side application has a Graphical User Interface (GUI) that displays the information to the user and processes user requests and sent them to the server side application. Client side application is developed in HTML and Javascript. Javascript libraries such as Leaflet, jQuery, jQuery-ui, jQurey-ui dialogextend, jQuery-ui markercluser, and DataTables are also used in the development of the GUI.

Server Side Application
---------

The server side application is responsible for storing, processing, and providing information to the client side application. The server side application is written in Java and PotgreSQL and PostGIS have been used as the relational database engine. Hibernate and Hibernate spatial libraries have been used to interface the server side application with the database.
