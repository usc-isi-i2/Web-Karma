package edu.isi.karma.semanticlabeling.app;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

// import com.mycompany.dsl.DSL;
import edu.isi.karma.semanticlabeling.dsl.FeatureExtractor;
import edu.isi.karma.semanticlabeling.dsl.ColumnBasedTable;
import edu.isi.karma.semanticlabeling.dsl.Column;
import edu.isi.karma.semanticlabeling.dsl.ColumnData;
import edu.isi.karma.semanticlabeling.dsl.SemType;

/**
 * This class creates objects from csv file data.
 *
 * @author rutujarane , Bidisha Das Baksi (bidisha.bksh@gmail.com)
 */

public class CreateDSLObjects {

    static Logger logger = LogManager.getLogger(CreateDSLObjects.class.getName());
    public static HashMap<String, SemType> sem_col;

    public static String[][] readFile(String fileName) {
        List<String[]> rowList = new ArrayList<String[]>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] lineItems = line.split(",", -1);
                rowList.add(lineItems);
            }
            br.close();

            String[][] matrix = new String[rowList.size()][];
            for (int i = 0; i < rowList.size(); i++) {
                String[] row = rowList.get(i);
                matrix[i] = row;
            }
            return matrix;
        } catch (Exception e) {
            logger.info("ERROR: File not readable");
        }
        String[][] matrix = new String[0][0];
        return matrix;
    }

    public static void deleteFile(File file) {
        try {
            Files.deleteIfExists(Paths.get(file.getAbsolutePath()));
        } catch (NoSuchFileException e) {
            logger.info("No such file/directory exists");
        } catch (DirectoryNotEmptyException e) {
            logger.info("Directory is not empty.");
        } catch (IOException e) {
            logger.info("Invalid permissions.");
        }
        logger.info("Deletion successful.");
    }

    public static FeatureExtractor create_feature_extractor(String[] files) throws IOException {
        List<ColumnBasedTable> columnBasedTableObj = new ArrayList<ColumnBasedTable>();

        int kk = 0;
        for (String file : files) {
            String[][] data = readFile(file);
            System.out.println("File gen:" + file);
            if (data.length == 0) {
                logger.info("Warning: file not readable " + file);
                continue;
            }
            logger.info("Read the file" + file);
            columnBasedTableObj.add(findDatatype(data, file));
            kk++;
        }
        return new FeatureExtractor(columnBasedTableObj);

    }

    public static FeatureExtractor create_feature_extractor(HashMap<String, String[][]> dataMap) throws IOException {
        List<ColumnBasedTable> columnBasedTableObj = new ArrayList<ColumnBasedTable>();
        for (Map.Entry<String, String[][]> entry : dataMap.entrySet()) {
            String data[][] = entry.getValue();
            columnBasedTableObj.add(findDatatype(data, entry.getKey())); // Assuming tf idf is computed at token level and each cell value is not a whole token
        }
        return new FeatureExtractor(columnBasedTableObj);

    }


    public static ColumnBasedTable findDatatype(String[][] data, String tableName) {
        logger.info("TabName:" + tableName);
        List<Column> columns = new ArrayList<Column>();
        for (int index = 0; index < data[0].length; index++) {
            List<String> colData = getColumnData(data, index);
            SemType semTypeObj;
            if (sem_col.containsKey(colData.get(0)))
                semTypeObj = sem_col.get(colData.get(0));
            else
                semTypeObj = findSemType(colData.get(1));
            Hashtable<String, Float> typeStats = new Hashtable<String, Float>();
            Column columnObj = new Column(tableName, colData.get(0), semTypeObj, colData.get(2), data.length, typeStats);
            List<String> colSubList = new ArrayList<String>(colData.subList(1, colData.size())); //3
            columnObj.value = new ColumnData(colSubList);
            columns.add(columnObj);
            logger.info("Column Object created");
        }
        ColumnBasedTable columnBasedTableObj = new ColumnBasedTable(tableName, columns);
        return columnBasedTableObj;
    }

    public static SemType findSemType(String colName) {
        String col[] = colName.trim().replaceAll("\"", "").split("-");
        SemType semTypeObj = new SemType(col[0], col[0]);
        return semTypeObj;
    }

    public static List<String> getColumnData(String[][] data, int index) {
        List<String> column = new ArrayList<String>();
        for (int i = 0; i < data.length; i++) {
            column.add(data[i][index].trim().replaceAll("\"", ""));
        }
        return column;
    }
}