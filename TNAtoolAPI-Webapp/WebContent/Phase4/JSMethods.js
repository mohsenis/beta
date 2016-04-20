/////////////////////////////////
//////						/////
//////		Variables		/////
//////						/////
/////////////////////////////////
var key = Math.random();
var maxRadius = 5;
var qstring = '';
var qstringd = '';
var qstringx = '0.25';
var nameString = '';
var agencyId = getURIParameter("agency");
var w_qstringx = parseFloat(getURIParameter("x"));
var w_qstringl = parseInt(getURIParameter("l"));
var w_qstringd;
var w_qstring;
var keyName = getURIParameter("n");
var gap = parseFloat(getURIParameter("gap"));
var dbindex = parseInt(getURIParameter("dbindex"));
var popYear = parseInt(getURIParameter("popYear"));
var ajaxURL;
var progVal = 0;
var d = new Date();
var html, html2, temp;
var table;
var tableProperties = {
		hiddenCols : [],
		hiddenRows : [],
		unsortableCols : [], 
		colsToExport : [],
		iDisplayLength : 14,
		paging : true,
		bSort : true,
		bAutoWidth : true
		};


/////////////////////////////////
//////						/////
//////		Methods			/////
//////						/////
/////////////////////////////////

function loadDBList() {
	$.ajax({
		type: 'GET',
		datatype: 'json',
		url: '/TNAtoolAPI-Webapp/queries/transit/DBList',
		async: false,
		success: function(d){	
			var select = document.getElementById("dbselect");
			select.options.length = 0;
		    var menusize = 0;
		    $.each(d.DBelement, function(i,item){
		    	var option = document.createElement('option');
		        option.text = item;
		        option.value = i;
		        select.add(option, i);		    	
		    	menusize++;
		    });		    		    
		    if (dbindex<0 || dbindex>menusize-1){
		    	dbindex = 0;
		    	history.pushState('data', '', document.URL.split("dbindex")[0]+'dbindex=0');
		    }
		    select.options.size = menusize;
		    select.selectedIndex = dbindex;
		}			
	});
}

function progressBar() {
	var progressLabel = $(".progress-label");
	$("#progressbar").progressbar(
					{
						value : false,
						change : function() {
							progressLabel
									.html('<table><tr><td>Report in progress... </td><td>'+ $(this).progressbar("value")
											+ "% "+ '</td><td></span><img src="images/loadingGif.gif" alt="loading" style="width:20px;height:20px"></td></tr></table>');
						}
					});
	var prog = false;
	function progress() {
		$.ajax({
			type : 'GET',
			datatype : 'json',
			url : '/TNAtoolAPI-Webapp/queries/transit/PorgVal?&key=' + key,
			async : true,
			success : function(item) {
				progVal = parseInt(item.progVal);
				if (progVal == 0) {
					progVal = false;
					if (prog) {
						clearTimeout(timeVar);
					}
				} else {
					prog = true;
				}
				$("#progressbar").progressbar("value", progVal);
			}
		});
		if (progVal == 100) {
			clearTimeout(timeVar);
		}
	}
	timeVar = setInterval(progress, 100);
}

function pad(s) { return (s < 10) ? '0' + s : s; }

function buildDatatables(){
	var table  = $('#RT').DataTable( {
		"paging": tableProperties.paging,
		"bAutoWidth": tableProperties.bAutoWidth,
		"bSort" : tableProperties.bSort,
		"iDisplayLength": tableProperties.iDisplayLength,
	    "order": [[ 1, "asc" ]],
	    "aoColumnDefs": [
		                 { "bSortable": false, "aTargets": tableProperties.unsortableCols},
		                 { "visible": false, "targets": tableProperties.hiddenCols}
		               ],
	    select: {
            style: 'os',
        },
	    dom: 'Bfrtip',
	    
		buttons: [
					{
		            	className: 'buttons-csv-meta buttons-html5',
		            	footer: false,
					    fieldSeparator: ',',
						fieldBoundary: '"',
						escapeChar: '"',
						charset: null,
						header: true,
		            	text: "Export CSV & Metadata",
		                    //toolTip: "Sources of the data and description of the metrics",
		                    action: function ( e, dt, node, config ) {
		                    	//var output = dt.buttons.exportData(config.exportOptions);
		                    	var output = exportData( dt, config ).str;	 		                    	
		                    	var charset = config.charset;
		            		if ( config.customize ) {
		            			output = config.customize( output, config );
		            		}
		            		if ( charset !== false ) {
		            			if ( ! charset ) {
		            				charset = document.characterSet || document.charset;
		            			}
		            			if ( charset ) {
		            				charset = ';charset='+charset;
		            			}				            		}
		            		else {
		            			charset = '';
		            		}
		                    	var zip = new JSZip();
							zip.file($(document).find("title").text()+"-metaData.txt", "test\n");
							zip.file($(document).find("title").text()+".csv", output, {type: 'text/csv'+charset});
							var content = zip.generate({type:"blob"});
							saveAs(content, $(document).find("title").text()+".zip");
		                }
		                },
					{
					    extend: 'print',
					    text: 'Print Report',
					    footer: false,
					    exportOptions: {
					    	stripHtml: false,
					    	stripNewlines: false,
					    	columns: ':visible'
					    }
					},
		            {
		                extend: 'copyHtml5',
	                    text: 'Copy selected',
		                exportOptions: {
		                    columns: tableProperties.colsToExport,
		                    modifier: {
		                        selected: true
		                    }
		                }
		            },
		             
		        ],
		        language: {
					buttons: {
						copyTitle: '<p><b>Copy to clipboard</b></p>',
						copySuccess: {
							0: "No row was copied",
							1: "Copied one row to clipboard",
							_: "Copied %d rows to clipboard"
						},
					}
				},
		
	} );
	$( ".dt-buttons" ).css( "float", "right");
    $( ".dt-buttons" ).css( "margin-bottom", "1em");
    $.contextMenu({
        selector: '#RT tbody tr', 
        callback: function(key, options) {
            $(".buttons-copy").click();
        },
        items: {
            "copy": {name: "Copy Selected Rows", icon: "copy"}
        }
    });
    $( ".buttons-copy" ).hide();
    $(document).keydown(function(e) {
        if (e.keyCode == 67 && e.ctrlKey) {
        	$(".buttons-copy").click();
        }
    });
    $('#RT_wrapper').css("width", $('#RT').css("width"));
    $('#RT_wrapper').css("margin", "auto");
	$("#RT_length").remove();
    $("#RT_filter").insertBefore("#RT_info");
    $( ".dataTables_filter" ).css( "float", "left");
    $( ".dataTables_filter" ).before( "<br>" );
	return table;
}


function updateToolTips() {
	
	$(document).tooltip({
		position : {
			my : "left bottom",
			at : "right bottom",
		}
	});
	}


function reloadPage(){
	var output = document.URL;
	$(".input").each(function(index, object){
		output = setURIParameter(output, object.name, object.value, null)
	});
	try {
		var dates = $('#datepicker').multiDatesPicker('getDates');
		if(dates.length==0){
			$( "#datepicker" ).multiDatesPicker({
				addDates: [new Date()]
			});
		}
		dates = $('#datepicker').multiDatesPicker('getDates');
		w_qstringd = dates.join(",");
		output = setURIParameter(output, 'n', setDates(w_qstringd), keyName)
		keyName = setDates(w_qstringd);
	}catch(err){
		console.log("error: " + err.message);
	}
	window.location.href = output;
}

/* This method is implemented to be used for gathering the metadata of the report
 * in a text file to be exported.
 */
function getToolTips(){
	$("th").each(function(index, object){
		console.log($(object).text() + ': ' + $(object).find("em").attr("title"));
	});
}

function setURIParameter(url, param, newValue, currentValue) {
	if (newValue != currentValue) {
		var URL = url.split("&" + param + "=");
		var last = "";
		if (URL[1].indexOf("&") != -1) {
			last = URL[1].substring(URL[1].indexOf("&"));
		}
		return URL[0] + "&" + param + "=" + newValue + last;
	}else
		return url;
}


function getURIParameter(param, asArray) {
    return document.location.search.substring(1).split('&').reduce(function(p,c) {
        var parts = c.split('=', 2).map(function(param) { return decodeURIComponent(param); });
        if(parts.length == 0 || parts[0] != param) return (p instanceof Array) && !asArray ? null : p;
        return asArray ? p.concat(parts.concat(true)[1]) : parts.concat(true)[1];
    }, []);
}


function getDates(hex){
	if(hex=="--"){
		return null;
	}
	
	var year = ['a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z',
			    'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z',
			    '0','1','2','3','4','5','6','7','8','9','!','@','#','$','%','^','*','(',')','-','+','_','`','~'];
	var month = ['a','b','c','d','e','f','g','h','i','j','k','l'];
	var day = ['a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z',
	           'A','B','C','D','E'];
	
	var str="";
	var tmp="";
	var j =0;
	for(var i=0; i<Math.floor(hex.length/3); i++){
		tmp=month.indexOf(hex[j])+1;
		if(tmp<10){
			str+='0';
		}
		str+=tmp;
		str+='/';
		j++;
		
		tmp=day.indexOf(hex[j])+1;
		if(tmp<10){
			str+='0';
		}
		str+=tmp;
		str+='/';
		j++;
		
		str+=year.indexOf(hex[j])+2000;
		if(i<Math.floor(hex.length/3)-1){
			str+=',';
		}
		j++;
	}
	return str;
		
}


function go(key){	
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
}


function addDate(date){
	$( "<li title='Click to remove.' id="+dateID+" class='selectedDate' onclick=\"dateRemove(this, '"+date+"')\">"+Date.parse(date).toString('dddd, MMMM d, yyyy')+"</li>" ).appendTo( "#accordionItems" );
	$("#"+dateID).css({"border":"1px solid black","padding-left":"10px","font-size":"95%","display":"block","width":"80%","background-color":"grey","text-decoration":"none","color":"white","margin":"3px","border-radius":"5px"});
	$("#"+dateID).hover(function(){
		  $(this).css({"cursor":"pointer","-moz-transform":"scale(1.1,1.1)","-webkit-transform":"scale(1.1,1.1)","transform":"scale(1.1,1.1)"});
	},function(){
		  $(this).css({"cursor":"pointer","-moz-transform":"scale(1,1)","-webkit-transform":"scale(1,1)","transform":"scale(1,1)"});
	});			
	$('.selectedDate').css('margin','auto');
}


function getSession(){
	var username = "admin";
	$.ajax({
        type: "GET",
        url: "/TNAtoolAPI-Webapp/FileUpload?getSessionUser=gsu",
        dataType: "json",
        async: false,
        success: function(d) {
        	username = d.username;
        }
	});
	return username;
}


function hideLastCol(table){
	var column = table.column($('#RT thead th').length - 1);
	column.visible( ! column.visible() );
}


function dateRemove(e, d){
	$(e).remove();
	$("#datepicker").multiDatesPicker('removeDates', d);
	$("#accordion > h3").html($('#datepicker').multiDatesPicker('getDates').length + " day(s) selected");	
	$("#submit").trigger('mouseenter');    
}

function setDates(str){
	var year = ['a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z',
			    'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z',
			    '0','1','2','3','4','5','6','7','8','9','!','@','#','$','%','^','*','(',')','-','+','_','`','~'];
	var month = ['a','b','c','d','e','f','g','h','i','j','k','l'];
	var day = ['a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z',
	           'A','B','C','D','E'];
	
	var strs = str.split(',');
	var hex = "";
	var date;
	for(var i=0; i<strs.length; i++){
		date = strs[i].split('/');
		if(parseInt(date[2])>2075){
			date[2]='2075';
		}else if(parseInt(date[2])<2000){
			date[2]='2000';
		}
		hex+=month[parseInt(date[0])-1]+day[parseInt(date[1]-1)]+year[parseInt(date[2])-2000];		
	}
	return hex;	
}

function exportData( dt, config ){
	var newLine = NewLine( config );
	var data = dt.buttons.exportData( config.exportOptions );
	var boundary = config.fieldBoundary;
	var separator = config.fieldSeparator;
	var reBoundary = new RegExp( boundary, 'g' );
	var escapeChar = config.escapeChar !== undefined ?
		config.escapeChar :
		'\\';
	var join = function ( a ) {
		var s = '';

		// If there is a field boundary, then we might need to escape it in
		// the source data
		for ( var i=0, ien=a.length ; i<ien ; i++ ) {
			if ( i > 0 ) {
				s += separator;
			}

			s += boundary ?
				boundary + ('' + a[i]).replace( reBoundary, escapeChar+boundary ) + boundary :
				a[i];
		}

		return s;
	};

	var header = config.header ? join( data.header )+newLine : '';
	var footer = config.footer && data.footer ? newLine+join( data.footer ) : '';
	var body = [];

	for ( var i=0, ien=data.body.length ; i<ien ; i++ ) {
		body.push( join( data.body[i] ) );
	}

	return {
		str: header + body.join( newLine ) + footer,
		rows: body.length
	};
}

function NewLine( config )
{
	return config.newline ?
		config.newline :
		navigator.userAgent.match(/Windows/) ?
			'\r\n' :
			'\n';
}

function numberconv(x) {
	if (x.indexOf('E') > -1){
		x = Number(x).toString();
	}
    var parts = x.split(".");
    parts[0] = parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, ",");
    if (parts[1]>0){
    	return parts.join(".");
    }else{
    	return parts[0];
    }    
}

function numWithCommas(x) {
    return x.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
}

function isWholeNumber(evt) {
	evt = (evt) ? evt : window.event;
	var charCode = (evt.which) ? evt.which : evt.keyCode;
	if (charCode > 31 && (charCode < 48 || charCode > 57)) {
		return false;
	}
	return true;
	}

function isNumber(evt) {
	evt = (evt) ? evt : window.event;
	var charCode = (evt.which) ? evt.which : evt.keyCode;
	if (charCode == 46) {
		if ($("#Sradius").val().indexOf('.') !== -1 ) return false;
	} else if (charCode > 31 && (charCode < 48 || charCode > 57)) {
	return false;
	}
	return true;
}

function setPopOptions(){
	var popselect = document.getElementById("popselect");
	var years = [2010,2015,2020,2025,2030,2035,2040,2045,2050];
	var option;
	for(var i=0; i<years.length;i++){
		option  = document.createElement('option');
	    option.text = years[i];
	    option.value = years[i];
	    popselect.add(option, i);
	};
	$('#popselect').val(popYear);
}

function inputUpdateHint(){
	$(".input").each(function(index, object){
		$(object).on('input', function() {	
			$("#submit").trigger('mouseenter');	
		});			
	});
	
	$("#submit").tooltip({
		  open: function () {		    	    
		    setTimeout(function () {		      
		    	$("#submit").trigger('mouseleave');
		    }, 4000);
		  }
		});
}

function showDollarSign(v){
	if(!isNaN(v)) return '$'+v;
	else return 'N/A';	
}

function addPercent(x) {
	return x+'%';
}

function trimLat(x){
	if (x.length > 12) 
		x = x.substring(0,11);
	return x;
}

function trimLon(x){
	if (x.length > 14) 
		x = x.substring(0,13);
	return x;
}