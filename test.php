

<?php
/* header('Content-Type: text/json; charset=utf-8); */
$request_limit_num = 20;
$db_table_name = "pages_table_test";
$db_lock_column = "ad_NR";

$stdout = fopen("php://stdout","w");
@ $db = new mysqli('127.0.0.1', 'root', 'root0717', 'trainingDB_3');
if (mysqli_connect_errno()) {
    echo 'Error: Could not connect to database.  Please try again later.';
    exit;
}

$query = "select page_id, page_url from $db_table_name where tag IS NULL and $db_lock_column <> 1 limit $request_limit_num";
$break_lock_query = "select page_id, page_url from $db_table_name where tag IS NULL limit $request_limit_num";

/* lock these page so that another one would not repeatedly get this */
$lock_update_prepare = $db->prepare("update $db_table_name set $db_lock_column = 1 where page_id = ?");
fwrite($stdout, "get here 1\n");
$lock_update_prepare->bind_param('i', $pgid) or fwrite($stdout, "bind_param() fail");
fwrite($stdout, "get here 2\n");

$result = $db->query($query);
$num_results = $result->num_rows;
/* there maybe row that have been visited but not tagged */
if(!$num_results){
    $result = $db->query($break_lock_query);
    $num_results = $result->num_rows;
}
fwrite($stdout, "get here 3\n");

$arr = array();
for($i=0;$i<$num_results;$i++){
    $row = $result->fetch_assoc();
    $pgid = $row['page_id'];
    $arr[$pgid] = $row['page_url'];
    //$lock_update_prepare.execute();
}
echo json_encode($arr);
$db->close();
?>
