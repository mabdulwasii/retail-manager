/**
 * Shop Manager Desktop - Preload Script
 *
 * This script runs in a sandboxed context with access to both
 * the renderer process and Node.js APIs. It exposes a safe API
 * to the renderer via contextBridge.
 */

const { contextBridge, ipcRenderer } = require('electron');

// Expose safe API to renderer process
contextBridge.exposeInMainWorld('shopManager', {
  // Docker operations
  checkDocker: () => ipcRenderer.invoke('check-docker'),
  startServices: () => ipcRenderer.invoke('start-services'),
  stopServices: () => ipcRenderer.invoke('stop-services'),
  restartServices: () => ipcRenderer.invoke('restart-services'),
  getServiceStatus: () => ipcRenderer.invoke('get-service-status'),

  // Configuration
  readConfig: () => ipcRenderer.invoke('read-config'),
  saveConfig: (config) => ipcRenderer.invoke('save-config', config),
  generateConfig: () => ipcRenderer.invoke('generate-config'),

  // Backup operations
  createBackup: () => ipcRenderer.invoke('create-backup'),

  // Settings
  getSettings: () => ipcRenderer.invoke('get-settings'),
  saveSettings: (settings) => ipcRenderer.invoke('save-settings', settings),

  // Setup
  completeSetup: () => ipcRenderer.invoke('complete-setup'),

  // Utilities
  openUrl: (url) => ipcRenderer.invoke('open-url', url),
  getVersion: () => ipcRenderer.invoke('get-version'),

  // Event listeners
  onServiceLog: (callback) => {
    ipcRenderer.on('service-log', (event, data) => callback(data));
  },
  onServicesStarted: (callback) => {
    ipcRenderer.on('services-started', () => callback());
  },
  onServicesStopped: (callback) => {
    ipcRenderer.on('services-stopped', () => callback());
  },
  onShowLogs: (callback) => {
    ipcRenderer.on('show-logs', () => callback());
  },
  onShowSettings: (callback) => {
    ipcRenderer.on('show-settings', () => callback());
  }
});
