
import { Component, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-settings-page',
  imports: [RouterLink],
  templateUrl: './settings.html',
  styleUrl: './settings.css',
})
export class SettingsPage {
  readonly orderUpdates = signal(true);
  readonly promotionalEmails = signal(false);

  onOrderUpdatesChange(event: Event): void {
    const checkbox = event.target as HTMLInputElement;
    this.orderUpdates.set(checkbox.checked);
  }

  onPromotionalEmailsChange(event: Event): void {
    const checkbox = event.target as HTMLInputElement;
    this.promotionalEmails.set(checkbox.checked);
  }
}
