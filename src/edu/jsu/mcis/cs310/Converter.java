package edu.jsu.mcis.cs310;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;

public class Converter {

    @SuppressWarnings("unchecked")
    public static String csvToJson(String csvString) {

        try (CSVReader reader = new CSVReaderBuilder(new StringReader(csvString)).build()) {
            List<String[]> rows = reader.readAll();

            JsonObject root = new JsonObject();
            if (rows.isEmpty()) return root.toJson().trim();


            String[] header = rows.get(0);
            JsonArray colHeadings = new JsonArray();
            for (String h : header) colHeadings.add(h);

            JsonArray prodNums = new JsonArray();
            JsonArray data = new JsonArray();

            for (int r = 1; r < rows.size(); r++) {
                String[] line = rows.get(r);
                if (line == null || line.length == 0) continue;


                prodNums.add(line[0]);


                JsonArray row = new JsonArray();
                for (int c = 1; c < header.length; c++) {
                    String heading = header[c];
                    String val = (c < line.length) ? line[c] : "";

                    if (("Season".equals(heading) || "Episode".equals(heading)) && !val.isEmpty()) {
                        row.add(Integer.parseInt(val.trim()));
                    } else {
                        row.add(val);
                    }
                }
                data.add(row);
            }

            root.put("ProdNums",    prodNums);
            root.put("ColHeadings", colHeadings);
            root.put("Data",        data);

            return root.toJson().trim();

        } catch (Exception e) {
            e.printStackTrace();
            return "{}";
        }
    }

    @SuppressWarnings("unchecked")
    public static String jsonToCsv(String jsonString) {

        try {
            JsonObject root = (JsonObject) Jsoner.deserialize(jsonString);

            JsonArray prodNums    = (JsonArray) root.get("ProdNums");
            JsonArray colHeadings = (JsonArray) root.get("ColHeadings");
            JsonArray data        = (JsonArray) root.get("Data");

            StringWriter sw = new StringWriter();

            try (ICSVWriter writer = new CSVWriterBuilder(sw).withLineEnd("\n").build()) {


                String[] header = new String[colHeadings.size()];
                for (int i = 0; i < colHeadings.size(); i++) {
                    header[i] = String.valueOf(colHeadings.get(i));
                }
                writer.writeNext(header, true); 


                for (int r = 0; r < data.size(); r++) {
                    JsonArray rowJson = (JsonArray) data.get(r);
                    String[] row = new String[header.length];


                    row[0] = String.valueOf(prodNums.get(r));


                    for (int c = 1; c < header.length; c++) {
                        String heading = header[c];
                        Object cell = rowJson.get(c - 1);

                        if ("Episode".equals(heading) && (cell instanceof Number)) {
                            row[c] = String.format("%02d", ((Number) cell).intValue()); 
                        } else {
                            row[c] = String.valueOf(cell);
                        }
                    }

                    writer.writeNext(row, true);
                }
            }


            String out = sw.toString();
            if (out.endsWith("\n")) out = out.substring(0, out.length() - 1);
            return out;

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
