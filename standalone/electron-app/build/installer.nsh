; Shop Manager NSIS Installer Script
; Custom installation steps and checks

!macro customHeader
  ; Add custom installer header
!macroend

!macro preInit
  ; Pre-initialization checks
  SetRegView 64
  WriteRegExpandStr HKLM "${INSTALL_REGISTRY_KEY}" InstallLocation "$ INSTDIR"
  WriteRegExpandStr HKCU "${INSTALL_REGISTRY_KEY}" InstallLocation "$INSTDIR"
  SetRegView 32
  WriteRegExpandStr HKLM "${INSTALL_REGISTRY_KEY}" InstallLocation "$INSTDIR"
  WriteRegExpandStr HKCU "${INSTALL_REGISTRY_KEY}" InstallLocation "$INSTDIR"
!macroend

!macro customInstall
  ; Custom installation steps
  DetailPrint "Installing Shop Manager..."

  ; Create desktop shortcut if selected
  ${if} $isForceCurrentInstall == "true"
    DetailPrint "Installing for current user only"
  ${else}
    DetailPrint "Installing for all users"
  ${endif}
!macroend

!macro customUnInstall
  ; Custom uninstallation steps
  DetailPrint "Uninstalling Shop Manager..."

  ; Remove user data (optional - commented out by default)
  ; RMDir /r "$APPDATA\shop-manager-desktop"
!macroend

!macro customInit
  ; Custom initialization
!macroend

!macro customInstallMode
  ; Set install mode
!macroend
