function loadDBList() {
	$.ajax({
		type : 'GET',
		datatype : 'json',
		url : '/TNAtoolAPI-Webapp/queries/transit/DBList',
		async : false,
		success : function(d) {
			var select = document.getElementById("dbselect");
			select.options.length = 0;
			var menusize = 0;
			$.each(d.DBelement, function(i, item) {
				var option = document.createElement('option');
				option.text = item;
				option.value = i;
				select.add(option, i);
				menusize++;
			});
			if (dbindex < 0 || dbindex > menusize - 1) {
				dbindex = 0;
				history.pushState('data', '', setURIParameter({
					value : '0'
				}, 'dbindex', null));
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


function buildDatatables(){
	var table  = $('#RT').DataTable( {
		//"scrollY": "76%",
		"paging": true,
		"iDisplayLength": 14,
	    "order": [[ 1, "asc" ]],
		
	    select: {
            style: 'os',
        },
	    dom: 'Bfrtip',
	    
		buttons: [
					{
		            	className: 'buttons-csv-meta buttons-html5',
		            	footer: false,
					    /*exportOptions: {
					        columns: [0,1,2,3,4,6,7,8,9,10,12,13,14,15,16,17,18,20,21,22,23,24],
					    },*/
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
		                    	//var blob = new Blob(["Hello, world!"], {type: "text/plain;charset=utf-8"});
		                    	
		                    	var zip = new JSZip();
							zip.file($(document).find("title").text()+"-metaData.txt", "test\n");
							zip.file($(document).find("title").text()+".csv", output, {type: 'text/csv'+charset});
							var content = zip.generate({type:"blob"});
							saveAs(content, $(document).find("title").text()+".zip");
		                    	
		                    	//var blob = new Blob( [output], {type: 'text/csv'+charset} );
		                    	//saveAs(blob, $(document).find("title").text()+".csv");
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
		                    /*columns: [0,1,2,3,4,17,18 ],*/
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
		var URL = output.split(object.name);
		var last = "";
		if (URL[1].indexOf("&") != -1) {
			last = URL[1].substring(URL[1].indexOf("&"));
		}
		output = URL[0] + object.name + "=" + object.value + last;
	});
	location.replace(output);
}

function setURIParameter(element, param, currentValue) {
	if (element.value != currentValue) {
		var URL = document.URL.split(param);
		var last = "";
		if (URL[1].indexOf("&") != -1) {
			last = URL[1].substring(URL[1].indexOf("&"));
		}
		return URL[0] + param + "=" + element.value + last;
	}
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