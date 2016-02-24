package com.github.jsonldjava.impl;

import com.github.jsonldjava.core.JsonLdTripleCallback;
import com.github.jsonldjava.core.RDFDataset;
import com.github.jsonldjava.core.RDFDatasetUtils;

public class NQuadTripleCallback implements JsonLdTripleCallback {
    @Override
    public Object call(RDFDataset dataset) {
        return RDFDatasetUtils.toNQuads(dataset);
    }
}
