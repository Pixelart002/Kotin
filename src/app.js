import { Capacitor } from '@capacitor/core';
import { App } from '@capacitor/app';

class UPIVoiceAlert {
    private isInitialized = false;
    private settings = {
        voiceEnabled: true,
        vibrateEnabled: true,
        volume: 80,
        language: 'en'
    };
    private transactions: any[] = [];

    constructor() {
        this.initialize();
    }

    async initialize() {
        try {
            console.log('Initializing UPI Voice Alert App');
            console.log('Platform:', Capacitor.getPlatform());

            // Load saved settings
            this.loadSettings();

            // Setup event listeners
            this.setupEventListeners();

            // Check permissions
            this.checkPermissions();

            // Load saved transactions
            this.loadTransactions();

            this.isInitialized = true;
            this.updateUI();

            // Listen for app events
            App.addListener('pause', () => {
                console.log('App paused');
            });

            App.addListener('resume', () => {
                console.log('App resumed');
                this.checkPermissions();
            });

        } catch (error) {
            console.error('Failed to initialize app:', error);
        }
    }

    setupEventListeners() {
        // Voice toggle
        const voiceToggle = document.getElementById('voiceToggle') as HTMLInputElement;
        voiceToggle?.addEventListener('change', (e) => {
            this.settings.voiceEnabled = e.target.checked;
            this.saveSettings();
        });

        // Vibrate toggle
        const vibrateToggle = document.getElementById('vibrateToggle') as HTMLInputElement;
        vibrateToggle?.addEventListener('change', (e) => {
            this.settings.vibrateEnabled = e.target.checked;
            this.saveSettings();
        });

        // Volume control
        const volumeControl = document.getElementById('volumeControl') as HTMLInputElement;
        volumeControl?.addEventListener('input', (e) => {
            this.settings.volume = parseInt(e.target.value);
            const volumeValue = document.getElementById('volumeValue');
            if (volumeValue) {
                volumeValue.textContent = `${this.settings.volume}%`;
            }
            this.saveSettings();
        });

        // Language select
        const languageSelect = document.getElementById('languageSelect') as HTMLSelectElement;
        languageSelect?.addEventListener('change', (e) => {
            this.settings.language = e.target.value;
            this.saveSettings();
        });

        // Grant permissions button
        const grantBtn = document.getElementById('grantPermissionsBtn');
        grantBtn?.addEventListener('click', () => {
            this.requestPermissions();
        });
    }

    async requestPermissions() {
        if (Capacitor.getPlatform() !== 'android') {
            alert('This feature is only available on Android');
            return;
        }

        try {
            // In a real app, you would use @capacitor/permissions plugin
            alert('Please grant Notification Listener Access in Settings > Apps > Permissions > Notification listener service\n\nThen grant Audio Recording permission when prompted');
            this.checkPermissions();
        } catch (error) {
            console.error('Error requesting permissions:', error);
            alert('Failed to request permissions. Please check app settings manually.');
        }
    }

    async checkPermissions() {
        // This would normally check actual permission status
        // For now, we'll show UI indicators
        const notificationStatus = document.getElementById('notificationStatus');
        const audioStatus = document.getElementById('audioStatus');

        if (notificationStatus) {
            notificationStatus.textContent = '⚠️ Check Settings';
            notificationStatus.style.color = '#FF9800';
        }

        if (audioStatus) {
            audioStatus.textContent = '⚠️ Check Settings';
            audioStatus.style.color = '#FF9800';
        }
    }

    updateUI() {
        const voiceToggle = document.getElementById('voiceToggle') as HTMLInputElement;
        const vibrateToggle = document.getElementById('vibrateToggle') as HTMLInputElement;
        const volumeControl = document.getElementById('volumeControl') as HTMLInputElement;
        const languageSelect = document.getElementById('languageSelect') as HTMLSelectElement;

        if (voiceToggle) voiceToggle.checked = this.settings.voiceEnabled;
        if (vibrateToggle) vibrateToggle.checked = this.settings.vibrateEnabled;
        if (volumeControl) volumeControl.value = this.settings.volume.toString();
        if (languageSelect) languageSelect.value = this.settings.language;

        const volumeValue = document.getElementById('volumeValue');
        if (volumeValue) {
            volumeValue.textContent = `${this.settings.volume}%`;
        }

        this.updateStatusIndicator();
        this.displayTransactions();
    }

    updateStatusIndicator() {
        const indicator = document.getElementById('statusIndicator');
        const dot = indicator?.querySelector('.dot');
        const statusText = indicator?.querySelector('.status-text');

        if (this.isInitialized && Capacitor.getPlatform() === 'android') {
            dot?.classList.add('online');
            dot?.classList.remove('offline');
            if (statusText) statusText.textContent = 'Online - Listening for UPI notifications';
        } else {
            dot?.classList.add('offline');
            dot?.classList.remove('online');
            if (statusText) statusText.textContent = 'Please grant permissions to start';
        }
    }

    saveSettings() {
        localStorage.setItem('upiVoiceAlertSettings', JSON.stringify(this.settings));
    }

    loadSettings() {
        const saved = localStorage.getItem('upiVoiceAlertSettings');
        if (saved) {
            this.settings = JSON.parse(saved);
        }
    }

    saveTransaction(data: any) {
        const transaction = {
            ...data,
            timestamp: new Date().toLocaleString()
        };
        this.transactions.unshift(transaction);
        
        // Keep only last 20 transactions
        if (this.transactions.length > 20) {
            this.transactions = this.transactions.slice(0, 20);
        }
        
        localStorage.setItem('upiTransactions', JSON.stringify(this.transactions));
        this.displayTransactions();
    }

    loadTransactions() {
        const saved = localStorage.getItem('upiTransactions');
        if (saved) {
            this.transactions = JSON.parse(saved);
        }
    }

    displayTransactions() {
        const list = document.getElementById('transactionsList');
        if (!list) return;

        if (this.transactions.length === 0) {
            list.innerHTML = '<p class="empty-state">No transactions yet</p>';
            return;
        }

        list.innerHTML = this.transactions
            .map(tx => `
                <div class="transaction-item">
                    <strong>${tx.senderName || 'Unknown'}</strong>
                    <span class="amount">${tx.amount || 'N/A'}</span>
                    <div class="timestamp">${tx.timestamp}</div>
                </div>
            `)
            .join('');
    }

    // Public method for native code to call
    static handleNotification(data: any) {
        console.log('Handling notification:', data);
        // Save transaction and update UI
        // This will be called from native Kotlin code
    }
}

// Initialize app when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    window.__upiVoiceAlert = new UPIVoiceAlert();
});

// Expose for native Android bridge
declare global {
    interface Window {
        __upiVoiceAlert: UPIVoiceAlert;
    }
}
