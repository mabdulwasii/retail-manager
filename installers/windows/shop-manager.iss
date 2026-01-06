; ============================================================================
; Shop Manager - Windows Installer Script (Inno Setup)
; ============================================================================
; This script creates a Windows installer (.exe) for Shop Manager Embedded
; ============================================================================

; Allow version to be passed as command-line parameter
#ifndef MyAppVersion
  #define MyAppVersion "1.0.0"
#endif
#ifndef JarVersion
  #define JarVersion "1.0.0-SNAPSHOT"
#endif

#define MyAppName "Shop Manager"
#define MyAppPublisher "Princely Software"
#define MyAppURL "https://github.com/yourorg/shop-manager"
#define MyAppExeName "shop-manager.bat"
#define JarFileName "shop-manager-" + JarVersion + "-embedded.jar"

[Setup]
; App identification
AppId={{8F7B3C5D-9E2A-4B1C-8D6E-5F4A3B2C1D0E}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppPublisher={#MyAppPublisher}
AppPublisherURL={#MyAppURL}
AppSupportURL={#MyAppURL}
AppUpdatesURL={#MyAppURL}

; Installation directories
DefaultDirName={autopf}\{#MyAppName}
DefaultGroupName={#MyAppName}
DisableProgramGroupPage=yes

; Output
OutputDir=..\..\build\installers\windows
OutputBaseFilename=shop-manager-{#MyAppVersion}-windows-x64-setup
; SetupIconFile=assets\shop-manager.ico
UninstallDisplayIcon={app}\{#MyAppExeName}

; Compression
Compression=lzma2
SolidCompression=yes

; Windows version requirements
MinVersion=10.0.17763
ArchitecturesAllowed=x64
ArchitecturesInstallIn64BitMode=x64

; Privileges
PrivilegesRequired=admin
PrivilegesRequiredOverridesAllowed=dialog

; UI
WizardStyle=modern
; WizardImageFile=assets\wizard-image.bmp
; WizardSmallImageFile=assets\wizard-small.bmp

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"
Name: "quicklaunchicon"; Description: "{cm:CreateQuickLaunchIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked
Name: "autostart"; Description: "Start {#MyAppName} automatically when Windows starts"; GroupDescription: "Startup Options:"; Flags: unchecked

[Files]
; Embedded JAR
Source: "..\..\backend\target\{#JarFileName}"; DestDir: "{app}\lib"; Flags: ignoreversion

; Launcher scripts
Source: "scripts\shop-manager.bat"; DestDir: "{app}"; Flags: ignoreversion
Source: "scripts\shop-manager-console.bat"; DestDir: "{app}"; Flags: ignoreversion
Source: "scripts\install-service.bat"; DestDir: "{app}"; Flags: ignoreversion
Source: "scripts\uninstall-service.bat"; DestDir: "{app}"; Flags: ignoreversion

; Configuration templates
Source: "config\.env.template"; DestDir: "{app}\config"; Flags: ignoreversion onlyifdoesntexist
Source: "config\application.yml"; DestDir: "{app}\config"; Flags: ignoreversion onlyifdoesntexist

; Documentation
Source: "..\..\docs\EMBEDDED_DEPLOYMENT.md"; DestDir: "{app}\docs"; Flags: ignoreversion
Source: "..\..\docs\CLOUD_SYNC_SETUP.md"; DestDir: "{app}\docs"; Flags: ignoreversion
Source: "..\..\README.md"; DestDir: "{app}\docs"; Flags: ignoreversion
; Source: "..\..\LICENSE"; DestDir: "{app}"; Flags: ignoreversion

; Assets
; Source: "assets\shop-manager.ico"; DestDir: "{app}\assets"; Flags: ignoreversion

[Dirs]
Name: "{app}\data"; Permissions: users-modify
Name: "{app}\data\h2"; Permissions: users-modify
Name: "{app}\data\uploads"; Permissions: users-modify
Name: "{app}\data\logs"; Permissions: users-modify
Name: "{app}\data\backups"; Permissions: users-modify
Name: "{app}\config"; Permissions: users-modify

[Icons]
Name: "{group}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; IconFilename: "{app}\assets\shop-manager.ico"
Name: "{group}\{#MyAppName} (Console)"; Filename: "{app}\shop-manager-console.bat"; IconFilename: "{app}\assets\shop-manager.ico"
Name: "{group}\Configuration"; Filename: "notepad.exe"; Parameters: "{app}\config\.env"; IconFilename: "{sys}\shell32.dll"; IconIndex: 70
Name: "{group}\Data Folder"; Filename: "{app}\data"; IconFilename: "{sys}\shell32.dll"; IconIndex: 3
Name: "{group}\Documentation"; Filename: "{app}\docs"; IconFilename: "{sys}\shell32.dll"; IconIndex: 23
Name: "{group}\{cm:UninstallProgram,{#MyAppName}}"; Filename: "{uninstallexe}"

Name: "{autodesktop}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; IconFilename: "{app}\assets\shop-manager.ico"; Tasks: desktopicon
Name: "{userappdata}\Microsoft\Internet Explorer\Quick Launch\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; IconFilename: "{app}\assets\shop-manager.ico"; Tasks: quicklaunchicon

[Registry]
; Add to startup if selected
Root: HKCU; Subkey: "Software\Microsoft\Windows\CurrentVersion\Run"; ValueType: string; ValueName: "{#MyAppName}"; ValueData: """{app}\{#MyAppExeName}"""; Tasks: autostart

; Register application
Root: HKCU; Subkey: "Software\{#MyAppPublisher}\{#MyAppName}"; ValueType: string; ValueName: "InstallPath"; ValueData: "{app}"; Flags: uninsdeletekey
Root: HKCU; Subkey: "Software\{#MyAppPublisher}\{#MyAppName}"; ValueType: string; ValueName: "Version"; ValueData: "{#MyAppVersion}"

[Run]
; Open browser after installation
Filename: "http://localhost:8081/actuator/health"; Description: "Open {#MyAppName} health check"; Flags: shellexec postinstall skipifsilent

; Offer to start application
Filename: "{app}\{#MyAppExeName}"; Description: "Launch {#MyAppName}"; Flags: nowait postinstall skipifsilent

[UninstallRun]
; Stop service if installed
Filename: "{app}\uninstall-service.bat"; RunOnceId: "StopService"; Flags: runhidden

[Code]
var
  JavaVersionPage: TInputOptionWizardPage;
  EnvConfigPage: TInputQueryWizardPage;
  CloudSyncPage: TInputOptionWizardPage;
  CloudApiKeyPage: TInputQueryWizardPage;
  JavaFound: Boolean;
  JavaVersion: String;
  JavaPath: String;

// Check if Java 21+ is installed
function CheckJavaVersion(): Boolean;
var
  ResultCode: Integer;
  JavaOutput: AnsiString;
  TempFile: String;
begin
  Result := False;
  TempFile := ExpandConstant('{tmp}\java-version.txt');

  if Exec('cmd.exe', '/c java -version 2>&1 | findstr "version" > "' + TempFile + '"', '', SW_HIDE, ewWaitUntilTerminated, ResultCode) then
  begin
    if LoadStringFromFile(TempFile, JavaOutput) then
    begin
      if (Pos('21.', String(JavaOutput)) > 0) or
         (Pos('22.', String(JavaOutput)) > 0) or
         (Pos('23.', String(JavaOutput)) > 0) then
      begin
        Result := True;
        JavaVersion := String(JavaOutput);
      end;
    end;
  end;

  DeleteFile(TempFile);
end;

procedure InitializeWizard;
begin
  // Check Java installation
  JavaFound := CheckJavaVersion();

  // Java version page
  JavaVersionPage := CreateInputOptionPage(wpWelcome,
    'Java Runtime Environment',
    'Shop Manager requires Java 21 or higher to run.',
    'Please select an option:',
    True, False);

  if JavaFound then
  begin
    JavaVersionPage.Add('Java ' + JavaVersion + ' detected - Continue with installation');
    JavaVersionPage.Values[0] := True;
  end
  else
  begin
    JavaVersionPage.Add('Download and install Java 21 (recommended)');
    JavaVersionPage.Add('I will install Java manually later');
    JavaVersionPage.Values[0] := True;
  end;

  // Environment configuration page
  EnvConfigPage := CreateInputQueryPage(JavaVersionPage.ID,
    'Initial Configuration',
    'Configure basic settings for Shop Manager',
    'You can change these settings later by editing the .env file in the config folder.');

  EnvConfigPage.Add('Shop Hostname (use .local for mDNS discovery):', False);
  EnvConfigPage.Values[0] := 'shopmanager.local';

  EnvConfigPage.Add('Shop Name:', False);
  EnvConfigPage.Values[1] := 'Shop Manager';

  EnvConfigPage.Add('Backend Port:', False);
  EnvConfigPage.Values[2] := '8081';

  EnvConfigPage.Add('Frontend Port:', False);
  EnvConfigPage.Values[3] := '3001';

  EnvConfigPage.Add('Generate JWT Secret automatically', True);
  EnvConfigPage.Values[4] := 'Yes';

  // Cloud Sync configuration page
  CloudSyncPage := CreateInputOptionPage(EnvConfigPage.ID,
    'Cloud Sync Configuration',
    'Optional: Connect to cloud aggregator for multi-shop analytics',
    'Cloud sync allows you to aggregate data from multiple shops. You can configure this later in the application settings.',
    True, False);

  CloudSyncPage.Add('Do not enable cloud sync (run standalone)');
  CloudSyncPage.Add('Register new cloud account (auto-generate API key)');
  CloudSyncPage.Add('Use existing API key (link to existing cloud account)');
  CloudSyncPage.Values[0] := True;  // Default to standalone

  // Cloud API Key page (shown only if "Use existing API key" is selected)
  CloudApiKeyPage := CreateInputQueryPage(CloudSyncPage.ID,
    'Cloud API Key',
    'Enter your existing cloud API key',
    'If you already have a cloud account, enter your API key below. You can find this in your cloud account settings.');

  CloudApiKeyPage.Add('API Key (format: rhq_...):', False);
  CloudApiKeyPage.Values[0] := '';

  CloudApiKeyPage.Add('Cloud API URL (optional, leave blank for default):', False);
  CloudApiKeyPage.Values[1] := 'https://api.retailhq.app';
end;

function ShouldSkipPage(PageID: Integer): Boolean;
begin
  Result := False;

  // Skip CloudApiKeyPage unless "Use existing API key" is selected
  if PageID = CloudApiKeyPage.ID then
  begin
    Result := not CloudSyncPage.Values[2];  // Show only if option 2 (existing API key) is selected
  end;
end;

function NextButtonClick(CurPageID: Integer): Boolean;
var
  ErrorCode: Integer;
begin
  Result := True;

  if CurPageID = JavaVersionPage.ID then
  begin
    if not JavaFound and JavaVersionPage.Values[0] then
    begin
      // Open Java download page
      ShellExec('open', 'https://adoptium.net/temurin/releases/?version=21', '', '', SW_SHOW, ewNoWait, ErrorCode);
      Result := False;
      MsgBox('Please download and install Java 21, then restart this installer.', mbInformation, MB_OK);
    end;
  end;

  // Validate API key format if using existing key
  if CurPageID = CloudApiKeyPage.ID then
  begin
    if CloudApiKeyPage.Values[0] <> '' then
    begin
      if Pos('rhq_', CloudApiKeyPage.Values[0]) <> 1 then
      begin
        MsgBox('Invalid API key format. API keys must start with "rhq_".', mbError, MB_OK);
        Result := False;
      end;
    end else begin
      MsgBox('Please enter an API key or go back and select a different option.', mbError, MB_OK);
      Result := False;
    end;
  end;
end;

procedure CurStepChanged(CurStep: TSetupStep);
var
  EnvFile: String;
  EnvContent: TArrayOfString;
  ResultCode: Integer;
  JwtSecret: AnsiString;
  TempFile: String;
begin
  if CurStep = ssPostInstall then
  begin
    // Create .env file from template
    EnvFile := ExpandConstant('{app}\config\.env');

    if not FileExists(EnvFile) then
    begin
      LoadStringsFromFile(ExpandConstant('{app}\config\.env.template'), EnvContent);

      // Set hostname and shop name
      StringChangeEx(EnvContent[7], 'SHOP_HOSTNAME=shopmanager.local', 'SHOP_HOSTNAME=' + EnvConfigPage.Values[0], True);
      StringChangeEx(EnvContent[8], 'SHOP_NAME=Shop Manager', 'SHOP_NAME=' + EnvConfigPage.Values[1], True);

      // Set ports
      StringChangeEx(EnvContent[9], 'BACKEND_PORT=8081', 'BACKEND_PORT=' + EnvConfigPage.Values[2], True);
      StringChangeEx(EnvContent[10], 'FRONTEND_PORT=3001', 'FRONTEND_PORT=' + EnvConfigPage.Values[3], True);

      // Generate JWT secret if requested
      if EnvConfigPage.Values[4] = 'Yes' then
      begin
        TempFile := ExpandConstant('{tmp}\jwt-secret.txt');
        if Exec('powershell.exe', '-Command "[Convert]::ToBase64String((1..64 | ForEach-Object { Get-Random -Maximum 256 })) | Out-File -FilePath ''' + TempFile + ''' -Encoding ASCII -NoNewline"', '', SW_HIDE, ewWaitUntilTerminated, ResultCode) then
        begin
          if LoadStringFromFile(TempFile, JwtSecret) then
          begin
            StringChangeEx(EnvContent[23], 'JWT_SECRET=REPLACE_WITH_SECURE_RANDOM_SECRET', 'JWT_SECRET=' + JwtSecret, True);
          end;
          DeleteFile(TempFile);
        end;
      end;

      // Configure cloud sync based on user selection
      if CloudSyncPage.Values[1] then
      begin
        // Option 1: Register new account (will be handled by application on first run)
        StringChangeEx(EnvContent[33], 'CLOUD_SYNC_REQUIRED=false', 'CLOUD_SYNC_REQUIRED=true', True);
        StringChangeEx(EnvContent[34], 'CLOUD_API_KEY=', 'CLOUD_API_KEY=AUTO_REGISTER', True);
      end
      else if CloudSyncPage.Values[2] then
      begin
        // Option 2: Use existing API key
        StringChangeEx(EnvContent[33], 'CLOUD_SYNC_REQUIRED=false', 'CLOUD_SYNC_REQUIRED=true', True);
        StringChangeEx(EnvContent[34], 'CLOUD_API_KEY=', 'CLOUD_API_KEY=' + CloudApiKeyPage.Values[0], True);

        // Update cloud API URL if provided
        if CloudApiKeyPage.Values[1] <> '' then
        begin
          StringChangeEx(EnvContent[32], 'CLOUD_REGISTRATION_URL=https://api.retailhq.app',
            'CLOUD_REGISTRATION_URL=' + CloudApiKeyPage.Values[1], True);
        end;
      end;
      // Option 0: Standalone mode (no changes needed, defaults are fine)

      SaveStringsToFile(EnvFile, EnvContent, False);
    end;
  end;
end;

[UninstallDelete]
Type: filesandordirs; Name: "{app}\data\logs"
Type: files; Name: "{app}\config\.env.backup.*"
