package com.dscjss.taskexecutor.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Shell {

    public static void exec(String... command) throws IOException {
        ProcessBuilder b = new ProcessBuilder(command);
        try {
            Process process = b.start();
            process.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line = null;
            while ( (line = reader.readLine()) != null) {
                builder.append(line);
                builder.append(System.getProperty("line.separator"));
            }
            String result = builder.toString();
            System.out.println(result);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }
}
