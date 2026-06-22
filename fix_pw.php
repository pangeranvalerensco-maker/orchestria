<?php
$hash = password_hash('AdminOrchestria123!', PASSWORD_BCRYPT);
file_put_contents('fix_pw3.sql', "UPDATE users SET password = '$hash';\n");
echo "Done";
