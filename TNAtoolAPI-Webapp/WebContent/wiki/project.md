Transit Network Analysis Software Tool Project
=========

Assessing the current "state of health" of individual transit networks is a fundamental part of studies aimed at planning changes and/or upgrades to the transportation network serving a region. To be able to effect changes that benefit both the individual transit networks as well as the larger transportation system, organizations need to develop meaningful strategies guided by specific performance metrics. A fundamental requirement for the development of these performance metrics is the availability of accurate data regarding transit networks.
Prior to 2005, transit data was not readily available. This situation complicated the assessment of single transit networks, let alone performing a state-wide or region-wide study. The advent of the General Transit Feed Specification [GTFS](https://developers.google.com/transit/gtfs) changed this constrained landscape and motivated transit operators to release their schedules and route information to third party developers.

[Oregon Department of Transportation](http://www.oregon.gov/ODOT "Oregon Department of Transportation") (ODOT) [Public Transit Division](http://www.oregon.gov/ODOT/PT) (PTD) funded this research project to develop an open source Transit Network Analysis tool.
 
<img src="images/odot.jpg" alt="Oregon Department of Transportation" height="150" width="200" style="margin:0 auto;display:block;" title="Oregon Department of Transportation"> 

Phase 1
-------
First phase of the project was titled 'Proof of Concept: GTFS Data as a Basis for Optimization of Oregon's regional and statewide Transit Networks', started on Aug 2013 and finished on Dec 2013. In this phase, a review was conducted on currently available software packages that perform some form of transit network analysis. These packages varied largely in terms of the software platforms they are built upon as well as the analysis capabilities they offer. A total of 43 software packages in different areas related to the analysis of transit networks were identified and analyzed based on the following criteria:

 * Be capable of collecting and storing GTFS data. 
 * Be capable of keeping GTFS data up-to-date.
 * Allow the visualization of GTFS data on a map. Desirable features of this capability may include: displaying stops, routes, transit agency information and other information provided on a map; allowing the user to select what feed/feeds to display; and providing a web-based interface.
 * Be capable of generating different kinds of reports based on queries from GTFS data.
 * Be developed using open source tools.

The conclusion reached from this analysis was that only OpenTripPlanner met most of the criteria discussed before and provided the best balance between already available features and required development work.

<img src="images/oldtna.png" alt="Transit Network Analysis Tool - the old version" height="100" width="400" style="margin:0 auto;display:block;" title="Transit Network Analysis Tool - the old version"> 

The final product, referred to as the Transit Network Analysis (TNA) software tool, incorporates publicly available transit data and census data as its main inputs and can be used to visualize, analyze and report on the Oregon transit network. 

Note: The first phase final project report can be accessed on [ODOT](http://www.oregon.gov/ODOT/TD/TP_RES/docs/ProjectWorkPlans/SPR752WP.pdf) page.

phase 2
-------
The second phase of the project is titled "An Open Source Tool For The Visualization, Analysis And Reporting Of Regional And Statewide Transit Networks", started on Jan 2014 and will complete by the end of Dec 2014. Since the prior version of the TNA tool developed in phase one could no longer accommodate the new project requirements, in this phase a new Transit Network Analysis tool is developed from scratch using open source development tools and platforms. 
