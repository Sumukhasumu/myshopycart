
import { Injectable, computed, signal } from '@angular/core';
import { CartItem } from './store-state.service';
import { DeliveryAddress } from './checkout-state.service';

export type DemoOrderStatus =
  | 'CREATED'
  | 'CONFIRMED'
  | 'PROCESSING'
  | 'SHIPPED'
  | 'DELIVERED'
  | 'CANCELLED';

export type DemoPaymentMethod =
  | 'UPI'
  | 'CARD'
  | 'DIGITAL_WALLET';

export interface DemoOrderItem {
  productId: number;
  productName: string;
  price: number;
  quantity: number;
  image: string;
}

export interface DemoOrder {
  id: string;
  status: DemoOrderStatus;
  createdAt: string;
  updatedAt: string;
  items: DemoOrderItem[];
  totalAmount: number;
  customer: {
    fullName: string;
    phone: string;
  };
  shippingAddress: DeliveryAddress;
  paymentMethod?: DemoPaymentMethod;
  paymentStatus?: 'SUCCESS' | 'FAILURE';
}

export interface DemoInvoice {
  invoiceNumber: string;
  issueDate: string;
  paymentMethod: DemoPaymentMethod;
  customer: {
    name: string;
    email: string;
    phone: string;
  };
  billingAddress: DeliveryAddress;
  shippingAddress: DeliveryAddress;
  items: Array<{
    productName: string;
    productId: number;
    unitPrice: number;
    quantity: number;
    lineTotal: number;
  }>;
  subtotal: number;
  taxAmount: number;
  totalAmount: number;
}

@Injectable({
  providedIn: 'root',
})
export class DemoOrderStateService {
  private readonly _orders = signal<DemoOrder[]>([]);

  readonly orders = this._orders.asReadonly();

  readonly orderCount = computed(() => this._orders().length);

  createOrder(
    cartItems: CartItem[],
    address: DeliveryAddress,
  ): DemoOrder | null {
    if (cartItems.length === 0) {
      return null;
    }

    const now = new Date().toISOString();
    const orderId = `DEMO-${Date.now()}`;

    const items: DemoOrderItem[] = cartItems.map((item) => ({
      productId: item.id,
      productName: item.name,
      price: item.price,
      quantity: item.quantity,
      image: item.image,
    }));

    const totalAmount = items.reduce(
      (total, item) => total + item.price * item.quantity,
      0,
    );

    const order: DemoOrder = {
      id: orderId,
      status: 'CREATED',
      createdAt: now,
      updatedAt: now,
      items,
      totalAmount,
      customer: {
        fullName: address.fullName,
        phone: address.mobile,
      },
      shippingAddress: { ...address },
    };

    this._orders.update((orders) => [order, ...orders]);

    return order;
  }

  getOrderById(orderId: string): DemoOrder | undefined {
    return this._orders().find((order) => order.id === orderId);
  }

  updatePayment(
    orderId: string,
    method: DemoPaymentMethod,
    succeeded: boolean,
  ): DemoOrder | null {
    const existing = this.getOrderById(orderId);

    if (!existing || existing.status !== 'CREATED') {
      return null;
    }

    const now = new Date().toISOString();

    const updated: DemoOrder = {
      ...existing,
      status: succeeded ? 'CONFIRMED' : 'CANCELLED',
      updatedAt: now,
      paymentMethod: method,
      paymentStatus: succeeded ? 'SUCCESS' : 'FAILURE',
    };

    this._orders.update((orders) =>
      orders.map((order) =>
        order.id === orderId ? updated : order,
      ),
    );

    return updated;
  }

  getInvoice(orderId: string): DemoInvoice | null {
    const order = this.getOrderById(orderId);

    if (
      !order ||
      order.status === 'CANCELLED' ||
      !order.paymentMethod
    ) {
      return null;
    }

    const subtotal = order.items.reduce(
      (total, item) => total + item.price * item.quantity,
      0,
    );

    const taxAmount = 0;
    const totalAmount = subtotal + taxAmount;

    return {
      invoiceNumber: `INV-${order.id}`,
      issueDate: order.updatedAt,
      paymentMethod: order.paymentMethod,
      customer: {
        name: order.customer.fullName,
        email: 'demo@example.com',
        phone: order.customer.phone,
      },
      billingAddress: { ...order.shippingAddress },
      shippingAddress: { ...order.shippingAddress },
      items: order.items.map((item) => ({
        productName: item.productName,
        productId: item.productId,
        unitPrice: item.price,
        quantity: item.quantity,
        lineTotal: item.price * item.quantity,
      })),
      subtotal,
      taxAmount,
      totalAmount,
    };
  }

  clearOrders(): void {
    this._orders.set([]);
  }
}
