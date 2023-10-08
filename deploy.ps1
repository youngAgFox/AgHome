Write-Host "Stopping Tomcat"
stop-service Tomcat9
Write-Host "Removing old class file directory"
remove-item -Path "c:\Program Files\Apache Software Foundation\Tomcat 9.0\webapps\ROOT\WEB-INF\classes\*" -Recurse
Write-Host "Adding current target class file directory"
Copy-Item -Path "c:\Program Files\Apache Software Foundation\Tomcat 9.0\webapps\ROOT\fridgeinv\target\classes\*" -Destination "c:\Program Files\Apache Software Foundation\Tomcat 9.0\webapps\ROOT\WEB-INF\classes" -Recurse
Write-Host "Starting Tomcat"
Start-Service Tomcat9