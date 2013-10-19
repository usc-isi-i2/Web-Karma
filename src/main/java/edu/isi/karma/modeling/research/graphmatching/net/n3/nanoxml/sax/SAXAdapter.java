/* SAXAdapter.java                                                  NanoXML/SAX
 *
 * $Revision: 1.4 $
 * $Date: 2002/01/04 21:03:28 $
 * $Name: RELEASE_2_2_1 $
 *
 * This file is part of the SAX adapter for NanoXML 2 for Java.
 * Copyright (C) 2000-2002 Marc De Scheemaecker, All Rights Reserved.
 *
 * This software is provided 'as-is', without any express or implied warranty.
 * In no event will the authors be held liable for any damages arising from the
 * use of this software.
 *
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 *
 *  1. The origin of this software must not be misrepresented; you must not
 *     claim that you wrote the original software. If you use this software in
 *     a product, an acknowledgment in the product documentation would be
 *     appreciated but is not required.
 *
 *  2. Altered source versions must be plainly marked as such, and must not be
 *     misrepresented as being the original software.
 *
 *  3. This notice may not be removed or altered from any source distribution.
 */

package edu.isi.karma.modeling.research.graphmatching.net.n3.nanoxml.sax;


import java.io.Reader;

import org.xml.sax.DocumentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.HandlerBase;
import org.xml.sax.helpers.AttributeListImpl;
import org.xml.sax.helpers.LocatorImpl;

import edu.isi.karma.modeling.research.graphmatching.net.n3.nanoxml.IXMLBuilder;
import edu.isi.karma.modeling.research.graphmatching.net.n3.nanoxml.IXMLReader;


/**
 * @author   kriesen
 */
public class SAXAdapter
   implements IXMLBuilder
{

   /**
    * The NanoXML reader.
    */
   private IXMLReader nanoxmlReader;


   /**
    * The SAX document handler.
    */
   private DocumentHandler saxDocumentHandler;


   /**
    * The SAX error handler.
    */
   private ErrorHandler saxErrorHandler;


   /**
    * The SAX locator.
    */
   private LocatorImpl saxLocator;


   /**
    * The SAX attribute list.
    */
   private AttributeListImpl saxAttributes;


   /**
    * Creates the adapter.
    */
   public SAXAdapter()
   {
      this.saxDocumentHandler = new HandlerBase();
      this.saxLocator = new LocatorImpl();
      this.saxLocator.setColumnNumber(-1);
   }


   /**
    * Cleans up the object when it's destroyed.
    */
   protected void finalize()
      throws Throwable
   {
      this.nanoxmlReader = null;
      this.saxDocumentHandler = null;
      this.saxErrorHandler = null;
      this.saxLocator = null;
      this.saxAttributes = null;
      super.finalize();
   }


   /**
    * Sets the document handler.
    *
    * @param handler the document handler
    */
   public void setDocumentHandler(DocumentHandler handler)
   {
      this.saxDocumentHandler = handler;
   }


   /**
    * Sets the reader.
    *
    * @param reader the reader.
    */
   public void setReader(IXMLReader reader)
   {
      this.nanoxmlReader = reader;
   }


   /**
    * This method is called before the parser starts processing its input.
    *
    * @param systemID the system ID of the data source
    * @param lineNr the line on which the parsing starts
    */
   public void startBuilding(String systemID,
                             int    lineNr)
      throws Exception
   {
      this.saxLocator.setLineNumber(lineNr);
      this.saxLocator.setSystemId(systemID);
      this.saxDocumentHandler.setDocumentLocator(this.saxLocator);
      this.saxDocumentHandler.startDocument();
   }


   /**
    * This method is called when a processing instruction is encountered.
    * PIs with target "xml" are handled by the parser.
    *
    * @param target the PI target
    * @param reader to read the data from the PI
    */
   public void newProcessingInstruction(String target,
                                        Reader reader)
      throws Exception
   {
      StringBuffer data = new StringBuffer();
      char[] chars = new char[1024];
      int charsRead = reader.read(chars);

      while (charsRead > 0) {
         data.append(chars, 0, charsRead);
         charsRead = reader.read(chars);
      }

      this.saxDocumentHandler.processingInstruction(target, data.toString());
   }


   /**
    * This method is called when a new XML element is encountered.
    *
    * @see #endElement
    *
    * @param name the name of the element
    * @param nsPrefix the prefix used to identify the namespace
    * @param nsSystemId the system ID associated with the namespace
    * @param systemID the system ID of the data source
    * @param lineNr the line in the source where the element starts
    */
   public void startElement(String name,
                            String nsPrefix,
                            String nsSystemId,
                            String systemID,
                            int    lineNr)
      throws Exception
   {
      if (nsPrefix != null) {
         name = nsPrefix + ':' + name;
      }

      this.saxLocator.setLineNumber(lineNr);
      this.saxLocator.setSystemId(systemID);
      this.saxAttributes = new AttributeListImpl();
   }


   /**
    * This method is called when the attributes of an XML element have been
    * processed.
    *
    * @see #startElement
    * @see #addAttribute
    *
    * @param name the name of the element
    * @param nsPrefix the prefix used to identify the namespace
    * @param nsSystemId the system ID associated with the namespace
    */
   public void elementAttributesProcessed(String name,
                                          String nsPrefix,
                                          String nsSystemId)
      throws Exception
   {
      if (nsPrefix != null) {
         name = nsPrefix + ':' + name;
      }

      this.saxDocumentHandler.startElement(name, this.saxAttributes);
   }


   /**
    * This method is called when the end of an XML elemnt is encountered.
    *
    * @see #startElement
    *
    * @param name the name of the element
    * @param nsPrefix the prefix used to identify the namespace
    * @param nsSystemId the system ID associated with the namespace
    */
   public void endElement(String name,
                          String nsPrefix,
                          String nsSystemId)
      throws Exception
   {
      if (nsPrefix != null) {
         name = nsPrefix + ':' + name;
      }

      this.saxDocumentHandler.endElement(name);
   }


   /**
    * This method is called when a new attribute of an XML element is
    * encountered.
    *
    * @param key the key (name) of the attribute
    * @param nsPrefix the prefix used to identify the namespace
    * @param nsSystemId the system ID associated with the namespace
    * @param value the value of the attribute
    * @param type the type of the attribute ("CDATA" if unknown)
    */
   public void addAttribute(String key,
                            String nsPrefix,
                            String nsSystemId,
                            String value,
                            String type)
      throws Exception
   {
      if (nsPrefix != null) {
         key = nsPrefix + ':' + key;
      }

      this.saxAttributes.addAttribute(key, type, value);
   }


   /**
    * This method is called when a PCDATA element is encountered. A Java
    * reader is supplied from which you can read the data. The reader will
    * only read the data of the element. You don't need to check for
    * boundaries. If you don't read the full element, the rest of the data
    * is skipped. You also don't have to care about entities; they are
    * resolved by the parser.
    *
    * @param reader the Java reader from which you can retrieve the data
    * @param systemID the system ID of the data source
    * @param lineNr the line in the source where the element starts
    *
    * @throws java.io.IOException
    *		when the reader throws such exception
    */
   public void addPCData(Reader reader,
                         String systemID,
                         int    lineNr)
      throws Exception
   {
      this.saxLocator.setLineNumber(lineNr);
      this.saxLocator.setSystemId(systemID);
      char[] chars = new char[2048];
      int charsRead = reader.read(chars);

      while (charsRead > 0) {
         this.saxDocumentHandler.characters(chars, 0, charsRead);
         charsRead = reader.read(chars);
      }
   }


   /**
    * Returns the result of the building process. This method is called just
    * before the parse() method of IXMLParser returns.
    *
    * @see net.n3.nanoxml.IXMLParser#parse
    *
    * @return the result of the building process.
    */
   public Object getResult()
      throws Exception
   {
      return null;
   }


   /**
    * Indicates that parsing has been completed.
    */
   public void endDocument()
      throws Exception
   {
      this.saxDocumentHandler.endDocument();
   }

}
