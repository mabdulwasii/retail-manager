import { Alert, AlertDescription } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { usePermissions } from "@/hooks/usePermissions";
import { useCreateShop } from "@/hooks/useShops";
import { ShopCreateRequest } from "@/services/shopService";
import { yupResolver } from "@hookform/resolvers/yup";
import {
  AlertCircle,
  ArrowLeft,
  CheckCircle2,
  Loader2,
  Store,
} from "lucide-react";
import React from "react";
import { useForm } from "react-hook-form";
import { useNavigate } from "react-router-dom";
import * as yup from "yup";

// Validation schema
const shopSchema = yup.object().shape({
  name: yup
    .string()
    .required("Shop name is required")
    .min(2, "Shop name must be at least 2 characters")
    .max(100, "Shop name must not exceed 100 characters"),
  email: yup
    .string()
    .required("Email is required")
    .email("Must be a valid email address"),
  description: yup
    .string()
    .max(500, "Description must not exceed 500 characters"),
  phoneNumber: yup
    .string()
    .matches(
      /^[+]?[(]?[0-9]{1,4}[)]?[-\s.]?[(]?[0-9]{1,4}[)]?[-\s.]?[0-9]{1,9}$/,
      {
        message: "Phone number is not valid",
        excludeEmptyString: true,
      }
    ),
  address: yup.string().max(200, "Address must not exceed 200 characters"),
  city: yup.string().max(100, "City must not exceed 100 characters"),
  state: yup.string().max(100, "State must not exceed 100 characters"),
  country: yup.string().max(100, "Country must not exceed 100 characters"),
  postalCode: yup.string().max(20, "Postal code must not exceed 20 characters"),
  taxId: yup.string().max(50, "Tax ID must not exceed 50 characters"),
  openingDate: yup.string(),
});

type ShopFormData = yup.InferType<typeof shopSchema>;

export const CreateShopPage: React.FC = () => {
  const navigate = useNavigate();
  const permissions = usePermissions();
  const createShopMutation = useCreateShop();

  const canCreateShop = permissions.canCreateShop();

  // Redirect if no permission
  React.useEffect(() => {
    if (!canCreateShop) {
      navigate("/shops");
    }
  }, [canCreateShop, navigate]);

  if (!canCreateShop) {
    return null;
  }

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
    reset,
  } = useForm<ShopFormData>({
    resolver: yupResolver(shopSchema),
    defaultValues: {
      name: "",
      email: "",
      description: "",
      phoneNumber: "",
      address: "",
      city: "",
      state: "",
      country: "",
      postalCode: "",
      taxId: "",
      openingDate: new Date().toISOString().split("T")[0],
    },
  });

  const onSubmit = async (data: ShopFormData) => {
    try {
      // Convert date to ISO 8601 format if provided
      let isoOpeningDate: string | undefined = undefined;
      if (data.openingDate) {
        const date = new Date(data.openingDate);
        // Set time to noon UTC to avoid timezone issues
        date.setUTCHours(12, 0, 0, 0);
        isoOpeningDate = date.toISOString();
      }

      const shopData: ShopCreateRequest = {
        name: data.name,
        email: data.email,
        ...(data.description && { description: data.description }),
        ...(data.phoneNumber && { phoneNumber: data.phoneNumber }),
        ...(data.address && { address: data.address }),
        ...(data.city && { city: data.city }),
        ...(data.state && { state: data.state }),
        ...(data.country && { country: data.country }),
        ...(data.postalCode && { postalCode: data.postalCode }),
        ...(data.taxId && { taxId: data.taxId }),
        ...(isoOpeningDate && { openingDate: isoOpeningDate }),
      };

      const newShop = await createShopMutation.mutateAsync(shopData);

      // Navigate to the newly created shop's detail page
      if (newShop) {
        navigate(`/shops/${newShop.id}`);
      }
    } catch (error) {
      // Error handling is done in the mutation hook
      console.error("Failed to create shop:", error);
    }
  };

  const handleCancel = () => {
    navigate("/shops");
  };

  return (
    <div className="space-y-6 max-w-4xl">
      {/* Header */}
      <div className="flex flex-col gap-4">
        <Button variant="ghost" className="w-fit" onClick={handleCancel}>
          <ArrowLeft className="mr-2 h-4 w-4" />
          Back to Shops
        </Button>

        <div className="flex items-center gap-3">
          <div className="p-2 bg-primary/10 rounded-lg">
            <Store className="h-6 w-6 text-primary" />
          </div>
          <div>
            <h1 className="text-3xl font-bold tracking-tight">
              Create New Shop
            </h1>
            <p className="text-muted-foreground mt-1">
              Add a new retail location to your business
            </p>
          </div>
        </div>
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
        {/* Basic Information */}
        <Card>
          <CardHeader>
            <CardTitle>Basic Information</CardTitle>
            <CardDescription>Essential details about the shop</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {/* Shop Name */}
              <div className="space-y-2 md:col-span-2">
                <Label htmlFor="name">
                  Shop Name <span className="text-destructive">*</span>
                </Label>
                <Input
                  id="name"
                  {...register("name")}
                  placeholder="Enter shop name"
                  aria-invalid={!!errors.name}
                />
                {errors.name && (
                  <p className="text-sm text-destructive">
                    {errors.name.message}
                  </p>
                )}
              </div>

              {/* Email */}
              <div className="space-y-2">
                <Label htmlFor="email">
                  Email Address <span className="text-destructive">*</span>
                </Label>
                <Input
                  id="email"
                  type="email"
                  {...register("email")}
                  placeholder="shop@example.com"
                  aria-invalid={!!errors.email}
                />
                {errors.email && (
                  <p className="text-sm text-destructive">
                    {errors.email.message}
                  </p>
                )}
              </div>

              {/* Phone Number */}
              <div className="space-y-2">
                <Label htmlFor="phoneNumber">Phone Number</Label>
                <Input
                  id="phoneNumber"
                  type="tel"
                  {...register("phoneNumber")}
                  placeholder="+1 (555) 123-4567"
                  aria-invalid={!!errors.phoneNumber}
                />
                {errors.phoneNumber && (
                  <p className="text-sm text-destructive">
                    {errors.phoneNumber.message}
                  </p>
                )}
              </div>

              {/* Opening Date */}
              <div className="space-y-2">
                <Label htmlFor="openingDate">Opening Date</Label>
                <Input
                  id="openingDate"
                  type="date"
                  {...register("openingDate")}
                  aria-invalid={!!errors.openingDate}
                />
                {errors.openingDate && (
                  <p className="text-sm text-destructive">
                    {errors.openingDate.message}
                  </p>
                )}
              </div>

              {/* Tax ID */}
              <div className="space-y-2">
                <Label htmlFor="taxId">Tax ID / VAT Number</Label>
                <Input
                  id="taxId"
                  {...register("taxId")}
                  placeholder="Enter tax identification number"
                  aria-invalid={!!errors.taxId}
                />
                {errors.taxId && (
                  <p className="text-sm text-destructive">
                    {errors.taxId.message}
                  </p>
                )}
              </div>

              {/* Description */}
              <div className="space-y-2 md:col-span-2">
                <Label htmlFor="description">Description</Label>
                <Textarea
                  id="description"
                  {...register("description")}
                  placeholder="Brief description of the shop (optional)"
                  rows={3}
                  aria-invalid={!!errors.description}
                />
                {errors.description && (
                  <p className="text-sm text-destructive">
                    {errors.description.message}
                  </p>
                )}
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Address Information */}
        <Card>
          <CardHeader>
            <CardTitle>Location & Address</CardTitle>
            <CardDescription>
              Physical location details (optional)
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {/* Street Address */}
              <div className="space-y-2 md:col-span-2">
                <Label htmlFor="address">Street Address</Label>
                <Input
                  id="address"
                  {...register("address")}
                  placeholder="123 Main Street"
                  aria-invalid={!!errors.address}
                />
                {errors.address && (
                  <p className="text-sm text-destructive">
                    {errors.address.message}
                  </p>
                )}
              </div>

              {/* City */}
              <div className="space-y-2">
                <Label htmlFor="city">City</Label>
                <Input
                  id="city"
                  {...register("city")}
                  placeholder="Enter city"
                  aria-invalid={!!errors.city}
                />
                {errors.city && (
                  <p className="text-sm text-destructive">
                    {errors.city.message}
                  </p>
                )}
              </div>

              {/* State */}
              <div className="space-y-2">
                <Label htmlFor="state">State / Province</Label>
                <Input
                  id="state"
                  {...register("state")}
                  placeholder="Enter state or province"
                  aria-invalid={!!errors.state}
                />
                {errors.state && (
                  <p className="text-sm text-destructive">
                    {errors.state.message}
                  </p>
                )}
              </div>

              {/* Postal Code */}
              <div className="space-y-2">
                <Label htmlFor="postalCode">Postal / ZIP Code</Label>
                <Input
                  id="postalCode"
                  {...register("postalCode")}
                  placeholder="12345"
                  aria-invalid={!!errors.postalCode}
                />
                {errors.postalCode && (
                  <p className="text-sm text-destructive">
                    {errors.postalCode.message}
                  </p>
                )}
              </div>

              {/* Country */}
              <div className="space-y-2">
                <Label htmlFor="country">Country</Label>
                <Input
                  id="country"
                  {...register("country")}
                  placeholder="Enter country"
                  aria-invalid={!!errors.country}
                />
                {errors.country && (
                  <p className="text-sm text-destructive">
                    {errors.country.message}
                  </p>
                )}
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Action Buttons */}
        <Card>
          <CardContent className="pt-6">
            <div className="flex flex-col sm:flex-row gap-3 justify-end">
              <Button
                type="button"
                variant="outline"
                onClick={handleCancel}
                disabled={isSubmitting || createShopMutation.isPending}
              >
                Cancel
              </Button>
              <Button
                type="submit"
                disabled={isSubmitting || createShopMutation.isPending}
              >
                {isSubmitting || createShopMutation.isPending ? (
                  <>
                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                    Creating Shop...
                  </>
                ) : (
                  <>
                    <CheckCircle2 className="mr-2 h-4 w-4" />
                    Create Shop
                  </>
                )}
              </Button>
            </div>
          </CardContent>
        </Card>

        {/* Helper Text */}
        <Alert>
          <AlertCircle className="h-4 w-4" />
          <AlertDescription>
            <span className="text-destructive">*</span> Required fields must be
            filled out. All other fields are optional but recommended for
            complete shop information.
          </AlertDescription>
        </Alert>
      </form>
    </div>
  );
};
