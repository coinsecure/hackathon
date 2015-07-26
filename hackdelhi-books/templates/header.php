<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="UTF-8">
	<title>HackDelhi Books</title>

  	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  	<meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1.0, user-scalable=no"/>
  	
  	<!-- CSS  -->
  	<link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
  	<link href="css/materialize.css" type="text/css" rel="stylesheet" media="screen,projection"/>
    <link rel="stylesheet" href="css/font-awesome.min.css">
  	<!-- Jquery -->
  	<script src="https://ajax.googleapis.com/ajax/libs/jquery/2.1.3/jquery.min.js"></script>
	  <!-- Compiled and minified JavaScript -->
	  <script src="https://cdnjs.cloudflare.com/ajax/libs/materialize/0.97.0/js/materialize.min.js"></script>
    <script src="js/app.js"></script>

</head>



<body class="grey lighten-5">

<!--
  <script>
      window.fbAsyncInit = function() {
        FB.init({
          appId      : '839288846167954',
          xfbml      : true,
          version    : 'v2.3'
        });
      };

      (function(d, s, id){
         var js, fjs = d.getElementsByTagName(s)[0];
         if (d.getElementById(id)) {return;}
         js = d.createElement(s); js.id = id;
         js.src = "http://connect.facebook.net/en_US/sdk.js";
         fjs.parentNode.insertBefore(js, fjs);
       }(document, 'script', 'facebook-jssdk'));
  </script>

-->

  <nav class="teal" role="navigation">
    <div class="nav-wrapper container">
      <a id="logo-container" href="index.php" class="brand-logo">HackDelhi Books</a>
      <ul class="right hide-on-med-and-down"> 
        <?php if(isset($_SESSION["id"])){ ?>
          <li><a href="search.php"><i class="material-icons left">search</i>Search Books</a></li>
          <li><a href="viewcourseware.php"><i class="material-icons left">assignment</i>View Courseware</a></li>
          <li><a href="logout.php"><i class="material-icons left">trending_flat</i>Logout</a></li>
        <?php } else { ?>
          <li><a href="login.php">Login</a></li>
          <li><a href="register.php">Signup</a></li>  
          <?php } ?>
        </ul>

        <ul id="nav-mobile" class="side-nav">
          <?php if(isset($_SESSION["id"])){ ?>
          <li><a href="search.php"><i class="material-icons left">search</i>Search Books</a></li>
          <li><a href="viewcourseware.php"><i class="material-icons left">assignment</i>View Courseware</a></li>
          <li><a href="logout.php"><i class="material-icons left">trending_flat</i>Logout</a></li>
          <?php } else { ?>
          <li><a href="login.php">Login</a></li>
          <li><a href="register.php">Signup</a></li>  
          <?php } ?>
        </ul>
     <a href="#" data-activates="nav-mobile" class="button-collapse"><i class="material-icons">menu</i></a>
    </div>
  </nav>
	