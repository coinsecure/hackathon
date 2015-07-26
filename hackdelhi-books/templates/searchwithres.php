<div class="container">
	<br>
	<br>
	 <nav class="teal">
    <div class="nav-wrapper">
      <form action="search.php" method="get">
        <div class="input-field">
          <input name="q" id="search" type="search" required>
          <label for="search"><i class="material-icons">search</i></label>
        <!--   <i class="material-icons">close</i>
         --></div>
      </form>
    </div>
  </nav>
  <br>
  <br>
   <div class="row">
   
  <?php foreach ($data as $value) {
    ?>
        <div class="col s12 m6">
          <div class="card">
            <div class="card-content">
              <span class="card-title black-text"><?php echo $value["bookname"]; ?></span>
              <p><?php echo $value["bookauthor"]; ?></p>
            </div>
            <div class="card-action">
              <a href="#">View Sellers</a>
              
            </div>
          </div>
        </div>
    <?php
  } ?>
     </div>
   
</div>
