<html>
<body>
<?php
$GLOBALS["access_code"] = "1234"; // application must provide this number for security
$GLOBALS["db_name"] = "greentea_scoretest"; // name of the database
$GLOBALS["table_name"] = "highscore1"; // name of the table in the database
$GLOBALS["view_user"] = "greentea_pacman"; // public access MySQL user
$GLOBALS["view_pass"] = "wakawaka"; // public password
$GLOBALS["db_error"] = 0;
$GLOBALS["db_link"] = null;
function is_error() {
	$result = ($GLOBALS["db_error"] != 0);
	return $result;
}
function error() {
	$GLOBALS["db_error"] = 1;
	print "Error: ".mysql_error()."\n";
}
function sql($q) {
	$result = @mysql_query($q) or error();
	return $result;
}
function select_db() {
	if (!mysql_select_db($GLOBALS["db_name"])) error();
}
function db_connect($user,$pass) {
	if ($GLOBALS["db_link"] != null) {
		@mysql_close($GLOBALS["db_link"]);
	}
	$GLOBALS["db_link"] = @mysql_connect("localhost",$user,$pass) or error();
	if (is_error()) {
		db_close();
		exit;
	}
}
function db_close() {
	if ($GLOBALS["db_link"] != null) {
		@mysql_close($GLOBALS["db_link"]);
	}
	print (is_error()?"0":"1");
	$GLOBALS["db_link"] = null;
}
function db_view() {
	db_connect($GLOBALS["view_user"],$GLOBALS["view_pass"]);
	select_db();
}
function db_store($u,$f,$p,$s,$user,$pass) {
	if ($p != "") {
		db_connect($user,$pass);
		select_db();
		sql("INSERT INTO `{$GLOBALS['table_name']}` (`picurl`,`firstname`,`name`,`score`) VALUES('{$u}','{$f}',{$p},{$s})");
		db_close();
		exit;
	}
}
function db_list() {
	db_view();
	$scores = sql("SELECT `picurl`,`firstname`,`name`, MAX(`score`) FROM `{$GLOBALS['table_name']}` group by `name` ORDER BY MAX(`score`) DESC LIMIT 10");
	$numrows = @mysql_num_rows($scores);
			echo "<h1>ScoreBoard</h1>";
			echo "| User Picture | "; echo "| User Name | "; echo "| User Score |"; echo "<br /><br />";

	for ($j = 0;$j < $numrows;$j++) {
		$row = mysql_fetch_row($scores);
		//$user_details = $facebook->api_client->users_getInfo($row[0], 'first_name'); 
		//$firstName = $user_details[0]['first_name'];
		//echo "<img src='$row[0]'>";
		//print "{$row[0]} {$row[1]} {$row[2]} {$row[3]}".($j == $numrows-1?"":"\n");
		//echo "<img src="http://profile.ak.fbcdn.net/v223/690/21/s502651732_540.jpg">"
		echo '<img src="' . $row[0] . '"></img>&nbsp;&nbsp;&nbsp;&nbsp;|&nbsp;'; echo $row[1].'   |   '; echo $row[3]; echo " points<br />";
		
	}	
	@mysql_free_result($scores) or error();
	@mysql_close($GLOBALS["db_link"]) or error();
	exit;
}
function db_install($user,$pass) {
	db_connect($user,$pass);
	sql("DROP DATABASE IF EXISTS `{$GLOBALS['db_name']}`");
	sql("CREATE DATABASE `{$GLOBALS['db_name']}`");
	select_db();
	sql("CREATE TABLE `{$GLOBALS['table_name']}` (`name` INT SUNSIGNED,`score` INT UNSIGNED)");
	db_close();
	exit;
}
function check_access() {
	if (!isset($_GET["access_code"])) {
		$GLOBALS["db_error"] = 1;
		db_close();
		exit;
	}
	if ($_GET["access_code"] != $GLOBALS["access_code"]) {
		$GLOBALS["db_error"] = 1;
		db_close();
		exit;
	}
}
if (isset($_GET["action"])) {
	check_access();
	if ($_GET["action"] == "install" && isset($_GET["admin_user"]) && isset($_GET["admin_pass"])) {
		db_install($_GET["admin_user"],$_GET["admin_pass"]);
	}
	else if ($_GET["action"] == "submit" && isset($_GET["admin_user"]) && isset($_GET["admin_pass"]) && isset($_GET["picurl"]) && isset($_GET["firstname"]) && isset($_GET["name"]) && isset($_GET["score"])) {
		db_store($_GET["picurl"],$_GET["firstname"],$_GET["name"],$_GET["score"],$_GET["admin_user"],$_GET["admin_pass"]);
	}
	else if ($_GET["action"] == "list") {
		db_list();
	}
}
?>
</html>
</body>