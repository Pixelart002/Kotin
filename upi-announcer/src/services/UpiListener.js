import { Plugins } from '@capacitor/core';
import { Notification } from '@capacitor/notification';

const { Modals } = Plugins;

export class UpiListener {
 constructor() {
  this.isListening = false;
  this.transactionCallbacks = [];
 }
 
 async requestNotificationPermission() {
  try {
   const permission = await Notification.requestPermissions();
   return permission.receive === 'granted';
  } catch (error) {
   console.error('Permission error:', error);
   return false;
  }
 }
 
 async startListening() {
  this.isListening = true;
  
  // Add notification listener
  await Notification.addListener('notificationReceived', (notification) => {
   this.handleNotification(notification);
  });
  
  // Also listen for local notifications
  await Notification.addListener('localNotificationReceived', (notification) => {
   this.handleNotification(notification);
  });
  
  console.log('Started listening for UPI notifications');
 }
 
 stopListening() {
  this.isListening = false;
  Notification.removeAllListeners();
  console.log('Stopped listening for UPI notifications');
 }
 
 handleNotification(notification) {
  if (!this.isListening) return;
  
  const title = notification.title?.toLowerCase() || '';
  const body = notification.body || '';
  
  // Check if it's a UPI transaction notification
  if (title.includes('upi') || body.includes('upi') ||
   title.includes('paid') || body.includes('received')) {
   
   const transaction = this.parseTransaction(body);
   if (transaction) {
    this.notifyTransaction(transaction);
   }
  }
 }
 
 parseTransaction(notificationBody) {
  // Parse different UPI app formats
  const transaction = {
   amount: 0,
   type: 'unknown',
   sender: '',
   receiver: '',
   remark: '',
   timestamp: new Date()
  };
  
  // Extract amount (₹ symbol or Rs)
  const amountMatch = notificationBody.match(/[₹Rs.\s]*(\d+(?:\.\d{1,2})?)/);
  if (amountMatch) {
   transaction.amount = parseFloat(amountMatch[1]);
  }
  
  // Determine transaction type
  if (notificationBody.toLowerCase().includes('received') ||
   notificationBody.toLowerCase().includes('credited') ||
   notificationBody.toLowerCase().includes('got')) {
   transaction.type = 'credit';
  } else if (notificationBody.toLowerCase().includes('paid') ||
   notificationBody.toLowerCase().includes('debited') ||
   notificationBody.toLowerCase().includes('sent')) {
   transaction.type = 'debit';
  }
  
  // Extract sender/receiver
  const upiMatch = notificationBody.match(/([a-zA-Z0-9._-]+@[a-zA-Z0-9]+)/);
  if (upiMatch) {
   if (transaction.type === 'credit') {
    transaction.sender = upiMatch[1];
   } else {
    transaction.receiver = upiMatch[1];
   }
  }
  
  // Extract remark (text after "for" or "remark")
  const remarkMatch = notificationBody.match(/for\s+([^.]+)/i) ||
   notificationBody.match(/remark[:\s]+([^.]+)/i);
  if (remarkMatch) {
   transaction.remark = remarkMatch[1].trim();
  }
  
  return transaction;
 }
 
 onTransaction(callback) {
  this.transactionCallbacks.push(callback);
 }
 
 notifyTransaction(transaction) {
  this.transactionCallbacks.forEach(callback => callback(transaction));
 }
}