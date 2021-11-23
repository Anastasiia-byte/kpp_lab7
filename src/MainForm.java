import com.uni.lab7.DirectoryManager;
import com.uni.lab7.monitor.ThreadMonitor;

import javax.swing.*;
import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainForm {
    private JTextArea textArea;
    private JPanel panel1;
    private JButton runButton;
    private JSlider threadCountSlider;
    private JLabel threadCountLabel;
    private JRadioButton useExecutorServiceRadioButton;
    private JRadioButton useJavaLangThreadRadioButton;
    private JScrollPane scrollPane;
    private DirectoryManager directoryManager;


    public static void main(String[] args) {
        JFrame frame = new JFrame("Laba7");
        MainForm form = new MainForm();
        frame.setContentPane(form.panel1);
        ButtonGroup bg = new ButtonGroup();
        bg.add(form.useExecutorServiceRadioButton);
        bg.add(form.useJavaLangThreadRadioButton);
        form.threadCountSlider.setPaintLabels(true);
        form.threadCountSlider.setPaintTicks(true);
        form.threadCountSlider.setMajorTickSpacing(3);
        form.threadCountSlider.setMinorTickSpacing(1);
        form.scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        form.scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setSize(frame.getWidth(), 500);

        form.threadCountSlider.addChangeListener(e -> {
            JSlider slider = (JSlider) e.getSource();
            form.threadCountLabel.setText(slider.getValue() + "");
        });

        form.runButton.addActionListener(e -> {
            form.directoryManager = DirectoryManager.builder().build();
            List<File> files = form.directoryManager.filterFiles();;

            int threadCount = form.threadCountSlider.getValue();
            List<Thread> threads = new ArrayList<>();
            for (int i = 0; i < threadCount; ++i) {
                threads.add(new Thread(() -> form.directoryManager.search(files)));
            }

            Instant startTime = Instant.now();
            ThreadMonitor monitor = new ThreadMonitor(threads);
            monitor.startDisplaying(form.textArea, 10);
            if (form.useExecutorServiceRadioButton.isSelected()) {
                ExecutorService executor = Executors.newFixedThreadPool(threads.size());
                for (var thread: threads) {
                    executor.submit(thread);
                }
            } else {
                for (var thread: threads) {
                    thread.start();
                }
            }

            var timer = new java.util.Timer();

            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    if (form.directoryManager.isFinished()) {
                        timer.cancel();
                        monitor.stopDisplaying();
                        Instant endTime = Instant.now();
                        long timeElapsed = Duration.between(startTime, endTime).toMillis();

                        JOptionPane.showMessageDialog(frame, "Directory inspection took " +
                                timeElapsed + "ms");
                    }
                }
            };
            timer.schedule(task, 0, 1);
        });

    }
}
