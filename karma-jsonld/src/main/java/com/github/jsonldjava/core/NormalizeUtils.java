package com.github.jsonldjava.core;

import static com.github.jsonldjava.core.RDFDatasetUtils.parseNQuads;
import static com.github.jsonldjava.core.RDFDatasetUtils.toNQuad;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.github.jsonldjava.utils.Obj;

class NormalizeUtils {

    private final UniqueNamer namer;
    private final Map<String, Object> bnodes;
    private final List<Object> quads;
    private final JsonLdOptions options;

    public NormalizeUtils(List<Object> quads, Map<String, Object> bnodes, UniqueNamer namer,
            JsonLdOptions options) {
        this.options = options;
        this.quads = quads;
        this.bnodes = bnodes;
        this.namer = namer;
    }

    // generates unique and duplicate hashes for bnodes
    public Object hashBlankNodes(Collection<String> unnamed_) throws JsonLdError {
        List<String> unnamed = new ArrayList<>(unnamed_);
        List<String> nextUnnamed = new ArrayList<>();
        Map<String, List<String>> duplicates = new LinkedHashMap<>();
        Map<String, String> unique = new LinkedHashMap<>();

        // NOTE: not using the same structure as javascript here to avoid
        // possible stack overflows
        // hash quads for each unnamed bnode
        for (int hui = 0;; hui++) {
            if (hui == unnamed.size()) {
                // done, name blank nodes
                Boolean named = false;
                List<String> hashes = new ArrayList<>(unique.keySet());
                Collections.sort(hashes);
                for (final String hash : hashes) {
                    final String bnode = unique.get(hash);
                    namer.getName(bnode);
                    named = true;
                }

                // continue to hash bnodes if a bnode was assigned a name
                if (named) {
                    // this resets the initial variables, so it seems like it
                    // has to go on the stack
                    // but since this is the end of the function either way, it
                    // might not have to
                    // hashBlankNodes(unnamed);
                    hui = -1;
                    unnamed = nextUnnamed;
                    nextUnnamed = new ArrayList<>();
                    duplicates = new LinkedHashMap<>();
                    unique = new LinkedHashMap<>();
                    continue;
                }
                // name the duplicate hash bnods
                else {
                    // names duplicate hash bnodes
                    // enumerate duplicate hash groups in sorted order
                    hashes = new ArrayList<>(duplicates.keySet());
                    Collections.sort(hashes);

                    // process each group
                    for (int pgi = 0;; pgi++) {
                        if (pgi == hashes.size()) {
                            // done, create JSON-LD array
                            // return createArray();
                            final List<String> normalized = new ArrayList<>();

                            // Note: At this point all bnodes in the set of RDF
                            // quads have been
                            // assigned canonical names, which have been stored
                            // in the 'namer' object.
                            // Here each quad is updated by assigning each of
                            // its bnodes its new name
                            // via the 'namer' object

                            // update bnode names in each quad and serialize
                            for (int cai = 0; cai < quads.size(); ++cai) {
                                final Map<String, Object> quad = (Map<String, Object>) quads
                                        .get(cai);
                                for (final String attr : new String[] { "subject", "object", "name" }) {
                                    if (quad.containsKey(attr)) {
                                        final Map<String, Object> qa = (Map<String, Object>) quad
                                                .get(attr);
                                        if (qa != null
                                                && "blank node".equals(qa.get("type"))
                                                && ((String) qa.get("value")).indexOf("_:c14n") != 0) {
                                            qa.put("value",
                                                    namer.getName((String) qa.get(("value"))));
                                        }
                                    }
                                }
                                normalized
                                .add(toNQuad(
                                        (RDFDataset.Quad) quad,
                                        quad.containsKey("name")
                                        && quad.get("name") != null ? (String) ((Map<String, Object>) quad
                                                .get("name")).get("value") : null));
                            }

                            // sort normalized output
                            Collections.sort(normalized);

                            // handle output format
                            if (options.format != null) {
                                if ("application/nquads".equals(options.format)) {
                                    String rval = "";
                                    for (final String n : normalized) {
                                        rval += n;
                                    }
                                    return rval;
                                } else {
                                    throw new JsonLdError(JsonLdError.Error.UNKNOWN_FORMAT,
                                            options.format);
                                }
                            }
                            String rval = "";
                            for (final String n : normalized) {
                                rval += n;
                            }
                            return parseNQuads(rval);
                        }

                        // name each group member
                        final List<String> group = duplicates.get(hashes.get(pgi));
                        final List<HashResult> results = new ArrayList<>();
                        for (int n = 0;; n++) {
                            if (n == group.size()) {
                                // name bnodes in hash order
                                Collections.sort(results, new Comparator<HashResult>() {
                                    @Override
                                    public int compare(HashResult a, HashResult b) {
                                        final int res = a.hash.compareTo(b.hash);
                                        return res;
                                    }
                                });
                                for (final HashResult r : results) {
                                    // name all bnodes in path namer in
                                    // key-entry order
                                    // Note: key-order is preserved in
                                    // javascript
                                    for (final String key : r.pathNamer.existing().keySet()) {
                                        namer.getName(key);
                                    }
                                }
                                // processGroup(i+1);
                                break;
                            } else {
                                // skip already-named bnodes
                                final String bnode = group.get(n);
                                if (namer.isNamed(bnode)) {
                                    continue;
                                }

                                // hash bnode paths
                                final UniqueNamer pathNamer = new UniqueNamer("_:b");
                                pathNamer.getName(bnode);

                                final HashResult result = hashPaths(bnode, bnodes, namer, pathNamer);
                                results.add(result);
                            }
                        }
                    }
                }
            }

            // hash unnamed bnode
            final String bnode = unnamed.get(hui);
            final String hash = hashQuads(bnode, bnodes, namer);

            // store hash as unique or a duplicate
            if (duplicates.containsKey(hash)) {
                duplicates.get(hash).add(bnode);
                nextUnnamed.add(bnode);
            } else if (unique.containsKey(hash)) {
                final List<String> tmp = new ArrayList<>();
                tmp.add(unique.get(hash));
                tmp.add(bnode);
                duplicates.put(hash, tmp);
                nextUnnamed.add(unique.get(hash));
                nextUnnamed.add(bnode);
                unique.remove(hash);
            } else {
                unique.put(hash, bnode);
            }
        }
    }

    private static class HashResult {
        String hash;
        UniqueNamer pathNamer;
    }

    /**
     * Produces a hash for the paths of adjacent bnodes for a bnode,
     * incorporating all information about its subgraph of bnodes. This method
     * will recursively pick adjacent bnode permutations that produce the
     * lexicographically-least 'path' serializations.
     *
     * @param id
     *            the ID of the bnode to hash paths for.
     * @param bnodes
     *            the map of bnode quads.
     * @param namer
     *            the canonical bnode namer.
     * @param pathNamer
     *            the namer used to assign names to adjacent bnodes.
     * @param callback
     *            (err, result) called once the operation completes.
     */
    private static HashResult hashPaths(String id, Map<String, Object> bnodes, UniqueNamer namer,
            UniqueNamer pathNamer) {
        try {
            // create SHA-1 digest
            final MessageDigest md = MessageDigest.getInstance("SHA-1");

            final Map<String, List<String>> groups = new LinkedHashMap<>();
            List<String> groupHashes;
            final List<Object> quads = (List<Object>) ((Map<String, Object>) bnodes.get(id))
                    .get("quads");

            for (int hpi = 0;; hpi++) {
                if (hpi == quads.size()) {
                    // done , hash groups
                    groupHashes = new ArrayList<>(groups.keySet());
                    Collections.sort(groupHashes);
                    for (int hgi = 0;; hgi++) {
                        if (hgi == groupHashes.size()) {
                            final HashResult res = new HashResult();
                            res.hash = encodeHex(md.digest());
                            res.pathNamer = pathNamer;
                            return res;
                        }

                        // digest group hash
                        final String groupHash = groupHashes.get(hgi);
                        md.update(groupHash.getBytes("UTF-8"));

                        // choose a path and namer from the permutations
                        String chosenPath = null;
                        UniqueNamer chosenNamer = null;
                        final Permutator permutator = new Permutator(groups.get(groupHash));
                        while (true) {
                            Boolean contPermutation = false;
                            Boolean breakOut = false;
                            final List<String> permutation = permutator.next();
                            UniqueNamer pathNamerCopy = pathNamer.clone();

                            // build adjacent path
                            String path = "";
                            final List<String> recurse = new ArrayList<>();
                            for (final String bnode : permutation) {
                                // use canonical name if available
                                if (namer.isNamed(bnode)) {
                                    path += namer.getName(bnode);
                                } else {
                                    // recurse if bnode isn't named in the path
                                    // yet
                                    if (!pathNamerCopy.isNamed(bnode)) {
                                        recurse.add(bnode);
                                    }
                                    path += pathNamerCopy.getName(bnode);
                                }

                                // skip permutation if path is already >= chosen
                                // path
                                if (chosenPath != null && path.length() >= chosenPath.length()
                                        && path.compareTo(chosenPath) > 0) {
                                    // return nextPermutation(true);
                                    if (permutator.hasNext()) {
                                        contPermutation = true;
                                    } else {
                                        // digest chosen path and update namer
                                        md.update(chosenPath.getBytes("UTF-8"));
                                        pathNamer = chosenNamer;
                                        // hash the nextGroup
                                        breakOut = true;
                                    }
                                    break;
                                }
                            }

                            // if we should do the next permutation
                            if (contPermutation) {
                                continue;
                            }
                            // if we should stop processing this group
                            if (breakOut) {
                                break;
                            }

                            // does the next recursion
                            for (int nrn = 0;; nrn++) {
                                if (nrn == recurse.size()) {
                                    // return nextPermutation(false);
                                    if (chosenPath == null || path.compareTo(chosenPath) < 0) {
                                        chosenPath = path;
                                        chosenNamer = pathNamerCopy;
                                    }
                                    if (!permutator.hasNext()) {
                                        // digest chosen path and update namer
                                        md.update(chosenPath.getBytes("UTF-8"));
                                        pathNamer = chosenNamer;
                                        // hash the nextGroup
                                        breakOut = true;
                                    }
                                    break;
                                }

                                // do recursion
                                final String bnode = recurse.get(nrn);
                                final HashResult result = hashPaths(bnode, bnodes, namer,
                                        pathNamerCopy);
                                path += pathNamerCopy.getName(bnode) + "<" + result.hash + ">";
                                pathNamerCopy = result.pathNamer;

                                // skip permutation if path is already >= chosen
                                // path
                                if (chosenPath != null && path.length() >= chosenPath.length()
                                        && path.compareTo(chosenPath) > 0) {
                                    // return nextPermutation(true);
                                    if (!permutator.hasNext()) {
                                        // digest chosen path and update namer
                                        md.update(chosenPath.getBytes("UTF-8"));
                                        pathNamer = chosenNamer;
                                        // hash the nextGroup
                                        breakOut = true;
                                    }
                                    break;
                                }
                                // do next recursion
                            }

                            // if we should stop processing this group
                            if (breakOut) {
                                break;
                            }
                        }
                    }
                }

                // get adjacent bnode
                final Map<String, Object> quad = (Map<String, Object>) quads.get(hpi);
                String bnode = getAdjacentBlankNodeName((Map<String, Object>) quad.get("subject"),
                        id);
                String direction = null;
                if (bnode != null) {
                    // normal property
                    direction = "p";
                } else {
                    bnode = getAdjacentBlankNodeName((Map<String, Object>) quad.get("object"), id);
                    if (bnode != null) {
                        // reverse property
                        direction = "r";
                    }
                }

                if (bnode != null) {
                    // get bnode name (try canonical, path, then hash)
                    String name;
                    if (namer.isNamed(bnode)) {
                        name = namer.getName(bnode);
                    } else if (pathNamer.isNamed(bnode)) {
                        name = pathNamer.getName(bnode);
                    } else {
                        name = hashQuads(bnode, bnodes, namer);
                    }

                    // hash direction, property, end bnode name/hash
                    final MessageDigest md1 = MessageDigest.getInstance("SHA-1");
                    // String toHash = direction + (String) ((Map<String,
                    // Object>) quad.get("predicate")).get("value") + name;
                    md1.update(direction.getBytes("UTF-8"));
                    md1.update(((String) ((Map<String, Object>) quad.get("predicate")).get("value"))
                            .getBytes("UTF-8"));
                    md1.update(name.getBytes("UTF-8"));
                    final String groupHash = encodeHex(md1.digest());
                    if (groups.containsKey(groupHash)) {
                        groups.get(groupHash).add(bnode);
                    } else {
                        final List<String> tmp = new ArrayList<>();
                        tmp.add(bnode);
                        groups.put(groupHash, tmp);
                    }
                }
            }
        } catch (final NoSuchAlgorithmException e) {
            // TODO: i don't expect that SHA-1 is even NOT going to be
            // available?
            // look into this further
            throw new RuntimeException(e);
        } catch (final UnsupportedEncodingException e) {
            // TODO: i don't expect that UTF-8 is ever not going to be available
            // either
            throw new RuntimeException(e);
        }
    }

    /**
     * Hashes all of the quads about a blank node.
     *
     * @param id
     *            the ID of the bnode to hash quads for.
     * @param bnodes
     *            the mapping of bnodes to quads.
     * @param namer
     *            the canonical bnode namer.
     *
     * @return the new hash.
     */
    private static String hashQuads(String id, Map<String, Object> bnodes, UniqueNamer namer) {
        // return cached hash
        if (((Map<String, Object>) bnodes.get(id)).containsKey("hash")) {
            return (String) ((Map<String, Object>) bnodes.get(id)).get("hash");
        }

        // serialize all of bnode's quads
        final List<Map<String, Object>> quads = (List<Map<String, Object>>) ((Map<String, Object>) bnodes
                .get(id)).get("quads");
        final List<String> nquads = new ArrayList<>();
        for (int i = 0; i < quads.size(); ++i) {
            nquads.add(toNQuad((RDFDataset.Quad) quads.get(i),
                    quads.get(i).get("name") != null ? (String) ((Map<String, Object>) quads.get(i)
                            .get("name")).get("value") : null, id));
        }
        // sort serialized quads
        Collections.sort(nquads);
        // return hashed quads
        final String hash = sha1hash(nquads);
        ((Map<String, Object>) bnodes.get(id)).put("hash", hash);
        return hash;
    }

    /**
     * A helper class to sha1 hash all the strings in a collection
     *
     * @param nquads
     * @return
     */
    private static String sha1hash(Collection<String> nquads) {
        try {
            // create SHA-1 digest
            final MessageDigest md = MessageDigest.getInstance("SHA-1");
            for (final String nquad : nquads) {
                md.update(nquad.getBytes("UTF-8"));
            }
            return encodeHex(md.digest());
        } catch (final NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    // TODO: this is something to optimize
    private static String encodeHex(final byte[] data) {
        String rval = "";
        for (final byte b : data) {
            rval += String.format("%02x", b);
        }
        return rval;
    }

    /**
     * A helper function that gets the blank node name from an RDF quad node
     * (subject or object). If the node is a blank node and its value does not
     * match the given blank node ID, it will be returned.
     *
     * @param node
     *            the RDF quad node.
     * @param id
     *            the ID of the blank node to look next to.
     *
     * @return the adjacent blank node name or null if none was found.
     */
    private static String getAdjacentBlankNodeName(Map<String, Object> node, String id) {
        return "blank node".equals(node.get("type"))
                && (!node.containsKey("value") || !Obj.equals(node.get("value"), id)) ? (String) node
                        .get("value") : null;
    }

    private static class Permutator {

        private final List<String> list;
        private boolean done;
        private final Map<String, Boolean> left;

        public Permutator(List<String> list) {
            this.list = (List<String>) JsonLdUtils.clone(list);
            Collections.sort(this.list);
            this.done = false;
            this.left = new LinkedHashMap<>();
            for (final String i : this.list) {
                this.left.put(i, true);
            }
        }

        /**
         * Returns true if there is another permutation.
         *
         * @return true if there is another permutation, false if not.
         */
        public boolean hasNext() {
            return !this.done;
        }

        /**
         * Gets the next permutation. Call hasNext() to ensure there is another
         * one first.
         *
         * @return the next permutation.
         */
        public List<String> next() {
            final List<String> rval = (List<String>) JsonLdUtils.clone(this.list);

            // Calculate the next permutation using Steinhaus-Johnson-Trotter
            // permutation algoritm

            // get largest mobile element k
            // (mobile: element is grater than the one it is looking at)
            String k = null;
            int pos = 0;
            final int length = this.list.size();
            for (int i = 0; i < length; ++i) {
                final String element = this.list.get(i);
                final Boolean left = this.left.get(element);
                if ((k == null || element.compareTo(k) > 0)
                        && ((left && i > 0 && element.compareTo(this.list.get(i - 1)) > 0) || (!left
                                && i < (length - 1) && element.compareTo(this.list.get(i + 1)) > 0))) {
                    k = element;
                    pos = i;
                }
            }

            // no more permutations
            if (k == null) {
                this.done = true;
            } else {
                // swap k and the element it is looking at
                final int swap = this.left.get(k) ? pos - 1 : pos + 1;
                this.list.set(pos, this.list.get(swap));
                this.list.set(swap, k);

                // reverse the direction of all element larger than k
                for (int i = 0; i < length; i++) {
                    if (this.list.get(i).compareTo(k) > 0) {
                        this.left.put(this.list.get(i), !this.left.get(this.list.get(i)));
                    }
                }
            }

            return rval;
        }

    }

}
