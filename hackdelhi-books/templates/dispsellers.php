<div class="container">
  <br>
  <div class="row">
    <?php 
    $sellers = getsellers($_GET["book"]);
    // echo "<pre>";
    // print_r($sellers);
    // echo "</pre>";
    if ($sellers != false) {
      # code...
   
    foreach ($sellers as $value) {
       $userdetails = getuserdetails($value["userid"]);
       $userdetails = $userdetails[0];
      ?>
  
<div class="col m4 s6">
       <div class="card">
    <div class="card-image waves-effect waves-block waves-light">
      <img class="activator" src="img/default.png">
    </div>
    <div class="card-content">
      <span class="card-title activator grey-text text-darken-4"><?php echo $userdetails["name"]; ?><i class="material-icons right">more_vert</i></span>
      
      <p class="green-text">Price: <?php echo $value["price"]; ?> INR</p>
      <p class="green-text">Price : 
        <?php
        $x = getbitcoinrate();
        $x = $x->result[0];
        $paisa = getbitcoinvalue($value["price"]*100,$x->lowestAsk) ; 
        echo $paisa;
        ?>
        BTC
      </p>
    </div>
    <div class="card-reveal">
      <span class="card-title grey-text text-darken-4"><?php echo $userdetails["name"]; ?><i class="material-icons right">close</i></span>
      <p>Email: <?php echo $userdetails["email"]; ?></p>
      <p>Mobile: <?php echo $userdetails["contact"]; ?></p>
      <p>Semester: <?php echo $userdetails["sem"]; ?></p>
      <b class="green-text">Price: <?php echo $value["price"]; ?> INR </b>
  <br>
      <b class="green-text">Price: <?php echo $paisa; ?> BTC</b>
      <br>
      <br>
      <div class="col s12">
        <?php 
          $res = disableping($userdetails["id"]);
        if($res[0]["count"] == 0) {?>
          <button onclick="send_notification(<?php echo $userdetails['id']; ?>,<?php echo $_GET['book']; ?>)" class="btn" style="width:100%;">Ping!</button>
          <br><br>
           <button class="btn" onclick="location.href='paywithbitcoin.php?book=<?php echo $_GET['book']; ?>&id=<?php echo $userdetails['id']; ?>'" style="width:100%;">Pay with BitCoin</button>
        <?php }else{ ?>
         <button disabled class="btn" style="width:100%;">Ping!</button>
        <?php } ?>
     </div>
   </div>
 </div>
  </div>

      <?php
    }
     }else{
    ?>
      <h1> Sorry No Sellers are available for your college for this book :(</h1>
      <h3>Please contact your seniors and urge them to use the platform </h3>
    <?php } ?>
  </div>
</div>