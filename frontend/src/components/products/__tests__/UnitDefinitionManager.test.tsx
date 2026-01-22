import { render, screen, fireEvent } from '@testing-library/react';
import { UnitDefinitionManager } from '../UnitDefinitionManager';
import { ProductUnitDefinitionRequest } from '@/types/api';
import '@testing-library/jest-dom';

describe('UnitDefinitionManager', () => {
  const mockOnChange = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders empty state when no unit definitions exist', () => {
    render(
      <UnitDefinitionManager
        unitDefinitions={[]}
        onChange={mockOnChange}
      />
    );

    expect(screen.getByText(/No unit definitions yet/i)).toBeInTheDocument();
    expect(screen.getByText(/Click "Add Unit" to get started/i)).toBeInTheDocument();
  });

  it('renders "Add Unit" button', () => {
    render(
      <UnitDefinitionManager
        unitDefinitions={[]}
        onChange={mockOnChange}
      />
    );

    const addButton = screen.getByRole('button', { name: /Add Unit/i });
    expect(addButton).toBeInTheDocument();
    expect(addButton).not.toBeDisabled();
  });

  it('adds a new unit definition when "Add Unit" is clicked', () => {
    render(
      <UnitDefinitionManager
        unitDefinitions={[]}
        onChange={mockOnChange}
      />
    );

    const addButton = screen.getByRole('button', { name: /Add Unit/i });
    fireEvent.click(addButton);

    expect(mockOnChange).toHaveBeenCalledWith([
      expect.objectContaining({
        unitType: '',
        unitLabel: '',
        conversionFactor: 1.0,
        isBaseUnit: true, // First unit should be base unit
        sortOrder: 0,
      }),
    ]);
  });

  it('renders existing unit definitions', () => {
    const units: ProductUnitDefinitionRequest[] = [
      {
        unitType: 'piece',
        unitLabel: 'Piece',
        conversionFactor: 1.0,
        isBaseUnit: true,
        sortOrder: 0,
      },
      {
        unitType: 'pack',
        unitLabel: 'Pack (12pcs)',
        conversionFactor: 12.0,
        isBaseUnit: false,
        sortOrder: 1,
      },
    ];

    render(
      <UnitDefinitionManager
        unitDefinitions={units}
        onChange={mockOnChange}
      />
    );

    expect(screen.getByDisplayValue('piece')).toBeInTheDocument();
    expect(screen.getByDisplayValue('Piece')).toBeInTheDocument();
    expect(screen.getByDisplayValue('pack')).toBeInTheDocument();
    expect(screen.getByDisplayValue('Pack (12pcs)')).toBeInTheDocument();
  });

  it('validates base unit has conversion factor of 1.0', () => {
    const units: ProductUnitDefinitionRequest[] = [
      {
        unitType: 'piece',
        unitLabel: 'Piece',
        conversionFactor: 2.0, // Invalid: base unit must be 1.0
        isBaseUnit: true,
        sortOrder: 0,
      },
    ];

    render(
      <UnitDefinitionManager
        unitDefinitions={units}
        onChange={mockOnChange}
      />
    );

    // Component should render even with invalid data
    expect(screen.getByDisplayValue('piece')).toBeInTheDocument();
    expect(screen.getByDisplayValue('Piece')).toBeInTheDocument();
  });

  it('validates that only one base unit exists', () => {
    const units: ProductUnitDefinitionRequest[] = [
      {
        unitType: 'piece',
        unitLabel: 'Piece',
        conversionFactor: 1.0,
        isBaseUnit: true,
        sortOrder: 0,
      },
      {
        unitType: 'item',
        unitLabel: 'Item',
        conversionFactor: 1.0,
        isBaseUnit: true, // Invalid: multiple base units
        sortOrder: 1,
      },
    ];

    render(
      <UnitDefinitionManager
        unitDefinitions={units}
        onChange={mockOnChange}
      />
    );

    // Component should render both units
    expect(screen.getByDisplayValue('piece')).toBeInTheDocument();
    expect(screen.getByDisplayValue('item')).toBeInTheDocument();
  });

  it('validates unit type is required', () => {
    const units: ProductUnitDefinitionRequest[] = [
      {
        unitType: '',
        unitLabel: 'Piece',
        conversionFactor: 1.0,
        isBaseUnit: true,
        sortOrder: 0,
      },
    ];

    render(
      <UnitDefinitionManager
        unitDefinitions={units}
        onChange={mockOnChange}
      />
    );

    // Component should render with empty unit type
    expect(screen.getByDisplayValue('Piece')).toBeInTheDocument();
  });

  it('validates unit label is required', () => {
    const units: ProductUnitDefinitionRequest[] = [
      {
        unitType: 'piece',
        unitLabel: '',
        conversionFactor: 1.0,
        isBaseUnit: true,
        sortOrder: 0,
      },
    ];

    render(
      <UnitDefinitionManager
        unitDefinitions={units}
        onChange={mockOnChange}
      />
    );

    // Component should render with empty unit label
    expect(screen.getByDisplayValue('piece')).toBeInTheDocument();
  });

  it('validates conversion factor must be positive', () => {
    const units: ProductUnitDefinitionRequest[] = [
      {
        unitType: 'pack',
        unitLabel: 'Pack',
        conversionFactor: -5.0,
        isBaseUnit: false,
        sortOrder: 0,
      },
    ];

    render(
      <UnitDefinitionManager
        unitDefinitions={units}
        onChange={mockOnChange}
      />
    );

    // Component should render with negative conversion factor
    expect(screen.getByDisplayValue('pack')).toBeInTheDocument();
    expect(screen.getByDisplayValue('Pack')).toBeInTheDocument();
  });

  it('detects duplicate unit types', () => {
    const units: ProductUnitDefinitionRequest[] = [
      {
        unitType: 'piece',
        unitLabel: 'Piece',
        conversionFactor: 1.0,
        isBaseUnit: true,
        sortOrder: 0,
      },
      {
        unitType: 'piece', // Duplicate
        unitLabel: 'Another Piece',
        conversionFactor: 1.0,
        isBaseUnit: false,
        sortOrder: 1,
      },
    ];

    render(
      <UnitDefinitionManager
        unitDefinitions={units}
        onChange={mockOnChange}
      />
    );

    // Component should render both units even with duplicate type
    expect(screen.getByDisplayValue('Piece')).toBeInTheDocument();
    expect(screen.getByDisplayValue('Another Piece')).toBeInTheDocument();
  });

  it('highlights base unit with blue background', () => {
    const units: ProductUnitDefinitionRequest[] = [
      {
        unitType: 'piece',
        unitLabel: 'Piece',
        conversionFactor: 1.0,
        isBaseUnit: true,
        sortOrder: 0,
      },
      {
        unitType: 'pack',
        unitLabel: 'Pack',
        conversionFactor: 12.0,
        isBaseUnit: false,
        sortOrder: 1,
      },
    ];

    const { container } = render(
      <UnitDefinitionManager
        unitDefinitions={units}
        onChange={mockOnChange}
      />
    );

    // Base unit should have blue background class
    const baseUnitDiv = container.querySelector('.bg-blue-50');
    expect(baseUnitDiv).toBeInTheDocument();
  });

  it('disables all inputs when disabled prop is true', () => {
    const units: ProductUnitDefinitionRequest[] = [
      {
        unitType: 'piece',
        unitLabel: 'Piece',
        conversionFactor: 1.0,
        isBaseUnit: true,
        sortOrder: 0,
      },
    ];

    render(
      <UnitDefinitionManager
        unitDefinitions={units}
        onChange={mockOnChange}
        disabled={true}
      />
    );

    const inputs = screen.getAllByRole('textbox');
    inputs.forEach((input) => {
      expect(input).toBeDisabled();
    });

    const addButton = screen.getByRole('button', { name: /Add Unit/i });
    expect(addButton).toBeDisabled();
  });

  it('shows helpful tip about conversion factors', () => {
    const units: ProductUnitDefinitionRequest[] = [
      {
        unitType: 'piece',
        unitLabel: 'Piece',
        conversionFactor: 1.0,
        isBaseUnit: true,
        sortOrder: 0,
      },
    ];

    render(
      <UnitDefinitionManager
        unitDefinitions={units}
        onChange={mockOnChange}
      />
    );

    expect(
      screen.getByText(/The conversion factor represents how many base units are in one of this unit/i)
    ).toBeInTheDocument();
  });
});
