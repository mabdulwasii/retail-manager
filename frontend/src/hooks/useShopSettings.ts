import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import {
  shopConfigurationService,
  ShopConfigurationRequest,
  ShopCustomizationRequest,
  ThemeVariant,
  FontSize,
} from '@/services/shopConfigurationService';

// Hook to fetch shop configuration
export const useShopConfiguration = (shopId: string | undefined) => {
  return useQuery({
    queryKey: ['shop', shopId, 'configuration'],
    queryFn: () => shopConfigurationService.getConfiguration(shopId!),
    enabled: !!shopId,
    staleTime: 5 * 60 * 1000, // 5 minutes
    retry: 2,
  });
};

// Hook to update shop configuration
export const useUpdateShopConfiguration = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({
      shopId,
      config,
    }: {
      shopId: string;
      config: ShopConfigurationRequest;
    }) => {
      console.log('Updating shop configuration:', shopId, config);
      return shopConfigurationService.updateConfiguration(shopId, config);
    },
    onSuccess: (data, variables) => {
      console.log('Configuration updated successfully:', data);
      // Invalidate and refetch
      queryClient.invalidateQueries({ queryKey: ['shop', variables.shopId, 'configuration'] });
      queryClient.invalidateQueries({ queryKey: ['shops', variables.shopId] });
      toast.success('Settings updated successfully', {
        description: 'Shop configuration has been saved.',
      });
    },
    onError: (error: any) => {
      console.error('Failed to update configuration:', error);
      console.error('Error response:', error.response?.data);
      toast.error('Failed to update settings', {
        description: error.response?.data?.message || error.message || 'An error occurred',
      });
    },
  });
};

// Hook to fetch shop customization
export const useShopCustomization = (shopId: string | undefined) => {
  return useQuery({
    queryKey: ['shop', shopId, 'customization'],
    queryFn: () => shopConfigurationService.getCustomization(shopId!),
    enabled: !!shopId,
    staleTime: 5 * 60 * 1000, // 5 minutes
    retry: 2,
  });
};

export const useUpdateShopCustomization = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({
      shopId,
      customization,
    }: {
      shopId: string;
      customization: ShopCustomizationRequest;
    }) => {
      console.log('Updating shop customization:', shopId, customization);
      return shopConfigurationService.updateCustomization(shopId, customization);
    },
    onSuccess: (data, variables) => {
      console.log('Customization updated successfully:', data);
      queryClient.invalidateQueries({ queryKey: ['shop', variables.shopId, 'customization'] });
      toast.success('Customization updated successfully', {
        description: 'Shop branding has been saved.',
      });
    },
    onError: (error: any) => {
      console.error('Failed to update customization:', error);
      console.error('Error response:', error.response?.data);
      toast.error('Failed to update customization', {
        description: error.response?.data?.message || error.message || 'An error occurred',
      });
    },
  });
};

// Hook to update theme settings
export const useUpdateTheme = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({
      shopId,
      theme,
    }: {
      shopId: string;
      theme: {
        themeVariant?: ThemeVariant;
        fontSize?: FontSize;
      };
    }) => shopConfigurationService.updateTheme(shopId, theme),
    onSuccess: (data, variables) => {
      queryClient.invalidateQueries({ queryKey: ['shop', variables.shopId, 'customization'] });
      toast.success('Theme updated', {
        description: 'Theme settings have been saved.',
      });
    },
    onError: (error: any) => {
      toast.error('Failed to update theme', {
        description: error.response?.data?.message || error.message || 'An error occurred',
      });
    },
  });
};

// Hook to update colors
export const useUpdateColors = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({
      shopId,
      colors,
    }: {
      shopId: string;
      colors: {
        primaryColor?: string;
        secondaryColor?: string;
        accentColor?: string;
      };
    }) => shopConfigurationService.updateColors(shopId, colors),
    onSuccess: (data, variables) => {
      queryClient.invalidateQueries({ queryKey: ['shop', variables.shopId, 'customization'] });
      toast.success('Colors updated', {
        description: 'Color scheme has been saved.',
      });
    },
    onError: (error: any) => {
      toast.error('Failed to update colors', {
        description: error.response?.data?.message || error.message || 'An error occurred',
      });
    },
  });
};

// Hook to upload logo
export const useUploadLogo = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ shopId, file }: { shopId: string; file: File }) =>
      shopConfigurationService.uploadLogo(shopId, file),
    onSuccess: (data, variables) => {
      queryClient.invalidateQueries({ queryKey: ['shop', variables.shopId, 'customization'] });
      toast.success('Logo uploaded', {
        description: 'Shop logo has been updated.',
      });
    },
    onError: (error: any) => {
      toast.error('Failed to upload logo', {
        description: error.response?.data?.message || error.message || 'An error occurred',
      });
    },
  });
};

export const useResetCustomization = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (shopId: string) => shopConfigurationService.resetCustomization(shopId),
    onSuccess: (data, shopId) => {
      queryClient.invalidateQueries({ queryKey: ['shop', shopId, 'customization'] });
      toast.success('Customization reset', {
        description: 'Shop customization has been reset to defaults.',
      });
    },
    onError: (error: any) => {
      toast.error('Failed to reset customization', {
        description: error.response?.data?.message || error.message || 'An error occurred',
      });
    },
  });
};
