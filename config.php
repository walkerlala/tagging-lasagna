<?php
if (!get_magic_quotes_gpc()) {
    if (isset($_POST)) {
        foreach ($_POST as $key => $value) {
            $_POST[$key] =  trim(addslashes($value));
        }
    }
}
?>
