import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class DigitalSignalEncoder extends JFrame {
    
    private static final long serialVersionUID = -3130844295177684913L;
    private JTextField inputField;
    private JComboBox<String> encodingCombo;
    private JPanel signalPanel;
    
    private static final Color BACKGROUND_COLOR = Color.BLACK;
    private static final Color AXIS_COLOR = Color.GREEN;
    private static final Color MARKING_COLOR = Color.WHITE;
    private static final Color SIGNAL_COLOR = Color.RED;
    private static final Color GRID_COLOR = new Color(0, 100, 0);

    public DigitalSignalEncoder() {
        setTitle("Digital Signal Encoder");
        setSize(650, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10)); 

        // Input panel
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);  

        inputField = new JTextField(20);
        gbc.gridx = 0;
        gbc.gridy = 0;
        inputPanel.add(new JLabel("Digital Input (0s and 1s):"), gbc);
        gbc.gridx = 1;
        inputPanel.add(inputField, gbc);

        // Encoding technique selection
        String[] encodings = {"Unipolar NRZ", "Polar NRZ", "Polar NRZ-I", "Manchester", "Differential Manchester"};
        encodingCombo = new JComboBox<>(encodings);
        gbc.gridx = 0;
        gbc.gridy = 1;
        inputPanel.add(new JLabel("Encoding Technique:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(encodingCombo, gbc);

        // Encode button
        JButton encodeButton = new JButton("Encode");
        encodeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                encodeAndVisualize();
            }
        });
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        inputPanel.add(encodeButton, gbc);

        add(inputPanel, BorderLayout.NORTH);

        // Signal visualization panel
        signalPanel = new JPanel() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                setBackground(BACKGROUND_COLOR);
                drawSignal(g);
            }
        };
        signalPanel.setPreferredSize(new Dimension(780, 400));
        add(new JScrollPane(signalPanel), BorderLayout.CENTER);

        // Add legend
        JPanel legendPanel = createLegendPanel();
        add(legendPanel, BorderLayout.SOUTH);
    }

    private JPanel createLegendPanel() {
        JPanel legendPanel = new JPanel();
        legendPanel.setBackground(BACKGROUND_COLOR);
        JLabel legendLabel = new JLabel("Legend: ");
        legendLabel.setForeground(MARKING_COLOR);
        legendPanel.add(legendLabel);
        
        JLabel signalLabel = new JLabel("Signal");
        signalLabel.setForeground(SIGNAL_COLOR);
        legendPanel.add(signalLabel);

        return legendPanel;
    }

    private void encodeAndVisualize() {
        signalPanel.repaint();
    }

    private void drawSignal(Graphics g) {
        String input = inputField.getText();
        String encoding = (String) encodingCombo.getSelectedItem();

        if (!input.matches("[01]+")) {
            g.setColor(MARKING_COLOR);
            g.drawString("Invalid input. Please enter only 0s and 1s.", 10, 20);
            return;
        }

        int yMiddle = signalPanel.getHeight() / 2;
        int xStart = 50;
        int bitWidth = 60;  
        int amplitude = 100;

        // Draw axes with markings
        drawAxes(g, xStart, yMiddle, input.length(), bitWidth, amplitude);

        // Draw the signal
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(SIGNAL_COLOR);
        g2d.setStroke(new BasicStroke(2));  

        int previousY = yMiddle;
        int currentLevel = 1; // For NRZ-I and Differential Manchester

        for (int i = 0; i < input.length(); i++) {
            int bit = Character.getNumericValue(input.charAt(i));
            int x = xStart + i * bitWidth;

            switch (encoding) {
                case "Unipolar NRZ":
                    previousY = drawUnipolarNRZ(g2d, x, yMiddle, bitWidth, amplitude, bit, previousY);
                    break;
                case "Polar NRZ":
                    previousY = drawPolarNRZ(g2d, x, yMiddle, bitWidth, amplitude, bit, previousY);
                    break;
                case "Polar NRZ-I":
                    previousY = drawPolarNRZI(g2d, x, yMiddle, bitWidth, amplitude, bit, currentLevel, previousY);
                    currentLevel = (previousY == yMiddle - amplitude) ? 1 : -1;
                    break;
                case "Manchester":
                    previousY = drawManchester(g2d, x, yMiddle, bitWidth, amplitude, bit, previousY);
                    break;
                case "Differential Manchester":
                    previousY = drawDifferentialManchester(g2d, x, yMiddle, bitWidth, amplitude, bit, currentLevel, previousY);
                    currentLevel = (previousY == yMiddle - amplitude) ? 1 : -1;
                    break;
            }
        }
    }

    private int drawUnipolarNRZ(Graphics2D g, int x, int yMiddle, int bitWidth, int amplitude, int bit, int previousY) {
        int y = bit == 1 ? yMiddle - amplitude : yMiddle;
        g.drawLine(x, previousY, x, y);
        g.drawLine(x, y, x + bitWidth, y);
        return y;
    }

    private int drawPolarNRZ(Graphics2D g, int x, int yMiddle, int bitWidth, int amplitude, int bit, int previousY) {
        int y = bit == 0 ? yMiddle + amplitude : yMiddle - amplitude;
        g.drawLine(x, previousY, x, y);
        g.drawLine(x, y, x + bitWidth, y);
        return y;
    }

    private int drawPolarNRZI(Graphics2D g, int x, int yMiddle, int bitWidth, int amplitude, int bit, int currentLevel, int previousY) {
        int y = (currentLevel == 1) ? yMiddle - amplitude : yMiddle + amplitude;
        if (bit == 1) {
            y = (currentLevel == 1) ? yMiddle + amplitude : yMiddle - amplitude;
            currentLevel = -currentLevel; // Flip the current level for the next bit
        }
        g.drawLine(x, previousY, x, y);
        g.drawLine(x, y, x + bitWidth, y);
        return y;
    }

    private int drawManchester(Graphics2D g, int x, int yMiddle, int bitWidth, int amplitude, int bit, int previousY) {
        int halfWidth = bitWidth / 2;
        int y1 = bit == 0 ? yMiddle - amplitude : yMiddle + amplitude;
        int y2 = bit == 0 ? yMiddle + amplitude : yMiddle - amplitude;
        
        g.drawLine(x, previousY, x, y1);
        g.drawLine(x, y1, x + halfWidth, y1);
        g.drawLine(x + halfWidth, y1, x + halfWidth, y2);
        g.drawLine(x + halfWidth, y2, x + bitWidth, y2);
        
        return y2;
    }

    private int drawDifferentialManchester(Graphics2D g, int x, int yMiddle, int bitWidth, int amplitude, int bit, int currentLevel, int previousY) {
        int halfWidth = bitWidth / 2;
        int y1 = (currentLevel == 1) ? yMiddle - amplitude : yMiddle + amplitude;
        int y2 = (y1 == yMiddle - amplitude) ? yMiddle + amplitude : yMiddle - amplitude;
        
        if (bit == 0) {
            // No transition at the beginning, transition in the middle
            g.drawLine(x, y1, x + halfWidth, y1);
            g.drawLine(x + halfWidth, y1, x + halfWidth, y2);
            g.drawLine(x + halfWidth, y2, x + bitWidth, y2);
            currentLevel = -currentLevel; // Flip the current level for the next bit
        } else {
            // Transition at the beginning, transition in the middle
            g.drawLine(x, y1, x, y2);
            g.drawLine(x, y2, x + halfWidth, y2);
            g.drawLine(x + halfWidth, y2, x + halfWidth, y1);
            g.drawLine(x + halfWidth, y1, x + bitWidth, y1);
            // Current level remains the same for the next bit
        }
        
        return (bit == 0) ? y2 : y1;
    }
    
    private void drawAxes(Graphics g, int xStart, int yMiddle, int bitsCount, int bitWidth, int amplitude) {
        g.setColor(AXIS_COLOR);
        
        // Draw x-axis
        g.drawLine(xStart, yMiddle, signalPanel.getWidth(), yMiddle);

        // Draw y-axis
        g.drawLine(xStart, 10, xStart, signalPanel.getHeight() - 10);

        // Draw x-axis markings
        g.setColor(MARKING_COLOR);
        for (int i = 0; i <= bitsCount; i++) {
            int x = xStart + i * bitWidth;
            g.drawLine(x, yMiddle - 5, x, yMiddle + 5);
            g.drawString(String.valueOf(i), x - 5, yMiddle + 20);
        }

        // Draw y-axis markings
        g.drawString("+5", xStart - 25, yMiddle - amplitude - 5);
        g.drawString("+2.5", xStart - 25, yMiddle - amplitude / 2 - 5);
        g.drawString("0", xStart - 15, yMiddle + 5);
        g.drawString("-2.5", xStart - 25, yMiddle + amplitude / 2 + 5);
        g.drawString("-5", xStart - 25, yMiddle + amplitude + 5);

        // Draw horizontal lines for amplitude reference
        g.setColor(GRID_COLOR);
        g.drawLine(xStart, yMiddle - amplitude, signalPanel.getWidth(), yMiddle - amplitude);
        g.drawLine(xStart, yMiddle - amplitude / 2, signalPanel.getWidth(), yMiddle - amplitude / 2);
        g.drawLine(xStart, yMiddle + amplitude / 2, signalPanel.getWidth(), yMiddle + amplitude / 2);
        g.drawLine(xStart, yMiddle + amplitude, signalPanel.getWidth(), yMiddle + amplitude);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new DigitalSignalEncoder().setVisible(true);
            }
        });
    }
}