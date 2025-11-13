import {
  ProductFilters,
  ProductFilterValues,
} from "@/components/products/ProductFilters";
import { ProductList } from "@/components/products/ProductList";
import { ProductStats } from "@/components/products/ProductStats";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import { Button } from "@/components/ui/button";
import {
  Pagination,
  PaginationContent,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
} from "@/components/ui/pagination";
import { usePermissions } from "@/hooks/usePermissions";
import {
  useDeleteProduct,
  useProducts,
  useUpdateProductStatus,
} from "@/hooks/useProducts";
import { Product, ProductStatus } from "@/types/api";
import { Plus, Tag } from "lucide-react";
import React, { useState } from "react";
import { useNavigate } from "react-router-dom";

export const ProductsPage: React.FC = () => {
  const navigate = useNavigate();
  const permissions = usePermissions();

  const [currentPage, setCurrentPage] = useState(0);

  // Check permissions based on backend permission matrix
  const canCreateProduct = permissions.canCreateProduct();
  const canUpdateProduct = permissions.canEditProduct();
  const canDeleteProduct = permissions.canDeleteProduct();
  const canViewCategories = permissions.canViewCategories();
  const [filters, setFilters] = useState<ProductFilterValues>({
    search: "",
    categoryId: "",
    status: "",
  });
  const [productToDelete, setProductToDelete] = useState<Product | null>(null);

  // Fetch products with filters
  const { products, totalPages, totalElements, isLoading } = useProducts({
    page: currentPage,
    size: 10,
    ...(filters.search && { search: filters.search }),
    ...(filters.categoryId && { categoryId: filters.categoryId }),
    ...(filters.status && { status: filters.status }),
  });

  const deleteProductMutation = useDeleteProduct();
  const updateStatusMutation = useUpdateProductStatus();

  const handleFiltersChange = (newFilters: ProductFilterValues) => {
    setFilters(newFilters);
    setCurrentPage(0); // Reset to first page when filters change
  };

  const handleClearFilters = () => {
    setFilters({ search: "", categoryId: "", status: "" });
    setCurrentPage(0);
  };

  const handleEdit = (product: Product) => {
    navigate(`/products/${product.id}/edit`);
  };

  const handleDelete = (product: Product) => {
    setProductToDelete(product);
  };

  const confirmDelete = async () => {
    if (!productToDelete) return;

    try {
      await deleteProductMutation.mutateAsync(productToDelete.id);
      setProductToDelete(null);
    } catch (error) {
      console.warn("Failed to delete product:", error);
    }
  };

  const handleToggleStatus = async (product: Product) => {
    const newStatus =
      product.status === ProductStatus.ACTIVE
        ? ProductStatus.INACTIVE
        : ProductStatus.ACTIVE;

    try {
      await updateStatusMutation.mutateAsync({
        productId: product.id,
        status: newStatus,
      });
    } catch (error) {
      console.warn("Failed to update product status:", error);
    }
  };

  const handlePageChange = (page: number) => {
    setCurrentPage(page);
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold">Products</h1>
          <p className="text-gray-600 mt-1">
            Manage your product catalog and inventory
          </p>
        </div>
        <div className="flex gap-2">
          {canViewCategories && (
            <Button variant="outline" onClick={() => navigate("/categories")}>
              <Tag className="h-4 w-4 mr-2" />
              Manage Categories
            </Button>
          )}
          {canCreateProduct && (
            <Button onClick={() => navigate("/products/create")}>
              <Plus className="h-4 w-4 mr-2" />
              New Product
            </Button>
          )}
        </div>
      </div>

      {/* Stats Cards */}
      <ProductStats products={products} isLoading={isLoading} />

      {/* Filters */}
      <ProductFilters
        filters={filters}
        onFiltersChange={handleFiltersChange}
        onClear={handleClearFilters}
      />

      {/* Results Count */}
      {!isLoading && (
        <div className="text-sm text-gray-600">
          Showing <strong>{products.length}</strong> of{" "}
          <strong>{totalElements}</strong> products
        </div>
      )}

      {/* Product List */}
      <ProductList
        products={products}
        {...(canUpdateProduct && { onEdit: handleEdit })}
        {...(canDeleteProduct && { onDelete: handleDelete })}
        {...(canUpdateProduct && { onToggleStatus: handleToggleStatus })}
        isLoading={isLoading}
      />

      {/* Pagination */}
      {!isLoading && totalPages > 1 && (
        <Pagination>
          <PaginationContent>
            <PaginationItem>
              <PaginationPrevious
                onClick={() => handlePageChange(currentPage - 1)}
                className={
                  currentPage === 0
                    ? "pointer-events-none opacity-50"
                    : "cursor-pointer"
                }
              />
            </PaginationItem>

            {[...Array(Math.min(5, totalPages))].map((_, idx) => {
              let pageNum = idx;
              if (totalPages > 5 && currentPage > 2) {
                pageNum = currentPage - 2 + idx;
                if (pageNum >= totalPages) pageNum = totalPages - 5 + idx;
              }

              return (
                <PaginationItem key={pageNum}>
                  <PaginationLink
                    onClick={() => handlePageChange(pageNum)}
                    isActive={pageNum === currentPage}
                    className="cursor-pointer"
                  >
                    {pageNum + 1}
                  </PaginationLink>
                </PaginationItem>
              );
            })}

            <PaginationItem>
              <PaginationNext
                onClick={() => handlePageChange(currentPage + 1)}
                className={
                  currentPage === totalPages - 1
                    ? "pointer-events-none opacity-50"
                    : "cursor-pointer"
                }
              />
            </PaginationItem>
          </PaginationContent>
        </Pagination>
      )}

      {/* Delete Confirmation Dialog */}
      <AlertDialog
        open={!!productToDelete}
        onOpenChange={() => setProductToDelete(null)}
      >
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Delete Product</AlertDialogTitle>
            <AlertDialogDescription>
              Are you sure you want to delete "{productToDelete?.name}"? This
              action cannot be undone and will affect all associated inventory
              records.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancel</AlertDialogCancel>
            <AlertDialogAction
              onClick={confirmDelete}
              className="bg-red-600 hover:bg-red-700"
            >
              Delete
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
};
