<div class="container" style="padding-top:150px; padding-bottom:30px">
	<center><h4>Please select a subject to view your prescribed books:</h4></center>
	<br>
	<div class="row">
		<form action="viewcourseware.php" method="get">
		<div class="input-field col s6 offset-s3">
			<select name="subject" id="subject" class="browser-default">
				<option value="">Choose Subject</option>
				<?php 
					$subjects = getsubjects($_SESSION["user"]["sem"],$_SESSION["user"]["stream"]);
	 						foreach ($subjects as $value) {
 								echo '<option value="'.$value["subjectid"].'">'.$value["subjectname"].'</option>';
 							}
 				?>
			</select>
		</div>
		<div class="input-field col s2">
			<button type="submit" class="btn">Go</button>
		</div>
		</form>
	</div>
	<br><br><br><br>
	<center><h5>
		HackDelhi Books saves the course structure and the recommended books related to every subject beforehand. So, no need to worry about searching for books, just select the course and you're good to go!
	</h5></center>

</div>