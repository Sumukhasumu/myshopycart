
import {
  Injectable,
  Component,
  computed,
  inject,
  signal,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { ProductCatalogService } from '../../../core/services/product-catalog.service';
import { DatePipe } from '@angular/common';

export interface DistributorInventoryItem {
  id: number;
  productId: number;
  availableStock: number;
  reservedStock: number;
}

export interface InventoryTransaction {
  id: number;
  productId: number;
  transactionType: string;
  quantity: number;
  previousAvailableStock: number;
  newAvailableStock: number;
  reason: string;
  createdAt: Date;
}

@Injectable({ providedIn: 'root' })
export class DistributorInventoryService {
  private readonly inventoryState =
    signal<DistributorInventoryItem[]>([]);

  private readonly transactionState =
    signal<InventoryTransaction[]>([]);

  private transactionId = 0;

  readonly inventory = this.inventoryState.asReadonly();
  readonly transactions = this.transactionState.asReadonly();

  private recordTransaction(
    productId: number,
    transactionType: string,
    quantity: number,
    previousAvailableStock: number,
    newAvailableStock: number,
    reason: string,
  ): void {
    this.transactionState.update((items) => [
      {
        id: ++this.transactionId,
        productId,
        transactionType,
        quantity,
        previousAvailableStock,
        newAvailableStock,
        reason,
        createdAt: new Date(),
      },
      ...items,
    ]);
  }

  createInventory(productId: number, availableStock: number): boolean {
    if (
      this.inventoryState().some(
        (item) => item.productId === productId,
      )
    ) {
      return false;
    }

    this.inventoryState.update((items) => [
      ...items,
      {
        id: Date.now(),
        productId,
        availableStock,
        reservedStock: 0,
      },
    ]);

    this.recordTransaction(
      productId,
      'INITIALIZE',
      availableStock,
      0,
      availableStock,
      'Initial inventory created',
    );

    return true;
  }

  addStock(productId: number, quantity: number): boolean {
    const item = this.inventoryState().find(
      (entry) => entry.productId === productId,
    );

    if (!item) {
      return false;
    }

    const previousStock = item.availableStock;
    const newStock = previousStock + quantity;

    this.inventoryState.update((items) =>
      items.map((entry) =>
        entry.productId === productId
          ? { ...entry, availableStock: newStock }
          : entry,
      ),
    );

    this.recordTransaction(
      productId,
      'ADD_STOCK',
      quantity,
      previousStock,
      newStock,
      'Additional stock added',
    );

    return true;
  }

  adjustStock(
    productId: number,
    quantity: number,
    operation: 'INCREASE' | 'DECREASE',
    reason: string,
  ): boolean {
    const item = this.inventoryState().find(
      (entry) => entry.productId === productId,
    );

    if (!item) {
      return false;
    }

    const previousStock = item.availableStock;

    const newStock =
      operation === 'INCREASE'
        ? previousStock + quantity
        : previousStock - quantity;

    if (newStock < 0) {
      return false;
    }

    this.inventoryState.update((items) =>
      items.map((entry) =>
        entry.productId === productId
          ? { ...entry, availableStock: newStock }
          : entry,
      ),
    );

    this.recordTransaction(
      productId,
      operation,
      quantity,
      previousStock,
      newStock,
      reason,
    );

    return true;
  }

  getTransactionHistory(productId: number): InventoryTransaction[] {
    return this.transactionState().filter(
      (transaction) => transaction.productId === productId,
    );
  }
}

@Component({
  selector: 'app-distributor-inventory-page',
  imports: [FormsModule, DatePipe],
  templateUrl: './distributor-inventory.html',
  styleUrl: './distributor-inventory.css',
})
export class DistributorInventoryPage {
  private readonly authService = inject(AuthService);
  private readonly productCatalog = inject(ProductCatalogService);
  private readonly inventoryService = inject(DistributorInventoryService);

  readonly submitting = signal(false);
  readonly error = signal('');
  readonly message = signal('');

  selectedProductId = '';
  initialStock = '';
  stockProductId = '';
  additionalStock = '';

  adjustmentProductId = '';
  adjustmentType: 'INCREASE' | 'DECREASE' = 'INCREASE';
  adjustmentQuantity = '';
  adjustmentReason = '';

  readonly historyProductId = signal<number | null>(null);

  readonly history = computed(() => {
    const productId = this.historyProductId();

    return productId === null
      ? []
      : this.inventoryService.getTransactionHistory(productId);
  });

  readonly user = this.authService.currentUser;

  readonly products = computed(() => {
    const username = this.user()?.username;

    if (!username) {
      return [];
    }

    return this.productCatalog.products().filter(
      (product) => product.distributorId === username,
    );
  });

  readonly inventory = computed(() => {
    const productIds = new Set(
      this.products().map((product) => product.id),
    );

    return this.inventoryService.inventory().filter(
      (item) => productIds.has(item.productId),
    );
  });

  readonly productsWithoutInventory = computed(() => {
    const inventoryProductIds = new Set(
      this.inventory().map((item) => item.productId),
    );

    return this.products().filter(
      (product) => !inventoryProductIds.has(product.id),
    );
  });

  createInventory(): void {
    this.error.set('');
    this.message.set('');

    if (!this.selectedProductId || this.initialStock === '') {
      this.error.set('Select a product and enter initial stock.');
      return;
    }

    const quantity = Number(this.initialStock);

    if (!Number.isInteger(quantity) || quantity < 0) {
      this.error.set(
        'Initial stock must be a non-negative whole number.',
      );
      return;
    }

    this.submitting.set(true);

    const created = this.inventoryService.createInventory(
      Number(this.selectedProductId),
      quantity,
    );

    if (!created) {
      this.error.set('Inventory already exists for this product.');
    } else {
      this.message.set('Inventory initialized successfully.');
      this.selectedProductId = '';
      this.initialStock = '';
    }

    this.submitting.set(false);
  }

  addStock(): void {
    this.error.set('');
    this.message.set('');

    if (!this.stockProductId || this.additionalStock === '') {
      this.error.set('Select a product and enter the stock quantity.');
      return;
    }

    const quantity = Number(this.additionalStock);

    if (!Number.isInteger(quantity) || quantity <= 0) {
      this.error.set(
        'Additional stock must be a positive whole number.',
      );
      return;
    }

    this.submitting.set(true);

    const added = this.inventoryService.addStock(
      Number(this.stockProductId),
      quantity,
    );

    if (!added) {
      this.error.set('Unable to find the selected inventory record.');
    } else {
      this.message.set('Stock added successfully.');
      this.stockProductId = '';
      this.additionalStock = '';
    }

    this.submitting.set(false);
  }

  adjustStock(): void {
    this.error.set('');
    this.message.set('');

    if (
      !this.adjustmentProductId ||
      this.adjustmentQuantity === '' ||
      !this.adjustmentReason.trim()
    ) {
      this.error.set(
        'Select a product, enter a quantity and provide a reason.',
      );
      return;
    }

    const quantity = Number(this.adjustmentQuantity);

    if (!Number.isInteger(quantity) || quantity <= 0) {
      this.error.set(
        'Adjustment quantity must be a positive whole number.',
      );
      return;
    }

    this.submitting.set(true);

    const adjusted = this.inventoryService.adjustStock(
      Number(this.adjustmentProductId),
      quantity,
      this.adjustmentType,
      this.adjustmentReason.trim(),
    );

    if (!adjusted) {
      this.error.set(
        'Adjustment failed. Decrease quantity cannot exceed available stock.',
      );
    } else {
      this.message.set('Stock adjusted successfully.');
      this.adjustmentQuantity = '';
      this.adjustmentReason = '';
    }

    this.submitting.set(false);
  }

  viewHistory(productId: number): void {
    this.historyProductId.set(productId);
  }

  closeHistory(): void {
    this.historyProductId.set(null);
  }

  refreshInventory(): void {
    this.error.set('');
    this.message.set('Inventory refreshed successfully.');
  }
}
