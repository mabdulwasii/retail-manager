; Shop Manager NSIS Installer Script
; Custom installation steps and checks

!macro customHeader
  ; Add custom installer header
!macroend

!macro preInit
  ; Pre-initialization - keep it simple to avoid NSIS errors
  DetailPrint "Initializing Shop Manager installer..."
!macroend

!macro customInstall
  ; Custom installation steps
  DetailPrint "Installing Shop Manager..."
!macroend

!macro customUnInstall
  ; Custom uninstallation steps
  DetailPrint "Uninstalling Shop Manager..."
!macroend

!macro customInit
  ; Custom initialization
!macroend

!macro customInstallMode
  ; Set install mode
!macroend
