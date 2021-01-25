package edu.isi.karma.semanticlabeling.app;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import edu.isi.karma.semanticlabeling.dsl.FeatureExtractor;
import edu.isi.karma.semanticlabeling.dsl.ColumnBasedTable;
import edu.isi.karma.semanticlabeling.dsl.Column;
import edu.isi.karma.semanticlabeling.dsl.ColumnType;
import edu.isi.karma.semanticlabeling.dsl.ColumnData;
import edu.isi.karma.semanticlabeling.dsl.SemType;

/**
 * This class creates objects from csv file data.
 * @author rutujarane
 * 
*/

public class CreateDSLObjects {

    static Logger logger = LogManager.getLogger(CreateDSLObjects.class.getName());
    // Reads the file and creates matrix object.
    public static String[][] readFile(String fileName){
        List<String[]> rowList = new ArrayList<String[]>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                // logger.info("Line:"+line);
                String[] lineItems = line.split(",",-1);
                rowList.add(lineItems);
            }
            br.close();

            String[][] matrix = new String[rowList.size()][];
            for (int i = 0; i < rowList.size(); i++) {
                String[] row = rowList.get(i);
                matrix[i] = row;
            }
            return matrix;
        }
        catch(Exception e){
            // Handle any I/O problems
            logger.info("ERROR: File not readable");
        }
        String[][] matrix = new String[0][0];
        return matrix;
    }

    // Deletes the file.
    public static void deleteFile(String fileName){
        try
        { 
            Files.deleteIfExists(Paths.get(fileName)); 
        } 
        catch(NoSuchFileException e) 
        { 
            logger.info("No such file/directory exists"); 
        } 
        catch(DirectoryNotEmptyException e) 
        { 
            logger.info("Directory is not empty."); 
        } 
        catch(IOException e) 
        { 
            logger.info("Invalid permissions."); 
        }
        logger.info("Deletion successful."); 
    }

    // Creates the FeatureExtractor Object from data in file.
    public static FeatureExtractor create_feature_extractor(String files[]) throws IOException{
        List<ColumnBasedTable> columnBasedTableObj = new ArrayList<ColumnBasedTable>();

        for(String file: files){
            String [][] data = readFile(file);
            logger.info("File gen:"+file);
            if(data.length == 0){
                logger.info("Warning: file not readable "+file);
                continue;
            }
            logger.info("Read the file"+file);
            columnBasedTableObj.add(findDatatype(data,file));
        }
        return new FeatureExtractor(columnBasedTableObj);

    }

    // To find the datatype of the column.
    public static ColumnBasedTable findDatatype(String[][] data, String tableName){
        logger.info("TableName:"+tableName);
        List<Column> columns = new ArrayList<Column>();
        for(int index=0; index<data[0].length; index++){
            List<String> colData = getColumnData(data,index);
            SemType semTypeObj = findSemType(colData.get(1));
            Hashtable<String, Float> typeStats = new Hashtable<String, Float>();
            Column columnObj = new Column(tableName, colData.get(0), semTypeObj, colData.get(2), data.length, typeStats);
            List<String> colSubList = new ArrayList<String>(colData.subList(2,colData.size())); //3
            columnObj.value = new ColumnData(colSubList);
            columns.add(columnObj);
            logger.info("Column Object created");
        }
        ColumnBasedTable columnBasedTableObj = new ColumnBasedTable(tableName, columns);
        return columnBasedTableObj;
    }

    // Returns the semantic type of the column.
    public static SemType findSemType(String colName){
        String col[] = colName.trim().replaceAll("\"","").split("-");
        logger.info("SemType: 1:"+col[0]+" 2: "+col[col.length-1]);
        SemType semTypeObj = new SemType(col[0],col[col.length-1]);
        return semTypeObj;
    }

    // Returns the data in the column in list form.
    public static List<String> getColumnData(String[][] data, int index){
        List<String> column = new ArrayList<String>();
        for(int i=0; i<data.length; i++){
            column.add(data[i][index].trim().replaceAll("\"",""));
        }
        return column;
    }
}