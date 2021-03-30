package com.sivabalan.xmlparser;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.*;
import java.util.*;

public class XMLStringValuesComparer {

    private static final String STRING = "string";
    private static final String NAME = "name";

    private final XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
    private final LinkedHashMap<String, List<String>> file1StringsMap = new LinkedHashMap<>();
    private final LinkedHashMap<String, List<String>> file2StringsMap = new LinkedHashMap<>();

    static XMLStringValuesComparer newInstance() {
        return new XMLStringValuesComparer();
    }

    public static void main(String[] args) {
        XMLStringValuesComparer obj = newInstance();
        String url1 = obj.getUrl();
        String url2 = obj.getUrl();

        try {
            obj.init(url1, url2);
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
    }

    private String getUrl()   {
        System.out.print("Enter the url...");
        Scanner scan = new Scanner(System.in);
        return scan.next();
    }

    private void init(String url1, String url2) throws XMLStreamException {
        File stringFile1 = new File(url1);
        File stringFile2 = new File(url2);
        parse(stringFile1, file1StringsMap);
        parse(stringFile2, file2StringsMap);
        printDuplicateStringsInFile(file2StringsMap);
    }

    private void parse(File file, LinkedHashMap<String, List<String>> stringsInFile) throws XMLStreamException {
        try {
            InputStream stream = new FileInputStream(file);
            XMLEventReader reader = xmlInputFactory.createXMLEventReader(stream);
            while(reader.hasNext()) {
                XMLEvent event = reader.nextEvent();
                if(event.isStartElement() && event.asStartElement().getName().getLocalPart().equals(STRING))    {
                    try {
                        String key = event.asStartElement().getAttributeByName(new QName(NAME)).getValue();
                        String value = reader.getElementText();
                        ArrayList<String> keysList = new ArrayList<>();
                        List<String> tempList = stringsInFile.get(value);
                        if(tempList != null) {
                            keysList.addAll(tempList);
                        }
                        keysList.add(key);
                        stringsInFile.put(value, keysList);
                    } catch (XMLStreamException e) {
                        // we are ignoring this exception (getElementText expects string but was START_ELEMENT)
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println(file.toString() + " file not found.");
            e.printStackTrace();
        }
    }

    /**
     * Prints the output in a file with output of the form "string_value -> duplicate_keys_list"
     * @param stringsMap map containing the parsed xml file
     */
    private void printDuplicateStringsInFile(Map<String, List<String>> stringsMap) {
        StringBuilder output = new StringBuilder();

        for(Map.Entry<String, List<String>> entry : stringsMap.entrySet()) {
            List<String> duplicateKeysList = entry.getValue();
            if(duplicateKeysList == null || duplicateKeysList.size() < 2) {
                continue;
            }
            output.append(entry.getKey()).append(" -> ").append(joinToString(duplicateKeysList)).append("\n");
        }
        File outputFile = new File("output.txt");
        try(FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(output.toString().getBytes());
            System.out.println("Output written to file..." + outputFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void printOutput()  {
        StringBuilder output = new StringBuilder();
        long totalCommonStrings = 0;

        output.append("\tValue").append("\t\t|\t\t").append("Key (File 1)").append("\t\t|\t\t").append("Key (File 2)");
        output.append("\n");

        for(Map.Entry<String, List<String>> entry : file1StringsMap.entrySet()) {
            if(file2StringsMap.containsKey(entry.getKey())) {
                String string = entry.getKey();
                totalCommonStrings += entry.getValue().size();
                output.append(string);
                output.append("\t").append("|");
                output.append("\t").append(joinToString(entry.getValue())).append("\t|");
                output.append("\t").append(joinToString(file2StringsMap.get(string)));
                output.append("\n");
            }
        }
        output.append("\n");
        output.append("Total common strings = ").append(totalCommonStrings).append("\n");

        File outputFile = new File("output.txt");
        try(FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(output.toString().getBytes());
            System.out.println("Output written to file..." + outputFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String joinToString(List<String> list) {
        if(list == null || list.isEmpty()) {
            return "";
        }
        String string = list.toString();
        return string.substring(1, string.length() - 1);
    }
}
