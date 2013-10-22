/**
 * *****************************************************************************
 * Copyright 2012 University of Southern California
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 * This code was developed by the Information Integration Group as part of the
 * Karma project at the Information Sciences Institute of the University of
 * Southern California. For more information, publications, and related
 * projects, please see: http://www.isi.edu/integration
 *****************************************************************************
 */
package edu.isi.karma.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class FileUtil {

    private static String DESTINATION_DIR_PATH = ServletContextParameterMap.getParameterValue(ContextParameter.USER_DIRECTORY_PATH)
            + "UserUploadedFiles/";
    private static Logger logger = LoggerFactory.getLogger(FileUtil.class);

    static public File downloadFileFromHTTPRequest(HttpServletRequest request) {
        // Download the file to the upload file folder
        File destinationDir = new File(DESTINATION_DIR_PATH);
        logger.debug("File upload destination directory: " + destinationDir.getAbsolutePath());
        if (!destinationDir.isDirectory()) {
            destinationDir.mkdir();
        }

        DiskFileItemFactory fileItemFactory = new DiskFileItemFactory();

        // Set the size threshold, above which content will be stored on disk.
        fileItemFactory.setSizeThreshold(1 * 1024 * 1024); //1 MB

        //Set the temporary directory to store the uploaded files of size above threshold.
        fileItemFactory.setRepository(destinationDir);

        ServletFileUpload uploadHandler = new ServletFileUpload(fileItemFactory);

        File uploadedFile = null;
        try {
            // Parse the request
            @SuppressWarnings("rawtypes")
            List items = uploadHandler.parseRequest(request);
            @SuppressWarnings("rawtypes")
            Iterator itr = items.iterator();
            while (itr.hasNext()) {
                FileItem item = (FileItem) itr.next();

                // Ignore Form Fields.
                if (item.isFormField()) {
                    // Do nothing
                } else {
                    //Handle Uploaded files. Write file to the ultimate location.
                    uploadedFile = new File(destinationDir, item.getName());
                    item.write(uploadedFile);
                }
            }
        } catch (FileUploadException ex) {
            logger.error("Error encountered while parsing the request", ex);
        } catch (Exception ex) {
            logger.error("Error encountered while uploading file", ex);
        }
        return uploadedFile;
    }

    public static void copyFiles(File destination, File source) throws FileNotFoundException, IOException {
        if (!destination.exists()) {
            destination.createNewFile();
        }
        InputStream in = new FileInputStream(source);
        OutputStream out = new FileOutputStream(destination);

        byte[] buf = new byte[1024];
        int len;

        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
        logger.debug("Done copying contents of " + source.getName() + " to " + destination.getName());
    }

    public static void writePrettyPrintedJSONObjectToFile(JSONObject json, File jsonFile)
            throws JSONException, IOException {
        String prettyPrintedJSONString = json.toString(4);
        FileWriter writer = new FileWriter(jsonFile);
        writer.write(prettyPrintedJSONString);
        writer.close();
        logger.debug("Done writing JSON Object into a File: " + jsonFile.getAbsolutePath());
    }

    public static String readFileContentsToString(File file) throws IOException {
//        FileInputStream stream = new FileInputStream(file);
//        try {
//            FileChannel fc = stream.getChannel();
//            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
//
//            //MVS: Detect encoding of textfile dynamically
//            String encoding = EncodingDetector.detect(stream);
//
//            if (encoding != null) {
//                return Charset.forName(encoding).decode(bb).toString();
//            }
//            /* Instead of using default, pass in a decoder. */
//            return Charset.defaultCharset().decode(bb).toString();
//        } finally {
//            stream.close();
//        }
        return EncodingDetector.getString(file);
    }

    /**
     * Saves a string to a file.
     *
     * @param str
     * @param fileName
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     */
    public static void writeStringToFile(String str, String fileName) throws UnsupportedEncodingException, FileNotFoundException {
        OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(fileName), "UTF-8");
        BufferedWriter bw = new BufferedWriter(fw);
        PrintWriter outWriter = new PrintWriter(bw);
        outWriter.println(str);
        outWriter.close();
    }
    /* does not write UTF8
     public static void writeStringToFile(String string, String fileName) throws IOException {
     BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
     writer.write(string);
     writer.flush();
     writer.close();
     }
     */
}
