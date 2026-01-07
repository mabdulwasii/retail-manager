import { Alert, AlertDescription } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { useAuth } from "@/context/UnifiedAuthContext";
import { apiService } from "@/services/api";
import { UserProfileResponse } from "@/types/user";
import { UserRole } from "@/types/roles";
import {
  AlertCircle,
  Building,
  Calendar,
  Edit,
  Loader2,
  Mail,
  Phone,
  Shield,
  Store,
  User,
} from "lucide-react";
import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { toast } from "sonner";

export const ProfilePage: React.FC = () => {
  const { user: authUser, isAuthenticated } = useAuth();
  const [profile, setProfile] = useState<UserProfileResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchProfile = async () => {
      if (!isAuthenticated) {
        setIsLoading(false);
        return;
      }

      try {
        setIsLoading(true);
        setError(null);
        const profileData = await apiService.getUserProfile();
        setProfile(profileData);
      } catch (err) {
        console.error("Failed to fetch user profile:", err);
        setError("Failed to load profile information. Please try again.");
      } finally {
        setIsLoading(false);
      }
    };

    fetchProfile();
  }, [isAuthenticated]);

  const formatDate = (dateString?: string) => {
    if (!dateString) return "N/A";
    return new Date(dateString).toLocaleDateString("en-US", {
      year: "numeric",
      month: "long",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  const getStatusColor = (status: string) => {
    switch (status?.toUpperCase()) {
      case "ACTIVE":
        return "bg-green-100 text-green-800";
      case "INACTIVE":
        return "bg-gray-100 text-gray-800";
      case "SUSPENDED":
        return "bg-red-100 text-red-800";
      default:
        return "bg-gray-100 text-gray-800";
    }
  };

  const getRoleBadgeColor = (roleName: string) => {
    switch (roleName?.toUpperCase()) {
      case UserRole.TENANT_ADMIN:
        return "bg-purple-100 text-purple-800";
      case UserRole.SHOP_OWNER: // Backend sends 'OWNER' which maps to SHOP_OWNER enum
        return "bg-indigo-100 text-indigo-800";
      case UserRole.MANAGER:
        return "bg-blue-100 text-blue-800";
      case UserRole.EMPLOYEE:
        return "bg-green-100 text-green-800";
      case UserRole.INVESTOR:
        return "bg-yellow-100 text-yellow-800";
      case UserRole.CASHIER:
        return "bg-cyan-100 text-cyan-800";
      default:
        return "bg-gray-100 text-gray-800";
    }
  };

  if (!isAuthenticated) {
    return (
      <div className="container mx-auto py-8">
        <Alert>
          <AlertCircle className="h-4 w-4" />
          <AlertDescription>
            You must be logged in to view your profile.
          </AlertDescription>
        </Alert>
      </div>
    );
  }

  if (isLoading) {
    return (
      <div className="container mx-auto py-8">
        <div className="flex items-center justify-center">
          <Loader2 className="h-8 w-8 animate-spin" />
          <span className="ml-2">Loading profile...</span>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="container mx-auto py-8">
        <Alert variant="destructive">
          <AlertCircle className="h-4 w-4" />
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      </div>
    );
  }

  const displayProfile = profile || authUser;

  return (
    <div className="container mx-auto py-8 space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Profile</h1>
          <p className="text-muted-foreground">
            Manage your account information and preferences
          </p>
        </div>
        <Button
          variant="outline"
          onClick={() => navigate(`/users/edit/${displayProfile?.id}`)}
        >
          <Edit className="mr-2 h-4 w-4" />
          Edit Profile
        </Button>
      </div>

      <div className="grid gap-6 md:grid-cols-2">
        {/* Personal Information */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <User className="h-5 w-5" />
              Personal Information
            </CardTitle>
            <CardDescription>
              Your basic account details and contact information
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="text-sm font-medium text-gray-500">
                  First Name
                </label>
                <p className="text-sm font-medium">
                  {displayProfile?.firstName || "Not provided"}
                </p>
              </div>
              <div>
                <label className="text-sm font-medium text-gray-500">
                  Last Name
                </label>
                <p className="text-sm font-medium">
                  {displayProfile?.lastName || "Not provided"}
                </p>
              </div>
            </div>

            <div>
              <label className="text-sm font-medium text-gray-500">
                Full Name
              </label>
              <p className="text-sm font-medium">
                {displayProfile?.fullName ||
                  displayProfile?.username ||
                  "Not provided"}
              </p>
            </div>

            <div>
              <label className="text-sm font-medium text-gray-500">
                Username
              </label>
              <p className="text-sm font-medium">{displayProfile?.username}</p>
            </div>

            <div className="flex items-center gap-2">
              <Mail className="h-4 w-4 text-gray-500" />
              <div>
                <label className="text-sm font-medium text-gray-500">
                  Email
                </label>
                <p className="text-sm font-medium">{displayProfile?.email}</p>
              </div>
            </div>

            {profile?.phoneNumber && (
              <div className="flex items-center gap-2">
                <Phone className="h-4 w-4 text-gray-500" />
                <div>
                  <label className="text-sm font-medium text-gray-500">
                    Phone
                  </label>
                  <p className="text-sm font-medium">{profile.phoneNumber}</p>
                </div>
              </div>
            )}
          </CardContent>
        </Card>

        {/* Account Status & Roles */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Shield className="h-5 w-5" />
              Account & Permissions
            </CardTitle>
            <CardDescription>
              Your account status and assigned roles
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div>
              <label className="text-sm font-medium text-gray-500">
                Account Status
              </label>
              <div className="mt-1">
                <Badge className={getStatusColor(profile?.status || "ACTIVE")}>
                  {profile?.status || "ACTIVE"}
                </Badge>
              </div>
            </div>

            <div>
              <label className="text-sm font-medium text-gray-500">Roles</label>
              <div className="mt-2 flex flex-wrap gap-2">
                {displayProfile?.roles?.map((role) => (
                  <Badge
                    key={typeof role === "string" ? role : role.name}
                    variant="secondary"
                    className={getRoleBadgeColor(
                      typeof role === "string" ? role : role.name
                    )}
                  >
                    {(typeof role === "string" ? role : role.name).replace(
                      /_/g,
                      " "
                    )}
                  </Badge>
                )) || (
                  <span className="text-sm text-gray-500">
                    No roles assigned
                  </span>
                )}
              </div>
            </div>

            {profile?.isInvestor && (
              <div>
                <label className="text-sm font-medium text-gray-500">
                  Investor Status
                </label>
                <div className="mt-1">
                  <Badge className="bg-yellow-100 text-yellow-800">
                    Investor
                  </Badge>
                </div>
              </div>
            )}

            {displayProfile?.tenantId && (
              <div className="flex items-center gap-2">
                <Building className="h-4 w-4 text-gray-500" />
                <div>
                  <label className="text-sm font-medium text-gray-500">
                    Tenant ID
                  </label>
                  <p className="text-sm font-medium font-mono">
                    {displayProfile.tenantId}
                  </p>
                </div>
              </div>
            )}

            {displayProfile?.shopId && (
              <div className="flex items-center gap-2">
                <Store className="h-4 w-4 text-gray-500" />
                <div>
                  <label className="text-sm font-medium text-gray-500">
                    Shop ID
                  </label>
                  <p className="text-sm font-medium font-mono">
                    {displayProfile.shopId}
                  </p>
                </div>
              </div>
            )}
          </CardContent>
        </Card>

        {/* Account Timeline */}
        {profile && (
          <Card className="md:col-span-2">
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Calendar className="h-5 w-5" />
                Account Timeline
              </CardTitle>
              <CardDescription>
                Important dates in your account history
              </CardDescription>
            </CardHeader>
            <CardContent>
              <div className="grid md:grid-cols-2 gap-4">
                <div>
                  <label className="text-sm font-medium text-gray-500">
                    Account Created
                  </label>
                  <p className="text-sm font-medium">
                    {formatDate(profile.createdAt)}
                  </p>
                </div>
                <div>
                  <label className="text-sm font-medium text-gray-500">
                    Last Updated
                  </label>
                  <p className="text-sm font-medium">
                    {formatDate(profile.updatedAt)}
                  </p>
                </div>
              </div>
            </CardContent>
          </Card>
        )}
      </div>

      {/* Actions */}
      <Card>
        <CardHeader>
          <CardTitle>Account Actions</CardTitle>
          <CardDescription>
            Manage your account settings and preferences
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="flex flex-wrap gap-4">
            <Button
              variant="outline"
              onClick={() => navigate(`/users/edit/${displayProfile?.id}`)}
            >
              <Edit className="mr-2 h-4 w-4" />
              Edit Profile
            </Button>
            <Button
              variant="outline"
              onClick={() =>
                toast.info("Security Settings feature is coming soon", {
                  description:
                    "Password change and security settings will be available in a future update",
                })
              }
            >
              <Shield className="mr-2 h-4 w-4" />
              Security Settings
            </Button>
            <Button
              variant="outline"
              onClick={() =>
                toast.info("Notification Preferences feature is coming soon", {
                  description:
                    "Email and notification preferences will be available in a future update",
                })
              }
            >
              <Mail className="mr-2 h-4 w-4" />
              Notification Preferences
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};
