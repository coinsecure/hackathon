<?php

    /**
     * functions.php
     *
     * Hackdelhibooks
     *
     * Helper functions.
     */

    require_once("constants.php");

/**
     * Apologizes to user with message.
     */
    function apologize($message)
    {
       // require("../templates/header.php");

        render("apology.php", ["message" => $message]);
        
        //require("../templates/footer.php");
        exit;
    }


    function logout()
    {
        // unset any session variables
        $_SESSION = [];

        // expire cookie
        if (!empty($_COOKIE[session_name()]))
        {
            setcookie(session_name(), "", time() - 42000);
        }

        // destroy session
        session_destroy();
    }

    function query(/* $sql [, ... ] */)
    {
        // SQL statement
        $sql = func_get_arg(0);

        // parameters, if any
        $parameters = array_slice(func_get_args(), 1);

        // try to connect to database
        static $handle;
        if (!isset($handle))
        {
            try
            {
                // connect to database
                $handle = new PDO("mysql:dbname=" . DATABASE . ";host=" . SERVER, USERNAME, PASSWORD);

                // ensure that PDO::prepare returns false when passed invalid SQL
                $handle->setAttribute(PDO::ATTR_EMULATE_PREPARES, false); 
            }
            catch (Exception $e)
            {
                // trigger (big, orange) error
                trigger_error($e->getMessage(), E_USER_ERROR);
                exit;
            }
        }

        // prepare SQL statement
        $statement = $handle->prepare($sql);
        if ($statement === false)
        {
            // trigger (big, orange) error
            trigger_error($handle->errorInfo()[2], E_USER_ERROR);
            exit;
        }

        // execute SQL statement
        $results = $statement->execute($parameters);

        // return result set's rows, if any
        if ($results !== false)
        {
            return $statement->fetchAll(PDO::FETCH_ASSOC);
        }
        else
        {
            return false;
        }
    }

    /**
     * Redirects user to destination, which can be
     * a URL or a relative path on the local host.
     *
     * Because this function outputs an HTTP header, it
     * must be called before caller outputs any HTML.
     */
    function redirect($destination)
    {
        // handle URL
        if (preg_match("/^https?:\/\//", $destination))
        {
            header("Location: " . $destination);
        }

        // handle absolute path
        else if (preg_match("/^\//", $destination))
        {
            $protocol = (isset($_SERVER["HTTPS"])) ? "https" : "http";
            $host = $_SERVER["HTTP_HOST"];
            header("Location: $protocol://$host$destination");
        }

        // handle relative path
        else
        {
            // adapted from http://www.php.net/header
            $protocol = (isset($_SERVER["HTTPS"])) ? "https" : "http";
            $host = $_SERVER["HTTP_HOST"];
            $path = rtrim(dirname($_SERVER["PHP_SELF"]), "/\\");
            header("Location: $protocol://$host$path/$destination");
        }

        // exit immediately since we're redirecting anyway
        exit;
    }

    /**
     * Renders template, passing in values.
     */
    function render($template, $values = [])
    {
        // if template exists, render it
        if (file_exists("../templates/$template"))
        {
            // extract variables into local scope
            extract($values);

            // render header
            require("../templates/header.php");
            //require("../templates/navbar.php");
            // render template
            require("../templates/$template");

            // render footer
            require("../templates/footer.php");
        }

        // else err
        else
        {
            trigger_error("Invalid template: $template", E_USER_ERROR);
        }
    }

    // Function to get Colleges
    function getcolleges(){
        $result = query("SELECT * FROM colleges");
        return $result;
    }
    
    //function to update college
    function updatecollege($id){
        $result = query("UPDATE users SET college = $id WHERE id = ?",$_SESSION["user"]["id"]);
        if($result == false)
            return false;
        else
            return true;
    }

    //function to get stream data 
    function getstreamdata(){
        $result = query("SELECT * FROM streams WHERE collegeid = ? ",$_SESSION["user"]["college"]);
        return $result;
    }
    // function to update stream
    function updatestream($streamid,$sem){
          $result = query("UPDATE users SET stream = $streamid, sem = $sem WHERE id = ?",$_SESSION["user"]["id"]);
        if($result == false)
            return false;
        else
            return true;
    
    }

    //function to get subjects
    function getsubjects($sem,$stream){
        $result = query("SELECT * FROM courseware WHERE streamid = ? AND sem = ?",$stream,$sem);
        return $result;
    }

    function getbooks($subjectid){
        $result = query("SELECT * FROM books WHERE subjectid = ? ",$subjectid);
        return $result;
    }

    function addbook($bookid,$price){
        $result = query("INSERT INTO sellers(bookid,price,userid) VALUES(?,?,?)",$bookid,$price,$_SESSION["user"]["id"]);
        return $result;
    }

    // function to get sellers
    function getsellers($bookid){
        $result = query("SELECT * FROM sellers WHERE bookid = ?",$bookid);
        return $result;
    }

    function getuserdetails($userid){
        $result = query("SELECT * FROM users WHERE id = ?",$userid);
        return $result;
    }

    //function to disable ping
    function disableping($userid){
        $result = query("SELECT count(*) as `count` FROM notifications WHERE recieverid = ? AND senderid = ?",$userid,$_SESSION["user"]["id"]);
        if($result == false){
            return false;
        }
        else{
            return $result;
        }
    }

    //function to get notifaction
    function getnotif(){
        $result = query("SELECT * FROM notifications WHERE recieverid = ? ",$_SESSION["user"]["id"]);
        return $result;
    }

    //function to get book details
    function getbookd($bookid){
        $result = query("SELECT * FROM books WHERE bookid = ? ",$bookid);
        return $result;

    }

    //function to get search res
    function getsearchres($string){
        $result = query("SELECT * FROM books WHERE bookname LIKE ? ",'%'.$string.'%');
        return $result;
    }

    //fucntion to get bitcoin balance
    function getbitcoinbal(){
        $data = array("apiKey" => "er5i6BK9nwgNTTY5a6jwanHWWUBvyjPHFd4xWGyJ");                                                                    
        $data_string = json_encode($data);                                                                                   

        $ch = curl_init('https://api.coinsecureis.cool/v0/auth/coinbalance');                                                                      
        curl_setopt($ch, CURLOPT_CUSTOMREQUEST, "POST");                                                                     
        curl_setopt($ch, CURLOPT_POSTFIELDS, $data_string);                                                                  
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);                                                                      
        curl_setopt($ch, CURLOPT_HTTPHEADER, array(                                                                          
            'Content-Type: application/json',                                                                                
            'Content-Length: ' . strlen($data_string))                                                                       
        );                                                                                                                   

        $result = curl_exec($ch);

        $x = json_decode($result);
        return $x;
    }

    function getbitcoinrate(){
        $ch = curl_init('https://api.coinsecureis.cool/v0/noauth/lowestask');                                                                      
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);                                                                      
        
        $result = curl_exec($ch);

        return json_decode($result);
      }

    function getbitcoinvalue($paisa,$bitcoinrate){
        return $paisa/$bitcoinrate;
    }

     function getpaymentdetails($userid,$bookid){
         $result = query("SELECT * from sellers WHERE userid = ? AND bookid = ?",$userid,$bookid);
         return $result;
     }

     function getnotifdetails($notifid){
        $result = query("SELECT * from notifications WHERE notifid = ?",$notifid);
        return $result;
     }
?>

