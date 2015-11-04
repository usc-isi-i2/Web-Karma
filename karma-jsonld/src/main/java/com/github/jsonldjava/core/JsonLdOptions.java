package com.github.jsonldjava.core;

/**
 * The JsonLdOptions type as specified in the <a
 * href="http://www.w3.org/TR/json-ld-api/#the-jsonldoptions-type">JSON-LD-API
 * specification</a>.
 *
 * @author tristan
 *
 */
public class JsonLdOptions {

    /**
     * Constructs an instance of JsonLdOptions using an empty base.
     */
    public JsonLdOptions() {
        this("");
    }

    /**
     * Constructs an instance of JsonLdOptions using the given base.
     *
     * @param base
     *            The base IRI for the document.
     */
    public JsonLdOptions(String base) {
        this.setBase(base);
    }

    // Base options : http://www.w3.org/TR/json-ld-api/#idl-def-JsonLdOptions

    /**
     * http://www.w3.org/TR/json-ld-api/#widl-JsonLdOptions-base
     */
    private String base = null;

    /**
     * http://www.w3.org/TR/json-ld-api/#widl-JsonLdOptions-compactArrays
     */
    private Boolean compactArrays = true;
    /**
     * http://www.w3.org/TR/json-ld-api/#widl-JsonLdOptions-expandContext
     */
    private Object expandContext = null;
    /**
     * http://www.w3.org/TR/json-ld-api/#widl-JsonLdOptions-processingMode
     */
    private String processingMode = "json-ld-1.0";
    /**
     * http://www.w3.org/TR/json-ld-api/#widl-JsonLdOptions-documentLoader
     */
    private DocumentLoader documentLoader = new DocumentLoader();

    // Frame options : http://json-ld.org/spec/latest/json-ld-framing/

    private Boolean embed = null;
    private Boolean explicit = null;
    private Boolean omitDefault = null;

    // RDF conversion options :
    // http://www.w3.org/TR/json-ld-api/#serialize-rdf-as-json-ld-algorithm

    Boolean useRdfType = false;
    Boolean useNativeTypes = false;
    private boolean produceGeneralizedRdf = false;

    public Boolean getEmbed() {
        return embed;
    }

    public void setEmbed(Boolean embed) {
        this.embed = embed;
    }

    public Boolean getExplicit() {
        return explicit;
    }

    public void setExplicit(Boolean explicit) {
        this.explicit = explicit;
    }

    public Boolean getOmitDefault() {
        return omitDefault;
    }

    public void setOmitDefault(Boolean omitDefault) {
        this.omitDefault = omitDefault;
    }

    public Boolean getCompactArrays() {
        return compactArrays;
    }

    public void setCompactArrays(Boolean compactArrays) {
        this.compactArrays = compactArrays;
    }

    public Object getExpandContext() {
        return expandContext;
    }

    public void setExpandContext(Object expandContext) {
        this.expandContext = expandContext;
    }

    public String getProcessingMode() {
        return processingMode;
    }

    public void setProcessingMode(String processingMode) {
        this.processingMode = processingMode;
    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public Boolean getUseRdfType() {
        return useRdfType;
    }

    public void setUseRdfType(Boolean useRdfType) {
        this.useRdfType = useRdfType;
    }

    public Boolean getUseNativeTypes() {
        return useNativeTypes;
    }

    public void setUseNativeTypes(Boolean useNativeTypes) {
        this.useNativeTypes = useNativeTypes;
    }

    public boolean getProduceGeneralizedRdf() {
        return this.produceGeneralizedRdf;
    }

    public void setProduceGeneralizedRdf(Boolean produceGeneralizedRdf) {
        this.produceGeneralizedRdf = produceGeneralizedRdf;
    }

    public DocumentLoader getDocumentLoader() {
        return documentLoader;
    }

    public void setDocumentLoader(DocumentLoader documentLoader) {
        this.documentLoader = documentLoader;
    }

    // TODO: THE FOLLOWING ONLY EXIST SO I DON'T HAVE TO DELETE A LOT OF CODE,
    // REMOVE IT WHEN DONE
    public String format = null;
    public Boolean useNamespaces = false;
    public String outputForm = null;
}
