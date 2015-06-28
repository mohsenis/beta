//var coorcoor = [{lat: 44.054395, lng: -123.088453}, {lat: 44.63030637, lng: -123.10329518}, {lat: 45.517039, lng: -122.679887}];
//var coorcoor = [L.latLng(44.054395, -123.088453),L.latLng(44.63030637, -123.10329518),L.latLng(45.517039, -122.679887)];
function addShapefile(coords){
	var that = drawControl._toolbars[L.DrawToolbar.TYPE]._modes.polygon.handler;
	that.enable();
	
	for(var i=0;i<coords.length;i++){
		that.addVertex(coords[i]);
	}
	that._finishShape();
	that.disable();
}
var ggm=false;
var ggb;
function openStreetView(lat, lon){
	if(!ggb){
		alert('You need to be in Google Aerial map layer to use this feature');
		return;
	}
	var place = new google.maps.LatLng(lat, lon);
	panorama = ggm.getStreetView();
	panorama.setPosition(place);
	map.dragging.disable();
	map.touchZoom.disable();
	map.doubleClickZoom.disable();
	map.scrollWheelZoom.disable();	
	google.maps.event.addListener(panorama, 'closeclick', function() {
		map.dragging.enable();
		map.touchZoom.enable();
		map.doubleClickZoom.enable();
		map.scrollWheelZoom.enable();
	});
	google.maps.event.addListener(panorama, 'visible_changed', function() {
		if(panorama.getVisible()){
			$('div.gm-style:nth-child(2)').css('z-index','10000');
			$('#map > div.leaflet-map-pane').css('position','relative');
			$('#map > div.leaflet-control-container').children().css('position','relative');
			$('body > div.ui-dialog.ui-widget.ui-widget-content.ui-corner-all.ui-front.ui-draggable.ui-resizable').css('display','none');
		}else{
			$('#map > div.leaflet-map-pane').css('position','absolute');
			$('#map > div.leaflet-control-container').children().css('position','absolute');
			$('body > div.ui-dialog.ui-widget.ui-widget-content.ui-corner-all.ui-front.ui-draggable.ui-resizable').css('display','');
		}
	});
	panorama.setVisible(true);
	checkSVChange();	
}

function checkSVChange()
{
	if($('div.gm-style:nth-child(2)').css('z-index')=='10000'){
		return;
	}
	$('div.gm-style:nth-child(2)').css('z-index','10000');
    setTimeout( checkSVChange, 100 );
}

function onMapSubmit(){
	map.removeLayer(onMapCluster);
	$( '.POPcal').datepicker( "hide" );
	currentLayer.closePopup();
	if(!dialog.dialog( "isOpen" )){
		dialog.dialog( "open" );
	}
	$('#dialogLat').html(drawCentroid[0]);
	$('#dialogLng').html(drawCentroid[1]);
	$('#dialogArea').html(Math.round(area*100)/100);
	$('#popRadio').prop('checked', true);
	
	var index = $('#tabs a[href="#transit"]').parent().index();
	$("#tabs").tabs("option", "active", index);
	$('#blocksCheck').prop('checked', true);
	//$('#blocksCheck').prop('checked', true);
	$("#dialogDate").datepicker( "setDate", currentDate);
	$("#tabs").hide();
	$('#dialogPreLoader').show();
	//alert(currentDate);
	showOnMapReport(currentLats, currentLngs, currentDate, currentX);
}

function setDialog(){
	$( "#dialogDate" ).datepicker({
		duration: "fast",
		showButtonPanel: true,
		onSelect: function (date) {
			
			currentDate = date;
			onMapSubmit();
	    }
	});
    $( "#tabs" ).tabs();
}
var onMapCluster=new L.FeatureGroup();
var onMapStopCluster=new L.FeatureGroup();
var onMapRouteCluster=new L.FeatureGroup();
var onMapBlockCluster=new L.FeatureGroup();
var onMapTractCluster=new L.FeatureGroup();
var onMapPnrCluster = new L.FeatureGroup();
var onMapPnrStopCluster = new L.FeatureGroup();
var onMapPnrRouteCluster = new L.FeatureGroup();
var stopCluster;
var routeCluster;
var blockCluster;
var tractCluster;
var pnrCluster;
var pnrStopCluster;
//var pnrRouteCluster;

function transitRadio(r){
	//alert(r.value);
	onMapCluster.removeLayer(onMapStopCluster);
	onMapCluster.removeLayer(onMapRouteCluster);
	if(r.value=='stops'){
		onMapCluster.addLayer(onMapStopCluster);
	}else if(r.value=='routes'){
		onMapCluster.addLayer(onMapRouteCluster);
	}else{
		onMapCluster.addLayer(onMapRouteCluster);
		onMapCluster.addLayer(onMapStopCluster);
	}
}

function geoRadio(r){
	//alert(r.value);
	onMapCluster.removeLayer(onMapBlockCluster);
	onMapCluster.removeLayer(onMapTractCluster);
	if(r.value=='blocks'){
		onMapCluster.addLayer(onMapBlockCluster);
	}else if(r.value=='tracts'){
		onMapCluster.addLayer(onMapTractCluster);
	}else{
		onMapCluster.addLayer(onMapBlockCluster);
		onMapCluster.addLayer(onMapTractCluster);
	}
}
function doNotDelete(){
    //DONT DELETE
};

function showOnMapReport(lat, lon, date, x){
	lat = lat.join(",");
	lon = lon.join(",");
	var key =1;
	var d0;
	var d1;
	var d;
	var points;
	onMapCluster = new L.FeatureGroup();
	onMapStopCluster=new L.FeatureGroup();
	onMapRouteCluster=new L.FeatureGroup();
	onMapBlockCluster=new L.FeatureGroup();
	onMapTractCluster=new L.FeatureGroup();
	onMapPnrCluster = new L.FeatureGroup();
	onMapPnrStopCluster = new L.FeatureGroup();
	onMapPnrRouteCluster = new L.FeatureGroup();
	onMapCluster.addLayer(onMapStopCluster);
	onMapCluster.addLayer(onMapRouteCluster);
	onMapCluster.addLayer(onMapBlockCluster);
	onMapCluster.addLayer(onMapPnrCluster);
	onMapCluster.addLayer(onMapPnrStopCluster);
	onMapCluster.addLayer(onMapPnrRouteCluster);
	onMapCluster.addLayer(onMapTractCluster);
	map.addLayer(onMapCluster);
	stopCluster = new Array();
	routeCluster = new Array();
	blockCluster = new Array();
	tractCluster = new Array();
	pnrCluster = new Array();
	pnrStopCluster = new Array();
	pnrRouteCluster = new Array();
	
	var colorArray=['gcluster', 'picluster', 'ccluster', 'rcluster', 'pucluster', 'brcluster'];
	var GcolorArray=['blockscluster', 'tractscluster'];
	$('#displayTransitReport').empty();
	$('#displayGeoReport').empty();
	$('#displayPnrCounties').empty();
	$("#overlay").show();	
	$.ajax({
		type: 'GET',
		datatype: 'json',
		url: '/TNAtoolAPI-Webapp/queries/transit/onmapreport?&lat='+lat+'&lon='+lon+'&x='+x+'&day='+date+'&dbindex='+dbindex,
		async: true,
		success: function(data){
			$('#ts').html(numberWithCommas(data.MapTr.TotalStops));
			$('#tr').html(numberWithCommas(data.MapTr.TotalRoutes));
			$('#af').html('$'+Math.round(data.MapTr.AverageFare*100)/100);
			$('#mff').html('$'+data.MapTr.MedianFare);
			var html = '<table id="transitTable" class="display" align="center">';
			var tmp = '<th>Row</th>'+
			'<th>Agency Name</th>'+
			'<th>Routes</th>'+
			'<th>Stops</th>'+
			'<th>Service Stops</th></tr>';	
			html += '<thead>'+tmp+'</thead><tbody>';
			var html2 = '<tfoot>'+tmp+'</tfoot>';
			var counter=1;
			$.each(data.MapTr.MapAgencies, function(i,item){
				html += '<td>'+counter++ +'</td>'+
						'<td>'+item.Name+'</td>'+
						'<td>'+numberWithCommas(item.RoutesCount)+'</td>'+
						'<td>'+numberWithCommas(item.MapStops.length)+'</td>'+
						'<td>'+numberWithCommas(item.ServiceStop)+'</td></tr>';
				//var tmpStopCluster = new L.FeatureGroup();
				var tmpRouteCluster = new L.FeatureGroup();
				
				var c = i % 6;
				var tmpStopCluster = new L.MarkerClusterGroup({
					maxClusterRadius: 120,
					iconCreateFunction: function (cluster) {
						return new L.DivIcon({ html: cluster.getChildCount(), className: colorArray[c], iconSize: new L.Point(30, 30) });
					},
					spiderfyOnMaxZoom: true, showCoverageOnHover: true, zoomToBoundsOnClick: true, singleMarkerMode: true, maxClusterRadius: 30
				});
				$.each(item.MapStops, function(j,jtem){
					
					var marker = L.marker([jtem.Lat,jtem.Lng]/*, {icon: onMapIcon}*/);
					pophtml='<br><b>Serving Routes ID(s):</b>';
					$.each(jtem.RouteIds, function(h,htem){
						pophtml+='<br><span style="margin-left:2em">'+htem+'</span>';
					});
					marker.bindPopup('<b>Stop ID:</b> '+jtem.Id+'<br><b>Stop Name:</b> '+jtem.Name+'<br><b>Agency:</b> '+jtem.AgencyId+'<br><b>Service Frequency :</b> '+jtem.Frequency+pophtml);
					tmpStopCluster.addLayer(marker);
				});
				stopCluster.push(tmpStopCluster);
				$.each(item.MapRoutes, function(k,ktem){
					if(ktem.hasDirection){
						d0 = L.PolylineUtil.decode(ktem.Shape0);
						d1 = L.PolylineUtil.decode(ktem.Shape1);
						points = [d0, d1];
					}else{
						d = L.PolylineUtil.decode(ktem.Shape);
						points = [d];
					}
					var polyline = L.multiPolyline(points, {	
						weight: 5,
						color: colorset[c],
						//fillColor: colorset[k],
						//color: "#000",
						//weight: 1,
						opacity: .5,
						//fillOpacity: 0.6
						smoothFactor: 1
						});	
					polyline.bindPopup('<b>Route ID:</b> '+ktem.Id+'<br><b>Route Name:</b> '+ktem.Name+'<br><b>Agency:</b> '+ktem.AgencyId+'<br><b>Length:</b> '+numberWithCommas(Math.round(ktem.Length*100)/100)+' miles<br><b>Average Route Fare: </b> '+ktem.Fare+'<br><b>Run Frequency:</b> '+ktem.Frequency);
					tmpRouteCluster.addLayer(polyline);
				});
				routeCluster.push(tmpRouteCluster);
			});		
			html = html + '</tbody></table>';
			//html = html + '</tbody>'+html2+'</table>';
			$('#displayTransitReport').append($(html));
			var transitTable = $('#transitTable').DataTable( {
				"paging": false,
				"bSort": true,
				//"scrollY": "40%",
				"dom": 'T<"clear">lfrtip',
		        "tableTools": {
		        	"sSwfPath": "js/lib/DataTables/swf/copy_csv_xls_pdf.swf",
		        	"sRowSelect": "multi",
		        	"aButtons": [],
		        	"columns":[{ "width": "50%" },
		        	           { "width": "50%" },
		        	           { "width": "50%" },
		        	           { "width": "50%" },
		        	           { "width": "50%" }
		        	           ]
			}
			});
			$("#transitTable_length").remove();
		    $("#transitTable_filter").remove();
		    $("#transitTable_info").remove();
		    transitTable.$('tr').click( function () {
		        // data = oTable.fnGetData( this );
		    	if($(this).hasClass('selected')){
		    		
		    		onMapStopCluster.removeLayer(stopCluster[$(this).children().eq(0).html()-1]);
		    		onMapRouteCluster.removeLayer(routeCluster[$(this).children().eq(0).html()-1]);
		    	}else{
		    		
		    		onMapStopCluster.addLayer(stopCluster[$(this).children().eq(0).html()-1]);
		    		onMapRouteCluster.addLayer(routeCluster[$(this).children().eq(0).html()-1]);
		    	}
		    });
		    
			$('#tpu').html(numberWithCommas(data.MapG.UrbanPopulation));
			$('#tpr').html(numberWithCommas(data.MapG.RuralPopulation));
			$('#tb').html(numberWithCommas(data.MapG.TotalBlocks));
			$('#tt').html(numberWithCommas(data.MapG.TotalTracts));					
			html = '<table id="geoTable" class="display" align="center">';
			tmp = '<th>Row</th>'+
			'<th>County Name</th>'+
			'<th>Tracts</th>'+
			'<th>Blocks</th>'+
			'<th>Urban Pop. (2010)</th>'+
			'<th>Rural Pop. (2010)</th></tr>';	
			html += '<thead>'+tmp+'</thead><tbody>';
			var counter=1;
			$.each(data.MapG.MapCounties, function(i,item){
				html += '<td>'+ counter++ +'</td>'+
						'<td>'+item.Name.replace(' County','')+'</td>'+
						'<td>'+numberWithCommas(item.MapTracts.length)+'</td>'+
						'<td>'+numberWithCommas(item.MapBlocks.length)+'</td>'+
						'<td>'+numberWithCommas(item.UrbanPopulation)+'</td>'+
						'<td>'+numberWithCommas(item.RuralPopulation)+'</td></tr>';				
				onMapIcon = L.icon({
				    iconUrl: 'js/lib/leaflet-0.7/images/block.png',
				    iconAnchor:   [15, 29], // point of the icon which will correspond to marker's location
				    popupAnchor:  [0, -20] // point from which the popup should open relative to the iconAnchor
				});
				//var tmpBlockCluster = new L.MarkerClusterGroup();
				var tmpBlockCluster = new L.MarkerClusterGroup({
					/*maxClusterRadius: 120,*/
					iconCreateFunction: function (cluster) {
						return new L.DivIcon({ html: cluster.getChildCount(), className: GcolorArray[0], iconSize: new L.Point(30, 30) });						
					},
					spiderfyOnMaxZoom: true, showCoverageOnHover: false, zoomToBoundsOnClick: true, singleMarkerMode: true, maxClusterRadius: 30
				});
				var tmpTractCluster = new L.MarkerClusterGroup({
					maxClusterRadius: 80,
					iconCreateFunction: function (cluster) {
						return new L.DivIcon({ html: cluster.getChildCount(), className: GcolorArray[1], iconSize: new L.Point(30, 30) });						
					},
					spiderfyOnMaxZoom: true, showCoverageOnHover: false, zoomToBoundsOnClick: true, singleMarkerMode: true
				});
				$.each(item.MapBlocks, function(j,jtem){						
						var marker = L.marker([jtem.Lat,jtem.Lng]/*, {icon: onMapIcon}*/);
						marker.bindPopup('<b>Block ID:</b> '+jtem.ID+'<br><b>Type:</b> '+jtem.Type+'<br><b>Population:</b> '+numberWithCommas(jtem.Population)+'<br><b>County:</b> '+jtem.County+'<br><b>Land Area:</b> '+ numberWithCommas(Math.round(parseFloat(jtem.LandArea)*0.0000386102)/100)+' mi<sup>2</sup>');
						tmpBlockCluster.addLayer(marker);								
					/*var blocmarker = L.marker([jtem.Lat,jtem.Lng], {icon: onMapIcon});
					blocmarker.bindPopup('<b>Block ID:</b> '+jtem.ID+'<br><b>Type:</b> '+jtem.Type+'<br><b>Population:</b> '+numberWithCommas(jtem.Population)+'<br><b>County:</b> '+jtem.County+'<br><b>Land Area:</b> '+ numberWithCommas(Math.round(parseFloat(jtem.LandArea)*0.0000386102)/100)+' mi<sup>2</sup>');
					tmpBlockCluster.addLayer(blocmarker);*/
				});
				blockCluster.push(tmpBlockCluster);
				onMapIcon = L.icon({
				    iconUrl: 'js/lib/leaflet-0.7/images/tract.png',
				    //iconSize:     [40, 55], // size of the icon
				    iconAnchor:   [20, 39], // point of the icon which will correspond to marker's location
				    popupAnchor:  [0, -30] // point from which the popup should open relative to the iconAnchor
				});
				$.each(item.MapTracts, function(k,ktem){
					var tractmarker = L.marker([ktem.Lat,ktem.Lng]/*, {icon: onMapIcon}*/);
					tractmarker.bindPopup('<b>Tract ID:</b> '+ktem.ID+'<br><b>Population:</b> '+numberWithCommas(ktem.Population)+'<br><b>County:</b> '+ktem.County+'<br><b>Land Area:</b> '+ numberWithCommas(Math.round(parseFloat(ktem.LandArea)*0.0000386102)/100)+' mi<sup>2</sup>');
					tmpTractCluster.addLayer(tractmarker);
				});
				tractCluster.push(tmpTractCluster);
			});		
			html = html + '</tbody></table>';
			$('#displayGeoReport').append($(html));
			var geoTable = $('#geoTable').DataTable( {
				"paging": false,
				"bSort": true,
				"bAutoWidth": false,
				//"scrollY": "40%",
				"dom": 'T<"clear">lfrtip',
				"tableTools":{
		        	"sSwfPath": "js/lib/DataTables/swf/copy_csv_xls_pdf.swf",
		        	"sRowSelect": "multi",
		        	"aButtons": [],
		        	"columns": [
		        	            { "width": "10%"},
		        	            null,
		        	            null,
		        	            null,
		        	            null
		        	          ]}
			});
			$("#geoTable_length").remove();
		    $("#geoTable_filter").remove();
		    $("#geoTable_info").remove();
		    geoTable.$('tr').click( function () {
		        // data = oTable.fnGetData( this );
		    	if($(this).hasClass('selected')){		    		
		    		onMapBlockCluster.removeLayer(blockCluster[$(this).children().eq(0).html()-1]);
		    		onMapTractCluster.removeLayer(tractCluster[$(this).children().eq(0).html()-1]);
		    	}else{		    		
		    		onMapBlockCluster.addLayer(blockCluster[$(this).children().eq(0).html()-1]);
		    		onMapTractCluster.addLayer(tractCluster[$(this).children().eq(0).html()]-1);
		    	}
		    });
		    
		    //Beginning point of the Park n Ride table
		    $('#npnr').html(numberWithCommas(data.MapPnR.totalPnR));
			$('#nspc').html(numberWithCommas(data.MapPnR.totalSpaces));
			var html = 	'<table id="pnrTable" class="display" align="center">';
			var tmp = 	'<tr><th>Row</th>'+
						'<th>County</th>'+
						'<th>P&R Lots</th>'+
						'<th>Spaces</th></tr>';
			
			html += '<thead>'+tmp+'</thead><tbody>';
			var html2 = '<tfoot>'+tmp+'</tfoot>';
			var counter=1;
			$.each(data.MapPnR.MapPnrCounty, function(i,item){
				html += '<tr><td>'+(counter++)+'</td>'+
						'<td>'+item.countyName+'</td>'+
						'<td>'+item.totalPnRs+'</td>'+
						'<td>'+numberWithCommas(item.totalSpaces)+'</td></tr>';
				
				var tmpPnrCluster = new L.FeatureGroup();
	
				onMapIcon = L.icon({
				    iconUrl: 'js/lib/leaflet-0.7/images/pnr.ico',
				    iconSize:     [40, 40], // size of the icon
				    iconAnchor:   [20, 39], // point of the icon which will correspond to marker's location
				    popupAnchor:  [0, -36] // point from which the popup should open relative to the iconAnchor
				});
				var cntr=1;
				$.each(item.MapPnrRecords, function(j,jtem){
					var marker = L.marker([jtem.lat,jtem.lon], {icon: onMapIcon}).on('click',onClick);
					var temp='<b>County Name:</b> '+jtem.countyName+
							'<br><b>Lot Name:</b> '+jtem.lotName+
							'<br><b>Transit Services:</b> '+jtem.transitSerives+
							'<br><b>Total Spaces: </b> '+jtem.spaces;
					marker.bindPopup(temp);
					marker.id = jtem.id;
					tmpPnrCluster.addLayer(marker);
					
					// Begining of new routes and stops markers
					var tmpPnrRouteCluster = new L.FeatureGroup();
					var c = i % 6;
					var tmpPnrStopCluster = new L.MarkerClusterGroup({
						maxClusterRadius: 120,
						iconCreateFunction: function (cluster) {
							return new L.DivIcon({ html: cluster.getChildCount(), className: colorArray[c], iconSize: new L.Point(30, 30) });
						},
						spiderfyOnMaxZoom: true, showCoverageOnHover: true, zoomToBoundsOnClick: true, singleMarkerMode: true, maxClusterRadius: 30
					});
					$.each(jtem.MapPnrSL, function(k,ktem){
							var marker = L.marker([ktem.Lat,ktem.Lng]);
//							pophtml2='<br><b>Serving Routes ID(s):</b>';
//							$.each(ltem.RouteIds, function(m,mtem){
//								pophtml2+='<br><span style="margin-left:2em">'+mtem+'</span>';
//							});
							marker.bindPopup('<b>Stop ID:</b> '+ktem.Id+'<br><b>Stop Name:</b> '+ktem.Name+'<br><b>Agency:</b> '+ktem.AgencyId+'<br><b>Service Frequency :</b> '+ktem.Frequency/*+pophtml2*/);
							tmpPnrStopCluster.addLayer(marker);
						});	
					pnrStopCluster[jtem.id] = tmpPnrStopCluster;
					
					$.each(jtem.MapPnrRL, function(k,ktem){
						d = L.PolylineUtil.decode(ktem.Shape);
						points = [d];
						var polyline = L.multiPolyline(points, {	
							weight: 5,
							color: colorset[k],
							//fillColor: colorset[k],
							//color: "#000",
							//weight: 1,
							opacity: .5,
							//fillOpacity: 0.6
							smoothFactor: 1
							});	
						polyline.bindPopup('<b>Route ID:</b> '+ktem.Id+'<br><b>Route Name:</b> '+ktem.Name+'<br><b>Agency:</b> '+ktem.AgencyId);
						tmpPnrRouteCluster.addLayer(polyline);
					});
					pnrRouteCluster[jtem.id] = tmpPnrRouteCluster;
				});	
			pnrCluster.push(tmpPnrCluster);
			});
				
			function onClick(){
				onMapPnrStopCluster.eachLayer(function (layer) {
					onMapPnrStopCluster.removeLayer(layer);
				});
				onMapPnrStopCluster.addLayer(pnrStopCluster[this.id]);
				onMapPnrRouteCluster.eachLayer(function (layer) {
					onMapPnrRouteCluster.removeLayer(layer);
				});
				onMapPnrRouteCluster.addLayer(pnrRouteCluster[this.id]);
			}
			
			html = html + '</tbody></table>';
			$('#displayPnrCounties').append($(html));
			var pnrTable = $('#pnrTable').DataTable( {
				"paging": false,
				"bSort": true,
				//"scrollY": "40%",
				"dom": 'T<"clear">lfrtip',
		        "tableTools": {
		        	"sSwfPath": "js/lib/DataTables/swf/copy_csv_xls_pdf.swf",
		        	"sRowSelect": "multi",
		        	"aButtons": []},
		        	/*"columns": [
		        	            {"bVisible":false},
		        	            null,
		        	            null,
		        	            null
		        	          ]*/
			});		
			
			$("#pnrTable_length").remove();
		    $("#pnrTable_filter").remove();
		    $("#pnrTable_info").remove();
		    
		    
		    pnrTable.$('tr').click( function () {
		    	if($(this).hasClass('selected')){
		    		onMapPnrCluster.removeLayer(pnrCluster[$(this).children().eq(0).html()-1]);
		    	}else{
//		    		alert($(hits).find( ":hidden" ).not( "script" ).html);
		    		onMapPnrCluster.addLayer(pnrCluster[$(this).children().eq(0).html()-1]);
		    	}
		    });
		    
		    $('pnrTable')
		    $('.dataTables_scrollHead thead th').css('text-align','center');
			$('#dialogPreLoader').hide();
			$("#tabs").show();
		},
		complete: function(){
			$("#overlay").hide();
		}
	});
	
}

function numberWithCommas(x) {
    return x.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
}
