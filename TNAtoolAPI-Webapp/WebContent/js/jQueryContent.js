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
	
	$( "#datepicker" ).multiDatesPicker({
		changeMonth: true,
      	changeYear: true,
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
	
	$( "#datepicker" ).multiDatesPicker({
		changeMonth: true,
      	changeYear: true,
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
	//document.getElementById("LoS").value = w_qstringl;
		
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