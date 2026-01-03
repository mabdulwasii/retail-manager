import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import configService from '@/config/runtime-config';

/**
 * First Run Detection Hook
 * Detects if this is the first time the embedded app is being run
 * and redirects to the setup wizard if needed.
 *
 * Only active in embedded mode (not cloud mode)
 */

const SETUP_COMPLETE_KEY = 'retailhq_setup_complete';
const CLOUD_CONFIG_KEY = 'retailhq_cloud_config';

interface CloudConfig {
  apiKey: string;
  tenantId: string;
  shopId: string;
  cloudSyncEnabled: boolean;
  configuredAt: string;
}

export const useFirstRunDetection = () => {
  const navigate = useNavigate();
  const [isFirstRun, setIsFirstRun] = useState<boolean>(false);
  const [isCheckingSetup, setIsCheckingSetup] = useState<boolean>(true);
  const [cloudConfig, setCloudConfig] = useState<CloudConfig | null>(null);

  useEffect(() => {
    // Only check for first run in embedded mode
    if (!configService.isEmbeddedMode) {
      setIsCheckingSetup(false);
      return;
    }

    checkSetupStatus();
  }, []);

  const checkSetupStatus = () => {
    try {
      // Check if setup has been completed
      const setupComplete = localStorage.getItem(SETUP_COMPLETE_KEY);
      const cloudConfigStr = localStorage.getItem(CLOUD_CONFIG_KEY);

      if (!setupComplete) {
        // First run - needs setup
        setIsFirstRun(true);
        setIsCheckingSetup(false);
        return;
      }

      // Setup complete - load cloud config if exists
      if (cloudConfigStr) {
        try {
          const config = JSON.parse(cloudConfigStr) as CloudConfig;
          setCloudConfig(config);
        } catch (error) {
          console.error('Failed to parse cloud config:', error);
        }
      }

      setIsFirstRun(false);
      setIsCheckingSetup(false);
    } catch (error) {
      console.error('Error checking setup status:', error);
      setIsCheckingSetup(false);
    }
  };

  const triggerSetupWizard = () => {
    navigate('/setup');
  };

  const markSetupComplete = (config?: CloudConfig) => {
    try {
      localStorage.setItem(SETUP_COMPLETE_KEY, 'true');
      localStorage.setItem('setup_completed_at', new Date().toISOString());

      if (config) {
        localStorage.setItem(CLOUD_CONFIG_KEY, JSON.stringify(config));
        setCloudConfig(config);
      }

      setIsFirstRun(false);
    } catch (error) {
      console.error('Failed to save setup status:', error);
      throw error;
    }
  };

  const resetSetup = () => {
    try {
      localStorage.removeItem(SETUP_COMPLETE_KEY);
      localStorage.removeItem(CLOUD_CONFIG_KEY);
      localStorage.removeItem('setup_completed_at');
      setIsFirstRun(true);
      setCloudConfig(null);
    } catch (error) {
      console.error('Failed to reset setup:', error);
      throw error;
    }
  };

  const updateCloudConfig = (config: CloudConfig) => {
    try {
      localStorage.setItem(CLOUD_CONFIG_KEY, JSON.stringify(config));
      setCloudConfig(config);
    } catch (error) {
      console.error('Failed to update cloud config:', error);
      throw error;
    }
  };

  const isCloudSyncEnabled = cloudConfig?.cloudSyncEnabled || false;

  return {
    isFirstRun,
    isCheckingSetup,
    cloudConfig,
    isCloudSyncEnabled,
    triggerSetupWizard,
    markSetupComplete,
    resetSetup,
    updateCloudConfig,
    checkSetupStatus,
  };
};

export default useFirstRunDetection;
