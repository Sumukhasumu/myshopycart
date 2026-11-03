
import { Component, computed, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NgClass } from '@angular/common';
import {
  DemoOrder,
  DemoOrderStateService,
} from '../../core/services/demo-order-state.service';

@Component({
  selector: 'app-orders-page',
  imports: [RouterLink, NgClass],
  templateUrl: './orders.html',
  styleUrl: './orders.css',
})
export class OrdersPage {
  private readonly demoOrderState = inject(DemoOrderStateService);

  readonly orders = this.demoOrderState.orders;

  readonly hasOrders = computed(() => this.orders().length > 0);

  formatPrice(price: number): string {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 2,
    }).format(price);
  }

  formatDate(date: string): string {
    return new Intl.DateTimeFormat('en-IN', {
      dateStyle: 'medium',
      timeStyle: 'short',
    }).format(new Date(date));
  }

  getStatusClass(status: DemoOrder['status']): string {
    switch (status) {
      case 'CREATED':
        return 'order-status--created';
      case 'CONFIRMED':
        return 'order-status--confirmed';
      case 'PROCESSING':
        return 'order-status--processing';
      case 'SHIPPED':
        return 'order-status--shipped';
      case 'DELIVERED':
        return 'order-status--delivered';
      case 'CANCELLED':
        return 'order-status--cancelled';
      default:
        return 'order-status--unknown';
    }
  }

  getItemCount(order: DemoOrder): number {
    return order.items.reduce(
      (total, item) => total + item.quantity,
      0,
    );
  }

  getLineTotal(price: number, quantity: number): number {
    return price * quantity;
  }
}
