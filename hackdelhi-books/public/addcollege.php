<?php 

	require '../includes/config.php';
	
	// setting the college
	if($_SERVER["REQUEST_METHOD"] == "POST"){
		updatecollege($_POST["college"]);
		$_SESSION["user"]["college"] = $_POST["college"];
		$streamdata = getstreamdata();
		render("semandcourse.php",["streams"=>$streamdata]);
	}else{
		redirect("/");
	}

	

 ?>