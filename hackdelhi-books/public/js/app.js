function login(){
   FB.login(function(response) {
   if (response.authResponse) {
     console.log('Welcome!  Fetching your information.... ');
     FB.api('/me',{fields:'name,email'}, function(response) {
       console.log('Good to see you, ' + response.name + '. ' + response.email);
       console.log(response);
       
       $.ajax({
          method: "POST",
          url: "validateuser.php",
          data: {data: response},
          success: function(data){
             console.log(data);
             location.reload();
          },
          error: function(reply){
            console.log(reply);
            console.log("Error");
          }

       });
     });     
   } else {
     console.log('User cancelled login or did not fully authorize.');
   }
 },{scope: 'email'});
}

function populatesubjects(){
  var idsub = $('#stream')[0].value;
  var sem = $('#sem')[0].value;
  $.ajax({
    method: "POST",
    url: "getsubjects.php",
    data: {subjectid: idsub,semester: sem},
    success: function(data){
      var select = document.getElementById("subject");
      var options = JSON.parse(data);
      // Clearing select
      for (i = 1; i < select.options.length; i++) {
        select.options[i] = null;
      }
      for (var i = 0; i < options.length; i++) {
        console.log("in loop");
        var opt = options[i];
        var el = document.createElement("option");
        el.textContent = opt["subjectname"];
        el.value = opt["subjectid"];
        select.appendChild(el);
      }
    }
  });
}

function populatebooks(){
  var idsub = $('#subject')[0].value;

  $.ajax({
    method: "POST",
    url: "getbooks.php",
    data: {subjectid: idsub},
    success: function(data){
      var select = document.getElementById("book");
      var options = JSON.parse(data);
      // Clearing select
      for (i = 1; i < select.options.length; i++) {
        select.options[i] = null;
      }
      for (var i = 0; i < options.length; i++) {
        console.log("in loop");
        var opt = options[i];
        var el = document.createElement("option");
        el.textContent = opt["bookname"];
        el.value = opt["bookid"];
        select.appendChild(el);
      }
    }
  });
}

function send_notification(userid,bookid){
  $.ajax({
    method: "POST",
    url: "sendnotif.php",
    data: {userid: userid,bookid: bookid},
    success: function(data){
      console.log(data);
      location.href = "/index.php";
    }
  });
}


function removenotif(notifid){
  $.ajax({
    method: "POST",
    url: "removenotif.php",
    data: {notifid: notifid},
    success: function(data){
      console.log(data);
      location.href = "/index.php";
    }
  });
}

function removenotif2(notifid){
  $.ajax({
    method: "POST",
    url: "removenotif2.php",
    data: {notifid: notifid},
    success: function(data){
      console.log(data);
      location.href = "/index.php";
    }
  });
}