@echo off
cd /d C:\dev\kofiu\kofiu-monitor
java -jar target/kofiu-monitor-1.0-SNAPSHOT-jar-with-dependencies.jar >> log.txt 2>&1
