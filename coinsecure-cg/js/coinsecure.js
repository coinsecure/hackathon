// 
// Created : Mon Jul 13 23:03:25 IST 2015
//
// Copyright (C) 2015, Sriram Karra <karra.etc@gmail.com>
// All Rights Reserved
//
// Licensed under the GNU AGPL v3
// 

var url_base = "https://api.coinsecureis.cool/v0";
var url_high_bid = "/noauth/highestbid";
var cur_page;

function addDatePickerHandler () {
    $("#from").datepicker({
	defaultDate: "+1w",
	changeMonth: true,
	changeYear: true,
	numberOfMonths: 1,
	dateFormat: "yy-mm-dd",
	onClose: function(selectedDate) {
	    $("#to").datepicker("option", "minDate", selectedDate);
	}
    });

    $("#to").datepicker({
	defaultDate: "+1w",
	changeMonth: true,
	changeYear: true,
	numberOfMonths: 1,
	dateFormat: "yy-mm-dd",
	onClose: function(selectedDate) {
	    $("#from").datepicker("option", "maxDate", selectedDate);
	}
    });
}

function saveAllFormFields () {
    console.log('Saving all form fields to localstorage...');

    if (cur_page == 'transactions') {
	localStorage.setItem("cs_apikey", $("#apikey").val());
    }

    if (cur_page == 'cg-actual') {
	localStorage.setItem("cs_apikey", $("#apikey").val());
	localStorage.setItem("cs_from", $("#from").val());
	localStorage.setItem("cs_to", $("#to").val());
    }

    if (cur_page == 'cg-proj') {
	localStorage.setItem("cs_apikey", $("#apikey").val());
	localStorage.setItem("cs_sell_qty", $("#sell_qty").val());
	localStorage.setItem("cs_sell_price", $("#sell_price").val());
    }

    console.log('Saving all form fields to localstorage...done.');
}

function loadFormFields () {
    if (cur_page == 'transactions') {
	$("#apikey").val(localStorage.getItem("cs_apikey"));
    }

    if (cur_page == 'cg-actual') {
	$("#apikey").val(localStorage.getItem("cs_apikey"));
	$("#from").val(localStorage.getItem("cs_from"));
	$("#to").val(localStorage.getItem("cs_to"));
    }

    if (cur_page == 'cg-proj') {
	$("#apikey").val(localStorage.getItem("cs_apikey"));
	$("#sell_qty").val(localStorage.getItem("cs_sell_qty"));
	$("#sell_price").val(localStorage.getItem("cs_sell_price"));
    }
}

// Register callbacks to handle specific events on our main UI.
function addFormHandlers () {
    console.log('addFormHandlers');

    addDatePickerHandler();

    $("#compute_cg_act").submit(function() {
	saveAllFormFields();
    });

    $("#compute_cg_proj").submit(function() {
	saveAllFormFields();
    });

    $("#fetch_txns").submit(function() {
	localStorage.setItem("cs_apikey", $("#apikey").val());
    });

    $("#cgtxns").dataTable();
    $("#buys").dataTable();
    $("#sells").dataTable();
}

function setActiveNav () {
    var pathname = window.location.pathname;;
    console.log('url: ' + pathname);

    if (pathname == '/') {
	$("#nav_brand").addClass("active-nav");
	cur_page = 'home';
    } else if (pathname == "/transactions") {
	$("#nav_txns").addClass("active-nav");
	cur_page = 'transactions';
    } else if (pathname == "/cgActual") {
	$("#nav_cg_act").addClass("active-nav");
	cur_page = 'cg-actual';
    } else if (pathname == "/cgProj") {
	$("#nav_cg_pr").addClass("active-nav");
	cur_page = 'cg-proj';
    }

    mixpanel.track('PageView', {
	'Page Name' : cur_page,
	'url' : window.location.pathname
    });
    console.log('Set current page: ' + cur_page);
}

function onLoad () {
    // Initialize the database if available
    addFormHandlers();
    setActiveNav();
    loadFormFields();
}

jQuery(onLoad);
