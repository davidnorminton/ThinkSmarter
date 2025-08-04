# ThinkSmarter - Critical Thinking Enhancement App

A modern Android application designed to enhance critical thinking skills through AI-powered question generation and comprehensive feedback analysis.

## 🚀 Features

### Core Functionality
- **AI-Powered Question Generation**: Generate thought-provoking questions using Anthropic's Claude API
- **Comprehensive Answer Evaluation**: Get detailed feedback across 4 dimensions (Clarity, Logic, Perspective, Depth)
- **Enhanced Feedback System**: 
  - Word & phrase suggestions for better language
  - Specific improvement recommendations
  - Thought process guidance for future problems
  - Model answers for reference
- **Adaptive Difficulty**: 10-level difficulty system (1-10) with appropriate answer length expectations
- **Category-Based Learning**: Multiple categories including Philosophical, Leadership, Psychological, Scientific, Technological, Society, and General

### Advanced Features
- **Daily Challenges**: Progressive learning with streak tracking
- **Statistics & Analytics**: Detailed performance insights and progress tracking
- **Recent Questions**: Dedicated page to review past questions and answers
- **Modern Dark Mode UI**: Beautiful Material 3 design with custom color scheme
- **Local Data Storage**: Secure on-device storage using Room database

## 🏗️ Architecture

### MVVM Pattern
```
UI (Compose Screens) 
    ↓
ViewModels (State & Logic)
    ↓
Repositories (Data Access)
    ↓
Room / Retrofit (Local & Remote)
```

### Key Technologies
- **Jetpack Compose**: Modern declarative UI
- **Room Database**: Local data persistence
- **DataStore**: Settings and preferences
- **Retrofit**: API communication with Anthropic
- **Moshi**: JSON serialization
- **Coroutines & StateFlow**: Asynchronous operations
- **Navigation-Compose**: Screen navigation

## 📱 Screenshots

### Main Features
- **Question Generation**: Clean interface for generating new questions
- **Answer Input**: Focused text area with automatic keyboard display
- **Feedback Display**: Comprehensive evaluation with multiple improvement sections
- **Settings**: API key management and preferences
- **Recent Questions**: History of all questions and answers
- **Statistics**: Performance analytics and insights

## 🛠️ Setup & Installation

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK 34+
- Java 17
- Anthropic API key

### Installation
1. **Clone the repository**:
   ```bash
   git clone https://github.com/davidnorminton/ThinkSmarter.git
   cd ThinkSmarter
   ```

2. **Open in Android Studio**:
   - Open Android Studio
   - Select "Open an existing project"
   - Navigate to the cloned directory

3. **Configure API Key**:
   - Get your Anthropic API key from [Anthropic Console](https://console.anthropic.com/)
   - Open the app and go to Settings
   - Enter your API key (format: `sk-ant-...`)

4. **Build and Run**:
   ```bash
   ./gradlew installDebug
   ```

### API Key Setup
1. Visit [Anthropic Console](https://console.anthropic.com/)
2. Create an account and generate an API key
3. Copy the key (starts with `sk-ant-`)
4. Open ThinkSmarter app → Settings → Enter API Key

## 🎯 Usage

### Getting Started
1. **Generate Your First Question**:
   - Tap "Ask Question" button
   - Select difficulty level (1-10)
   - Choose a category
   - Wait for AI to generate your question

2. **Answer the Question**:
   - Type your thoughtful response
   - Consider the expected answer length
   - Tap "Submit Answer" when ready

3. **Review Feedback**:
   - View your scores across 4 dimensions
   - Read detailed feedback and suggestions
   - Review word/phrase improvements
   - Study the model answer
   - Use thought process guidance for future questions

### Advanced Features
- **Daily Challenges**: Complete daily questions to build streaks
- **Statistics**: Track your progress and performance over time
- **Recent Questions**: Review past questions and answers
- **Settings**: Customize difficulty, categories, and preferences

## 🔧 Development

### Project Structure
```
app/src/main/java/com/example/thinksmarter/
├── data/
│   ├── db/           # Room database and DAOs
│   ├── model/        # Data entities
│   ├── network/      # Retrofit API interfaces
│   └── repository/   # Repository implementations
├── domain/
│   └── repository/   # Repository interfaces
├── ui/
│   ├── components/   # Reusable UI components
│   ├── screens/      # Screen composables
│   ├── theme/        # Material 3 theming
│   └── viewmodel/    # ViewModels
└── util/             # Utility classes and templates
```

### Key Components
- **MainViewModel**: Manages question generation and answer evaluation
- **SettingsViewModel**: Handles API key and preferences
- **StatisticsViewModel**: Calculates performance metrics
- **RecentQuestionsViewModel**: Manages question history
- **DailyChallengeViewModel**: Handles daily challenges and streaks

### Database Schema
- **Question**: Stores generated questions with metadata
- **Answer**: Stores user answers with comprehensive feedback
- **Category**: Manages question categories
- **DailyChallenge**: Tracks daily challenges and completion
- **UserStreak**: Manages user learning streaks

## 🧪 Testing

### Unit Tests
- Repository layer testing with Room in-memory database
- ViewModel testing with MockK
- API response parsing validation

### Test Coverage
- Data layer: Repository and DAO operations
- Domain layer: Business logic validation
- UI layer: ViewModel state management

## 🎨 UI/UX Design

### Material 3 Implementation
- **Dark Mode**: Default dark theme with custom color palette
- **Modern Components**: Custom Material 3 components
- **Responsive Design**: Adapts to different screen sizes
- **Accessibility**: Proper content descriptions and navigation

### Color Scheme
- **Primary**: Custom blue gradient
- **Secondary**: Green accent for positive feedback
- **Tertiary**: Orange for warnings and highlights
- **Surface**: Dark backgrounds with proper contrast

## 🔒 Privacy & Security

### Data Storage
- **Local Only**: All data stored on device using Room
- **No Cloud Sync**: Complete privacy - your data stays on your device
- **Secure API**: API keys stored securely using DataStore

### API Usage
- **Anthropic Claude**: Uses `claude-3-7-sonnet-latest` model
- **Secure Communication**: HTTPS-only API calls
- **Minimal Data**: Only sends question text and user answers

## 🤝 Contributing

### Development Setup
1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Commit changes: `git commit -m 'Add amazing feature'`
4. Push to branch: `git push origin feature/amazing-feature`
5. Open a Pull Request

### Code Style
- Follow Kotlin coding conventions
- Use meaningful variable and function names
- Add comments for complex logic
- Maintain consistent formatting

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- **Anthropic**: For providing the Claude API
- **Google**: For Android Jetpack libraries
- **Material Design**: For the design system
- **Open Source Community**: For various libraries and tools

## 📞 Support

If you encounter any issues or have questions:
1. Check the [Issues](https://github.com/davidnorminton/ThinkSmarter/issues) page
2. Create a new issue with detailed information
3. Include device information and steps to reproduce

---

**ThinkSmarter** - Enhancing critical thinking, one question at a time. 🧠✨ 