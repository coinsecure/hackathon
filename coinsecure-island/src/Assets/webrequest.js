#pragma strict

import SimpleJSON;

public var url: String = "https://api.coinsecure.in/v0/noauth/ticker";

function Start () {
	Debug.Log("Initializing..");
	// Every 2 mins
	InvokeRepeating("GetDataOnInterval", 0, 120);
}


function Update () {

}

function GetDataOnInterval() {
	GetDataFromServer();
}

function GetDataFromServer() {

  Debug.Log("Invoked..");
  var www : WWW = new WWW (url);

  yield www;

  if (www.error == null) { 
   Debug.Log("WWW OK: " + www.text);
   
   var mydata = JSON.Parse(www.text);
   
   Debug.Log("Server data" + mydata);
   
   var highestBid = parseInt(mydata["result"][4]["highestBid"]) / 100;
   var lowestAsk  = parseInt(mydata["result"][5]["lowestAsk"]) / 100;
   
   
   var finalString = "Highest Bid : " + highestBid + " Rs \nLowest Ask : " + lowestAsk + " Rs";
   
   Debug.Log(finalString);   
   
   var playerObject : GameObject = GameObject.FindWithTag("Player");   	
   	   	
    
    var textMesh: TextMesh = new TextMesh();
    textMesh = playerObject.GetComponent(TextMesh);
    
    textMesh.text = finalString;
  
   Debug.Log(finalString);
   
  
   
  } else { 
   Debug.Log("WWW Error: "+ www.error); 
  } 
}



