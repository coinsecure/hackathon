<?php

	require '../includes/config.php'; 
	
	if ($_SERVER["REQUEST_METHOD"] == "POST") {
		
		$x = getnotifdetails($_POST["notifid"]);
		$x = $x[0];
		$result = query("DELETE FROM sellers WHERE bookid = ? and userid = ? ",$x["bookid"],$x["recieverid"]);
		$result = query("DELETE FROM notifications WHERE notifid = ?",$_POST["notifid"]);


	}else{
		redirect("/");
	}

 ?>