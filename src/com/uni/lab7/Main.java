package com.uni.lab7;

import com.uni.lab7.monitor.ThreadMonitor;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    public static void main(String[] args) {
        DirectoryManager directoryManager;

        Scanner scanner = new Scanner(System.in);

        System.out.println("Would you like to filter the files? (Y/N)");
        String answ = scanner.nextLine();
        if(answ.equalsIgnoreCase("Y")){
            System.out.println("Enter min size of files: ");
            int minSize = scanner.nextInt();
            scanner.nextLine();
            System.out.println("Enter the filter: (for example .pdf)");
            String filter = scanner.nextLine();
            directoryManager = DirectoryManager.builder()
                    .fileFilter(filter)
                    .minSize(minSize)
                    .build();
        } else {
            directoryManager = DirectoryManager.builder().build();
        }

        List<File> files = directoryManager.filterFiles();

        System.out.print("Enter amount of threads: ");
        int threadCount = scanner.nextInt();
        scanner.nextLine();

        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < threadCount; ++i) {
            threads.add(new Thread(() -> directoryManager.search(files)));
        }
        ThreadMonitor monitor = new ThreadMonitor(threads);


        JFrame frame = new JFrame("Laba 7");
        JTextArea area = new JTextArea();
        JScrollPane pane = new JScrollPane(area);
        frame.add(pane);
        monitor.startDisplaying(area, 10);

        System.out.print("Use ExecutorService (Y/N)? ");
        String str = scanner.nextLine();
        long startTime = System.nanoTime();
        if (answ.equalsIgnoreCase("Y")) {
            ExecutorService executor = Executors.newFixedThreadPool(threads.size());
            for (var thread: threads) {
                executor.submit(thread);
            }
        } else {
            for (var thread: threads) {
                thread.start();
            }
        }


        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setSize(300, 500);


        var timer = new java.util.Timer();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (directoryManager.isFinished()) {
                    timer.cancel();
                    monitor.stopDisplaying();
                    long endTime = System.nanoTime();
                    double timeElapsed = (endTime - startTime) / 1E6;

                    System.out.println("Matrix solution took " + timeElapsed + "ms");

                }
            }
        };
        timer.schedule(task, 0, 1);
    }

}
