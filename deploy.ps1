Write-Host "Stopping Tomcat"
stop-service Tomcat9
Write-Host "Removing old class file directory"
remove-item -Path .\WEB-INF\classes\* -Recurse
Write-Host "Adding current target class file directory"
Copy-Item -Path .\fridgeinv\target\classes\* -Destination .\WEB-INF\classes -Recurse
Write-Host "Starting Tomcat"
Start-Service Tomcat9