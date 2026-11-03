
import { Component, computed, inject } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import {
  DemoOrder,
  DemoOrderStateService,
} from '../../core/services/demo-order-state.service';

@Component({
  selector: 'app-order-details-page',
  imports: [RouterLink],
  templateUrl: './order-details.html',
  styleUrl: './order-details.css',
})
export class OrderDetailsPage {
  private readonly route = inject(ActivatedRoute);
  private readonly demoOrderState = inject(DemoOrderStateService);

  readonly orderId =
    this.route.snapshot.paramMap.get('orderId') ?? '';

  readonly order: DemoOrder | null =
    this.demoOrderState.getOrderById(this.orderId) ?? null;

  readonly invoice = computed(() =>
    this.demoOrderState.getInvoice(this.orderId),
  );

  readonly hasOrder = computed(() => this.order !== null);

  readonly hasInvoice = computed(() => this.invoice() !== null);

  readonly totalQuantity = computed(
    () =>
      this.order?.items.reduce(
        (total, item) => total + item.quantity,
        0,
      ) ?? 0,
  );

  readonly paymentMethodLabel = computed(() => {
    switch (this.order?.paymentMethod) {
      case 'UPI':
        return 'UPI';
      case 'CARD':
        return 'Credit or Debit Card';
      case 'DIGITAL_WALLET':
        return 'Digital Wallet';
      default:
        return 'Not available';
    }
  });

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

  getLineTotal(price: number, quantity: number): number {
    return price * quantity;
  }

  getAddressLines(
    address: DemoOrder['shippingAddress'],
  ): string[] {
    return [
      address.fullName,
      address.mobile,
      address.addressLine,
      address.city,
      address.state,
      address.pincode,
    ].filter((line) => !!line.trim());
  }

  private escapeHtml(value: string): string {
    return value.replace(/[&<>"']/g, (character) => {
      const entities: Record<string, string> = {
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#39;',
      };

      return entities[character];
    });
  }

  downloadInvoice(): void {
    const invoice = this.invoice();
    const order = this.order;

    if (!invoice || !order) {
      return;
    }

    const invoiceRows = invoice.items
      .map(
        (item) => `
          <tr>
            <td>${this.escapeHtml(item.productName)}</td>
            <td>${item.productId}</td>
            <td>${this.formatPrice(item.unitPrice)}</td>
            <td>${item.quantity}</td>
            <td>${this.formatPrice(item.lineTotal)}</td>
          </tr>
        `,
      )
      .join('');

    const billingAddress = this.getAddressLines(
      invoice.billingAddress,
    )
      .map((line) => `<div>${this.escapeHtml(line)}</div>`)
      .join('');

    const shippingAddress = this.getAddressLines(
      invoice.shippingAddress,
    )
      .map((line) => `<div>${this.escapeHtml(line)}</div>`)
      .join('');

    const printWindow = window.open('', '_blank', 'width=900,height=700');

    if (!printWindow) {
      return;
    }

    printWindow.document.write(`
      <!doctype html>
      <html lang="en">
        <head>
          <meta charset="utf-8">
          <title>${this.escapeHtml(invoice.invoiceNumber)}</title>
          <style>
            body {
              font-family: Arial, sans-serif;
              margin: 36px;
              color: #202d42;
            }
            h1, h2, p { margin-top: 0; }
            h1 { color: #2457a7; }
            .meta, .addresses {
              display: grid;
              grid-template-columns: 1fr 1fr;
              gap: 20px;
              margin: 24px 0;
            }
            .panel {
              padding: 16px;
              border: 1px solid #dce3ed;
              border-radius: 8px;
            }
            table {
              width: 100%;
              border-collapse: collapse;
              margin-top: 24px;
            }
            th, td {
              padding: 10px;
              border-bottom: 1px solid #dce3ed;
              text-align: left;
            }
            th { background: #f3f6fa; }
            .totals {
              width: 320px;
              margin: 24px 0 0 auto;
            }
            .totals p {
              display: flex;
              justify-content: space-between;
            }
            .grand-total {
              padding-top: 12px;
              border-top: 2px solid #202d42;
              font-size: 18px;
              font-weight: bold;
            }
            .note {
              margin-top: 36px;
              color: #65738a;
              font-size: 12px;
            }
            @media print {
              body { margin: 16mm; }
            }
          </style>
        </head>
        <body>
          <h1>MYSHOPYCART</h1>
          <h2>Invoice</h2>

          <div class="meta">
            <div>
              <strong>Invoice Number:</strong>
              ${this.escapeHtml(invoice.invoiceNumber)}
              <br><br>
              <strong>Order Number:</strong>
              ${this.escapeHtml(order.id)}
            </div>
            <div>
              <strong>Invoice Date:</strong>
              ${this.escapeHtml(this.formatDate(invoice.issueDate))}
              <br><br>
              <strong>Payment Method:</strong>
              ${this.escapeHtml(this.paymentMethodLabel())}
            </div>
          </div>

          <div class="addresses">
            <div class="panel">
              <h3>Billing Address</h3>
              ${billingAddress}
            </div>
            <div class="panel">
              <h3>Shipping Address</h3>
              ${shippingAddress}
            </div>
          </div>

          <div class="panel">
            <h3>Customer Information</h3>
            <div>${this.escapeHtml(invoice.customer.name)}</div>
            <div>${this.escapeHtml(invoice.customer.email)}</div>
            <div>${this.escapeHtml(invoice.customer.phone)}</div>
          </div>

          <table>
            <thead>
              <tr>
                <th>Product</th>
                <th>Product ID</th>
                <th>Unit Price</th>
                <th>Quantity</th>
                <th>Total</th>
              </tr>
            </thead>
            <tbody>${invoiceRows}</tbody>
          </table>

          <div class="totals">
            <p>
              <span>Subtotal</span>
              <strong>${this.formatPrice(invoice.subtotal)}</strong>
            </p>
            <p>
              <span>Tax</span>
              <strong>${this.formatPrice(invoice.taxAmount)}</strong>
            </p>
            <p class="grand-total">
              <span>Total</span>
              <strong>${this.formatPrice(invoice.totalAmount)}</strong>
            </p>
          </div>

          <p class="note">
            Demo invoice generated by MYSHOPYCART Showcase.
            No real financial transaction was performed.
          </p>

          <script>
            window.onload = () => window.print();
          </script>
        </body>
      </html>
    `);

    printWindow.document.close();
  }
}
