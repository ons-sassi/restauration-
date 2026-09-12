import { Component, EventEmitter, Input, Output } from '@angular/core';

export interface NotificationItem {
  id: number;
  title: string;
  message: string;
  date: string;
  read: boolean;
}

@Component({
  selector: 'app-notification',
  standalone: true,
  templateUrl: './notification.component.html',
  styleUrl: './notification.component.css',
})
export class NotificationComponent {
  @Input() notifications: NotificationItem[] = [];

  @Output() notificationClicked = new EventEmitter<NotificationItem>();

  protected get unreadCount(): number {
    return this.notifications.filter((notification) => !notification.read).length;
  }

  protected select(notification: NotificationItem): void {
    this.notificationClicked.emit(notification);
  }
}
