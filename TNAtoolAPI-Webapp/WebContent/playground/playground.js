function userCount(){
	$.ajax({
        type: "GET",
        url: "/TNAtoolAPI-Webapp/modifiers/dbupdate/userCount",
        dataType: "json",
        async: false,
        success: function(d) {
        	if(d.DBError=="false"){
        		alert("Sorry. Currently the tool is not able register more users into the system.");
        		window.location.href = "index.html";
        	}
        }
	});
	
}

function PGlogin(){
	var password = $('#pass').val();
	var username = $('#user').val();
	$.ajax({
        type: "GET",
        url: "/TNAtoolAPI-Webapp/modifiers/dbupdate/validateUser?&user="+username+"&pass="+password,
        dataType: "json",
        async: false,
        success: function(d) {
        	if(d.DBError!="false"){
        		username = d.DBError;
        		if(isActive(username)!="true"){
        			alert("Your account has not been activated yet.");
        			return false;
        		}
    			//sessionStorage.setItem("username", username);
        		openSeession(username);
    			window.location.href = "playground.html";
    			
        	}else{
        		alert("The Username or Password was incorrect");
        		return false;
        	}
        }
	});
}

function isActive(username){
	var isActive = false;
	$.ajax({
        type: "GET",
        url: "/TNAtoolAPI-Webapp/modifiers/dbupdate/isActive?&user="+username,
        dataType: "json",
        async: false,
        success: function(d) {
        	if(d.DBError=="t"){
        		isActive = "true";
        	}else{
        		isActive = d.DBError;
        	}
        }
	});
	return isActive;
}

function openSeession(username){
	$.ajax({
        type: "GET",
        url: "/TNAtoolAPI-Webapp/FileUpload?&setSessionUser="+username,
        dataType: "json",
        async: false,
        success: function(d) {
        	
        }
	});
}

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

function endSession(){
	$.ajax({
        type: "GET",
        url: "/TNAtoolAPI-Webapp/FileUpload?&endSessionUser=esu",
        dataType: "json",
        async: false,
        success: function(d) {
        	
        }
	});
	window.location.href = "playground.html";
}

function PGregister(){
	//var passkey = $('#key').val();
	
	var username = $('#user').val();
	var password = $('#pass').val();
	var passwordR = $('#passR').val();
	if(password!=passwordR){
		alert('Passwrods entered do not match.');
		return false;
	}
	var email = $('#email').val();
	var firstname = $('#firstname').val();
	var lastname = $('#lastname').val();
	
	var regex = new RegExp("^[a-z][a-z0-9\_]+$");
    if (!regex.test(username)||username.length>8) {
    	$( "#UsernameDialog" ).dialog("open");
    	return false;
    }
    var cu = checkUser(username);
	var ce = checkUser(email);
	if(cu=="false" && ce=="false"){
		var au = addUser(username,password,email,firstname,lastname);
		sendRequestEmail(username,email,firstname,lastname);
		if(au=="true"){
			alert("You are successfully registered. Your account has to get approved and activated before using." +
					" Once approved, you'll receive an email indicating that you can use your credentials to log into the website.");
			
			window.location.href = "index.html";
		}else{
			alert(au);
			return false;
		}
	}else if(cu=="true"){
		alert("The Username \""+username+"\" already exists");
		return false;
	}else if(ce=="true"){
		alert("The Email \""+email+"\" already exists");
		return false;
	}else{
		return false;
	}
	/*$.ajax({
        type: "GET",
        url: "/TNAtoolAPI-Webapp/modifiers/dbupdate/validatePass?&pass="+passkey,
        dataType: "json",
        async: false,
        success: function(d) {
        	if(d.DBError=="true"){
        		var cu = checkUser(username);
        		var ce = checkUser(email);
        		if(cu=="false" && ce=="false"){
        			var au = addUser(username,password,email,firstname,lastname);
        			sendRequestEmail(username,email,firstname,lastname);
        			if(au=="true"){
        				alert("You are successfully registered. Your account has to get approved and activated before using." +
        						" Once approved, you'll receive an email indicating that you can use your credentials to log into the website.");
        				
        				window.location.href = "index.html";
        			}else{
        				alert(au);
        				return false;
        			}
        		}else if(cu=="true"){
        			alert("The Username \""+username+"\" already exists");
        			return false;
        		}else if(ce=="true"){
        			alert("The Email \""+email+"\" already exists");
        			return false;
        		}else{
        			return false;
        		}
        	}else{
        		alert("The Passkey is incorrect. Please try again");
        		return false;
        	}
        }
	});*/
}

function sendRequestEmail(username,email,firstname,lastname){
	
	$.ajax({
        type: "GET",
        url: "/TNAtoolAPI-Webapp/FileUpload?&username="+username+"&email="+email+"&firstname="+firstname+"&lastname="+lastname,
        dataType: "json",
        async: false,
        success: function(d) {
        	
        }
	});
	
}

function launchTNAguest(){
	window.open(
	  'http://localhost:8080/TNAtoolAPI-Webapp/?&dbindex=2',
	  '_blank'
	);
}

function launchTNA(){
	var isEmpty = selectFeed();
	if (isEmpty){
		alert('You must at least select one feed.')
		return;
	}
	window.open(
	  'http://localhost:8080/TNAtoolAPI-Webapp/?&dbindex=2',
	  '_blank'
	);
}

function guestUser(){
	$( "#guestDialog" ).dialog("open");
}

function getUserInfo(username){
	var userInfo= new Array();
	$.ajax({
        type: "GET",
        url: "/TNAtoolAPI-Webapp/modifiers/dbupdate/getUserInfo?&user="+username,
        dataType: "json",
        async: false,
        success: function(d) {
        	userInfo.push(d.Firstname);
        	userInfo.push(d.Lastname);
        	userInfo.push(d.Username);
        	userInfo.push(d.Quota);
        	userInfo.push(d.Usedspace);
        }
	});
	return userInfo;
}

function checkUser(username){
	var cu="false";
	$.ajax({
        type: "GET",
        url: "/TNAtoolAPI-Webapp/modifiers/dbupdate/checkUser?&user="+username,
        dataType: "json",
        async: false,
        success: function(d) {
        	cu = d.DBError;
        }
	});
	return cu;
}

function addUser(username,password,email,firstname,lastname){
	var au="";
	$.ajax({
        type: "GET",
        url: "/TNAtoolAPI-Webapp/modifiers/dbupdate/addUser?&user="+username+"&pass="+password+"&email="+email+"&firstname="+firstname+"&lastname="+lastname,
        dataType: "json",
        async: false,
        success: function(d) {
        	au = d.DBError;
        }
	});
	return au;
}

function listOfFeeds(){
	var b=false;
	var html = "<table class='feedTable'><tr><th style='text-align:center;height:60px'>Feed Name</th><th style='text-align:center;height:60px;'>Public</th></tr>";
	$.ajax({
        type: "GET",
        url: "/TNAtoolAPI-Webapp/FileUpload?&username="+username,
        dataType: "json",
        async: false,
        success: function(d) {
        	if(undefined != d.DBError){
        		return d.DBError;
        	}else{
	        	
	        	$.each(d.feeds, function(i,item){
	        		if(d.names[i]!=null){
		        		b=true;
	        			html+="<tr><td>";
		        		var agencynames = d.names[i].split(",");
		        		var html1 = "<br><span style='margin-left:3.5em'>Agencies:</span>";
		        		for(var k=0; k<agencynames.length; k++){
		        			html1 += "<br><span style='margin-left:6em'>"+agencynames[k]+"</span>";
		        		}
		        		html += "<p><input type='checkbox' class='selectFeed' id='"+item+"' style='margin-right:2em'><span>"+item+"</span>"
		        		+"<br><span style='margin-left:3.5em'>Start Date: "+stringToDate(d.startdates[i])+"</span>"+"<br><span style='margin-left:3.5em'>End Date: "+stringToDate(d.enddates[i])+"</span>"+html1+"</p></td>";
		        		html += "<td style='padding-left:20px'>" +
		        				"<select onchange='changePublic(this.value,\""+item+"\")'>";
		        		if(d.isPublic[i]=="f"){
		        			html +=	"<option value='TRUE'>Yes</option>" +
	        						"<option value='FALSE' selected>No</option>";
		        		}else{
		        			html +=	"<option value='TRUE' selected>Yes</option>" +
    								"<option value='FALSE'>No</option>";
		        		}
		        				
		        		html+="</select></td></tr>";
	        		}
	        	});
	        	html += "</table><br><input type='button' value='Delete feeds from database' onclick='deleteFeed()' class='btn btn-danger delete'>";
	        	
        	}
        }
	});
	if(!b){
		return "";
	}
	return html;
}

function selectFeed(){
	
	var feeds = new Array();
	
	$('.selectFeed').each(function(){
		if($(this).is(':checked')){
			feeds.push($(this).attr('id'));
		}
	});
	
	$('.selectPublicFeed').each(function(){
		if($(this).is(':checked')){
			feeds.push($(this).attr('id'));
		}
	});
	
	$('.selectOregonFeed').each(function(){
		if($(this).is(':checked')){
			feeds.push($(this).attr('id'));
		}
	});
	feeds = feeds.join(",");
	if(feeds.length==0){
		return true;
	}
	$.ajax({
        type: "GET",
        url: "/TNAtoolAPI-Webapp/modifiers/dbupdate/selectedFeeds?&feeds="+feeds+"&username="+username,
        dataType: "json",
        async: false,
        success: function(d) {
        	return false;
        }
	});
}

function changePublic(p, feedname){
	
	$.ajax({
        type: "GET",
        url: "/TNAtoolAPI-Webapp/modifiers/dbupdate/changePublic?&isPublic="+p+"&feedname="+feedname,
        dataType: "json",
        async: false,
        success: function(d) {
        	location.reload(true);
        }
	});
	
}

function listOfPublicFeed(){
	
	var html = "<table class='feedTable'><tr><th style='text-align:center;height:60px'>Feed Name</th><th style='text-align:center;height:60px'>Owner</th></tr>";
	var b = false;
	$.ajax({
        type: "GET",
        url: "/TNAtoolAPI-Webapp/FileUpload",
        dataType: "json",
        async: false,
        success: function(d) {
        	if(undefined != d.DBError){
        		return d.DBError;
        	}else{
	        	
	        	$.each(d.feeds, function(i,item){
	        		if(d.names[i]!=null&&d.ownerUsername[i]!=username){
	        			b=true;
	        			html+="<tr><td>";
		        		var agencynames = d.names[i].split(",");
		        		var html1 = "<br><span style='margin-left:3.5em'>Agencies:</span>";
		        		for(var k=0; k<agencynames.length; k++){
		        			html1 += "<br><span style='margin-left:6em'>"+agencynames[k]+"</span>";
		        		}
		        		html += "<p><input type='checkbox' class='selectPublicFeed' id='"+item+"' style='margin-right:2em'><span>"+item+"</span>"
		        		+"<br><span style='margin-left:3.5em'>Start Date: "+stringToDate(d.startdates[i])+"</span>"+"<br><span style='margin-left:3.5em'>End Date: "+stringToDate(d.enddates[i])+"</span>"+html1+"</p></td>";
		        		html += "<td  style='padding-left:20px'><P>";
		        		html += "<span>"+d.ownerFirstname[i]+"</span><br>";
		        		html += "<span>"+d.ownerLastname[i]+"</span>";
		        		html += "</p></td></tr>";
	        		}
	        	});
	        	html += "</table>";
	        	
        	}
        }
	});
	if(!b){
		return "";
	}
	return html;
}

function listOfOregonFeed(){
	
	var html = "";
	$.ajax({
        type: "GET",
        url: "/TNAtoolAPI-Webapp/FileUpload?&list=oregon",
        dataType: "json",
        async: false,
        success: function(d) {
        	if(undefined != d.DBError){
        		return d.DBError;
        	}else{
	        	
	        	$.each(d.feeds, function(i,item){
	        		if(d.names[i]!=null){
		        		var agencynames = d.names[i].split(",");
		        		var html1 = "<br><span style='margin-left:3.5em'>Agencies:</span>";
		        		for(var k=0; k<agencynames.length; k++){
		        			html1 += "<br><span style='margin-left:6em'>"+agencynames[k]+"</span>";
		        		}
		        		html += "<p><input type='checkbox' class='selectOregonFeed' id='"+item+"' style='margin-right:2em'><span>"+item+"</span>"
		        		+"<br><span style='margin-left:3.5em'>Start Date: "+stringToDate(d.startdates[i])+"</span>"+"<br><span style='margin-left:3.5em'>End Date: "+stringToDate(d.enddates[i])+"</span>"+html1+"</p>";
		        		
	        		}
	        	});
	        	
        	}
        }
	});
	
	return html;
}

function stringToDate(str){
	if(str==null){
		return "";
	}
	var sArr = new Array();
	sArr.push(str.substring(4, 6));
	sArr.push(str.substring(6, 8));
	sArr.push(str.substring(0, 4));
	return sArr.join("/");
}

function deleteFeed(){
	
	var feeds = new Array();
	
	$('.selectFeed').each(function(){
		if($(this).is(':checked')){
			feeds.push($(this).attr('id'));
		}
	});
	$("#panel-default").hide();
	$("#dialogPreLoader").show();
	$("#overlay").show();
	for(var i=0; i<feeds.length; i++){
		$.ajax({
	        type: "GET",
	        url: "/TNAtoolAPI-Webapp/FileUpload?&feedname="+feeds[i]+"&username="+username,
	        dataType: "json",
	        async: false,
	        success: function(d) {
	        	
	        }
		});
	}
	location.reload(true);
}

function getTotalSize(files) {
    var total = 0;
    $.each(files, function (index, file) {
        total += file.size || 1;
    });
    return total;
}

var username;
function go(){
	$('#oregonFeeds').html(listOfOregonFeed());
	$( "#feedAccordion" ).accordion({
	      collapsible: true
	});
	$( ".ui-accordion-header" ).click();
	
	$('.col-lg-7').css("width",'100%');
	$('.col-lg-5').css("width",'100%');
	//username = sessionStorage.getItem("username");
	username = getSession();
	var userInfo = getUserInfo(username);
	var freeSpace = userInfo[3]-userInfo[4];
	$('#helloUser').html("Hello "+userInfo[0]+" "+userInfo[1]);
	$('#freeSpace').html((freeSpace/1000000).toFixed(3));
	$('#feedList').html(listOfFeeds());
	$('#publicfeedList').html(listOfPublicFeed());
	
	
	'use strict';
	$('#fileupload').fileupload({
        url: '/TNAtoolAPI-Webapp/FileUpload',
        acceptFileTypes: /(zip)$/i,
        singleFileUploads: false,
        formData: {user: username},
        //sequentialUploads: true,
        maxFileSize: freeSpace,
    }).bind('fileuploadsubmit', function (e, data) {
    	if(getTotalSize(data.files)>freeSpace){
    		alert("The total size of the file(s) selected is more than the available space.");
    		return false;
    	}
    	$("#panel-default").hide();
    	$("#dialogPreLoader").show();
    	$("#overlay").show();
    	/*alert(getTotalSize(data.files));*/
    }).bind('fileuploaddone', function (e, data) {
    	location.reload(true);
    });
    
    // Load existing files:
    $('#fileupload').addClass('fileupload-processing');
    
    $.ajax({
        url: $('#fileupload').fileupload('option', 'url'),
        dataType: null,
        context: $('#fileupload')[0]
    }).always(function () {
        $(this).removeClass('fileupload-processing');
    }).done(function (result) {
        $(this).fileupload('option', 'done')
            .call(this, $.Event('done'), {result: result});
    });
    
    
}