var INIT_LOCATION = new L.LatLng(44.141894, -121.783660); 
var AUTO_CENTER_MAP = false;
var ROUTER_ID = "";
//alert (window.screen.availHeight);

var map = new L.Map('map', {	
	minZoom : 6,
	maxZoom : 19,
	//dragging: false,
	// what we really need is a fade transition between old and new tiles without removing the old ones
});

var OSMURL    = "http://{s}.mqcdn.com/tiles/1.0.0/osm/{z}/{x}/{y}.png";
var aerialURL = "http://{s}.mqcdn.com/tiles/1.0.0/sat/{z}/{x}/{y}.png";
var minimalLayer = new L.StamenTileLayer("toner");
//var mapboxAttrib = "Tiles from <a href='http://mapbox.com/about/maps' target='_blank'> Streets</a>";
//var minimalLayer = new L.TileLayer(CloudURL, {maxZoom: 17, attribution: mapboxAttrib});
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
    height: 870,
    width: 420,
    modal: false,
    draggable: false,
    resizable: false,
    closeOnEscape: false,
    position: { my: "right top", at: "right-50 top", of: window },
    //position: [calc(100% - 40), 2],
    buttons: {
      /*"Submit": dialogResultOpen*//*function() {
    	  dialogResults.dialog( "open" );
    	  $('.ui-dialog:eq(1)').css('top','400px');  
      }*///,
      /*Close: function(){
    	  dialog.dialog( "close" );
      }*/
    },
    close: function() {
    	miniMap._restore();
    	map.removeLayer(onMapCluster);
      //form[ 0 ].reset();
    },
    open: function( event, ui ) {
    	miniMap._minimize();
    	//$(".ui-dialog-titlebar-close", ui.dialog || ui).hide();
    	/*$('#dialogLat').html(drawCentroid[0]);
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
    	$('#stopselect option[value="all"]').prop("selected",true);*/
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
/*var dialogResults = $( "#dialogResults" ).dialog({
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
    	  $('.ui-dialog:eq(1)').css('top','400px'); 
      }
  });*/
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
	//event.preventDefault(); 
	//event.stopPropagation();
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
		//alert(layer.getLatLng());
		currentCircleCenter = layer.getLatLng();
		currentCircleCenterTmp= layer.getLatLng();
		drawCentroid[0] = layer.getLatLng().lat;
		drawCentroid[1] = layer.getLatLng().lng;
		currentLats.push((Math.round(drawCentroid[0] * 1000000) / 1000000).toString());
		currentLngs.push((Math.round(drawCentroid[1] * 1000000) / 1000000).toString());
		currentX = layer.getRadius();
		//alert(layer.getRadius());
		area = Math.pow(layer.getRadius()*0.000621371,2)*Math.PI;
	}else{
		area = L.GeometryUtil.geodesicArea(layer.getLatLngs())*0.000000386102;
		drawCentroid = getCentroid(layer.getLatLngs());
		var tmpPoints = layer.getLatLngs();
		for(var ii=0; ii<tmpPoints.length; ii++){
			currentLats.push((Math.round(tmpPoints[ii].lat * 1000000) / 1000000).toString());
			currentLngs.push((Math.round(tmpPoints[ii].lng * 1000000) / 1000000).toString());
		}
		//alert(layer.getLatLngs().length);
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
				//$('#POPbutton').prop('disabled', false);
				//w_qstringd = date;
				//localStorage.setItem(keyName, w_qstringd);
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
			'<p><button type="button" style="width:100%" id="POPbutton" onclick="onMapSubmit()">Generate Report</button></p>'/*+
			'<p><button type="button" style="width:100%" id="streetViewButton" onclick="openStreetView('+drawCentroid[0]+','+drawCentroid[1]+')">Open Stree View</button></p>'*/
	,{closeOnClick:false,draggable:true}).openPopup();
	//map.fitBounds(layer.getBounds().pad(1));
	//dialog.dialog( "open" );
	/*var tmpCenter = new L.FeatureGroup();
	var tmpmarker = L.marker(getCentroid(layer.getLatLngs()));
	tmpCenter.addLayer(tmpmarker);
	map.addLayer(tmpCenter);*/
	//gglMap.setOptions({draggable:true});
});
setDialog();
function circleMove(latlng){
	currentCircleCenterTmp = latlng;
}
function circleResize(radius){
	//var radius = latlng.distanceTo(currentCircleCenter)*0.000621371;
	radius = Math.round(radius*0.0621371)/100;
	$('#circleRadius1').css('visibility','visible');
	$('#circleRadius2').html(radius);
	//console.log(radius);
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
    		//alert(layer.getRadius());
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
	//dialog.dialog( "open" );
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
			url: '/TNAtoolAPI-Webapp/queries/transit/stops?&agency='+agency,
			success: function(d){		
			$.each(d.stops, function(i,stop){        	
				points.push([new L.LatLng(Number(stop.stopLat), Number(stop.stopLon)),stop.stopName]);	
				//points.push([Number(stop.stopLon),Number(stop.stopLat)]); 			
	        });				
			if (points.length!=0) callback("A"+agency,k,points,popup,node);
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
			if (points.length!=0) callback("R"+agency+route,k,points,popup,node);
	    }});
		break;
	case 3:
		//alert(agency+","+ route+","+variant);		
		$.ajax({
			type: 'GET',
			datatype: 'json',
			url: '/TNAtoolAPI-Webapp/queries/transit/shape?&agency='+agency+'&trip='+variant,
			success: function(d){				
			//if (d.points!= null) callback(k,d.points,"V"+agency+route+variant,node);
			if (d.points!= null) callback(k,d,"V"+agency+route+variant);
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
	/*resetStyle(e.target);
	info.update();*/
	var layer = e.target;
    layer.setStyle({              
        fillOpacity: 0.1
    });
    info.update();
}
/*function resetHighlightTracts(e) {
	tractShape.resetStyle(e.target);
	info.updateTracts();
}*/
function highlightFeature(e) {
    var layer = e.target;
    layer.setStyle({              
        fillOpacity: 0
    });
    //layer.bringToFront(); //enable if borders are changed on mouseover
    info.update(layer.feature.properties);    
}

/*function highlightFeatureTracts(e) {
    var layer = e.target;
    layer.setStyle({              
        fillOpacity: 0
    });
    //layer.bringToFront(); //enable if borders are changed on mouseover
    info.updateTracts(layer.feature.properties);    
}*/

function onEachFeature(feature, layer) {
    layer.on({
        mouseover: highlightFeature,
        mouseout: resetHighlight,
        click: zoomToFeature        
    });
}
/*function onEachFeatureTracts(feature, layer) {
    layer.on({
        mouseover: highlightFeatureTracts,
        mouseout: resetHighlightTracts,
        click: zoomToFeature        
    });
}*/
var stops = new L.LayerGroup().addTo(map);
var routes = new L.LayerGroup().addTo(map);

var county = L.geoJson(countyshape, {style: style, onEachFeature: onEachFeature});
var odot = L.geoJson(odotregionshape, {style: style, onEachFeature: onEachFeature}); 
var urban = L.geoJson(urbanshapes, {style: style, onEachFeature: onEachFeature});
var congdist = L.geoJson(congdistshape, {style: style, onEachFeature: onEachFeature});

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

function disponmap(layerid,k,points,popup,node){
	
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
					//$('#'+markerLat+'POPbutton'+markerLng).prop('disabled', false);
					//w_qstringd = date;
					//localStorage.setItem(keyName, w_qstringd);
			    }
			});
			$('#'+markerLat+'POPdatepicker'+markerLng).datepicker( "setDate", new Date());
			var d = new Date();
			currentDate = [pad(d.getMonth()+1), pad(d.getDate()), d.getFullYear()].join('/');
			/*$('#'+markerLat+'POPdatepicker'+markerLng).focusout(function(){
				$( '#'+markerLat+'POPdatepicker'+markerLng).datepicker( "hide" );
			});*/
			$('.leaflet-popup-content-wrapper').css('opacity','0.70');
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
				'<p><button type="button" style="width:100%" id="'+marLat+'streetViewButton" onclick="openStreetView('+p[0].lat+','+p[0].lng+')">Open Stree View</button></p>'
				
				//'<input type="button" value="Submit" style="font-size:90%;width:50px;height:22px" onclick="showPop('+p[0].lat+','+p[0].lng+')">'+
				//'<br><input type="text" id="p'+p[0].lat+p[0].lng+'" style="font-size:90%;width:60px;height:20px" disabled>&nbsp&nbsp'+
				//'<span style="margin-left:50px">Show on map</span>: <input type="checkbox" id="c'+p[0].lat+p[0].lng+'" onchange="triggerShow(this)">'
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
	//currentCircle = L.circle([lat,lng], x*1609.34);
	//currentCircle.addTo(map);
	var that = drawControl._toolbars[L.DrawToolbar.TYPE]._modes.circle.handler;
	that.enable();
	that._startLatLng = [lat,lng];
	that._shape = new L.Circle([lat,lng], x*1609.34, that.options.shapeOptions);
	that._map.addLayer(that._shape);
	//map.fitBounds(that._shape.getBounds());
	//that._map.fire('draw:created', { layer: that, layerType: that.type });
	that._fireCreatedEvent();//L.Draw.Feature.prototype._fireCreatedEvent.call(this, poly);
	that.disable();
	//drawControl._toolbars[L.DrawToolbar.TYPE]._modes.circle.handler.disable();
	//currentDate = $('#'+mlat+'POPdatepicker'+mlng).datepicker( "getDate" );
	//currentCircle.addTo(map);
	onMapSubmit();
}
/////////////////////////////////////////////////////////////////////////////////
/*function triggerShow(e){
	if(e.checked==false){
		map.removeLayer(markersCentroids);
	}else{
		map.addLayer(markersCentroids);
		map.fitBounds(markersCentroids.getBounds());
	}
}*/

/*var markersCentroids = new L.FeatureGroup();
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
	
}*/


///////////////////////////////////////////////////////////////////////////////////////////////////

function dispronmap(k,d,name){	
	var polyline = L.Polyline.fromEncoded(d.points, {	

		weight: 5,
		color: colorset[k],
		smoothFactor: 10,
		//fillColor: colorset[k],
		//color: "#000",
		//weight: 1,
		opacity: .5,
		//fillOpacity: 0.6
		smoothFactor: 1
		});	
		//mylayer.bindpopup(name);		
		polyline.bindPopup('<b>Agency ID:</b> '+d.agency+'<br><b>Trip Name:</b> '+d.headSign);
		polyline._leaflet_id = name;	
		routes.addLayer(polyline);
		$.jstree._reference($mylist).set_type("default", $(node));
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
//var gglMap;
var visible = true;
var ggl = new L.Google();
$('.leaflet-control-zoom').css('margin-top','50px');

L.control.scale({'metric': false, 'position': 'bottomright', 'maxWidth':200}).addTo(map);
var geocoder = L.Control.Geocoder.google('AIzaSyCTlrYCuni4VWJkeJXzf8Ku_cnhX9aBh74');
var searchControl = L.Control.geocoder({
				geocoder: geocoder,
				position:'topright'
			}).addTo(map);
map.on('click', function(){
	if (searchControl._geocodeMarker) {
		searchControl._map.removeLayer(searchControl._geocodeMarker);
	}
});

//alert(map.options.dragging);
osmLayer.on('load', function(e) {
	ggb = false;
    //map.dragging.enable();
});
minimalLayer.on('load', function(e) {
	ggb = false;
	//map.dragging.enable();
});
var mmRecLat = 0;
var mmRecLng = 0;
var miniMap = new L.Control.MiniMap(new L.TileLayer(OSMURL, {subdomains: ["otile1","otile2","otile3","otile4"], minZoom: 5, maxZoom: 5, attribution: osmAttrib}),{position:'bottomright',toggleDisplay:true}).addTo(map);

$('.leaflet-control-scale-line').css({'border':'2px solid grey','line-height':'1.2','margin-left':'0px'});
//$('.leaflet-control-attribution').remove();



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
		"ODOT Regions": odot,
		"Urbanized Areas 50k+": urban,
		"Congressional Districts": congdist		
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
	    	$(".ui-dialog-titlebar-minimize:eq( 1 )").attr("title", "Minimize");
	    	$(".ui-dialog-titlebar-buttonpane:eq( 1 )").css("right", 25 + "px");    	
		    var titlebar = $(".ui-dialog-titlebar:eq( 1 )");
		    var div = $("<div/>");
		    div.addClass("ui-dialog-titlebar-other");	    
		    var button = $( "<button/>" ).text( "Reports" );	    
		    button.attr("data-toggle", "dropdown");	    
		    button.button( { icons: { primary: "ui-icon-document" }, text: false } )	    
	        .addClass( "ui-dialog-titlebar-other" )	       
	        .css( "right", 1 + "px" )
	        .css( "top", 55 + "%" )
	        .appendTo(div);        
		    div.append('<ul id="rmenu" class="dropdown-menu" role="menu" aria-labelledby="drop4"><li role="presentation"><a id="ASR" href="#">Transit Agency Reports</a></li><li role="presentation"><a id="CSR" href="#">Counties Reports</a></li><li role="presentation"><a id="CPSR" href="#">Census Places Reports</a></li><li role="presentation"><a id="CDSR" href="#">Congressional Districts Reports</a></li><li role="presentation"><a id="UASR" href="#">Urban Areas Reports</a></li><li role="presentation"><a id="ORSR" href="#">ODOT Transit Regions Reports</a></li></ul>');
			div.appendTo(titlebar);
		    $('.ui-dialog-titlebar-other').dropdown();	    
			$mylist.dialogExtend("collapse");
			$("#minimize").attr("title", "Minimize");
			$('a').click(function(e){
				//alert('oy');
			    if ($(this).attr('id')=="ASR"){
			    	var qstringx = '0.1';
			    	window.open('/TNAtoolAPI-Webapp/report.html?&x='+qstringx);
			    }else if($(this).attr('id')=="CSR"){
			    	window.open('/TNAtoolAPI-Webapp/GeoCountiesReport.html');	    		
			    }else if($(this).attr('id')=="CPSR"){
			    	window.open('/TNAtoolAPI-Webapp/GeoPlacesReport.html');	    		
			    }else if($(this).attr('id')=="CDSR"){
			    	window.open('/TNAtoolAPI-Webapp/GeoCongDistsReport.html');	    		
			    }else if($(this).attr('id')=="UASR"){
			    	window.open('/TNAtoolAPI-Webapp/GeoUAreasReport.html');	    		
			    }else if($(this).attr('id')=="ORSR"){
			    	window.open('/TNAtoolAPI-Webapp/GeoRegionsReport.html');	    		
			    }
			});

    	//$(".ui-dialog-titlebar-buttonpane:eq( 1 )").css("right", 25 + "px");
    	/*$(".ui-dialog-titlebar-maximize").css("right", 80+ "px");*/
	    //var titlebar = $(".ui-dialog-titlebar:eq( 1 )");
	    	    
	    /*	    titlebar.append('<div class=dropdown><button class="dropdown-toggle" role="button" data-toggle="dropdown" text="reports"><img src="/path/to/ui-icon-document" alt="Submit"></button><ul id="menu1" class="dropdown-menu" role="menu" aria-labelledby="drop4"><li role="presentation"><a id="rep1" href="#">Transit Agnecy Summary Report</a></li><li role="presentation"><a id="rep2" href="#">Counties Summary Report</a></li><li role="presentation"><a id="rep2" href="#">ODOT Transit Regions Summary Report</a></li></ul><div>');*/
		/*$('.dropdown-toggle').dropdown();*/
				//var button = $( "<button/>" ).text( "Reports" );
        	/*right = titlebar.find( "[role='button']:last" )
                             .css( "right" );*/
	    /*button.button( { icons: { primary: "ui-icon-document" }, text: false } )
            .addClass( "ui-dialog-titlebar-other" )
            .css( "right", 5 + "px" )
            .click( function( e ) {
                openrep();
            } )
            .appendTo(titlebar);*/
	      /*$(".ui-dialog-titlebar-minimize").after('<span class="ui-icon ui-icon-plusthick">minus</span>');*/
		  $mylist.dialogExtend("collapse");
		  $("#minimize").attr("title", "Minimize");		  
		  	  
	    },
	    "restore": function(evt,dlg){
	    	$("#collapse").attr("title", "Collapse");	    	
	    	$(".dropdown-menu").css("top", 100+"%" );
	    	$(".dropdown-menu").css("bottom", "auto" );
	    	$('.leaflet-draw-edit-remove').click();
	    	//addShapefile(coorcoor);
	    	drawControl._toolbars[L.DrawToolbar.TYPE]._modes.rectangle.handler.disable();
	    	drawControl._toolbars[L.DrawToolbar.TYPE]._modes.polygon.handler.disable();
	    	drawControl._toolbars[L.DrawToolbar.TYPE]._modes.circle.handler.disable();
	    	drawControl._toolbars[L.EditToolbar.TYPE]._modes.edit.handler.disable();

	    	//L.Draw.Feature._cancelDrawing();
	    	/*$(".dropdown-menu").css("right", "auto");
	    	$(".dropdown-menu").css("left", 0+"px");*/
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
    			//$(node).disabled = true;
    			$.jstree._reference($mylist).set_type("disabled", $(node));    			   			
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
    			getdata(1,node.attr("id"),"","",Layers%6,disponmap,$.jstree._reference($mylist).get_text(node),node);
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
    			$.jstree._reference($mylist).set_type("disabled", $(node));
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
    			getdata(2,rparent.attr("id"),node.attr("id"),"",Layers%6,disponmap,$.jstree._reference($mylist).get_text(node),node);
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
    			$.jstree._reference($mylist).set_type("disabled", $(node));
    			//alert(id+','+d.rslt.attr("type"));
    			//alert(d.rslt.attr("id"));
    			//alert(d.rslt.attr("type")+", routeid: "+d.rslt.attr("id")+", variant: "+d.rslt.text()+", agencyid: "+ d.inst._get_parent((d.inst._get_parent(d.rslt))).attr("id"));
    			//var nodeid = d.rslt.attr("id").split("_")[1];
    			//alert(nodeid);
    			node.css("background-color", colorset[Layers%6]);
    			vparent.css("font-weight", "bold");
    			$.jstree._reference($mylist)._get_parent(vparent).css("opacity", "0.6");
    			getdata(3,d.inst._get_parent((d.inst._get_parent(node))).attr("id"),(d.inst._get_parent(node)).attr("id"),node.attr("id"),Layers%6,dispronmap,node.attr("id"),node);
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
