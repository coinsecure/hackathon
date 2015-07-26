<?php 

	include '../includes/config.php';

	if($_SERVER["REQUEST_METHOD"] == "POST"){
		$subject = $_POST["subjectid"];
		$arr = getbooks($subject);
		echo json_encode($arr);
		//echo json_encode((getsubjects($stream,$sem));
	}
	else{
		redirect("/");
	}
 ?>