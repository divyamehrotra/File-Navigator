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
        SwingUtilities.invokeLater(Search::createAndShowGUI);//method reference syntax
        //SwingUtilities.invokeLater() is a method used to perform GUI-related tasks asynchronously. It schedules the specified task to be executed on the EDT.
    }

    private static void createAndShowGUI() {
        JFrame frame = createFrame();
        JPanel contentPane = createContentPane();
        addComponentsToContentPane(contentPane, frame);
        frame.setContentPane(contentPane);
        frame.setVisible(true);
        frame.setResizable(false);
    }

    private static JFrame createFrame() {
        JFrame frame = new JFrame("File Search Application");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 400);
        frame.setLocationRelativeTo(null); //center
        return frame;
    }

    private static JPanel createContentPane() {
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        contentPane.setBackground(Color.BLACK); 
        return contentPane;
    }

    private static void addComponentsToContentPane(JPanel contentPane, JFrame frame) {
        // Title Panel
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel titleLabel = new JLabel("NIO Search Engine");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 40));
        titleLabel.setForeground(Color.WHITE); 
        titlePanel.add(titleLabel);
        titlePanel.setBackground(Color.BLACK); 
        // Input Panel
        JPanel inputPanel = new JPanel();
        inputPanel.setPreferredSize(new Dimension(400, 50)); 

        JTextField directoryField = new JTextField();
        directoryField.setPreferredSize(new Dimension(150, 25)); 

        JTextField fileField = new JTextField();
        fileField.setPreferredSize(new Dimension(150, 25)); 

        JButton searchButton = new JButton("Search");
        searchButton.setPreferredSize(new Dimension(80, 15)); 
        searchButton.setBackground(Color.GRAY);
        searchButton.setForeground(Color.white);


        JButton browseButton = new JButton("Browse");
        browseButton.setPreferredSize(new Dimension(80, 15)); 
        browseButton.setBackground(Color.GRAY);
        browseButton.setForeground(Color.white);

        inputPanel.add(new JLabel("Directory:"));
        inputPanel.add(directoryField);
        inputPanel.add(browseButton);
        inputPanel.add(new JLabel("File Name:"));
        inputPanel.add(fileField);
        inputPanel.add(searchButton);
        inputPanel.setBackground(Color.LIGHT_GRAY);

        JPanel resultPanel = new JPanel();
        JTextArea resultTextArea = new JTextArea(15, 50);
        resultTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultTextArea);
        resultPanel.add(scrollPane);
        resultPanel.setBackground(Color.PINK); 

        contentPane.add(titlePanel, BorderLayout.NORTH);
        contentPane.add(inputPanel, BorderLayout.CENTER);
        contentPane.add(resultPanel, BorderLayout.SOUTH);

        // Action Listeners
        searchButton.addActionListener(e -> searchAction(directoryField, fileField, resultTextArea));
        browseButton.addActionListener(e -> browseAction(directoryField, resultTextArea, frame));

        // Add mouse listener to resultTextArea
        resultTextArea.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getClickCount() == 2) {
                    JTextArea textArea = (JTextArea) e.getSource();
                    int offset = textArea.viewToModel2D(e.getPoint());
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

   
}