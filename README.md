# 📱 MUSTASE - Application Mobile de Gestion d'Ordonnances

## 📋 Description

**MUSTASE** est une application mobile Android moderne développée en **Kotlin** qui permet de gérer intelligemment vos ordonnances médicales. L'application utilise la technologie OCR (Reconnaissance Optique de Caractères) pour scanner et extraire automatiquement les informations de vos ordonnances, puis configure des rappels de prise de médicaments personnalisés.

## ✨ Fonctionnalités Principales

### 🔍 Scan d'Ordonnances
- **Capture par caméra** : Prenez une photo de votre ordonnance directement depuis l'application
- **Import depuis la galerie** : Sélectionnez une image existante de votre galerie
- **OCR intelligent** : Extraction automatique du texte de l'ordonnance via l'API OCR.space
- **Stockage local** : Toutes vos ordonnances sont sauvegardées localement avec leur image

### 💊 Analyse Intelligente des Prescriptions
- **Parser de prescriptions avancé** : Détection automatique des médicaments, dosages et fréquences
- **Reconnaissance de patterns multiples** :
  - Détection de plus de 50 médicaments courants français
  - Extraction des posologies (X fois par jour)
  - Reconnaissance des moments de prise (matin, midi, soir)
  - Extraction de la durée du traitement (en jours)
- **Support de formats variés** :
  - "Doliprane 1000mg 3x/jour"
  - "Prendre 2 comprimés de X, 3 fois par jour"
  - "Médicament: 1 cp matin, midi et soir"
  - Et bien d'autres formats

### ⏰ Système de Rappels
- **Configuration personnalisée** : Définissez les heures de prise pour chaque médicament
- **Rappels automatiques** : Notifications programmées selon votre traitement
- **Gestion de la durée** : Rappels configurés pour toute la durée du traitement
- **Horaires par défaut intelligents** :
  - 1 fois/jour : 08:00
  - 2 fois/jour : 08:00, 20:00
  - 3 fois/jour : 08:00, 12:00, 20:00
  - 4 fois/jour : 08:00, 12:00, 16:00, 20:00
- **Gestion avancée** :
  - Activation/désactivation des rappels
  - Modification des horaires
  - Annulation individuelle ou globale

### 📜 Historique et Détails
- **Liste complète** : Visualisez toutes vos ordonnances scannées
- **Vue détaillée** : Consultez l'image et le texte extrait de chaque ordonnance
- **Gestion des ordonnances** : Suppression possible des ordonnances obsolètes
- **Horodatage** : Chaque ordonnance est datée automatiquement

## 🏗️ Architecture Technique

### Architecture Globale
L'application suit une **architecture moderne en couches** basée sur les principes de **Clean Architecture** et **MVVM (Model-View-ViewModel)** :

```
┌─────────────────────────────────────────┐
│         Presentation Layer              │
│  (Jetpack Compose UI + ViewModels)     │
├─────────────────────────────────────────┤
│          Domain Layer                   │
│    (Business Logic + Use Cases)        │
├─────────────────────────────────────────┤
│           Data Layer                    │
│  (Repositories + Local DB + Remote API) │
└─────────────────────────────────────────┘
```

### Composants Principaux

#### 🎨 Interface Utilisateur (UI)
- **Jetpack Compose** : UI moderne et déclarative
- **Material Design 3** : Design system Google
- **Navigation Compose** : Navigation fluide entre écrans
- **4 écrans principaux** :
  - `HistoryScreen` : Écran d'accueil avec liste des ordonnances
  - `ScanScreen` : Capture et traitement d'ordonnances
  - `DetailScreen` : Visualisation détaillée d'une ordonnance
  - `ReminderScreen` : Configuration des rappels

#### 🧠 ViewModels
- `HistoryViewModel` : Gestion de la liste des ordonnances
- `ScanViewModel` : Gestion du scan et de l'OCR
- `DetailViewModel` : Affichage des détails d'une ordonnance
- `ReminderViewModel` : Configuration des rappels de médicaments

#### 💾 Couche de Données

**Base de données locale (Room)** :
- `PrescriptionEntity` : Table des ordonnances
  - ID, titre, URI de l'image, texte extrait, timestamp
- `ReminderEntity` : Table des rappels
  - ID, prescription associée, médicament, dosage, fréquence, horaires, durée, statut

**API Externe** :
- `OcrWebService` : Interface Retrofit pour OCR.space
- Extraction de texte depuis images de prescriptions

**Repositories** :
- `PrescriptionRepository` : Gestion des ordonnances et appels OCR
- `ReminderRepository` : Gestion des rappels de médicaments

**Parser** :
- `PrescriptionParser` : Analyse intelligente du texte OCR
  - 5 patterns de reconnaissance de médicaments
  - 3 patterns de reconnaissance de durée
  - Fallback sur liste de 50+ médicaments courants

#### 🔔 Système de Notifications
- `NotificationHelper` : Création du canal de notifications
- `ReminderScheduler` : Programmation des rappels avec WorkManager
- `ReminderWorker` : Exécution des notifications au bon moment

#### 💉 Injection de Dépendances
- **Koin** : Framework DI simple et puissant
- `PrescriptionModule` : Module centralisant toutes les dépendances
  - Base de données Room
  - DAOs
  - Client HTTP (OkHttp + Retrofit)
  - Repositories
  - ViewModels

## 🛠️ Technologies Utilisées

### Langage
- **Kotlin** : 100% Kotlin avec coroutines pour l'asynchrone

### UI/UX
- **Jetpack Compose** : Framework UI moderne
- **Material Design 3** : Design system
- **Coil** : Chargement d'images optimisé

### Architecture & Navigation
- **MVVM** : Pattern d'architecture
- **Navigation Compose** : Navigation entre écrans
- **ViewModel** : Gestion d'état
- **StateFlow** : Flux de données réactif

### Persistance
- **Room** : Base de données SQLite
- **Kotlin Serialization** : Sérialisation JSON

### Réseau
- **Retrofit** : Client HTTP
- **OkHttp** : Gestion des requêtes réseau
- **OCR.space API** : Service OCR externe

### Injection de Dépendances
- **Koin** : Framework DI léger pour Android

### Tâches en Arrière-plan
- **WorkManager** : Gestion des rappels et notifications

### Build System
- **Gradle Kotlin DSL** : Configuration de build moderne
- **Kotlin Symbol Processing (KSP)** : Pour Room compiler

## 📁 Structure du Projet

```
app/src/main/java/com/example/mustase/
├── prescription/
│   ├── PrescriptionApplication.kt          # Application principale
│   ├── PrescriptionActivity.kt             # Activity hôte
│   │
│   ├── data/
│   │   ├── model/
│   │   │   ├── PrescriptionEntity.kt       # Modèle d'ordonnance
│   │   │   ├── ReminderEntity.kt           # Modèle de rappel
│   │   │   ├── ExtractedPrescription.kt    # Prescription extraite
│   │   │   ├── OcrResponse.kt              # Réponse API OCR
│   │   │   └── Resource.kt                 # Wrapper de résultat
│   │   │
│   │   ├── local/
│   │   │   ├── PrescriptionDatabase.kt     # Base de données Room
│   │   │   ├── PrescriptionDao.kt          # DAO ordonnances
│   │   │   └── ReminderDao.kt              # DAO rappels
│   │   │
│   │   ├── remote/
│   │   │   └── OcrWebService.kt            # API OCR.space
│   │   │
│   │   ├── repository/
│   │   │   ├── PrescriptionRepository.kt   # Repository ordonnances
│   │   │   └── ReminderRepository.kt       # Repository rappels
│   │   │
│   │   └── parser/
│   │       └── PrescriptionParser.kt       # Parser intelligent
│   │
│   ├── ui/
│   │   ├── screen/
│   │   │   ├── HistoryScreen.kt            # Écran historique
│   │   │   ├── ScanScreen.kt               # Écran scan
│   │   │   ├── DetailScreen.kt             # Écran détails
│   │   │   └── ReminderScreen.kt           # Écran rappels
│   │   │
│   │   ├── viewmodel/
│   │   │   ├── HistoryViewModel.kt
│   │   │   ├── ScanViewModel.kt
│   │   │   ├── DetailViewModel.kt
│   │   │   └── ReminderViewModel.kt
│   │   │
│   │   ├── navigation/
│   │   │   └── PrescriptionNavHost.kt      # Navigation
│   │   │
│   │   └── theme/
│   │       └── Theme.kt                    # Thème Material
│   │
│   ├── notification/
│   │   ├── NotificationHelper.kt           # Gestion notifications
│   │   ├── ReminderScheduler.kt            # Programmation rappels
│   │   └── ReminderWorker.kt               # Worker notifications
│   │
│   └── di/
│       └── PrescriptionModule.kt           # Module Koin
```

## 🚀 Installation et Configuration

### Prérequis
- Android Studio Hedgehog (2023.1.1) ou supérieur
- JDK 11 ou supérieur
- SDK Android 24 (Android 7.0) minimum
- SDK Android 36 (Android 14) pour la compilation

### Clonage et Configuration

```bash
# Cloner le repository
git clone https://github.com/zegueri/MUSTASE.git
cd MUSTASE

# Ouvrir avec Android Studio
# File > Open > Sélectionner le dossier MUSTASE

# Synchroniser Gradle
# Android Studio le fera automatiquement
```

### Configuration de l'API OCR

L'application utilise **OCR.space** pour l'extraction de texte. L'API key est intégrée dans le code (pour un usage de démonstration). Pour un usage en production, il est recommandé de :

1. Obtenir votre propre clé API sur [OCR.space](https://ocr.space/ocrapi)
2. La stocker de manière sécurisée (pas dans le code source)

### Build et Exécution

```bash
# Build debug
./gradlew assembleDebug

# Installation sur appareil/émulateur
./gradlew installDebug

# Ou via Android Studio: Run > Run 'app'
```

## 📱 Permissions Requises

L'application demande les permissions suivantes :

- **INTERNET** : Pour l'API OCR
- **CAMERA** : Pour prendre des photos d'ordonnances
- **POST_NOTIFICATIONS** : Pour envoyer des rappels (Android 13+)
- **SCHEDULE_EXACT_ALARM** : Pour programmer des rappels précis
- **USE_EXACT_ALARM** : Pour les alarmes exactes
- **RECEIVE_BOOT_COMPLETED** : Pour reprogrammer les rappels après redémarrage

## 🎯 Configuration Technique

### Versions SDK
- `minSdk`: 24 (Android 7.0 Nougat)
- `targetSdk`: 36 (Android 14)
- `compileSdk`: 36

### Compatibilité
- Compatible Android 7.0 et supérieur
- Testé sur Android 10, 11, 12, 13 et 14

### Build Configuration
- **Java Version**: 11
- **Kotlin JVM Target**: 11
- **View Binding**: Activé
- **Jetpack Compose**: Activé
- **ProGuard**: Configuré (non actif en debug)

## 🧪 Tests

L'application inclut une infrastructure de tests :
- `test/` : Tests unitaires (JUnit)
- `androidTest/` : Tests d'instrumentation (Espresso)

```bash
# Exécuter les tests unitaires
./gradlew test

# Exécuter les tests d'instrumentation
./gradlew connectedAndroidTest
```

## 📦 Dépendances Principales

| Bibliothèque | Version | Usage |
|--------------|---------|-------|
| Kotlin | 1.9.x | Langage principal |
| Jetpack Compose | 2024.11.00 | UI moderne |
| Material3 | Latest | Design system |
| Room | Latest | Base de données |
| Retrofit | Latest | Client HTTP |
| Koin | Latest | Injection de dépendances |
| Coil | Latest | Chargement d'images |
| WorkManager | Latest | Tâches en arrière-plan |
| Coroutines | Latest | Programmation asynchrone |
| Navigation Compose | Latest | Navigation |

## 🔐 Sécurité

- Stockage local sécurisé avec Room
- Gestion des permissions runtime
- Pas de stockage de données sensibles en clair
- FileProvider pour le partage sécurisé de fichiers

## 🎨 Design

- **Material Design 3** : Design moderne et accessible
- **Thème adaptatif** : Support du mode sombre/clair
- **Icônes Material** : Icônes cohérentes et familières
- **Animations fluides** : Transitions naturelles avec Compose
- **Responsive** : S'adapte aux différentes tailles d'écran

## 🚦 Flux Utilisateur

1. **Lancement** → Écran d'historique (liste des ordonnances)
2. **Scanner** → Bouton "+" → Choix caméra/galerie
3. **Capture** → Image envoyée à l'OCR → Texte extrait
4. **Parser** → Analyse automatique des médicaments
5. **Sauvegarde** → Ordonnance enregistrée dans la base
6. **Détail** → Consultation de l'ordonnance
7. **Rappels** → Configuration des heures de prise
8. **Notifications** → Rappels automatiques aux horaires définis

## 🌟 Points Forts

- ✅ **Interface moderne** avec Jetpack Compose
- ✅ **Architecture propre** et maintenable (MVVM + Clean Architecture)
- ✅ **Injection de dépendances** avec Koin
- ✅ **Base de données locale** performante avec Room
- ✅ **Parser intelligent** reconnaissant de nombreux formats
- ✅ **Système de rappels robuste** avec WorkManager
- ✅ **Gestion d'état réactive** avec StateFlow
- ✅ **Code 100% Kotlin** moderne
- ✅ **Permissions gérées correctement**
- ✅ **Support de l'OCR externe** pour reconnaissance de texte

## 📝 Améliorations Futures Possibles

- [ ] Support multilingue (anglais, espagnol, etc.)
- [ ] Mode hors-ligne complet avec OCR local (ML Kit)
- [ ] Synchronisation cloud (Firebase)
- [ ] Statistiques de prise de médicaments
- [ ] Export PDF des ordonnances
- [ ] Reconnaissance de codes-barres médicaments
- [ ] Intégration avec calendrier système
- [ ] Widget d'accueil pour rappels rapides
- [ ] Tests automatisés complets
- [ ] CI/CD avec GitHub Actions

## 👨‍💻 Développeur

Développé avec ❤️ en Kotlin

## 📄 Licence

Ce projet est un projet personnel de démonstration.

---

**MUSTASE** - Votre assistant personnel pour la gestion de vos traitements médicaux 💊
