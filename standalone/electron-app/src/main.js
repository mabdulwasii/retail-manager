/**
 * Shop Manager Desktop Application
 * Main Electron Process
 */

const { app, BrowserWindow, ipcMain, Tray, Menu, dialog, shell } = require('electron');
const path = require('path');
const fs = require('fs');
const { spawn } = require('child_process');
const Store = require('electron-store');
const Docker = require('dockerode');
const { autoUpdater } = require('electron-updater');
const log = require('electron-log');

// Initialize electron-store for persistent settings
const store = new Store();

// Configure auto-updater logging
log.transports.file.level = 'info';
autoUpdater.logger = log;

// Global variables
let mainWindow = null;
let tray = null;
let dockerProcess = null;
const docker = new Docker();

// Application paths
const isDev = process.env.NODE_ENV === 'development';
const resourcesPath = isDev
  ? path.join(__dirname, '../../')
  : process.resourcesPath;

const configPath = path.join(resourcesPath, 'config.yaml');
const dockerComposePath = path.join(resourcesPath, 'docker-compose.yml');

/**
 * Auto-updater configuration and event handlers
 */
function setupAutoUpdater() {
  // Configure auto-updater (GitHub releases)
  autoUpdater.setFeedURL({
    provider: 'github',
    owner: 'yourorg',  // Update with actual GitHub org
    repo: 'shop-manager',
    private: false
  });

  // Don't automatically download updates
  autoUpdater.autoDownload = false;

  // Update available
  autoUpdater.on('update-available', (info) => {
    log.info('Update available:', info.version);

    dialog.showMessageBox(mainWindow, {
      type: 'info',
      title: 'Update Available',
      message: `A new version (${info.version}) is available!`,
      detail: 'Would you like to download it now?',
      buttons: ['Download', 'Later'],
      defaultId: 0,
      cancelId: 1
    }).then((result) => {
      if (result.response === 0) {
        autoUpdater.downloadUpdate();
        if (mainWindow) {
          mainWindow.webContents.send('update-downloading');
        }
      }
    });
  });

  // Update not available
  autoUpdater.on('update-not-available', () => {
    log.info('Update not available - running latest version');
  });

  // Download progress
  autoUpdater.on('download-progress', (progressObj) => {
    let message = `Download speed: ${progressObj.bytesPerSecond} - Downloaded ${progressObj.percent}%`;
    log.info(message);
    if (mainWindow) {
      mainWindow.webContents.send('update-download-progress', {
        percent: progressObj.percent,
        bytesPerSecond: progressObj.bytesPerSecond
      });
    }
  });

  // Update downloaded
  autoUpdater.on('update-downloaded', (info) => {
    log.info('Update downloaded');

    dialog.showMessageBox(mainWindow, {
      type: 'info',
      title: 'Update Ready',
      message: `Update to version ${info.version} has been downloaded.`,
      detail: 'The update will be installed when you restart the application.',
      buttons: ['Restart Now', 'Restart Later'],
      defaultId: 0,
      cancelId: 1
    }).then((result) => {
      if (result.response === 0) {
        autoUpdater.quitAndInstall(false, true);
      }
    });
  });

  // Error handler
  autoUpdater.on('error', (error) => {
    log.error('Auto-updater error:', error);
  });
}

/**
 * Check for updates manually (called by user)
 */
function checkForUpdates() {
  autoUpdater.checkForUpdates();
}

/**
 * Create main application window
 */
function createMainWindow() {
  mainWindow = new BrowserWindow({
    width: 1400,
    height: 900,
    minWidth: 1024,
    minHeight: 768,
    title: 'Shop Manager',
    icon: path.join(__dirname, '../assets/icon.png'),
    webPreferences: {
      nodeIntegration: false,
      contextIsolation: true,
      preload: path.join(__dirname, 'preload.js')
    },
    show: false  // Don't show until ready
  });

  // Load the appropriate page based on setup status
  const isSetupComplete = store.get('setupComplete', false);

  if (!isSetupComplete) {
    mainWindow.loadFile(path.join(__dirname, 'pages/setup.html'));
  } else {
    mainWindow.loadFile(path.join(__dirname, 'pages/dashboard.html'));
  }

  // Show window when ready
  mainWindow.once('ready-to-show', () => {
    mainWindow.show();

    // Auto-start services if configured
    const autoStart = store.get('autoStart', false);
    if (autoStart && isSetupComplete) {
      startServices();
    }
  });

  // Handle window close
  mainWindow.on('close', (event) => {
    const minimizeToTray = store.get('minimizeToTray', true);

    if (minimizeToTray && !app.isQuitting) {
      event.preventDefault();
      mainWindow.hide();
    }
  });

  mainWindow.on('closed', () => {
    mainWindow = null;
  });

  // Open external links in browser
  mainWindow.webContents.setWindowOpenHandler(({ url }) => {
    shell.openExternal(url);
    return { action: 'deny' };
  });
}

/**
 * Create system tray icon
 */
function createTray() {
  const trayIconPath = path.join(__dirname, '../assets/tray-icon.png');
  tray = new Tray(trayIconPath);

  const contextMenu = Menu.buildFromTemplate([
    {
      label: 'Show App',
      click: () => {
        if (mainWindow) {
          mainWindow.show();
        } else {
          createMainWindow();
        }
      }
    },
    { type: 'separator' },
    {
      label: 'Services',
      submenu: [
        {
          label: 'Start Services',
          click: startServices
        },
        {
          label: 'Stop Services',
          click: stopServices
        },
        {
          label: 'Restart Services',
          click: restartServices
        },
        { type: 'separator' },
        {
          label: 'View Logs',
          click: () => {
            if (mainWindow) {
              mainWindow.webContents.send('show-logs');
            }
          }
        }
      ]
    },
    {
      label: 'Quick Access',
      submenu: [
        {
          label: 'Open Application',
          click: () => {
            const domain = store.get('domain', 'localhost');
            shell.openExternal(`http://${domain}:3001`);
          }
        },
        {
          label: 'Open Keycloak',
          click: () => {
            const domain = store.get('domain', 'localhost');
            shell.openExternal(`http://${domain}:8080`);
          }
        },
        {
          label: 'API Documentation',
          click: () => {
            const domain = store.get('domain', 'localhost');
            shell.openExternal(`http://${domain}:8081/swagger-ui/index.html`);
          }
        }
      ]
    },
    { type: 'separator' },
    {
      label: 'Backup Now',
      click: createBackup
    },
    {
      label: 'Settings',
      click: () => {
        if (mainWindow) {
          mainWindow.webContents.send('show-settings');
        }
      }
    },
    { type: 'separator' },
    {
      label: 'Quit',
      click: () => {
        app.isQuitting = true;
        app.quit();
      }
    }
  ]);

  tray.setToolTip('Shop Manager');
  tray.setContextMenu(contextMenu);

  // Double-click to show window
  tray.on('double-click', () => {
    if (mainWindow) {
      mainWindow.show();
    } else {
      createMainWindow();
    }
  });
}

/**
 * Check if Docker is installed and running
 */
async function checkDocker() {
  try {
    await docker.ping();
    return { installed: true, running: true };
  } catch (error) {
    // Try to detect if Docker is installed but not running
    const isInstalled = await checkDockerInstalled();
    return { installed: isInstalled, running: false };
  }
}

/**
 * Check if Docker is installed (but possibly not running)
 */
function checkDockerInstalled() {
  return new Promise((resolve) => {
    const process = spawn('docker', ['--version']);
    process.on('close', (code) => {
      resolve(code === 0);
    });
    process.on('error', () => {
      resolve(false);
    });
  });
}

/**
 * Start Docker Compose services
 */
function startServices() {
  return new Promise((resolve, reject) => {
    const composeDir = path.dirname(dockerComposePath);

    dockerProcess = spawn('docker', ['compose', 'up', '-d'], {
      cwd: composeDir,
      shell: true
    });

    let output = '';

    dockerProcess.stdout.on('data', (data) => {
      output += data.toString();
      if (mainWindow) {
        mainWindow.webContents.send('service-log', data.toString());
      }
    });

    dockerProcess.stderr.on('data', (data) => {
      output += data.toString();
      if (mainWindow) {
        mainWindow.webContents.send('service-log', data.toString());
      }
    });

    dockerProcess.on('close', (code) => {
      if (code === 0) {
        updateTrayStatus('running');
        if (mainWindow) {
          mainWindow.webContents.send('services-started');
        }
        resolve();
      } else {
        reject(new Error(`Docker Compose exited with code ${code}`));
      }
    });
  });
}

/**
 * Stop Docker Compose services
 */
function stopServices() {
  return new Promise((resolve, reject) => {
    const composeDir = path.dirname(dockerComposePath);

    const stopProcess = spawn('docker', ['compose', 'down'], {
      cwd: composeDir,
      shell: true
    });

    stopProcess.on('close', (code) => {
      if (code === 0) {
        updateTrayStatus('stopped');
        if (mainWindow) {
          mainWindow.webContents.send('services-stopped');
        }
        resolve();
      } else {
        reject(new Error(`Docker Compose exited with code ${code}`));
      }
    });
  });
}

/**
 * Restart Docker Compose services
 */
async function restartServices() {
  await stopServices();
  await new Promise(resolve => setTimeout(resolve, 2000)); // Wait 2 seconds
  await startServices();
}

/**
 * Get service status
 */
async function getServiceStatus() {
  try {
    const containers = await docker.listContainers({ all: true });
    const shopManagerContainers = containers.filter(c =>
      c.Names.some(name => name.includes('shop-manager'))
    );

    return shopManagerContainers.map(c => ({
      name: c.Names[0].replace(/^\//, ''),
      status: c.State,
      image: c.Image,
      ports: c.Ports.map(p => `${p.PublicPort || p.PrivatePort}/${p.Type}`)
    }));
  } catch (error) {
    console.error('Error getting service status:', error);
    return [];
  }
}

/**
 * Update tray icon based on service status
 */
function updateTrayStatus(status) {
  if (!tray) return;

  const iconPath = status === 'running'
    ? path.join(__dirname, '../assets/tray-icon-active.png')
    : path.join(__dirname, '../assets/tray-icon.png');

  if (fs.existsSync(iconPath)) {
    tray.setImage(iconPath);
  }

  tray.setToolTip(`Shop Manager - ${status === 'running' ? 'Running' : 'Stopped'}`);
}

/**
 * Create database backup
 */
async function createBackup() {
  try {
    const backupDir = path.join(app.getPath('userData'), 'backups');

    if (!fs.existsSync(backupDir)) {
      fs.mkdirSync(backupDir, { recursive: true });
    }

    const timestamp = new Date().toISOString().replace(/[:.]/g, '-');
    const backupFile = path.join(backupDir, `backup-${timestamp}.sql`);

    const command = `docker compose exec -T postgres pg_dump -U shop shopdb > "${backupFile}"`;

    const backupProcess = spawn(command, {
      cwd: path.dirname(dockerComposePath),
      shell: true
    });

    backupProcess.on('close', (code) => {
      if (code === 0) {
        dialog.showMessageBox({
          type: 'info',
          title: 'Backup Complete',
          message: 'Database backup created successfully',
          detail: `Backup saved to:\n${backupFile}`
        });
      } else {
        dialog.showErrorBox('Backup Failed', 'Failed to create database backup');
      }
    });
  } catch (error) {
    dialog.showErrorBox('Backup Error', error.message);
  }
}

/**
 * IPC Handlers
 */
function setupIPCHandlers() {
  // Check Docker status
  ipcMain.handle('check-docker', async () => {
    return await checkDocker();
  });

  // Start services
  ipcMain.handle('start-services', async () => {
    try {
      await startServices();
      return { success: true };
    } catch (error) {
      return { success: false, error: error.message };
    }
  });

  // Stop services
  ipcMain.handle('stop-services', async () => {
    try {
      await stopServices();
      return { success: true };
    } catch (error) {
      return { success: false, error: error.message };
    }
  });

  // Restart services
  ipcMain.handle('restart-services', async () => {
    try {
      await restartServices();
      return { success: true };
    } catch (error) {
      return { success: false, error: error.message };
    }
  });

  // Get service status
  ipcMain.handle('get-service-status', async () => {
    return await getServiceStatus();
  });

  // Read configuration
  ipcMain.handle('read-config', () => {
    try {
      return fs.readFileSync(configPath, 'utf8');
    } catch (error) {
      return null;
    }
  });

  // Save configuration
  ipcMain.handle('save-config', (event, config) => {
    try {
      fs.writeFileSync(configPath, config, 'utf8');
      return { success: true };
    } catch (error) {
      return { success: false, error: error.message };
    }
  });

  // Generate configuration
  ipcMain.handle('generate-config', async () => {
    try {
      log.info('Generating configuration...');

      const generateConfig = require(path.join(resourcesPath, 'scripts/generate-config.js'));
      const result = await generateConfig(configPath);

      log.info('Configuration generated successfully');
      return {
        success: true,
        output: `Configuration generated successfully!\nOutput directory: ${result.outputDir}`
      };
    } catch (error) {
      log.error('Configuration generation failed:', error);
      return {
        success: false,
        error: error.message || 'Failed to generate configuration files'
      };
    }
  });

  // Create backup
  ipcMain.handle('create-backup', async () => {
    await createBackup();
  });

  // Open external URL
  ipcMain.handle('open-url', (event, url) => {
    shell.openExternal(url);
  });

  // Get app version
  ipcMain.handle('get-version', () => {
    return app.getVersion();
  });

  // Check for updates manually
  ipcMain.handle('check-for-updates', () => {
    checkForUpdates();
  });

  // Get settings
  ipcMain.handle('get-settings', () => {
    return {
      autoStart: store.get('autoStart', false),
      minimizeToTray: store.get('minimizeToTray', true),
      domain: store.get('domain', 'localhost')
    };
  });

  // Save settings
  ipcMain.handle('save-settings', (event, settings) => {
    Object.keys(settings).forEach(key => {
      store.set(key, settings[key]);
    });
    return { success: true };
  });

  // Mark setup as complete
  ipcMain.handle('complete-setup', () => {
    store.set('setupComplete', true);
    return { success: true };
  });
}

/**
 * App lifecycle
 */

// Single instance lock
const gotTheLock = app.requestSingleInstanceLock();

if (!gotTheLock) {
  app.quit();
} else {
  app.on('second-instance', () => {
    if (mainWindow) {
      if (mainWindow.isMinimized()) mainWindow.restore();
      mainWindow.focus();
    }
  });

  app.whenReady().then(() => {
    setupIPCHandlers();
    createMainWindow();
    createTray();
    setupAutoUpdater();

    // Check for updates on startup (after 10 seconds delay)
    setTimeout(() => {
      autoUpdater.checkForUpdates();
    }, 10000);

    // Check for updates periodically (every 24 hours)
    setInterval(() => {
      autoUpdater.checkForUpdates();
    }, 24 * 60 * 60 * 1000);
  });

  app.on('window-all-closed', () => {
    // Keep app running in tray
    // On macOS, only quit when explicitly requested
    if (process.platform !== 'darwin' && !store.get('minimizeToTray', true)) {
      app.quit();
    }
  });

  app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) {
      createMainWindow();
    }
  });

  app.on('before-quit', () => {
    app.isQuitting = true;
  });
}

// Cleanup on exit
process.on('exit', () => {
  if (dockerProcess) {
    dockerProcess.kill();
  }
});
