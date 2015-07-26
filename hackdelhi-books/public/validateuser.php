<?php
	include '../includes/functions.php';
	include '../includes/constants.php';
	session_start();

	if(isset($_POST["data"])){
		$response = $_POST["data"];
		$_SESSION["user"] = $response;
		$_SESSION["logged"] = "true";
		$email = $_SESSION["user"]["email"];
		$name = $_SESSION["user"]["name"];
		echo "$email";

		// Create connection
		

		$result = query("INSERT INTO users (name, email) VALUES(?, ?)", $name,$email );
		if ($result === false)
		{
			apologize("Could not register new user, Username might be already taken");
		}
		else
		{
			$rows = query("SELECT LAST_INSERT_ID() AS id");
			$id = $rows[0]["id"];
			$_SESSION["id"] = $id;
			redirect("/");
		}

		//print_r($_SESSION);	

	}
	else{
		redirect("/");
	}
	
 ?>