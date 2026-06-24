import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * QuizGame - An interactive multiple-choice quiz game with GUI.
 * Topics: Java Programming, General Knowledge, Science
 * Features: scoring, per-question timer, progress bar, immediate feedback,
 * final results screen with grade and review.
 */
public class QuizGame extends JFrame {

    // ---------- Data model ----------
    static class Question {
        String text;
        String[] options;
        int correctIndex;
        Question(String text, String[] options, int correctIndex) {
            this.text = text;
            this.options = options;
            this.correctIndex = correctIndex;
        }
    }

    static class Topic {
        String name;
        List<Question> questions;
        Topic(String name, List<Question> questions) {
            this.name = name;
            this.questions = questions;
        }
    }

    // ---------- Game state ----------
    private final List<Topic> topics = new ArrayList<>();
    private Topic currentTopic;
    private List<Question> activeQuestions;
    private int currentIndex = 0;
    private int score = 0;
    private int[] userAnswers; // -1 = unanswered/timeout
    private Timer questionTimer;
    private int timeLeft;
    private static final int TIME_PER_QUESTION = 20; // seconds

    // ---------- UI components ----------
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainPanel = new JPanel(cardLayout);

    // Home screen
    private JComboBox<String> topicSelector;

    // Quiz screen
    private JLabel questionNumberLabel;
    private JLabel scoreLabel;
    private JLabel timerLabel;
    private JTextArea questionArea;
    private JRadioButton[] optionButtons;
    private ButtonGroup optionGroup;
    private JButton nextButton;
    private JProgressBar progressBar;

    private static final Color PRIMARY = new Color(63, 81, 181);
    private static final Color PRIMARY_DARK = new Color(48, 63, 159);
    private static final Color ACCENT = new Color(255, 152, 0);
    private static final Color CORRECT_COLOR = new Color(76, 175, 80);
    private static final Color WRONG_COLOR = new Color(229, 57, 53);
    private static final Color BG = new Color(245, 246, 250);

    public QuizGame() {
        super("Java Quiz Challenge");
        buildTopics();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(720, 560);
        setMinimumSize(new Dimension(640, 500));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG);

        mainPanel.add(buildHomePanel(), "HOME");
        mainPanel.add(buildQuizPanel(), "QUIZ");
        mainPanel.add(buildResultPanel(), "RESULT");
        add(mainPanel);

        cardLayout.show(mainPanel, "HOME");
        setVisible(true);
    }

    // ================= TOPIC DATA =================
    private void buildTopics() {
        List<Question> java = new ArrayList<>();
        java.add(new Question("What is the size of an int variable in Java?",
                new String[]{"16 bit", "32 bit", "64 bit", "8 bit"}, 1));
        java.add(new Question("Which keyword is used to inherit a class in Java?",
                new String[]{"implements", "extends", "inherits", "super"}, 1));
        java.add(new Question("Which method is the entry point of a Java application?",
                new String[]{"start()", "run()", "main()", "init()"}, 2));
        java.add(new Question("Which collection class allows duplicate elements and maintains insertion order?",
                new String[]{"HashSet", "TreeSet", "ArrayList", "HashMap"}, 2));
        java.add(new Question("What is the default value of a boolean variable in Java?",
                new String[]{"true", "false", "0", "null"}, 1));
        java.add(new Question("Which keyword is used to prevent method overriding?",
                new String[]{"static", "final", "private", "abstract"}, 1));
        java.add(new Question("Which of these is NOT a primitive data type in Java?",
                new String[]{"int", "String", "char", "boolean"}, 1));
        java.add(new Question("What does JVM stand for?",
                new String[]{"Java Virtual Machine", "Java Variable Method", "Java Visual Machine", "Java Verified Module"}, 0));

        List<Question> gk = new ArrayList<>();
        gk.add(new Question("What is the capital of Australia?",
                new String[]{"Sydney", "Melbourne", "Canberra", "Perth"}, 2));
        gk.add(new Question("Who wrote the play 'Romeo and Juliet'?",
                new String[]{"Charles Dickens", "William Shakespeare", "Mark Twain", "Leo Tolstoy"}, 1));
        gk.add(new Question("Which is the longest river in the world?",
                new String[]{"Amazon", "Nile", "Yangtze", "Mississippi"}, 1));
        gk.add(new Question("How many continents are there on Earth?",
                new String[]{"5", "6", "7", "8"}, 2));
        gk.add(new Question("Which country gifted the Statue of Liberty to the USA?",
                new String[]{"United Kingdom", "Spain", "France", "Italy"}, 2));
        gk.add(new Question("The Great Wall is located in which country?",
                new String[]{"Japan", "China", "India", "Mongolia"}, 1));
        gk.add(new Question("Which planet is known as the Red Planet?",
                new String[]{"Venus", "Jupiter", "Mars", "Saturn"}, 2));
        gk.add(new Question("Which is the smallest country in the world by area?",
                new String[]{"Monaco", "Vatican City", "San Marino", "Malta"}, 1));

        List<Question> sci = new ArrayList<>();
        sci.add(new Question("What is the chemical symbol for water?",
                new String[]{"H2O", "O2", "CO2", "HO2"}, 0));
        sci.add(new Question("What force keeps planets in orbit around the sun?",
                new String[]{"Magnetism", "Gravity", "Friction", "Inertia"}, 1));
        sci.add(new Question("How many bones are there in the adult human body?",
                new String[]{"196", "206", "216", "226"}, 1));
        sci.add(new Question("What gas do plants absorb from the atmosphere for photosynthesis?",
                new String[]{"Oxygen", "Nitrogen", "Carbon Dioxide", "Hydrogen"}, 2));
        sci.add(new Question("What is the powerhouse of the cell?",
                new String[]{"Nucleus", "Ribosome", "Mitochondria", "Golgi Body"}, 2));
        sci.add(new Question("Which element has the atomic number 1?",
                new String[]{"Helium", "Hydrogen", "Oxygen", "Carbon"}, 1));
        sci.add(new Question("What is the speed of light approximately?",
                new String[]{"3,00,000 km/s", "1,50,000 km/s", "5,00,000 km/s", "1,00,000 km/s"}, 0));
        sci.add(new Question("Which organ pumps blood throughout the human body?",
                new String[]{"Liver", "Heart", "Kidney", "Lungs"}, 1));

        topics.add(new Topic("Java Programming", java));
        topics.add(new Topic("General Knowledge", gk));
        topics.add(new Topic("Science", sci));
    }

    // ================= HOME PANEL =================
    private JPanel buildHomePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG);
        panel.setBorder(new EmptyBorder(60, 60, 60, 60));

        JLabel title = new JLabel("🎯 Java Quiz Challenge");
        title.setFont(new Font("SansSerif", Font.BOLD, 32));
        title.setForeground(PRIMARY_DARK);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Test your knowledge. Beat the clock. Have fun!");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 15));
        subtitle.setForeground(Color.GRAY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel chooseLabel = new JLabel("Choose a topic:");
        chooseLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        chooseLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        String[] topicNames = topics.stream().map(t -> t.name).toArray(String[]::new);
        topicSelector = new JComboBox<>(topicNames);
        topicSelector.setMaximumSize(new Dimension(300, 36));
        topicSelector.setFont(new Font("SansSerif", Font.PLAIN, 15));
        topicSelector.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton startButton = new JButton("Start Quiz ▶");
        styleButton(startButton, PRIMARY, Color.WHITE);
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        startButton.setMaximumSize(new Dimension(220, 46));
        startButton.addActionListener(e -> startQuiz((String) topicSelector.getSelectedItem()));

        JLabel info = new JLabel("Each quiz has 8 questions • 20 seconds per question");
        info.setFont(new Font("SansSerif", Font.ITALIC, 12));
        info.setForeground(Color.GRAY);
        info.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(Box.createVerticalGlue());
        panel.add(title);
        panel.add(Box.createVerticalStrut(10));
        panel.add(subtitle);
        panel.add(Box.createVerticalStrut(40));
        panel.add(chooseLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(topicSelector);
        panel.add(Box.createVerticalStrut(30));
        panel.add(startButton);
        panel.add(Box.createVerticalStrut(20));
        panel.add(info);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    // ================= QUIZ PANEL =================
    private JPanel buildQuizPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BG);
        panel.setBorder(new EmptyBorder(20, 30, 20, 30));

        // Top bar: question number, score, timer
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        questionNumberLabel = new JLabel("Question 1/8");
        questionNumberLabel.setFont(new Font("SansSerif", Font.BOLD, 16));

        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        scoreLabel.setForeground(PRIMARY_DARK);

        timerLabel = new JLabel("⏱ 20s", SwingConstants.RIGHT);
        timerLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        timerLabel.setForeground(ACCENT);

        JPanel centerInfo = new JPanel(new GridLayout(1, 1));
        centerInfo.setOpaque(false);
        centerInfo.add(scoreLabel);

        topBar.add(questionNumberLabel, BorderLayout.WEST);
        topBar.add(centerInfo, BorderLayout.CENTER);
        topBar.add(timerLabel, BorderLayout.EAST);

        progressBar = new JProgressBar(0, 8);
        progressBar.setValue(0);
        progressBar.setForeground(PRIMARY);
        progressBar.setStringPainted(false);
        progressBar.setPreferredSize(new Dimension(100, 8));

        JPanel topContainer = new JPanel();
        topContainer.setOpaque(false);
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.add(topBar);
        topContainer.add(Box.createVerticalStrut(8));
        topContainer.add(progressBar);

        // Question text
        questionArea = new JTextArea();
        questionArea.setFont(new Font("SansSerif", Font.BOLD, 19));
        questionArea.setEditable(false);
        questionArea.setOpaque(false);
        questionArea.setLineWrap(true);
        questionArea.setWrapStyleWord(true);
        questionArea.setFocusable(false);
        questionArea.setBorder(new EmptyBorder(20, 0, 20, 0));

        // Options
        JPanel optionsPanel = new JPanel();
        optionsPanel.setOpaque(false);
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));

        optionButtons = new JRadioButton[4];
        optionGroup = new ButtonGroup();
        for (int i = 0; i < 4; i++) {
            JRadioButton rb = new JRadioButton();
            rb.setFont(new Font("SansSerif", Font.PLAIN, 16));
            rb.setOpaque(true);
            rb.setBackground(Color.WHITE);
            rb.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
                    new EmptyBorder(12, 14, 12, 14)));
            rb.setAlignmentX(Component.LEFT_ALIGNMENT);
            rb.setMaximumSize(new Dimension(2000, 50));
            rb.setFocusPainted(false);
            final int idx = i;
            rb.addActionListener(e -> onOptionSelected(idx));
            optionGroup.add(rb);
            optionButtons[i] = rb;
            optionsPanel.add(rb);
            optionsPanel.add(Box.createVerticalStrut(10));
        }

        JScrollPane questionScroll = new JScrollPane(questionArea);
        questionScroll.setBorder(null);
        questionScroll.setOpaque(false);
        questionScroll.getViewport().setOpaque(false);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(questionScroll, BorderLayout.NORTH);
        centerPanel.add(optionsPanel, BorderLayout.CENTER);

        // Bottom: next button
        nextButton = new JButton("Next ▶");
        styleButton(nextButton, PRIMARY, Color.WHITE);
        nextButton.setEnabled(false);
        nextButton.addActionListener(e -> goToNextQuestion());

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setOpaque(false);
        bottomPanel.add(nextButton);

        panel.add(topContainer, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    // ================= RESULT PANEL =================
    private JPanel resultContentPanel;

    private JPanel buildResultPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BG);
        wrapper.setBorder(new EmptyBorder(30, 40, 30, 40));

        resultContentPanel = new JPanel();
        resultContentPanel.setLayout(new BoxLayout(resultContentPanel, BoxLayout.Y_AXIS));
        resultContentPanel.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(resultContentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        wrapper.add(scrollPane, BorderLayout.CENTER);
        return wrapper;
    }

    private void populateResultPanel() {
        resultContentPanel.removeAll();

        int total = activeQuestions.size();
        double percent = (score * 100.0) / total;
        String grade;
        Color gradeColor;
        if (percent >= 80) { grade = "Excellent! 🏆"; gradeColor = CORRECT_COLOR; }
        else if (percent >= 60) { grade = "Good Job! 👍"; gradeColor = PRIMARY; }
        else if (percent >= 40) { grade = "Not Bad — Keep Practicing 📘"; gradeColor = ACCENT; }
        else { grade = "Keep Learning! 💪"; gradeColor = WRONG_COLOR; }

        JLabel title = new JLabel("Quiz Complete — " + currentTopic.name);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(PRIMARY_DARK);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel scoreBig = new JLabel(score + " / " + total + "  (" + String.format("%.0f", percent) + "%)");
        scoreBig.setFont(new Font("SansSerif", Font.BOLD, 40));
        scoreBig.setForeground(gradeColor);
        scoreBig.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel gradeLabel = new JLabel(grade);
        gradeLabel.setFont(new Font("SansSerif", Font.PLAIN, 18));
        gradeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        resultContentPanel.add(Box.createVerticalStrut(10));
        resultContentPanel.add(title);
        resultContentPanel.add(Box.createVerticalStrut(15));
        resultContentPanel.add(scoreBig);
        resultContentPanel.add(Box.createVerticalStrut(5));
        resultContentPanel.add(gradeLabel);
        resultContentPanel.add(Box.createVerticalStrut(25));

        JLabel reviewHeader = new JLabel("Review your answers:");
        reviewHeader.setFont(new Font("SansSerif", Font.BOLD, 16));
        reviewHeader.setAlignmentX(Component.CENTER_ALIGNMENT);
        resultContentPanel.add(reviewHeader);
        resultContentPanel.add(Box.createVerticalStrut(10));

        for (int i = 0; i < total; i++) {
            Question q = activeQuestions.get(i);
            boolean correct = userAnswers[i] == q.correctIndex;
            JPanel card = new JPanel();
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBackground(Color.WHITE);
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(correct ? CORRECT_COLOR : WRONG_COLOR, 1, true),
                    new EmptyBorder(12, 14, 12, 14)));
            card.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.setMaximumSize(new Dimension(2000, 130));

            JLabel qLabel = new JLabel("<html><b>Q" + (i + 1) + ":</b> " + escapeHtml(q.text) + "</html>");
            qLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));

            String yourAns = userAnswers[i] == -1 ? "No answer (timed out)" : q.options[userAnswers[i]];
            JLabel yourLabel = new JLabel((correct ? "✅ " : "❌ ") + "Your answer: " + yourAns);
            yourLabel.setFont(new Font("SansSerif", correct ? Font.PLAIN : Font.PLAIN, 13));
            yourLabel.setForeground(correct ? CORRECT_COLOR.darker() : WRONG_COLOR.darker());

            card.add(qLabel);
            card.add(Box.createVerticalStrut(4));
            card.add(yourLabel);
            if (!correct) {
                JLabel correctLabel = new JLabel("✔ Correct answer: " + q.options[q.correctIndex]);
                correctLabel.setFont(new Font("SansSerif", Font.ITALIC, 13));
                correctLabel.setForeground(CORRECT_COLOR.darker());
                card.add(Box.createVerticalStrut(2));
                card.add(correctLabel);
            }

            resultContentPanel.add(card);
            resultContentPanel.add(Box.createVerticalStrut(10));
        }

        JButton retryButton = new JButton("Try Another Topic 🔁");
        styleButton(retryButton, PRIMARY, Color.WHITE);
        retryButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        retryButton.addActionListener(e -> {
            if (questionTimer != null) questionTimer.stop();
            cardLayout.show(mainPanel, "HOME");
        });

        resultContentPanel.add(Box.createVerticalStrut(15));
        resultContentPanel.add(retryButton);
        resultContentPanel.add(Box.createVerticalStrut(10));

        resultContentPanel.revalidate();
        resultContentPanel.repaint();
    }

    private String escapeHtml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    // ================= GAME LOGIC =================
    private void startQuiz(String topicName) {
        currentTopic = topics.stream().filter(t -> t.name.equals(topicName)).findFirst().orElse(topics.get(0));
        activeQuestions = new ArrayList<>(currentTopic.questions);
        Collections.shuffle(activeQuestions);
        currentIndex = 0;
        score = 0;
        userAnswers = new int[activeQuestions.size()];
        Arrays.fill(userAnswers, -1);
        progressBar.setMaximum(activeQuestions.size());
        progressBar.setValue(0);
        cardLayout.show(mainPanel, "QUIZ");
        loadQuestion();
    }

    private void loadQuestion() {
        Question q = activeQuestions.get(currentIndex);
        questionNumberLabel.setText("Question " + (currentIndex + 1) + "/" + activeQuestions.size());
        scoreLabel.setText("Score: " + score);
        questionArea.setText(q.text);
        optionGroup.clearSelection();

        for (int i = 0; i < optionButtons.length; i++) {
            JRadioButton rb = optionButtons[i];
            rb.setText(q.options[i]);
            rb.setEnabled(true);
            rb.setBackground(Color.WHITE);
            rb.setForeground(Color.BLACK);
        }
        progressBar.setValue(currentIndex);
        nextButton.setEnabled(false);
        nextButton.setText(currentIndex == activeQuestions.size() - 1 ? "Finish 🏁" : "Next ▶");

        startTimer();
    }

    private void startTimer() {
        if (questionTimer != null) questionTimer.stop();
        timeLeft = TIME_PER_QUESTION;
        timerLabel.setText("⏱ " + timeLeft + "s");
        timerLabel.setForeground(ACCENT);
        questionTimer = new Timer(1000, e -> {
            timeLeft--;
            timerLabel.setText("⏱ " + timeLeft + "s");
            if (timeLeft <= 5) timerLabel.setForeground(WRONG_COLOR);
            if (timeLeft <= 0) {
                questionTimer.stop();
                handleTimeout();
            }
        });
        questionTimer.start();
    }

    private void handleTimeout() {
        // Lock options, show correct answer, mark unanswered
        Question q = activeQuestions.get(currentIndex);
        userAnswers[currentIndex] = -1;
        for (JRadioButton rb : optionButtons) rb.setEnabled(false);
        optionButtons[q.correctIndex].setBackground(CORRECT_COLOR);
        optionButtons[q.correctIndex].setForeground(Color.WHITE);
        nextButton.setEnabled(true);
    }

    private void onOptionSelected(int selectedIndex) {
        if (questionTimer != null) questionTimer.stop();
        Question q = activeQuestions.get(currentIndex);
        userAnswers[currentIndex] = selectedIndex;

        for (JRadioButton rb : optionButtons) rb.setEnabled(false);

        if (selectedIndex == q.correctIndex) {
            score++;
            optionButtons[selectedIndex].setBackground(CORRECT_COLOR);
            optionButtons[selectedIndex].setForeground(Color.WHITE);
        } else {
            optionButtons[selectedIndex].setBackground(WRONG_COLOR);
            optionButtons[selectedIndex].setForeground(Color.WHITE);
            optionButtons[q.correctIndex].setBackground(CORRECT_COLOR);
            optionButtons[q.correctIndex].setForeground(Color.WHITE);
        }
        scoreLabel.setText("Score: " + score);
        nextButton.setEnabled(true);
    }

    private void goToNextQuestion() {
        if (questionTimer != null) questionTimer.stop();
        currentIndex++;
        if (currentIndex >= activeQuestions.size()) {
            progressBar.setValue(activeQuestions.size());
            populateResultPanel();
            cardLayout.show(mainPanel, "RESULT");
        } else {
            loadQuestion();
        }
    }

    // ================= UI HELPERS =================
    private void styleButton(JButton button, Color bg, Color fg) {
        button.setBackground(bg);
        button.setForeground(fg);
        button.setFocusPainted(false);
        button.setFont(new Font("SansSerif", Font.BOLD, 15));
        button.setBorder(new EmptyBorder(10, 22, 10, 22));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        button.setBorderPainted(false);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        SwingUtilities.invokeLater(QuizGame::new);
    }
}
