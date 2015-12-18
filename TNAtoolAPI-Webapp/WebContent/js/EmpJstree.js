var default_jstree = {		
			"core":{
				"check_callback" : true,
				"themes" : { "stripes" : true },
				"data" : [
					{"text" : "CA - Age", "type" : "CA_default", "id" : "CA", "state" : {"opened" : true},
						"children" : [
									{"text" : "CA01-workers age 29 or younger", "type" : "CA", "id" : "CA01"},
									{"text" : "CA02-workers age 30 to 54", "type" : "CA", "id" : "CA02"},
									{"text" : "CA03-workers age 55 or older", "type" : "CA", "id" : "CA03"}
						              ]
					},
					{"text" : "CE - Earnings", "type" : "CE_default", "id" : "CE", "state" : {"opened" : true},
						"children" : [
										{"text" : "CE01-earnings $1250/month or less", "type" : "CE", "id" : "CE01"},
										{"text" : "CE02-earnings $1251/month to $3333/month", "type" : "CE", "id" : "CE02"},
										{"text" : "CE03-earnings greater than $3333/month", "type" : "CE", "id" : "CE03"}
							              ]            	  
					},
					{"text" : "CNS - NAICS sectors", "type" : "CNS_default", "id" : "CNS",  "state" : {"opened" : true},
						"children" : [
										{"text" : "CNS01-sector 11 (Agriculture, Forestry, Fishing and Hunting)", "type" : "CNS", "id" : "CNS01"},
										{"text" : "CNS02-sector 21 (Mining, Quarrying, and Oil and Gas Extraction)", "type" : "CNS", "id" : "CNS02"},
										{"text" : "CNS03-sector 22 (Utilities)", "type" : "CNS", "id" : "CNS03"},
										{"text" : "CNS04-sector 23 (Construction)", "type" : "CNS", "id" : "CNS04"},
										{"text" : "CNS05-sector 31-33 (Manufacturing)", "type" : "CNS", "id" : "CNS05"},
										{"text" : "CNS06-sector 42 (Wholesale Trade)", "type" : "CNS", "id" : "CNS06"},
										{"text" : "CNS07-sector 44-45 (Retail Trade)", "type" : "CNS", "id" : "CNS07"},
										{"text" : "CNS08-sector 48-49 (Transportation and Warehousing)", "type" : "CNS", "id" : "CNS08"},
										{"text" : "CNS09-sector 51 (Information)", "type" : "CNS", "id" : "CNS09"},
										{"text" : "CNS10-sector 52 (Finance and Insurance)", "type" : "CNS", "id" : "CNS10"},
										{"text" : "CNS11-sector 53 (Real Estate and Rental and Leasing)", "type" : "CNS", "id" : "CNS11"},
										{"text" : "CNS12-sector 54 (Professional, Scientific, and Technical Services)", "type" : "CNS", "id" : "CNS12"},
										{"text" : "CNS13-sector 55 (Management of Companies and Enterprises)", "type" : "CNS", "id" : "CNS13"},
										{"text" : "CNS14-sector 56 (Administrative and Support and Waste Management and Remediation Services)", "type" : "CNS", "id" : "CNS14"},
										{"text" : "CNS15-sector 61 (Educational Services)", "type" : "CNS", "id" : "CNS15"},
										{"text" : "CNS16-sector 62 (Health Care and Social Assistance)", "type" : "CNS", "id" : "CNS16"},
										{"text" : "CNS17-sector 71 (Arts, Entertainment, and Recreation)", "type" : "CNS", "id" : "CNS17"},
										{"text" : "CNS18-sector 72 (Accommodation and Food Services)", "type" : "CNS", "id" : "CNS18"},
										{"text" : "CNS19-sector 81 (Other Services [except Public Administration])", "type" : "CNS", "id" : "CNS19"},
										{"text" : "CNS20-sector 92 (Public Administration)", "type" : "CNS", "id" : "CNS20"}
							              ]
					},
					{"text" : "CR - Race", "type" : "CR_default", "id" : "CR", "state" : {"opened" : true},
						"children" : [
										{"text" : "CR01-White, Alone", "type" : "CR", "id" : "CR01"},
										{"text" : "CR02-Black or African American Alone", "type" : "CR", "id" : "CR02"},
										{"text" : "CR03-American Indian or Alaska Native Alone", "type" : "CR", "id" : "CR03"},
										{"text" : "CR04-Asian Alone", "type" : "CR", "id" : "CR04"},
										{"text" : "CR05-Native Hawaiian or Other Pacific Islander Alone", "type" : "CR", "id" : "CR05"},
										{"text" : "CR07-Two or More Race Groups", "type" : "CR", "id" : "CR07"},
							              ]            	  
					},
					{"text" : "CT - Ethnicity", "type" : "CT_default", "id" : "CT", "state" : {"opened" : true},
						"children" : [
										{"text" : "CT01-Not Hispanic or Latino", "type" : "CT", "id" : "CT01"},
										{"text" : "CT02-Hispanic or Latino", "type" : "CT", "id" : "CT02"}
							              ]            	  
					},
					{"text" : "CD - Educational Attainment", "type" : "CD_default", "id" : "CD", "state" : {"opened" : true},
						"children" : [
										{"text" : "CD01-Less than high school", "type" : "CD", "id" : "CD01"},
										{"text" : "CD02-High school or equivalent, no college", "type" : "CD", "id" : "CD02"},
										{"text" : "CD03-Some college or Associate degree", "type" : "CD", "id" : "CD03"},
										{"text" : "CD04-Bachelor's degree or advanced degree", "type" : "CD", "id" : "CD04"}
							              ]            	  
					},
					{"text" : "CS - Sex", "type" : "CS_default", "id" : "CS", "state" : {"opened" : true},
						"children" : [
										{"text" : "CS01-Male", "type" : "CS", "id" : "CS01"},
										{"text" : "CS02-Female", "type" : "CS", "id" : "CS02"}
							              ]            	  
					}
			]},
			
			"types" : {
				"CA_default" : {
					"icon" : "js/lib/images/folder_closed-go.ico",
					"valid_children" : ["CA", "CA_default"]					
				},
				"CA" : {
					"icon" : "js/lib/images/arrow-right.ico",
					"valid_children" : []
				},
				"CE_default" : {
					"icon" : "js/lib/images/folder_closed-go.ico",
					"valid_children" : ["CE", "CE_default"]					
				},
				"CE" : {
					"icon" : "js/lib/images/arrow-right.ico",
					"valid_children" : []
				},
				"CNS_default" : {
					"icon" : "js/lib/images/folder_closed-go.ico",
					"valid_children" : ["CNS", "CNS_default"]					
				},
				"CNS" : {
					"icon" : "js/lib/images/arrow-right.ico",
					"valid_children" : []
				},
				"CR_default" : {
					"icon" : "js/lib/images/folder_closed-go.ico",
					"valid_children" : ["CR", "CR-default"]					
				},
				"CR" : {
					"icon" : "js/lib/images/arrow-right.ico",
					"valid_children" : []
				},
				"CT_default" : {
					"icon" : "js/lib/images/folder_closed-go.ico",
					"valid_children" : ["CT", "CT_default"]					
				},
				"CT" : {
					"icon" : "js/lib/images/arrow-right.ico",
					"valid_children" : []
				},
				
				"CD_default" : {
					"icon" : "js/lib/images/folder_closed-go.ico",
					"valid_children" : ["CD", "CD_default"]					
				},
				"CD" : {
					"icon" : "js/lib/images/arrow-right.ico",
					"valid_children" : []
				},
				"CS_default" : {
					"icon" : "js/lib/images/folder_closed-go.ico",
					"valid_children" : ["CS", "CS_default"]					
				},
				"CS" : {
					"icon" : "js/lib/images/arrow-right.ico",
					"valid_children" : []
				}					
			},
			"contextmenu":{         
				"items": function($node) {
					var tree = $("#jstree").jstree(true);
					if (tree.get_type($node).indexOf('default') > -1 && tree.get_parent($node) == '#'){
						return {
							"Add group":{
								"separator_before": false,
								"separator_after": false,
								"label": "Add group",
								"icon" : "js/lib/images/folder_closed-add.ico",
								"action": function (obj){
									var nodesList = [];
									$('.jstree-node').each(function(){
									  if (tree.get_type($(this)) == tree.get_type($node)){
										  var id   = $(this).attr('id');
										  var text = $(this).children('a').text();
										  nodesList.push(text);
										 }
									});

									var counter = 1;
									var newName = "Aggregated " + tree.get_text($node) + " " + counter;
									
									if ($.inArray(newName, nodesList) < 0){
										var d = new Date();
										var uniqeNo = d.getTime();
										tree.create_node($($node).attr('id'),{ "text" : newName, "type" : tree.get_type($node), "id":  uniqeNo + "_aggregate", "state" : {"opened" : true} },'first',null,null);
									}else{			
										while ($.inArray(newName, nodesList) >= 0) {
											newName = "Aggregated " + tree.get_text($node) + " " + counter++;
										}
										var d = new Date();
										var uniqeNo = d.getTime();
										tree.create_node($($node).attr('id'),{ "text" : newName, "type" : tree.get_type($node), "id":  uniqeNo + "_aggregate",  "state" : {"opened" : true} },'first',null,null);
									}
									tree.deselect_node($node);
								}
							},
							"Rename": {
								"separator_before": false,
								"separator_after": false,
								"label": "Rename",
								"icon" : "js/lib/images/Rename.ico",
								"action": function (obj) { 
									tree.edit($node);
								}
							}
						};	
					}else if (tree.get_type($node).indexOf('default') > -1 && tree.get_parent($node) != '#'){
						return {
							"Rename": {
								"separator_before": false,
								"separator_after": false,
								"label": "Rename",
								"icon" : "js/lib/images/Rename.ico",
								"action": function (obj) { 
									tree.edit($node);
								}
							},                         
							"Remove": {
								"separator_before": false,
								"separator_after": false,
								"label": "Remove",
								"icon" : "js/lib/images/folder_open-delete.ico",
								"action": function (obj) { 
									var children = tree.get_children_dom($node);
									$.each(children, function(ind,obj){
										tree.move_node($(this).attr('id'), tree.get_type($(this)));
									});
									tree.delete_node($node);
								}
							}
						};	
					}else{
						return {
							"Rename": {
								"separator_before": false,
								"separator_after": false,
								"label": "Rename",
								"icon" : "js/lib/images/Rename.ico",
								"action": function (obj) { 
									tree.edit($node);
								}
							}
						};	
					}					
				}
			},
			"plugins" : [
						"dnd",
						"contextmenu",
						"sort",
						"types",
						"unique",
						"checkbox"
						]
		
	}


var new_jstree = {
		"core":{
			"check_callback" : true,
			"themes" : { "stripes" : true },
			"data" : [
				{"text" : "CA - Age", "type" : "CA_default", "id" : "CA", "state" : {"opened" : true, "disabled" : true},
					"children" : [
									{"text" : "CA01-workers age 29 or younger", "type" : "CA", "id" : "CA01", "state" : {"selected" : false, "disabled" : true}},
									{"text" : "CA02-workers age 30 to 54", "type" : "CA", "id" : "CA02", "state" : {"selected" : false, "disabled" : true}},
									{"text" : "CA03-workers age 55 or older", "type" : "CA", "id" : "CA03", "state" : {"selected" : false, "disabled" : true}}
						              ]
					},
					{"text" : "CE - Earnings", "type" : "CE_default", "id" : "CE", "state" : {"opened" : true, "disabled" : true},
						"children" : [
										{"text" : "CE01-earnings $1250/month or less", "type" : "CE", "id" : "CE01", "state" : {"selected" : false, "disabled" : true}},
										{"text" : "CE02-earnings $1251/month to $3333/month", "type" : "CE", "id" : "CE02", "state" : {"selected" : false, "disabled" : true}},
										{"text" : "CE03-earnings greater than $3333/month", "type" : "CE", "id" : "CE03", "state" : {"selected" : false, "disabled" : true}}
							              ]            	  
					},
				{"text" : "CNS", "type" : "CNS_default", "id" : "CNS",  "state" : {"opened" : true},
					"children" : [
					              {"text" : "Category 01", "type" : "CNS_default", "id" : "Category01",  "state" : {"opened" : true},
					            	  "children" : [{"text" : "CNS01-sector 11 (Agriculture, Forestry, Fishing and Hunting)", "type" : "CNS", "id" : "CNS01", "state" : {"selected" : false, "disabled" : true}}]
					              },
					              {"text" : "Category 02", "type" : "CNS_default", "id" : "Category02",  "state" : {"opened" : true},
					            	  "children" : [{"text" : "CNS02-sector 21 (Mining, Quarrying, and Oil and Gas Extraction)", "type" : "CNS", "id" : "CNS02", "state" : {"selected" : false, "disabled" : true}}]
					              },
					              {"text" : "Category 03", "type" : "CNS_default", "id" : "Category03",  "state" : {"opened" : true},
					            	  "children" : [{"text" : "CNS04-sector 23 (Construction)", "type" : "CNS", "id" : "CNS04", "state" : {"selected" : false, "disabled" : true}}]
					              },
					              {"text" : "Category 04", "type" : "CNS_default", "id" : "Category04",  "state" : {"opened" : true},
					            	  "children" : [{"text" : "CNS05-sector 31-33 (Manufacturing)", "type" : "CNS", "id" : "CNS05", "state" : {"selected" : false, "disabled" : true}},
					            	                {"text" : "CNS09-sector 51 (Information)", "type" : "CNS", "id" : "CNS09", "state" : {"selected" : false, "disabled" : true}}
					            	  ]
					              },
					              {"text" : "Category 05", "type" : "CNS_default", "id" : "Category05",  "state" : {"opened" : true},
					            	  "children" : [{"text" : "CNS03-sector 22 (Utilities)", "type" : "CNS", "id" : "CNS03", "state" : {"selected" : false, "disabled" : true}},
					            	                {"text" : "CNS08-sector 48-49 (Transportation and Warehousing)", "type" : "CNS", "id" : "CNS08", "state" : {"selected" : false, "disabled" : true}}
					            	  ]
					              },
					              {"text" : "Category 06", "type" : "CNS_default", "id" : "Category6",  "state" : {"opened" : true},
					            	  "children" : [{"text" : "CNS06-sector 42 (Wholesale Trade)", "type" : "CNS", "id" : "CNS06", "state" : {"selected" : false, "disabled" : true}}]
					              },
					              {"text" : "Category 07", "type" : "CNS_default", "id" : "Category07",  "state" : {"opened" : true},
					            	  "children" : [{"text" : "CNS07-sector 44-45 (Retail Trade)", "type" : "CNS", "id" : "CNS07", "state" : {"selected" : false, "disabled" : true}}]
					              },
					              {"text" : "Category 08", "type" : "CNS_default", "id" : "Category08",  "state" : {"opened" : true},
					            	  "children" : [{"text" : "CNS10-sector 52 (Finance and Insurance)", "type" : "CNS", "id" : "CNS10", "state" : {"selected" : false, "disabled" : true}},
					            	                {"text" : "CNS13-sector 55 (Management of Companies and Enterprises)", "type" : "CNS", "id" : "CNS13", "state" : {"selected" : false, "disabled" : true}}
					            	  ]
					              },
					              {"text" : "Category 09", "type" : "CNS_default", "id" : "Category09",  "state" : {"opened" : true},
					            	  "children" : [{"text" : "CNS11-sector 53 (Real Estate and Rental and Leasing)", "type" : "CNS", "id" : "CNS11", "state" : {"selected" : false, "disabled" : true}},
					            	                {"text" : "CNS12-sector 54 (Professional, Scientific, and Technical Services)", "type" : "CNS", "id" : "CNS12", "state" : {"selected" : false, "disabled" : true}},
					            	                {"text" : "CNS14-sector 56 (Administrative and Support and Waste Management and Remediation Services)", "type" : "CNS", "id" : "CNS14", "state" : {"selected" : false, "disabled" : true}},
													{"text" : "CNS15-sector 61 (Educational Services)", "type" : "CNS", "id" : "CNS15", "state" : {"selected" : false, "disabled" : true}},
													{"text" : "CNS16-sector 62 (Health Care and Social Assistance)", "type" : "CNS", "id" : "CNS16", "state" : {"selected" : false, "disabled" : true}},
													{"text" : "CNS17-sector 71 (Arts, Entertainment, and Recreation)", "type" : "CNS", "id" : "CNS17", "state" : {"selected" : false, "disabled" : true}},
													{"text" : "CNS18-sector 72 (Accommodation and Food Services)", "type" : "CNS", "id" : "CNS18", "state" : {"selected" : false, "disabled" : true}},
													{"text" : "CNS19-sector 81 (Other Services [except Public Administration])", "type" : "CNS", "id" : "CNS19", "state" : {"selected" : false, "disabled" : true}}
					            	  ]
					              },
					              {"text" : "Category 10", "type" : "CNS_default", "id" : "Category10",  "state" : {"opened" : true},
					            	  "children" : [{"text" : "CNS20-sector 92 (Public Administration)", "type" : "CNS", "id" : "CNS20", "state" : {"selected" : false, "disabled" : true}}]
					              }
					              ]
				},
				{"text" : "CR - Race", "type" : "CR_default", "id" : "CR", "state" : {"opened" : true, "disabled" : true},
					"children" : [
									{"text" : "CR01-White, Alone", "type" : "CR", "id" : "CR01", "state" : {"disabled" : true}},
									{"text" : "CR02-Black or African American Alone", "type" : "CR", "id" : "CR02", "state" : {"disabled" : true}},
									{"text" : "CR03-American Indian or Alaska Native Alone", "type" : "CR", "id" : "CR03", "state" : {"disabled" : true}},
									{"text" : "CR04-Asian Alone", "type" : "CR", "id" : "CR04", "state" : {"disabled" : true}},
									{"text" : "CR05-Native Hawaiian or Other Pacific Islander Alone", "type" : "CR", "id" : "CR05", "state" : {"disabled" : true}},
									{"text" : "CR07-Two or More Race Groups", "type" : "CR", "id" : "CR07", "state" : {"disabled" : true}}
						              ]            	  
				},
				{"text" : "CT - Ethnicity", "type" : "CT_default", "id" : "CT", "state" : {"opened" : true, "disabled" : true},
					"children" : [
									{"text" : "CT01-Not Hispanic or Latino", "type" : "CT", "id" : "CT01", "state" : {"disabled" : true}},
									{"text" : "CT02-Hispanic or Latino", "type" : "CT", "id" : "CT02", "state" : {"disabled" : true}}
						              ]            	  
				},
				{"text" : "CD - Educational Attainment", "type" : "CD_default", "id" : "CD", "state" : {"opened" : true, "disabled" : true},
					"children" : [
									{"text" : "CD01-Less than high school", "type" : "CD", "id" : "CD01", "state" : {"disabled" : true}},
										{"text" : "CD02-High school or equivalent, no college", "type" : "CD", "id" : "CD02", "state" : {"disabled" : true}},
										{"text" : "CD03-Some college or Associate degree", "type" : "CD", "id" : "CD03", "state" : {"disabled" : true}},
										{"text" : "CD04-Bachelor's degree or advanced degree", "type" : "CD", "id" : "CD04", "state" : {"disabled" : true}}
						              ]            	  
				},
				{"text" : "CS - Sex", "type" : "CS_default", "id" : "CS", "state" : {"opened" : true, "disabled" : true},
					"children" : [
									{"text" : "CS01-Male", "type" : "CS", "id" : "CS01", "state" : {"disabled" : true}},
										{"text" : "CS02-Female", "type" : "CS", "id" : "CS02", "state" : {"disabled" : true}}
						              ]            	  
				}
		]},
		
		"dnd" : {
			"is_draggable" : function(node) {
                for (i = 0; i < node.length; i++) { 
                	if (node[i].type.indexOf('default') == -1 )
                		return false;
                }
                return true;
            }
		},
		
		"types" : {
			"#" : {
				"icon" : "js/lib/images/folder_closed-go.ico",
				"valid_children" : []					
			},
			"CA_default" : {
				"icon" : "js/lib/images/folder_closed-go.ico",
				"valid_children" : ["CA", "CA_default"]					
			},
			"CA" : {
				"icon" : "js/lib/images/arrow-right.ico",
				"valid_children" : []
			},
			"CE_default" : {
				"icon" : "js/lib/images/folder_closed-go.ico",
				"valid_children" : ["CE", "CE_default"]					
			},
			"CE" : {
				"icon" : "js/lib/images/arrow-right.ico",
				"valid_children" : []
			},
			"CNS_default" : {
				"icon" : "js/lib/images/folder_closed-go.ico",
				"valid_children" : ["CNS", "CNS_default"]					
			},
			"CNS" : {
				"icon" : "js/lib/images/arrow-right.ico",
				"valid_children" : []
			},
			"CR_default" : {
				"icon" : "js/lib/images/folder_closed-go.ico",
				"valid_children" : ["CR", "CR-default"]					
			},
			"CR" : {
				"icon" : "js/lib/images/arrow-right.ico",
				"valid_children" : []
			},
			"CT_default" : {
				"icon" : "js/lib/images/folder_closed-go.ico",
				"valid_children" : ["CT", "CT_default"]					
			},
			"CT" : {
				"icon" : "js/lib/images/arrow-right.ico",
				"valid_children" : []
			},
			
			"CD_default" : {
				"icon" : "js/lib/images/folder_closed-go.ico",
				"valid_children" : ["CD", "CD_default"]					
			},
			"CD" : {
				"icon" : "js/lib/images/arrow-right.ico",
				"valid_children" : []
			},
			"CS_default" : {
				"icon" : "js/lib/images/folder_closed-go.ico",
				"valid_children" : ["CS", "CS_default"]					
			},
			"CS" : {
				"icon" : "js/lib/images/arrow-right.ico",
				"valid_children" : []
			}					
		},
		"contextmenu":{         
			"items": function($node) {
				var tree = $("#jstree").jstree(true);
				if (tree.get_type($node).indexOf('default') > -1 && tree.get_parent($node) == '#'){
					return {
						"Add group":{
							"separator_before": false,
							"separator_after": false,
							"label": "Add group",
							"icon" : "js/lib/images/folder_closed-add.ico",
							"action": function (obj){
								var nodesList = [];
								$('.jstree-node').each(function(){
								  if (tree.get_type($(this)) == tree.get_type($node)){
									  var id   = $(this).attr('id');
									  var text = $(this).children('a').text();
									  nodesList.push(text);
									 }
								});

								var counter = 1;
								var newName = "Aggregated " + tree.get_text($node) + " " + counter;
								
								if ($.inArray(newName, nodesList) < 0){
									var d = new Date();
									var uniqeNo = d.getTime();
									tree.create_node($($node).attr('id'),{ "text" : newName, "type" : tree.get_type($node), "id":  uniqeNo + "_aggregate", "state" : {"opened" : true} },'first',null,null);
								}else{			
									while ($.inArray(newName, nodesList) >= 0) {
										newName = "Aggregated " + tree.get_text($node) + " " + counter++;
									}
									var d = new Date();
									var uniqeNo = d.getTime();
									tree.create_node($($node).attr('id'),{ "text" : newName, "type" : tree.get_type($node), "id":  uniqeNo + "_aggregate",  "state" : {"opened" : true} },'first',null,null);
								}
								tree.deselect_node($node);
							}
						},
						"Rename": {
							"separator_before": false,
							"separator_after": false,
							"label": "Rename",
							"icon" : "js/lib/images/Rename.ico",
							"action": function (obj) { 
								tree.edit($node);
							}
						}
					};					
				}else{
					return {
						"Rename": {
							"separator_before": false,
							"separator_after": false,
							"label": "Rename",
							"icon" : "js/lib/images/Rename.ico",
							"action": function (obj) { 
								tree.edit($node);
							}
						}
					};	
				}					
			}
		},
		"plugins" : [
					"dnd",
					"contextmenu",
					"sort",
					"types",
					"unique",
					"checkbox"
					]
	
}