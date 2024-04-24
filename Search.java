import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Utilities;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.EnumSet;

public class Search {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Search::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame frame = createFrame();
        JPanel contentPane = createContentPane();
        addComponentsToContentPane(contentPane, frame);
        frame.setContentPane(contentPane);
        frame.setVisible(true);
    }

    private static JFrame createFrame() {
        JFrame frame = new JFrame("File Search Application");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLocationRelativeTo(null); // Center the frame on the screen
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                adjustInputSizes(frame);
            }
        });
        return frame;
    }

    private static JPanel createContentPane() {
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Add some padding
        contentPane.setBackground(Color.BLACK); // Set background color
        return contentPane;
    }

    private static void addComponentsToContentPane(JPanel contentPane, JFrame frame) {
        // Title Panel
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel titleLabel = new JLabel("File Search Application");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 40));
        titleLabel.setForeground(new Color(255, 255, 255)); // Set text color
        titlePanel.add(titleLabel);
        titlePanel.setBackground(new Color(65, 105, 225)); // Set background color

        JPanel spacePanel = new JPanel();
        spacePanel.setPreferredSize(new Dimension(50, 60));

        // Input Panel
        JPanel inputPanel = new JPanel();
        inputPanel.setPreferredSize(new Dimension(400, 50)); // Set the preferred size of the panel

        JTextField directoryField = new JTextField();
        directoryField.setPreferredSize(new Dimension(150, 15)); // Set the preferred size of the directory field

        JTextField fileField = new JTextField();
        fileField.setPreferredSize(new Dimension(150, 15)); // Set the preferred size of the file field

        JButton searchButton = new JButton("Search");
        searchButton.setPreferredSize(new Dimension(80, 15)); // Set the preferred size of the search button

        JButton browseButton = new JButton("Browse");
        browseButton.setPreferredSize(new Dimension(80, 15)); // Set the preferred size of the browse button

        inputPanel.add(new JLabel("Directory:"));
        inputPanel.add(directoryField);
        inputPanel.add(browseButton);
        inputPanel.add(new JLabel("File Name:"));
        inputPanel.add(fileField);
        inputPanel.add(searchButton);
        inputPanel.setBackground(Color.LIGHT_GRAY);

        // Result Panel
        JPanel resultPanel = new JPanel(new BorderLayout());
        JTextArea resultTextArea = new JTextArea(15, 50);
        resultTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultTextArea);
        resultPanel.add(scrollPane, BorderLayout.CENTER);
        resultPanel.setBackground(new Color(240, 240, 240)); // Set background color

        // Add components to content pane
        contentPane.add(titlePanel, BorderLayout.NORTH);
        contentPane.add(inputPanel, BorderLayout.CENTER);
        contentPane.add(resultPanel, BorderLayout.SOUTH);

        // Action Listeners
        searchButton.addActionListener(e -> searchAction(directoryField, fileField, resultTextArea));
        browseButton.addActionListener(e -> browseAction(directoryField, resultTextArea, frame));

        // Add mouse listener to resultTextArea
        resultTextArea.addMouseListener(new MouseAdapter() {
            @SuppressWarnings("deprecation")
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getClickCount() == 2) {
                    JTextArea textArea = (JTextArea) e.getSource();
                    int offset = textArea.viewToModel(e.getPoint());
                    try {
                        int start = Utilities.getRowStart(textArea, offset);
                        int end = Utilities.getRowEnd(textArea, offset);
                        String line = textArea.getText().substring(start, end);
                        if (line.startsWith("Found: ")) {
                            String filePath = line.substring("Found: ".length()).trim();
                            Path file = Paths.get(filePath);
                            if (Files.exists(file)) {
                                try {
                                    Desktop.getDesktop().open(file.toFile());
                                } catch (IOException ex) {
                                    JOptionPane.showMessageDialog(null, "Error opening file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                                }
                            } else {
                                JOptionPane.showMessageDialog(null, "File not found: " + filePath, "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    } catch (BadLocationException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

    private static void searchAction(JTextField directoryField, JTextField fileField, JTextArea resultTextArea) {
        String directoryName = directoryField.getText().trim();
        String fileName = fileField.getText().trim();
        if (!directoryName.isEmpty() && !fileName.isEmpty()) {
            Path directory = Paths.get(directoryName);
            try {
                ArrayList<String> results = new ArrayList<>();
                Files.walkFileTree(directory, EnumSet.noneOf(FileVisitOption.class), 5, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        if (file.getFileName().toString().contains(fileName)) {
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
                    resultTextArea.setText("");
                    for (String result : results) {
                        resultTextArea.append(result + "\n");
                    }
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "Error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(null, "Please enter both directory name and file name.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void browseAction(JTextField directoryField, JTextArea resultTextArea, JFrame frame) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int option = fileChooser.showOpenDialog(frame);
        if (option == JFileChooser.APPROVE_OPTION) {
            Path selectedDirectory = fileChooser.getSelectedFile().toPath();
            directoryField.setText(selectedDirectory.toAbsolutePath().toString());
        }
    }

    private static void adjustInputSizes(JFrame frame) {
        for (Component component : frame.getContentPane().getComponents()) {
            if (component instanceof JPanel) {
                JPanel panel = (JPanel) component;
                for (Component innerComponent : panel.getComponents()) {
                    if (innerComponent instanceof JTextField || innerComponent instanceof JButton) {
                        Dimension newDimension = calculateNewSize(frame, innerComponent);
                        innerComponent.setPreferredSize(newDimension);
                    }
                }
            }
        }
    }

    private static Dimension calculateNewSize(JFrame frame, Component component) {
        int newWidth = (int) (frame.getWidth() * 0.3); // Adjust the percentage as needed
        int newHeight = (int) (frame.getHeight() * 0.1); // Adjust the percentage as needed
        return new Dimension(newWidth, newHeight);
    }
}
