<div class="container">
	<br><br>
	<div class="row">
        <div class="col s12 m12">
          <div class="card">
            <div class="card-content">
             <h3>Select your college</h3>
              <br>
              <div class="row">
                <form action="addcollege.php" method="post" class="col s12">
                	<div class="input-field col s12">
                		<select name="college" class="browser-default">
 						<?php 
	 						foreach ($colleges as $value) {
 								echo '<option value="'.$value["collegeid"].'">'.$value["collegename"].'</option>';
 							}
 						?>
                		</select>
						<br>
						
                		<button type="submit" class="btn">Select College</button>
            
                	</div>

                </form>
              </div>
			
            </div>

            <!-- 
            <div class="card-action">
              <button class="btn blue">Sign in with Facebook</button>
              <button class="btn green" onclick="location.href='register.php'">Sign up</button>
            </div>
             -->
          </div>
        </div>
      </div>
</div>
</div>