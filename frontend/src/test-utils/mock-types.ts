/**
 * Common type definitions for mock components in tests
 * This prevents the need to use 'any' types in test files
 */

import React from 'react';

export interface MockCardProps {
  children: React.ReactNode;
  className?: string;
}

export interface MockButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  children: React.ReactNode;
  className?: string;
  asChild?: boolean;
  variant?: string;
  size?: string;
}

export interface MockSelectProps {
  children: React.ReactNode;
  value?: string;
  onValueChange?: (value: string) => void;
}

export interface MockSelectItemProps {
  children: React.ReactNode;
  value: string;
}

export interface MockShopSelectorProps {
  value?: string;
  onValueChange?: (value: string) => void;
}

export interface MockAlertProps {
  children: React.ReactNode;
  variant?: string;
}
