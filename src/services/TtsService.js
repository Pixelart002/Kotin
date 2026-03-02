import { TextToSpeech } from '@capacitor/text-to-speech';

export class TtsService {
 static instance = null;
 
 static getInstance() {
  if (!TtsService.instance) {
   TtsService.instance = new TtsService();
  }
  return TtsService.instance;
 }
 
 async speak(text, lang = 'en-IN') {
  try {
   await TextToSpeech.speak({
    text: text,
    lang: lang,
    rate: 1.0,
    pitch: 1.0,
    volume: 1.0,
    category: 'ambient'
   });
  } catch (error) {
   console.error('TTS error:', error);
  }
 }
 
 async stop() {
  await TextToSpeech.stop();
 }
 
 async getSupportedLanguages() {
  // This would need a native implementation
  return ['en-IN', 'hi-IN', 'ta-IN', 'te-IN', 'kn-IN', 'ml-IN', 'bn-IN', 'gu-IN'];
 }
}