package com.github.jsonldjava.utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonLdUrl {

    public String href = "";
    public String protocol = "";
    public String host = "";
    public String auth = "";
    public String user = "";
    public String password = "";
    public String hostname = "";
    public String port = "";
    public String relative = "";
    public String path = "";
    public String directory = "";
    public String file = "";
    public String query = "";
    public String hash = "";

    // things not populated by the regex (NOTE: i don't think it matters if
    // these are null or "" to start with)
    public String pathname = null;
    public String normalizedPath = null;
    public String authority = null;

    private static Pattern parser = Pattern
            .compile("^(?:([^:\\/?#]+):)?(?:\\/\\/((?:(([^:@]*)(?::([^:@]*))?)?@)?([^:\\/?#]*)(?::(\\d*))?))?((((?:[^?#\\/]*\\/)*)([^?#]*))(?:\\?([^#]*))?(?:#(.*))?)");

    public static JsonLdUrl parse(String url) {
        final JsonLdUrl rval = new JsonLdUrl();
        rval.href = url;

        final Matcher matcher = parser.matcher(url);
        if (matcher.matches()) {
            if (matcher.group(1) != null) {
                rval.protocol = matcher.group(1);
            }
            if (matcher.group(2) != null) {
                rval.host = matcher.group(2);
            }
            if (matcher.group(3) != null) {
                rval.auth = matcher.group(3);
            }
            if (matcher.group(4) != null) {
                rval.user = matcher.group(4);
            }
            if (matcher.group(5) != null) {
                rval.password = matcher.group(5);
            }
            if (matcher.group(6) != null) {
                rval.hostname = matcher.group(6);
            }
            if (matcher.group(7) != null) {
                rval.port = matcher.group(7);
            }
            if (matcher.group(8) != null) {
                rval.relative = matcher.group(8);
            }
            if (matcher.group(9) != null) {
                rval.path = matcher.group(9);
            }
            if (matcher.group(10) != null) {
                rval.directory = matcher.group(10);
            }
            if (matcher.group(11) != null) {
                rval.file = matcher.group(11);
            }
            if (matcher.group(12) != null) {
                rval.query = matcher.group(12);
            }
            if (matcher.group(13) != null) {
                rval.hash = matcher.group(13);
            }

            // normalize to node.js API
            if (!"".equals(rval.host) && "".equals(rval.path)) {
                rval.path = "/";
            }
            rval.pathname = rval.path;
            parseAuthority(rval);
            rval.normalizedPath = removeDotSegments(rval.pathname, !"".equals(rval.authority));
            if (!"".equals(rval.query)) {
                rval.path += "?" + rval.query;
            }
            if (!"".equals(rval.protocol)) {
                rval.protocol += ":";
            }
            if (!"".equals(rval.hash)) {
                rval.hash = "#" + rval.hash;
            }
            return rval;
        }

        return rval;
    }

    /**
     * Removes dot segments from a JsonLdUrl path.
     *
     * @param path
     *            the path to remove dot segments from.
     * @param hasAuthority
     *            true if the JsonLdUrl has an authority, false if not.
     * @return The URL without the dot segments
     */
    public static String removeDotSegments(String path, boolean hasAuthority) {
        String rval = "";

        if (path.indexOf("/") == 0) {
            rval = "/";
        }

        // RFC 3986 5.2.4 (reworked)
        final List<String> input = new ArrayList<>(Arrays.asList(path.split("/")));
        if (path.endsWith("/")) {
            // javascript .split includes a blank entry if the string ends with
            // the delimiter, java .split does not so we need to add it manually
            input.add("");
        }
        final List<String> output = new ArrayList<>();
        for (int i = 0; i < input.size(); i++) {
            if (".".equals(input.get(i)) || ("".equals(input.get(i)) && input.size() - i > 1)) {
                // input.remove(0);
                continue;
            }
            if ("..".equals(input.get(i))) {
                // input.remove(0);
                if (hasAuthority
                        || (!output.isEmpty() && !"..".equals(output.get(output.size() - 1)))) {
                    // [].pop() doesn't fail, to replicate this we need to check
                    // that there is something to remove
                    if (!output.isEmpty()) {
                        output.remove(output.size() - 1);
                    }
                } else {
                    output.add("..");
                }
                continue;
            }
            output.add(input.get(i));
            // input.remove(0);
        }

        if (!output.isEmpty()) {
            rval += output.get(0);
            for (int i = 1; i < output.size(); i++) {
                rval += "/" + output.get(i);
            }
        }
        return rval;
    }

    public static String removeBase(Object baseobj, String iri) {
        if (baseobj == null) {
            return iri;
        }

        JsonLdUrl base;
        if (baseobj instanceof String) {
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
        final List<String> baseSegments = new ArrayList<>(Arrays.asList(base.normalizedPath
                .split("/")));
        if (base.normalizedPath.endsWith("/")) {
            baseSegments.add("");
        }
        final List<String> iriSegments = new ArrayList<>(Arrays.asList(rel.normalizedPath
                .split("/")));
        if (rel.normalizedPath.endsWith("/")) {
            iriSegments.add("");
        }

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
        if (!iriSegments.isEmpty()) {
            rval += iriSegments.get(0);
        }
        for (int i = 1; i < iriSegments.size(); i++) {
            rval += "/" + iriSegments.get(i);
        }

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

    public static String resolve(String baseUri, String pathToResolve) {
        // TODO: some input will need to be normalized to perform the expected
        // result with java
        // TODO: we can do this without using java URI!
        if (baseUri == null) {
            return pathToResolve;
        }
        if (pathToResolve == null || "".equals(pathToResolve.trim())) {
            return baseUri;
        }
        try {
            URI uri = new URI(baseUri);
            // query string parsing
            if (pathToResolve.startsWith("?")) {
                // drop fragment from uri if it has one
                if (uri.getFragment() != null) {
                    uri = new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), null, null);
                }
                // add query to the end manually (as URI.resolve does it wrong)
                return uri.toString() + pathToResolve;
            }

            uri = uri.resolve(pathToResolve);
            // java doesn't discard unnecessary dot segments
            String path = uri.getPath();
            if (path != null) {
                path = JsonLdUrl.removeDotSegments(uri.getPath(), true);
            }
            return new URI(uri.getScheme(), uri.getAuthority(), path, uri.getQuery(),
                    uri.getFragment()).toString();
        } catch (final URISyntaxException e) {
            return null;
        }
    }

    /**
     * Parses the authority for the pre-parsed given JsonLdUrl.
     *
     * @param parsed
     *            the pre-parsed JsonLdUrl.
     */
    private static void parseAuthority(JsonLdUrl parsed) {
        // parse authority for unparsed relative network-path reference
        if (parsed.href.indexOf(":") == -1 && parsed.href.indexOf("//") == 0
                && "".equals(parsed.host)) {
            // must parse authority from pathname
            parsed.pathname = parsed.pathname.substring(2);
            final int idx = parsed.pathname.indexOf("/");
            if (idx == -1) {
                parsed.authority = parsed.pathname;
                parsed.pathname = "";
            } else {
                parsed.authority = parsed.pathname.substring(0, idx);
                parsed.pathname = parsed.pathname.substring(idx);
            }
        } else {
            // construct authority
            parsed.authority = parsed.host;
            if (!"".equals(parsed.auth)) {
                parsed.authority = parsed.auth + "@" + parsed.authority;
            }
        }
    }
}
