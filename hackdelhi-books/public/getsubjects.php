<?php 

	include '../includes/config.php';

	if($_SERVER["REQUEST_METHOD"] == "POST"){
		$sem = $_POST["semester"];
		$stream = $_POST["subjectid"];

		$arr = getsubjects($stream,$sem);
		echo json_encode($arr);
		//echo json_encode((getsubjects($stream,$sem));
	}
	else{
		redirect("/");
	}
 ?>