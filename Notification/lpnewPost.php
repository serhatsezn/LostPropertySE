<?php

require "init.php";
$UserName = $_POST['userName'];
$TokenDevice = $_POST['tokendevice'];
$bildirimpost = $_POST['bildirimPost'];
$Post_type = $_POST['post_type'];
$Post_key = $_POST['post_key'];

$path_to_fcm = 'https://fcm.googleapis.com/fcm/send';
$server_key = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";

$headers = array(
			'Authorization:key ='.$server_key,
			'Content-Type:application/json',
		);

$fields = array(
        'registration_ids' =>  [$TokenDevice],
         'notification' => array('title' => $UserName, 
         'body' => $bildirimpost,
         'subtitle'      => $bildirimpost,
         'smallIcon'	=> 'ic',
         'click_action' => 'NewPost',
         'sound' => "default"),
         'data' => array('userName' =>$UserName,
         'msg' => "Çevrenden yeni bir paylaşım var!",
         'post_type' =>$Post_type,
         'post_key'	=> $Post_key,
         'title' => $bildirimpost,
         'click_action' =>'NewPost')
        );
        
$payload = json_encode($fields);

$ch = curl_init();
curl_setopt($ch, CURLOPT_URL, $path_to_fcm);
curl_setopt($ch, CURLOPT_POST, true);
curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, 0);  
curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
curl_setopt($ch, CURLOPT_IPRESOLVE, CURL_IPRESOLVE_V4);
curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($fields));	

$result = curl_exec($ch);   
curl_close($ch);
$clos = mysqli_close($con);
if($clos)
	echo "Closed";
else
	echo "Error Closed";
?>