<?php

	require '../includes/config.php'; 
	
	if ($_SERVER["REQUEST_METHOD"] == "POST") {
		$result = query("INSERT INTO notifications(recieverid,senderid,bookid) VALUES (?,?,?)",$_POST["userid"],$_SESSION["user"]["id"],$_POST["bookid"]);
		
	}else{
		redirect("/");
	}

 ?>