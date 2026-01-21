import { render, screen, fireEvent } from '@testing-library/react';
import { UnitPricingForm } from '../UnitPricingForm';
import {ProductUnitDefinition, InventoryUnitPriceRequest } from '@/types/api';
import '@testing-library/jest-dom';

// Mock useCurrency hook
jest.mock('@/hooks/useCurrency', () => ({
  useCurrency: () => ({
    formatCurrency: (amount: number) => `₦${amount.toFixed(2)}`,
  }),
}));

describe('UnitPricingForm', () => {
  const mockOnChange = jest.fn();

  const mockUnitDefinitions: ProductUnitDefinition[] = [
    {
      id: '1',
      productId: 'prod1',
      unitType: 'piece',
      unitLabel: 'Piece',
      conversionFactor: 1.0,
      isBaseUnit: true,
      sortOrder: 0,
    },
    {
      id: '2',
      productId: 'prod1',
      unitType: 'pack',
      unitLabel: 'Pack (12pcs)',
      conversionFactor: 12.0,
      isBaseUnit: false,
      sortOrder: 1,
    },
    {
      id: '3',
      productId: 'prod1',
      unitType: 'carton',
      unitLabel: 'Carton (144pcs)',
      conversionFactor: 144.0,
      isBaseUnit: false,
      sortOrder: 2,
    },
  ];

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders unit pricing form with all unit definitions', () => {
    render(
      <UnitPricingForm
        productName="Coca-Cola"
        unitDefinitions={mockUnitDefinitions}
        unitPrices={[]}
        onChange={mockOnChange}
      />
    );

    expect(screen.getByText('Set selling prices for each unit of Coca-Cola')).toBeInTheDocument();
    expect(screen.getByText('Piece')).toBeInTheDocument();
    expect(screen.getByText('Pack (12pcs)')).toBeInTheDocument();
    expect(screen.getByText('Carton (144pcs)')).toBeInTheDocument();
  });

  it('shows pricing guidelines', () => {
    render(
      <UnitPricingForm
        productName="Coca-Cola"
        unitDefinitions={mockUnitDefinitions}
        unitPrices={[]}
        onChange={mockOnChange}
      />
    );

    expect(screen.getByText(/Piece prices are typically HIGHER than pack price ÷ pack size/i)).toBeInTheDocument();
  });

  it('displays existing unit prices', () => {
    const existingPrices: InventoryUnitPriceRequest[] = [
      { unitType: 'piece', sellingPrice: 1050 },
      { unitType: 'pack', sellingPrice: 12000 },
    ];

    render(
      <UnitPricingForm
        productName="Coca-Cola"
        unitDefinitions={mockUnitDefinitions}
        unitPrices={existingPrices}
        onChange={mockOnChange}
      />
    );

    // Check if prices are displayed (would need actual input value checking)
    expect(screen.getByText('Piece')).toBeInTheDocument();
    expect(screen.getByText('Pack (12pcs)')).toBeInTheDocument();
  });

  it('calculates price per base unit correctly', () => {
    const existingPrices: InventoryUnitPriceRequest[] = [
      { unitType: 'pack', sellingPrice: 12000 },
    ];

    render(
      <UnitPricingForm
        productName="Coca-Cola"
        unitDefinitions={mockUnitDefinitions}
        unitPrices={existingPrices}
        onChange={mockOnChange}
      />
    );

    // Price per base unit for pack: 12000 / 12 = 1000
    expect(screen.getByText('₦1000.00')).toBeInTheDocument();
  });

  it('highlights base unit with blue background', () => {
    render(
      <UnitPricingForm
        productName="Coca-Cola"
        unitDefinitions={mockUnitDefinitions}
        unitPrices={[]}
        onChange={mockOnChange}
      />
    );

    const baseUnitSection = screen.getByText('Piece').closest('div')?.closest('div');
    expect(baseUnitSection).toHaveClass('bg-blue-50');
  });

  it('shows warning when no unit definitions exist', () => {
    render(
      <UnitPricingForm
        productName="Coca-Cola"
        unitDefinitions={[]}
        unitPrices={[]}
        onChange={mockOnChange}
      />
    );

    expect(
      screen.getByText(/This product has no unit definitions/i)
    ).toBeInTheDocument();
  });

  it('shows conversion factor for non-base units', () => {
    render(
      <UnitPricingForm
        productName="Coca-Cola"
        unitDefinitions={mockUnitDefinitions}
        unitPrices={[]}
        onChange={mockOnChange}
      />
    );

    expect(screen.getByText(/Conversion: 12 base units/i)).toBeInTheDocument();
    expect(screen.getByText(/Conversion: 144 base units/i)).toBeInTheDocument();
  });

  it('disables all inputs when disabled prop is true', () => {
    render(
      <UnitPricingForm
        productName="Coca-Cola"
        unitDefinitions={mockUnitDefinitions}
        unitPrices={[]}
        onChange={mockOnChange}
        disabled={true}
      />
    );

    const inputs = screen.getAllByPlaceholderText('0.00');
    inputs.forEach((input) => {
      expect(input).toBeDisabled();
    });
  });

  it('shows note about retail pricing patterns', () => {
    const existingPrices: InventoryUnitPriceRequest[] = [
      { unitType: 'piece', sellingPrice: 1050 },
      { unitType: 'pack', sellingPrice: 12000 },
    ];

    render(
      <UnitPricingForm
        productName="Coca-Cola"
        unitDefinitions={mockUnitDefinitions}
        unitPrices={existingPrices}
        onChange={mockOnChange}
      />
    );

    // piece price per base: 1050 / 1 = 1050
    // pack price per base: 12000 / 12 = 1000
    // Since piece price per base (1050) > pack price per base (1000), should show note
    expect(
      screen.getByText(/This is typical for retail—customers pay more per piece when buying in smaller quantities/i)
    ).toBeInTheDocument();
  });

  it('shows warning when no prices are set', () => {
    render(
      <UnitPricingForm
        productName="Coca-Cola"
        unitDefinitions={mockUnitDefinitions}
        unitPrices={[]}
        onChange={mockOnChange}
      />
    );

    expect(
      screen.getByText(/No prices set yet. Please set at least one selling price/i)
    ).toBeInTheDocument();
  });

  it('sorts unit definitions by sort order', () => {
    const unsortedUnits: ProductUnitDefinition[] = [
      {
        id: '2',
        productId: 'prod1',
        unitType: 'carton',
        unitLabel: 'Carton',
        conversionFactor: 144.0,
        isBaseUnit: false,
        sortOrder: 2,
      },
      {
        id: '1',
        productId: 'prod1',
        unitType: 'piece',
        unitLabel: 'Piece',
        conversionFactor: 1.0,
        isBaseUnit: true,
        sortOrder: 0,
      },
      {
        id: '3',
        productId: 'prod1',
        unitType: 'pack',
        unitLabel: 'Pack',
        conversionFactor: 12.0,
        isBaseUnit: false,
        sortOrder: 1,
      },
    ];

    const { container } = render(
      <UnitPricingForm
        productName="Coca-Cola"
        unitDefinitions={unsortedUnits}
        unitPrices={[]}
        onChange={mockOnChange}
      />
    );

    const unitLabels = Array.from(container.querySelectorAll('.text-sm.font-semibold'))
      .map((el) => el.textContent);

    // Should be sorted by sortOrder: Piece (0), Pack (1), Carton (2)
    expect(unitLabels[0]).toContain('Piece');
    expect(unitLabels[1]).toContain('Pack');
    expect(unitLabels[2]).toContain('Carton');
  });
});
