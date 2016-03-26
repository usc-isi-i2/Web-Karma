package com.github.jsonldjava.core;

import java.util.LinkedHashMap;
import java.util.Map;

public class UniqueNamer implements Cloneable {
    private final String prefix;
    private int counter;
    private Map<String, String> existing;

    /**
     * Creates a new UniqueNamer. A UniqueNamer issues unique names, keeping
     * track of any previously issued names.
     *
     * @param prefix
     *            the prefix to use ('&lt;prefix&gt;&lt;counter&gt;').
     */
    public UniqueNamer(String prefix) {
        this.prefix = prefix;
        this.counter = 0;
        this.existing = new LinkedHashMap<>();
    }

    /**
     * Copies this UniqueNamer.
     *
     * @return a copy of this UniqueNamer.
     */
    @Override
    public UniqueNamer clone() {
        final UniqueNamer copy = new UniqueNamer(this.prefix);
        copy.counter = this.counter;
        copy.existing = (Map<String, String>) JsonLdUtils.clone(this.existing);
        return copy;
    }

    /**
     * Gets the new name for the given old name, where if no old name is given a
     * new name will be generated.
     *
     * @param oldName
     *            the old name to get the new name for.
     *
     * @return the new name.
     */
    public String getName(String oldName) {
        if (oldName != null && this.existing.containsKey(oldName)) {
            return this.existing.get(oldName);
        }

        final String name = this.prefix + this.counter;
        this.counter++;

        if (oldName != null) {
            this.existing.put(oldName, name);
        }

        return name;
    }

    public String getName() {
        return getName(null);
    }

    public Boolean isNamed(String oldName) {
        return this.existing.containsKey(oldName);
    }

    public Map<String, String> existing() {
        return existing;
    }
}