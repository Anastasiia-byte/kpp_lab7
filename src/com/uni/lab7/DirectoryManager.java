package com.uni.lab7;

import lombok.Builder;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Builder
public class DirectoryManager {
    private final String directoryName = "E:\\5семестр";
    private String fileFilter;
    private @Builder.Default int minSize = 0;
    private List<File> files;

    public List<File> filterFiles(){
        File directory = new File(directoryName);
        System.out.println("Looking for files in directory: " + directoryName);
        if (!directory.exists()) {
            System.out.printf("Directory %s does not exist%n", directoryName);
            return null;
        }
        if (!directory.isDirectory()) {
            System.out.printf("Provided value %s is not a directory%n", directoryName);
            return null;
        }
        System.out.printf("files found in dir: %s%n", Arrays.asList(Objects.requireNonNull(directory.listFiles())));
        FileFilter logFileFilter = null;
        if(fileFilter != null)
            logFileFilter = file -> file.getName().endsWith(fileFilter) && file.length() >= minSize;

        files = new ArrayList<>(List.of(Objects.requireNonNull(directory.listFiles(logFileFilter))));
        return files;
    }

    public boolean isFinished(){
        synchronized (files){
            return files.isEmpty();
        }
    }

    public void search(List<File> files){
        while (true){
            File item = null;
            synchronized (files){
                if (files.size() != 0)
                    item = files.remove(0);
                files.notifyAll();
            }

            if(item != null){
                if(item.isDirectory()){
                    System.out.println(Thread.currentThread().getId() + ": " + item + " - directory");
                    List<File> nextFiles = new ArrayList<>(List.of(Objects.requireNonNull(item.listFiles())));
                    search(nextFiles);
                }else{
                    System.out.println(Thread.currentThread().getId() + ": " + item + " - file");
                }
            } else{
                break;
            }
        }

    }

}
