import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Checkbox } from "@/components/ui/checkbox";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { NumericInput } from "@/components/ui/numeric-input";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Textarea } from "@/components/ui/textarea";
import { useCategories } from "@/hooks/useCategories";
import { useCurrency } from "@/hooks/useCurrency";
import { Product, ProductStatus } from "@/types/api";
import { yupResolver } from "@hookform/resolvers/yup";
import { Loader2 } from "lucide-react";
import React, { useEffect, useState } from "react";
import { Controller, useForm } from "react-hook-form";
import * as yup from "yup";

const productSchema = yup.object({
  name: yup
    .string()
    .required("Product name is required")
    .min(2, "Product name must be at least 2 characters")
    .max(200, "Product name must be at most 200 characters"),
  description: yup
    .string()
    .optional()
    .max(1000, "Description must be at most 1000 characters"),
  categoryId: yup.string().required("Category is required"),
  category: yup.string().optional(), // For display only
  unit: yup.string().optional(),
  customUnit: yup.string().optional(),
  weightInKg: yup
    .number()
    .typeError("Weight must be a number")
    .min(0, "Weight must be 0 or greater")
    .optional()
    .nullable()
    .transform((value, originalValue) => (originalValue === "" ? null : value)),
  dimensions: yup.string().optional(),
  supplierName: yup.string().optional(),
  supplierContact: yup.string().optional(),
  imageUrl: yup.string().url("Must be a valid URL").optional(),
  isTaxable: yup.boolean().optional(),
  isDiscountable: yup.boolean().optional(),
  // sku: yup.string()
  //   .optional()
  //   .matches(/^[A-Z0-9-]+$/, 'SKU must contain only uppercase letters, numbers, and hyphens'),
  barcode: yup
    .string()
    .optional()
    .nullable()
    .transform((value, originalValue) => (originalValue === "" ? null : value))
    .matches(/^[0-9]*$/, "Barcode must contain only numbers"), // Allow empty string
  status: yup.string().oneOf(Object.values(ProductStatus)).optional(),
});

export type ProductFormData = yup.InferType<typeof productSchema>;

interface ProductFormProps {
  product?: Product;
  onSubmit: (data: ProductFormData) => void | Promise<void>;
  onCancel: () => void;
  isSubmitting?: boolean;
  shopId?: string;
}

// Common product units
const PRODUCT_UNITS = [
  { value: "piece", label: "Piece" },
  { value: "pack", label: "Pack" },
  { value: "box", label: "Box" },
  { value: "bottle", label: "Bottle" },
  { value: "can", label: "Can" },
  { value: "kg", label: "Kilogram (kg)" },
  { value: "g", label: "Gram (g)" },
  { value: "l", label: "Liter (L)" },
  { value: "ml", label: "Milliliter (ml)" },
  { value: "carton", label: "Carton" },
  { value: "dozen", label: "Dozen" },
  { value: "roll", label: "Roll" },
  { value: "other", label: "Other" },
];

export const ProductForm: React.FC<ProductFormProps> = ({
  product,
  onSubmit,
  onCancel,
  isSubmitting = false,
  shopId,
}) => {
  const { data: categories = [], isLoading: categoriesLoading } =
    useCategories(false, shopId);
  const { formatCurrency } = useCurrency();
  const [showCustomUnit, setShowCustomUnit] = useState(false);

  const {
    register,
    handleSubmit,
    setValue,
    watch,
    control,
    formState: { errors },
  } = useForm<ProductFormData>({
    resolver: yupResolver(productSchema),
    defaultValues: {
      name: product?.name || "",
      description: product?.description || "",
      categoryId: product?.categoryId || "",
      category: product?.category || "", // For display
      unit:
        product?.unit && PRODUCT_UNITS.find((u) => u.value === product.unit)
          ? product.unit
          : product?.unit
          ? "other"
          : "",
      customUnit:
        product?.unit && !PRODUCT_UNITS.find((u) => u.value === product.unit)
          ? product.unit
          : "",
      weightInKg: product?.weightInGrams
        ? product.weightInGrams / 1000
        : undefined,
      // location: product?.location || '',
      dimensions: product?.dimensions || "",
      supplierName: product?.supplierName || "",
      supplierContact: product?.supplierContact || "",
      imageUrl: product?.imageUrl || "",
      isTaxable: product?.isTaxable ?? product?.taxable ?? true,
      isDiscountable: product?.isDiscountable ?? product?.discountable ?? true,
      // sku: product?.sku || '',
      barcode: product?.barcode || "",
      status: product?.status || ProductStatus.ACTIVE,
    },
  });

  const categoryId = watch("categoryId");
  const status = watch("status");
  const selectedUnit = watch("unit");

  // Show custom unit input when 'other' is selected
  useEffect(() => {
    setShowCustomUnit(selectedUnit === "other");
  }, [selectedUnit]);

  // const handleGenerateSKU = () => {
  //   const newSKU = productService.generateSKU()
  //   setValue('sku', newSKU)
  // }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
      <Card>
        <CardContent className="pt-6 space-y-4">
          {/* Product Name */}
          <div className="space-y-2">
            <Label htmlFor="name">
              Product Name <span className="text-red-500">*</span>
            </Label>
            <Input
              id="name"
              {...register("name")}
              placeholder="Enter product name"
              disabled={isSubmitting}
            />
            {errors.name && (
              <p className="text-sm text-red-500">{errors.name.message}</p>
            )}
          </div>

          {/* Description */}
          <div className="space-y-2">
            <Label htmlFor="description">Description</Label>
            <Textarea
              id="description"
              {...register("description")}
              placeholder="Enter product description"
              rows={3}
              disabled={isSubmitting}
            />
            {errors.description && (
              <p className="text-sm text-red-500">
                {errors.description.message}
              </p>
            )}
          </div>

          {/* Category */}
          <div className="space-y-2">
            <Label htmlFor="categoryId">
              Category <span className="text-red-500">*</span>
            </Label>
            {categoriesLoading ? (
              <div className="flex items-center gap-2 text-sm text-gray-500">
                <Loader2 className="h-4 w-4 animate-spin" />
                Loading categories...
              </div>
            ) : categories.length === 0 ? (
              <div className="text-sm text-gray-500">
                No categories available. Please create a category first.
              </div>
            ) : (
              <Select
                value={categoryId}
                onValueChange={(value) => {
                  setValue("categoryId", value);
                  const selectedCat = categories.find((c) => c.id === value);
                  if (selectedCat) {
                    setValue("category", selectedCat.name);
                  }
                }}
                disabled={isSubmitting}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Select a category" />
                </SelectTrigger>
                <SelectContent>
                  {categories.map((cat) => (
                    <SelectItem key={cat.id} value={cat.id}>
                      {cat.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            )}
            {errors.categoryId && (
              <p className="text-sm text-red-500">
                {errors.categoryId.message}
              </p>
            )}
          </div>


          {/* SKU */}
          {/* <div className="space-y-2">
            <Label htmlFor="sku">SKU (Stock Keeping Unit)</Label>
            <div className="flex gap-2">
              <Input
                id="sku"
                {...register('sku')}
                placeholder="Auto-generated if empty"
                disabled={isSubmitting}
                className="flex-1"
              />
              <Button
                type="button"
                variant="outline"
                onClick={handleGenerateSKU}
                disabled={isSubmitting}
              >
                <Sparkles className="h-4 w-4 mr-2" />
                Generate
              </Button>
            </div>
            {errors.sku && (
              <p className="text-sm text-red-500">{errors.sku.message}</p>
            )}
          </div> */}

          {/* Barcode */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="barcode">Barcode</Label>
              <Input
                id="barcode"
                {...register("barcode")}
                placeholder="Enter barcode"
                disabled={isSubmitting}
              />
              {errors.barcode && (
                <p className="text-sm text-red-500">{errors.barcode.message}</p>
              )}
            </div>

            {/* Image URL */}
            <div className="space-y-2">
              <Label htmlFor="imageUrl">Image URL</Label>
              <Input
                id="imageUrl"
                type="url"
                {...register("imageUrl")}
                placeholder="https://cdn.example.com/products/product-image.jpg"
                disabled={isSubmitting}
              />
              {errors.imageUrl && (
                <p className="text-sm text-red-500">
                  {errors.imageUrl.message}
                </p>
              )}
            </div>
          </div>
          {/* Unit and Weight */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="unit">Unit</Label>
              <Select
                value={selectedUnit || ""}
                onValueChange={(value) => setValue("unit", value)}
                disabled={isSubmitting}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Select unit" />
                </SelectTrigger>
                <SelectContent>
                  {PRODUCT_UNITS.map((unit) => (
                    <SelectItem key={unit.value} value={unit.value}>
                      {unit.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {errors.unit && (
                <p className="text-sm text-red-500">{errors.unit.message}</p>
              )}
            </div>

            {showCustomUnit && (
              <div className="space-y-2">
                <Label htmlFor="customUnit">Custom Unit</Label>
                <Input
                  id="customUnit"
                  {...register("customUnit")}
                  placeholder="Specify custom unit"
                  disabled={isSubmitting}
                />
                {errors.customUnit && (
                  <p className="text-sm text-red-500">
                    {errors.customUnit.message}
                  </p>
                )}
              </div>
            )}

            {!showCustomUnit && (
              <div className="space-y-2">
                <Label htmlFor="weightInKg">Weight (kg)</Label>
                <Controller
                  name="weightInKg"
                  control={control}
                  render={({ field }) => (
                    <NumericInput
                      id="weightInKg"
                      value={field.value ?? ""}
                      onValueChange={(values) => {
                        field.onChange(values.floatValue ?? null);
                      }}
                      placeholder="0.520"
                      disabled={isSubmitting}
                      suffix=" kg"
                      prefix=""
                      decimalScale={3}
                      allowNegative={false}
                    />
                  )}
                />
                {errors.weightInKg && (
                  <p className="text-sm text-red-500">
                    {errors.weightInKg.message}
                  </p>
                )}
              </div>
            )}
          </div>

          {/* Location and Dimensions */}
          {/* <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="location">Location</Label>
              <Input
                id="location"
                {...register('location')}
                placeholder="Aisle 3, Shelf B"
                disabled={isSubmitting}
              />
              {errors.location && (
                <p className="text-sm text-red-500">{errors.location.message}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="dimensions">Dimensions</Label>
              <Input
                id="dimensions"
                {...register('dimensions')}
                placeholder="20cm x 10cm x 25cm"
                disabled={isSubmitting}
              />
              {errors.dimensions && (
                <p className="text-sm text-red-500">{errors.dimensions.message}</p>
              )}
            </div>
          </div> */}

          {/* Supplier Information */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="supplierName">Supplier Name</Label>
              <Input
                id="supplierName"
                {...register("supplierName")}
                placeholder="Coca-Cola Bottling Company"
                disabled={isSubmitting}
              />
              {errors.supplierName && (
                <p className="text-sm text-red-500">
                  {errors.supplierName.message}
                </p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="supplierContact">Supplier Contact</Label>
              <Input
                id="supplierContact"
                {...register("supplierContact")}
                placeholder="+234-800-COCA-COLA"
                disabled={isSubmitting}
              />
              {errors.supplierContact && (
                <p className="text-sm text-red-500">
                  {errors.supplierContact.message}
                </p>
              )}
            </div>
          </div>

          {/* Tax and Discount Options */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="flex items-center space-x-2">
              <Checkbox
                id="isTaxable"
                checked={watch("isTaxable") ?? true}
                onCheckedChange={(checked) =>
                  setValue("isTaxable", checked === true)
                }
                disabled={isSubmitting}
              />
              <Label htmlFor="isTaxable" className="cursor-pointer">
                Taxable Product
              </Label>
            </div>

            <div className="flex items-center space-x-2">
              <Checkbox
                id="isDiscountable"
                checked={watch("isDiscountable") ?? true}
                onCheckedChange={(checked) =>
                  setValue("isDiscountable", checked === true)
                }
                disabled={isSubmitting}
              />
              <Label htmlFor="isDiscountable" className="cursor-pointer">
                Discountable Product
              </Label>
            </div>
          </div>

          {/* Status (only show for edit mode) */}
          {product && (
            <div className="space-y-2">
              <Label htmlFor="status">Status</Label>
              <Select
                value={status || undefined}
                onValueChange={(value) =>
                  setValue("status", value as ProductStatus)
                }
                disabled={isSubmitting}
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value={ProductStatus.ACTIVE}>Active</SelectItem>
                  <SelectItem value={ProductStatus.INACTIVE}>
                    Inactive
                  </SelectItem>
                  <SelectItem value={ProductStatus.DISCONTINUED}>
                    Discontinued
                  </SelectItem>
                </SelectContent>
              </Select>
            </div>
          )}
        </CardContent>
      </Card>

      {/* Form Actions */}
      <div className="flex justify-end gap-3">
        <Button
          type="button"
          variant="outline"
          onClick={onCancel}
          disabled={isSubmitting}
        >
          Cancel
        </Button>
        <Button type="submit" disabled={isSubmitting}>
          {isSubmitting && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
          {product ? "Update Product" : "Create Product"}
        </Button>
      </div>
    </form>
  );
};
