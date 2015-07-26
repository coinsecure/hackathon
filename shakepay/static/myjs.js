var myShakeEvent = new Shake({
    threshold: 15, // optional shake strength threshold
    timeout: 1000 // optional, determines the frequency of event generation
});
myShakeEvent.start();
window.addEventListener('shake', shakeEventDidOccur, false);

//function to call when shake occurs
function shakeEventDidOccur () {
   //put your own code here etc.
    if (user_id) {
    	$('#loading_response').show();
    	var data = {};
    	data.user_id = user_id;
    	data.long = 1;
    	data.lat = 1;
    	var amount = $('#amount').val();
    	if(amount) {
    		data.amount = amount;
    	}

    	$.ajax({
		    // The URL for the request
		    url: "shake",
		 	
		 	data : data,
		    // Whether this is a POST or GET request
		    type: "GET",
		    // The type of data we expect back
		    dataType : "json",
		    // Code to run if the request succeeds;
		    // the response is passed to the function
		    success: function( json ) {
		    	$('#loading_response').hide();
		        $('#response').text(JSON.stringify(json)).show();
		    }
		});
    }
}

$(document).ready(function(){
	$('#submit').click(function(){

		user_id = $('#user_id').val();
		$('#loading_balance').show();
		$.ajax({
	    // The URL for the request
		    url: "user/" + user_id,
		 
		    // Whether this is a POST or GET request
		    type: "GET",
		 
		    // The type of data we expect back
		    dataType : "json",
		 
		    // Code to run if the request succeeds;
		    // the response is passed to the function
		    success: function( json ) {
		    	$('#loading_balance').hide();
		        var balance = json.balance.data.available_balance;

		        $.ajax({
		        	url : 'https://api.coinsecure.in/v0/noauth/lasttrade',
		        	type : "GET",
		        	dataType : "json",
		        	success : function(json) {
		        		var lastSellPrice = json.result[0].lasttrade[0].ask[0][0].rate;
		        		$('#others').show();
		        		$('#balance').text(balance);
		        		balance = parseFloat(balance);
		        		alert(balance);
		        		alert(lastSellPrice);
		        		$('#total_cost').text(Math.floor(lastSellPrice*balance));
		        	}
		        });


		    }
		});
	});

});