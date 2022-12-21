package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException {
        String[] columnMapping = {"id", "firstName", "lastName", "country", "age"};
        String fileName = "data.csv";
        List<Employee> list = parseCSV(columnMapping, fileName);
        String json = listToJson(list);
        System.out.println(json);
        writeString("data.json", json);

        List<Employee> list2 = parseXML("data.xml");
        String json2 = listToJson(list2);
        System.out.println(json2);
        writeString("data2.json", json2);
    }

    private static List<Employee> parseXML(String fileName) throws IOException, SAXException, ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new File(fileName));

        Node root = doc.getDocumentElement();
        System.out.println("Корневой элемент: " + root.getNodeName());

        return read(root);
    }

    public static List<Employee> parseCSV(String[] columnMapping, String fileName) {
        try (CSVReader csvReader = new CSVReader(new FileReader(fileName))) {
            ColumnPositionMappingStrategy<Employee> strategy =
                    new ColumnPositionMappingStrategy<>();
            strategy.setType(Employee.class);
            strategy.setColumnMapping(columnMapping);

            CsvToBean<Employee> csv = new CsvToBeanBuilder<Employee>(csvReader)
                    .withMappingStrategy(strategy)
                    .build();

            List<Employee> data = csv.parse();
            return data;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String listToJson(List<Employee> list) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        Type listType = new TypeToken<List<Employee>>() {
        }.getType();
        return gson.toJson(list, listType);
    }

    public static void writeString(String fileName, String obj) {
        try (FileWriter file = new FileWriter(fileName)) {
            file.write(obj);
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Employee> read(Node node) {
        NodeList nodeList = node.getChildNodes();
        List<Employee> listEmployee = new ArrayList<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node_ = nodeList.item(i);
            if (Node.ELEMENT_NODE == node_.getNodeType()) {
                System.out.println("Текущий узел: " + node_.getNodeName());
                Element employee = (Element) node_;
                if (employee.getNodeName().equals("employee")) {
                    Employee newEmployee = new Employee();
                    newEmployee.setId(Long.parseLong(employee.getElementsByTagName("id").item(0).getTextContent()));
                    newEmployee.setAge(Integer.parseInt(employee.getElementsByTagName("age").item(0).getTextContent()));
                    newEmployee.setCountry(employee.getElementsByTagName("country").item(0).getTextContent());
                    newEmployee.setFirstName(employee.getElementsByTagName("firstName").item(0).getTextContent());
                    newEmployee.setLastName(employee.getElementsByTagName("lastName").item(0).getTextContent());
                    listEmployee.add(newEmployee);
                }
                listEmployee.addAll(read(node_));
            }
        }
        return listEmployee;
    }
}