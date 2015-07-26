<div class="container">
	<div class="row" align="center">
		<?php $data = getuserdetails($_GET["id"]);
			$payment = getpaymentdetails($_GET["id"],$_GET["book"]);
			
		 ?>
		 	<h2>Payment To: <?php echo $data[0]["name"]; ?></h2>
		 	<h2>Payment of:
		 	 <?php
		 	  $x = getbitcoinrate();
        $x = $x->result[0];
        $paisa = getbitcoinvalue($payment[0]["price"]*100,$x->lowestAsk) ; 
        echo $paisa;
 ?> BTC</h2>
			<h3>mjVWniBvYQv2qwhVbM7ccutFFPTvjixrtj</h3>	
		<img src="img/qr.png" alt="">
	
		
		
	</div>


</div>