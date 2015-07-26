<?php
 
    /**
     * config.php
     *
     * Hackdelhibooks
     *
     * Configures pages.
     */
 
    // display errors, warnings, and notices
    ini_set("display_errors", true);
    error_reporting(E_ALL);
 
    // requirements
    require("constants.php");
    require("functions.php");
 
    // enable sessions
    session_start();
 
    // require authentication for most pages
    if (!preg_match("{(?:login|logout|register)\.php$}", $_SERVER["PHP_SELF"]))
    {
        if (empty($_SESSION["id"]))
        {
            redirect("login.php");
        }
    }


     // Checking if already logged in and trying to access login page
    
    if(preg_match("/login.php/", $_SERVER["PHP_SELF"]))
        // check if the session is running for user then redirect back to user dashboard
            if(isset($_SESSION["id"]))
                redirect("/");

/*
 // Checking if already logged in and trying to access login page
    
    if(preg_match("/register.php/", $_SERVER["PHP_SELF"]))
        // check if the session is running for user then redirect back to user dashboard
            if(isset($_SESSION["id"]))
                redirect("/");

 */
?>