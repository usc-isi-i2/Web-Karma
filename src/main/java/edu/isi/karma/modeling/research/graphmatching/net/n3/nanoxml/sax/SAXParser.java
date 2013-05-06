/* SAXParser.java                                                   NanoXML/SAX
 *
 * $Revision: 1.5 $
 * $Date: 2002/03/24 11:39:20 $
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


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

import org.xml.sax.DTDHandler;
import org.xml.sax.DocumentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.HandlerBase;
import org.xml.sax.InputSource;
import org.xml.sax.Parser;
import org.xml.sax.SAXException;

import edu.isi.karma.modeling.research.graphmatching.net.n3.nanoxml.IXMLParser;
import edu.isi.karma.modeling.research.graphmatching.net.n3.nanoxml.StdXMLReader;
import edu.isi.karma.modeling.research.graphmatching.net.n3.nanoxml.XMLParserFactory;


/**
 * @author   kriesen
 */
public class SAXParser
   implements Parser
{

   /**
    * The SAX adapter.
    */
   private SAXAdapter adapter;
    

   /**
    * The client error handler.
    */
   private ErrorHandler errorHandler;


   /**
    * The entity resolver.
    */
   private SAXEntityResolver entityResolver;


   /**
    * Creates the SAX parser.
    */
   public SAXParser()
   {
      this.adapter = new SAXAdapter();
      this.errorHandler = new HandlerBase();
      this.entityResolver = new SAXEntityResolver();
   }


   /**
    * Cleans up the object when it's destroyed.
    */
   protected void finalize()
      throws Throwable
   {
      this.adapter = null;
      this.errorHandler = null;
      this.entityResolver = null;
      super.finalize();
   }


   /**
    * Sets the locale. Only locales using the language english are accepted.
    *
    * @param locale the locale
    *
    * @exception org.xml.sax.SAXException
    *    if <CODE>locale</CODE> is <CODE>null</CODE> or the associated
    *    language is not english.
    */
   public void setLocale(Locale locale)
      throws SAXException
   {
      if ((locale == null) || (! locale.getLanguage().equals("en"))) {
         throw new SAXException("NanoXML/SAX doesn't support locale: "
                                + locale);
      }
   }


   /**
    * Sets the entity resolver.
    *
    * @param resolver the entity resolver
    */
   public void setEntityResolver(EntityResolver resolver)
   {
      this.entityResolver.setEntityResolver(resolver);
   }


   /**
    * Sets the DTD handler. As the parser is non-validating, this handler is
    * never called.
    *
    * @param handler the DTD handler
    */
   public void setDTDHandler(DTDHandler handler)
   {
      // nothing to do
   }


   /**
    * Allows an application to register a document event handler.
    *
    * @param handler the document handler
    */
   public void setDocumentHandler(DocumentHandler handler)
   {
      this.adapter.setDocumentHandler(handler);
   }


   /**
 * Allow an application to register an error event handler.
 * @param handler   the error handler
 * @uml.property   name="errorHandler"
 */
   public void setErrorHandler(ErrorHandler handler)
   {
      this.errorHandler = handler;
   }


   /**
    * Creates the XML parser.
    */
   private IXMLParser createParser()
      throws SAXException
   {
      try {
         return XMLParserFactory.createDefaultXMLParser();
      } catch (Exception exception) {
         throw new SAXException(exception);
      }
   }


   /**
    * Parse an XML document.
    *
    * @param source the input source
    */
   public void parse(InputSource source)
      throws SAXException,
             IOException
   {
      IXMLParser parser = this.createParser();
      parser.setBuilder(this.adapter);
      parser.setResolver(this.entityResolver);
      Reader reader = source.getCharacterStream();

      if (reader != null) {
         parser.setReader(new StdXMLReader(reader));
      } else {
         InputStream stream = source.getByteStream();

         if (stream != null) {
            String encoding = source.getEncoding();

            if (encoding != null) {
               try {
                  reader = new InputStreamReader(stream, encoding);
                  parser.setReader(new StdXMLReader(reader));
               } catch (UnsupportedEncodingException exception) {
                  throw new SAXException(exception);
               }
            } else { // if encoding == null
               parser.setReader(new StdXMLReader(stream));
            }
         } else { // if stream == null
            parser.setReader(new StdXMLReader(source.getPublicId(),
                                              source.getSystemId()));
         }
      }

      try {
         parser.parse();
         this.adapter.endDocument();
      } catch (IOException exception) {
         throw exception;
      } catch (Exception exception) {
         throw new SAXException(exception);
      } finally {
          reader.close();
      }
   }


   /**
    * Parse an XML document from a system identifier (URI).
    *
    * @param systemId the system ID
    */
   public void parse(String systemId)
      throws SAXException,
             IOException
   {
      IXMLParser parser = this.createParser();
      parser.setBuilder(this.adapter);
      parser.setReader(new StdXMLReader(null, systemId));

      try {
         parser.parse();
         this.adapter.endDocument();
      } catch (IOException exception) {
         throw exception;
      } catch (Exception exception) {
         throw new SAXException(exception);
      }
   }

}
