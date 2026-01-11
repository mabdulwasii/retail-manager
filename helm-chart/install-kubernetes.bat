@echo off
REM ============================================================================
REM install-kubernetes.bat - Windows Launcher for Kubernetes Installation
REM ============================================================================
REM This batch file makes it easy to run the PowerShell installation script
REM Double-click this file to start the installation process
REM ============================================================================

echo.
echo ============================================================================
echo Shop Manager - Kubernetes Installation
echo ============================================================================
echo.
echo Starting PowerShell installation script...
echo.

REM Run PowerShell script with bypass execution policy for this session only
powershell.exe -ExecutionPolicy Bypass -NoExit -File "%~dp0install-kubernetes.ps1"

REM Note: -NoExit keeps the PowerShell window open after completion
REM The script itself also has pauses for user to review output
