package com.sivabalan.xmlparser;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class TranslationNeededStringsParser {

    private static final String STRING = "string";
    private static final String NAME = "name";

    private final XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
    private final ArrayList<File> stringFilesList = new ArrayList<>();
    private final HashMap<String, String> strings = new HashMap<>();
    private final HashMap<File, HashMap<String,String>> table = new HashMap<>();

    static TranslationNeededStringsParser newInstance() {
        return new TranslationNeededStringsParser();
    }

    public static void main(String[] args) {
        TranslationNeededStringsParser obj = newInstance();
        String url = (args != null && args.length >= 1) ? args[0] : obj.getUrl();
        obj.init(url);
    }

    private String getUrl()   {
        System.out.print("Enter the url...");
        Scanner scan = new Scanner(System.in);
        return scan.next();
    }

    private void init(String url) {
        fetchAllStringFiles(url);
        System.out.println(stringFilesList.size() + " string files found.");
        parseAllStringFiles();
        printOutput();
    }

    private void fetchAllStringFiles(String url)    {
        File dir = new File(url);
        File[] files = dir.listFiles();
        if(files == null) {
            System.out.println("No such url (or) no files found.");
            return;
        }
        for(File file : files)  {
            if(file.isFile())   {
                String fileName = file.getName();
                if(fileName.equals("strings.xml"))    {
                    stringFilesList.add(file);
                }
            } else  {
                fetchAllStringFiles(file.toString());
            }
        }
    }

    private void parseAllStringFiles()  {
        for (File file : stringFilesList) {
            try {
                parse(file);
            } catch (XMLStreamException e) {
                e.printStackTrace();
            }
        }
    }

    private void parse(File file) throws XMLStreamException {
        try {
            InputStream stream = new FileInputStream(file);
            XMLEventReader reader = xmlInputFactory.createXMLEventReader(stream);
            HashMap<String, String> stringsInFile = new HashMap<>();
            while(reader.hasNext()) {
                XMLEvent event = reader.nextEvent();
                if(event.isStartElement() && event.asStartElement().getName().getLocalPart().equals(STRING))    {
                    String key = event.asStartElement().getAttributeByName(new QName(NAME)).getValue();
                    System.out.println("File -> " + file.getAbsolutePath() + ", Key: " + key);
                    String value = reader.getElementText();
                    stringsInFile.put(key, value);
                    if(!strings.containsKey(key))    {
                        strings.put(key, value);
                    }
//                    System.out.println(key + " -> " + value);
                }
            }
            table.put(file, stringsInFile);
        } catch (FileNotFoundException e) {
            System.err.println(file.toString() + " file not found.");
            e.printStackTrace();
        }
    }

    private void printOutput()  {
        StringBuilder output = new StringBuilder("Total strings: " + strings.size() + "\n");
        for(Map.Entry<File, HashMap<String, String>> m : table.entrySet())  {
            File file = m.getKey();
            HashMap<String, String> stringsInFile = m.getValue();
            output.append("String count in ")
                    .append(file.getParentFile().getName())
                    .append(": ")
                    .append(stringsInFile.size())
                    .append("\t\t\t Required strings count: ")
                    .append(strings.size() - stringsInFile.size())
                    .append("\t\tRequired in ")
                    .append(file.getParentFile().getName())
                    .append("\n");
            for(Map.Entry<String, String> ss : strings.entrySet())    {
                if(!stringsInFile.containsKey(ss.getKey()))    {
                    output.append("<string name=\"")
                            .append(ss.getKey())
                            .append("\" >")
                            .append(ss.getValue())
                            .append("</string>\n");
                }
            }
            output.append("\n\n");
        }
        File outputFile = new File("output.txt");
        try(FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(output.toString().getBytes());
            System.out.println("Output written to file..." + outputFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
