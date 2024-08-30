package rae.wc.wcontroller;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class WeightPersister {

    private static final String FILE_NAME = "weight.csv";
    private List<Pair<String, Float>> weightList = new ArrayList<>();

    public void saveWeight(String formattedDate, String weight){

        String[] data = {formattedDate, weight};

        try {
            appendToCSV(data);
            System.out.println("CSV file saved successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void appendToCSV(String[] data) throws IOException {

        String userHome = System.getProperty("user.home");
        Path filePath = Paths.get(userHome, FILE_NAME);
        boolean firstWeight=false;

        if (!Files.exists(filePath)) {
            Files.createFile(filePath);
            firstWeight=true;
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile(), true))) {
            if (firstWeight) {
                String[] headers = {"Date", "Weight"};
                writer.write(String.join(",", headers));
                writer.newLine();
            }
            writer.write(String.join(",", data));
            writer.newLine();
        }
    }

    public void loadCsvToList(){
        String line;
        String userHome = System.getProperty("user.home");
        Path filePath = Paths.get(userHome, FILE_NAME);
        String csvSplitBy = ",";
        if(Files.exists(filePath)) {
            try (BufferedReader br = new BufferedReader(new FileReader(filePath.toFile()))) {
                br.readLine();
                while ((line = br.readLine()) != null) {
                    String[] data = line.split(csvSplitBy);
                    String name = data[0];
                    Float value = Float.parseFloat(data[1]);
                    weightList.add(new Pair<>(name, value));
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public List<Pair<String, Float>> getWholeSeries(){
        return weightList;
    }

    public List<Pair<String, Float>> getLastWeek(){
       return weightList.stream()
                .skip(Math.max(0, weightList.size() - 7))
                .collect(Collectors.toList());
    }

    public List<Pair<String, Float>> getLastMonth(){
        return weightList.stream()
                .skip(Math.max(0, weightList.size() - 30))
                .collect(Collectors.toList());
    }

    public List<Pair<String, Float>> getLastYear(){
        return weightList.stream()
                .skip(Math.max(0, weightList.size() - 365))
                .collect(Collectors.toList());
    }
}


