
import { Injectable, signal } from '@angular/core';

export interface DeliveryAddress {
  fullName: string;
  mobile: string;
  addressLine: string;
  city: string;
  state: string;
  pincode: string;
}

@Injectable({
  providedIn: 'root',
})
export class CheckoutStateService {
  private readonly _deliveryAddress = signal<DeliveryAddress>({
    fullName: '',
    mobile: '',
    addressLine: '',
    city: '',
    state: '',
    pincode: '',
  });

  readonly deliveryAddress = this._deliveryAddress.asReadonly();

  saveDeliveryAddress(address: DeliveryAddress): void {
    this._deliveryAddress.set({ ...address });
  }

  useDemoAddress(): void {
    this._deliveryAddress.set({
      fullName: 'Demo Customer',
      mobile: '+91-1234567890',
      addressLine: '123, Demo Street',
      city: 'Bengaluru',
      state: 'Karnataka',
      pincode: '560001',
    });
  }

  clearDeliveryAddress(): void {
    this._deliveryAddress.set({
      fullName: '',
      mobile: '',
      addressLine: '',
      city: '',
      state: '',
      pincode: '',
    });
  }
}
