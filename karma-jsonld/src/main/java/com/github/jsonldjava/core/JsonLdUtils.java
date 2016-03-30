package com.github.jsonldjava.core;

import static com.github.jsonldjava.utils.Obj.newMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.github.jsonldjava.utils.JsonLdUrl;
import com.github.jsonldjava.utils.Obj;

public class JsonLdUtils {

    private static final int MAX_CONTEXT_URLS = 10;

    private JsonLdUtils() {
    }

    /**
     * Returns whether or not the given value is a keyword (or a keyword alias).
     *
     * @param v
     *            the value to check.
     * @param [ctx] the active context to check against.
     *
     * @return true if the value is a keyword, false if not.
     */
    static boolean isKeyword(Object key) {
        if (!isString(key)) {
            return false;
        }
        return "@base".equals(key) || "@context".equals(key) || "@container".equals(key)
                || "@default".equals(key) || "@embed".equals(key) || "@explicit".equals(key)
                || "@graph".equals(key) || "@id".equals(key) || "@index".equals(key)
                || "@language".equals(key) || "@list".equals(key) || "@omitDefault".equals(key)
                || "@reverse".equals(key) || "@preserve".equals(key) || "@set".equals(key)
                || "@type".equals(key) || "@value".equals(key) || "@vocab".equals(key);
    }

    public static Boolean deepCompare(Object v1, Object v2, Boolean listOrderMatters) {
        if (v1 == null) {
            return v2 == null;
        } else if (v2 == null) {
            return v1 == null;
        } else if (v1 instanceof Map && v2 instanceof Map) {
            final Map<String, Object> m1 = (Map<String, Object>) v1;
            final Map<String, Object> m2 = (Map<String, Object>) v2;
            if (m1.size() != m2.size()) {
                return false;
            }
            for (final Map.Entry<String, Object> stringObjectEntry : m1.entrySet()) {
                if (!m2.containsKey(stringObjectEntry.getKey())
                        || !deepCompare(stringObjectEntry.getValue(), m2.get(stringObjectEntry.getKey()), listOrderMatters)) {
                    return false;
                }
            }
            return true;
        } else if (v1 instanceof List && v2 instanceof List) {
            final List<Object> l1 = (List<Object>) v1;
            final List<Object> l2 = (List<Object>) v2;
            if (l1.size() != l2.size()) {

                return false;
            }
            // used to mark members of l2 that we have already matched to avoid
            // matching the same item twice for lists that have duplicates
            final boolean alreadyMatched[] = new boolean[l2.size()];
            for (int i = 0; i < l1.size(); i++) {
                final Object o1 = l1.get(i);
                Boolean gotmatch = false;
                if (listOrderMatters) {
                    gotmatch = deepCompare(o1, l2.get(i), listOrderMatters);
                } else {
                    for (int j = 0; j < l2.size(); j++) {
                        if (!alreadyMatched[j] && deepCompare(o1, l2.get(j), listOrderMatters)) {
                            alreadyMatched[j] = true;
                            gotmatch = true;
                            break;
                        }
                    }
                }
                if (!gotmatch) {
                    return false;
                }
            }
            return true;
        } else {
            return v1.equals(v2);
        }
    }

    public static Boolean deepCompare(Object v1, Object v2) {
        return deepCompare(v1, v2, false);
    }

    public static boolean deepContains(List<Object> values, Object value) {
        for (final Object item : values) {
            if (deepCompare(item, value, false)) {
                return true;
            }
        }
        return false;
    }

    static void mergeValue(Map<String, Object> obj, String key, Object value) {
        if (obj == null) {
            return;
        }
        List<Object> values = (List<Object>) obj.get(key);
        if (values == null) {
            values = new ArrayList<>();
            obj.put(key, values);
        }
        if ("@list".equals(key)
                || (value instanceof Map && ((Map<String, Object>) value).containsKey("@list"))
                || !deepContains(values, value)) {
            values.add(value);
        }
    }

    static void mergeCompactedValue(Map<String, Object> obj, String key, Object value) {
        if (obj == null) {
            return;
        }
        final Object prop = obj.get(key);
        if (prop == null) {
            obj.put(key, value);
            return;
        }
        if (!(prop instanceof List)) {
            final List<Object> tmp = new ArrayList<>();
            tmp.add(prop);
        }
        if (value instanceof List) {
            ((List<Object>) prop).addAll((List<Object>) value);
        } else {
            ((List<Object>) prop).add(value);
        }
    }

    public static boolean isAbsoluteIri(String value) {
        // TODO: this is a bit simplistic!
        return value.contains(":");
    }

    /**
     * Returns true if the given value is a subject with properties.
     *
     * @param v
     *            the value to check.
     *
     * @return true if the value is a subject with properties, false if not.
     */
    static boolean isNode(Object v) {
        // Note: A value is a subject if all of these hold true:
        // 1. It is an Object.
        // 2. It is not a @value, @set, or @list.
        // 3. It has more than 1 key OR any existing key is not @id.
        if (v instanceof Map
                && !(((Map) v).containsKey("@value") || ((Map) v).containsKey("@set") || ((Map) v)
                        .containsKey("@list"))) {
            return ((Map<String, Object>) v).size() > 1 || !((Map) v).containsKey("@id");
        }
        return false;
    }

    /**
     * Returns true if the given value is a subject reference.
     *
     * @param v
     *            the value to check.
     *
     * @return true if the value is a subject reference, false if not.
     */
    static boolean isNodeReference(Object v) {
        // Note: A value is a subject reference if all of these hold true:
        // 1. It is an Object.
        // 2. It has a single key: @id.
        return (v instanceof Map && ((Map<String, Object>) v).size() == 1 && ((Map<String, Object>) v)
                .containsKey("@id"));
    }

    // TODO: fix this test
    public static boolean isRelativeIri(String value) {
        if (!(isKeyword(value) || isAbsoluteIri(value))) {
            return true;
        }
        return false;
    }

    // //////////////////////////////////////////////////// OLD CODE BELOW

    /**
     * Adds a value to a subject. If the value is an array, all values in the
     * array will be added.
     *
     * Note: If the value is a subject that already exists as a property of the
     * given subject, this method makes no attempt to deeply merge properties.
     * Instead, the value will not be added.
     *
     * @param subject
     *            the subject to add the value to.
     * @param property
     *            the property that relates the value to the subject.
     * @param value
     *            the value to add.
     * @param [propertyIsArray] true if the property is always an array, false
     *        if not (default: false).
     * @param [allowDuplicate] true if the property is a @list, false if not
     *        (default: false).
     */
    static void addValue(Map<String, Object> subject, String property, Object value,
            boolean propertyIsArray, boolean allowDuplicate) {

        if (isArray(value)) {
            if (((List) value).isEmpty() && propertyIsArray && !subject.containsKey(property)) {
                subject.put(property, new ArrayList<>());
            }
            for (final Object val : (List) value) {
                addValue(subject, property, val, propertyIsArray, allowDuplicate);
            }
        } else if (subject.containsKey(property)) {
            // check if subject already has the value if duplicates not allowed
            final boolean hasValue = !allowDuplicate && hasValue(subject, property, value);

            // make property an array if value not present or always an array
            if (!isArray(subject.get(property)) && (!hasValue || propertyIsArray)) {
                final List<Object> tmp = new ArrayList<>();
                tmp.add(subject.get(property));
                subject.put(property, tmp);
            }

            // add new value
            if (!hasValue) {
                ((List<Object>) subject.get(property)).add(value);
            }
        } else {
            // add new value as a set or single value
            Object tmp;
            if (propertyIsArray) {
                tmp = new ArrayList<>();
                ((List<Object>) tmp).add(value);
            } else {
                tmp = value;
            }
            subject.put(property, tmp);
        }
    }

    static void addValue(Map<String, Object> subject, String property, Object value,
            boolean propertyIsArray) {
        addValue(subject, property, value, propertyIsArray, true);
    }

    static void addValue(Map<String, Object> subject, String property, Object value) {
        addValue(subject, property, value, false, true);
    }

    /**
     * Prepends a base IRI to the given relative IRI.
     *
     * @param base
     *            the base IRI.
     * @param iri
     *            the relative IRI.
     *
     * @return the absolute IRI.
     *
     *         TODO: the JsonLdUrl class isn't as forgiving as the Node.js url
     *         parser, we may need to re-implement the parser here to support
     *         the flexibility required
     */
    private static String prependBase(Object baseobj, String iri) {
        // already an absolute IRI
        if (iri.indexOf(":") != -1) {
            return iri;
        }

        // parse base if it is a string
        JsonLdUrl base;
        if (isString(baseobj)) {
            base = JsonLdUrl.parse((String) baseobj);
        } else {
            // assume base is already a JsonLdUrl
            base = (JsonLdUrl) baseobj;
        }

        final JsonLdUrl rel = JsonLdUrl.parse(iri);

        // start hierarchical part
        String hierPart = base.protocol;
        if (!"".equals(rel.authority)) {
            hierPart += "//" + rel.authority;
        } else if (!"".equals(base.href)) {
            hierPart += "//" + base.authority;
        }

        // per RFC3986 normalize
        String path;

        // IRI represents an absolute path
        if (rel.pathname.indexOf("/") == 0) {
            path = rel.pathname;
        } else {
            path = base.pathname;

            // append relative path to the end of the last directory from base
            if (!"".equals(rel.pathname)) {
                path = path.substring(0, path.lastIndexOf("/") + 1);
                if (path.length() > 0 && !path.endsWith("/")) {
                    path += "/";
                }
                path += rel.pathname;
            }
        }

        // remove slashes anddots in path
        path = JsonLdUrl.removeDotSegments(path, !"".equals(hierPart));

        // add query and hash
        if (!"".equals(rel.query)) {
            path += "?" + rel.query;
        }

        if (!"".equals(rel.hash)) {
            path += rel.hash;
        }

        final String rval = hierPart + path;

        if ("".equals(rval)) {
            return "./";
        }
        return rval;
    }

    /**
     * Expands a language map.
     *
     * @param languageMap
     *            the language map to expand.
     *
     * @return the expanded language map.
     * @throws JsonLdError
     */
    static List<Object> expandLanguageMap(Map<String, Object> languageMap) throws JsonLdError {
        final List<Object> rval = new ArrayList<>();
        final List<String> keys = new ArrayList<>(languageMap.keySet());
        Collections.sort(keys); // lexicographically sort languages
        for (final String key : keys) {
            List<Object> val;
            if (!isArray(languageMap.get(key))) {
                val = new ArrayList<>();
                val.add(languageMap.get(key));
            } else {
                val = (List<Object>) languageMap.get(key);
            }
            for (final Object item : val) {
                if (!isString(item)) {
                    throw new JsonLdError(JsonLdError.Error.SYNTAX_ERROR);
                }
                final Map<String, Object> tmp = newMap();
                tmp.put("@value", item);
                tmp.put("@language", key.toLowerCase());
                rval.add(tmp);
            }
        }

        return rval;
    }

    /**
     * Throws an exception if the given value is not a valid @type value.
     *
     * @param v
     *            the value to check.
     * @throws JsonLdError
     */
    static boolean validateTypeValue(Object v) throws JsonLdError {
        if (v == null) {
            throw new NullPointerException("\"@type\" value cannot be null");
        }

        // must be a string, subject reference, or empty object
        if (v instanceof String
                || (v instanceof Map && (((Map<String, Object>) v).containsKey("@id") || ((Map<String, Object>) v).isEmpty()))) {
            return true;
        }

        // must be an array
        boolean isValid = false;
        if (v instanceof List) {
            isValid = true;
            for (final Object i : (List) v) {
                if (!(i instanceof String || i instanceof Map
                        && ((Map<String, Object>) i).containsKey("@id"))) {
                    isValid = false;
                    break;
                }
            }
        }

        if (!isValid) {
            throw new JsonLdError(JsonLdError.Error.SYNTAX_ERROR);
        }
        return true;
    }

    /**
     * Removes a base IRI from the given absolute IRI.
     *
     * @param base
     *            the base IRI.
     * @param iri
     *            the absolute IRI.
     *
     * @return the relative IRI if relative to base, otherwise the absolute IRI.
     */
    private static String removeBase(Object baseobj, String iri) {
        JsonLdUrl base;
        if (isString(baseobj)) {
            base = JsonLdUrl.parse((String) baseobj);
        } else {
            base = (JsonLdUrl) baseobj;
        }

        // establish base root
        String root = "";
        if (!"".equals(base.href)) {
            root += (base.protocol) + "//" + base.authority;
        }
        // support network-path reference with empty base
        else if (iri.indexOf("//") != 0) {
            root += "//";
        }

        // IRI not relative to base
        if (iri.indexOf(root) != 0) {
            return iri;
        }

        // remove root from IRI and parse remainder
        final JsonLdUrl rel = JsonLdUrl.parse(iri.substring(root.length()));

        // remove path segments that match
        final List<String> baseSegments = _split(base.normalizedPath, "/");
        final List<String> iriSegments = _split(rel.normalizedPath, "/");

        while (!baseSegments.isEmpty() && !iriSegments.isEmpty()) {
            if (!baseSegments.get(0).equals(iriSegments.get(0))) {
                break;
            }
            if (!baseSegments.isEmpty()) {
                baseSegments.remove(0);
            }
            if (!iriSegments.isEmpty()) {
                iriSegments.remove(0);
            }
        }

        // use '../' for each non-matching base segment
        String rval = "";
        if (!baseSegments.isEmpty()) {
            // don't count the last segment if it isn't a path (doesn't end in
            // '/')
            // don't count empty first segment, it means base began with '/'
            if (!base.normalizedPath.endsWith("/") || "".equals(baseSegments.get(0))) {
                baseSegments.remove(baseSegments.size() - 1);
            }
            for (int i = 0; i < baseSegments.size(); ++i) {
                rval += "../";
            }
        }

        // prepend remaining segments
        rval += _join(iriSegments, "/");

        // add query and hash
        if (!"".equals(rel.query)) {
            rval += "?" + rel.query;
        }
        if (!"".equals(rel.hash)) {
            rval += rel.hash;
        }

        if ("".equals(rval)) {
            rval = "./";
        }

        return rval;
    }

    /**
     * Removes the @preserve keywords as the last step of the framing algorithm.
     *
     * @param ctx
     *            the active context used to compact the input.
     * @param input
     *            the framed, compacted output.
     * @param options
     *            the compaction options used.
     *
     * @return the resulting output.
     * @throws JsonLdError
     */
    static Object removePreserve(Context ctx, Object input, JsonLdOptions opts) throws JsonLdError {
        // recurse through arrays
        if (isArray(input)) {
            final List<Object> output = new ArrayList<>();
            for (final Object i : (List<Object>) input) {
                final Object result = removePreserve(ctx, i, opts);
                // drop nulls from arrays
                if (result != null) {
                    output.add(result);
                }
            }
            input = output;
        } else if (isObject(input)) {
            // remove @preserve
            if (((Map<String, Object>) input).containsKey("@preserve")) {
                if ("@null".equals(((Map<String, Object>) input).get("@preserve"))) {
                    return null;
                }
                return ((Map<String, Object>) input).get("@preserve");
            }

            // skip @values
            if (isValue(input)) {
                return input;
            }

            // recurse through @lists
            if (isList(input)) {
                ((Map<String, Object>) input).put("@list",
                        removePreserve(ctx, ((Map<String, Object>) input).get("@list"), opts));
                return input;
            }

            // recurse through properties
            for (final String prop : ((Map<String, Object>) input).keySet()) {
                Object result = removePreserve(ctx, ((Map<String, Object>) input).get(prop), opts);
                final String container = ctx.getContainer(prop);
                if (opts.getCompactArrays() && isArray(result)
                        && ((List<Object>) result).size() == 1 && container == null) {
                    result = ((List<Object>) result).get(0);
                }
                ((Map<String, Object>) input).put(prop, result);
            }
        }
        return input;
    }

    /**
     * replicate javascript .join because i'm too lazy to keep doing it manually
     *
     * @param iriSegments
     * @param string
     * @return
     */
    private static String _join(List<String> list, String joiner) {
        String rval = "";
        if (!list.isEmpty()) {
            rval += list.get(0);
        }
        for (int i = 1; i < list.size(); i++) {
            rval += joiner + list.get(i);
        }
        return rval;
    }

    /**
     * replicates the functionality of javascript .split, which has different
     * results to java's String.split if there is a trailing /
     *
     * @param string
     * @param delim
     * @return
     */
    private static List<String> _split(String string, String delim) {
        final List<String> rval = new ArrayList<>(Arrays.asList(string.split(delim)));
        if (string.endsWith("/")) {
            // javascript .split includes a blank entry if the string ends with
            // the delimiter, java .split does not so we need to add it manually
            rval.add("");
        }
        return rval;
    }

    /**
     * Compares two strings first based on length and then lexicographically.
     *
     * @param a
     *            the first string.
     * @param b
     *            the second string.
     *
     * @return -1 if a < b, 1 if a > b, 0 if a == b.
     */
    static int compareShortestLeast(String a, String b) {
        if (a.length() < b.length()) {
            return -1;
        } else if (b.length() < a.length()) {
            return 1;
        }
        return Integer.signum(a.compareTo(b));
    }

    /**
     * Determines if the given value is a property of the given subject.
     *
     * @param subject
     *            the subject to check.
     * @param property
     *            the property to check.
     * @param value
     *            the value to check.
     *
     * @return true if the value exists, false if not.
     */
    static boolean hasValue(Map<String, Object> subject, String property, Object value) {
        boolean rval = false;
        if (hasProperty(subject, property)) {
            Object val = subject.get(property);
            final boolean isList = isList(val);
            if (isList || val instanceof List) {
                if (isList) {
                    val = ((Map<String, Object>) val).get("@list");
                }
                for (final Object i : (List) val) {
                    if (compareValues(value, i)) {
                        rval = true;
                        break;
                    }
                }
            } else if (!(value instanceof List)) {
                rval = compareValues(value, val);
            }
        }
        return rval;
    }

    private static boolean hasProperty(Map<String, Object> subject, String property) {
        boolean rval = false;
        if (subject.containsKey(property)) {
            final Object value = subject.get(property);
            rval = (!(value instanceof List) || !((List) value).isEmpty());
        }
        return rval;
    }

    /**
     * Compares two JSON-LD values for equality. Two JSON-LD values will be
     * considered equal if:
     *
     * 1. They are both primitives of the same type and value. 2. They are both @values
     * with the same @value, @type, and @language, OR 3. They both have @ids
     * they are the same.
     *
     * @param v1
     *            the first value.
     * @param v2
     *            the second value.
     *
     * @return true if v1 and v2 are considered equal, false if not.
     */
    static boolean compareValues(Object v1, Object v2) {
        if (v1.equals(v2)) {
            return true;
        }

        if (isValue(v1)
                && isValue(v2)
                && Obj.equals(((Map<String, Object>) v1).get("@value"),
                        ((Map<String, Object>) v2).get("@value"))
                        && Obj.equals(((Map<String, Object>) v1).get("@type"),
                                ((Map<String, Object>) v2).get("@type"))
                                && Obj.equals(((Map<String, Object>) v1).get("@language"),
                                        ((Map<String, Object>) v2).get("@language"))
                                        && Obj.equals(((Map<String, Object>) v1).get("@index"),
                                                ((Map<String, Object>) v2).get("@index"))) {
            return true;
        }

        if ((v1 instanceof Map && ((Map<String, Object>) v1).containsKey("@id"))
                && (v2 instanceof Map && ((Map<String, Object>) v2).containsKey("@id"))
                && ((Map<String, Object>) v1).get("@id").equals(
                        ((Map<String, Object>) v2).get("@id"))) {
            return true;
        }

        return false;
    }

    /**
     * Removes a value from a subject.
     *
     * @param subject
     *            the subject.
     * @param property
     *            the property that relates the value to the subject.
     * @param value
     *            the value to remove.
     * @param [options] the options to use: [propertyIsArray] true if the
     *        property is always an array, false if not (default: false).
     */
    static void removeValue(Map<String, Object> subject, String property, Map<String, Object> value) {
        removeValue(subject, property, value, false);
    }

    static void removeValue(Map<String, Object> subject, String property,
            Map<String, Object> value, boolean propertyIsArray) {
        // filter out value
        final List<Object> values = new ArrayList<>();
        if (subject.get(property) instanceof List) {
            for (final Object e : (List) subject.get(property)) {
                if (!value.equals(e)) {
                    values.add(value);
                }
            }
        } else {
            if (!value.equals(subject.get(property))) {
                values.add(subject.get(property));
            }
        }

        if (values.isEmpty()) {
            subject.remove(property);
        } else if (values.size() == 1 && !propertyIsArray) {
            subject.put(property, values.get(0));
        } else {
            subject.put(property, values);
        }
    }

    /**
     * Returns true if the given value is a blank node.
     *
     * @param v
     *            the value to check.
     *
     * @return true if the value is a blank node, false if not.
     */
    static boolean isBlankNode(Object v) {
        // Note: A value is a blank node if all of these hold true:
        // 1. It is an Object.
        // 2. If it has an @id key its value begins with '_:'.
        // 3. It has no keys OR is not a @value, @set, or @list.
        if (v instanceof Map) {
            if (((Map) v).containsKey("@id")) {
                return ((String) ((Map) v).get("@id")).startsWith("_:");
            } else {
                return ((Map) v).isEmpty()
                        || !(((Map) v).containsKey("@value") || ((Map) v).containsKey("@set") || ((Map) v)
                                .containsKey("@list"));
            }
        }
        return false;
    }

    /**
     * Finds all @context URLs in the given JSON-LD input.
     *
     * @param input
     *            the JSON-LD input.
     * @param urls
     *            a map of URLs (url => false/@contexts).
     * @param replace
     *            true to replace the URLs in the given input with the
     * @contexts from the urls map, false not to.
     *
     * @return true if new URLs to resolve were found, false if not.
     */
    private static boolean findContextUrls(Object input, Map<String, Object> urls, Boolean replace) {
        final int count = urls.size();
        if (input instanceof List) {
            for (final Object i : (List) input) {
                findContextUrls(i, urls, replace);
            }
            return count < urls.size();
        } else if (input instanceof Map) {
            for (final String key : ((Map<String, Object>) input).keySet()) {
                if (!"@context".equals(key)) {
                    findContextUrls(((Map) input).get(key), urls, replace);
                    continue;
                }

                // get @context
                final Object ctx = ((Map) input).get(key);

                // array @context
                if (ctx instanceof List) {
                    int length = ((List) ctx).size();
                    for (int i = 0; i < length; i++) {
                        Object _ctx = ((List) ctx).get(i);
                        if (_ctx instanceof String) {
                            // replace w/@context if requested
                            if (replace) {
                                _ctx = urls.get(_ctx);
                                if (_ctx instanceof List) {
                                    // add flattened context
                                    ((List) ctx).remove(i);
                                    ((List) ctx).addAll((Collection) _ctx);
                                    i += ((List) _ctx).size();
                                    length += ((List) _ctx).size();
                                } else {
                                    ((List) ctx).set(i, _ctx);
                                }
                            }
                            // @context JsonLdUrl found
                            else if (!urls.containsKey(_ctx)) {
                                urls.put((String) _ctx, Boolean.FALSE);
                            }
                        }
                    }
                }
                // string @context
                else if (ctx instanceof String) {
                    // replace w/@context if requested
                    if (replace) {
                        ((Map) input).put(key, urls.get(ctx));
                    }
                    // @context JsonLdUrl found
                    else if (!urls.containsKey(ctx)) {
                        urls.put((String) ctx, Boolean.FALSE);
                    }
                }
            }
            return count < urls.size();
        }
        return false;
    }

    static Object clone(Object value) {// throws
        // CloneNotSupportedException {
        Object rval = null;
        if (value instanceof Cloneable) {
            try {
                rval = value.getClass().getMethod("clone").invoke(value);
            } catch (final Exception e) {
                rval = e;
            }
        }
        if (rval == null || rval instanceof Exception) {
            // the object wasn't cloneable, or an error occured
            if (value == null || value instanceof String || value instanceof Number
                    || value instanceof Boolean) {
                // strings numbers and booleans are immutable
                rval = value;
            } else {
                // TODO: making this throw runtime exception so it doesn't have
                // to be caught
                // because simply it should never fail in the case of JSON-LD
                // and means that
                // the input JSON-LD is invalid
                throw new RuntimeException(new CloneNotSupportedException(
                        rval instanceof Exception ? ((Exception) rval).getMessage() : ""));
            }
        }
        return rval;
    }

    /**
     * Returns true if the given value is a JSON-LD Array
     *
     * @param v
     *            the value to check.
     * @return
     */
    static Boolean isArray(Object v) {
        return v instanceof List;
    }

    /**
     * Returns true if the given value is a JSON-LD List
     *
     * @param v
     *            the value to check.
     * @return
     */
    static Boolean isList(Object v) {
        return v instanceof Map && ((Map<String, Object>) v).containsKey("@list");
    }

    /**
     * Returns true if the given value is a JSON-LD Object
     *
     * @param v
     *            the value to check.
     * @return
     */
    static Boolean isObject(Object v) {
        return v instanceof Map;
    }

    /**
     * Returns true if the given value is a JSON-LD value
     *
     * @param v
     *            the value to check.
     * @return
     */
    static Boolean isValue(Object v) {
        return v instanceof Map && ((Map<String, Object>) v).containsKey("@value");
    }

    /**
     * Returns true if the given value is a JSON-LD string
     *
     * @param v
     *            the value to check.
     * @return
     */
    static Boolean isString(Object v) {
        // TODO: should this return true for arrays of strings as well?
        return v instanceof String;
    }
}