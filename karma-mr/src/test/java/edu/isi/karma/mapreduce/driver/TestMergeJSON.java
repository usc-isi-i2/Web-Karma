package edu.isi.karma.mapreduce.driver;

import org.apache.hadoop.io.Text;
import org.junit.Assert;
import org.junit.Test;

import edu.isi.karma.mapreduce.function.MergeJSON;

public class TestMergeJSON {
	@Test
	public void testMerge() {
		MergeJSON test = new MergeJSON();
		String a = " {\"syll:twitterId\":\"caknoblock\",\"@type\":\"http://lod.isi.edu/ontology/syllabus/Person\",\"foaf:homepage\":{\"@type\":\"http://xmlns.com/foaf/0.1/Document\",\"@id\":\"http://www.isi.edu/~knoblock\"},\"foaf:name\":\"Craig Knoblock\",\"foaf:lastName\":\"Knoblock\",\"@id\":\"http://lod.isi.edu/cs548/person/Knoblock\",\"id\":\"http://lod.isi.edu/cs548/person/Knoblock\",\"foaf:depiction\":{\"@type\":\"http://xmlns.com/foaf/0.1/Image\",\"@id\":\"http://www.isi.edu/integration/people/knoblock/img/CraigKnoblock.jpg\",\"foaf:depicts\":\"<http://lod.isi.edu/cs548/person/Knoblock>\"},\"foaf:title\":\"Prof\",\"foaf:mbox\":\"mailto:knoblock@isi.edu\"}";
		String b = " {\"rdfs:label\":\"knoblock@isi.edu\",\"@type\":\"http://www.w3.org/2002/07/owl#Thing\",\"@id\":\"mailto:knoblock@isi.edu\",\"id\":\"mailto:knoblock@isi.edu\"}";
		Text result = test.evaluate(new Text(a), new Text(b), new Text("foaf:mbox"));
		String expected = "{\"id\":\"http://lod.isi.edu/cs548/person/Knoblock\",\"syll:twitterId\":\"caknoblock\",\"@type\":\"http://lod.isi.edu/ontology/syllabus/Person\",\"foaf:homepage\":{\"@type\":\"http://xmlns.com/foaf/0.1/Document\",\"@id\":\"http://www.isi.edu/~knoblock\"},\"foaf:lastName\":\"Knoblock\",\"foaf:name\":\"Craig Knoblock\",\"foaf:depiction\":{\"@type\":\"http://xmlns.com/foaf/0.1/Image\",\"@id\":\"http://www.isi.edu/integration/people/knoblock/img/CraigKnoblock.jpg\",\"foaf:depicts\":\"<http://lod.isi.edu/cs548/person/Knoblock>\"},\"@id\":\"http://lod.isi.edu/cs548/person/Knoblock\",\"foaf:title\":\"Prof\",\"foaf:mbox\":{\"id\":\"mailto:knoblock@isi.edu\",\"rdfs:label\":\"knoblock@isi.edu\",\"@type\":\"http://www.w3.org/2002/07/owl#Thing\",\"@id\":\"mailto:knoblock@isi.edu\"}}";
		Assert.assertEquals(result.toString(), expected);
	}
}
