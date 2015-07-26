<?php 

	require '../includes/config.php';
	
	// setting the college
	if($_SERVER["REQUEST_METHOD"] == "POST"){
		updatestream($_POST["stream"],$_POST["sem"]);
		$_SESSION["user"]["stream"] = $_POST["stream"];
		$_SESSION["user"]["sem"] = $_POST["sem"];
		redirect("/");
	}else{
		redirect("/");
	}

	

 ?>