function getSession(){
	var username = "admin";
	$.ajax({
        type: "GET",
        url: "/TNAtoolAPI-Webapp/FileUpload?&getSessionUser=gsu",
        dataType: "json",
        async: false,
        success: function(d) {
        	username = d.username;
        }
	});
	return username;
}

function dateRemove(e, d){
	$(e).remove();
	$("#datepicker").multiDatesPicker('removeDates', d);
	$("#accordion > h3").html($('#datepicker').multiDatesPicker('getDates').length + " day(s) selected");	
	$("#submit").trigger('mouseenter');    
}

function isNumber(evt) {
	evt = (evt) ? evt : window.event;
	//var havedot = (howManyDecimals(document.getElementById("Sradius").value));
	var charCode = (evt.which) ? evt.which : evt.keyCode;
	//alert(charCode);
	if (charCode == 46) {
	//alert ('test');
	if (document.getElementById("Sradius").value.indexOf('.') !== -1) return false;
	} else if (charCode > 31 && (charCode < 48 || charCode > 57)) {
	return false;
	}
	return true;
	};
	
function isWholeNumber(evt) {
	evt = (evt) ? evt : window.event;
	//var havedot = (howManyDecimals(document.getElementById("Sradius").value));
	var charCode = (evt.which) ? evt.which : evt.keyCode;
	//alert(charCode);
	if (charCode > 31 && (charCode < 48 || charCode > 57)) {
		return false;
	}
	return true;
	};

function addDate(date){
	$( "<li title='Click to remove.' id="+dateID+" onclick=\"dateRemove(this, '"+date+"')\">"+Date.parse(date).toString('dddd, MMMM d, yyyy')+"</li>" ).appendTo( "#accordionItems" );
	$("#"+dateID).css({"border":"1px solid black","padding-left":"10px","font-size":"95%","display":"block","width":"80%","background-color":"grey","text-decoration":"none","color":"white","margin":"3px","border-radius":"5px"});
	$("#"+dateID).hover(function(){
		  $(this).css({"cursor":"pointer","-moz-transform":"scale(1.1,1.1)","-webkit-transform":"scale(1.1,1.1)","transform":"scale(1.1,1.1)"});
	},function(){
		  $(this).css({"cursor":"pointer","-moz-transform":"scale(1,1)","-webkit-transform":"scale(1,1)","transform":"scale(1,1)"});
	});			
}

function numberconv(x) {
    var parts = x.split(".");
    parts[0] = parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, ",");
    return parts.join(".");
}

function reload(){		
	var tmpX = (parseFloat(document.getElementById("Sradius").value)).toString();	
	history.pushState("", "", document.URL.replace('x='+w_qstringx, 'x='+tmpX));
	var dates = $('#datepicker').multiDatesPicker('getDates');
	if(dates.length==0){
		$( "#datepicker" ).multiDatesPicker({
			addDates: [new Date()]
		});
	}
	dates = $('#datepicker').multiDatesPicker('getDates');
	w_qstringd = dates.join(",");
	localStorage.setItem(keyName, w_qstringd);
	location.reload();	
}

function reloadU(){		
	var tmpU = (parseFloat(document.getElementById("Upop").value)).toString();	
	history.pushState("", "", document.URL.replace('pop='+upop, 'pop='+tmpU));	
	location.reload();	
}

function reloadG(){		
	var tmpX = (parseFloat(document.getElementById("Sradius").value)).toString();
	var tmpLos = (parseFloat(document.getElementById("LoS").value)).toString();
	history.pushState("", "", document.URL.replace('x='+w_qstringx, 'x='+tmpX));
	history.pushState("", "", document.URL.replace('l='+w_qstringl, 'l='+tmpLos));
	var dates = $('#datepicker').multiDatesPicker('getDates');
	if(dates.length==0){
		$( "#datepicker" ).multiDatesPicker({
			addDates: [new Date()]
		});
	}
	dates = $('#datepicker').multiDatesPicker('getDates');
	w_qstringd = dates.join(",");
	localStorage.setItem(keyName, w_qstringd);
	location.reload();	
}

function reloadUG(){
	var tmpU = (parseFloat(document.getElementById("Upop").value)).toString();
	var tmpX = (parseFloat(document.getElementById("Sradius").value)).toString();
	var tmpLos = (parseFloat(document.getElementById("LoS").value)).toString();
	history.pushState("", "", document.URL.replace('x='+w_qstringx, 'x='+tmpX));
	history.pushState("", "", document.URL.replace('l='+w_qstringl, 'l='+tmpLos));
	history.pushState("", "", document.URL.replace('pop='+w_qstring, 'pop='+tmpU));
	var dates = $('#datepicker').multiDatesPicker('getDates');
	if(dates.length==0){
		$( "#datepicker" ).multiDatesPicker({
			addDates: [new Date()]
		});
	}
	dates = $('#datepicker').multiDatesPicker('getDates');
	w_qstringd = dates.join(",");
	localStorage.setItem(keyName, w_qstringd);
	location.reload();	
}

function closebutton(){
	window.close();
}

function printbutton(){
	window.print();
}

function exportbutton(){
	var uri = 'data:application/csv;fileName=Report.csv;base64,'+ window.btoa(csvfile);
	window.open(uri);
}

function dateToString(date){
	var dArr = date.split("/");
	return dArr[2]+dArr[0]+dArr[1];
}

function stringToDate(str){
	var sArr = new Array();
	sArr.push(str.substring(4, 6));
	sArr.push(str.substring(6, 8));
	sArr.push(str.substring(0, 4));
	return sArr.join("/");
}

function parseDate(str) {
    var mdy = str.split('/');
    return new Date(mdy[2], mdy[0]-1, mdy[1]);
}

function daydiff(first, second) {
    return (second-first)/(1000*60*60*24);
}

function pad(s) { return (s < 10) ? '0' + s : s; }

function go(key){	
	$(document).tooltip({
		position: {
	        my: "left bottom",
	        at: "right bottom",
	    }
	});
	
	$( "#progressbar" ).progressbar({
	    value: false,
	}); 
	var prog=false;
	function progress() {
		$.ajax({
			type: 'GET',
			datatype: 'json',
			url: '/TNAtoolAPI-Webapp/queries/transit/PorgVal?&key='+key,
			async: true,
			success: function(item){
				progVal = parseInt(item.progVal);
				if(progVal==0){
					progVal=false;
					if(prog){
						clearTimeout(timeVar);
					}
				}else{
					prog=true;
				}
				
				$( "#progressbar" ).progressbar( "value", progVal );	
			}			
		});	
	    if ( progVal == 100 ) {
			clearTimeout(timeVar);
	  	}
	} 
	
	timeVar = setInterval(progress, 100);
	
	//check if the selected dates are within the agency's start and end date.
	var startDateUnion="";
	var endDateUnion="";	
	$.ajax({
		type: 'GET',
		datatype: 'json',
		url: '/TNAtoolAPI-Webapp/queries/transit/calendarRange?&dbindex='+dbindex,
		async: false,
		success: function(d){
			startDateUnion = d.Startdateunion;
			endDateUnion = d.Enddateunion;
			
		}			
	});
	var tmpdates= new Array();
	for(var i=0;i<w_qstringd.split(",").length;i++){
		if(startDateUnion<=dateToString(w_qstringd.split(",")[i]) && dateToString(w_qstringd.split(",")[i])<=endDateUnion){
			tmpdates.push(w_qstringd.split(",")[i]);
		}
	}
	
	if(tmpdates.length==0){
		w_qstringd=stringToDate(endDateUnion);
	}else{
		w_qstringd = tmpdates.join(",");
	}
	
	var maxDate = Math.ceil(daydiff(new Date(), parseDate(stringToDate(endDateUnion))));
	var minDate = Math.ceil(daydiff(new Date(), parseDate(stringToDate(startDateUnion))));
	//*****************//
	
	$( "#datepicker" ).multiDatesPicker({
		changeMonth: true,
      	changeYear: true,
      	minDate: minDate,
      	maxDate: maxDate,
		addDates: w_qstringd.split(","),
		onSelect: function (date) {
			dateID = date.replace("/","").replace("/","");
			if($("#"+dateID).length==0){
				addDate(date);
				$("#submit").trigger('mouseenter');
			}else{
				$("#"+dateID).remove();
				$("#submit").trigger('mouseenter');
			}
			$("#accordion > h3").html($('#datepicker').multiDatesPicker('getDates').length + " day(s) selected");
	    }
	});
	
	var cdate;
	for(var i=0; i<w_qstringd.split(",").length; i++){
		cdate = w_qstringd.split(",")[i];
		dateID = cdate.replace("/","").replace("/","");
		addDate(cdate);		
	}
	
	$("#accordion").accordion({
		collapsible: true,
		active: false,
		heightStyle: "content"
	});
	$("#accordion").accordion("refresh");
	$("#accordion > h3").html(w_qstringd.split(",").length + " day(s) selected");
	
	
	document.getElementById("Sradius").value = w_qstringx;
	document.getElementById("LoS").value = w_qstringl;
		
	jQuery('#Sradius').on('input', function() {		
		$("#submit").trigger('mouseenter');		
	});
	
	jQuery('#LoS').on('input', function() {		
		$("#submit").trigger('mouseenter');		
	});
	
	$("#submit").tooltip({
		  open: function () {		    	    
		    setTimeout(function () {		      
		    	$("#submit").trigger('mouseleave');
		    }, 4000);
		  }
		});
}

function gos(key){
	$(document).tooltip({
		position: {
	        my: "left bottom",
	        at: "right bottom",
	    }
	});
	
	$( "#progressbar" ).progressbar({
	    value: false,
	}); 
	var prog=false;
	function progress() {
		$.ajax({
			type: 'GET',
			datatype: 'json',
			url: '/TNAtoolAPI-Webapp/queries/transit/PorgVal?&key='+key,
			async: true,
			success: function(item){
				progVal = parseInt(item.progVal);
				if(progVal==0){
					progVal=false;
					if(prog){
						clearTimeout(timeVar);
					}
				}else{
					prog=true;
				}
				
				$( "#progressbar" ).progressbar( "value", progVal );	
			}			
		});	
	    if ( progVal == 100 ) {
			clearTimeout(timeVar);
	  	}
	} 
	
	timeVar = setInterval(progress, 100);
	
	//check if the selected dates are within the agency's start and end date.
	$.ajax({
		type: 'GET',
		datatype: 'json',
		url: '/TNAtoolAPI-Webapp/queries/transit/agencyCalendarRange?&dbindex='+dbindex+'&agency='+w_qstring,
		async: false,
		success: function(d){
			startDate = d.Startdate;
			endDate = d.Enddate;
		}			
	});
	var tmpdates= new Array();
	for(var i=0;i<w_qstringd.split(",").length;i++){
		if(startDate<=dateToString(w_qstringd.split(",")[i]) && dateToString(w_qstringd.split(",")[i])<=endDate){
			tmpdates.push(w_qstringd.split(",")[i]);
		}
	}
	
	if(tmpdates.length==0){
		w_qstringd=stringToDate(endDate);
	}else{
		w_qstringd = tmpdates.join(",");
	}
	
	var maxDate = Math.ceil(daydiff(new Date(), parseDate(stringToDate(endDate))));
	var minDate = Math.ceil(daydiff(new Date(), parseDate(stringToDate(startDate))));
	//*****************//
	
	$( "#datepicker" ).multiDatesPicker({
		changeMonth: true,
      	changeYear: true,
      	minDate: minDate,
      	maxDate: maxDate,
		addDates: w_qstringd.split(","),
		onSelect: function (date) {
			dateID = date.replace("/","").replace("/","");
			if($("#"+dateID).length==0){
				addDate(date);
				$("#submit").trigger('mouseenter');
			}else{
				$("#"+dateID).remove();
				$("#submit").trigger('mouseenter');
			}
			$("#accordion > h3").html($('#datepicker').multiDatesPicker('getDates').length + " day(s) selected<span style='margin-left:3em;font-size:85%'>Active Service Dates: "+stringToDate(startDate)+" to "+stringToDate(endDate)+"<span>");
	    }
	});
	
	var cdate;
	for(var i=0; i<w_qstringd.split(",").length; i++){
		cdate = w_qstringd.split(",")[i];
		dateID = cdate.replace("/","").replace("/","");
		addDate(cdate);		
	}
	
	$("#accordion").accordion({
		collapsible: true,
		active: false,
		heightStyle: "content"
	});
	$("#accordion").accordion("refresh");
	$("#accordion > h3").html(w_qstringd.split(",").length + " day(s) selected<span style='margin-left:3em;font-size:85%'>Active Service Dates: "+stringToDate(startDate)+" to "+stringToDate(endDate)+"<span>");
	
	
	document.getElementById("Sradius").value = w_qstringx;
	//document.getElementById("LoS").value = w_qstringl;
		
	jQuery('#Sradius').on('input', function() {		
		$("#submit").trigger('mouseenter');		
	});
	
	/*jQuery('#LoS').on('input', function() {		
		$("#submit").trigger('mouseenter');		
	});*/
	
	$("#submit").tooltip({
		  open: function () {		    	    
		    setTimeout(function () {		      
		    	$("#submit").trigger('mouseleave');
		    }, 4000);
		  }
		});
	
}