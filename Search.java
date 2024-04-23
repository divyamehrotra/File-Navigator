import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.EnumSet;

public class Search {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> createAndShowGUI());
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("File Search Application");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLocationRelativeTo(null); // Center the frame on the screen

        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Add some padding

        // Title Panel
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel titleLabel = new JLabel("File Search Application");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titlePanel.add(titleLabel);

        // Input Panel
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JTextField userInputField = new JTextField(30);
        JButton searchButton = new JButton("Search");
        inputPanel.add(userInputField);
        inputPanel.add(searchButton);

        // Result Panel
        JPanel resultPanel = new JPanel(new BorderLayout());
        JTextArea resultTextArea = new JTextArea(15, 50);
        resultTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultTextArea);
        resultPanel.add(scrollPane, BorderLayout.CENTER);

        // Add components to content pane
        contentPane.add(titlePanel, BorderLayout.NORTH);
        contentPane.add(inputPanel, BorderLayout.CENTER);
        contentPane.add(resultPanel, BorderLayout.SOUTH);

        frame.setContentPane(contentPane);
        frame.setVisible(true);

        // Action Listener for Search Button
        searchButton.addActionListener(e -> {
            String input = userInputField.getText().trim();
            if (!input.isEmpty()) {
                Path directory = Paths.get(System.getProperty("user.home")); // Change this to your desired directory
                try {
                    ArrayList<String> results = new ArrayList<>();
                    Files.walkFileTree(directory, EnumSet.noneOf(FileVisitOption.class), 5, new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            if (file.getFileName().toString().contains(input)) {
                                results.add("Found: " + file.toString());
                            }
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                            return FileVisitResult.CONTINUE;
                        }
                    });
                    if (results.isEmpty()) {
                        resultTextArea.setText("No files found matching the search criteria.");
                    } else {
                        resultTextArea.setText(String.join("\n", results));
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(frame, "Error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Please enter a valid file name.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
