package com.github.jsonldjava.core;

import com.github.jsonldjava.core.JsonLdError.Error;
import com.github.jsonldjava.utils.Obj;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.util.*;

import static com.github.jsonldjava.core.JsonLdConsts.*;
import static com.github.jsonldjava.core.JsonLdUtils.isKeyword;
import static com.github.jsonldjava.utils.Obj.newMap;

/**
 * A container object to maintain state relating to JsonLdOptions and the
 * current Context, and push these into the relevant algorithms in
 * JsonLdProcessor as necessary.
 *
 * @author tristan
 */
public class JsonLdApi {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    JsonLdOptions opts;
    Object value = null;
    Context context = null;

    private Map<String, String> blankNodeMapping = new HashMap<>();

    /**
     * Constructs an empty JsonLdApi object using the default JsonLdOptions, and
     * without initialization.
     */
    public JsonLdApi() {
        this(new JsonLdOptions(""));
    }

    /**
     * Constructs a JsonLdApi object using the given object as the initial
     * JSON-LD object, and the given JsonLdOptions.
     *
     * @param input
     *            The initial JSON-LD object.
     * @param opts
     *            The JsonLdOptions to use.
     * @throws JsonLdError
     *             If there is an error initializing using the object and
     *             options.
     */
    public JsonLdApi(Object input, JsonLdOptions opts) throws JsonLdError {
        this(opts);
        initialize(input, null);
    }

    /**
     * Constructs a JsonLdApi object using the given object as the initial
     * JSON-LD object, the given context, and the given JsonLdOptions.
     *
     * @param input
     *            The initial JSON-LD object.
     * @param context
     *            The initial context.
     * @param opts
     *            The JsonLdOptions to use.
     * @throws JsonLdError
     *             If there is an error initializing using the object and
     *             options.
     */
    public JsonLdApi(Object input, Object context, JsonLdOptions opts) throws JsonLdError {
        this(opts);
        initialize(input, null);
    }

    /**
     * Constructs an empty JsonLdApi object using the given JsonLdOptions, and
     * without initialization. <br>
     * If the JsonLdOptions parameter is null, then the default options are
     * used.
     *
     * @param opts
     *            The JsonLdOptions to use.
     */
    public JsonLdApi(JsonLdOptions opts) {
        if (opts == null) {
            opts = new JsonLdOptions("");
        } else {
            this.opts = opts;
        }
    }

    /**
     * Initializes this object by cloning the input object using
     * {@link JsonLdUtils#clone(Object)}, and by parsing the context using
     * {@link Context#parse(Object)}.
     *
     * @param input
     *            The initial object, which is to be cloned and used in
     *            operations.
     * @param context
     *            The context object, which is to be parsed and used in
     *            operations.
     * @throws JsonLdError
     *             If there was an error cloning the object, or in parsing the
     *             context.
     */
    private void initialize(Object input, Object context) throws JsonLdError {
        if (input instanceof List || input instanceof Map) {
            this.value = JsonLdUtils.clone(input);
        }
        // TODO: string/IO input
        this.context = new Context(opts);
        if (context != null) {
            this.context = this.context.parse(context);
        }
    }

    /***
     * ____ _ _ _ _ _ _ / ___|___ _ __ ___ _ __ __ _ ___| |_ / \ | | __ _ ___ _
     * __(_) |_| |__ _ __ ___ | | / _ \| '_ ` _ \| '_ \ / _` |/ __| __| / _ \ |
     * |/ _` |/ _ \| '__| | __| '_ \| '_ ` _ \ | |__| (_) | | | | | | |_) | (_|
     * | (__| |_ / ___ \| | (_| | (_) | | | | |_| | | | | | | | | \____\___/|_|
     * |_| |_| .__/ \__,_|\___|\__| /_/ \_\_|\__, |\___/|_| |_|\__|_| |_|_| |_|
     * |_| |_| |___/
     */

    /**
     * Compaction Algorithm
     *
     * http://json-ld.org/spec/latest/json-ld-api/#compaction-algorithm
     *
     * @param activeCtx
     *            The Active Context
     * @param activeProperty
     *            The Active Property
     * @param element
     *            The current element
     * @param compactArrays
     *            True to compact arrays.
     * @return The compacted JSON-LD object.
     * @throws JsonLdError
     *             If there was an error during compaction.
     */
    public Object compact(Context activeCtx, String activeProperty, Object element,
            boolean compactArrays) throws JsonLdError {
        // 2)
        if (element instanceof List) {
            // 2.1)
            final List<Object> result = new ArrayList<>();
            // 2.2)
            for (final Object item : (List<Object>) element) {
                // 2.2.1)
                final Object compactedItem = compact(activeCtx, activeProperty, item, compactArrays);
                // 2.2.2)
                if (compactedItem != null) {
                    result.add(compactedItem);
                }
            }
            // 2.3)
            if (compactArrays && result.size() == 1
                    && activeCtx.getContainer(activeProperty) == null) {
                return result.get(0);
            }
            // 2.4)
            return result;
        }

        // 3)
        if (element instanceof Map) {
            // access helper
            final Map<String, Object> elem = (Map<String, Object>) element;

            // 4
            if (elem.containsKey("@value") || elem.containsKey("@id")) {
                final Object compactedValue = activeCtx.compactValue(activeProperty, elem);
                if (!(compactedValue instanceof Map || compactedValue instanceof List)) {
                    return compactedValue;
                }
            }
            // 5)
            final boolean insideReverse = "@reverse".equals(activeProperty);

            // 6)
            final Map<String, Object> result = newMap();
            // 7)
            final List<String> keys = new ArrayList<>(elem.keySet());
            Collections.sort(keys);
            for (final String expandedProperty : keys) {
                final Object expandedValue = elem.get(expandedProperty);

                // 7.1)
                if ("@id".equals(expandedProperty) || "@type".equals(expandedProperty)) {
                    Object compactedValue;

                    // 7.1.1)
                    if (expandedValue instanceof String) {
                        compactedValue = activeCtx.compactIri((String) expandedValue,
                                "@type".equals(expandedProperty));
                    }
                    // 7.1.2)
                    else {
                        final List<String> types = new ArrayList<>();
                        // 7.1.2.2)
                        for (final String expandedType : (List<String>) expandedValue) {
                            types.add(activeCtx.compactIri(expandedType, true));
                        }
                        // 7.1.2.3)
                        if (types.size() == 1) {
                            compactedValue = types.get(0);
                        } else {
                            compactedValue = types;
                        }
                    }

                    // 7.1.3)
                    final String alias = activeCtx.compactIri(expandedProperty, true);
                    // 7.1.4)
                    result.put(alias, compactedValue);
                    continue;
                    // TODO: old add value code, see if it's still relevant?
                    // addValue(rval, alias, compactedValue,
                    // isArray(compactedValue)
                    // && ((List<Object>) expandedValue).size() == 0);
                }

                // 7.2)
                if ("@reverse".equals(expandedProperty)) {
                    // 7.2.1)
                    final Map<String, Object> compactedValue = (Map<String, Object>) compact(
                            activeCtx, "@reverse", expandedValue, compactArrays);

                    // 7.2.2)
                    // Note: Must create a new set to avoid modifying the set we
                    // are iterating over
                    for (final String property : new HashSet<>(compactedValue.keySet())) {
                        final Object value = compactedValue.get(property);
                        // 7.2.2.1)
                        if (activeCtx.isReverseProperty(property)) {
                            // 7.2.2.1.1)
                            if (("@set".equals(activeCtx.getContainer(property)) || !compactArrays)
                                    && !(value instanceof List)) {
                                final List<Object> tmp = new ArrayList<>();
                                tmp.add(value);
                                result.put(property, tmp);
                            }
                            // 7.2.2.1.2)
                            if (!result.containsKey(property)) {
                                result.put(property, value);
                            }
                            // 7.2.2.1.3)
                            else {
                                if (!(result.get(property) instanceof List)) {
                                    final List<Object> tmp = new ArrayList<>();
                                    tmp.add(result.put(property, tmp));
                                }
                                if (value instanceof List) {
                                    ((List<Object>) result.get(property))
                                    .addAll((List<Object>) value);
                                } else {
                                    ((List<Object>) result.get(property)).add(value);
                                }
                            }
                            // 7.2.2.1.4)
                            compactedValue.remove(property);
                        }
                    }
                    // 7.2.3)
                    if (!compactedValue.isEmpty()) {
                        // 7.2.3.1)
                        final String alias = activeCtx.compactIri("@reverse", true);
                        // 7.2.3.2)
                        result.put(alias, compactedValue);
                    }
                    // 7.2.4)
                    continue;
                }

                // 7.3)
                if ("@index".equals(expandedProperty)
                        && "@index".equals(activeCtx.getContainer(activeProperty))) {
                    continue;
                }
                // 7.4)
                else if ("@index".equals(expandedProperty) || "@value".equals(expandedProperty)
                        || "@language".equals(expandedProperty)) {
                    // 7.4.1)
                    final String alias = activeCtx.compactIri(expandedProperty, true);
                    // 7.4.2)
                    result.put(alias, expandedValue);
                    continue;
                }

                // NOTE: expanded value must be an array due to expansion
                // algorithm.

                // 7.5)
                if (((List<Object>) expandedValue).isEmpty()) {
                    // 7.5.1)
                    final String itemActiveProperty = activeCtx.compactIri(expandedProperty,
                            expandedValue, true, insideReverse);
                    // 7.5.2)
                    if (!result.containsKey(itemActiveProperty)) {
                        result.put(itemActiveProperty, new ArrayList<>());
                    } else {
                        final Object value = result.get(itemActiveProperty);
                        if (!(value instanceof List)) {
                            final List<Object> tmp = new ArrayList<>();
                            tmp.add(value);
                            result.put(itemActiveProperty, tmp);
                        }
                    }
                }

                // 7.6)
                for (final Object expandedItem : (List<Object>) expandedValue) {
                    // 7.6.1)
                    final String itemActiveProperty = activeCtx.compactIri(expandedProperty,
                            expandedItem, true, insideReverse);
                    // 7.6.2)
                    final String container = activeCtx.getContainer(itemActiveProperty);

                    // get @list value if appropriate
                    final boolean isList = (expandedItem instanceof Map && ((Map<String, Object>) expandedItem)
                            .containsKey("@list"));
                    Object list = null;
                    if (isList) {
                        list = ((Map<String, Object>) expandedItem).get("@list");
                    }

                    // 7.6.3)
                    Object compactedItem = compact(activeCtx, itemActiveProperty, isList ? list
                            : expandedItem, compactArrays);

                    // 7.6.4)
                    if (isList) {
                        // 7.6.4.1)
                        if (!(compactedItem instanceof List)) {
                            final List<Object> tmp = new ArrayList<>();
                            tmp.add(compactedItem);
                            compactedItem = tmp;
                        }
                        // 7.6.4.2)
                        if (!"@list".equals(container)) {
                            // 7.6.4.2.1)
                            final Map<String, Object> wrapper = newMap();
                            // TODO: SPEC: no mention of vocab = true
                            wrapper.put(activeCtx.compactIri("@list", true), compactedItem);
                            compactedItem = wrapper;

                            // 7.6.4.2.2)
                            if (((Map<String, Object>) expandedItem).containsKey("@index")) {
                                ((Map<String, Object>) compactedItem).put(
                                        // TODO: SPEC: no mention of vocab =
                                        // true
                                        activeCtx.compactIri("@index", true),
                                        ((Map<String, Object>) expandedItem).get("@index"));
                            }
                        }
                        // 7.6.4.3)
                        else if (result.containsKey(itemActiveProperty)) {
                            throw new JsonLdError(Error.COMPACTION_TO_LIST_OF_LISTS,
                                    "There cannot be two list objects associated with an active property that has a container mapping");
                        }
                    }

                    // 7.6.5)
                    if ("@language".equals(container) || "@index".equals(container)) {
                        // 7.6.5.1)
                        Map<String, Object> mapObject;
                        if (result.containsKey(itemActiveProperty)) {
                            mapObject = (Map<String, Object>) result.get(itemActiveProperty);
                        } else {
                            mapObject = newMap();
                            result.put(itemActiveProperty, mapObject);
                        }

                        // 7.6.5.2)
                        if ("@language".equals(container)
                                && (compactedItem instanceof Map && ((Map<String, Object>) compactedItem)
                                        .containsKey("@value"))) {
                            compactedItem = ((Map<String, Object>) compactedItem).get("@value");
                        }

                        // 7.6.5.3)
                        final String mapKey = (String) ((Map<String, Object>) expandedItem)
                                .get(container);
                        // 7.6.5.4)
                        if (!mapObject.containsKey(mapKey)) {
                            mapObject.put(mapKey, compactedItem);
                        } else {
                            List<Object> tmp;
                            if (!(mapObject.get(mapKey) instanceof List)) {
                                tmp = new ArrayList<>();
                                tmp.add(mapObject.put(mapKey, tmp));
                            } else {
                                tmp = (List<Object>) mapObject.get(mapKey);
                            }
                            tmp.add(compactedItem);
                        }
                    }
                    // 7.6.6)
                    else {
                        // 7.6.6.1)
                        final Boolean check = (!compactArrays || "@set".equals(container)
                                || "@list".equals(container) || "@list".equals(expandedProperty) || "@graph"
                                .equals(expandedProperty))
                                && (!(compactedItem instanceof List));
                        if (check) {
                            final List<Object> tmp = new ArrayList<>();
                            tmp.add(compactedItem);
                            compactedItem = tmp;
                        }
                        // 7.6.6.2)
                        if (!result.containsKey(itemActiveProperty)) {
                            result.put(itemActiveProperty, compactedItem);
                        } else {
                            if (!(result.get(itemActiveProperty) instanceof List)) {
                                final List<Object> tmp = new ArrayList<>();
                                tmp.add(result.put(itemActiveProperty, tmp));
                            }
                            if (compactedItem instanceof List) {
                                ((List<Object>) result.get(itemActiveProperty))
                                .addAll((List<Object>) compactedItem);
                            } else {
                                ((List<Object>) result.get(itemActiveProperty)).add(compactedItem);
                            }
                        }

                    }
                }
            }
            // 8)
            return result;
        }

        // 2)
        return element;
    }

    /**
     * Compaction Algorithm
     *
     * http://json-ld.org/spec/latest/json-ld-api/#compaction-algorithm
     *
     * @param activeCtx
     *            The Active Context
     * @param activeProperty
     *            The Active Property
     * @param element
     *            The current element
     * @return The compacted JSON-LD object.
     * @throws JsonLdError
     *             If there was an error during compaction.
     */
    public Object compact(Context activeCtx, String activeProperty, Object element)
            throws JsonLdError {
        return compact(activeCtx, activeProperty, element, true);
    }

    /***
     * _____ _ _ _ _ _ _ | ____|_ ___ __ __ _ _ __ __| | / \ | | __ _ ___ _
     * __(_) |_| |__ _ __ ___ | _| \ \/ / '_ \ / _` | '_ \ / _` | / _ \ | |/ _`
     * |/ _ \| '__| | __| '_ \| '_ ` _ \ | |___ > <| |_) | (_| | | | | (_| | /
     * ___ \| | (_| | (_) | | | | |_| | | | | | | | | |_____/_/\_\ .__/ \__,_|_|
     * |_|\__,_| /_/ \_\_|\__, |\___/|_| |_|\__|_| |_|_| |_| |_| |_| |___/
     */

    /**
     * Expansion Algorithm
     *
     * http://json-ld.org/spec/latest/json-ld-api/#expansion-algorithm
     *
     * @param activeCtx
     *            The Active Context
     * @param activeProperty
     *            The Active Property
     * @param element
     *            The current element
     * @return The expanded JSON-LD object.
     * @throws JsonLdError
     *             If there was an error during expansion.
     */
    public Object expand(Context activeCtx, String activeProperty, Object element)
            throws JsonLdError {
        // 1)
        if (element == null) {
            return null;
        }

        // 3)
        if (element instanceof List) {
            // 3.1)
            final List<Object> result = new ArrayList<>();
            // 3.2)
            for (final Object item : (List<Object>) element) {
                // 3.2.1)
                final Object v = expand(activeCtx, activeProperty, item);
                // 3.2.2)
                if (("@list".equals(activeProperty) || "@list".equals(activeCtx
                        .getContainer(activeProperty)))
                        && (v instanceof List || (v instanceof Map && ((Map<String, Object>) v)
                                .containsKey("@list")))) {
                    throw new JsonLdError(Error.LIST_OF_LISTS, "lists of lists are not permitted.");
                }
                // 3.2.3)
                else if (v != null) {
                    if (v instanceof List) {
                        result.addAll((Collection<? extends Object>) v);
                    } else {
                        result.add(v);
                    }
                }
            }
            // 3.3)
            return result;
        }
        // 4)
        else if (element instanceof Map) {
            // access helper
            final Map<String, Object> elem = (Map<String, Object>) element;
            // 5)
            if (elem.containsKey("@context")) {
                activeCtx = activeCtx.parse(elem.get("@context"));
            }
            // 6)
            Map<String, Object> result = newMap();
            // 7)
            final List<String> keys = new ArrayList<>(elem.keySet());
            Collections.sort(keys);
            for (final String key : keys) {
                final Object value = elem.get(key);
                // 7.1)
                if (key.equals("@context")) {
                    continue;
                }
                // 7.2)
                final String expandedProperty = activeCtx.expandIri(key, false, true, null, null);
                Object expandedValue = null;
                // 7.3)
                if (expandedProperty == null
                        || (!expandedProperty.contains(":") && !isKeyword(expandedProperty))) {
                    continue;
                }
                // 7.4)
                if (isKeyword(expandedProperty)) {
                    // 7.4.1)
                    if ("@reverse".equals(activeProperty)) {
                        throw new JsonLdError(Error.INVALID_REVERSE_PROPERTY_MAP,
                                "a keyword cannot be used as a @reverse propery");
                    }
                    // 7.4.2)
                    if (result.containsKey(expandedProperty)) {
                        throw new JsonLdError(Error.COLLIDING_KEYWORDS, expandedProperty
                                + " already exists in result");
                    }
                    // 7.4.3)
                    if ("@id".equals(expandedProperty)) {
                        if (!(value instanceof String)) {
                            throw new JsonLdError(Error.INVALID_ID_VALUE,
                                    "value of @id must be a string");
                        }
                        expandedValue = activeCtx
                                .expandIri((String) value, true, false, null, null);
                    }
                    // 7.4.4)
                    else if ("@type".equals(expandedProperty)) {
                        if (value instanceof List) {
                            expandedValue = new ArrayList<String>();
                            for (final Object v : (List) value) {
                                if (!(v instanceof String)) {
                                    throw new JsonLdError(Error.INVALID_TYPE_VALUE,
                                            "@type value must be a string or array of strings");
                                }
                                ((List<String>) expandedValue).add(activeCtx.expandIri((String) v,
                                        true, true, null, null));
                            }
                        } else if (value instanceof String) {
                            expandedValue = activeCtx.expandIri((String) value, true, true, null,
                                    null);
                        }
                        // TODO: SPEC: no mention of empty map check
                        else if (value instanceof Map) {
                            if (!((Map<String, Object>) value).isEmpty()) {
                                throw new JsonLdError(Error.INVALID_TYPE_VALUE,
                                        "@type value must be a an empty object for framing");
                            }
                            expandedValue = value;
                        } else {
                            throw new JsonLdError(Error.INVALID_TYPE_VALUE,
                                    "@type value must be a string or array of strings");
                        }
                    }
                    // 7.4.5)
                    else if ("@graph".equals(expandedProperty)) {
                        expandedValue = expand(activeCtx, "@graph", value);
                    }
                    // 7.4.6)
                    else if ("@value".equals(expandedProperty)) {
                        if (value != null && (value instanceof Map || value instanceof List)) {
                            throw new JsonLdError(Error.INVALID_VALUE_OBJECT_VALUE, "value of "
                                    + expandedProperty + " must be a scalar or null");
                        }
                        expandedValue = value;
                        if (expandedValue == null) {
                            result.put("@value", null);
                            continue;
                        }
                    }
                    // 7.4.7)
                    else if ("@language".equals(expandedProperty)) {
                        if (!(value instanceof String)) {
                            throw new JsonLdError(Error.INVALID_LANGUAGE_TAGGED_STRING, "Value of "
                                    + expandedProperty + " must be a string");
                        }
                        expandedValue = ((String) value).toLowerCase();
                    }
                    // 7.4.8)
                    else if ("@index".equals(expandedProperty)) {
                        if (!(value instanceof String)) {
                            throw new JsonLdError(Error.INVALID_INDEX_VALUE, "Value of "
                                    + expandedProperty + " must be a string");
                        }
                        expandedValue = value;
                    }
                    // 7.4.9)
                    else if ("@list".equals(expandedProperty)) {
                        // 7.4.9.1)
                        if (activeProperty == null || "@graph".equals(activeProperty)) {
                            continue;
                        }
                        // 7.4.9.2)
                        expandedValue = expand(activeCtx, activeProperty, value);

                        // NOTE: step not in the spec yet
                        if (!(expandedValue instanceof List)) {
                            final List<Object> tmp = new ArrayList<>();
                            tmp.add(expandedValue);
                            expandedValue = tmp;
                        }

                        // 7.4.9.3)
                        for (final Object o : (List<Object>) expandedValue) {
                            if (o instanceof Map && ((Map<String, Object>) o).containsKey("@list")) {
                                throw new JsonLdError(Error.LIST_OF_LISTS,
                                        "A list may not contain another list");
                            }
                        }
                    }
                    // 7.4.10)
                    else if ("@set".equals(expandedProperty)) {
                        expandedValue = expand(activeCtx, activeProperty, value);
                    }
                    // 7.4.11)
                    else if ("@reverse".equals(expandedProperty)) {
                        if (!(value instanceof Map)) {
                            throw new JsonLdError(Error.INVALID_REVERSE_VALUE,
                                    "@reverse value must be an object");
                        }
                        // 7.4.11.1)
                        expandedValue = expand(activeCtx, "@reverse", value);
                        // NOTE: algorithm assumes the result is a map
                        // 7.4.11.2)
                        if (((Map<String, Object>) expandedValue).containsKey("@reverse")) {
                            final Map<String, Object> reverse = (Map<String, Object>) ((Map<String, Object>) expandedValue)
                                    .get("@reverse");
                            for (final Map.Entry<String, Object> stringObjectEntry : reverse.entrySet()) {
                                final Object item = stringObjectEntry.getValue();
                                // 7.4.11.2.1)
                                if (!result.containsKey(stringObjectEntry.getKey())) {
                                    result.put(stringObjectEntry.getKey(), new ArrayList<>());
                                }
                                // 7.4.11.2.2)
                                if (item instanceof List) {
                                    ((List<Object>) result.get(stringObjectEntry.getKey()))
                                    .addAll((List<Object>) item);
                                } else {
                                    ((List<Object>) result.get(stringObjectEntry.getKey())).add(item);
                                }
                            }
                        }
                        // 7.4.11.3)
                        if (((Map<String, Object>) expandedValue).size() > (((Map<String, Object>) expandedValue)
                                .containsKey("@reverse") ? 1 : 0)) {
                            // 7.4.11.3.1)
                            if (!result.containsKey("@reverse")) {
                                result.put("@reverse", newMap());
                            }
                            // 7.4.11.3.2)
                            final Map<String, Object> reverseMap = (Map<String, Object>) result
                                    .get("@reverse");
                            // 7.4.11.3.3)
                            for (final String property : ((Map<String, Object>) expandedValue)
                                    .keySet()) {
                                if ("@reverse".equals(property)) {
                                    continue;
                                }
                                // 7.4.11.3.3.1)
                                final List<Object> items = (List<Object>) ((Map<String, Object>) expandedValue)
                                        .get(property);
                                for (final Object item : items) {
                                    // 7.4.11.3.3.1.1)
                                    if (item instanceof Map
                                            && (((Map<String, Object>) item).containsKey("@value") || ((Map<String, Object>) item)
                                                    .containsKey("@list"))) {
                                        throw new JsonLdError(Error.INVALID_REVERSE_PROPERTY_VALUE);
                                    }
                                    // 7.4.11.3.3.1.2)
                                    if (!reverseMap.containsKey(property)) {
                                        reverseMap.put(property, new ArrayList<>());
                                    }
                                    // 7.4.11.3.3.1.3)
                                    ((List<Object>) reverseMap.get(property)).add(item);
                                }
                            }
                        }
                        // 7.4.11.4)
                        continue;
                    }
                    // TODO: SPEC no mention of @explicit etc in spec
                    else if ("@explicit".equals(expandedProperty)
                            || "@default".equals(expandedProperty)
                            || "@embed".equals(expandedProperty)
                            || "@embedChildren".equals(expandedProperty)
                            || "@omitDefault".equals(expandedProperty)) {
                        expandedValue = expand(activeCtx, expandedProperty, value);
                    }
                    // 7.4.12)
                    if (expandedValue != null) {
                        result.put(expandedProperty, expandedValue);
                    }
                    // 7.4.13)
                    continue;
                }
                // 7.5
                else if ("@language".equals(activeCtx.getContainer(key)) && value instanceof Map) {
                    // 7.5.1)
                    expandedValue = new ArrayList<>();
                    // 7.5.2)
                    for (final String language : ((Map<String, Object>) value).keySet()) {
                        Object languageValue = ((Map<String, Object>) value).get(language);
                        // 7.5.2.1)
                        if (!(languageValue instanceof List)) {
                            final Object tmp = languageValue;
                            languageValue = new ArrayList<>();
                            ((List<Object>) languageValue).add(tmp);
                        }
                        // 7.5.2.2)
                        for (final Object item : (List<Object>) languageValue) {
                            // 7.5.2.2.1)
                            if (!(item instanceof String)) {
                                throw new JsonLdError(Error.INVALID_LANGUAGE_MAP_VALUE, "Expected "
                                        + item.toString() + " to be a string");
                            }
                            // 7.5.2.2.2)
                            final Map<String, Object> tmp = newMap();
                            tmp.put("@value", item);
                            tmp.put("@language", language.toLowerCase());
                            ((List<Object>) expandedValue).add(tmp);
                        }
                    }
                }
                // 7.6)
                else if ("@index".equals(activeCtx.getContainer(key)) && value instanceof Map) {
                    // 7.6.1)
                    expandedValue = new ArrayList<>();
                    // 7.6.2)
                    final List<String> indexKeys = new ArrayList<>(
                            ((Map<String, Object>) value).keySet());
                    Collections.sort(indexKeys);
                    for (final String index : indexKeys) {
                        Object indexValue = ((Map<String, Object>) value).get(index);
                        // 7.6.2.1)
                        if (!(indexValue instanceof List)) {
                            final Object tmp = indexValue;
                            indexValue = new ArrayList<>();
                            ((List<Object>) indexValue).add(tmp);
                        }
                        // 7.6.2.2)
                        indexValue = expand(activeCtx, key, indexValue);
                        // 7.6.2.3)
                        for (final Map<String, Object> item : (List<Map<String, Object>>) indexValue) {
                            // 7.6.2.3.1)
                            if (!item.containsKey("@index")) {
                                item.put("@index", index);
                            }
                            // 7.6.2.3.2)
                            ((List<Object>) expandedValue).add(item);
                        }
                    }
                }
                // 7.7)
                else {
                    expandedValue = expand(activeCtx, key, value);
                }
                // 7.8)
                if (expandedValue == null) {
                    continue;
                }
                // 7.9)
                if ("@list".equals(activeCtx.getContainer(key))) {
                    if (!(expandedValue instanceof Map)
                            || !((Map<String, Object>) expandedValue).containsKey("@list")) {
                        Object tmp = expandedValue;
                        if (!(tmp instanceof List)) {
                            tmp = new ArrayList<>();
                            ((List<Object>) tmp).add(expandedValue);
                        }
                        expandedValue = newMap();
                        ((Map<String, Object>) expandedValue).put("@list", tmp);
                    }
                }
                // 7.10)
                if (activeCtx.isReverseProperty(key)) {
                    // 7.10.1)
                    if (!result.containsKey("@reverse")) {
                        result.put("@reverse", newMap());
                    }
                    // 7.10.2)
                    final Map<String, Object> reverseMap = (Map<String, Object>) result
                            .get("@reverse");
                    // 7.10.3)
                    if (!(expandedValue instanceof List)) {
                        final Object tmp = expandedValue;
                        expandedValue = new ArrayList<>();
                        ((List<Object>) expandedValue).add(tmp);
                    }
                    // 7.10.4)
                    for (final Object item : (List<Object>) expandedValue) {
                        // 7.10.4.1)
                        if (item instanceof Map
                                && (((Map<String, Object>) item).containsKey("@value") || ((Map<String, Object>) item)
                                        .containsKey("@list"))) {
                            throw new JsonLdError(Error.INVALID_REVERSE_PROPERTY_VALUE);
                        }
                        // 7.10.4.2)
                        if (!reverseMap.containsKey(expandedProperty)) {
                            reverseMap.put(expandedProperty, new ArrayList<>());
                        }
                        // 7.10.4.3)
                        if (item instanceof List) {
                            ((List<Object>) reverseMap.get(expandedProperty))
                            .addAll((List<Object>) item);
                        } else {
                            ((List<Object>) reverseMap.get(expandedProperty)).add(item);
                        }
                    }
                }
                // 7.11)
                else {
                    // 7.11.1)
                    if (!result.containsKey(expandedProperty)) {
                        result.put(expandedProperty, new ArrayList<>());
                    }
                    // 7.11.2)
                    if (expandedValue instanceof List) {
                        ((List<Object>) result.get(expandedProperty))
                        .addAll((List<Object>) expandedValue);
                    } else {
                        ((List<Object>) result.get(expandedProperty)).add(expandedValue);
                    }
                }
            }
            // 8)
            if (result.containsKey("@value")) {
                // 8.1)
                // TODO: is this method faster than just using containsKey for
                // each?
                final Set<String> keySet = new HashSet(result.keySet());
                keySet.remove("@value");
                keySet.remove("@index");
                final boolean langremoved = keySet.remove("@language");
                final boolean typeremoved = keySet.remove("@type");
                if ((langremoved && typeremoved) || !keySet.isEmpty()) {
                    throw new JsonLdError(Error.INVALID_VALUE_OBJECT,
                            "value object has unknown keys");
                }
                // 8.2)
                final Object rval = result.get("@value");
                if (rval == null) {
                    // nothing else is possible with result if we set it to
                    // null, so simply return it
                    return null;
                }
                // 8.3)
                if (!(rval instanceof String) && result.containsKey("@language")) {
                    throw new JsonLdError(Error.INVALID_LANGUAGE_TAGGED_VALUE,
                            "when @language is used, @value must be a string");
                }
                // 8.4)
                else if (result.containsKey("@type")) {
                    // TODO: is this enough for "is an IRI"
                    if (!(result.get("@type") instanceof String)
                            || ((String) result.get("@type")).startsWith("_:")
                            || !((String) result.get("@type")).contains(":")) {
                        throw new JsonLdError(Error.INVALID_TYPED_VALUE,
                                "value of @type must be an IRI");
                    }
                }
            }
            // 9)
            else if (result.containsKey("@type")) {
                final Object rtype = result.get("@type");
                if (!(rtype instanceof List)) {
                    final List<Object> tmp = new ArrayList<>();
                    tmp.add(rtype);
                    result.put("@type", tmp);
                }
            }
            // 10)
            else if (result.containsKey("@set") || result.containsKey("@list")) {
                // 10.1)
                if (result.size() > (result.containsKey("@index") ? 2 : 1)) {
                    throw new JsonLdError(Error.INVALID_SET_OR_LIST_OBJECT,
                            "@set or @list may only contain @index");
                }
                // 10.2)
                if (result.containsKey("@set")) {
                    // result becomes an array here, thus the remaining checks
                    // will never be true from here on
                    // so simply return the value rather than have to make
                    // result an object and cast it with every
                    // other use in the function.
                    return result.get("@set");
                }
            }
            // 11)
            if (result.containsKey("@language") && result.size() == 1) {
                result = null;
            }
            // 12)
            if (activeProperty == null || "@graph".equals(activeProperty)) {
                // 12.1)
                if (result != null
                        && (result.isEmpty() || result.containsKey("@value") || result
                        .containsKey("@list"))) {
                    result = null;
                }
                // 12.2)
                else if (result != null && result.containsKey("@id") && result.size() == 1) {
                    result = null;
                }
            }
            // 13)
            return result;
        }
        // 2) If element is a scalar
        else {
            // 2.1)
            if (activeProperty == null || "@graph".equals(activeProperty)) {
                return null;
            }
            return activeCtx.expandValue(activeProperty, element);
        }
    }

    /**
     * Expansion Algorithm
     *
     * http://json-ld.org/spec/latest/json-ld-api/#expansion-algorithm
     *
     * @param activeCtx
     *            The Active Context
     * @param element
     *            The current element
     * @return The expanded JSON-LD object.
     * @throws JsonLdError
     *             If there was an error during expansion.
     */
    public Object expand(Context activeCtx, Object element) throws JsonLdError {
        return expand(activeCtx, null, element);
    }

    /***
     * _____ _ _ _ _ _ _ _ _ | ___| | __ _| |_| |_ ___ _ __ / \ | | __ _ ___ _
     * __(_) |_| |__ _ __ ___ | |_ | |/ _` | __| __/ _ \ '_ \ / _ \ | |/ _` |/ _
     * \| '__| | __| '_ \| '_ ` _ \ | _| | | (_| | |_| || __/ | | | / ___ \| |
     * (_| | (_) | | | | |_| | | | | | | | | |_| |_|\__,_|\__|\__\___|_| |_| /_/
     * \_\_|\__, |\___/|_| |_|\__|_| |_|_| |_| |_| |___/
     */

    void generateNodeMap(Object element, Map<String, Object> nodeMap) throws JsonLdError {
        generateNodeMap(element, nodeMap, "@default", null, null, null);
    }

    void generateNodeMap(Object element, Map<String, Object> nodeMap, String activeGraph)
            throws JsonLdError {
        generateNodeMap(element, nodeMap, activeGraph, null, null, null);
    }

    void generateNodeMap(Object element, Map<String, Object> nodeMap, String activeGraph,
            Object activeSubject, String activeProperty, Map<String, Object> list)
                    throws JsonLdError {
        // 1)
        if (element instanceof List) {
            // 1.1)
            for (final Object item : (List<Object>) element) {
                generateNodeMap(item, nodeMap, activeGraph, activeSubject, activeProperty, list);
            }
            return;
        }

        // for convenience
        final Map<String, Object> elem = (Map<String, Object>) element;

        // 2)
        if (!nodeMap.containsKey(activeGraph)) {
            nodeMap.put(activeGraph, newMap());
        }
        final Map<String, Object> graph = (Map<String, Object>) nodeMap.get(activeGraph);
        Map<String, Object> node = (Map<String, Object>) (activeSubject == null ? null : graph
                .get(activeSubject));
        if (activeSubject != null && activeSubject.toString().startsWith("_:")) {
            String old = blankNodeMapping.get(activeSubject.toString());
            blankNodeMapping.put(activeSubject.toString(), old + generateBlankNodeId(activeProperty, element));
        }
        // 3)
        if (elem.containsKey("@type")) {
            // 3.1)
            List<String> oldTypes;
            final List<String> newTypes = new ArrayList<>();
            if (elem.get("@type") instanceof List) {
                oldTypes = (List<String>) elem.get("@type");
            } else {
                oldTypes = new ArrayList<>();
                oldTypes.add((String) elem.get("@type"));
            }
            for (final String item : oldTypes) {
                if (item.startsWith("_:")) {
                    newTypes.add(generateBlankNodeIdentifier(item));
                } else {
                    newTypes.add(item);
                }
            }
            if (elem.get("@type") instanceof List) {
                elem.put("@type", newTypes);
            } else {
                elem.put("@type", newTypes.get(0));
            }
        }

        // 4)
        if (elem.containsKey("@value")) {
            // 4.1)
            if (list == null) {
                JsonLdUtils.mergeValue(node, activeProperty, elem);
            }
            // 4.2)
            else {
                JsonLdUtils.mergeValue(list, "@list", elem);
            }
        }

        // 5)
        else if (elem.containsKey("@list")) {
            // 5.1)
            final Map<String, Object> result = newMap("@list", new ArrayList<>());
            // 5.2)
            // for (final Object item : (List<Object>) elem.get("@list")) {
            // generateNodeMap(item, nodeMap, activeGraph, activeSubject,
            // activeProperty, result);
            // }
            generateNodeMap(elem.get("@list"), nodeMap, activeGraph, activeSubject, activeProperty,
                    result);
            // 5.3)
            JsonLdUtils.mergeValue(node, activeProperty, result);
        }

        // 6)
        else {
            // 6.1)
            String id = (String) elem.remove("@id");
            if (id != null) {
                if (id.startsWith("_:")) {
                    id = generateBlankNodeIdentifier(id);
                }
            }
            // 6.2)
            else {
                id = generateBlankNodeIdentifier(null);
            }
            // 6.3)
            if (!graph.containsKey(id)) {
                final Map<String, Object> tmp = newMap("@id", id);
                graph.put(id, tmp);
                if (id.startsWith("_:")) {
                    blankNodeMapping.put(id, id);
                }
            }
            // 6.4) TODO: SPEC this line is asked for by the spec, but it breaks
            // various tests
            // node = (Map<String, Object>) graph.get(id);
            // 6.5)
            if (activeSubject instanceof Map) {
                // 6.5.1)
                JsonLdUtils.mergeValue((Map<String, Object>) graph.get(id), activeProperty,
                        activeSubject);
            }
            // 6.6)
            else if (activeProperty != null) {
                final Map<String, Object> reference = newMap("@id", id);
                // 6.6.2)
                if (list == null) {
                    // 6.6.2.1+2)
                    JsonLdUtils.mergeValue(node, activeProperty, reference);
                }
                // 6.6.3) TODO: SPEC says to add ELEMENT to @list member, should
                // be REFERENCE
                else {
                    JsonLdUtils.mergeValue(list, "@list", reference);
                }
            }
            // TODO: SPEC this is removed in the spec now, but it's still needed
            // (see 6.4)
            node = (Map<String, Object>) graph.get(id);
            // 6.7)
            if (elem.containsKey("@type")) {
                for (final Object type : (List<Object>) elem.remove("@type")) {
                    JsonLdUtils.mergeValue(node, "@type", type);
                }
            }
            // 6.8)
            if (elem.containsKey("@index")) {
                final Object elemIndex = elem.remove("@index");
                if (node.containsKey("@index")) {
                    if (!JsonLdUtils.deepCompare(node.get("@index"), elemIndex)) {
                        throw new JsonLdError(Error.CONFLICTING_INDEXES);
                    }
                } else {
                    node.put("@index", elemIndex);
                }
            }
            // 6.9)
            if (elem.containsKey("@reverse")) {
                // 6.9.1)
                final Map<String, Object> referencedNode = newMap("@id", id);
                // 6.9.2+6.9.4)
                final Map<String, Object> reverseMap = (Map<String, Object>) elem
                        .remove("@reverse");
                // 6.9.3)
                for (final Map.Entry<String, Object> stringObjectEntry : reverseMap.entrySet()) {
                    final List<Object> values = (List<Object>) stringObjectEntry.getValue();
                    // 6.9.3.1)
                    for (final Object value : values) {
                        // 6.9.3.1.1)
                        generateNodeMap(value, nodeMap, activeGraph, referencedNode, stringObjectEntry.getKey(), null);
                    }
                }
            }
            // 6.10)
            if (elem.containsKey("@graph")) {
                generateNodeMap(elem.remove("@graph"), nodeMap, id, null, null, null);
            }
            // 6.11)
            final List<String> keys = new ArrayList<>(elem.keySet());
            Collections.sort(keys);
            for (String property : keys) {
                final Object value = elem.get(property);
                // 6.11.1)
                if (property.startsWith("_:")) {
                    property = generateBlankNodeIdentifier(property);
                }
                // 6.11.2)
                if (!node.containsKey(property)) {
                    node.put(property, new ArrayList<>());
                }
                // 6.11.3)
                generateNodeMap(value, nodeMap, activeGraph, id, property, null);
            }
        }
    }

    private String generateBlankNodeId(String property, Object elem) {
        StringBuilder builder = new StringBuilder();
        builder.append("_").append(DigestUtils.sha256Hex(property));
        if (elem instanceof Map) {
            Map element = (Map) elem;
            Object atValue = element.get("@value");
            if (atValue != null) {
                builder.append("_").append(DigestUtils.sha256Hex(atValue.toString()));
            }
        }
        return builder.toString();
    }

    /**
     * Blank Node identifier map specified in:
     *
     * http://www.w3.org/TR/json-ld-api/#generate-blank-node-identifier
     */
    private final Map<String, String> blankNodeIdentifierMap = new LinkedHashMap<>();

    /**
     * Generates a blank node identifier for the given key using the algorithm
     * specified in:
     *
     * http://www.w3.org/TR/json-ld-api/#generate-blank-node-identifier
     *
     * @param id
     *            The id, or null to generate a fresh, unused, blank node
     *            identifier.
     * @return A blank node identifier based on id if it was not null, or a
     *         fresh, unused, blank node identifier if it was null.
     */
    String generateBlankNodeIdentifier(String id) {
        if (id != null && blankNodeIdentifierMap.containsKey(id)) {
            return blankNodeIdentifierMap.get(id);
        }
        final String bnid = "_:b" + UUID.randomUUID();
        if (id != null) {
            blankNodeIdentifierMap.put(id, bnid);
        }
        return bnid;
    }

    /**
     * Generates a fresh, unused, blank node identifier using the algorithm
     * specified in:
     *
     * http://www.w3.org/TR/json-ld-api/#generate-blank-node-identifier
     *
     * @return A fresh, unused, blank node identifier.
     */
    String generateBlankNodeIdentifier() {
        return generateBlankNodeIdentifier(null);
    }

    /***
     * _____ _ _ _ _ _ _ | ___| __ __ _ _ __ ___ (_)_ __ __ _ / \ | | __ _ ___ _
     * __(_) |_| |__ _ __ ___ | |_ | '__/ _` | '_ ` _ \| | '_ \ / _` | / _ \ |
     * |/ _` |/ _ \| '__| | __| '_ \| '_ ` _ \ | _|| | | (_| | | | | | | | | | |
     * (_| | / ___ \| | (_| | (_) | | | | |_| | | | | | | | | |_| |_| \__,_|_|
     * |_| |_|_|_| |_|\__, | /_/ \_\_|\__, |\___/|_| |_|\__|_| |_|_| |_| |_|
     * |___/ |___/
     */

    private class FramingContext {
        public boolean embed;
        public boolean explicit;
        public boolean omitDefault;

        public FramingContext() {
            embed = true;
            explicit = false;
            omitDefault = false;
            embeds = null;
        }

        public FramingContext(JsonLdOptions opts) {
            this();
            if (opts.getEmbed() != null) {
                this.embed = opts.getEmbed();
            }
            if (opts.getExplicit() != null) {
                this.explicit = opts.getExplicit();
            }
            if (opts.getOmitDefault() != null) {
                this.omitDefault = opts.getOmitDefault();
            }
        }

        public Map<String, EmbedNode> embeds = null;
    }

    private class EmbedNode {
        public Object parent = null;
        public String property = null;
    }

    private Map<String, Object> nodeMap;

    /**
     * Performs JSON-LD <a
     * href="http://json-ld.org/spec/latest/json-ld-framing/">framing</a>.
     *
     * @param input
     *            the expanded JSON-LD to frame.
     * @param frame
     *            the expanded JSON-LD frame to use.
     * @return the framed output.
     * @throws JsonLdError
     *             If the framing was not successful.
     */
    public List<Object> frame(Object input, List<Object> frame) throws JsonLdError {
        // create framing state
        final FramingContext state = new FramingContext(this.opts);

        // use tree map so keys are sotred by default
        final Map<String, Object> nodes = new TreeMap<>();
        generateNodeMap(input, nodes);
        this.nodeMap = (Map<String, Object>) nodes.get("@default");

        final List<Object> framed = new ArrayList<>();
        // NOTE: frame validation is done by the function not allowing anything
        // other than list to me passed
        frame(state,
                this.nodeMap,
                (frame != null && !frame.isEmpty() ? (Map<String, Object>) frame.get(0) : newMap()),
                framed, null);

        return framed;
    }

    /**
     * Frames subjects according to the given frame.
     *
     * @param state
     *            the current framing state.
     * @param subjects
     *            the subjects to filter.
     * @param frame
     *            the frame.
     * @param parent
     *            the parent subject or top-level array.
     * @param property
     *            the parent property, initialized to null.
     * @throws JsonLdError
     *             If there was an error during framing.
     */
    private void frame(FramingContext state, Map<String, Object> nodes, Map<String, Object> frame,
            Object parent, String property) throws JsonLdError {

        // filter out subjects that match the frame
        final Map<String, Object> matches = filterNodes(state, nodes, frame);

        // get flags for current frame
        Boolean embedOn = getFrameFlag(frame, "@embed", state.embed);
        final Boolean explicicOn = getFrameFlag(frame, "@explicit", state.explicit);

        // add matches to output
        final List<String> ids = new ArrayList<>(matches.keySet());
        Collections.sort(ids);
        for (final String id : ids) {
            if (property == null) {
                state.embeds = new LinkedHashMap<>();
            }

            // start output
            final Map<String, Object> output = newMap();
            output.put("@id", id);

            // prepare embed meta info
            final EmbedNode embeddedNode = new EmbedNode();
            embeddedNode.parent = parent;
            embeddedNode.property = property;

            // if embed is on and there is an existing embed
            if (embedOn && state.embeds.containsKey(id)) {
                final EmbedNode existing = state.embeds.get(id);
                embedOn = false;

                if (existing.parent instanceof List) {
                    for (final Object p : (List<Object>) existing.parent) {
                        if (JsonLdUtils.compareValues(output, p)) {
                            embedOn = true;
                            break;
                        }
                    }
                }
                // existing embed's parent is an object
                else {
                    if (((Map<String, Object>) existing.parent).containsKey(existing.property)) {
                        for (final Object v : (List<Object>) ((Map<String, Object>) existing.parent)
                                .get(existing.property)) {
                            if (v instanceof Map
                                    && Obj.equals(id, ((Map<String, Object>) v).get("@id"))) {
                                embedOn = true;
                                break;
                            }
                        }
                    }
                }

                // existing embed has already been added, so allow an overwrite
                if (embedOn) {
                    removeEmbed(state, id);
                }
            }

            // not embedding, add output without any other properties
            if (!embedOn) {
                addFrameOutput(state, parent, property, output);
            } else {
                // add embed meta info
                state.embeds.put(id, embeddedNode);

                // iterate over subject properties
                final Map<String, Object> element = (Map<String, Object>) matches.get(id);
                List<String> props = new ArrayList<>(element.keySet());
                Collections.sort(props);
                for (final String prop : props) {

                    // copy keywords to output
                    if (isKeyword(prop)) {
                        output.put(prop, JsonLdUtils.clone(element.get(prop)));
                        continue;
                    }

                    // if property isn't in the frame
                    if (!frame.containsKey(prop)) {
                        // if explicit is off, embed values
                        if (!explicicOn) {
                            embedValues(state, element, prop, output);
                        }
                        continue;
                    }

                    // add objects
                    final List<Object> value = (List<Object>) element.get(prop);

                    for (final Object item : value) {

                        // recurse into list
                        if ((item instanceof Map)
                                && ((Map<String, Object>) item).containsKey("@list")) {
                            // add empty list
                            final Map<String, Object> list = newMap();
                            list.put("@list", new ArrayList<>());
                            addFrameOutput(state, output, prop, list);

                            // add list objects
                            for (final Object listitem : (List<Object>) ((Map<String, Object>) item)
                                    .get("@list")) {
                                // recurse into subject reference
                                if (JsonLdUtils.isNodeReference(listitem)) {
                                    final Map<String, Object> tmp = newMap();
                                    final String itemid = (String) ((Map<String, Object>) listitem)
                                            .get("@id");
                                    // TODO: nodes may need to be node_map,
                                    // which is global
                                    tmp.put(itemid, this.nodeMap.get(itemid));
                                    frame(state, tmp,
                                            (Map<String, Object>) ((List<Object>) frame.get(prop))
                                            .get(0), list, "@list");
                                } else {
                                    // include other values automatcially (TODO:
                                    // may need JsonLdUtils.clone(n))
                                    addFrameOutput(state, list, "@list", listitem);
                                }
                            }
                        }

                        // recurse into subject reference
                        else if (JsonLdUtils.isNodeReference(item)) {
                            final Map<String, Object> tmp = newMap();
                            final String itemid = (String) ((Map<String, Object>) item).get("@id");
                            // TODO: nodes may need to be node_map, which is
                            // global
                            tmp.put(itemid, this.nodeMap.get(itemid));
                            frame(state, tmp,
                                    (Map<String, Object>) ((List<Object>) frame.get(prop)).get(0),
                                    output, prop);
                        } else {
                            // include other values automatically (TODO: may
                            // need JsonLdUtils.clone(o))
                            addFrameOutput(state, output, prop, item);
                        }
                    }
                }

                // handle defaults
                props = new ArrayList<>(frame.keySet());
                Collections.sort(props);
                for (final String prop : props) {
                    // skip keywords
                    if (isKeyword(prop)) {
                        continue;
                    }

                    final List<Object> pf = (List<Object>) frame.get(prop);
                    Map<String, Object> propertyFrame = !pf.isEmpty() ? (Map<String, Object>) pf
                            .get(0) : null;
                            if (propertyFrame == null) {
                                propertyFrame = newMap();
                            }
                            final boolean omitDefaultOn = getFrameFlag(propertyFrame, "@omitDefault",
                                    state.omitDefault);
                            if (!omitDefaultOn && !output.containsKey(prop)) {
                                Object def = "@null";
                                if (propertyFrame.containsKey("@default")) {
                                    def = JsonLdUtils.clone(propertyFrame.get("@default"));
                                }
                                if (!(def instanceof List)) {
                                    final List<Object> tmp = new ArrayList<>();
                                    tmp.add(def);
                                    def = tmp;
                                }
                                final Map<String, Object> tmp1 = newMap("@preserve", def);
                                final List<Object> tmp2 = new ArrayList<>();
                                tmp2.add(tmp1);
                                output.put(prop, tmp2);
                            }
                }

                // add output to parent
                addFrameOutput(state, parent, property, output);
            }
        }
    }

    private Boolean getFrameFlag(Map<String, Object> frame, String name, boolean thedefault) {
        Object value = frame.get(name);
        if (value instanceof List) {
            if (!((List<Object>) value).isEmpty()) {
                value = ((List<Object>) value).get(0);
            }
        }
        if (value instanceof Map && ((Map<String, Object>) value).containsKey("@value")) {
            value = ((Map<String, Object>) value).get("@value");
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return thedefault;
    }

    /**
     * Removes an existing embed.
     *
     * @param state
     *            the current framing state.
     * @param id
     *            the @id of the embed to remove.
     */
    private static void removeEmbed(FramingContext state, String id) {
        // get existing embed
        final Map<String, EmbedNode> embeds = state.embeds;
        final EmbedNode embed = embeds.get(id);
        final Object parent = embed.parent;
        final String property = embed.property;

        // create reference to replace embed
        final Map<String, Object> node = newMap("@id", id);

        // remove existing embed
        if (JsonLdUtils.isNode(parent)) {
            // replace subject with reference
            final List<Object> newvals = new ArrayList<>();
            final List<Object> oldvals = (List<Object>) ((Map<String, Object>) parent)
                    .get(property);
            for (final Object v : oldvals) {
                if (v instanceof Map && Obj.equals(((Map<String, Object>) v).get("@id"), id)) {
                    newvals.add(node);
                } else {
                    newvals.add(v);
                }
            }
            ((Map<String, Object>) parent).put(property, newvals);
        }
        // recursively remove dependent dangling embeds
        removeDependents(embeds, id);
    }

    private static void removeDependents(Map<String, EmbedNode> embeds, String id) {
        // get embed keys as a separate array to enable deleting keys in map
        for (final Map.Entry<String, EmbedNode> stringEmbedNodeEntry : embeds.entrySet()) {
            final EmbedNode e = stringEmbedNodeEntry.getValue();
            final Object p = e.parent != null ? e.parent : newMap();
            if (!(p instanceof Map)) {
                continue;
            }
            final String pid = (String) ((Map<String, Object>) p).get("@id");
            if (Obj.equals(id, pid)) {
                embeds.remove(stringEmbedNodeEntry.getKey());
                removeDependents(embeds, stringEmbedNodeEntry.getKey());
            }
        }
    }

    private Map<String, Object> filterNodes(FramingContext state, Map<String, Object> nodes,
            Map<String, Object> frame) throws JsonLdError {
        final Map<String, Object> rval = newMap();
        for (final Map.Entry<String, Object> stringObjectEntry : nodes.entrySet()) {
            final Map<String, Object> element = (Map<String, Object>) stringObjectEntry.getValue();
            if (element != null && filterNode(state, element, frame)) {
                rval.put(stringObjectEntry.getKey(), element);
            }
        }
        return rval;
    }

    private boolean filterNode(FramingContext state, Map<String, Object> node,
            Map<String, Object> frame) throws JsonLdError {
        final Object types = frame.get("@type");
        if (types != null) {
            if (!(types instanceof List)) {
                throw new JsonLdError(Error.SYNTAX_ERROR, "frame @type must be an array");
            }
            Object nodeTypes = node.get("@type");
            if (nodeTypes == null) {
                nodeTypes = new ArrayList<>();
            } else if (!(nodeTypes instanceof List)) {
                throw new JsonLdError(Error.SYNTAX_ERROR, "node @type must be an array");
            }
            if (((List<Object>) types).size() == 1 && ((List<Object>) types).get(0) instanceof Map
                    && ((Map<String, Object>) ((List<Object>) types).get(0)).isEmpty()) {
                return !((List<Object>) nodeTypes).isEmpty();
            } else {
                for (final Object i : (List<Object>) nodeTypes) {
                    for (final Object j : (List<Object>) types) {
                        if (JsonLdUtils.deepCompare(i, j)) {
                            return true;
                        }
                    }
                }
                return false;
            }
        } else {
            for (final String key : frame.keySet()) {
                if ("@id".equals(key) || !isKeyword(key) && !(node.containsKey(key))) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Adds framing output to the given parent.
     *
     * @param state
     *            the current framing state.
     * @param parent
     *            the parent to add to.
     * @param property
     *            the parent property.
     * @param output
     *            the output to add.
     */
    private static void addFrameOutput(FramingContext state, Object parent, String property,
            Object output) {
        if (parent instanceof Map) {
            List<Object> prop = (List<Object>) ((Map<String, Object>) parent).get(property);
            if (prop == null) {
                prop = new ArrayList<>();
                ((Map<String, Object>) parent).put(property, prop);
            }
            prop.add(output);
        } else {
            ((List) parent).add(output);
        }
    }

    /**
     * Embeds values for the given subject and property into the given output
     * during the framing algorithm.
     *
     * @param state
     *            the current framing state.
     * @param element
     *            the subject.
     * @param property
     *            the property.
     * @param output
     *            the output.
     */
    private void embedValues(FramingContext state, Map<String, Object> element, String property,
            Object output) {
        // embed subject properties in output
        final List<Object> objects = (List<Object>) element.get(property);
        for (Object o : objects) {
            // handle subject reference
            if (JsonLdUtils.isNodeReference(o)) {
                final String sid = (String) ((Map<String, Object>) o).get("@id");

                // embed full subject if isn't already embedded
                if (!state.embeds.containsKey(sid)) {
                    // add embed
                    final EmbedNode embed = new EmbedNode();
                    embed.parent = output;
                    embed.property = property;
                    state.embeds.put(sid, embed);

                    // recurse into subject
                    o = newMap();
                    Map<String, Object> s = (Map<String, Object>) this.nodeMap.get(sid);
                    if (s == null) {
                        s = newMap("@id", sid);
                    }
                    for (final Map.Entry<String, Object> stringObjectEntry : s.entrySet()) {
                        // copy keywords
                        if (isKeyword(stringObjectEntry.getKey())) {
                            ((Map<String, Object>) o).put(stringObjectEntry.getKey(), JsonLdUtils.clone(stringObjectEntry.getValue()));
                            continue;
                        }
                        embedValues(state, s, stringObjectEntry.getKey(), o);
                    }
                }
                addFrameOutput(state, output, property, o);
            }
            // copy non-subject value
            else {
                addFrameOutput(state, output, property, JsonLdUtils.clone(o));
            }
        }
    }

    /***
     * ____ _ __ ____ ____ _____ _ _ _ _ _ / ___|___ _ ____ _____ _ __| |_ / _|_
     * __ ___ _ __ ___ | _ \| _ \| ___| / \ | | __ _ ___ _ __(_) |_| |__ _ __
     * ___ | | / _ \| '_ \ \ / / _ \ '__| __| | |_| '__/ _ \| '_ ` _ \ | |_) | |
     * | | |_ / _ \ | |/ _` |/ _ \| '__| | __| '_ \| '_ ` _ \ | |__| (_) | | | \
     * V / __/ | | |_ | _| | | (_) | | | | | | | _ <| |_| | _| / ___ \| | (_| |
     * (_) | | | | |_| | | | | | | | | \____\___/|_| |_|\_/ \___|_| \__| |_| |_|
     * \___/|_| |_| |_| |_| \_\____/|_| /_/ \_\_|\__, |\___/|_| |_|\__|_| |_|_|
     * |_| |_| |___/
     */

    /**
     * Helper class for node usages
     *
     * @author tristan
     */
    private class UsagesNode {
        public UsagesNode(NodeMapNode node, String property, Map<String, Object> value) {
            this.node = node;
            this.property = property;
            this.value = value;
        }

        public NodeMapNode node = null;
        public String property = null;
        public Map<String, Object> value = null;
    }

    private class NodeMapNode extends LinkedHashMap<String, Object> {
        public List<UsagesNode> usages = new ArrayList();

        public NodeMapNode(String id) {
            super();
            this.put("@id", id);
        }

        // helper fucntion for 4.3.3
        public boolean isWellFormedListNode() {
            if (usages.size() != 1) {
                return false;
            }
            int keys = 0;
            if (containsKey(RDF_FIRST)) {
                keys++;
                if (!(get(RDF_FIRST) instanceof List && ((List<Object>) get(RDF_FIRST)).size() == 1)) {
                    return false;
                }
            }
            if (containsKey(RDF_REST)) {
                keys++;
                if (!(get(RDF_REST) instanceof List && ((List<Object>) get(RDF_REST)).size() == 1)) {
                    return false;
                }
            }
            if (containsKey("@type")) {
                keys++;
                if (!(get("@type") instanceof List && ((List<Object>) get("@type")).size() == 1)
                        && RDF_LIST.equals(((List<Object>) get("@type")).get(0))) {
                    return false;
                }
            }
            // TODO: SPEC: 4.3.3 has no mention of @id
            if (containsKey("@id")) {
                keys++;
            }
            if (keys < size()) {
                return false;
            }
            return true;
        }

        // return this node without the usages variable
        public Map<String, Object> serialize() {
            return new LinkedHashMap<>(this);
        }
    }

    /**
     * Converts RDF statements into JSON-LD.
     *
     * @param dataset
     *            the RDF statements.
     * @return A list of JSON-LD objects found in the given dataset.
     * @throws JsonLdError
     *             If there was an error during conversion from RDF to JSON-LD.
     */
    public List<Object> fromRDF(final RDFDataset dataset) throws JsonLdError {
        // 1)
        final Map<String, NodeMapNode> defaultGraph = new LinkedHashMap<>();
        // 2)
        final Map<String, Map<String, NodeMapNode>> graphMap = new LinkedHashMap<>();
        graphMap.put("@default", defaultGraph);

        // 3/3.1)
        for (final String name : dataset.graphNames()) {

            final List<RDFDataset.Quad> graph = dataset.getQuads(name);

            // 3.2+3.4)
            Map<String, NodeMapNode> nodeMap;
            if (!graphMap.containsKey(name)) {
                nodeMap = new LinkedHashMap<>();
                graphMap.put(name, nodeMap);
            } else {
                nodeMap = graphMap.get(name);
            }

            // 3.3)
            if (!"@default".equals(name) && !Obj.contains(defaultGraph, name)) {
                defaultGraph.put(name, new NodeMapNode(name));
            }

            // 3.5)
            for (final RDFDataset.Quad triple : graph) {
                final String subject = triple.getSubject().getValue();
                final String predicate = triple.getPredicate().getValue();
                final RDFDataset.Node object = triple.getObject();

                // 3.5.1+3.5.2)
                NodeMapNode node;
                if (!nodeMap.containsKey(subject)) {
                    node = new NodeMapNode(subject);
                    nodeMap.put(subject, node);
                } else {
                    node = nodeMap.get(subject);
                }

                // 3.5.3)
                if ((object.isIRI() || object.isBlankNode())
                        && !nodeMap.containsKey(object.getValue())) {
                    nodeMap.put(object.getValue(), new NodeMapNode(object.getValue()));
                }

                // 3.5.4)
                if (RDF_TYPE.equals(predicate) && (object.isIRI() || object.isBlankNode())
                        && !opts.getUseRdfType()) {
                    JsonLdUtils.mergeValue(node, "@type", object.getValue());
                    continue;
                }

                // 3.5.5)
                final Map<String, Object> value = object.toObject(opts.getUseNativeTypes());

                // 3.5.6+7)
                JsonLdUtils.mergeValue(node, predicate, value);

                // 3.5.8)
                if (object.isBlankNode() || object.isIRI()) {
                    // 3.5.8.1-3)
                    nodeMap.get(object.getValue()).usages
                    .add(new UsagesNode(node, predicate, value));
                }
            }
        }

        // 4)
        for (final Map.Entry<String, Map<String, NodeMapNode>> stringMapEntry : graphMap.entrySet()) {
            final Map<String, NodeMapNode> graph = stringMapEntry.getValue();

            // 4.1)
            if (!graph.containsKey(RDF_NIL)) {
                continue;
            }

            // 4.2)
            final NodeMapNode nil = graph.get(RDF_NIL);
            // 4.3)
            for (final UsagesNode usage : nil.usages) {
                // 4.3.1)
                NodeMapNode node = usage.node;
                String property = usage.property;
                Map<String, Object> head = usage.value;
                // 4.3.2)
                final List<Object> list = new ArrayList<>();
                final List<String> listNodes = new ArrayList<>();
                // 4.3.3)
                while (RDF_REST.equals(property) && node.isWellFormedListNode()) {
                    // 4.3.3.1)
                    list.add(((List<Object>) node.get(RDF_FIRST)).get(0));
                    // 4.3.3.2)
                    listNodes.add((String) node.get("@id"));
                    // 4.3.3.3)
                    final UsagesNode nodeUsage = node.usages.get(0);
                    // 4.3.3.4)
                    node = nodeUsage.node;
                    property = nodeUsage.property;
                    head = nodeUsage.value;
                    // 4.3.3.5)
                    if (!JsonLdUtils.isBlankNode(node)) {
                        break;
                    }
                }
                // 4.3.4)
                if (RDF_FIRST.equals(property)) {
                    // 4.3.4.1)
                    if (RDF_NIL.equals(node.get("@id"))) {
                        continue;
                    }
                    // 4.3.4.3)
                    final String headId = (String) head.get("@id");
                    // 4.3.4.4-5)
                    head = (Map<String, Object>) ((List<Object>) graph.get(headId).get(RDF_REST))
                            .get(0);
                    // 4.3.4.6)
                    list.remove(list.size() - 1);
                    listNodes.remove(listNodes.size() - 1);
                }
                // 4.3.5)
                head.remove("@id");
                // 4.3.6)
                Collections.reverse(list);
                // 4.3.7)
                head.put("@list", list);
                // 4.3.8)
                for (final String nodeId : listNodes) {
                    graph.remove(nodeId);
                }
            }
        }

        // 5)
        final List<Object> result = new ArrayList<>();
        // 6)
        final List<String> ids = new ArrayList<>(defaultGraph.keySet());
        Collections.sort(ids);
        for (final String subject : ids) {
            final NodeMapNode node = defaultGraph.get(subject);
            // 6.1)
            if (graphMap.containsKey(subject)) {
                // 6.1.1)
                node.put("@graph", new ArrayList<>());
                // 6.1.2)
                final List<String> keys = new ArrayList<>(graphMap.get(subject).keySet());
                Collections.sort(keys);
                for (final String s : keys) {
                    final NodeMapNode n = graphMap.get(subject).get(s);
                    if (n.size() == 1 && n.containsKey("@id")) {
                        continue;
                    }
                    ((List<Object>) node.get("@graph")).add(n.serialize());
                }
            }
            // 6.2)
            if (node.size() == 1 && node.containsKey("@id")) {
                continue;
            }
            result.add(node.serialize());
        }

        return result;
    }

    /***
     * ____ _ _ ____ ____ _____ _ _ _ _ _ / ___|___ _ ____ _____ _ __| |_ | |_
     * ___ | _ \| _ \| ___| / \ | | __ _ ___ _ __(_) |_| |__ _ __ ___ | | / _ \|
     * '_ \ \ / / _ \ '__| __| | __/ _ \ | |_) | | | | |_ / _ \ | |/ _` |/ _ \|
     * '__| | __| '_ \| '_ ` _ \ | |__| (_) | | | \ V / __/ | | |_ | || (_) | |
     * _ <| |_| | _| / ___ \| | (_| | (_) | | | | |_| | | | | | | | |
     * \____\___/|_| |_|\_/ \___|_| \__| \__\___/ |_| \_\____/|_| /_/ \_\_|\__,
     * |\___/|_| |_|\__|_| |_|_| |_| |_| |___/
     */

    /**
     * Adds RDF triples for each graph in the current node map to an RDF
     * dataset.
     *
     * @return the RDF dataset.
     * @throws JsonLdError
     *             If there was an error converting from JSON-LD to RDF.
     */
    public RDFDataset toRDF() throws JsonLdError {
        // TODO: make the default generateNodeMap call (i.e. without a
        // graphName) create and return the nodeMap
        final Map<String, Object> nodeMap = newMap();
        nodeMap.put("@default", newMap());
        generateNodeMap(this.value, nodeMap);

        final RDFDataset dataset = new RDFDataset(this);

        for (final Map.Entry<String, Object> stringObjectEntry : nodeMap.entrySet()) {
            // 4.1)
            if (JsonLdUtils.isRelativeIri(stringObjectEntry.getKey())) {
                continue;
            }
            final Map<String, Object> graph = (Map<String, Object>) stringObjectEntry.getValue();
            replaceBlankNode(graph);
            dataset.graphToRDF(stringObjectEntry.getKey(), graph);
        }

        return dataset;
    }

    private void replaceBlankNode(Map<String, Object> graph) {
        List<String> entriesToRemove = new ArrayList<>();
        for (Map.Entry<String, Object> entry : graph.entrySet()) {
            if (entry.getKey().startsWith("_:")) {
                entriesToRemove.add(entry.getKey());
            }
            Object atId = graph.get("@id");
            if (atId != null && atId.toString().startsWith("_:")) {
                graph.put("@id", blankNodeMapping.get(atId));
            }
            Object obj = entry.getValue();
            if (obj instanceof Map) {
                replaceBlankNode((Map<String, Object>)obj);
            }
            if (obj instanceof List) {
                for (Object o : (List)obj) {
                    if (o instanceof Map) {
                        replaceBlankNode((Map<String, Object>)o);
                    }
                }
            }
        }
        for (String key : entriesToRemove) {
            Object tmp = graph.get(key);
            graph.put(blankNodeMapping.get(key), tmp);
            graph.remove(key);
        }

    }

    /***
     * _ _ _ _ _ _ _ _ _ _ _ | \ | | ___ _ __ _ __ ___ __ _| (_)______ _| |_(_)
     * ___ _ __ / \ | | __ _ ___ _ __(_) |_| |__ _ __ ___ | \| |/ _ \| '__| '_ `
     * _ \ / _` | | |_ / _` | __| |/ _ \| '_ \ / _ \ | |/ _` |/ _ \| '__| | __|
     * '_ \| '_ ` _ \ | |\ | (_) | | | | | | | | (_| | | |/ / (_| | |_| | (_) |
     * | | | / ___ \| | (_| | (_) | | | | |_| | | | | | | | | |_| \_|\___/|_|
     * |_| |_| |_|\__,_|_|_/___\__,_|\__|_|\___/|_| |_| /_/ \_\_|\__, |\___/|_|
     * |_|\__|_| |_|_| |_| |_| |___/
     */

    /**
     * Performs RDF normalization on the given JSON-LD input.
     *
     * @param dataset
     *            the expanded JSON-LD object to normalize.
     * @return The normalized JSON-LD object
     * @throws JsonLdError
     *             If there was an error while normalizing.
     */
    public Object normalize(Map<String, Object> dataset) throws JsonLdError {
        // create quads and map bnodes to their associated quads
        final List<Object> quads = new ArrayList<>();
        final Map<String, Object> bnodes = newMap();
        for (String graphName : dataset.keySet()) {
            final List<Map<String, Object>> triples = (List<Map<String, Object>>) dataset
                    .get(graphName);
            if ("@default".equals(graphName)) {
                graphName = null;
            }
            for (final Map<String, Object> quad : triples) {
                if (graphName != null) {
                    if (graphName.indexOf("_:") == 0) {
                        final Map<String, Object> tmp = newMap();
                        tmp.put("type", "blank node");
                        tmp.put("value", graphName);
                        quad.put("name", tmp);
                    } else {
                        final Map<String, Object> tmp = newMap();
                        tmp.put("type", "IRI");
                        tmp.put("value", graphName);
                        quad.put("name", tmp);
                    }
                }
                quads.add(quad);

                final String[] attrs = new String[] { "subject", "object", "name" };
                for (final String attr : attrs) {
                    if (quad.containsKey(attr)
                            && "blank node".equals(((Map<String, Object>) quad.get(attr))
                                    .get("type"))) {
                        final String id = (String) ((Map<String, Object>) quad.get(attr))
                                .get("value");
                        if (!bnodes.containsKey(id)) {
                            bnodes.put(id, new LinkedHashMap<String, List<Object>>() {
                                {
                                    put("quads", new ArrayList<>());
                                }
                            });
                        }
                        ((List<Object>) ((Map<String, Object>) bnodes.get(id)).get("quads"))
                        .add(quad);
                    }
                }
            }
        }

        // mapping complete, start canonical naming
        final NormalizeUtils normalizeUtils = new NormalizeUtils(quads, bnodes, new UniqueNamer(
                "_:c14n"), opts);
        return normalizeUtils.hashBlankNodes(bnodes.keySet());
    }

}
