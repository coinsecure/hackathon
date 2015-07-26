<?php

	require '../includes/config.php'; 
	
	if ($_SERVER["REQUEST_METHOD"] == "POST") {
		$result = query("DELETE FROM notifications WHERE notifid = ?",$_POST["notifid"]);

	}else{
		redirect("/");
	}

 ?>