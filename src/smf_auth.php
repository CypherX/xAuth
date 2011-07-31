<?php
// this script is tested with SMF 2.X

/* The format is pretty simple, and always returns exactly 2 lines.

if successful, return this:

YES
forum_name

if not successful, return this:

ERROR
String to return to user describing error

*/

// $localaddr should be the IP your webserver is listening on, if this page isn't being visited by the same IP ($_SERVER['REMOTE_ADDR'])
// then errors are logged and a warning email is sent to the email configured in done() so no one tries to use this to bruteforce
// passwords, you really should just restrict this to only the server accessing it, I only make it accessible over localhost or to
// my home address over SSL only.
$localaddr = "127.0.0.1";
if($_SERVER['REMOTE_ADDR'] != $localaddr && $_SERVER['REMOTE_ADDR'] != gethostbyname('an.allowed.hostname') && $_SERVER['REMOTE_ADDR'] != '192.168.1.212' ) die("Access Denied!");

function writeToFile($message, $fname = 'auth.log', $mode = 'a'){
	$fp = fopen($fname, $mode);
	fwrite($fp, time().': '.$message."\n");
	fclose($fp);
}

function done($msg, $template = "ERROR\n%s"){
	printf($template, $msg);
	global $localaddr;
	if($_SERVER['REMOTE_ADDR'] != $localaddr){
		$result = sprintf(str_replace("\n", ", ", $template), $msg);
		writeToFile("result: ".$result);
		// only if it's a bad pass, text me
		if(strpos($msg, 'assword') === FALSE)
			exit;
		$to = "YOUR_EMAIL_ADDRESS_IF_REQUIRED";
		$subject = "auth alert";
		$message .= $result."\n";
		$message .= $_SERVER['REMOTE_ADDR']." user: ".$_REQUEST['user'].", field: ".$_REQUEST['field'].", pass length: ".strlen($_REQUEST['pass']);
		$from = "EMAIL_TO_SEND_FROM";
		$headers = "From: $from";
		$sendmail_params = "-f $from -r $from";
		writeToFile("mail sent: ".(mail($to,$subject,$message,$headers, $sendmail_params) ? 'true' : 'false'));
	}
	exit;
}

if(($_SERVER['REMOTE_ADDR'] != $localaddr && !isset($_SERVER['HTTPS']))
	|| !isset($_REQUEST['pass']) || !isset($_REQUEST['user']) || !isset($_REQUEST['field'])
	|| ($_REQUEST['field'] != 'minecra'))
     die("Access Denied!");

$user = $_REQUEST['user'];
$pass = $_REQUEST['pass'];
$field = 'cust_'.$_REQUEST['field'];

if($_SERVER['REMOTE_ADDR'] != $localaddr)
	writeToFile($_SERVER['REMOTE_ADDR']." user: $user, field: $field, pass length: ".strlen($pass));

$db_server = 'localhost';
$db_name = 'smf';
$db_user = 'smfadmin';
$db_passwd = 'your_db_pass';
$db_prefix = 'smf_';

$mysqli = new mysqli($db_server, $db_user, $db_passwd, $db_name);

$stmt = $mysqli->prepare("SELECT `member_name`, `passwd`, `real_name` FROM `smf_members` WHERE `is_activated` = '1' AND `id_member` = (SELECT `id_member` FROM `smf_themes` WHERE `value` = ? AND `variable` = ?) LIMIT 1") or done('MySQL Error');
$stmt->bind_param("ss", $user, $field);
$stmt->execute();
// bind result variables
$stmt->bind_result($member_name, $pass_hash, $display_name);
$success = $stmt->fetch();
$stmt->close();
$mysqli->close();

if(!$success)
     done('Name not registered, must put in profile on forum: URL_TO_YOUR_FORUM');

// hash password
$sha_passwd = sha1(strtolower($member_name) . htmlspecialchars_decode($pass));

if($sha_passwd != $pass_hash)
     done('Incorrect Password, make sure you use your forum password.');

done($display_name, "YES\n%s");
?>