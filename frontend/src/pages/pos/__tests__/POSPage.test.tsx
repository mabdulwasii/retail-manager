import { getMockProductsList } from "@/testData/products";
import { getMockSale } from "@/testData/sales";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { render, screen } from "@testing-library/react";
import { http, HttpResponse } from "msw";
import { setupServer } from "msw/node";
import React from "react";
import { MemoryRouter } from "react-router-dom";
import { POSPage } from "../POSPage";

const server = setupServer(
  http.get("*/products/search", () =>
    HttpResponse.json(getMockProductsList().content)
  ),
  http.post("*/shops/:shopId/sales", () => HttpResponse.json(getMockSale())),
  http.get("*/sales/:transactionId/receipt", async () => {
    const blob = new Blob(["PDF"], { type: "application/pdf" });
    return HttpResponse.arrayBuffer(await blob.arrayBuffer());
  })
);

jest.mock("@react-pdf/renderer", () => ({
  pdf: jest.fn(() => ({ toBlob: jest.fn().mockResolvedValue(new Blob()) })),
  Document: jest.fn(),
  Page: jest.fn(),
  Text: jest.fn(),
  View: jest.fn(),
  StyleSheet: { create: jest.fn(() => ({})) },
}));

jest.mock("@/hooks/useCurrency", () => ({
  useCurrency: () => ({
    formatCurrency: (amount: number) => `$${amount.toFixed(2)}`,
  }),
}));

jest.mock("@/context/UnifiedAuthContext", () => ({
  useAuth: () => ({
    user: {
      id: "user1",
      username: "cashier",
      firstName: "John",
      lastName: "Doe",
      shopId: "shop1",
    },
  }),
}));

jest.mock("@/context/ShopContext", () => ({
  useShopContext: () => ({
    selectedShopId: "shop1",
    setSelectedShopId: jest.fn(),
  }),
}));

jest.mock("sonner", () => ({
  toast: { success: jest.fn(), error: jest.fn() },
}));

jest.mock("@/components/sales/ProductSearch", () => ({
  ProductSearch: () => <div data-testid="product-search">Product Search</div>,
}));

jest.mock("@/components/sales/ShoppingCart", () => ({
  ShoppingCart: () => <div data-testid="shopping-cart">Shopping Cart</div>,
}));

jest.mock("@/components/sales/PaymentModal", () => ({
  PaymentModal: ({ isOpen }: { isOpen: boolean }) =>
    isOpen ? <div data-testid="payment-modal">Payment Modal</div> : null,
}));

jest.mock("@/components/ui/shop-selector", () => ({
  ShopSelector: () => (
    <select data-testid="shop-selector">
      <option>Shop 1</option>
    </select>
  ),
}));

describe("POSPage with MSW", () => {
  let queryClient: QueryClient;
  let wrapper: React.FC<{ children: React.ReactNode }>;

  beforeAll(() => {
    server.listen({ onUnhandledRequest: "warn" });
    global.URL.createObjectURL = jest.fn();
    global.URL.revokeObjectURL = jest.fn();
    global.window.open = jest.fn();
  });

  beforeEach(() => {
    queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false },
        mutations: { retry: false },
      },
    });
    wrapper = ({ children }) =>
      React.createElement(
        QueryClientProvider,
        { client: queryClient },
        React.createElement(MemoryRouter, {}, children)
      );
    window.confirm = jest.fn().mockReturnValue(false);
    jest.clearAllMocks();
  });

  afterEach(() => {
    server.resetHandlers();
    queryClient.clear();
  });

  afterAll(() => server.close());

  it("should render POS page with header", () => {
    render(<POSPage />, { wrapper });
    expect(screen.getByText("Point of Sale")).toBeInTheDocument();
    expect(screen.getByText(/Cashier: John Doe/)).toBeInTheDocument();
  });

  it("should render shop selector", () => {
    render(<POSPage />, { wrapper });
    expect(screen.getByTestId("shop-selector")).toBeInTheDocument();
  });

  it("should render product search", () => {
    render(<POSPage />, { wrapper });
    expect(screen.getByTestId("product-search")).toBeInTheDocument();
  });

  it("should show empty cart initially", () => {
    render(<POSPage />, { wrapper });
    expect(screen.getByText("Cart is empty")).toBeInTheDocument();
  });

  it("should show scan mode toggle", () => {
    render(<POSPage />, { wrapper });
    expect(screen.getByText("Scan Mode")).toBeInTheDocument();
  });
});
