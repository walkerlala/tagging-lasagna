<?php
    $stdout = fopen('php://stdout', 'w');
    $temp = $_POST['result'];
    var_dump("what");
    var_dump($_POST);
    fwrite($stdout, "From temp.php:\n");
    fwrite($stdout, $temp);
    fwrite($stdout, "END from temp.php:\n");
?>
