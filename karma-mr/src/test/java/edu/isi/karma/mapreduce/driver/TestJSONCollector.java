package edu.isi.karma.mapreduce.driver;

import org.apache.hadoop.io.Text;
import org.junit.Test;

import edu.isi.karma.mapreduce.function.CollectJSONObject;

public class TestJSONCollector {
	@Test
	public void testMerge() {
		CollectJSONObject test = new CollectJSONObject();
		String a = "{\r\n\"@type\":\"http://dig.isi.edu/ontology/URLEntity\",\r\n\"dig:snapshot\":[\r\n{\r\n\"dig:hasTitlePart\":{\r\n\"@type\":\"http://schema.org/WebPageElement\",\r\n\"@id\":\"https://karmadigstorage.blob.core.windows.net/arch/churl/20140301/inlandempire.backpage.com/FemaleEscorts/smoking-hot-snow-bunny-nice-round-booty-colton-215-fwy-25/38941250/title\"\r\n},\r\n\"dig:hasBodyPart\":{\r\n\"dig:mentionsPhoneNumber\":{\r\n\"dig:tenDigitPhoneNumber\":\"7024104348\",\r\n\"schema:location\":{\r\n\"@type\":\"http://schema.org/Place\",\r\n\"@id\":\"http://dig.isi.edu/data/exchange/702410\"\r\n},\r\n\"@type\":\"http://dig.isi.edu/ontology/PhoneNumber\",\r\n\"@id\":\"http://dig.isi.edu/data/phonenumber/7024104348\"\r\n},\r\n\"@type\":\"http://schema.org/WebPageElement\",\r\n\"@id\":\"https://karmadigstorage.blob.core.windows.net/arch/churl/20140301/inlandempire.backpage.com/FemaleEscorts/smoking-hot-snow-bunny-nice-round-booty-colton-215-fwy-25/38941250/body\"\r\n},\r\n\"@type\":\"http://schema.org/WebPage\",\r\n\"@id\":\"https://karmadigstorage.blob.core.windows.net/arch/churl/20140301/inlandempire.backpage.com/FemaleEscorts/smoking-hot-snow-bunny-nice-round-booty-colton-215-fwy-25/38941250\"\r\n},\r\n{\r\n\"dig:hasTitlePart\":{\r\n\"@type\":\"http://schema.org/WebPageElement\",\r\n\"@id\":\"https://karmadigstorage.blob.core.windows.net/arch/churl/20140302/inlandempire.backpage.com/FemaleEscorts/smoking-hot-snow-bunny-nice-round-booty-colton-215-fwy-25/38941250/title\"\r\n},\r\n\"dig:hasBodyPart\":{\r\n\"dig:mentionsPhoneNumber\":{\r\n\"dig:tenDigitPhoneNumber\":\"7024104348\",\r\n\"schema:location\":{\r\n\"@type\":\"http://schema.org/Place\",\r\n\"@id\":\"http://dig.isi.edu/data/exchange/702410\"\r\n},\r\n\"@type\":\"http://dig.isi.edu/ontology/PhoneNumber\",\r\n\"@id\":\"http://dig.isi.edu/data/phonenumber/7024104348\"\r\n},\r\n\"@type\":\"http://schema.org/WebPageElement\",\r\n\"@id\":\"https://karmadigstorage.blob.core.windows.net/arch/churl/20140302/inlandempire.backpage.com/FemaleEscorts/smoking-hot-snow-bunny-nice-round-booty-colton-215-fwy-25/38941250/body\"\r\n},\r\n\"@type\":\"http://schema.org/WebPage\",\r\n\"@id\":\"https://karmadigstorage.blob.core.windows.net/arch/churl/20140302/inlandempire.backpage.com/FemaleEscorts/smoking-hot-snow-bunny-nice-round-booty-colton-215-fwy-25/38941250\"\r\n}\r\n],\r\n\"@id\":\"http://inlandempire.backpage.com/FemaleEscorts/smoking-hot-snow-bunny-nice-round-booty-colton-215-fwy-25/38941250\"\r\n}";
		Text result = test.evaluate(new Text(a), new Text("$.dig:snapshot.dig:hasBodyPart.dig:mentionsPhoneNumber.schema:location._id"));
		System.out.println(result.toString());
	}
}
