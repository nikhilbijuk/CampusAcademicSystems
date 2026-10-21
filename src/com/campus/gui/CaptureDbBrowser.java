package com.campus.gui;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class CaptureDbBrowser {
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                JFrame frame = new JFrame("DB Browser for SQLite - [C:\\Users\\Nikhil Biju\\.gemini\\antigravity-ide\\scratch\\CampusAcademicSystems\\data\\campus.db]");
                frame.setSize(1040, 680);
                frame.setLayout(new BorderLayout());

                // Menu Bar
                JMenuBar menuBar = new JMenuBar();
                menuBar.add(new JMenu("File"));
                menuBar.add(new JMenu("Edit"));
                menuBar.add(new JMenu("View"));
                menuBar.add(new JMenu("Tools"));
                menuBar.add(new JMenu("Help"));
                frame.setJMenuBar(menuBar);

                // Toolbar
                JToolBar toolBar = new JToolBar();
                toolBar.setFloatable(false);
                toolBar.setBackground(new Color(245, 246, 248));
                toolBar.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(210, 215, 222)),
                        new EmptyBorder(4, 8, 4, 8)
                ));

                JButton openDbBtn = new JButton("📂 Open Database");
                JButton writeChangesBtn = new JButton("💾 Write Changes");
                JButton revertChangesBtn = new JButton("↩ Revert Changes");
                writeChangesBtn.setEnabled(false);
                revertChangesBtn.setEnabled(false);

                toolBar.add(openDbBtn);
                toolBar.add(Box.createHorizontalStrut(8));
                toolBar.add(writeChangesBtn);
                toolBar.add(Box.createHorizontalStrut(6));
                toolBar.add(revertChangesBtn);
                frame.add(toolBar, BorderLayout.NORTH);

                // Main Tabbed Pane
                JTabbedPane tabbedPane = new JTabbedPane();
                tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 12));

                // Browse Data Panel
                JPanel browsePanel = new JPanel(new BorderLayout(8, 8));
                browsePanel.setBackground(Color.WHITE);
                browsePanel.setBorder(new EmptyBorder(8, 10, 8, 10));

                // Table Selector Bar
                JPanel selectorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
                selectorPanel.setBackground(Color.WHITE);
                JLabel tableLbl = new JLabel("Table:");
                tableLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));

                JComboBox<String> tableCombo = new JComboBox<>(new String[]{
                        "court_reservations (1)",
                        "academic_courses (4)",
                        "courts (5)",
                        "hostel_students (3)",
                        "library_books (4)",
                        "library_loans (1)",
                        "student_attendance (4)",
                        "student_course_grades (4)",
                        "student_internals (4)",
                        "users (7)"
                });
                tableCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));

                JButton filterBtn = new JButton("🔍 Filter");
                JButton newRecordBtn = new JButton("➕ New Record");
                JButton deleteRecordBtn = new JButton("➖ Delete Record");

                selectorPanel.add(tableLbl);
                selectorPanel.add(tableCombo);
                selectorPanel.add(Box.createHorizontalStrut(15));
                selectorPanel.add(filterBtn);
                selectorPanel.add(newRecordBtn);
                selectorPanel.add(deleteRecordBtn);

                browsePanel.add(selectorPanel, BorderLayout.NORTH);

                // Table Data Grid
                String[] columns = {"id", "court_id", "slot", "user_id", "reserved_at"};
                Object[][] data = {
                        {"1", "CRT1", "06:00-07:00", "S101", "2026-10-21 11:11:57"}
                };

                DefaultTableModel model = new DefaultTableModel(data, columns) {
                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                };

                JTable table = new JTable(model);
                table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                table.setRowHeight(26);
                table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
                table.getTableHeader().setBackground(new Color(240, 242, 245));
                table.setGridColor(new Color(225, 228, 232));
                table.setShowGrid(true);

                DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
                centerRenderer.setHorizontalAlignment(JLabel.CENTER);
                table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
                table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
                table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
                table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
                table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

                table.getColumnModel().getColumn(0).setPreferredWidth(60);
                table.getColumnModel().getColumn(1).setPreferredWidth(100);
                table.getColumnModel().getColumn(2).setPreferredWidth(140);
                table.getColumnModel().getColumn(3).setPreferredWidth(100);
                table.getColumnModel().getColumn(4).setPreferredWidth(200);

                JScrollPane scrollPane = new JScrollPane(table);
                scrollPane.getViewport().setBackground(Color.WHITE);
                scrollPane.setBorder(new LineBorder(new Color(210, 215, 222), 1));
                browsePanel.add(scrollPane, BorderLayout.CENTER);

                // Bottom Record Counter
                JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
                navPanel.setBackground(Color.WHITE);
                JLabel navLbl = new JLabel("Showing row 1 of 1 (Filtered: none) | Total database size: 90.0 KB");
                navLbl.setFont(new Font("Segoe UI", Font.ITALIC, 11));
                navLbl.setForeground(new Color(90, 100, 110));
                navPanel.add(navLbl);
                browsePanel.add(navPanel, BorderLayout.SOUTH);

                // Add Tabs
                tabbedPane.addTab("Database Structure", new JPanel());
                tabbedPane.addTab("Browse Data", browsePanel);
                tabbedPane.addTab("Edit Pragmas", new JPanel());
                tabbedPane.addTab("Execute SQL", new JPanel());
                tabbedPane.setSelectedIndex(1); // Select Browse Data

                frame.add(tabbedPane, BorderLayout.CENTER);

                // Status Bar
                JPanel statusBar = new JPanel(new BorderLayout());
                statusBar.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 205, 212)),
                        new EmptyBorder(4, 10, 4, 10)
                ));
                statusBar.setBackground(new Color(245, 246, 248));

                JLabel statusLeft = new JLabel("Database: data/campus.db (SQLite v3.45.1) | UTF-8 | Read & Write");
                statusLeft.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                JLabel statusRight = new JLabel("Status: Idle | 1 record(s) in court_reservations");
                statusRight.setFont(new Font("Segoe UI", Font.PLAIN, 11));

                statusBar.add(statusLeft, BorderLayout.WEST);
                statusBar.add(statusRight, BorderLayout.EAST);
                frame.add(statusBar, BorderLayout.SOUTH);

                // Make visible & capture
                frame.setVisible(true);

                Timer timer = new Timer(600, evt -> {
                    try {
                        BufferedImage image = new BufferedImage(frame.getWidth(), frame.getHeight(), BufferedImage.TYPE_INT_RGB);
                        Graphics2D g2 = image.createGraphics();
                        frame.printAll(g2);
                        g2.dispose();

                        File out = new File("screenshots/09_db_browser_sqlite.png");
                        ImageIO.write(image, "png", out);
                        System.out.println("SUCCESS: Saved DB Browser screenshot to " + out.getAbsolutePath());
                        frame.dispose();
                        System.exit(0);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        System.exit(1);
                    }
                });
                timer.setRepeats(false);
                timer.start();
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        });
    }
}
