<?php 

	include '../includes/config.php';
	if (isset($_GET["book"]) && isset($_GET["id"])) {

		render("paybitcoin.php");
	}
	else{
		redirect("/");
	}

 ?>