
  <?php
    //header('Content-Type: text/json; charset=utf-8'); 
    $request_limit_num = 20;
    $db_table_name = "pages_table_test";
    $db_lock_column = "ad_NR";

    $stdout = fopen("php://stdout","w");
  	@ $db = new mysqli('127.0.0.1', 'root', 'root0717', 'trainingDB_3');
  	if (mysqli_connect_errno()) {
  	   echo 'Error: Could not connect to database.  Please try again later.';
   	   exit;
   	}
    /* change charset of db connection to utf8 */
    if (!$db->set_charset("utf8")) {
        //printf("Error loading character set utf8: %s\n", $db->error);
        fwrite($stdout, "cannot set db connection to utf8");
    } else {
        //printf("Current character set: %s\n", $db->character_set_name());
        fwrite($stdout, "successfully set db connection charset to utf-8");
    }	

    if($_SERVER['REQUEST_METHOD'] === 'GET'){
        $query = "select page_id, page_url from $db_table_name where tag IS NULL and $db_lock_column <> 1 limit $request_limit_num";
        $break_lock_query = "select page_id, page_url from $db_table_name where tag IS NULL limit $request_limit_num";

        /* lock these page so that another one would not repeatedly get this */
        $lock_update_prepare = $db->prepare("update $db_table_name set $db_lock_column = 1 where page_id = ?");
        $lock_update_prepare->bind_param('i', $pgid);

        $result = $db->query($query);
        $num_results = $result->num_rows;
        /* there maybe row that have been visited but not tagged */
        if(!$num_results){
            $result = $db->query($break_lock_query);
            $num_results = $result->num_rows;
        }

        $arr = array();
        for($i=0;$i<$num_results;$i++){
            $row = $result->fetch_assoc();
            $pgid = $row['page_id'];
            $arr[$pgid] = $row['page_url'];
            $lock_update_prepare->execute();
        }
        echo json_encode($arr);
    }else if($_SERVER['REQUEST_METHOD'] === 'POST'){
        $statement = $db->prepare("UPDATE $db_table_name SET tag = ? where page_id = ?");
        $statement->bind_param('si', $ptag, $pid);

        $post_val = json_decode($_POST['result']);

        foreach($post_val as $pid=>$ptag) {
            $ptag = urldecode($ptag);
            fwrite($stdout, $ptag);
            fwrite($stdout, "\n");
            $statement->execute();
        }
        $statement->close();
    }
    $db->close();
?>
