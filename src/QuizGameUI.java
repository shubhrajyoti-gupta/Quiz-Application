import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuizGameUI extends JFrame {
    private List<Question> questions;
    private int currentIndex = 0;
    private int score = 0;
    private int timeLeft = 15; // 15 seconds per question
    private JLabel questionLabel;
    private JButton[] optionButtons;
    private JLabel timerLabel;
    private JLabel scoreLabel;
    private Timer quizTimer;
    private JPanel progressPanel;
    private JLabel[] progressIndicators;
    private BackgroundPanel backgroundPanel;
    private JButton exitButton;
    private JDialog currentFeedbackDialog;

    public QuizGameUI() {
        setTitle("Quiz Challenge");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create background panel
        backgroundPanel = new BackgroundPanel();
        backgroundPanel.setLayout(new BorderLayout(10, 10));
        setContentPane(backgroundPanel);

        // Header with timer, score, and exit button
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(70, 130, 180, 200)); // Semi-transparent
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        scoreLabel.setForeground(Color.WHITE);
        headerPanel.add(scoreLabel, BorderLayout.WEST);

        // Exit button
        exitButton = new JButton("Exit");
        exitButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        exitButton.setBackground(new Color(231, 76, 60)); // Red background
        exitButton.setForeground(Color.WHITE);
        exitButton.setFocusPainted(false);
        exitButton.setBorder(new EmptyBorder(5, 15, 5, 15));
        exitButton.addActionListener(e -> System.exit(0));
        headerPanel.add(exitButton, BorderLayout.CENTER);

        timerLabel = new JLabel("15s", SwingConstants.CENTER);
        timerLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        timerLabel.setForeground(Color.WHITE);
        timerLabel.setBorder(new EmptyBorder(0, 0, 0, 20));
        headerPanel.add(timerLabel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Progress indicators
        progressPanel = new JPanel(new FlowLayout());
        progressPanel.setBackground(new Color(0, 0, 0, 0)); // Transparent
        progressPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        add(progressPanel, BorderLayout.SOUTH);

        // Question area with dark background and white text
        questionLabel = new JLabel("Question", SwingConstants.CENTER);
        questionLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        questionLabel.setBorder(new EmptyBorder(20, 20, 20, 20));
        questionLabel.setOpaque(true);
        questionLabel.setBackground(new Color(40, 40, 40)); // Dark background
        questionLabel.setForeground(Color.WHITE); // White text
        questionLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 80, 80)),
            new EmptyBorder(20, 20, 20, 20)
        ));
        add(questionLabel, BorderLayout.CENTER);

        // Options panel
        JPanel optionsPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        optionsPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        optionsPanel.setBackground(new Color(0, 0, 0, 0)); // Transparent

        optionButtons = new JButton[4];
        for (int i = 0; i < 4; i++) {
            JButton btn = new JButton();
            btn.setFont(new Font("SansSerif", Font.PLAIN, 16));
            btn.setFocusPainted(false);
            btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                new EmptyBorder(15, 15, 15, 15)
            ));
            btn.setBackground(new Color(255, 255, 255, 230)); // Semi-transparent
            btn.setOpaque(true);
            btn.addActionListener(this::handleAnswer);
            optionButtons[i] = btn;
            optionsPanel.add(btn);
        }
        add(optionsPanel, BorderLayout.SOUTH);

        loadQuestions();
        initProgressIndicators();
        showQuestion();
        startTimer();
        setVisible(true);
    }

    // Custom panel for background image
    class BackgroundPanel extends JPanel {
        private BufferedImage backgroundImage;

        public BackgroundPanel() {
            // Create a gradient background as fallback
            createGradientBackground();
        }

        private void createGradientBackground() {
            // Create a simple gradient background
            backgroundImage = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = backgroundImage.createGraphics();
            // Create gradient
            GradientPaint gradient = new GradientPaint(
                0, 0, new Color(70, 130, 180),
                0, 600, new Color(30, 80, 130)
            );
            g2d.setPaint(gradient);
            g2d.fillRect(0, 0, 800, 600);
            // Add some abstract shapes
            g2d.setColor(new Color(255, 255, 255, 30));
            for (int i = 0; i < 20; i++) {
                int x = (int) (Math.random() * 800);
                int y = (int) (Math.random() * 600);
                int size = (int) (Math.random() * 100) + 20;
                g2d.fillOval(x, y, size, size);
            }
            g2d.dispose();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }

    private void initProgressIndicators() {
        progressIndicators = new JLabel[questions.size()];
        for (int i = 0; i < questions.size(); i++) {
            progressIndicators[i] = new JLabel("○");
            progressIndicators[i].setFont(new Font("SansSerif", Font.BOLD, 20));
            progressIndicators[i].setForeground(new Color(255, 255, 255, 200)); // Semi-transparent white
            progressPanel.add(progressIndicators[i]);
        }
        updateProgressIndicators();
    }

    private void updateProgressIndicators() {
        for (int i = 0; i < progressIndicators.length; i++) {
            if (i < currentIndex) {
                progressIndicators[i].setText("●");
                progressIndicators[i].setForeground(new Color(255, 255, 255, 230)); // White for completed
            } else if (i == currentIndex) {
                progressIndicators[i].setText("●");
                progressIndicators[i].setForeground(new Color(255, 215, 0, 230)); // Gold for current
            } else {
                progressIndicators[i].setText("○");
                progressIndicators[i].setForeground(new Color(255, 255, 255, 150)); // Light white for upcoming
            }
        }
    }

    private void loadQuestions() {
        questions = new ArrayList<>();
        // Software Engineering Questions (10) - Shortened
        questions.add(new Question("What does the 'S' in SOLID stand for?",
            new String[]{"Single Responsibility", "Simple Structure", "Systematic Setup", "Standard Solution"},
            "Single Responsibility"));
        questions.add(new Question("Which pattern ensures one instance only?",
            new String[]{"Factory", "Singleton", "Observer", "Adapter"},
            "Singleton"));
        questions.add(new Question("What is RESTful API primarily used for?",
            new String[]{"Real-time chat", "Stateless communication", "Database storage", "File encryption"},
            "Stateless communication"));
        questions.add(new Question("What is a 'Sprint' in Agile?",
            new String[]{"Testing phase", "Time-boxed iteration", "Documentation phase", "Deployment process"},
            "Time-boxed iteration"));
        questions.add(new Question("What does CI/CD stand for?",
            new String[]{"Code Integration/Code Deployment", "Continuous Integration/Continuous Deployment",
                         "Component Integration/Component Delivery", "Configuration Integration/Configuration Deployment"},
            "Continuous Integration/Continuous Deployment"));
        questions.add(new Question("What tests individual units of code?",
            new String[]{"Integration Testing", "System Testing", "Unit Testing", "Acceptance Testing"},
            "Unit Testing"));
        questions.add(new Question("Main benefit of Git version control?",
            new String[]{"Faster execution", "Change tracking", "Auto optimization", "Enhanced security"},
            "Change tracking"));
        questions.add(new Question("Purpose of 'User Story' in Agile?",
            new String[]{"Technical specs", "User perspective", "Bug tracking", "Timeline management"},
            "User perspective"));
        questions.add(new Question("Open-Closed Principle means?",
            new String[]{"Open for bugs", "Open for extension", "Open for deletion", "Open for modification"},
            "Open for extension"));
        questions.add(new Question("What is 'Technical Debt'?",
            new String[]{"Financial costs", "Rework from shortcuts", "Hardware costs", "Licensing fees"},
            "Rework from shortcuts"));
        // Java Questions (10) - Shortened
        questions.add(new Question("Purpose of 'final' keyword in Java?",
            new String[]{"Immutable variable", "Prevent overriding", "Both A and B", "Garbage collection"},
            "Both A and B"));
        questions.add(new Question("Which allows null and order?",
            new String[]{"HashSet", "TreeSet", "ArrayList", "HashMap"},
            "ArrayList"));
        questions.add(new Question("Difference between '==' and '.equals()'?",
            new String[]{"Both same", "References vs values", "Values vs references", "Primitives only"},
            "References vs values"));
        questions.add(new Question("What does JVM stand for?",
            new String[]{"Java Virtual Machine", "Java Verification", "Java Validation", "Java Version"},
            "Java Virtual Machine"));
        questions.add(new Question("Keyword to call superclass constructor?",
            new String[]{"this", "super", "extends", "implements"},
            "super"));
        questions.add(new Question("Purpose of 'static' keyword?",
            new String[]{"No instantiation", "Shared variables", "Both A and B", "Memory optimization"},
            "Both A and B"));
        questions.add(new Question("Exception for illegal array index?",
            new String[]{"IllegalArgumentException", "ArrayIndexOutOfBoundsException",
                         "IndexOutOfBoundsException", "IllegalStateException"},
            "ArrayIndexOutOfBoundsException"));
        questions.add(new Question("Result of 'null instanceof Object'?",
            new String[]{"true", "false", "Compilation error", "Runtime error"},
            "false"));
        questions.add(new Question("Java feature for multiple inheritance?",
            new String[]{"Classes", "Interfaces", "Abstract classes", "Packages"},
            "Interfaces"));
        questions.add(new Question("Purpose of 'transient' keyword?",
            new String[]{"Non-serializable", "Prevent modification", "Optimize access", "Temporary variables"},
            "Non-serializable"));
        // UI/UX Questions (10) - Shortened
        questions.add(new Question("What does UX stand for?",
            new String[]{"User Experience", "User Execution", "Universal Exchange", "Utility Extension"},
            "User Experience"));
        questions.add(new Question("Principle for grouping related items?",
            new String[]{"Contrast", "Repetition", "Alignment", "Proximity"},
            "Proximity"));
        questions.add(new Question("Purpose of a wireframe?",
            new String[]{"Color schemes", "Website skeleton", "Backend code", "Database testing"},
            "Website skeleton"));
        questions.add(new Question("Best readability color combination?",
            new String[]{"Red/Green", "Black/White", "Yellow/White", "Blue/Purple"},
            "Black/White"));
        questions.add(new Question("Meaning of 'affordance' in UX?",
            new String[]{"Implementation cost", "Usage properties", "User count", "Aesthetic appeal"},
            "Usage properties"));
        questions.add(new Question("Method observing users completing tasks?",
            new String[]{"Unit testing", "Usability testing", "Regression testing", "Integration testing"},
            "Usability testing"));
        questions.add(new Question("Minimum touch target size?",
            new String[]{"24x24 pixels", "44x44 pixels", "32x32 pixels", "48x48 pixels"},
            "44x44 pixels"));
        questions.add(new Question("Design principle for recognition?",
            new String[]{"Visibility", "Feedback", "Recognition recall", "Constraints"},
            "Recognition recall"));
        questions.add(new Question("Goal of responsive web design?",
            new String[]{"Faster loading", "Cross-device experience", "Reduce server load", "SEO improvement"},
            "Cross-device experience"));
        questions.add(new Question("Best method for natural user behavior?",
            new String[]{"A/B testing", "Focus groups", "Ethnographic studies", "Surveys"},
            "Ethnographic studies"));
        Collections.shuffle(questions);
    }

    private void showQuestion() {
        if (currentIndex >= questions.size()) {
            showResult();
            return;
        }
        Question q = questions.get(currentIndex);
        questionLabel.setText("<html><div style='text-align: center; color: white;'>" +
                             "Q" + (currentIndex + 1) + ": " + q.getQuestionText() +
                             "</div></html>");
        String[] options = q.getOptions();
        for (int i = 0; i < 4; i++) {
            optionButtons[i].setText("<html><div style='text-align: center;'>" + options[i] + "</div></html>");
            optionButtons[i].setEnabled(true);
            optionButtons[i].setBackground(new Color(255, 255, 255, 230));
            optionButtons[i].setForeground(Color.BLACK);
        }
        resetTimer();
        updateProgressIndicators();
    }

    private void startTimer() {
        quizTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                timeLeft--;
                timerLabel.setText(timeLeft + "s");
                timerLabel.setForeground(timeLeft <= 5 ? Color.RED : Color.WHITE);
                if (timeLeft <= 0) {
                    quizTimer.stop();
                    handleTimeout();
                }
            }
        });
        quizTimer.start();
    }

    private void resetTimer() {
        if (quizTimer != null) {
            quizTimer.stop();
        }
        timeLeft = 15;
        timerLabel.setText(timeLeft + "s");
        timerLabel.setForeground(Color.WHITE);
        startTimer();
    }

    private void handleAnswer(ActionEvent e) {
        if (timeLeft <= 0) return; // Prevent answering after timeout
        quizTimer.stop();
        JButton clicked = (JButton) e.getSource();
        String chosenAnswer = clicked.getText();
        // Remove HTML tags for comparison
        chosenAnswer = chosenAnswer.replace("<html><div style='text-align: center;'>", "")
                                  .replace("</div></html>", "");
        Question q = questions.get(currentIndex);
        boolean correct = chosenAnswer.equalsIgnoreCase(q.getCorrectAnswer());
        if (correct) score++;
        // Visual feedback
        for (JButton btn : optionButtons) {
            btn.setEnabled(false);
            String buttonText = btn.getText().replace("<html><div style='text-align: center;'>", "")
                                            .replace("</div></html>", "");
            if (buttonText.equalsIgnoreCase(q.getCorrectAnswer())) {
                btn.setBackground(new Color(46, 204, 113, 230)); // Green for correct
                btn.setForeground(Color.WHITE);
            } else if (btn == clicked && !correct) {
                btn.setBackground(new Color(231, 76, 60, 230)); // Red for incorrect
                btn.setForeground(Color.WHITE);
            }
        }
        scoreLabel.setText("Score: " + score);
        showFeedback(correct);
    }

    private void handleTimeout() {
        Question q = questions.get(currentIndex);
        // Highlight correct answer
        for (JButton btn : optionButtons) {
            btn.setEnabled(false);
            String buttonText = btn.getText().replace("<html><div style='text-align: center;'>", "")
                                            .replace("</div></html>", "");
            if (buttonText.equalsIgnoreCase(q.getCorrectAnswer())) {
                btn.setBackground(new Color(46, 204, 113, 230));
                btn.setForeground(Color.WHITE);
            } else {
                btn.setBackground(new Color(231, 76, 60, 230));
                btn.setForeground(Color.WHITE);
            }
        }
        showFeedback(false);
    }

    private void showFeedback(boolean isCorrect) {
        // Close any existing feedback dialog
        if (currentFeedbackDialog != null && currentFeedbackDialog.isVisible()) {
            currentFeedbackDialog.dispose();
        }

        currentFeedbackDialog = new JDialog(this, "Result", true);
        currentFeedbackDialog.setSize(300, 200);
        currentFeedbackDialog.setLocationRelativeTo(this);
        currentFeedbackDialog.setLayout(new BorderLayout());
        currentFeedbackDialog.setUndecorated(true);
        currentFeedbackDialog.getRootPane().setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 2));

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(new Color(255, 255, 255, 240)); // Semi-transparent
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel icon = new JLabel();
        icon.setHorizontalAlignment(SwingConstants.CENTER);
        icon.setFont(new Font("SansSerif", Font.BOLD, 60));
        JLabel resultText = new JLabel("", SwingConstants.CENTER);
        resultText.setFont(new Font("SansSerif", Font.BOLD, 20));
        resultText.setBorder(new EmptyBorder(10, 0, 0, 0));

        if (isCorrect) {
            icon.setText("✓");
            icon.setForeground(new Color(46, 204, 113));
            resultText.setText("Correct!");
            resultText.setForeground(new Color(46, 204, 113));
        } else {
            icon.setText("✗");
            icon.setForeground(new Color(231, 76, 60));
            resultText.setText(timeLeft <= 0 ? "Time's Up!" : "Incorrect");
            resultText.setForeground(new Color(231, 76, 60));
        }

        contentPanel.add(icon, BorderLayout.CENTER);
        contentPanel.add(resultText, BorderLayout.SOUTH);
        currentFeedbackDialog.add(contentPanel);

        // Timer to auto-close dialog and move to next question
        Timer timer = new Timer(1000, ev -> {
            if (currentFeedbackDialog != null && currentFeedbackDialog.isVisible()) {
                currentFeedbackDialog.dispose();
            }
            currentIndex++;
            showQuestion();
        });
        timer.setRepeats(false);
        timer.start();
        currentFeedbackDialog.setVisible(true);
    }

    private void showResult() {
        quizTimer.stop();
        JDialog resultDialog = new JDialog(this, "Quiz Completed", true);
        resultDialog.setSize(400, 350); // Increased height to accommodate buttons better
        resultDialog.setLocationRelativeTo(this);
        resultDialog.setUndecorated(true);
        resultDialog.getRootPane().setBorder(BorderFactory.createLineBorder(new Color(70, 130, 180), 3));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(new Color(255, 255, 255, 240)); // Semi-transparent
        contentPanel.setBorder(new EmptyBorder(30, 30, 30, 30));

        JLabel titleLabel = new JLabel("Quiz Completed!", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setForeground(new Color(70, 130, 180));

        JLabel scoreLabel = new JLabel("Your Score: " + score + "/" + questions.size(), SwingConstants.CENTER);
        scoreLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        scoreLabel.setBorder(new EmptyBorder(20, 0, 20, 0));

        int percentage = (int) Math.round((double) score / questions.size() * 100);
        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setValue(percentage);
        progressBar.setStringPainted(true);
        progressBar.setFont(new Font("SansSerif", Font.PLAIN, 16));
        progressBar.setForeground(new Color(70, 130, 180));
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        progressBar.setBorder(new EmptyBorder(0, 0, 20, 0));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(0, 0, 0, 0));
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        JButton restartButton = new JButton("Play Again");
        restartButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        restartButton.setBackground(Color.WHITE); // White background
        restartButton.setForeground(new Color(70, 130, 180)); // Blue text
        restartButton.setFocusPainted(false);
        restartButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(70, 130, 180)),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        restartButton.addActionListener(e -> {
            resultDialog.dispose();
            restartQuiz();
        });

        JButton exitButton = new JButton("Exit");
        exitButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        exitButton.setBackground(Color.WHITE); // White background
        exitButton.setForeground(new Color(231, 76, 60)); // Red text
        exitButton.setFocusPainted(false);
        exitButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(231, 76, 60)),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        exitButton.addActionListener(e -> System.exit(0));

        buttonPanel.add(restartButton);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(exitButton);

        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(scoreLabel);
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(progressBar);
        contentPanel.add(Box.createVerticalStrut(30));
        contentPanel.add(buttonPanel);

        resultDialog.add(contentPanel);
        resultDialog.setVisible(true);
    }

    private void restartQuiz() {
        currentIndex = 0;
        score = 0;
        scoreLabel.setText("Score: 0");
        Collections.shuffle(questions);
        showQuestion();
        startTimer();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new QuizGameUI();
        });
    }
}

class Question {
    private String questionText;
    private String[] options;
    private String correctAnswer;

    public Question(String questionText, String[] options, String correctAnswer) {
        this.questionText = questionText;
        this.options = options;
        this.correctAnswer = correctAnswer;
    }

    public String getQuestionText() {
        return questionText;
    }

    public String[] getOptions() {
        return options;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }
}
