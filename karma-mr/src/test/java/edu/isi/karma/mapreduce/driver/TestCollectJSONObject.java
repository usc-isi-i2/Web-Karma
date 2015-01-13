package edu.isi.karma.mapreduce.driver;

import org.apache.hadoop.io.Text;
import org.junit.Test;

import edu.isi.karma.mapreduce.function.CollectJSONObject;

public class TestCollectJSONObject {

	@Test
	public void test()
	{
		CollectJSONObject cjo = new CollectJSONObject();
		Text result = cjo.evaluate(new Text("{\"http://purl.org/dc/elements/1.1/publisher\":[{\"@id\":\"http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor1/Vendor1\"}],\"http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/offerWebpage\":[{\"@id\":\"http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor1/Offer1/\"}],\"http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/vendor\":[{\"@id\":\"http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor1/Vendor1\"}],\"http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/validTo\":[{\"@type\":\"http://www.w3.org/2001/XMLSchema#dateTime\",\"@value\":\"2008-09-11T00:00:00\"}],\"@type\":[\"http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/Offer\"],\"http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/price\":[{\"@type\":\"http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/USD\",\"@value\":\"7683.53\"}],\"@id\":\"http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor1/Offer1\",\"http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/deliveryDays\":[{\"@type\":\"http://www.w3.org/2001/XMLSchema#integer\",\"@value\":\"5\"}],\"http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/validFrom\":[{\"@type\":\"http://www.w3.org/2001/XMLSchema#dateTime\",\"@value\":\"2008-03-20T00:00:00\"}],\"http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/product\":[{\"@id\":\"http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer1/Product3\"}],\"http://purl.org/dc/elements/1.1/date\":[{\"@type\":\"http://www.w3.org/2001/XMLSchema#date\",\"@value\":\"2008-06-17\"}]}"), new Text("$.http://www4\\.wiwiss\\.fu-berlin\\.de/bizer/bsbm/v01/vocabulary/vendor.@id"));
		Text result2 = cjo.evaluate(new Text("{\"http://purl.org/dc/elements/1.1/publisher\":[{\"@id\":\"http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor1/Vendor1\"}],\"@type\":[\"http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/Vendor\"],\"http://xmlns.com/foaf/0.1/homepage\":[{\"@id\":\"http://www.vendor1.com/\"}],\"http://www.w3.org/2000/01/rdf-schema#comment\":[{\"@value\":\"bihourly prosiest matrixes jaggedest violinists dins archipelagos heighths limber azons acceptee husbander ashram relativeness grannies rectangles unearthing conies capered toeshoe fervour domination impishly satirically photonegative kaleidoscopic morticians\"}],\"http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/country\":[{\"@id\":\"http://downlode.org/rdf/iso-3166/countries#GB\"}],\"http://www.w3.org/2000/01/rdf-schema#label\":[{\"@value\":\"reexhibit\"}],\"@id\":\"http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor1/Vendor1\",\"http://purl.org/dc/elements/1.1/date\":[{\"@type\":\"http://www.w3.org/2001/XMLSchema#date\",\"@value\":\"2008-05-31\"}]}"), new Text("$.@id"));
		System.out.println(result);
		System.out.println(result2);
		System.out.println(result.compareTo(result2));
	}
}
