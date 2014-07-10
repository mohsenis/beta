var INIT_LOCATION = new L.LatLng(44.141894, -121.783660); 
var AUTO_CENTER_MAP = false;
var ROUTER_ID = "";
//alert (window.screen.availHeight);

var map = new L.Map('map', {
	minZoom : 6,
	maxZoom : 17,
	// what we really need is a fade transition between old and new tiles without removing the old ones
});

var OSMURL    = "http://{s}.mqcdn.com/tiles/1.0.0/osm/{z}/{x}/{y}.png";
var aerialURL = "http://{s}.mqcdn.com/tiles/1.0.0/sat/{z}/{x}/{y}.png";

var minimalLayer = new L.StamenTileLayer("toner");
//var mapboxAttrib = "Tiles from <a href='http://mapbox.com/about/maps' target='_blank'> Streets</a>";
//var minimalLayer = new L.TileLayer(CloudURL, {maxZoom: 17, attribution: mapboxAttrib});
var osmAttrib = 'Map data &copy; 2013 OpenStreetMap contributors';
var osmLayer = new L.TileLayer(OSMURL, 
		{subdomains: ["otile1","otile2","otile3","otile4"], maxZoom: 18, attribution: osmAttrib});

var aerialLayer = new L.TileLayer(aerialURL, 
		{subdomains: ["oatile1","oatile2","oatile3","oatile4"], maxZoom: 18, attribution: osmAttrib});

map.addLayer(osmLayer);

///*****************Leaflet Draw******************///
var dialogAgencies = new Array();
var dialogAgenciesId = new Array();
var dialog = $( "#dialog-form" ).dialog({
    autoOpen: false,
    height: 400,
    width: 350,
    modal: false,
    draggable: false,
    resizable: false,
    closeOnEscape: false,
    position: { my: "right top", at: "right-50 top", of: window },
    //position: [calc(100% - 40), 2],
    buttons: {
      "Submit": dialogResultOpen/*function() {
    	  dialogResults.dialog( "open" );
    	  $('.ui-dialog:eq(2)').css('top','400px');  
      }*/,
      Cancel: function(){
    	  dialog.dialog( "close" );
      }
    },
    close: function() {
      //form[ 0 ].reset();
    },
    open: function( event, ui ) {
    	//$(".ui-dialog-titlebar-close", ui.dialog || ui).hide();
    	$('#dialogLat').html(drawCentroid[0]);
    	$('#dialogLng').html(drawCentroid[1]);
    	$('#dialogArea').html(Math.round(area*100)/100);
    	$('#popRadio').prop('checked', true);
    	if($('#routeselect option').length<=1){
	    	for(var i = 0; i < dialogAgencies.length; i++) {
	    	    var opt1 = document.createElement('option');
	    	    var opt2 = document.createElement('option');
	    	    opt1.innerHTML = dialogAgencies[i];
	    	    opt1.value = dialogAgenciesId[i];
	    	    opt2.innerHTML = dialogAgencies[i];
	    	    opt2.value = dialogAgencies[i];
	    	    $('#routeselect').append( opt1 );
	    	    $('#stopselect').append( opt2 );
	    	}
    	}
    	$('#routeselect option[value="all"]').prop("selected",true);
    	$('#stopselect option[value="all"]').prop("selected",true);
    },
  }).dialogExtend({
	  "closable" : false,
      "minimizable" : true,
      "minimizeLocation": "right"
  });
var dialogResults = $( "#dialogResults" ).dialog({
    autoOpen: false,
    height: 500,
    width: 400,
    modal: false,
    draggable: false,
    resizable: false,
    closeOnEscape: false,
    position: { my: "right", at: "right", of: window },
    buttons: {
      
      Close: function(){
    	  dialogResults.dialog( "close" );
      }
    },
    close: function() {
      //form[ 0 ].reset();
    },
    open: function( event, ui ) {
    	
    },
  }).dialogExtend({
	  "closable" : false,
      "minimizable" : true,
      "minimizeLocation": "right",
      "restore" : function(evt) {
    	  $('.ui-dialog:eq(2)').css('top','400px'); 
      }
  });
//$('.ui-dialog:eq(2)').css('margin-top','400px');
//$('div.ui-dialog:nth-child(8)').css('bottom','0px');
 /*var form = dialog.find( "form" ).on( "submit", function( event ) {
    event.preventDefault();
    alert();
  });*/
/*$('div.ui-dialog:nth-child(6) > div:nth-child(1) > button:nth-child(3)').click(function(){
	alert();
});*/

var drawnItems = new L.FeatureGroup();
map.addLayer(drawnItems);

var drawControl = new L.Control.Draw({
	draw: {
		polyline: false,
		polygon: {
			
			allowIntersection: false,
			showArea: true,
			drawError: {
				color: '#b00b00',
				timeout: 1000
			},
			shapeOptions: {
				color: 'blue'
			}
		},
		circle: {
			shapeOptions: {
				color: '#662d91'
			}
		},
		marker: false
	},
	edit: {
		featureGroup: drawnItems,
	}
});
map.addControl(drawControl);

map.on('draw:drawstart', function (e) {
	
	drawnItems.clearLayers();
	dialog.dialog( "close" );
	dialogResults.dialog( "close" );
});

$('.leaflet-draw-edit-remove').click(function(event){
	//event.preventDefault(); 
	//event.stopPropagation();
	drawnItems.clearLayers();
	dialog.dialog( "close" );
	dialogResults.dialog( "close" );
});

var getCentroid = function (arrr) { 
	var arr = new Array();
	for(var i=0;i<arrr.length;i++){
		var tmpP = [arrr[i].lat,arrr[i].lng];
		arr[i]=tmpP;
	}
    return arr.reduce(function (x,y) {
        return [x[0] + y[0]/arr.length, x[1] + y[1]/arr.length]; 
    }, [0,0]); 
};
var drawCentroid = [0,0];
var area=0;
map.on('draw:created', function (e) {
	type = e.layerType,
	layer = e.layer;
	layer.on('click', function() {
		dialog.dialog( "open" );
	});
	if (type === 'circle') {
		//alert(layer.getLatLng());
		drawCentroid[0] = layer.getLatLng().lat;
		drawCentroid[1] = layer.getLatLng().lng;
		//alert(layer.getRadius());
		area = Math.pow(layer.getRadius()*0.000621371,2)*Math.PI;
	}else{
		area = L.GeometryUtil.geodesicArea(layer.getLatLngs())*0.000000386102;
		drawCentroid = getCentroid(layer.getLatLngs());
		//alert(layer.getLatLngs().length);
	}
	drawnItems.addLayer(layer);
	dialog.dialog( "open" );
	/*var tmpCenter = new L.FeatureGroup();
	var tmpmarker = L.marker(getCentroid(layer.getLatLngs()));
	tmpCenter.addLayer(tmpmarker);
	map.addLayer(tmpCenter);*/
});
map.on('draw:edited', function (e) {
	
	/*var type = e.layerType,
	layer = e.layer;
	alert(layer);*/
	var layers = e.layers;
    layers.eachLayer(function (layer) {
        try{
        	drawCentroid[0] = layer.getLatLng().lat;
    		drawCentroid[1] = layer.getLatLng().lng;
    		//alert(layer.getRadius());
    		area = Math.pow(layer.getRadius()*0.000621371,2)*Math.PI;
        }catch(err){
        	area = L.GeometryUtil.geodesicArea(layer.getLatLngs())*0.000000386102;
    		drawCentroid = getCentroid(layer.getLatLngs());
        }
    });
	
	
	dialog.dialog( "close" );
	dialogResults.dialog( "close" );
	dialog.dialog( "open" );
});


 
////////*************************************/////////////////////
var Layers = 0;
function getdata(type,agency,route,variant,k,callback,popup) {	
	switch (type){
	case 1:
		var points = [];
		$.ajax({
			type: 'GET',
			datatype: 'json',
			url: '/TNAtoolAPI-Webapp/queries/transit/stops?&agency='+agency,
			success: function(d){		
			$.each(d.stops, function(i,stop){        	
				points.push([new L.LatLng(Number(stop.stopLat), Number(stop.stopLon)),stop.stopName]);	
				//points.push([Number(stop.stopLon),Number(stop.stopLat)]); 			
	        });				
			if (points.length!=0) callback("A"+agency,k,points,popup);
	    }});
		break;
	case 2:
		var points = [];
		$.ajax({
			type: 'GET',
			datatype: 'json',
			url: '/TNAtoolAPI-Webapp/queries/transit/stopsbyroute?&agency='+agency+'&route='+route,
			success: function(d){		
			$.each(d.stops, function(i,stop){        	
				points.push([new L.LatLng(Number(stop.stopLat), Number(stop.stopLon)),stop.stopName]);	
				//points.push([Number(stop.stopLon),Number(stop.stopLat)]); 			
	        });				
			if (points.length!=0) callback("R"+agency+route,k,points,popup);
	    }});
		break;
	case 3:
		//alert(agency+","+ route+","+variant);		
		$.ajax({
			type: 'GET',
			datatype: 'json',
			url: '/TNAtoolAPI-Webapp/queries/transit/shape?&agency='+agency+'&trip='+variant,
			success: function(d){				
			if (d.points!= null) callback(k,d.points,"V"+agency+route+variant);
	    }});
		break;
	}		
};

var info = L.control();
info.onAdd = function (map) {
    this._div = L.DomUtil.create('div', 'info'); // create a div with a class "info"
    this.update();
    return this._div;
};

info.update = function (props) {
    this._div.innerHTML = (props ?
        '<div id="box"><b>County: </b>' + props.NAME + ' <br/>' + '<b>Area: </b>'+ props.CENSUSAREA + ' mi<sup>2</sup></div>' 
        :'');
};

function style(feature) {
    return {
        fillColor: 'orange',
        weight: 2,
        opacity: 1,
        color: 'white',
        dashArray: '3',
        fillOpacity: 0.1
    };
}
function zoomToFeature(e) {
    map.fitBounds(e.target.getBounds());
}
function resetHighlight(e) {
	shapes.resetStyle(e.target);
	info.update();
}
function highlightFeature(e) {
    var layer = e.target;
    layer.setStyle({              
        fillOpacity: 0
    });
    //layer.bringToFront(); //enable if borders are changed on mouseover
    info.update(layer.feature.properties);    
}
function onEachFeature(feature, layer) {
    layer.on({
        mouseover: highlightFeature,
        mouseout: resetHighlight,
        click: zoomToFeature        
    });
}
var stops = new L.LayerGroup().addTo(map);
var routes = new L.LayerGroup().addTo(map);

var shapes = L.geoJson(shapedata, {style: style, onEachFeature: onEachFeature});
//var uscounties = new L.LayerGroup(shapes);
//uscounties.addLayer(shapes);
//uscounties.onAdd = function (obj) {
//function uscountiesonadd(obj){
    //this._div = L.DomUtil.create('div', 'info'); // create a div with a class "info"
    //this.update();
    //return this._div;
//	info.addTo(obj);
//	info.update();
	//return this._div;
//};
//uscounties.addLayer(shapes);
//uscounties.addLayer(info);
//map.addControl(new info());

var colorset = ["#6ECC39","#FF33FF","#05FAFC","#FE0A0A", "#7A00F5", "#CC6600"];
function disponmap2(layerid,k,points,popup){	
	
	//alert (points.length);
	//alert (k);
	var geojsonMarkerOptions = {
		    radius: 5,
		    fillColor: colorset[k],
		    color: "#000",
		    weight: 1,
		    opacity: 1,
		    fillOpacity: 0.6
		};
	var geojsonFeature = {
		    "type": "Feature",
		    "properties": {
		        "name": "stop",
		        "popupContent": popup
		    },
		    "geometry": {
		        "type": "MultiPoint",
		        "coordinates": points
		    }
		};
	
	function onEachFeature(feature, layer) {
		var popupContent = "";

		if (feature.properties && feature.properties.popupContent) {
			popupContent += feature.properties.popupContent;
		}

		layer.bindPopup(popupContent);
	}
	var mylayer = new L.geoJson(geojsonFeature, {
	    pointToLayer: function (feature, latlng) {
	        return L.circleMarker(latlng, geojsonMarkerOptions);
	    },
	    onEachFeature: onEachFeature
	});
	
	mylayer._leaflet_id = layerid;
	//mylayer.bindpopup("test");
	stops.addLayer(mylayer);	
	//stops.bringToFront();
};

function disponmap(layerid,k,points,popup){
	
	//maxClusterRadius: 120,
	//iconCreateFunction: function (cluster) {
	//	return new L.DivIcon({ html: cluster.getChildCount(), className: 'mycluster', iconSize: new L.Point(40, 40) });
	//},
	var markers;
	switch (k){
	case 0:
		markers = new L.MarkerClusterGroup({
			maxClusterRadius: 120,
			iconCreateFunction: function (cluster) {
			return new L.DivIcon({ html: cluster.getChildCount(), className: 'gcluster', iconSize: new L.Point(30, 30) });
		},
		spiderfyOnMaxZoom: true, showCoverageOnHover: true, zoomToBoundsOnClick: true, singleMarkerMode: true, maxClusterRadius: 30
	});
	break;
	case 1:
		markers = new L.MarkerClusterGroup({
			maxClusterRadius: 120,
			iconCreateFunction: function (cluster) {
				return new L.DivIcon({ html: cluster.getChildCount(), className: 'picluster', iconSize: new L.Point(30, 30) });
			},
			spiderfyOnMaxZoom: true, showCoverageOnHover: true, zoomToBoundsOnClick: true, singleMarkerMode: true, maxClusterRadius: 30
		});
		break;
	case 2:
		markers = new L.MarkerClusterGroup({
			maxClusterRadius: 120,
			iconCreateFunction: function (cluster) {
				return new L.DivIcon({ html: cluster.getChildCount(), className: 'ccluster', iconSize: new L.Point(30, 30) });
			},
			spiderfyOnMaxZoom: true, showCoverageOnHover: true, zoomToBoundsOnClick: true, singleMarkerMode: true, maxClusterRadius: 30
		});
		break;
	case 3:
		markers = new L.MarkerClusterGroup({
			maxClusterRadius: 120,
			iconCreateFunction: function (cluster) {
				return new L.DivIcon({ html: cluster.getChildCount(), className: 'rcluster', iconSize: new L.Point(30, 30) });
			},
			spiderfyOnMaxZoom: true, showCoverageOnHover: true, zoomToBoundsOnClick: true, singleMarkerMode: true, maxClusterRadius: 30
		});
		break;
	case 4:
		markers = new L.MarkerClusterGroup({
			maxClusterRadius: 120,
			iconCreateFunction: function (cluster) {
				return new L.DivIcon({ html: cluster.getChildCount(), className: 'pucluster', iconSize: new L.Point(30, 30) });
			},
			spiderfyOnMaxZoom: true, showCoverageOnHover: true, zoomToBoundsOnClick: true, singleMarkerMode: true, maxClusterRadius: 30
		});
		break;
	case 5:
		markers = new L.MarkerClusterGroup({
			maxClusterRadius: 120,
			iconCreateFunction: function (cluster) {
				return new L.DivIcon({ html: cluster.getChildCount(), className: 'brcluster', iconSize: new L.Point(30, 30) });
			},
			spiderfyOnMaxZoom: true, showCoverageOnHover: true, zoomToBoundsOnClick: true, singleMarkerMode: true, maxClusterRadius: 30
		});
		break;
	}
	for (var i = 0; i < points.length; i++) {
		var p = points[i];
		var marker = new L.Marker(p[0],{title:popup});
		marker.bindPopup('<b style="">'+p[1]+'</b><br><br><a class="expander" href="#"></a>'+
							
								'<div class="content">'+
									'Population Search Radius (miles) <input type="text" id="x'+p[0].lat+p[0].lng+'" style="font-size:90%;width:30px;height:20px">'+
									'<input type="button" value="Submit" style="font-size:90%;width:50px;height:22px" onclick="showPop('+p[0].lat+','+p[0].lng+')">'+
									'<br><input type="text" id="p'+p[0].lat+p[0].lng+'" style="font-size:90%;width:60px;height:20px" disabled>&nbsp&nbsp'+
									'<span style="margin-left:50px">Show on map</span>: <input type="checkbox" id="c'+p[0].lat+p[0].lng+'" onchange="triggerShow(this)">'+
					            '</div>');
		markers.addLayer(marker);
	}
	markers._leaflet_id = layerid;
	stops.addLayer(markers);
	$('.expander').simpleexpand();//remember ro delete simple expand js and link
}
/////////////////////////////////////////////////////////////////////////////////
function triggerShow(e){
	if(e.checked==false){
		map.removeLayer(markersCentroids);
	}else{
		map.addLayer(markersCentroids);
		map.fitBounds(markersCentroids.getBounds());
	}
}
map.on('click', removeAllCentroids);
var markersCentroids = new L.FeatureGroup();
function showPop(lat,lon){
	var personIcon = L.icon({
	    iconUrl: 'js/otp/person1.png',

	    iconSize:     [40, 55], // size of the icon
	    iconAnchor:   [20, 55], // point of the icon which will correspond to marker's location
	    popupAnchor:  [-3, -46] // point from which the popup should open relative to the iconAnchor
	});
	
	var x = document.getElementById('x'+lat+lon).value;
	var pop=0;
	if(!isNaN(x)){
		$.ajax({
			type: 'GET',
			datatype: 'json',
			url: '/TNAtoolAPI-Webapp/queries/transit/NearBlocks?&lat='+lat+'&lon='+lon+'&x='+x,
			async: false,
			success: function(d){
				map.removeLayer(markersCentroids);
				markersCentroids = new L.FeatureGroup();
				var circle = L.circle([lat, lon], x*1609.34, {
				    color: 'blue',
				    fillColor: '#f03',
				    fillOpacity: 0.3,
				    draggable:'true'
				});
				
				markersCentroids.addLayer(circle);
				if(d.centroids.length>10000){
					$.each(d.centroids, function(i, item){
						pop += item.population;
					});
				}else{
					$.each(d.centroids, function(i, item){
						pop += item.population;
						var marker = L.marker([item.latitude,item.longitude], {icon: personIcon});
						marker.bindPopup('Block id: '+item.id+'<br>Population: '+item.population);
						markersCentroids.addLayer(marker);
					});
				}
				
				if(document.getElementById('c'+lat+lon).checked==true){
					map.addLayer(markersCentroids);
					map.fitBounds(circle.getBounds());
				}
				
			}
		});
		document.getElementById('p'+lat+lon).value = pop;
	}
	
}
function removeAllCentroids(){
    map.removeLayer(markersCentroids);
}
///////////////////////////////////////////////////////////////////////////////////////////////////
function dispronmap(k,points,name){	
	var polyline = L.Polyline.fromEncoded(points, {	
		weight: 5,
		color: colorset[k],
		//fillColor: colorset[k],
		//color: "#000",
		//weight: 1,
		opacity: .5,
		//fillOpacity: 0.6
		smoothFactor: 1
		});	
		//mylayer.bindpopup(name);		
		polyline._leaflet_id = name;	
		routes.addLayer(polyline);
};
     
var initLocation = INIT_LOCATION;
if (AUTO_CENTER_MAP) {
	// attempt to get map metadata (bounds) from server
	var request = new XMLHttpRequest();
	request.open("GET", "/opentripplanner-api-webapp/ws/metadata", false); // synchronous request
	request.setRequestHeader("Accept", "application/xml");
	request.send(null);
	if (request.status == 200 && request.responseXML != null) {
		var x = request.responseXML;
		var minLat = parseFloat(x.getElementsByTagName('minLatitude')[0].textContent);
		var maxLat = parseFloat(x.getElementsByTagName('maxLatitude')[0].textContent);
		var minLon = parseFloat(x.getElementsByTagName('minLongitude')[0].textContent);
		var maxLon = parseFloat(x.getElementsByTagName('maxLongitude')[0].textContent);
		var lon = (minLon + maxLon) / 2;
		var lat = (minLat + maxLat) / 2;
		initLocation = new L.LatLng(lat, lon);
	}
}
map.setView(initLocation,8);

L.control.scale({'metric': false, 'position': 'bottomleft', 'maxWidth':200}).addTo(map);
var mini = new L.Control.MiniMap(new L.TileLayer(OSMURL, {subdomains: ["otile1","otile2","otile3","otile4"],minZoom: 4, maxZoom: 5, attribution: osmAttrib}),{position:'bottomleft'}).addTo(map);
$('.leaflet-control-minimap').css({'width':'180px','height':'170px','float':'left'});
$('.leaflet-control-scale-line').css({'border':'2px solid grey','line-height':'1.2','margin-left':'5px'});

$('.leaflet-control-attribution').remove();

/*var scaleD = $('.leaflet-zoom-hide > g:nth-child(1) > path:nth-child(1)').attr('d');
scaleD = scaleD.substr(scaleD.indexOf("z")-2);
function changeRec(){
	var tmpD = $('.leaflet-zoom-hide > g:nth-child(1) > path:nth-child(1)').attr('d');
	tmp = tmpD.substr(tmpD.indexOf("z")-2);
	alert(scaleD+tmp);
	//alert(tmp);
	//tmpD.replace(tmp, scaleD);
	//alert(tmpD);
	$('.leaflet-zoom-hide > g:nth-child(1) > path:nth-child(1)').attr('d',tmpD);
	alert($('.leaflet-zoom-hide > g:nth-child(1) > path:nth-child(1)').attr('d'));
	alert();
	
}*/
/*mini._mainMap.on('zoomend', function(e) {
	mini._aimingRect.redraw();
	$('.leaflet-zoom-hide > g:nth-child(1) > path:nth-child(1)').attr('d',scaleD);
	alert($('.leaflet-zoom-hide > g:nth-child(1) > path:nth-child(1)').attr('d'));
});*/


// add layers to map 
// do not add analyst layer yet -- it will be added in refresh() once params are pulled in

var baseMaps = {
	    "OSM": osmLayer,
	    "Toner": minimalLayer,
	    "Aerial Photo": aerialLayer
	};
		        
var overlayMaps = {
		"Stops": stops,
		"Routes": routes,
		"Counties": shapes
	};

map.addControl(new L.Control.Layers(baseMaps,overlayMaps));
info.addTo(map);
var $mylist = $("#list");

$mylist
.jstree({
	"checkbox": {        
        two_state: true,
        real_checkboxes: false,
        override_ui:false
     },
	"json_data" : {
		"ajax" : {
            "url" : "/TNAtoolAPI-Webapp/queries/transit/menu",
            "type" : "get",	                
            "success" : function(ops) {  
            	
            	$.each(ops.data, function(i,item){
            		dialogAgencies.push(item.data);
            		dialogAgenciesId.push(item.attr.id);
            	});
            	return ops.data;            	
            }    	               
        },
        "progressive_render" : true       
	},
	"types" : {
		"types": {
		"default": {
		"select_node" : false
		}
		}
	},
	"themes": {
        "theme": "default-rtl",
        "url": "js/lib/jstree-v.pre1.0/themes/default-rtl/style.css",
        "dots": false,
        "icons":false
    },
    "contextmenu" : {
        "items" : function (node) {
        	if ((node.attr("type"))!=="variant") {        	       	 
        	return { 
        		"show" : {
                    "label" : "Show Route Shapes",
                    "action" : function (node) { 
                    	//alert(node.attr("type"));
                    	switch (node.attr("type")){
                    	case "agency":                    		
                    		if ($.jstree._reference($mylist)._is_loaded(node)){
                    			$.each($.jstree._reference($mylist)._get_children(node), function(i,child){
                    				if ($.jstree._reference($mylist)._is_loaded(child)){
                    					$.each($.jstree._reference($mylist)._get_children(child), function(i,gchild){
            								$.jstree._reference($mylist).change_state(gchild, false);
            								});
                    				}else{
                    					$.jstree._reference($mylist).load_node_json(child, function(){$.each($.jstree._reference($mylist)._get_children(child), function(i,gchild){
            								$.jstree._reference($mylist).change_state(gchild, false);
            								});},function(){alert("Node Load Error");});
                    				}                        			
                                		});
                			} else{
                				$.jstree._reference($mylist).load_node_json(node,function(){$.each($.jstree._reference($mylist)._get_children(node), function(i,child){
                					if ($.jstree._reference($mylist)._is_loaded(child)){
                						$.each($.jstree._reference($mylist)._get_children(child), function(i,gchild){
            								$.jstree._reference($mylist).change_state(gchild, false);
            								});
                					}else{
                						$.jstree._reference($mylist).load_node_json(child, function(){$.each($.jstree._reference($mylist)._get_children(child), function(i,gchild){
            								$.jstree._reference($mylist).change_state(gchild, false);
            								});},function(){alert("Node Load Error");});
                					}                        			
                                		});},function(){alert("Node Load Error");});
                			}                    		                    		
	                    	break; 
                    	case "route":
                    		if ($.jstree._reference($mylist)._is_loaded(node)){
	                    		$.each($.jstree._reference($mylist)._get_children(node), function(i,child){
	                    		$.jstree._reference($mylist).change_state(child, false);
	                    		}); 
                    		}else {
                    			$.jstree._reference($mylist).load_node_json(node, function(){$.each($.jstree._reference($mylist)._get_children(node), function(i,child){
                            		$.jstree._reference($mylist).change_state(child, false);
                            		});},function(){alert("Node Load Error");});
                    		}
                    		break;
                    	}
                    }
                },
                "hide" : {
                    "label" : "Hide Route Shapes",
                    "action" : function (node) { 
                    	switch (node.attr("type")){
                    	case "agency":                    		
                    		$.each($.jstree._reference($mylist)._get_children(node), function(i,child){
                    			$.each($.jstree._reference($mylist)._get_children(child), function(i,gchild){
                            		$.jstree._reference($mylist).uncheck_node(gchild);
                            		});
                        		});
	                    	break; 
                    	case "route":
                    		$.each($.jstree._reference($mylist)._get_children(node), function(i,child){
                    		$.jstree._reference($mylist).uncheck_node(child);
                    		});
                    		break;
                    	}
                    }
                },
        	};        	     
                }            
        }
    },
	"plugins" : [ "themes","types","json_data", "checkbox", "sort", "ui" ,"contextmenu"]			
})
.bind("loaded.jstree", function (event, data) {
	$mylist
	.dialog({ 
		"title" : "Oregon Transportation Agencies", 
		width : 400,
		height: 720,
		maxHeight: 820,
		maxWidth: 600,
		closeOnEscape: false,
		position: [40,2],
		show: {
		 effect: "blind",
		 duration: 1000
		 }		 
		})	
	.dialogExtend({
	  "closable" : false,
	  "expandTitle":"test",
	  "minimizable" : true,
	  "collapsable" : true,
	  "maximizable" : false,
	  "dblclick" : "collapse",
	  "titlebar" : "transparent",
	  "icons" : { 
		  "collapse" : "ui-icon-circle-arrow-n",
	      "restore" : "ui-icon-newwin",
	      "close": "ui-icon-document",	     
	    },
	    //"events" : {
	    "load" : function(evt, dlg) {
	    	
    	$(".ui-dialog-titlebar-buttonpane:eq( 2 )").css("right", 25 + "px");
    	/*$(".ui-dialog-titlebar-maximize").css("right", 80+ "px");*/
	    var titlebar = $(".ui-dialog-titlebar:eq( 2 )");
	    var div = $("<div/>");
	    div.addClass("dropdown");
	    
	    /*	    titlebar.append('<div class=dropdown><button class="dropdown-toggle" role="button" data-toggle="dropdown" text="reports"><img src="/path/to/ui-icon-document" alt="Submit"></button><ul id="menu1" class="dropdown-menu" role="menu" aria-labelledby="drop4"><li role="presentation"><a id="rep1" href="#">Transit Agnecy Summary Report</a></li><li role="presentation"><a id="rep2" href="#">Counties Summary Report</a></li><li role="presentation"><a id="rep2" href="#">ODOT Transit Regions Summary Report</a></li></ul><div>');*/
		/*$('.dropdown-toggle').dropdown();*/
				var button = $( "<button/>" ).text( "Reports" );
        	/*right = titlebar.find( "[role='button']:last" )
                             .css( "right" );*/
	    button.button( { icons: { primary: "ui-icon-document" }, text: false } )
            .addClass( "ui-dialog-titlebar-other" )
            .css( "right", 5 + "px" )
            .click( function( e ) {
                openrep();
            } )
            .appendTo(titlebar);
	      /*$(".ui-dialog-titlebar-minimize").after('<span class="ui-icon ui-icon-plusthick">minus</span>');*/
		  $mylist.dialogExtend("collapse");
		  $("#minimize").attr("title", "Minimize");		  
		  	  
	    },
	    "restore": function(evt,dlg){
	    	$("#collapse").attr("title", "Collapse");
	    },
	    "minimize" : function(evt,dlg){
	    	$("#collapse").attr("title", "Restore");			
	    	//$('<div class="ui-dialog-titlebar-buttonpane"></div>').find('.ui-dialog-titlebar-restore').attr("title", "test");
	    	//$("#b2").attr("title", "test");
	    	//alert('minimized');
	    },
	    iconButtons: [
		                {
		                    text: "Reports",
		                    icon: "ui-icon-document",
		                    click: function( e ) {
		                        $( "#dialog" ).html( "<p>Searching...</p>" );
		                    }
		                 }
		            ]
	    
	   //}
	});        
	//alert("TREE IS LOADED");
	    })
.bind("change_state.jstree", function (e, d) {
    var tagName = d.args[0].tagName;
    var refreshing = d.inst.data.core.refreshing;
    //alert(tagName);
    if ((tagName == "A" || tagName == "INS"|| tagName == "LI")&&(refreshing != true && refreshing != "undefined")) {
    	//alert(d.rslt.attr("id")+" value: "+$("#" + d.rslt.attr("id") + ".jstree-checked").length); 
    	//id = d.rslt.attr("id");
    	//alert(id+','+d.rslt.attr("type"));
    	node = d.rslt;
    	//alert(node.attr("type")=='variant');
    	switch (node.attr("type")){
    	case "agency":    		
    		//mynode = $("#" + node.attr("id") + ".jstree-checked");
    		//alert(mynode.length);
    		//checkbox is checked
    		if ($.jstree._reference($mylist).is_checked(node)){    			
    			//if it has any checked children   			
    			$.jstree._reference($mylist)._get_children(node).each( function( idx, listItem ) {    				
    				if ($.jstree._reference($mylist).is_checked($(listItem))){
	    				//alert($(listItem).attr("id"));
	    				//if (($("#" + $(listItem).attr("id") + ".jstree-checked").length)>0){ 
	    				//alert($(listItem).attr("id"));
	    				//Layers = Layers -1;    				
	    				stops.eachLayer(function (layer) {
	    				if (layer._leaflet_id == "R"+node.attr("id")+ $(listItem).attr("id")){
	    					stops.removeLayer(layer);
	    				}
	    				});
	    				$(listItem).css("background-color","");	    				
	    				//$("#" + $(listItem).attr("id") + ".jstree-checked").css("background-color","");
	    				$.jstree._reference($mylist).uncheck_node($(listItem));
    				};
                 });
    			//alert(d.rslt.attr("id"));
    			//var nodeid = d.rslt.attr("id").split("_")[1];
    			//alert(nodeid);
    			node.css("opacity", "1");
    			node.css("background-color", colorset[Layers%6]);    			
    			getdata(1,node.attr("id"),"","",Layers%6,disponmap,$.jstree._reference($mylist).get_text(node));
    			Layers = Layers + 1;
    			//var marker = L.marker([44.574606,-123.27987]).addTo(stops);
    		} else {    			
    			//mynode = $("#" + node.attr("id") + ".jstree-unchecked");
    			//Layers = Layers -1;
    			node.css("background-color","");
    			if ((($($mylist).jstree("get_checked",node,true)).length)>0) node.css("opacity", "0.6");
    			stops.eachLayer(function (layer) {
    			if (layer._leaflet_id == "A"+node.attr("id")){    					
    				stops.removeLayer(layer);
    				}
    			});	    				
    		}
    		break;
    	case "route":
    		//mynode = $("#" + node.attr("id") + ".jstree-checked");
    		if ($.jstree._reference($mylist).is_checked(node)){
    			//parent= $("#" + d.inst._get_parent(node).attr("id") + ".jstree-checked");
    			rparent = $.jstree._reference($mylist)._get_parent(node);
    			//parent = d.inst._get_parent(node);
    			if($.jstree._reference($mylist).is_checked(rparent)){
    			//if((parent.length)>0){
    				//Layers = Layers -1;
    				stops.eachLayer(function (layer) {
    				if (layer._leaflet_id == "A"+ rparent.attr("id")){
    				stops.removeLayer(layer);
    				}
    				});
    				rparent.css("background-color","");    				
    				$.jstree._reference($mylist).uncheck_node(rparent);
    			}
    			//alert(d.rslt.attr("type")+" : "+d.inst._get_parent(d.rslt).attr("id")+" : "+d.rslt.attr("id"));
    			//var nodeid = d.rslt.attr("id").split("_")[1];
    			//alert(nodeid);
    			//$.jstree._reference($mylist).uncheck_node(d.inst._get_parent(d.rslt));
    			//d.inst._get_parent(d.rslt).uncheck_node();
    			node.css("background-color", colorset[Layers%6]); 
    			//alert(parent.attr("id"));
    			//$("#" + parent.attr("id")).addClass("hasSelections");
    			//parent.addClass("hasSelections");
    			rparent.css("opacity", "0.6");
    			getdata(2,rparent.attr("id"),node.attr("id"),"",Layers%6,disponmap,$.jstree._reference($mylist).get_text(node));
    			Layers = Layers + 1;
    			//var marker = L.marker([44.574606,-123.27987]).addTo(stops);
    		} else {    			
    			//mynode = $("#" + node.attr("id") + ".jstree-unchecked");
    			//Layers = Layers -1;    			
    			//alert($.jstree._reference("#list").get_checked(rparent, ture));
    			if ((($($mylist).jstree("get_checked",rparent,true)).length)==0) rparent.css("opacity", "1");
    			node.css("background-color","");
    			//parent.removeClass("hasSelections");    			
    			stops.eachLayer(function (layer) {
    				if (layer._leaflet_id == "R"+d.inst._get_parent(node).attr("id")+node.attr("id")){
    				stops.removeLayer(layer);
    				}
    			});	    				
    		}
    		break;
    	case "variant":
    		vparent = $.jstree._reference($mylist)._get_parent(node);
    		//alert($.jstree._reference($mylist).is_checked(node));
    		//mynode = $("#" + node.attr("id")+ ".jstree-checked");    		
    		//alert(d.inst.is_checked(mynode));
    		if ($.jstree._reference($mylist).is_checked(node)){
    			//alert(id+','+d.rslt.attr("type"));
    			//alert(d.rslt.attr("id"));
    			//alert(d.rslt.attr("type")+", routeid: "+d.rslt.attr("id")+", variant: "+d.rslt.text()+", agencyid: "+ d.inst._get_parent((d.inst._get_parent(d.rslt))).attr("id"));
    			//var nodeid = d.rslt.attr("id").split("_")[1];
    			//alert(nodeid);
    			node.css("background-color", colorset[Layers%6]);
    			vparent.css("font-weight", "bold");
    			$.jstree._reference($mylist)._get_parent(vparent).css("opacity", "0.6");
    			getdata(3,d.inst._get_parent((d.inst._get_parent(node))).attr("id"),(d.inst._get_parent(node)).attr("id"),node.attr("id"),Layers%6,dispronmap,node.attr("id"));
    			Layers = Layers + 1;
    			//var marker = L.marker([44.574606,-123.27987]).addTo(stops);
    		} else {    			
    			//mynode = $("#" + d.rslt.attr("id") + ".jstree-unchecked");
    			//alert('unchecked'+(d.rslt.text()).replace(/^\s+|\s+$/g, ''));
    			//Layers = Layers -1;
    			node.css("background-color","");
    			if ((($($mylist).jstree("get_checked",vparent,true)).length)==0) {
    				vparent.css("font-weight", "normal");
    				if ((($($mylist).jstree("get_checked",$.jstree._reference($mylist)._get_parent(vparent),true)).length)==0) {
    					$.jstree._reference($mylist)._get_parent(vparent).css("opacity", "1");
    				}
    			}    			
    			routes.eachLayer(function (layer) {
    				if (layer._leaflet_id == "V"+d.inst._get_parent((d.inst._get_parent(node))).attr("id")+(d.inst._get_parent(node)).attr("id")+node.attr("id")){
    				routes.removeLayer(layer);
    				}
    			});	    				
    		}
    		break;
    	}    	
    };
    
});
