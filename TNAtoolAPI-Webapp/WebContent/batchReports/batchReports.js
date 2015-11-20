var dbindices;

function checkTime(){
	var b = false;
	
	/*var start = 1*3600;
	var end = 6*3600;
	var now = new Date();
    now = now.getHours()*3600+now.getMinutes()*60+now.getSeconds();
    if(now>=start && now<=end){
    	b = true;
    }*/
	return b;
}

function getdbindices(){
	$.ajax({
		type: 'GET',
		datatype: 'json',
		url: '/TNAtoolAPI-Webapp/queries/transit/DBList',
		async: false,
		success: function(d){
			dbindices = d.DBelement;
		}   		
	});
}

function access(){
	if(checkTime()){
		$('#main').css('display','none');
		$('#afterHour').css('display','');
	}else{
		$('#main').css('display','');
		$('#afterHour').css('display','none');
		getdbindices();
	}
	$('body').css('display','');
}

function removeElement(e){
	$(e).siblings().remove();
	$(e).remove();
}

function addCalendar(){
	return 'Calendar<input readonly required type="text" class="tbox datePicker" style="width:25%"/>';
	
}

function addSearchRadius(){
	return 'Population Search Radius (miles)<input required type="text" class="tbox" onkeypress="return isNumber(event)"/>';
}

function addTransitSearchRadius(){
	return 'Transit Service Search Radius (miles)<input required type="text" class="tbox" onkeypress="return isNumber(event)"/>';
}

function addSpatialGap(){
	return 'Maximum Spatial Gap (miles)<input required type="text" class="tbox" onkeypress="return isNumber(event)"/>';
}

function addMinLevelService(){
	return 'Minimum Level of Service (times)<input required type="text" class="tbox" onkeypress="return isNumber(event)"/>';
}

function addDatabaseSelector(){
	var html = 'Database<select required style="height:25px">';
	html+='<option style="display:none"></option>';
	$.each(dbindices, function(i,item){
		html+='<option value="'+i+'">'+item+'</option>';
    });	
	html += '</select>';
	return html;
}

function addAgencyLikeReport(elementId){
	var html='';
	var report='';
	report+=addCalendar();
	report+='&nbsp--&nbsp'+addSearchRadius();
	report+='&nbsp--&nbsp'+addDatabaseSelector();
	html+='<a style="color:red; float:left; padding-top:7px" onclick="removeElement(this)" href="javascript:void(0)">Drop-</a>';
	html+='<div class="login-card" style="width:90%; padding:5px; background-color:#EFE6E6; text-align: center;" >'+report+'</div>';
	html='<span>'+html+'</span>';
	$( "#"+elementId ).append(html);
	$('.datePicker').multiDatesPicker();
}

function addCountyLikeReport(elementId){
	var html='';
	var report='';
	report+=addCalendar();
	report+='&nbsp--&nbsp'+addSearchRadius();
	report+='&nbsp--&nbsp'+addMinLevelService();
	report+='&nbsp--&nbsp'+addDatabaseSelector();
	html+='<a style="color:red; float:left; padding-top:7px" onclick="removeElement(this)" href="javascript:void(0)">Drop-</a>';
	html+='<div class="login-card" style="width:90%; padding:5px; background-color:#EFE6E6; text-align: center;" >'+report+'</div>';
	html='<span>'+html+'</span>';
	$( "#"+elementId ).append(html);
	$('.datePicker').multiDatesPicker();
}

function addParknRideLikeReport(elementId){
	var html='';
	var report='';
	report+=addTransitSearchRadius();
	report+='&nbsp--&nbsp'+addDatabaseSelector();
	html+='<a style="color:red; float:left; padding-top:7px" onclick="removeElement(this)" href="javascript:void(0)">Drop-</a>';
	html+='<div class="login-card" style="width:90%; padding:5px; background-color:#EFE6E6; text-align: center;" >'+report+'</div>';
	html='<span>'+html+'</span>';
	$( "#"+elementId ).append(html);
}

function addConnectedLikeReport(elementId){
	var html='';
	var report='';
	report+=addSpatialGap();
	report+='&nbsp--&nbsp'+addDatabaseSelector();
	html+='<a style="color:red; float:left; padding-top:7px" onclick="removeElement(this)" href="javascript:void(0)">Drop-</a>';
	html+='<div class="login-card" style="width:90%; padding:5px; background-color:#EFE6E6; text-align: center;" >'+report+'</div>';
	html='<span>'+html+'</span>';
	$( "#"+elementId ).append(html);
}