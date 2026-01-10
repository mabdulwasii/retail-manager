@echo off
REM ============================================================================
REM lite-init.bat - Windows Launcher for Docker Compose Lite Setup
REM ============================================================================
REM This batch file makes it easy to run the PowerShell setup script
REM Double-click this file to start the setup process
REM ============================================================================

echo.
echo ============================================================================
echo Shop Manager - Docker Compose Lite Setup
echo ============================================================================
echo.
echo Starting PowerShell setup script...
echo.

REM Run PowerShell script with bypass execution policy for this session only
powershell.exe -ExecutionPolicy Bypass -NoExit -File "%~dp0lite-init.ps1"

REM Note: -NoExit keeps the PowerShell window open after completion
REM The script itself also has pauses for user to review output
