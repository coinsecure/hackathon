<div class="container">
  <br>
  <h3>Hello there, <?php  echo $_SESSION["user"]["name"];?>!</h3>

	<div class="row">
		<div class="col s12 m6">
			<h4>Notifications</h4>
			<ul class="collection">
				<?php $res = getnotif();
				  	if($res != false){
					foreach ($res as $value) {

						$userd = getuserdetails($value["senderid"]);
						$userd = $userd[0];
						$bookd = getbookd($value["bookid"]);
						$bookd = $bookd[0];
						?>
					<li class="collection-item avatar">
					<i class="material-icons circle">folder</i>
					<span class="title"><?php echo $userd["name"]; ?></span>
					<p>wants to buy <?php echo $bookd["bookname"]; ?> <br>
						<button onclick="removenotif2(<?php echo $value["notifid"] ?>)" class="btn" style="cursor:pointer;"><i class="material-icons">send</i><span style="left: 5px;
top: -3px;
position: relative;">Accept</span></button>
					</p>
					<a onclick="removenotif(<?php echo $value["notifid"] ?>)" class="secondary-content" style="cursor:pointer;"><i class="material-icons">close</i></a>

				</li>
				
				<?php		
					}
				}
				else{
					echo "<li class=\"collection-item avatar\"> <i class=\"material-icons circle red\">report</i><b style=\"font-size:20px\"> Sorry No new Notifications :( </b></li>";
				}
				 ?>
				 
			</ul>
			
			<div class="col s12 m12">
				<div class="card">
					<div class="card-content white-text">
						<span class="card-title black-text">My BitCoin Balance</span>
						<?php $x = getbitcoinbal(); ?>
						<h3 class="black-text"><?php echo ($x->result[0]); ?> satoshis</h3>
						</div>
						
					</div>
				</div>
			

		</div>
		<div class="col s12 m6">
			<h4>Add Books</h4>
			 <form action="addbook.php" method="post" class="col s12">
			 	<div class="input-field col s12" style="margin-top:-15px;">
			 	  <p>Select Stream</p>
			 		<select onchange="populatesubjects()" id="stream" class="browser-default">
			 	  		<option value="">Choose Stream</option>
			 			<?php 
			 				$streams = getstreamdata();
	 						foreach ($streams as $value) {
 								echo '<option value="'.$value["streamid"].'">'.$value["streamname"].'</option>';
 							}
 						?>
			 		</select>

			 		<p>Semester</p>
			 		<select onchange="populatesubjects()" id="sem" name="sem" class="browser-default">
			 			<option value="">Choose Sem</option>
			 			<?php 
			 			$sems = ["0","I","II","III","IV","V","VI","VII","VIII"];
			 			for($i=1;$i<=8;$i++) {
			 				echo '<option value="'.$i.'">'.$sems[$i].'</option>';
			 			}
			 			?>
			 		</select>

                    <p>Subject</p>
                    <select name="subject" id="subject" onchange="populatebooks()" class="browser-default">
                    	<option value="">Choose subject</option>
			 			
                    </select>

					<p>Book</p>
                    <select name="book" id="book" class="browser-default">
                    	<option value="">Choose book</option>
                    	
                    </select>

			 	<br>
			 	</div>
			 	<div class="input-field col s12">
                    	<input name="price" id="last_name" type="number" class="validate white" width="100%">
                    	<label for="last_name">Price</label>

				<button type="submit" class="btn">Add Book</button>
                </div>

     		 </form>
		</div>
	</div>
</div>