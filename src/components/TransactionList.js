import React from 'react';
import { IonList, IonItem, IonLabel, IonNote, IonIcon } from '@ionic/react';
import { arrowUpCircle, arrowDownCircle } from 'ionicons/icons';
import './TransactionList.css';

export const TransactionList = ({ transactions }) => {
 const formatTime = (date) => {
  return date.toLocaleTimeString('en-IN', {
   hour: '2-digit',
   minute: '2-digit'
  });
 };
 
 const formatDate = (date) => {
  const today = new Date();
  const yesterday = new Date(today);
  yesterday.setDate(yesterday.getDate() - 1);
  
  if (date.toDateString() === today.toDateString()) {
   return 'Today';
  } else if (date.toDateString() === yesterday.toDateString()) {
   return 'Yesterday';
  } else {
   return date.toLocaleDateString('en-IN');
  }
 };
 
 return (
  <IonList>
      {transactions.length === 0 ? (
        <IonItem>
          <IonLabel className="ion-text-center">
            <p>No transactions yet</p>
          </IonLabel>
        </IonItem>
      ) : (
        transactions.map((transaction, index) => (
          <IonItem key={index} className="transaction-item">
            <IonIcon
              icon={transaction.type === 'credit' ? arrowDownCircle : arrowUpCircle}
              color={transaction.type === 'credit' ? 'success' : 'danger'}
              slot="start"
              size="large"
            />
            <IonLabel>
              <h2>
                {transaction.type === 'credit' 
                  ? `Received from ${transaction.sender || 'Unknown'}`
                  : `Paid to ${transaction.receiver || 'Unknown'}`}
              </h2>
              <p>{transaction.remark || 'No remark'}</p>
              <IonNote>{formatDate(transaction.timestamp)} at {formatTime(transaction.timestamp)}</IonNote>
            </IonLabel>
            <IonNote slot="end" color={transaction.type === 'credit' ? 'success' : 'danger'}>
              {transaction.type === 'credit' ? '+' : '-'}₹{transaction.amount.toFixed(2)}
            </IonNote>
          </IonItem>
        ))
      )}
    </IonList>
 );
};