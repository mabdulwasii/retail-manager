@echo off
REM ============================================================================
REM uninstall-kubernetes.bat - Windows Launcher for Kubernetes Uninstallation
REM ============================================================================
REM This batch file makes it easy to run the PowerShell uninstallation script
REM Double-click this file to start the uninstallation process
REM ============================================================================

echo.
echo ============================================================================
echo Shop Manager - Kubernetes Uninstallation
echo ============================================================================
echo.
echo Starting PowerShell uninstallation script...
echo.

REM Run PowerShell script with bypass execution policy for this session only
powershell.exe -ExecutionPolicy Bypass -NoExit -File "%~dp0uninstall-kubernetes.ps1"

REM Note: -NoExit keeps the PowerShell window open after completion
REM The script itself also has pauses for user to review output
