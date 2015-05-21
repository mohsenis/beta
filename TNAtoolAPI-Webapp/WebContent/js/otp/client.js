var INIT_LOCATION = new L.LatLng(44.141894, -121.783660); 
var dialogheight = Math.round((window.innerHeight)*.9); 
if (dialogheight > 1000){
	dialogheight = 1000;
}
if (dialogheight < 400){
	dialogheight = 400;
}
//alert ("windows height is: "+$(window).height()+" And doc height is: "+$(document).height()+ " Inner height is: "+window.innerHeight);
var map = new L.Map('map', {	
	minZoom : 6,
	maxZoom : 19	
});

var OSMURL    = "http://{s}.mqcdn.com/tiles/1.0.0/osm/{z}/{x}/{y}.png";
var aerialURL = "http://{s}.mqcdn.com/tiles/1.0.0/sat/{z}/{x}/{y}.png";
var minimalLayer = new L.StamenTileLayer("toner");

var osmAttrib = 'Map by &copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'+' | Census & shapes by &copy; <a href="http://www.census.gov">US Census Bureau</a> 2010';
var osmLayer = new L.TileLayer(OSMURL, 
		{subdomains: ["otile1","otile2","otile3","otile4"], maxZoom: 19, attribution: osmAttrib});

var aerialLayer = new L.TileLayer(aerialURL, 
		{subdomains: ["oatile1","oatile2","oatile3","oatile4"], maxZoom: 19, attribution: osmAttrib});

map.addLayer(osmLayer);

///*****************Leaflet Draw******************///
var dialogAgencies = new Array();
var dialogAgenciesId = new Array();
var dialog = $( "#dialog-form" ).dialog({
    autoOpen: false,
    height: dialogheight,
    width: 420,
    modal: false,
    draggable: false,
    resizable: false,
    closeOnEscape: false,
    position: { my: "right top", at: "right-50 top", of: window },    
    buttons: {
        },
    close: function() {
    	miniMap._restore();
    	map.removeLayer(onMapCluster);
      },
    open: function( event, ui ) {
    	miniMap._minimize();    	
    },
  }).dialogExtend({
	  "closable" : true,
      "minimizable" : true,
      "minimizeLocation": "right",
      "minimize" : function() {
    	  miniMap._restore();
      },
      "restore" : function() {
    	  miniMap._minimize();
      }
  });

var drawnItems = new L.FeatureGroup();
map.addLayer(drawnItems);

var drawControl = new L.Control.Draw({
	draw: {
		polyline: false,
		polygon: {
			metric: false,
			allowIntersection: false,
			showArea: false,
			drawError: {
				color: '#b00b00',
				timeout: 1000
			},
			shapeOptions: {
				color: 'blue'
			}
		},
		circle: {
			metric: false,
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
	$('.jstree-checked').each(function() {
		$( this ).children('a').children('.jstree-checkbox').click();
	});
	$mylist.dialogExtend("collapse");
	drawnItems.clearLayers();
	dialog.dialog( "close" );
});

$('.leaflet-draw-edit-remove').click(function(event){
	drawnItems.clearLayers();
	dialog.dialog( "close" );
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
var popX=0;
var currentLayer;
var currentCircleCenter;
var currentCircleCenterTmp;
var currentDate = new Date();
var currentX=0;
var currentLats;
var currentLngs;
function editCancel(){
	$('#circleRadius1').css('visibility','hidden');
	currentCircleCenterTmp=currentCircleCenter;
}
function pad(s) { return (s < 10) ? '0' + s : s; }
map.on('draw:created', function (e) {
	currentLats = new Array();
	currentLngs = new Array();
	type = e.layerType,
	layer = e.layer;
	currentLayer = layer;
	map.fitBounds(layer.getBounds().pad(0.5));
	if (type === 'circle') {		
		currentCircleCenter = layer.getLatLng();
		currentCircleCenterTmp= layer.getLatLng();
		drawCentroid[0] = layer.getLatLng().lat;
		drawCentroid[1] = layer.getLatLng().lng;
		currentLats.push((Math.round(drawCentroid[0] * 1000000) / 1000000).toString());
		currentLngs.push((Math.round(drawCentroid[1] * 1000000) / 1000000).toString());
		currentX = layer.getRadius();		
		area = Math.pow(layer.getRadius()*0.000621371,2)*Math.PI;
	}else{
		area = L.GeometryUtil.geodesicArea(layer.getLatLngs())*0.000000386102;
		drawCentroid = getCentroid(layer.getLatLngs());
		var tmpPoints = layer.getLatLngs();
		for(var ii=0; ii<tmpPoints.length; ii++){
			currentLats.push((Math.round(tmpPoints[ii].lat * 1000000) / 1000000).toString());
			currentLngs.push((Math.round(tmpPoints[ii].lng * 1000000) / 1000000).toString());
		}		
	}
	drawnItems.addLayer(layer);
	
	drawCentroid[0]= (Math.round(drawCentroid[0] * 1000000) / 1000000).toString();
	drawCentroid[1]= (Math.round(drawCentroid[1] * 1000000) / 1000000).toString();
	area = Math.round(area * 100) / 100;
	layer.on('popupopen', function(e) {
		
		$( "#POPdatepicker" ).datepicker({
		    showButtonPanel: true,
			onSelect: function (date) {
				currentDate = date;				
		    }
		});
		$("#POPdatepicker").datepicker( "setDate", new Date());
		var d = new Date();
		currentDate = [pad(d.getMonth()+1), pad(d.getDate()), d.getFullYear()].join('/');
		$('.leaflet-popup-content-wrapper').css('opacity','0.75');
		$('.leaflet-popup-close-button').css({'color':'#9B9A9A','z-index':'1'});
	});
	layer.bindPopup(
			'<p><b>Centroid:</b><br>'+
			'<span style="padding-left:1em">Latitude: <span id="POPlat" style="padding-left:1.5em">'+drawCentroid[0]+'</span></span><br>'+
	    	'<span style="padding-left:1em">Longitude: <span id="POPlon">'+drawCentroid[1]+'</span></span>'+
			'<p><b>Area:</b> <span id="POParea">'+area+'</span> mi<sup>2</sup></p>'+
			'<p><b>Date</b>: <input readonly type="text" class="POPcal" id="POPdatepicker"></p>'+
			'<p><button type="button" style="width:100%" id="POPbutton" onclick="onMapSubmit()">Generate Report</button></p>'
	,{closeOnClick:false,draggable:true}).openPopup();	
});
setDialog();
function circleMove(latlng){
	currentCircleCenterTmp = latlng;
}
function circleResize(radius){	
	radius = Math.round(radius*0.0621371)/100;
	$('#circleRadius1').css('visibility','visible');
	$('#circleRadius2').html(radius);	
}

map.on('draw:editstart', function (e) {
	currentLayer.closePopup();
	dialog.dialog( "close" );
});
map.on('draw:edited', function (e) {
	$('#circleRadius1').css('visibility','hidden');
	
	var layers = e.layers;
    layers.eachLayer(function (layer) {
    	currentLats = new Array();
		currentLngs = new Array();
    	map.fitBounds(layer.getBounds().pad(0.5));
        try{
        	drawCentroid[0] = layer.getLatLng().lat;
    		drawCentroid[1] = layer.getLatLng().lng;
    		currentX = layer.getRadius();    		
    		currentLats.push((Math.round(drawCentroid[0] * 1000000) / 1000000).toString());
    		currentLngs.push((Math.round(drawCentroid[1] * 1000000) / 1000000).toString());
    		area = Math.pow(layer.getRadius()*0.000621371,2)*Math.PI;
    		currentCircleCenter = layer.getLatLng();
    		currentCircleCenterTmp= layer.getLatLng();
        }catch(err){
        	area = L.GeometryUtil.geodesicArea(layer.getLatLngs())*0.000000386102;
    		drawCentroid = getCentroid(layer.getLatLngs());
    		var tmpPoints = layer.getLatLngs();
    		for(var ii=0; ii<tmpPoints.length; ii++){
    			currentLats.push((Math.round(tmpPoints[ii].lat * 1000000) / 1000000).toString());
    			currentLngs.push((Math.round(tmpPoints[ii].lng * 1000000) / 1000000).toString());
    		}
        }
        drawCentroid[0]= (Math.round(drawCentroid[0] * 1000000) / 1000000).toString();
    	drawCentroid[1]= (Math.round(drawCentroid[1] * 1000000) / 1000000).toString();
    	area = Math.round(area * 100) / 100;
    	layer.openPopup();
    	$('#POPlat').html(drawCentroid[0]);
    	$('#POPlon').html(drawCentroid[1]);
    	$('#POParea').html(area);
    	
    });
	
	
	dialog.dialog( "close" );	
}); 
////////*************************************/////////////////////
var Layers = 0;
function getdata(type,agency,route,variant,k,callback,popup,node) {	
	switch (type){
	case 1:
		var points = [];
		$.ajax({
			type: 'GET',
			datatype: 'json',
			url: '/TNAtoolAPI-Webapp/queries/transit/stops?&agency='+agency+"&dbindex="+dbindex,
			success: function(d){		
			$.each(d.stops, function(i,stop){        	
				points.push([new L.LatLng(Number(stop.stopLat), Number(stop.stopLon)),stop.stopName]);							
	        });				
			if (points.length!=0) callback("A"+agency,k,points,popup,node);
	    }});
		break;
	case 2:
		var points = [];
		$.ajax({
			type: 'GET',
			datatype: 'json',
			url: '/TNAtoolAPI-Webapp/queries/transit/stopsbyroute?&agency='+agency+'&route='+route+"&dbindex="+dbindex,
			success: function(d){		
			$.each(d.stops, function(i,stop){        	
				points.push([new L.LatLng(Number(stop.stopLat), Number(stop.stopLon)),stop.stopName]);				 			
	        });				
			if (points.length!=0) callback("R"+agency+route,k,points,popup,node);
	    }});
		break;
	case 3:			
		$.ajax({
			type: 'GET',
			datatype: 'json',
			url: '/TNAtoolAPI-Webapp/queries/transit/shape?&agency='+agency+'&trip='+variant+"&dbindex="+dbindex,
			success: function(d){
			if (d.points!= null) callback(k,d,"V"+agency+route+variant,node);
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

        '<div id="box"><b>Name: </b>' + props.name + ' <br/>' + '<b>Area: </b>'+ props.area + ' mi<sup>2</sup></div>' 
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
	var layer = e.target;
    layer.setStyle({              
        fillOpacity: 0.1
    });
    info.update();
}
function highlightFeature(e) {
    var layer = e.target;
    layer.setStyle({              
        fillOpacity: 0
    });    
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

var county = L.geoJson(countyshape, {style: style, onEachFeature: onEachFeature});
var odot = L.geoJson(odotregionshape, {style: style, onEachFeature: onEachFeature}); 
var urban = L.geoJson(urbanshapes, {style: style, onEachFeature: onEachFeature});
var congdist = L.geoJson(congdistshape, {style: style, onEachFeature: onEachFeature});

var colorset = ["#6ECC39","#FF33FF","#05FAFC","#FE0A0A", "#7A00F5", "#CC6600"];
function disponmap2(layerid,k,points,popup){	
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
	stops.addLayer(mylayer);	
};

function disponmap(layerid,k,points,popup,node){
	
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
		var marLat = (Math.round(marker.getLatLng().lat * 1000000) / 1000000).toString().replace('.','').replace('-','');
		var marLng = (Math.round(marker.getLatLng().lng * 1000000) / 1000000).toString().replace('.','').replace('-','');
		marker.on('popupopen', function(e) {
			dialog.dialog( "close" );
			var markerLat = (Math.round(this.getLatLng().lat * 1000000) / 1000000).toString().replace('.','').replace('-','');
			var markerLng = (Math.round(this.getLatLng().lng * 1000000) / 1000000).toString().replace('.','').replace('-','');
			$( '#'+markerLat+'POPdatepicker'+markerLng).datepicker({
				showButtonPanel: true,
				onSelect: function (date) {
					currentDate = date;
					}
			});
			$('#'+markerLat+'POPdatepicker'+markerLng).datepicker( "setDate", new Date());
			var d = new Date();
			currentDate = [pad(d.getMonth()+1), pad(d.getDate()), d.getFullYear()].join('/');			
			$('.leaflet-popup-content-wrapper').css('opacity','0.80');
			$('.leaflet-popup-close-button').css({'color':'#9B9A9A','z-index':'1'});
		});
		marker.bindPopup(
				'<p><b>'+p[1]+'</b></p>'+
				'<p><b>Location:</b><br>'+
				'<span style="padding-left:1em">Latitude: <span style="padding-left:1.5em">'+p[0].lat+'</span></span><br>'+
		    	'<span style="padding-left:1em">Longitude: <span>'+p[0].lng+'</span></span>'+
				'<p><b>Date:</b> <input readonly type="text" class="POPcal" id="'+marLat+'POPdatepicker'+marLng+'"></p>'+
				'<p><b>Population Search Radius (miles):</b> <input type="text" value="0.1" id="'+marLat+'POPx'+marLng+'" style="width:40px"></p>'+
				'<p><button type="button" id="'+marLat+'POPbutton'+marLng+'" style="width:100%" onclick="onMapBeforeSubmit('+p[0].lat+','+p[0].lng+','+marLat+','+marLng+')">Generate Report</button></p>'+
				'<p><button type="button" style="width:100%" id="'+marLat+'streetViewButton" onclick="openStreetView('+p[0].lat+','+p[0].lng+')">Open Street View</button></p>'				
				,{closeOnClick:false,draggable:true});
		markers.addLayer(marker);
	}
	markers._leaflet_id = layerid;
	stops.addLayer(markers);
	$.jstree._reference($mylist).set_type("default", $(node));
}

function onMapBeforeSubmit(lat,lng,mlat,mlng){
	var x = $('#'+mlat+'POPx'+mlng).val();
	if(isNaN(x)||x<=0){
		alert('Please enter a valid radius');
		return false;
	}
	drawCentroid[0]= lat;
	drawCentroid[1]= lng;	
	area = Math.pow(x,2)*Math.PI;
	drawCentroid[0]= (Math.round(drawCentroid[0] * 1000000) / 1000000).toString();
	drawCentroid[1]= (Math.round(drawCentroid[1] * 1000000) / 1000000).toString();
	area = Math.round(area * 100) / 100;
	var that = drawControl._toolbars[L.DrawToolbar.TYPE]._modes.circle.handler;
	that.enable();
	that._startLatLng = [lat,lng];
	that._shape = new L.Circle([lat,lng], x*1609.34, that.options.shapeOptions);
	that._map.addLayer(that._shape);	
	that._fireCreatedEvent();
	that.disable();	
	
	onMapSubmit();
}


function dispronmap(k,d,name,node){	
	var polyline = L.Polyline.fromEncoded(d.points, {	
		weight: 5,
		color: colorset[k],
		opacity: .5,
		smoothFactor: 9
		});	
		polyline.bindPopup('<b>Agency Name:</b> '+d.agencyName+ '<br><b>Agency ID:</b> '+d.agency+'<br><b>Trip Name:</b> '+d.headSign);
		polyline._leaflet_id = name;	
		routes.addLayer(polyline);
		$.jstree._reference($mylist).set_type("default", $(node));
};
     
var initLocation = INIT_LOCATION;

map.setView(initLocation,8);
var visible = true;
var ggl = new L.Google();
$('.leaflet-control-zoom').css('margin-top','50px');

L.control.scale({'metric': false, 'position': 'bottomright', 'maxWidth':200}).addTo(map);

var searchControl = L.Control.geocoder().addTo(map);
/*new L.Control.GeoSearch({
    provider: new L.GeoSearch.Provider.OpenStreetMap()
}).addTo(map);*/
map.on('click', function(){
	if (searchControl._geocodeMarker) {
		searchControl._map.removeLayer(searchControl._geocodeMarker);
	}
});

osmLayer.on('load', function(e) {
	ggb = false;    
});
minimalLayer.on('load', function(e) {
	ggb = false;	
});
var mmRecLat = 0;
var mmRecLng = 0;
var miniMap = new L.Control.MiniMap(new L.TileLayer(OSMURL, {subdomains: ["otile1","otile2","otile3","otile4"], minZoom: 5, maxZoom: 5, attribution: osmAttrib}),{position:'bottomright',toggleDisplay:true}).addTo(map);

$('.leaflet-control-scale-line').css({'border':'2px solid grey','line-height':'1.2','margin-left':'0px'});

if (document.URL.split("&").length<2){		    	
	history.pushState('data', '', document.URL+'?&dbindex=0');
}
var dbindex = parseInt(document.URL.split("&")[1].substr(document.URL.split("&")[1].indexOf("=")+1));

var menucontent = '<ul id="rmenu1" class="dropdown-menu" role="menu" aria-labelledby="drop4">';
$.ajax({
	type: 'GET',
	datatype: 'json',
	url: '/TNAtoolAPI-Webapp/queries/transit/DBList',
	async: false,
	success: function(d){
		var menusize = 0;
	    $.each(d.DBelement, function(i,item){
	    	menucontent+='<li role="presentation"><a id="DB'+i+'" href="#">'+item+'</a></li>';
	    	menusize++;
	    });
	    menucontent+='</ul>';
	    if (dbindex<0 || dbindex>menusize-1){
	    	dbindex =0;
	    	history.pushState('data', '', document.URL.split("?")[0]+'?&dbindex=0');
	    }
	}			
});
var baseMaps = {
	    "OSM": osmLayer,
	    "Toner": minimalLayer,
	    "Google Aerial":ggl,
	    //"Aerial Photo": aerialLayer
	};
		        
var overlayMaps = {
		"Stops": stops,
		"Routes": routes,
		"Counties": county,
		"ODOT Transit Regions": odot,
		"Urbanized Areas 50k+": urban,
		"Congressional Districts": congdist		
	};

map.addControl(new L.Control.Layers(baseMaps,overlayMaps));
info.addTo(map);
var $mylist = $("#list");
//session related
var username = sessionStorage.getItem("username");
$mylist
.jstree({
	"checkbox": {        
        two_state: true,
        real_checkboxes: false,
        override_ui:false
     },
	"json_data" : {
		"ajax" : {
            "url" : "/TNAtoolAPI-Webapp/queries/transit/menu?dbindex="+dbindex+"&username="+username,
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
		"types" : {
			"default": {	
				"icon" : {
	            	"image" : "js/lib/images/spacer.png"
	            	},
            	"select_node" : false,
            	"check_node" : true, 
                "uncheck_node" : true,                
                "open_node" :true,
                "hover_node" : true
			},
			"disabled" : {
				"icon" : {
	            	"image" : "js/lib/images/loader.png"
	            	},
	            "check_node" : false, 
	            "uncheck_node" : false,
	            "select_node" : false,
	            "open_node" :false,
	            "hover_node" : false
	          }
		}
	},
	"themes": {
        "theme": "default-rtl",
        "url": "js/lib/jstree-v.pre1.0/themes/default-rtl/style.css",
        "dots": false,
        "icons":true
    },
    "contextmenu" : {
        "items" : function (node) {        	
        	if ((node.attr("type"))!=="variant" && $.jstree._reference($mylist)._get_type($(node))!="disabled") {        	       	 
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
                    						//if ($.jstree._reference($mylist).is_checked(gchild)){
                    						$.jstree._reference($mylist).change_state(gchild, true);
                    						//}
                    						if ($(gchild).attr("longest")==1){
                    							$.jstree._reference($mylist).change_state(gchild, false);
                    						}            								
            								});
                    				}else{
                    					$.jstree._reference($mylist).load_node_json(child, function(){$.each($.jstree._reference($mylist)._get_children(child), function(i,gchild){
            								$.jstree._reference($mylist).change_state(gchild, true);
            								if ($(gchild).attr("longest")==1){
                    							$.jstree._reference($mylist).change_state(gchild, false);
                    						}
            								});},function(){alert("Node Load Error");});
                    				}                        			
                                		});
                			} else{
                				$.jstree._reference($mylist).load_node_json(node,function(){$.each($.jstree._reference($mylist)._get_children(node), function(i,child){
                					if ($.jstree._reference($mylist)._is_loaded(child)){
                						$.each($.jstree._reference($mylist)._get_children(child), function(i,gchild){
            								$.jstree._reference($mylist).change_state(gchild, true);
            								if ($(gchild).attr("longest")==1){
                    							$.jstree._reference($mylist).change_state(gchild, false);
                    						}
            								});
                					}else{
                						$.jstree._reference($mylist).load_node_json(child, function(){$.each($.jstree._reference($mylist)._get_children(child), function(i,gchild){
            								$.jstree._reference($mylist).change_state(gchild, true);
            								if ($(gchild).attr("longest")==1){
                    							$.jstree._reference($mylist).change_state(gchild, false);
                    						}
            								});},function(){alert("Node Load Error");});
                					}                        			
                                		});},function(){alert("Node Load Error");});
                			}                    		                    		
	                    	break; 
                    	case "route":
                    		if ($.jstree._reference($mylist)._is_loaded(node)){
	                    		$.each($.jstree._reference($mylist)._get_children(node), function(i,child){
	                    		$.jstree._reference($mylist).change_state(child, true);
	                    		if ($(child).attr("longest")==1){
        							$.jstree._reference($mylist).change_state(child, false);
        						}
	                    		}); 
                    		}else {
                    			$.jstree._reference($mylist).load_node_json(node, function(){$.each($.jstree._reference($mylist)._get_children(node), function(i,child){
                            		$.jstree._reference($mylist).change_state(child, true);
                            		if ($(child).attr("longest")==1){
            							$.jstree._reference($mylist).change_state(child, false);
            						}
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
		"title" : "Oregon Transit Agencies", 
		width : 400,
		height: dialogheight,
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
	    "load" : function(evt, dlg) {  	
	    	$(".ui-dialog-titlebar-minimize:eq( 1 )").attr("title", "Minimize");
	    	$(".ui-dialog-titlebar-buttonpane:eq( 1 )").css("right", 47 + "px");    	
		    var titlebar = $(".ui-dialog-titlebar:eq( 1 )");		
			var div2 = $("<div/>");
		    div2.addClass("ui-dialog-titlebar-other");	    
		    var button2 = $( "<button/>" ).text( "Databases" );	    
		    button2.attr("data-toggle", "dropdown");	    
		    button2.button( { icons: { primary: "ui-icon-gear" }, text: false } )	    
	        .addClass( "ui-dialog-titlebar-other" )	       
	        .css( "right", 22 + "px" )
	        .css( "top", 55 + "%" )
	        .appendTo(div2);
		    div2.append(menucontent);
		    div2.appendTo(titlebar);
		    document.getElementById('DB'+dbindex).innerHTML = '&#9989 '+document.getElementById('DB'+dbindex).innerHTML;
		    var div = $("<div/>");
		    div.addClass("ui-dialog-titlebar-other");	    
		    var button = $( "<button/>" ).text( "Reports" );	    
		    button.attr("data-toggle", "dropdown");	    
		    button.button( { icons: { primary: "ui-icon-document" }, text: false } )	    
	        .addClass( "ui-dialog-titlebar-other" )	       
	        .css( "right", 1 + "px" )
	        .css( "top", 55 + "%" )
	        .appendTo(div);        
		    div.append('<ul id="rmenu" class="dropdown-menu" role="menu" aria-labelledby="drop4"><li role="presentation"><a id="SSR" href="#">Statewide Report</a></li><li role="presentation"><a id="THR" href="#">Transit Hubs Report</a></li><li role="presentation"><a id="ASR" href="#">Transit Agency Reports</a></li><li role="presentation"><a id="CNSR" href="#">Connected Agencies Reports</a></li><li role="presentation"><a id="CSR" href="#">Counties Reports</a></li><li role="presentation"><a id="CPSR" href="#">Census Places Reports</a></li><li role="presentation"><a id="CDSR" href="#">Congressional Districts Reports</a></li><li role="presentation"><a id="UASR" href="#">Urban Areas Reports</a></li><li role="presentation"><a id="ORSR" href="#">ODOT Transit Regions Reports</a></li></ul>');
			div.appendTo(titlebar);
			$('.ui-dialog-titlebar-other').dropdown();			
			$mylist.dialogExtend("collapse");
			$("#minimize").attr("title", "Minimize");
			$('a').click(function(e){				
				var casestring = '';
				if ($(this).attr('id') != undefined) {
				casestring = $(this).attr('id');
				}
				if (casestring=="THR"){
					var d = new Date();
					var qstringx = '0.064';
					var qstringd = [pad(d.getMonth()+1), pad(d.getDate()), d.getFullYear()].join('/');
		    		var keyName = Math.random();
		    		localStorage.setItem(keyName, qstringd);
			    	window.open('/TNAtoolAPI-Webapp/HubSreport.html?&x='+qstringx+'&n='+keyName+'&dbindex='+dbindex);
			    }else if (casestring=="SSR"){			    	
			    	window.open('/TNAtoolAPI-Webapp/StateSreport.html?&dbindex='+dbindex);
			    }else if (casestring=="ASR"){
			    	var qstringx = '0.1';
			    	window.open('/TNAtoolAPI-Webapp/report.html?&x='+qstringx+'&dbindex='+dbindex);
			    }else if (casestring=="CNSR"){
			    	var qstringx = '500';
			    	window.open('/TNAtoolAPI-Webapp/ConNetSReport.html?&gap='+qstringx+'&dbindex='+dbindex);
			    }else if(casestring=="CSR"){
			    	window.open('/TNAtoolAPI-Webapp/GeoCountiesReport.html'+'?&dbindex='+dbindex);	    		
			    }else if(casestring=="CPSR"){
			    	window.open('/TNAtoolAPI-Webapp/GeoPlacesReport.html'+'?&dbindex='+dbindex);	    		
			    }else if(casestring=="CDSR"){
			    	window.open('/TNAtoolAPI-Webapp/GeoCongDistsReport.html'+'?&dbindex='+dbindex);	    		
			    }else if(casestring=="UASR"){
			    	window.open('/TNAtoolAPI-Webapp/GeoUAreasRReport.html'+'?&pop=50000'+'&dbindex='+dbindex);	    		
			    }else if(casestring=="ORSR"){
			    	window.open('/TNAtoolAPI-Webapp/GeoRegionsReport.html'+'?&dbindex='+dbindex);	    		
			    }else if(casestring.substring(0,2)=="DB"){
			    	if (dbindex!=parseInt(casestring.substring(2)))
			    		location.replace(document.URL.split("?")[0]+'?&dbindex='+parseInt(casestring.substring(2)));			    		    		
			    }				
			});
    	
		  $mylist.dialogExtend("collapse");
		  $("#minimize").attr("title", "Minimize");		  
		  	  
	    },
	    "restore": function(evt,dlg){
	    	$("#collapse").attr("title", "Collapse");	    	
	    	$(".dropdown-menu").css("top", 100+"%" );
	    	$(".dropdown-menu").css("bottom", "auto" );
	    	$('.leaflet-draw-edit-remove').click();	    	
	    	drawControl._toolbars[L.DrawToolbar.TYPE]._modes.rectangle.handler.disable();
	    	drawControl._toolbars[L.DrawToolbar.TYPE]._modes.polygon.handler.disable();
	    	drawControl._toolbars[L.DrawToolbar.TYPE]._modes.circle.handler.disable();
	    	drawControl._toolbars[L.EditToolbar.TYPE]._modes.edit.handler.disable();
	    },
	    "collapse" : function(evt,dlg){	    	
	    	$(".dropdown-menu").css("top", 100+"%" );
	    	$(".dropdown-menu").css("bottom", "auto" );
	    	$(".ui-dialog-titlebar-collapse:eq( 1 )").attr("title", "Collapse");
	    },	    
	    "minimize" : function(evt,dlg){
	    	$(".ui-dialog-titlebar-collapse:eq( 1 )").attr("title", "Restore");
	    	$(".ui-dialog-titlebar-restore:eq( 1 )").attr("title", "Maximize");
	    	$(".dropdown-menu").css("top", "auto" );
	    	$(".dropdown-menu").css("bottom", 100+"%" );	    	
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
	});        
	    })
.bind("change_state.jstree", function (e, d) {
    var tagName = d.args[0].tagName;
    var refreshing = d.inst.data.core.refreshing;
    //alert(tagName);
    if ((tagName == "A" || tagName == "INS"|| tagName == "LI")&&(refreshing != true && refreshing != "undefined")) {    	
    	node = d.rslt;    	
    	switch (node.attr("type")){
    	case "agency":   		
    		//checkbox is checked
    		if ($.jstree._reference($mylist).is_checked(node)){
    			//$(node).disabled = true;
    			$.jstree._reference($mylist).set_type("disabled", $(node));    			   			
    			$.jstree._reference($mylist)._get_children(node).each( function( idx, listItem ) {    				
    				if ($.jstree._reference($mylist).is_checked($(listItem))){
	    				stops.eachLayer(function (layer) {
	    				if (layer._leaflet_id == "R"+node.attr("id")+ $(listItem).attr("id")){
	    					stops.removeLayer(layer);
	    				}
	    				});
	    				$(listItem).css("background-color","");	    				
	    				$.jstree._reference($mylist).uncheck_node($(listItem));
    				};
                 });    			
    			node.css("opacity", "1");
    			node.css("background-color", colorset[Layers%6]);    			
    			getdata(1,node.attr("id"),"","",Layers%6,disponmap,$.jstree._reference($mylist).get_text(node),node);
    			Layers = Layers + 1;    			
    		} else {    			
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
    		if ($.jstree._reference($mylist).is_checked(node)){
    			$.jstree._reference($mylist).set_type("disabled", $(node));    			
    			rparent = $.jstree._reference($mylist)._get_parent(node);    			
    			if($.jstree._reference($mylist).is_checked(rparent)){    			
    				stops.eachLayer(function (layer) {
    				if (layer._leaflet_id == "A"+ rparent.attr("id")){
    				stops.removeLayer(layer);
    				}
    				});
    				rparent.css("background-color","");    				
    				$.jstree._reference($mylist).uncheck_node(rparent);
    			}
    			node.css("background-color", colorset[Layers%6]); 
    			rparent.css("opacity", "0.6");
    			getdata(2,rparent.attr("id"),node.attr("id"),"",Layers%6,disponmap,$.jstree._reference($mylist).get_text(node),node);
    			Layers = Layers + 1;    			
    		} else {    			
    			if ((($($mylist).jstree("get_checked",rparent,true)).length)==0) rparent.css("opacity", "1");
    			node.css("background-color","");    			    			
    			stops.eachLayer(function (layer) {
    				if (layer._leaflet_id == "R"+d.inst._get_parent(node).attr("id")+node.attr("id")){
    				stops.removeLayer(layer);
    				}
    			});	    				
    		}
    		break;
    	case "variant":
    		vparent = $.jstree._reference($mylist)._get_parent(node);    		
    		if ($.jstree._reference($mylist).is_checked(node)){
    			$.jstree._reference($mylist).set_type("disabled", $(node));    			
    			node.css("background-color", colorset[Layers%6]);
    			vparent.css("font-weight", "bold");
    			$.jstree._reference($mylist)._get_parent(vparent).css("opacity", "0.6");
    			getdata(3,d.inst._get_parent((d.inst._get_parent(node))).attr("id"),(d.inst._get_parent(node)).attr("id"),node.attr("id"),Layers%6,dispronmap,node.attr("id"),node);
    			Layers = Layers + 1;    			
    		} else {    			
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
