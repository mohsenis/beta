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
	$('.bothCheck').prop('checked', true);
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
var stopCluster;
var routeCluster;
var blockCluster;
var tractCluster;

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
		onMapCluster.addLayer(onMapTractClustern );
	}
}
function doNotDelete(){
    //DONT DELETE
};

function showOnMapReport(lat, lon, date, x){
	lat = lat.join(",");
	lon = lon.join(",");
	/*$( '#dialogDate').datepicker( "hide" );*/
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
	onMapCluster.addLayer(onMapStopCluster);
	onMapCluster.addLayer(onMapRouteCluster);
	onMapCluster.addLayer(onMapBlockCluster);
	onMapCluster.addLayer(onMapTractCluster);
	map.addLayer(onMapCluster);
	stopCluster = new Array();
	routeCluster = new Array();
	blockCluster = new Array();
	tractCluster = new Array();
	var colorArray=['gcluster', 'picluster', 'ccluster', 'rcluster', 'pucluster', 'brcluster'];
	$('#displayTransitReport').empty();
	$('#displayGeoReport').empty();
	$("#overlay").show();
	$.ajax({
		type: 'GET',
		datatype: 'json',
		url: '/TNAtoolAPI-Webapp/queries/transit/onmapreport?&lat='+lat+'&lon='+lon+'&x='+x+'&day='+date+'&key='+ key,
		async: true,
		success: function(data){
			$('#ts').html(numberWithCommas(data.MapTr.TotalStops));
			$('#tr').html(numberWithCommas(data.MapTr.TotalRoutes));
			$('#af').html('$'+Math.round(data.MapTr.AverageFare*100)/100);
			$('#mff').html('$'+data.MapTr.MedianFare);
			var html = '<table id="transitTable" class="display" align="center">';
			var tmp = //'<tr><th>Agency ID</th>'+
			'<th>Agency Name</th>'+
			'<th>Routes</th>'+
			'<th>Stops</th>'+
			'<th>Service Stops</th></tr>';	
			html += '<thead>'+tmp+'</thead><tbody>';
			var html2 = '<tfoot>'+tmp+'</tfoot>';
			$.each(data.MapTr.MapAgencies, function(i,item){
				html += //'<tr><td>'+item.Id+'</td>'+
						'<td>'+item.Name+'</td>'+
						'<td>'+numberWithCommas(item.MapRoutes.length)+'</td>'+
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
					marker.bindPopup('<b>Stop ID:</b> '+jtem.Id+'<br><b>Stop Name:</b> '+jtem.Name+'<br><b>Agency:</b> '+jtem.AgencyId+pophtml);
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
					polyline.bindPopup('<b>Route ID:</b> '+ktem.Id+'<br><b>Route Name:</b> '+ktem.Name+'<br><b>Agency:</b> '+ktem.AgencyId+'<br><b>Length:</b> '+numberWithCommas(Math.round(ktem.Length*100)/100)+' miles<br><b>Fare Price:</b> '+ktem.Fare+'<br><b>Run Frequency:</b> '+ktem.Frequency);
					tmpRouteCluster.addLayer(polyline);
				});
				routeCluster.push(tmpRouteCluster);
			});		
			html = html + '</tbody></table>';
			//html = html + '</tbody>'+html2+'</table>';
			$('#displayTransitReport').append($(html));
			var transitTable = $('#transitTable').DataTable( {
				"paging": false,
				"bSort": false,
				//"scrollY": "40%",
				"dom": 'T<"clear">lfrtip',
		        "tableTools": {
		        	"sSwfPath": "js/lib/DataTables/swf/copy_csv_xls_pdf.swf",
		        	"sRowSelect": "multi",
		        	"aButtons": []}
			});
			$("#transitTable_length").remove();
		    $("#transitTable_filter").remove();
		    $("#transitTable_info").remove();
		    transitTable.$('tr').click( function () {
		        // data = oTable.fnGetData( this );
		    	if($(this).hasClass('selected')){
		    		
		    		onMapStopCluster.removeLayer(stopCluster[$(this).index()]);
		    		onMapRouteCluster.removeLayer(routeCluster[$(this).index()]);
		    	}else{
		    		
		    		onMapStopCluster.addLayer(stopCluster[$(this).index()]);
		    		onMapRouteCluster.addLayer(routeCluster[$(this).index()]);
		    	}
		    });
		    
			$('#tp').html(numberWithCommas(data.MapG.TotalPopulation));
			$('#tb').html(numberWithCommas(data.MapG.TotalBlocks));
			$('#tt').html(numberWithCommas(data.MapG.TotalTracts));
			$('#tl').html(Math.round(parseFloat(data.MapG.TotalLandArea)*0.0000386102)/100+' mi<sup>2</sup>');
			
			html = '<table id="geoTable" class="display" align="center">';
			tmp = //'<tr><th>County ID</th>'+
			'<th>County Name</th>'+
			'<th>Tracts</th>'+
			'<th>Blocks</th>'+
			'<th>Population</th></tr>';	
			html += '<thead>'+tmp+'</thead><tbody>';
			$.each(data.MapG.MapCounties, function(i,item){
				html += //'<tr><td>'+item.Id+'</td>'+
						'<td>'+item.Name.replace(' County','')+'</td>'+
						'<td>'+numberWithCommas(item.MapTracts.length)+'</td>'+
						'<td>'+numberWithCommas(item.MapBlocks.length)+'</td>'+
						'<td>'+numberWithCommas(item.Poopulation)+'</td></tr>';
				var tmpBlockCluster = new L.FeatureGroup();
				var tmpTractCluster = new L.FeatureGroup();
				onMapIcon = L.icon({
				    iconUrl: 'js/lib/leaflet-0.7/images/block.png',
				    //iconSize:     [40, 55], // size of the icon
				    iconAnchor:   [15, 29], // point of the icon which will correspond to marker's location
				    popupAnchor:  [0, -20] // point from which the popup should open relative to the iconAnchor
				});
				$.each(item.MapBlocks, function(j,jtem){
					var blocmarker = L.marker([jtem.Lat,jtem.Lng], {icon: onMapIcon});
					blocmarker.bindPopup('<b>Block ID:</b> '+jtem.ID+'<br><b>Population:</b> '+numberWithCommas(jtem.Population)+'<br><b>County:</b> '+jtem.County+'<br><b>Land Area:</b> '+ numberWithCommas(Math.round(parseFloat(jtem.LandArea)*0.0000386102)/100)+' mi<sup>2</sup>');
					tmpBlockCluster.addLayer(blocmarker);
				});
				blockCluster.push(tmpBlockCluster);
				onMapIcon = L.icon({
				    iconUrl: 'js/lib/leaflet-0.7/images/tract.png',
				    //iconSize:     [40, 55], // size of the icon
				    iconAnchor:   [20, 39], // point of the icon which will correspond to marker's location
				    popupAnchor:  [0, -30] // point from which the popup should open relative to the iconAnchor
				});
				$.each(item.MapTracts, function(k,ktem){
					var tractmarker = L.marker([ktem.Lat,ktem.Lng], {icon: onMapIcon});
					tractmarker.bindPopup('<b>Tract ID:</b> '+ktem.ID+'<br><b>Population:</b> '+numberWithCommas(ktem.Population)+'<br><b>County:</b> '+ktem.County+'<br><b>Land Area:</b> '+ numberWithCommas(Math.round(parseFloat(ktem.LandArea)*0.0000386102)/100)+' mi<sup>2</sup>');
					tmpTractCluster.addLayer(tractmarker);
				});
				tractCluster.push(tmpTractCluster);
			});		
			html = html + '</tbody></table>';
			$('#displayGeoReport').append($(html));
			var geoTable = $('#geoTable').DataTable( {
				"paging": false,
				"bSort": false,
				//"scrollY": "40%",
				"dom": 'T<"clear">lfrtip',
				"tableTools":{
		        	"sSwfPath": "js/lib/DataTables/swf/copy_csv_xls_pdf.swf",
		        	"sRowSelect": "multi",
		        	"aButtons": []}
			});
			$("#geoTable_length").remove();
		    $("#geoTable_filter").remove();
		    $("#geoTable_info").remove();
		    geoTable.$('tr').click( function () {
		        // data = oTable.fnGetData( this );
		    	if($(this).hasClass('selected')){
		    		
		    		onMapBlockCluster.removeLayer(blockCluster[$(this).index()]);
		    		onMapTractCluster.removeLayer(tractCluster[$(this).index()]);
		    	}else{
		    		
		    		onMapBlockCluster.addLayer(blockCluster[$(this).index()]);
		    		onMapTractCluster.addLayer(tractCluster[$(this).index()]);
		    	}
		    });
		    
		    //$('#displayGeoReport .dataTables_scrollHead table').css('width',$('#displayGeoReport .dataTables_scrollHead').css('width'));
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