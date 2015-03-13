@ECHO
set PGPASSWORD=%1

%6 -U %2 -d %3 -c "CREATE EXTENSION postgis"
%7 -U %2 -d %3 -v %4 2> %5