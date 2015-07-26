<?php 
	include '../includes/config.php';

	if($_SERVER["REQUEST_METHOD"]=="POST"){
		$bookid = $_POST["book"];
		$price = $_POST["price"];
		addbook($bookid,$price);
		render("thanks.php",["message"=>"Thankyou for letting us know"]);
	}else{
		redirect("/");
	}
?>