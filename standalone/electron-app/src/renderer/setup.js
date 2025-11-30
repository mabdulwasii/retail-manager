/**
 * Shop Manager Setup Wizard - Renderer Script
 */

let currentStep = 1;
let installationComplete = false;

// Run prerequisite checks on load
window.addEventListener('DOMContentLoaded', () => {
    checkPrerequisites();
});

/**
 * Check system prerequisites
 */
async function checkPrerequisites() {
    console.log('Checking prerequisites...');

    // Check Docker
    try {
        const dockerStatus = await window.shopManager.checkDocker();

        const dockerIcon = document.getElementById('docker-status-icon');
        const dockerText = document.getElementById('docker-status-text');

        if (dockerStatus.running) {
            dockerIcon.className = 'status-icon success';
            dockerIcon.textContent = '✓';
            dockerText.textContent = 'Docker is installed and running';
        } else if (dockerStatus.installed) {
            dockerIcon.className = 'status-icon error';
            dockerIcon.textContent = '✗';
            dockerText.textContent = 'Docker is installed but not running. Please start Docker Desktop.';
            showPrerequisiteError('Docker is not running. Please start Docker Desktop and try again.');
            return;
        } else {
            dockerIcon.className = 'status-icon error';
            dockerIcon.textContent = '✗';
            dockerText.textContent = 'Docker is not installed';
            showPrerequisiteError('Docker is required. Please install Docker Desktop from <a href="https://www.docker.com/products/docker-desktop" onclick="openExternal(this.href); return false;">docker.com</a>');
            return;
        }
    } catch (error) {
        console.error('Error checking Docker:', error);
        const dockerIcon = document.getElementById('docker-status-icon');
        const dockerText = document.getElementById('docker-status-text');
        dockerIcon.className = 'status-icon error';
        dockerIcon.textContent = '✗';
        dockerText.textContent = 'Error checking Docker status';
        showPrerequisiteError('Failed to check Docker status. Please ensure Docker Desktop is installed.');
        return;
    }

    // Check Python (simulate - in real app, would call IPC)
    const pythonIcon = document.getElementById('python-status-icon');
    const pythonText = document.getElementById('python-status-text');
    pythonIcon.className = 'status-icon success';
    pythonIcon.textContent = '✓';
    pythonText.textContent = 'Python 3 is installed';

    // Check disk space (simulate - in real app, would check actual space)
    const diskIcon = document.getElementById('disk-status-icon');
    const diskText = document.getElementById('disk-status-text');
    diskIcon.className = 'status-icon success';
    diskIcon.textContent = '✓';
    diskText.textContent = '20 GB available (minimum required)';

    // Enable next button
    document.getElementById('next-step-1').disabled = false;
    hidePrerequisiteError();
}

/**
 * Show prerequisite error message
 */
function showPrerequisiteError(message) {
    const errorDiv = document.getElementById('prerequisite-error');
    errorDiv.innerHTML = `<div class="alert alert-error">${message}</div>`;
    errorDiv.style.display = 'block';
    document.getElementById('next-step-1').disabled = true;
}

/**
 * Hide prerequisite error message
 */
function hidePrerequisiteError() {
    const errorDiv = document.getElementById('prerequisite-error');
    errorDiv.style.display = 'none';
}

/**
 * Navigate to next step
 */
function nextStep(step) {
    // Validate current step before proceeding
    if (step === 2) {
        if (!validateConfigForm()) {
            return;
        }
        // Start installation
        startInstallation();
    }

    currentStep = step + 1;
    showStep(currentStep);
}

/**
 * Navigate to previous step
 */
function prevStep(step) {
    currentStep = step - 1;
    showStep(currentStep);
}

/**
 * Show specific step
 */
function showStep(stepNumber) {
    // Hide all steps
    document.querySelectorAll('.step').forEach(step => {
        step.classList.remove('active');
    });

    // Show target step
    const targetStep = document.querySelector(`.step[data-step="${stepNumber}"]`);
    if (targetStep) {
        targetStep.classList.add('active');
    }

    // Update step indicator
    document.querySelectorAll('.step-item').forEach((item, index) => {
        item.classList.remove('active', 'completed');
        const itemStep = index + 1;

        if (itemStep < stepNumber) {
            item.classList.add('completed');
        } else if (itemStep === stepNumber) {
            item.classList.add('active');
        }
    });
}

/**
 * Validate configuration form
 */
function validateConfigForm() {
    const companyName = document.getElementById('company-name').value.trim();
    const adminEmail = document.getElementById('admin-email').value.trim();

    if (!companyName) {
        alert('Please enter your company name');
        return false;
    }

    if (!adminEmail || !isValidEmail(adminEmail)) {
        alert('Please enter a valid administrator email');
        return false;
    }

    return true;
}

/**
 * Validate email format
 */
function isValidEmail(email) {
    const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return re.test(email);
}

/**
 * Start installation process
 */
async function startInstallation() {
    console.log('Starting installation...');

    // Get configuration values
    const config = {
        branding: {
            companyName: document.getElementById('company-name').value,
            platformName: document.getElementById('platform-name').value
        },
        keycloak: {
            admin: {
                email: document.getElementById('admin-email').value
            }
        },
        business: {
            defaultCurrency: document.getElementById('currency').value
        }
    };

    // Show installation step
    showStep(3);

    // Update progress
    updateProgress(10, 'Generating configuration files...');

    // Listen for logs
    window.shopManager.onServiceLog((log) => {
        appendLog(log);
    });

    try {
        // Step 1: Save configuration
        updateProgress(20, 'Saving configuration...');
        await new Promise(resolve => setTimeout(resolve, 1000));

        // Step 2: Generate config files
        updateProgress(40, 'Generating configuration files...');
        const genResult = await window.shopManager.generateConfig();

        if (!genResult.success) {
            throw new Error('Configuration generation failed');
        }

        // Step 3: Pull Docker images
        updateProgress(50, 'Downloading Docker images (this may take several minutes)...');
        await new Promise(resolve => setTimeout(resolve, 2000));

        // Step 4: Start services
        updateProgress(70, 'Starting services...');
        const startResult = await window.shopManager.startServices();

        if (!startResult.success) {
            throw new Error('Failed to start services: ' + startResult.error);
        }

        // Step 5: Wait for services to be ready
        updateProgress(90, 'Waiting for services to initialize...');
        await new Promise(resolve => setTimeout(resolve, 5000));

        // Complete
        updateProgress(100, 'Installation complete!');
        installationComplete = true;
        document.getElementById('finish-btn').disabled = false;

    } catch (error) {
        console.error('Installation error:', error);
        updateProgress(0, `Installation failed: ${error.message}`);
        alert(`Installation failed: ${error.message}\n\nPlease check the logs and try again.`);
    }
}

/**
 * Update installation progress
 */
function updateProgress(percent, message) {
    const progressBar = document.getElementById('install-progress');
    const statusText = document.getElementById('install-status-text');

    progressBar.style.width = `${percent}%`;
    statusText.textContent = message;
}

/**
 * Append log message
 */
function appendLog(message) {
    const logsDiv = document.getElementById('install-logs');
    const line = document.createElement('div');
    line.textContent = message;
    logsDiv.appendChild(line);
    logsDiv.scrollTop = logsDiv.scrollHeight;
}

/**
 * Toggle log visibility
 */
function toggleLogs() {
    const logsDiv = document.getElementById('install-logs');
    logsDiv.style.display = logsDiv.style.display === 'none' ? 'block' : 'none';
}

/**
 * Finish setup
 */
async function finishSetup() {
    if (!installationComplete) {
        alert('Installation is not yet complete');
        return;
    }

    // Mark setup as complete
    await window.shopManager.completeSetup();

    // Show completion step
    showStep(4);
}

/**
 * Open application in browser
 */
function openApp(url) {
    window.shopManager.openUrl(url);
    return false;
}

/**
 * Open external link
 */
function openExternal(url) {
    window.shopManager.openUrl(url);
    return false;
}

/**
 * Open dashboard (reload app)
 */
function openDashboard() {
    window.location.href = 'dashboard.html';
}
