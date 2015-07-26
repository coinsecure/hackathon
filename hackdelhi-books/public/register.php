<?php 
include '../includes/config.php';

if($_SERVER["REQUEST_METHOD"]=="POST"){
	$name = $_POST["name"];
	$email = $_POST["email"];
	$password = $_POST["password"];
	$contact = $_POST["contact"];
	$result = query("INSERT INTO users (name, email,password,contact) VALUES(?, ?, ?,?)", $name,$email,$password,$contact );
	if ($result === false)
	{
		apologize("Could not register new user, Email might be already taken");
	}
	else
	{
		$rows = query("SELECT LAST_INSERT_ID() AS id");
		$id = $rows[0]["id"];
		$rows = query("SELECT * FROM users WHERE id = ?",$id);
		$rows = $rows[0];
		$_SESSION["id"] = $id;
		$_SESSION["user"]= $rows;
		redirect("/");
	}
}
else{
	render("register.php");	
}
	
 ?>