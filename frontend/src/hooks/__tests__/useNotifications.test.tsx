/* eslint-disable @typescript-eslint/no-explicit-any */
import { act, renderHook } from "@testing-library/react";
import { useNotifications } from "../useNotifications";

// Mock AuthContext - useNotifications uses AuthContext not ManualAuthContext
jest.mock("@/context/UnifiedAuthContext", () => ({
  useAuth: () => ({
    user: {
      id: "user1",
      username: "testuser",
      email: "test@example.com",
    },
  }),
}));

describe.skip("useNotifications", () => {
  beforeEach(() => {
    jest.clearAllMocks();
    jest.useFakeTimers();
    // Mock browser Notification API
    globalThis.Notification = {
      permission: "default",
      requestPermission: jest.fn().mockResolvedValue("default"),
    } as any;
  });

  afterEach(() => {
    jest.clearAllTimers();
    jest.useRealTimers();
  });

  it("should initialize with default state", () => {
    const { result } = renderHook(() => useNotifications());

    expect(result.current.notifications).toBeDefined();
    expect(result.current.unreadCount).toBeGreaterThanOrEqual(0);
    expect(result.current.isLoading).toBe(false);
    expect(result.current.preferences).toEqual({
      emailEnabled: true,
      smsEnabled: false,
      pushEnabled: true,
      fraudAlerts: true,
      riskAssessments: true,
      systemUpdates: true,
    });
  });

  it("should load notifications on mount", () => {
    const { result } = renderHook(() => useNotifications());

    act(() => {
      jest.advanceTimersByTime(100);
    });

    expect(result.current.notifications.length).toBeGreaterThan(0);
  });

  it("should calculate unread count correctly", () => {
    const { result } = renderHook(() => useNotifications());

    act(() => {
      jest.advanceTimersByTime(100);
    });

    const unreadNotifications = result.current.notifications.filter(
      (n) => !n.read
    );
    expect(result.current.unreadCount).toBe(unreadNotifications.length);
  });

  it("should mark notification as read", () => {
    const { result } = renderHook(() => useNotifications());

    act(() => {
      jest.advanceTimersByTime(100);
    });

    const unreadNotification = result.current.notifications.find(
      (n) => !n.read
    );
    if (!unreadNotification) return;

    const initialUnreadCount = result.current.unreadCount;

    act(() => {
      result.current.markAsRead(unreadNotification.id);
    });

    const updatedNotification = result.current.notifications.find(
      (n) => n.id === unreadNotification.id
    );
    expect(updatedNotification?.read).toBe(true);
    expect(result.current.unreadCount).toBe(initialUnreadCount - 1);
  });

  it("should mark all notifications as read", () => {
    const { result } = renderHook(() => useNotifications());

    act(() => {
      jest.advanceTimersByTime(100);
    });

    act(() => {
      result.current.markAllAsRead();
    });

    expect(result.current.notifications.every((n) => n.read)).toBe(true);
    expect(result.current.unreadCount).toBe(0);
  });

  it("should delete notification", () => {
    const { result } = renderHook(() => useNotifications());

    act(() => {
      jest.advanceTimersByTime(100);
    });

    const notificationToDelete = result.current.notifications[0];
    const initialCount = result.current.notifications.length;

    act(() => {
      result.current.deleteNotification(notificationToDelete.id);
    });

    expect(result.current.notifications.length).toBe(initialCount - 1);
    expect(
      result.current.notifications.find((n) => n.id === notificationToDelete.id)
    ).toBeUndefined();
  });

  it("should update unread count when deleting unread notification", () => {
    const { result } = renderHook(() => useNotifications());

    act(() => {
      jest.advanceTimersByTime(100);
    });

    const unreadNotification = result.current.notifications.find(
      (n) => !n.read
    );
    if (!unreadNotification) return;

    const initialUnreadCount = result.current.unreadCount;

    act(() => {
      result.current.deleteNotification(unreadNotification.id);
    });

    expect(result.current.unreadCount).toBe(initialUnreadCount - 1);
  });

  it("should add new notification", () => {
    const { result } = renderHook(() => useNotifications());

    act(() => {
      jest.advanceTimersByTime(100);
    });

    const initialCount = result.current.notifications.length;
    const initialUnreadCount = result.current.unreadCount;

    const newNotification = {
      type: "INFO" as const,
      title: "Test Notification",
      message: "This is a test",
      severity: "low" as const,
      read: false,
    };

    act(() => {
      result.current.addNotification(newNotification);
    });

    expect(result.current.notifications.length).toBe(initialCount + 1);
    expect(result.current.unreadCount).toBe(initialUnreadCount + 1);
    expect(result.current.notifications[0].title).toBe("Test Notification");
  });

  it("should not increase unread count when adding read notification", () => {
    const { result } = renderHook(() => useNotifications());

    act(() => {
      jest.advanceTimersByTime(100);
    });

    const initialUnreadCount = result.current.unreadCount;

    act(() => {
      result.current.addNotification({
        type: "INFO" as const,
        title: "Read Notification",
        message: "Already read",
        severity: "low" as const,
        read: true,
      });
    });

    expect(result.current.unreadCount).toBe(initialUnreadCount);
  });

  it("should update notification preferences", async () => {
    const { result } = renderHook(() => useNotifications());

    const newPreferences = {
      emailEnabled: false,
      smsEnabled: true,
    };

    await act(async () => {
      await result.current.updatePreferences(newPreferences);
    });

    expect(result.current.preferences.emailEnabled).toBe(false);
    expect(result.current.preferences.smsEnabled).toBe(true);
    expect(result.current.preferences.pushEnabled).toBe(true); // Unchanged
  });

  it("should filter notifications by type", () => {
    const { result } = renderHook(() => useNotifications());

    act(() => {
      jest.advanceTimersByTime(100);
    });

    const fraudAlerts = result.current.getNotificationsByType("FRAUD_ALERT");

    expect(fraudAlerts.every((n) => n.type === "FRAUD_ALERT")).toBe(true);
  });

  it("should get unread notifications", () => {
    const { result } = renderHook(() => useNotifications());

    act(() => {
      jest.advanceTimersByTime(100);
    });

    const unreadNotifications = result.current.getUnreadNotifications();

    expect(unreadNotifications.every((n) => !n.read)).toBe(true);
    expect(unreadNotifications.length).toBe(result.current.unreadCount);
  });

  it("should get fraud alert notifications", () => {
    const { result } = renderHook(() => useNotifications());

    act(() => {
      jest.advanceTimersByTime(100);
    });

    const fraudNotifications = result.current.getFraudAlertNotifications();

    expect(
      fraudNotifications.every(
        (n) => n.type === "FRAUD_ALERT" || n.type === "RISK_ASSESSMENT"
      )
    ).toBe(true);
  });

  it("should have all required methods", () => {
    const { result } = renderHook(() => useNotifications());

    expect(typeof result.current.markAsRead).toBe("function");
    expect(typeof result.current.markAllAsRead).toBe("function");
    expect(typeof result.current.deleteNotification).toBe("function");
    expect(typeof result.current.addNotification).toBe("function");
    expect(typeof result.current.updatePreferences).toBe("function");
    expect(typeof result.current.getNotificationsByType).toBe("function");
    expect(typeof result.current.getUnreadNotifications).toBe("function");
    expect(typeof result.current.getFraudAlertNotifications).toBe("function");
  });
});
