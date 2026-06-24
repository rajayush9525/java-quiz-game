# Java Quiz Challenge 🎯

A complete interactive multiple-choice quiz game built with **Java Swing** — no external
dependencies, just the JDK.

![Java](https://img.shields.io/badge/Java-8%2B-orange) ![License](https://img.shields.io/badge/license-MIT-blue) ![GUI](https://img.shields.io/badge/UI-Swing-informational)

## Features
- 🎨 Clean, modern GUI (Swing, no external dependencies)
- 📚 3 quiz topics: **Java Programming**, **General Knowledge**, **Science** (8 questions each)
- ⏱ 20-second countdown timer per question (auto-locks and reveals the answer on timeout)
- ✅ Instant visual feedback — correct answers turn green, wrong answers turn red
- 📊 Live score tracker and progress bar
- 🏁 Final results screen with percentage, grade message, and a full question-by-question review
- 🔁 "Try Another Topic" button to replay instantly
- 🔀 Questions are shuffled each time you play

## Project Structure
```
QuizGame/
└── src/
    └── QuizGame.java   <- single self-contained source file
```

## How to Build & Run

### Requirements
- JDK 8 or higher (JDK 17/21 recommended)

### Steps
```bash
cd QuizGame/src
javac QuizGame.java
java QuizGame
```

That's it — a window titled **"Java Quiz Challenge"** will open.

## How to Play
1. On the home screen, pick a topic from the dropdown.
2. Click **Start Quiz**.
3. Select an answer for each question before the timer runs out.
4. Click **Next** to move on (the button enables once you've answered or the timer expires).
5. After the last question, view your score, grade, and a full review of right/wrong answers.
6. Click **Try Another Topic** to play again.

## Extending the Quiz
To add more topics or questions, open `QuizGame.java` and edit the `buildTopics()` method.
Each question is created like this:

```java
java.add(new Question(
    "Your question text?",
    new String[]{"Option A", "Option B", "Option C", "Option D"},
    1 // index (0-based) of the correct option
));
```

Add a new `Topic` object to the `topics` list the same way the existing three are added, and it
will automatically appear in the home-screen dropdown.

## Customization Ideas
- Change `TIME_PER_QUESTION` (in seconds) to adjust difficulty.
- Add new topics (e.g., History, Sports, Math).
- Add sound effects on correct/incorrect answers.
- Persist high scores to a file using `java.io`.
- Add difficulty levels by tagging questions and filtering before each quiz.

Enjoy the quiz! 🎉

## License
This project is licensed under the [MIT License](LICENSE) — free to use, modify, and distribute.

## Contributing
Pull requests are welcome! Feel free to add new topics, difficulty levels, sound effects, or
a leaderboard. Open an issue first for major changes.
