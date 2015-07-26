<?php 
	require("../includes/config.php");
	//print_r($_SESSION);	

	if(isset($_SESSION["user"]["college"]))
	{
		if(isset($_SESSION["user"]["stream"])){
			render("homepage.php");
		}
		else{
			$streamdata = getstreamdata();
			render("semandcourse.php",["streams" => $streamdata]);				
		}
			
	}
	else{
		$colleges = getcolleges();
		render("askfordetails.php",["colleges"=>$colleges]);
	}
	
 ?>