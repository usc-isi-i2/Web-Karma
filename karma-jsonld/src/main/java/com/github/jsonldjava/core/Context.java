package com.github.jsonldjava.core;

import static com.github.jsonldjava.core.JsonLdUtils.compareShortestLeast;
import static com.github.jsonldjava.utils.Obj.newMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.github.jsonldjava.core.JsonLdError.Error;
import com.github.jsonldjava.utils.JsonLdUrl;
import com.github.jsonldjava.utils.Obj;

/**
 * A helper class which still stores all the values in a map but gives member
 * variables easily access certain keys
 *
 * @author tristan
 *
 */
public class Context extends LinkedHashMap<String, Object> {

    private JsonLdOptions options;
    private Map<String, Object> termDefinitions;
    public Map<String, Object> inverse = null;

    public Context() {
        this(new JsonLdOptions());
    }

    public Context(JsonLdOptions opts) {
        super();
        init(opts);
    }

    public Context(Map<String, Object> map, JsonLdOptions opts) {
        super(map);
        init(opts);
    }

    public Context(Map<String, Object> map) {
        super(map);
        init(new JsonLdOptions());
    }

    public Context(Object context, JsonLdOptions opts) {
        // TODO: load remote context
        super(context instanceof Map ? (Map<String, Object>) context : null);
        init(opts);
    }

    private void init(JsonLdOptions options) {
        this.options = options;
        if (options.getBase() != null) {
            this.put("@base", options.getBase());
        }
        this.termDefinitions = newMap();
    }

    /**
     * Value Compaction Algorithm
     *
     * http://json-ld.org/spec/latest/json-ld-api/#value-compaction
     *
     * @param activeProperty
     *            The Active Property
     * @param value
     *            The value to compact
     * @return The compacted value
     */
    public Object compactValue(String activeProperty, Map<String, Object> value) {
        // 1)
        int numberMembers = value.size();
        // 2)
        if (value.containsKey("@index") && "@index".equals(this.getContainer(activeProperty))) {
            numberMembers--;
        }
        // 3)
        if (numberMembers > 2) {
            return value;
        }
        // 4)
        final String typeMapping = getTypeMapping(activeProperty);
        final String languageMapping = getLanguageMapping(activeProperty);
        if (value.containsKey("@id")) {
            // 4.1)
            if (numberMembers == 1 && "@id".equals(typeMapping)) {
                return compactIri((String) value.get("@id"));
            }
            // 4.2)
            if (numberMembers == 1 && "@vocab".equals(typeMapping)) {
                return compactIri((String) value.get("@id"), true);
            }
            // 4.3)
            return value;
        }
        final Object valueValue = value.get("@value");
        // 5)
        if (value.containsKey("@type") && Obj.equals(value.get("@type"), typeMapping)) {
            return valueValue;
        }
        // 6)
        if (value.containsKey("@language")) {
            // TODO: SPEC: doesn't specify to check default language as well
            if (Obj.equals(value.get("@language"), languageMapping)
                    || Obj.equals(value.get("@language"), this.get("@language"))) {
                return valueValue;
            }
        }
        // 7)
        if (numberMembers == 1
                && (!(valueValue instanceof String) || !this.containsKey("@language") || (termDefinitions
                        .containsKey(activeProperty)
                        && getTermDefinition(activeProperty).containsKey("@language") && languageMapping == null))) {
            return valueValue;
        }
        // 8)
        return value;
    }

    /**
     * Context Processing Algorithm
     *
     * http://json-ld.org/spec/latest/json-ld-api/#context-processing-algorithms
     *
     * @param localContext
     *            The Local Context object.
     * @param remoteContexts
     *            The list of Strings denoting the remote Context URLs.
     * @return The parsed and merged Context.
     * @throws JsonLdError
     *             If there is an error parsing the contexts.
     */
    public Context parse(Object localContext, List<String> remoteContexts) throws JsonLdError {
        if (remoteContexts == null) {
            remoteContexts = new ArrayList<>();
        }
        // 1. Initialize result to the result of cloning active context.
        Context result = this.clone(); // TODO: clone?
        // 2)
        if (!(localContext instanceof List)) {
            final Object temp = localContext;
            localContext = new ArrayList<>();
            ((List<Object>) localContext).add(temp);
        }
        // 3)
        for (Object context : ((List<Object>) localContext)) {
            // 3.1)
            if (context == null) {
                result = new Context(this.options);
                continue;
            } else if (context instanceof Context) {
                result = ((Context) context).clone();
            }
            // 3.2)
            else if (context instanceof String) {
                String uri = (String) result.get("@base");
                uri = JsonLdUrl.resolve(uri, (String) context);
                // 3.2.2
                if (remoteContexts.contains(uri)) {
                    throw new JsonLdError(Error.RECURSIVE_CONTEXT_INCLUSION, uri);
                }
                remoteContexts.add(uri);

                // 3.2.3: Dereference context
                final RemoteDocument rd = this.options.getDocumentLoader().loadDocument(uri);
                final Object remoteContext = rd.document;
                if (!(remoteContext instanceof Map)
                        || !((Map<String, Object>) remoteContext).containsKey("@context")) {
                    // If the dereferenced document has no top-level JSON object
                    // with an @context member
                    throw new JsonLdError(Error.INVALID_REMOTE_CONTEXT, context);
                }
                context = ((Map<String, Object>) remoteContext).get("@context");

                // 3.2.4
                result = result.parse(context, remoteContexts);
                // 3.2.5
                continue;
            } else if (!(context instanceof Map)) {
                // 3.3
                throw new JsonLdError(Error.INVALID_LOCAL_CONTEXT, context);
            }

            // 3.4
            if (remoteContexts.isEmpty() && ((Map<String, Object>) context).containsKey("@base")) {
                final Object value = ((Map<String, Object>) context).get("@base");
                if (value == null) {
                    result.remove("@base");
                } else if (value instanceof String) {
                    if (JsonLdUtils.isAbsoluteIri((String) value)) {
                        result.put("@base", value);
                    } else {
                        final String baseUri = (String) result.get("@base");
                        if (!JsonLdUtils.isAbsoluteIri(baseUri)) {
                            throw new JsonLdError(Error.INVALID_BASE_IRI, baseUri);
                        }
                        result.put("@base", JsonLdUrl.resolve(baseUri, (String) value));
                    }
                } else {
                    throw new JsonLdError(JsonLdError.Error.INVALID_BASE_IRI,
                            "@base must be a string");
                }
            }

            // 3.5
            if (((Map<String, Object>) context).containsKey("@vocab")) {
                final Object value = ((Map<String, Object>) context).get("@vocab");
                if (value == null) {
                    result.remove("@vocab");
                } else if (value instanceof String) {
                    if (JsonLdUtils.isAbsoluteIri((String) value)) {
                        result.put("@vocab", value);
                    } else {
                        throw new JsonLdError(Error.INVALID_VOCAB_MAPPING,
                                "@value must be an absolute IRI");
                    }
                } else {
                    throw new JsonLdError(Error.INVALID_VOCAB_MAPPING,
                            "@vocab must be a string or null");
                }
            }

            // 3.6
            if (((Map<String, Object>) context).containsKey("@language")) {
                final Object value = ((Map<String, Object>) context).get("@language");
                if (value == null) {
                    result.remove("@language");
                } else if (value instanceof String) {
                    result.put("@language", ((String) value).toLowerCase());
                } else {
                    throw new JsonLdError(Error.INVALID_DEFAULT_LANGUAGE, value);
                }
            }

            // 3.7
            final Map<String, Boolean> defined = new LinkedHashMap<>();
            for (final String key : ((Map<String, Object>) context).keySet()) {
                if ("@base".equals(key) || "@vocab".equals(key) || "@language".equals(key)) {
                    continue;
                }
                result.createTermDefinition((Map<String, Object>) context, key, defined);
            }
        }
        return result;
    }

    public Context parse(Object localContext) throws JsonLdError {
        return this.parse(localContext, new ArrayList<String>());
    }

    /**
     * Create Term Definition Algorithm
     *
     * http://json-ld.org/spec/latest/json-ld-api/#create-term-definition
     *
     * @param result
     * @param context
     * @param key
     * @param defined
     * @throws JsonLdError
     */
    private void createTermDefinition(Map<String, Object> context, String term,
            Map<String, Boolean> defined) throws JsonLdError {
        if (defined.containsKey(term)) {
            if (Boolean.TRUE.equals(defined.get(term))) {
                return;
            }
            throw new JsonLdError(Error.CYCLIC_IRI_MAPPING, term);
        }

        defined.put(term, false);

        if (JsonLdUtils.isKeyword(term)) {
            throw new JsonLdError(Error.KEYWORD_REDEFINITION, term);
        }

        this.termDefinitions.remove(term);
        Object value = context.get(term);
        if (value == null
                || (value instanceof Map && ((Map<String, Object>) value).containsKey("@id") && ((Map<String, Object>) value)
                        .get("@id") == null)) {
            this.termDefinitions.put(term, null);
            defined.put(term, true);
            return;
        }

        if (value instanceof String) {
            value = newMap("@id", value);
        }

        if (!(value instanceof Map)) {
            throw new JsonLdError(Error.INVALID_TERM_DEFINITION, value);
        }

        // casting the value so it doesn't have to be done below everytime
        final Map<String, Object> val = (Map<String, Object>) value;

        // 9) create a new term definition
        final Map<String, Object> definition = newMap();

        // 10)
        if (val.containsKey("@type")) {
            if (!(val.get("@type") instanceof String)) {
                throw new JsonLdError(Error.INVALID_TYPE_MAPPING, val.get("@type"));
            }
            String type = (String) val.get("@type");
            try {
                type = this.expandIri((String) val.get("@type"), false, true, context, defined);
            } catch (final JsonLdError error) {
                if (error.getType() != Error.INVALID_IRI_MAPPING) {
                    throw error;
                }
                throw new JsonLdError(Error.INVALID_TYPE_MAPPING, type);
            }
            // TODO: fix check for absoluteIri (blank nodes shouldn't count, at
            // least not here!)
            if ("@id".equals(type) || "@vocab".equals(type)
                    || (!type.startsWith("_:") && JsonLdUtils.isAbsoluteIri(type))) {
                definition.put("@type", type);
            } else {
                throw new JsonLdError(Error.INVALID_TYPE_MAPPING, type);
            }
        }

        // 11)
        if (val.containsKey("@reverse")) {
            if (val.containsKey("@id")) {
                throw new JsonLdError(Error.INVALID_REVERSE_PROPERTY, val);
            }
            if (!(val.get("@reverse") instanceof String)) {
                throw new JsonLdError(Error.INVALID_IRI_MAPPING,
                        "Expected String for @reverse value. got "
                                + (val.get("@reverse") == null ? "null" : val.get("@reverse")
                                        .getClass()));
            }
            final String reverse = this.expandIri((String) val.get("@reverse"), false, true,
                    context, defined);
            if (!JsonLdUtils.isAbsoluteIri(reverse)) {
                throw new JsonLdError(Error.INVALID_IRI_MAPPING, "Non-absolute @reverse IRI: "
                        + reverse);
            }
            definition.put("@id", reverse);
            if (val.containsKey("@container")) {
                final String container = (String) val.get("@container");
                if (container == null || "@set".equals(container) || "@index".equals(container)) {
                    definition.put("@container", container);
                } else {
                    throw new JsonLdError(Error.INVALID_REVERSE_PROPERTY,
                            "reverse properties only support set- and index-containers");
                }
            }
            definition.put("@reverse", true);
            this.termDefinitions.put(term, definition);
            defined.put(term, true);
            return;
        }

        // 12)
        definition.put("@reverse", false);

        // 13)
        if (val.get("@id") != null && !term.equals(val.get("@id"))) {
            if (!(val.get("@id") instanceof String)) {
                throw new JsonLdError(Error.INVALID_IRI_MAPPING,
                        "expected value of @id to be a string");
            }

            final String res = this.expandIri((String) val.get("@id"), false, true, context,
                    defined);
            if (JsonLdUtils.isKeyword(res) || JsonLdUtils.isAbsoluteIri(res)) {
                if ("@context".equals(res)) {
                    throw new JsonLdError(Error.INVALID_KEYWORD_ALIAS, "cannot alias @context");
                }
                definition.put("@id", res);
            } else {
                throw new JsonLdError(Error.INVALID_IRI_MAPPING,
                        "resulting IRI mapping should be a keyword, absolute IRI or blank node");
            }
        }

        // 14)
        else if (term.indexOf(":") >= 0) {
            final int colIndex = term.indexOf(":");
            final String prefix = term.substring(0, colIndex);
            final String suffix = term.substring(colIndex + 1);
            if (context.containsKey(prefix)) {
                this.createTermDefinition(context, prefix, defined);
            }
            if (termDefinitions.containsKey(prefix)) {
                definition.put("@id",
                        ((Map<String, Object>) termDefinitions.get(prefix)).get("@id") + suffix);
            } else {
                definition.put("@id", term);
            }
            // 15)
        } else if (this.containsKey("@vocab")) {
            definition.put("@id", this.get("@vocab") + term);
        } else {
            throw new JsonLdError(Error.INVALID_IRI_MAPPING,
                    "relative term definition without vocab mapping");
        }

        // 16)
        if (val.containsKey("@container")) {
            final String container = (String) val.get("@container");
            if (!"@list".equals(container) && !"@set".equals(container)
                    && !"@index".equals(container) && !"@language".equals(container)) {
                throw new JsonLdError(Error.INVALID_CONTAINER_MAPPING,
                        "@container must be either @list, @set, @index, or @language");
            }
            definition.put("@container", container);
        }

        // 17)
        if (val.containsKey("@language") && !val.containsKey("@type")) {
            if (val.get("@language") == null || val.get("@language") instanceof String) {
                final String language = (String) val.get("@language");
                definition.put("@language", language != null ? language.toLowerCase() : null);
            } else {
                throw new JsonLdError(Error.INVALID_LANGUAGE_MAPPING,
                        "@language must be a string or null");
            }
        }

        // 18)
        this.termDefinitions.put(term, definition);
        defined.put(term, true);
    }

    /**
     * IRI Expansion Algorithm
     *
     * http://json-ld.org/spec/latest/json-ld-api/#iri-expansion
     *
     * @param value
     * @param relative
     * @param vocab
     * @param context
     * @param defined
     * @return
     * @throws JsonLdError
     */
    String expandIri(String value, boolean relative, boolean vocab, Map<String, Object> context,
            Map<String, Boolean> defined) throws JsonLdError {
        // 1)
        if (value == null || JsonLdUtils.isKeyword(value)) {
            return value;
        }
        // 2)
        if (context != null && context.containsKey(value)
                && !Boolean.TRUE.equals(defined.get(value))) {
            this.createTermDefinition(context, value, defined);
        }
        // 3)
        if (vocab && this.termDefinitions.containsKey(value)) {
            final Map<String, Object> td = (LinkedHashMap<String, Object>) this.termDefinitions
                    .get(value);
            if (td != null) {
                return (String) td.get("@id");
            } else {
                return null;
            }
        }
        // 4)
        final int colIndex = value.indexOf(":");
        if (colIndex >= 0) {
            // 4.1)
            final String prefix = value.substring(0, colIndex);
            final String suffix = value.substring(colIndex + 1);
            // 4.2)
            if ("_".equals(prefix) || suffix.startsWith("//")) {
                return value;
            }
            // 4.3)
            if (context != null && context.containsKey(prefix)
                    && (!defined.containsKey(prefix) || defined.get(prefix) == false)) {
                this.createTermDefinition(context, prefix, defined);
            }
            // 4.4)
            if (this.termDefinitions.containsKey(prefix)) {
                return (String) ((LinkedHashMap<String, Object>) this.termDefinitions.get(prefix))
                        .get("@id") + suffix;
            }
            // 4.5)
            return value;
        }
        // 5)
        if (vocab && this.containsKey("@vocab")) {
            return this.get("@vocab") + value;
        }
        // 6)
        else if (relative) {
            return JsonLdUrl.resolve((String) this.get("@base"), value);
        } else if (context != null && JsonLdUtils.isRelativeIri(value)) {
            throw new JsonLdError(Error.INVALID_IRI_MAPPING, "not an absolute IRI: " + value);
        }
        // 7)
        return value;
    }

    /**
     * IRI Compaction Algorithm
     *
     * http://json-ld.org/spec/latest/json-ld-api/#iri-compaction
     *
     * Compacts an IRI or keyword into a term or prefix if it can be. If the IRI
     * has an associated value it may be passed.
     *
     * @param iri
     *            the IRI to compact.
     * @param value
     *            the value to check or null.
     * @param relativeTo
     *            options for how to compact IRIs: vocab: true to split after
     * @vocab, false not to.
     * @param reverse
     *            true if a reverse property is being compacted, false if not.
     *
     * @return the compacted term, prefix, keyword alias, or the original IRI.
     */
    String compactIri(String iri, Object value, boolean relativeToVocab, boolean reverse) {
        // 1)
        if (iri == null) {
            return null;
        }

        // 2)
        if (relativeToVocab && getInverse().containsKey(iri)) {
            // 2.1)
            String defaultLanguage = (String) this.get("@language");
            if (defaultLanguage == null) {
                defaultLanguage = "@none";
            }

            // 2.2)
            final List<String> containers = new ArrayList<>();
            // 2.3)
            String typeLanguage = "@language";
            String typeLanguageValue = "@null";

            // 2.4)
            if (value instanceof Map && ((Map<String, Object>) value).containsKey("@index")) {
                containers.add("@index");
            }

            // 2.5)
            if (reverse) {
                typeLanguage = "@type";
                typeLanguageValue = "@reverse";
                containers.add("@set");
            }
            // 2.6)
            else if (value instanceof Map && ((Map<String, Object>) value).containsKey("@list")) {
                // 2.6.1)
                if (!((Map<String, Object>) value).containsKey("@index")) {
                    containers.add("@list");
                }
                // 2.6.2)
                final List<Object> list = (List<Object>) ((Map<String, Object>) value).get("@list");
                // 2.6.3)
                String commonLanguage = (list.isEmpty()) ? defaultLanguage : null;
                String commonType = null;
                // 2.6.4)
                for (final Object item : list) {
                    // 2.6.4.1)
                    String itemLanguage = "@none";
                    String itemType = "@none";
                    // 2.6.4.2)
                    if (JsonLdUtils.isValue(item)) {
                        // 2.6.4.2.1)
                        if (((Map<String, Object>) item).containsKey("@language")) {
                            itemLanguage = (String) ((Map<String, Object>) item).get("@language");
                        }
                        // 2.6.4.2.2)
                        else if (((Map<String, Object>) item).containsKey("@type")) {
                            itemType = (String) ((Map<String, Object>) item).get("@type");
                        }
                        // 2.6.4.2.3)
                        else {
                            itemLanguage = "@null";
                        }
                    }
                    // 2.6.4.3)
                    else {
                        itemType = "@id";
                    }
                    // 2.6.4.4)
                    if (commonLanguage == null) {
                        commonLanguage = itemLanguage;
                    }
                    // 2.6.4.5)
                    else if (!commonLanguage.equals(itemLanguage) && JsonLdUtils.isValue(item)) {
                        commonLanguage = "@none";
                    }
                    // 2.6.4.6)
                    if (commonType == null) {
                        commonType = itemType;
                    }
                    // 2.6.4.7)
                    else if (!commonType.equals(itemType)) {
                        commonType = "@none";
                    }
                    // 2.6.4.8)
                    if ("@none".equals(commonLanguage) && "@none".equals(commonType)) {
                        break;
                    }
                }
                // 2.6.5)
                commonLanguage = (commonLanguage != null) ? commonLanguage : "@none";
                // 2.6.6)
                commonType = (commonType != null) ? commonType : "@none";
                // 2.6.7)
                if (!"@none".equals(commonType)) {
                    typeLanguage = "@type";
                    typeLanguageValue = commonType;
                }
                // 2.6.8)
                else {
                    typeLanguageValue = commonLanguage;
                }
            }
            // 2.7)
            else {
                // 2.7.1)
                if (value instanceof Map && ((Map<String, Object>) value).containsKey("@value")) {
                    // 2.7.1.1)
                    if (((Map<String, Object>) value).containsKey("@language")
                            && !((Map<String, Object>) value).containsKey("@index")) {
                        containers.add("@language");
                        typeLanguageValue = (String) ((Map<String, Object>) value).get("@language");
                    }
                    // 2.7.1.2)
                    else if (((Map<String, Object>) value).containsKey("@type")) {
                        typeLanguage = "@type";
                        typeLanguageValue = (String) ((Map<String, Object>) value).get("@type");
                    }
                    else {
                        typeLanguage = "@type";
                        typeLanguageValue = "@id";
                    }
                }
                // 2.7.2)
                else {
                    typeLanguage = "@type";
                    typeLanguageValue = "@id";
                }
                // 2.7.3)
                containers.add("@set");
            }

            // 2.8)
            containers.add("@none");
            // 2.9)
            if (typeLanguageValue == null) {
                typeLanguageValue = "@null";
            }
            // 2.10)
            final List<String> preferredValues = new ArrayList<>();
            // 2.11)
            if ("@reverse".equals(typeLanguageValue)) {
                preferredValues.add("@reverse");
            }
            // 2.12)
            if (("@reverse".equals(typeLanguageValue) || "@id".equals(typeLanguageValue))
                    && (value instanceof Map) && ((Map<String, Object>) value).containsKey("@id")) {
                // 2.12.1)
                final String result = this.compactIri(
                        (String) ((Map<String, Object>) value).get("@id"), null, true, true);
                if (termDefinitions.containsKey(result)
                        && ((Map<String, Object>) termDefinitions.get(result)).containsKey("@id")
                        && ((Map<String, Object>) value).get("@id").equals(
                                ((Map<String, Object>) termDefinitions.get(result)).get("@id"))) {
                    preferredValues.add("@vocab");
                    preferredValues.add("@id");
                }
                // 2.12.2)
                else {
                    preferredValues.add("@id");
                    preferredValues.add("@vocab");
                }
            }
            // 2.13)
            else {
                preferredValues.add(typeLanguageValue);
            }
            preferredValues.add("@none");

            // 2.14)
            final String term = selectTerm(iri, containers, typeLanguage, preferredValues);
            // 2.15)
            if (term != null) {
                return term;
            }
        }

        // 3)
        if (relativeToVocab && this.containsKey("@vocab")) {
            // determine if vocab is a prefix of the iri
            final String vocab = (String) this.get("@vocab");
            // 3.1)
            if (iri.indexOf(vocab) == 0 && !iri.equals(vocab)) {
                // use suffix as relative iri if it is not a term in the
                // active context
                final String suffix = iri.substring(vocab.length());
                if (!termDefinitions.containsKey(suffix)) {
                    return suffix;
                }
            }
        }

        // 4)
        String compactIRI = null;
        // 5)
        for (final Map.Entry<String, Object> stringObjectEntry : termDefinitions.entrySet()) {
            final Map<String, Object> termDefinition = (Map<String, Object>) stringObjectEntry.getValue();
            // 5.1)
            if (stringObjectEntry.getKey().contains(":")) {
                continue;
            }
            // 5.2)
            if (termDefinition == null || iri.equals(termDefinition.get("@id"))
                    || !iri.startsWith((String) termDefinition.get("@id"))) {
                continue;
            }

            // 5.3)
            final String candidate = stringObjectEntry.getKey() + ":"
                    + iri.substring(((String) termDefinition.get("@id")).length());
            // 5.4)
            if ((compactIRI == null || compareShortestLeast(candidate, compactIRI) < 0)
                    && (!termDefinitions.containsKey(candidate) || (iri
                            .equals(((Map<String, Object>) termDefinitions.get(candidate))
                                    .get("@id")) && value == null))) {
                compactIRI = candidate;
            }

        }

        // 6)
        if (compactIRI != null) {
            return compactIRI;
        }

        // 7)
        if (!relativeToVocab) {
            return JsonLdUrl.removeBase(this.get("@base"), iri);
        }

        // 8)
        return iri;
    }

    /**
     * Return a map of potential RDF prefixes based on the JSON-LD Term
     * Definitions in this context.
     * <p>
     * No guarantees of the prefixes are given, beyond that it will not contain
     * ":".
     *
     * @param onlyCommonPrefixes
     *            If <code>true</code>, the result will not include
     *            "not so useful" prefixes, such as "term1":
     *            "http://example.com/term1", e.g. all IRIs will end with "/" or
     *            "#". If <code>false</code>, all potential prefixes are
     *            returned.
     *
     * @return A map from prefix string to IRI string
     */
    public Map<String, String> getPrefixes(boolean onlyCommonPrefixes) {
        final Map<String, String> prefixes = new LinkedHashMap<>();
        for (final Map.Entry<String, Object> stringObjectEntry : termDefinitions.entrySet()) {
            if (stringObjectEntry.getKey().contains(":")) {
                continue;
            }
            final Map<String, Object> termDefinition = (Map<String, Object>) stringObjectEntry.getValue();
            if (termDefinition == null) {
                continue;
            }
            final String id = (String) termDefinition.get("@id");
            if (id == null) {
                continue;
            }
            if (stringObjectEntry.getKey().startsWith("@") || id.startsWith("@")) {
                continue;
            }
            if (!onlyCommonPrefixes || id.endsWith("/") || id.endsWith("#")) {
                prefixes.put(stringObjectEntry.getKey(), id);
            }
        }
        return prefixes;
    }

    String compactIri(String iri, boolean relativeToVocab) {
        return compactIri(iri, null, relativeToVocab, false);
    }

    String compactIri(String iri) {
        return compactIri(iri, null, false, false);
    }

    @Override
    public Context clone() {
        final Context rval = (Context) super.clone();
        // TODO: is this shallow copy enough? probably not, but it passes all
        // the tests!
        rval.termDefinitions = new LinkedHashMap<>(this.termDefinitions);
        return rval;
    }

    /**
     * Inverse Context Creation
     *
     * http://json-ld.org/spec/latest/json-ld-api/#inverse-context-creation
     *
     * Generates an inverse context for use in the compaction algorithm, if not
     * already generated for the given active context.
     *
     * @return the inverse context.
     */
    public Map<String, Object> getInverse() {

        // lazily create inverse
        if (inverse != null) {
            return inverse;
        }

        // 1)
        inverse = newMap();

        // 2)
        String defaultLanguage = (String) this.get("@language");
        if (defaultLanguage == null) {
            defaultLanguage = "@none";
        }

        // create term selections for each mapping in the context, ordererd by
        // shortest and then lexicographically least
        final List<String> terms = new ArrayList<>(termDefinitions.keySet());
        Collections.sort(terms, new Comparator<String>() {
            @Override
            public int compare(String a, String b) {
                return compareShortestLeast(a, b);
            }
        });

        for (final String term : terms) {
            final Map<String, Object> definition = (Map<String, Object>) termDefinitions.get(term);
            // 3.1)
            if (definition == null) {
                continue;
            }

            // 3.2)
            String container = (String) definition.get("@container");
            if (container == null) {
                container = "@none";
            }

            // 3.3)
            final String iri = (String) definition.get("@id");

            // 3.4 + 3.5)
            Map<String, Object> containerMap = (Map<String, Object>) inverse.get(iri);
            if (containerMap == null) {
                containerMap = newMap();
                inverse.put(iri, containerMap);
            }

            // 3.6 + 3.7)
            Map<String, Object> typeLanguageMap = (Map<String, Object>) containerMap.get(container);
            if (typeLanguageMap == null) {
                typeLanguageMap = newMap();
                typeLanguageMap.put("@language", newMap());
                typeLanguageMap.put("@type", newMap());
                containerMap.put(container, typeLanguageMap);
            }

            // 3.8)
            if (Boolean.TRUE.equals(definition.get("@reverse"))) {
                final Map<String, Object> typeMap = (Map<String, Object>) typeLanguageMap
                        .get("@type");
                if (!typeMap.containsKey("@reverse")) {
                    typeMap.put("@reverse", term);
                }
                // 3.9)
            } else if (definition.containsKey("@type")) {
                final Map<String, Object> typeMap = (Map<String, Object>) typeLanguageMap
                        .get("@type");
                if (!typeMap.containsKey(definition.get("@type"))) {
                    typeMap.put((String) definition.get("@type"), term);
                }
                // 3.10)
            } else if (definition.containsKey("@language")) {
                final Map<String, Object> languageMap = (Map<String, Object>) typeLanguageMap
                        .get("@language");
                String language = (String) definition.get("@language");
                if (language == null) {
                    language = "@null";
                }
                if (!languageMap.containsKey(language)) {
                    languageMap.put(language, term);
                }
                // 3.11)
            } else {
                // 3.11.1)
                final Map<String, Object> languageMap = (Map<String, Object>) typeLanguageMap
                        .get("@language");
                // 3.11.2)
                if (!languageMap.containsKey("@language")) {
                    languageMap.put("@language", term);
                }
                // 3.11.3)
                if (!languageMap.containsKey("@none")) {
                    languageMap.put("@none", term);
                }
                // 3.11.4)
                final Map<String, Object> typeMap = (Map<String, Object>) typeLanguageMap
                        .get("@type");
                // 3.11.5)
                if (!typeMap.containsKey("@none")) {
                    typeMap.put("@none", term);
                }
            }
        }
        // 4)
        return inverse;
    }

    /**
     * Term Selection
     *
     * http://json-ld.org/spec/latest/json-ld-api/#term-selection
     *
     * This algorithm, invoked via the IRI Compaction algorithm, makes use of an
     * active context's inverse context to find the term that is best used to
     * compact an IRI. Other information about a value associated with the IRI
     * is given, including which container mappings and which type mapping or
     * language mapping would be best used to express the value.
     *
     * @return the selected term.
     */
    private String selectTerm(String iri, List<String> containers, String typeLanguage,
            List<String> preferredValues) {
        final Map<String, Object> inv = getInverse();
        // 1)
        final Map<String, Object> containerMap = (Map<String, Object>) inv.get(iri);
        // 2)
        for (final String container : containers) {
            // 2.1)
            if (!containerMap.containsKey(container)) {
                continue;
            }
            // 2.2)
            final Map<String, Object> typeLanguageMap = (Map<String, Object>) containerMap
                    .get(container);
            // 2.3)
            final Map<String, Object> valueMap = (Map<String, Object>) typeLanguageMap
                    .get(typeLanguage);
            // 2.4 )
            for (final String item : preferredValues) {
                // 2.4.1
                if (!valueMap.containsKey(item)) {
                    continue;
                }
                // 2.4.2
                return (String) valueMap.get(item);
            }
        }
        // 3)
        return null;
    }

    /**
     * Retrieve container mapping.
     *
     * @param property
     *            The Property to get a container mapping for.
     * @return The container mapping
     */
    public String getContainer(String property) {
        if ("@graph".equals(property)) {
            return "@set";
        }
        if (JsonLdUtils.isKeyword(property)) {
            return property;
        }
        final Map<String, Object> td = (Map<String, Object>) termDefinitions.get(property);
        if (td == null) {
            return null;
        }
        return (String) td.get("@container");
    }

    public Boolean isReverseProperty(String property) {
        final Map<String, Object> td = (Map<String, Object>) termDefinitions.get(property);
        if (td == null) {
            return false;
        }
        final Object reverse = td.get("@reverse");
        return reverse != null && (Boolean) reverse;
    }

    private String getTypeMapping(String property) {
        final Map<String, Object> td = (Map<String, Object>) termDefinitions.get(property);
        if (td == null) {
            return null;
        }
        return (String) td.get("@type");
    }

    private String getLanguageMapping(String property) {
        final Map<String, Object> td = (Map<String, Object>) termDefinitions.get(property);
        if (td == null) {
            return null;
        }
        return (String) td.get("@language");
    }

    Map<String, Object> getTermDefinition(String key) {
        return ((Map<String, Object>) termDefinitions.get(key));
    }

    public Object expandValue(String activeProperty, Object value) throws JsonLdError {
        final Map<String, Object> rval = newMap();
        final Map<String, Object> td = getTermDefinition(activeProperty);
        // 1)
        if (td != null && "@id".equals(td.get("@type"))) {
            // TODO: i'm pretty sure value should be a string if the @type is
            // @id
            rval.put("@id", expandIri(value.toString(), true, false, null, null));
            return rval;
        }
        // 2)
        if (td != null && "@vocab".equals(td.get("@type"))) {
            // TODO: same as above
            rval.put("@id", expandIri(value.toString(), true, true, null, null));
            return rval;
        }
        // 3)
        rval.put("@value", value);
        // 4)
        if (td != null && td.containsKey("@type")) {
            rval.put("@type", td.get("@type"));
        }
        // 5)
        else if (value instanceof String) {
            // 5.1)
            if (td != null && td.containsKey("@language")) {
                final String lang = (String) td.get("@language");
                if (lang != null) {
                    rval.put("@language", lang);
                }
            }
            // 5.2)
            else if (this.get("@language") != null) {
                rval.put("@language", this.get("@language"));
            }
        }
        return rval;
    }

    public Object getContextValue(String activeProperty, String string) throws JsonLdError {
        throw new JsonLdError(Error.NOT_IMPLEMENTED,
                "getContextValue is only used by old code so far and thus isn't implemented");
    }

    public Map<String, Object> serialize() {
        final Map<String, Object> ctx = newMap();
        if (this.get("@base") != null && !this.get("@base").equals(options.getBase())) {
            ctx.put("@base", this.get("@base"));
        }
        if (this.get("@language") != null) {
            ctx.put("@language", this.get("@language"));
        }
        if (this.get("@vocab") != null) {
            ctx.put("@vocab", this.get("@vocab"));
        }
        for (final Map.Entry<String, Object> stringObjectEntry : termDefinitions.entrySet()) {
            final Map<String, Object> definition = (Map<String, Object>) stringObjectEntry.getValue();
            if (definition.get("@language") == null
                    && definition.get("@container") == null
                    && definition.get("@type") == null
                    && (definition.get("@reverse") == null || Boolean.FALSE.equals(definition
                            .get("@reverse")))) {
                final String cid = this.compactIri((String) definition.get("@id"));
                ctx.put(stringObjectEntry.getKey(), stringObjectEntry.getKey().equals(cid) ? definition.get("@id") : cid);
            } else {
                final Map<String, Object> defn = newMap();
                final String cid = this.compactIri((String) definition.get("@id"));
                final Boolean reverseProperty = Boolean.TRUE.equals(definition.get("@reverse"));
                if (!(stringObjectEntry.getKey().equals(cid) && !reverseProperty)) {
                    defn.put(reverseProperty ? "@reverse" : "@id", cid);
                }
                final String typeMapping = (String) definition.get("@type");
                if (typeMapping != null) {
                    defn.put("@type", JsonLdUtils.isKeyword(typeMapping) ? typeMapping
                            : compactIri(typeMapping, true));
                }
                if (definition.get("@container") != null) {
                    defn.put("@container", definition.get("@container"));
                }
                final Object lang = definition.get("@language");
                if (definition.get("@language") != null) {
                    defn.put("@language", Boolean.FALSE.equals(lang) ? null : lang);
                }
                ctx.put(stringObjectEntry.getKey(), defn);
            }
        }

        final Map<String, Object> rval = newMap();
        if (!(ctx == null || ctx.isEmpty())) {
            rval.put("@context", ctx);
        }
        return rval;
    }

}