<?php 

include '../includes/config.php';
// $data = array("apiKey" => "er5i6BK9nwgNTTY5a6jwanHWWUBvyjPHFd4xWGyJ");                                                                    
// $data_string = json_encode($data);                                                                                   
                                                                                                                     
// $ch = curl_init('https://api.coinsecureis.cool/v0/auth/getcoinaddresses');                                                                      
// curl_setopt($ch, CURLOPT_CUSTOMREQUEST, "POST");                                                                     
// curl_setopt($ch, CURLOPT_POSTFIELDS, $data_string);                                                                  
// curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);                                                                      
// curl_setopt($ch, CURLOPT_HTTPHEADER, array(                                                                          
//     'Content-Type: application/json',                                                                                
//     'Content-Length: ' . strlen($data_string))                                                                       
// );                                                                                                                   
                                                                                                                     
// $result = curl_exec($ch);

// echo $result;
// print_r($result);

$x = getbitcoinrate();
$x = $x->result[0];
print_r($x);
?>