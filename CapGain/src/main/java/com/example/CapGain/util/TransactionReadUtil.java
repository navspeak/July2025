package com.example.CapGain.util;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Slf4j
@AllArgsConstructor
public class TransactionReadUtil {

    public static List<String> readFromString(String input){
        return new BufferedReader(new StringReader(input))
                .lines()
                .toList();
    }

    public static List<String> readFromConsole(){
        return new BufferedReader(new InputStreamReader(System.in))
                .lines()
                .takeWhile(line -> line != null && !line.isBlank())
                .toList();
    }

    public static List<String> readFromFile__() throws FileNotFoundException {
        File file = Path.of(".").resolve("input.txt").toFile();
        return new BufferedReader(new FileReader(file))
                .lines()
                .toList();
    }
    public static List<String> readFromFile()  {
        var path = Path.of(".").resolve("input.txt");
        try {
            return Files.readAllLines(path);
        } catch (IOException e) {
            // For a clean pipeline
            throw new UncheckedIOException("Exception occured while reading File", (IOException) e);
        }
    }
    public static boolean inputExist()  {
        var path = Path.of(".").resolve("input.txt");
        return Files.exists(path);
    }
}
