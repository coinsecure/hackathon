<?php 
	include '../includes/config.php';
	if(isset($_GET["q"])){
		$data = getsearchres($_GET["q"]);
		render("searchwithres.php",["data"=>$data]);		
	}
	else{
		render("searchpage.php");	
	}
	
 ?>