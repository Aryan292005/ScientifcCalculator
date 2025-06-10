import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Stack;
import java.util.HashMap;
import java.util.Map;

public class ScientificCalculator {
    private JFrame frame;
    private JTextField inputField;
    private JTextArea historyArea;
    private Stack<String> history;
    private JPanel buttonPanel;
    private Map<String, Double> variables;
    private boolean radianMode = false;

    public ScientificCalculator() {
        history = new Stack<>();
        variables = new HashMap<>();
        variables.put("π", Math.PI);
        variables.put("e", Math.E);
        initializeUI();
    }
    private void initializeUI() {
        // Frame setup
        frame = new JFrame("Enhanced Scientific Calculator");
        frame.setSize(650, 800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(5, 5));
        createMenuBar();

        // Input field
        inputField = new JTextField();
        inputField.setFont(new Font("Arial", Font.PLAIN, 28));
        inputField.setHorizontalAlignment(JTextField.RIGHT);
        inputField.setMargin(new Insets(10, 10, 10, 10));
        frame.add(inputField, BorderLayout.NORTH);

        // Button panel
        buttonPanel = new JPanel(new GridLayout(0, 5, 5, 5));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        createButtons();

        JScrollPane buttonScrollPane = new JScrollPane(buttonPanel);
        frame.add(buttonScrollPane, BorderLayout.CENTER);
        // History area
        historyArea = new JTextArea(8, 30);
        historyArea.setEditable(false);
        historyArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane historyScrollPane = new JScrollPane(historyArea);
        frame.add(historyScrollPane, BorderLayout.SOUTH);
        addKeyboardSupport();
        frame.setVisible(true);
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu modeMenu = new JMenu("Mode");
        JRadioButtonMenuItem degreeItem = new JRadioButtonMenuItem("Degrees");
        JRadioButtonMenuItem radianItem = new JRadioButtonMenuItem("Radians", true);
        ButtonGroup modeGroup = new ButtonGroup();
        modeGroup.add(degreeItem);
        modeGroup.add(radianItem);

        degreeItem.addActionListener(e -> radianMode = false);
        radianItem.addActionListener(e -> radianMode = true);

        modeMenu.add(degreeItem);
        modeMenu.add(radianItem);
        menuBar.add(modeMenu);

        // Memory menu
        JMenu memoryMenu = new JMenu("Memory");
        JMenuItem storeItem = new JMenuItem("Store");
        JMenuItem recallItem = new JMenuItem("Recall");
        JMenuItem clearItem = new JMenuItem("Clear Memory");

        storeItem.addActionListener(e -> storeToMemory());
        recallItem.addActionListener(e -> recallFromMemory());
        clearItem.addActionListener(e -> variables.clear());

        memoryMenu.add(storeItem);
        memoryMenu.add(recallItem);
        memoryMenu.add(clearItem);
        menuBar.add(memoryMenu);

        frame.setJMenuBar(menuBar);
    }

    private void createButtons() {
        // Button groups
        String[][] buttonGroups = {
                {"7", "8", "9", "/", "sqrt", "x²", "x^y", "e^x", "ln"},
                {"4", "5", "6", "*", "pow", "x³", "10^x", "log", "mod"},
                {"1", "2", "3", "-", "sin", "cos", "tan", "(", ")"},
                {"0", ".", "=", "+", "π", "e", "!", "1/x", "|x|"},
                {"C", "CE", "(", ")", "rand", "MR", "MC", "MS", "M+"}
        };

        for (String[] group : buttonGroups) {
            for (String text : group) {
                JButton button = new JButton(text);
                button.setFont(new Font("Arial", Font.BOLD, 16));
                button.setFocusPainted(false);
                button.setMargin(new Insets(5, 5, 5, 5));

                // Color coding
                if (text.matches("[0-9.]")) {
                    button.setBackground(new Color(240, 240, 240));
                } else if (text.matches("[+\\-*/=]")) {
                    button.setBackground(new Color(200, 230, 255));
                } else if (text.matches("[A-Za-z]+")) {
                    button.setBackground(new Color(255, 230, 200));
                }

                button.addActionListener(this::handleButtonClick);
                buttonPanel.add(button);
            }
        }
    }

    private void handleButtonClick(ActionEvent e) {
        String command = e.getActionCommand();
        try {
            switch (command) {
                // Basic operations
                case "=" -> evaluateExpression();
                case "C" -> inputField.setText("");
                case "CE" -> clearAll();

                // Memory functions
                case "MS" -> storeToMemory();
                case "MR" -> recallFromMemory();
                case "MC" -> variables.clear();
                case "M+" -> addToMemory();

                // Scientific functions
                case "sqrt" -> inputField.setText(String.valueOf(Math.sqrt(getCurrentValue())));
                case "pow" -> inputField.setText(inputField.getText() + "^");
                case "sin" -> inputField.setText(String.valueOf(calculateTrigFunction("sin")));
                case "cos" -> inputField.setText(String.valueOf(calculateTrigFunction("cos")));
                case "tan" -> inputField.setText(String.valueOf(calculateTrigFunction("tan")));
                case "log" -> inputField.setText(String.valueOf(Math.log10(getCurrentValue())));
                case "ln" -> inputField.setText(String.valueOf(Math.log(getCurrentValue())));
                case "π" -> appendToInput(String.valueOf(Math.PI));
                case "e" -> appendToInput(String.valueOf(Math.E));
                case "x²" -> inputField.setText(String.valueOf(Math.pow(getCurrentValue(), 2)));
                case "x³" -> inputField.setText(String.valueOf(Math.pow(getCurrentValue(), 3)));
                case "x^y" -> inputField.setText(inputField.getText() + "^");
                case "10^x" -> inputField.setText(String.valueOf(Math.pow(10, getCurrentValue())));
                case "e^x" -> inputField.setText(String.valueOf(Math.exp(getCurrentValue())));
                case "!" -> inputField.setText(String.valueOf(factorial(getCurrentValue())));
                case "mod" -> inputField.setText(inputField.getText() + "%");
                case "rand" -> inputField.setText(String.valueOf(Math.random()));
                case "1/x" -> inputField.setText(String.valueOf(1 / getCurrentValue()));
                case "|x|" -> inputField.setText(String.valueOf(Math.abs(getCurrentValue())));

                // Default case for numbers and operators
                default -> appendToInput(command);
            }
        } catch (Exception ex) {
            showError("Invalid operation: " + ex.getMessage());
        }
    }

    private double calculateTrigFunction(String function) {
        double value = getCurrentValue();
        if (!radianMode) {
            value = Math.toRadians(value);
        }
        return switch (function) {
            case "sin" -> Math.sin(value);
            case "cos" -> Math.cos(value);
            case "tan" -> Math.tan(value);
            default -> throw new IllegalArgumentException("Unknown function");
        };
    }

    private void appendToInput(String text) {
        inputField.setText(inputField.getText() + text);
    }

    private void storeToMemory() {
        try {
            double value = eval(inputField.getText());
            variables.put("M", value);
            showMessage("Stored " + value + " to memory");
        } catch (Exception e) {
            showError("Cannot store invalid value to memory");
        }
    }

    private void recallFromMemory() {
        if (variables.containsKey("M")) {
            inputField.setText(String.valueOf(variables.get("M")));
        } else {
            showError("No value stored in memory");
        }
    }

    private void addToMemory() {
        if (variables.containsKey("M")) {
            try {
                double current = variables.get("M");
                double toAdd = eval(inputField.getText());
                variables.put("M", current + toAdd);
                showMessage("Added " + toAdd + " to memory");
            } catch (Exception e) {
                showError("Cannot add invalid value to memory");
            }
        } else {
            storeToMemory();
        }
    }

    private void clearAll() {
        inputField.setText("");
        history.clear();
        historyArea.setText("");
    }

    private double getCurrentValue() {
        String text = inputField.getText();
        if (text.isEmpty()) return 0;
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            showError("Invalid number format");
            return Double.NaN;
        }
    }

    private long factorial(double n) {
        if (n < 0) throw new IllegalArgumentException("Factorial of negative number");
        if (n > 20) throw new IllegalArgumentException("Value too large for factorial");
        long result = 1;
        for (int i = 1; i <= n; i++) {
            result *= i;
        }
        return result;
    }

    private void evaluateExpression() {
        try {
            String input = inputField.getText().trim();
            if (input.isEmpty()) {
                showError("No input provided");
                return;
            }

            // Replace special constants and functions
            input = preprocessInput(input);

            double result = eval(input);
            if (Double.isInfinite(result)) {
                showError("Result is too large");
                return;
            }
            if (Double.isNaN(result)) {
                showError("Invalid calculation");
                return;
            }

            String entry = input + " = " + result;
            history.push(entry);
            updateHistory();
            inputField.setText(String.valueOf(result));
        } catch (Exception e) {
            showError("Calculation error: " + e.getMessage());
        }
    }

    private String preprocessInput(String input) {
        // Replace constants
        input = input.replace("π", String.valueOf(Math.PI))
                .replace("e", String.valueOf(Math.E));

        // Replace memory variable
        if (variables.containsKey("M")) {
            input = input.replace("MR", String.valueOf(variables.get("M")));
        }

        return input;
    }

    private void updateHistory() {
        StringBuilder sb = new StringBuilder();
        sb.append("Calculation History:\n");
        for (String entry : history) {
            sb.append("• ").append(entry).append("\n");
        }
        historyArea.setText(sb.toString());
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(frame, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    private void addKeyboardSupport() {
        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    evaluateExpression();
                }
            }
        });

        // Add keyboard shortcuts for buttons
        InputMap inputMap = buttonPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = buttonPanel.getActionMap();

        String[] keyBindings = {
                "1", "2", "3", "4", "5", "6", "7", "8", "9", "0",
                "+", "-", "*", "/", "=", ".", "(", ")", "C", "CE"
        };

        for (String key : keyBindings) {
            inputMap.put(KeyStroke.getKeyStroke(key.charAt(0)), key);
            actionMap.put(key, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    for (Component comp : buttonPanel.getComponents()) {
                        if (comp instanceof JButton) {
                            JButton btn = (JButton) comp;
                            if (btn.getText().equals(key)) {
                                btn.doClick();
                                break;
                            }
                        }
                    }
                }
            });
        }
    }

    // Enhanced eval method with better error handling
    public static double eval(final String str) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < str.length()) ? str.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < str.length()) throw new RuntimeException("Unexpected character: " + (char)ch);
                return x;
            }

            double parseExpression() {
                double x = parseTerm();
                for (;;) {
                    if      (eat('+')) x += parseTerm();
                    else if (eat('-')) x -= parseTerm();
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (;;) {
                    if      (eat('*')) x *= parseFactor();
                    else if (eat('/')) x /= parseFactor();
                    else if (eat('%')) x %= parseFactor();
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor();
                if (eat('-')) return -parseFactor();

                double x;
                int startPos = this.pos;
                if (eat('(')) {
                    x = parseExpression();
                    if (!eat(')')) throw new RuntimeException("Missing ')'");
                } else if ((ch >= '0' && ch <= '9') || ch == '.') {
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(str.substring(startPos, this.pos));
                } else {
                    throw new RuntimeException("Unexpected: " + (char)ch);
                }

                if (eat('^')) x = Math.pow(x, parseFactor());
                return x;
            }
        }.parse();
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                new ScientificCalculator();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}