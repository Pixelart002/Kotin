import React, { useState, useEffect } from 'react';
import { IonApp, IonContent, IonHeader, IonToolbar, IonTitle, IonGrid, IonRow, IonCol, IonCard, IonCardContent, IonButton, IonIcon, IonToggle, IonLabel, IonItem, IonSelect, IonSelectOption } from '@ionic/react';
import { notificationsOutline, volumeHighOutline, settingsOutline } from 'ionicons/icons';
import { TtsService } from './services/TtsService';
import { UpiListener } from './services/UpiListener';
import { TransactionList } from './components/TransactionList';
import { SettingsPanel } from './components/SettingsPanel';

import '@ionic/react/css/core.css';
import './App.css';

const App = () => {
 const [transactions, setTransactions] = useState([]);
 const [isListening, setIsListening] = useState(false);
 const [announcementEnabled, setAnnouncementEnabled] = useState(true);
 const [language, setLanguage] = useState('en-IN');
 const [showSettings, setShowSettings] = useState(false);
 const [balance, setBalance] = useState(0);
 
 useEffect(() => {
  // Initialize services
  const upiListener = new UpiListener();
  const ttsService = TtsService.getInstance();
  
  // Set up notification listener
  upiListener.onTransaction((transaction) => {
   setTransactions(prev => [transaction, ...prev]);
   
   // Update balance
   if (transaction.type === 'credit') {
    setBalance(prev => prev + transaction.amount);
   } else if (transaction.type === 'debit') {
    setBalance(prev => prev - transaction.amount);
   }
   
   // Announce transaction
   if (announcementEnabled) {
    announceTransaction(transaction);
   }
  });
  
  return () => {
   upiListener.stopListening();
  };
 }, [announcementEnabled]);
 
 const announceTransaction = async (transaction) => {
  const ttsService = TtsService.getInstance();
  let message = '';
  
  if (transaction.type === 'credit') {
   message = `₹${transaction.amount} received from ${transaction.sender || 'someone'}`;
  } else {
   message = `₹${transaction.amount} paid to ${transaction.receiver || 'someone'}`;
  }
  
  if (transaction.remark) {
   message += `. ${transaction.remark}`;
  }
  
  await ttsService.speak(message, language);
 };
 
 const startListening = async () => {
  const upiListener = new UpiListener();
  const hasPermission = await upiListener.requestNotificationPermission();
  
  if (hasPermission) {
   await upiListener.startListening();
   setIsListening(true);
  }
 };
 
 const stopListening = () => {
  const upiListener = new UpiListener();
  upiListener.stopListening();
  setIsListening(false);
 };
 
 const clearTransactions = () => {
  setTransactions([]);
  setBalance(0);
 };
 
 return (
  <IonApp>
      <IonHeader>
        <IonToolbar color="primary">
          <IonTitle>UPI Announcer</IonTitle>
          <IonButton slot="end" fill="clear" onClick={() => setShowSettings(!showSettings)}>
            <IonIcon icon={settingsOutline} />
          </IonButton>
        </IonToolbar>
      </IonHeader>

      <IonContent>
        {showSettings ? (
          <SettingsPanel
            language={language}
            setLanguage={setLanguage}
            announcementEnabled={announcementEnabled}
            setAnnouncementEnabled={setAnnouncementEnabled}
          />
        ) : (
          <IonGrid>
            {/* Balance Card */}
            <IonRow>
              <IonCol size="12">
                <IonCard className="balance-card">
                  <IonCardContent>
                    <h2>Current Balance</h2>
                    <h1>₹{balance.toFixed(2)}</h1>
                  </IonCardContent>
                </IonCard>
              </IonCol>
            </IonRow>

            {/* Control Buttons */}
            <IonRow>
              <IonCol size="6">
                <IonButton 
                  expand="block" 
                  onClick={isListening ? stopListening : startListening}
                  color={isListening ? 'danger' : 'success'}
                >
                  <IonIcon icon={notificationsOutline} slot="start" />
                  {isListening ? 'Stop' : 'Start'} Listening
                </IonButton>
              </IonCol>
              <IonCol size="6">
                <IonButton expand="block" onClick={clearTransactions} color="medium">
                  Clear All
                </IonButton>
              </IonCol>
            </IonRow>

            {/* Status */}
            <IonRow>
              <IonCol size="12">
                <IonItem>
                  <IonLabel>
                    <h3>Status: {isListening ? '🟢 Active' : '⚫ Inactive'}</h3>
                    <p>Listening for UPI notifications...</p>
                  </IonLabel>
                </IonItem>
              </IonCol>
            </IonRow>

            {/* Transaction List */}
            <IonRow>
              <IonCol size="12">
                <h3>Recent Transactions</h3>
                <TransactionList transactions={transactions} />
              </IonCol>
            </IonRow>
          </IonGrid>
        )}
      </IonContent>
    </IonApp>
 );
};

export default App;