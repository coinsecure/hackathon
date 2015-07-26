<div class="container">
	<br><br>
	<div class="row">
		<form action="viewcourseware.php" method="get">
		<div class="input-field col s6 offset-s3">
			<select name="subject" id="subject" class="browser-default">
				<option value="">Choose Subject</option>
				<?php 
					$subjects = getsubjects($_SESSION["user"]["sem"],$_SESSION["user"]["stream"]);
	 						foreach ($subjects as $value) {
	 							if($value["subjectid"] == $_GET["subject"]){
	 								echo '<option value="'.$value["subjectid"].'" selected>'.$value["subjectname"].'</option>';
	 							}else{
	 								echo '<option value="'.$value["subjectid"].'">'.$value["subjectname"].'</option>';
	 							}
 							}
 				?>
			</select>
		</div>
		<div class="input-field col s2">
			<button type="submit" class="btn">Go</button>
		</div>
		</form>
	</div>


	<div class="row">
		<?php 
			$books = getbooks($_GET["subject"]);
			foreach ($books as $value) {
				?>

		 <div class="col s12 m4">
          <div class="card">
            <div class="card-image">
              <img src="img/book_cover_placeholder.png">
              <span class="card-title"><?php echo $value["bookname"] ?></span>
            </div>
            <div class="card-content">
              <p><?php echo $value["bookauthor"]; ?></p>
            </div>
            <div class="card-action">
              <a href="?subject=<?php echo $_GET["subject"]; ?>&book=<?php echo $value["bookid"]; ?>">View Sellers</a>
            </div>
          </div>
        </div>
     

		<?php
			}
		 ?>
	</div>
</div>