import api from "@/lib/axios";

// Type Aliases
export type ThemeVariant = 'LIGHT' | 'DARK' | 'AUTO';
export type FontSize = 'SMALL' | 'MEDIUM' | 'LARGE';
export type DashboardLayout = 'GRID' | 'LIST' | 'CARD';

// Configuration Interfaces
export interface ShopConfiguration {
  investmentEnabled: boolean;
  analyticsEnabled: boolean;
  fraudDetectionEnabled: boolean;
  autoBackupEnabled: boolean;
  currency: string;
  taxRate: number;
  maxDiscountPercentage: number;
  receiptFooter: string;
}

export interface ShopConfigurationRequest {
  investmentEnabled?: boolean;
  analyticsEnabled?: boolean;
  fraudDetectionEnabled?: boolean;
  autoBackupEnabled?: boolean;
  currency?: string;
  taxRate?: number;
  maxDiscountPercentage?: number;
  receiptFooter?: string;
}

// Customization Interfaces
export interface ShopCustomization {
  id: string;
  shopId: string;
  primaryColor: string;
  secondaryColor: string;
  accentColor: string;
  backgroundColor: string;
  textColor: string;
  logoUrl?: string;
  faviconUrl?: string;
  bannerImageUrl?: string;
  backgroundImageUrl?: string;
  websiteUrl?: string;
  socialMediaLinks?: string;
  themeVariant: ThemeVariant;
  fontFamily: string;
  fontSize: FontSize;
  borderRadius: number;
  customStyles?: string;
  dashboardLayout: DashboardLayout;
  receiptHeader: string;
  receiptFooter: string;
  receiptShowLogo: boolean;
  showBanner: boolean;
  enableAnimations: boolean;
  showAdvancedFeatures: boolean;
}

export interface ShopCustomizationRequest {
  primaryColor?: string;
  secondaryColor?: string;
  accentColor?: string;
  backgroundColor?: string;
  textColor?: string;
  logoUrl?: string;
  faviconUrl?: string;
  bannerImageUrl?: string;
  backgroundImageUrl?: string;
  websiteUrl?: string;
  socialMediaLinks?: string;
  themeVariant?: ThemeVariant;
  fontFamily?: string;
  fontSize?: FontSize;
  borderRadius?: number;
  customStyles?: string;
  dashboardLayout?: DashboardLayout;
  receiptHeader?: string;
  receiptFooter?: string;
  receiptShowLogo?: boolean;
  showBanner?: boolean;
  enableAnimations?: boolean;
  showAdvancedFeatures?: boolean;
}

export const shopConfigurationService = {
  // Configuration endpoints
  async getConfiguration(shopId: string): Promise<ShopConfiguration> {
    const { data } = await api.get(`/shops/${shopId}/configuration`);
    return data;
  },

  async updateConfiguration(
    shopId: string,
    config: ShopConfigurationRequest
  ): Promise<ShopConfiguration> {
    const { data } = await api.patch(`/shops/${shopId}/configuration`, config);
    return data;
  },

  // Customization endpoints
  async getCustomization(shopId: string): Promise<ShopCustomization> {
    const { data } = await api.get(`/shops/${shopId}/customization`);
    return data;
  },

  async updateCustomization(
    shopId: string,
    customization: ShopCustomizationRequest
  ): Promise<ShopCustomization> {
    const { data } = await api.patch(`/shops/${shopId}/customization`, customization);
    return data;
  },

  async updateColors(
    shopId: string,
    colors: {
      primaryColor?: string;
      secondaryColor?: string;
      accentColor?: string;
    }
  ): Promise<ShopCustomization> {
    const params = new URLSearchParams();
    if (colors.primaryColor) params.append('primaryColor', colors.primaryColor);
    if (colors.secondaryColor) params.append('secondaryColor', colors.secondaryColor);
    if (colors.accentColor) params.append('accentColor', colors.accentColor);

    const { data } = await api.patch(
      `/shops/${shopId}/customization/colors?${params.toString()}`
    );
    return data;
  },

  async updateTheme(
    shopId: string,
    theme: {
      themeVariant?: ThemeVariant;
      fontSize?: FontSize;
    }
  ): Promise<ShopCustomization> {
    const params = new URLSearchParams();
    if (theme.themeVariant) params.append('themeVariant', theme.themeVariant);
    if (theme.fontSize) params.append('fontSize', theme.fontSize);

    const { data } = await api.patch(
      `/shops/${shopId}/customization/theme?${params.toString()}`
    );
    return data;
  },

  async uploadLogo(shopId: string, file: File): Promise<ShopCustomization> {
    const formData = new FormData();
    formData.append('file', file);

    const { data } = await api.post(`/shops/${shopId}/customization/logo`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return data;
  },

  async updateContact(
    shopId: string,
    contact: {
      websiteUrl?: string;
      socialMediaLinks?: string;
    }
  ): Promise<ShopCustomization> {
    const params = new URLSearchParams();
    if (contact.websiteUrl) params.append('websiteUrl', contact.websiteUrl);
    if (contact.socialMediaLinks) params.append('socialMediaLinks', contact.socialMediaLinks);

    const { data } = await api.patch(
      `/shops/${shopId}/customization/contact?${params.toString()}`
    );
    return data;
  },

  async resetCustomization(shopId: string): Promise<ShopCustomization> {
    const { data } = await api.delete(`/shops/${shopId}/customization`);
    return data;
  },
};
