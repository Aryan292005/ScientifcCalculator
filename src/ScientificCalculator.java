import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Stack;
public class ScientificCalculator {

        private JTextField inputField;
        private JTextArea historyArea;
        private Stack<String> history;

        public ScientificCalculator() {
            history = new Stack<>();

            // Frame
            JFrame frame = new JFrame("Scientific Calculator");
            frame.setSize(500, 600);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout());

            // Light theme colors
            Color bgColor = Color.WHITE;
            Color fgColor = Color.BLACK;

            // Input field
            inputField = new JTextField();
            inputField.setFont(new Font("Arial", Font.PLAIN, 24));
            inputField.setBackground(bgColor);
            inputField.setForeground(fgColor);
            frame.add(inputField, BorderLayout.NORTH);

            // Buttons Panel
            JPanel buttonPanel = new JPanel(new GridLayout(6, 4, 5, 5));
            String[] buttons = {
                    "7", "8", "9", "/",
                    "4", "5", "6", "*",
                    "1", "2", "3", "-",
                    "0", ".", "=", "+",
                    "sqrt", "pow", "sin", "cos",
                    "tan", "log", "CLR", "EXIT"
            };

            for (String text : buttons) {
                JButton button = new JButton(text);
                button.setFont(new Font("Arial", Font.BOLD, 18));
                button.setBackground(bgColor);
                button.setForeground(fgColor);
                button.addActionListener(e -> onButtonClick(e.getActionCommand()));
                buttonPanel.add(button);
            }

            frame.add(buttonPanel, BorderLayout.CENTER);

            // History Area
            historyArea = new JTextArea(5, 20);
            historyArea.setEditable(false);
            historyArea.setBackground(bgColor);
            historyArea.setForeground(fgColor);
            JScrollPane scrollPane = new JScrollPane(historyArea);
            frame.add(scrollPane, BorderLayout.SOUTH);

            frame.setVisible(true);
        }

        private void onButtonClick(String command) {
            try {
                switch (command) {
                    case "=" -> evaluateExpression();
                    case "CLR" -> inputField.setText("");
                    case "EXIT" -> System.exit(0);
                    case "sqrt" -> inputField.setText(String.valueOf(Math.sqrt(Double.parseDouble(inputField.getText()))));
                    case "pow" -> inputField.setText(inputField.getText() + "^");
                    case "sin" -> inputField.setText(String.valueOf(Math.sin(Math.toRadians(Double.parseDouble(inputField.getText())))));
                    case "cos" -> inputField.setText(String.valueOf(Math.cos(Math.toRadians(Double.parseDouble(inputField.getText())))));
                    case "tan" -> inputField.setText(String.valueOf(Math.tan(Math.toRadians(Double.parseDouble(inputField.getText())))));
                    case "log" -> inputField.setText(String.valueOf(Math.log10(Double.parseDouble(inputField.getText()))));
                    default -> inputField.setText(inputField.getText() + command);
                }
            } catch (Exception ex) {
                inputField.setText("Error");
            }
        }

        private void evaluateExpression() {
            try {
                String input = inputField.getText();
                double result = eval(input);
                String entry = input + " = " + result;
                history.push(entry);
                updateHistory();
                inputField.setText(String.valueOf(result));
            } catch (Exception e) {
                inputField.setText("Error");
            }
        }

        private void updateHistory() {
            StringBuilder sb = new StringBuilder();
            for (String entry : history) {
                sb.append(entry).append("\n");
            }
            historyArea.setText(sb.toString());
        }

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
                    if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char)ch);
                    return x;
                }

                double parseExpression() {
                    double x = parseTerm();
                    while(true) {
                        if      (eat('+')) x += parseTerm();
                        else if (eat('-')) x -= parseTerm();
                        else return x;
                    }
                }

                double parseTerm() {
                    double x = parseFactor();
                    while(true) {
                        if      (eat('*')) x *= parseFactor();
                        else if (eat('/')) x /= parseFactor();
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
                        eat(')');
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
            SwingUtilities.invokeLater(ScientificCalculator::new);
        }
    }

