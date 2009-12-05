<?php
/* include the PHP Facebook Client Library to help
  with the API calls and make life easy */
require_once('facebook/php/facebook.php');
/* initialize the facebook API with your application API Key
  and Secret */
$facebook = new Facebook('e7d3ed1695c713a1d57d015b654d1923','8a12e2d2fe95e99f1e9c6703d073f2d8');

/* require the user to be logged into Facebook before
  using the application. If they are not logged in they
  will first be directed to a Facebook login page and then
  back to the application's page. require_login() returns
  the user's unique ID which we will store in fb_user */
$fb_user = $facebook->require_login();

/* now we will say:
  Hello USER_NAME! Welcome to my first application! */

$user_details = $facebook->api_client->users_getInfo($fb_user, 'first_name, pic_square'); 
$firstName = $user_details[0]['first_name'];
$pic_square = $user_details[0]['pic_square'];

$wtf = 'wtf';

//echo '<pre>';
//print_r($user_details);
//echo '</pre>';
?>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"> 
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:fb="http://www.facebook.com/2008/fbml"> 
<head>
<title>Java Pacman</title>
<body>
<script type="text/javascript">
// Javascript popup window code
function newPopup(url) {
	popupWindow = window.open(
		url,'popUpWindow','height=500,width=250,left=10,top=10,resizable=yes,scrollbars=yes,toolbar=yes,menubar=no,location=no,directories=no,status=yes')
}
</script>

Hello <?php echo $firstName; ?>! Welcome to Pacman M.D.!
<a href="JavaScript:newPopup('http://www.greentealatte.net/highscore.php?action=list&access_code=1234');">Check your highscore here!</a>

<APPLET CODE="PacMan.class" WIDTH=600 HEIGHT=500>
<param name=username value="<?php echo $fb_user; ?>">
<param name=firstname value="<?php echo $firstName; ?>">
<param name=pic_url value="<?php echo $pic_square; ?>">


<script type="text/javascript"> 
FB_RequireFeatures(["XFBML"], function(){ 
FB.Facebook.init("e7d3ed1695c713a1d57d015b654d1923", "PacMan_ubc/php/xd_receiver.htm"); }); 
</script> 
</APPLET>

</body>