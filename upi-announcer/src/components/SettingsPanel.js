import React from 'react';
import { IonList, IonItem, IonLabel, IonToggle, IonSelect, IonSelectOption, IonNote } from '@ionic/react';

export const SettingsPanel = ({
 language,
 setLanguage,
 announcementEnabled,
 setAnnouncementEnabled
}) => {
 const languages = [
  { value: 'en-IN', label: 'English (India)' },
  { value: 'hi-IN', label: 'Hindi' },
  { value: 'ta-IN', label: 'Tamil' },
  { value: 'te-IN', label: 'Telugu' },
  { value: 'kn-IN', label: 'Kannada' },
  { value: 'ml-IN', label: 'Malayalam' },
  { value: 'bn-IN', label: 'Bengali' },
  { value: 'gu-IN', label: 'Gujarati' }
 ];
 
 return (
  <IonList>
      <IonItem>
        <IonLabel>Voice Announcements</IonLabel>
        <IonToggle 
          checked={announcementEnabled}
          onIonChange={e => setAnnouncementEnabled(e.detail.checked)}
        />
      </IonItem>

      {announcementEnabled && (
        <IonItem>
          <IonLabel>Announcement Language</IonLabel>
          <IonSelect 
            value={language} 
            onIonChange={e => setLanguage(e.detail.value)}
            interface="popover"
          >
            {languages.map(lang => (
              <IonSelectOption key={lang.value} value={lang.value}>
                {lang.label}
              </IonSelectOption>
            ))}
          </IonSelect>
        </IonItem>
      )}

      <IonItem>
        <IonLabel>
          <h3>About</h3>
          <p>UPI Announcer v1.0.0</p>
          <p className="ion-padding-top">
            This app listens for UPI transaction notifications 
            and announces them aloud. Make sure to grant 
            notification access permission.
          </p>
        </IonLabel>
      </IonItem>

      <IonItem lines="none">
        <IonNote color="medium" className="ion-text-wrap">
          Supported UPI Apps: Google Pay, PhonePe, Paytm, 
          BHIM, and other UPI apps with notifications
        </IonNote>
      </IonItem>
    </IonList>
 );
};