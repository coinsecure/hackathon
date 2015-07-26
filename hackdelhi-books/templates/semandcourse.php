<div class="container">
	<br><br>
	<div class="row">
        <div class="col s12 m12">
          <div class="card">
            <div class="card-content">
             <h3>Select your Stream and Semester</h3>
              <br>
              <div class="row">
                <form action="addsem.php" method="post" class="col s12">
                	<div class="input-field col s12">
                    <p>Stream</p>
                		<select name="stream" class="browser-default">
 						<?php 
	 						foreach ($streams as $value) {
 								echo '<option value="'.$value["streamid"].'">'.$value["streamname"].'</option>';
 							}
 						?>
                		</select>
						<br>
             <p>Sem</p>
                    <select name="sem" class="browser-default">
            <?php 
              $sems = ["0","I","II","III","IV","V","VI","VII","VIII"];
              for($i=1;$i<=8;$i++) {
                echo '<option value="'.$i.'">'.$sems[$i].'</option>';
              }
            ?>
                    </select>
            <br>
						
                		<button type="submit" class="btn">Select College</button>
            
                	</div>

                </form>
              </div>
			
            </div>

            
          </div>
        </div>
      </div>
</div>
</div>