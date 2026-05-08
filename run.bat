@echo off
cd /d %~dp0
echo Starting BookStoreAI...
mvn clean spring-boot:run
pause
