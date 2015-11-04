package com.github.jsonldjava.core;

import java.util.Map;

public class JsonLdError extends Exception {

    Map<String, Object> details;
    private Error type;

    public JsonLdError(Error type, Object detail) {
        // TODO: pretty toString (e.g. print whole json objects)
        super(detail == null ? "" : detail.toString());
        this.type = type;
    }

    public JsonLdError(Error type) {
        super("");
        this.type = type;
    }

    public enum Error {
        LOADING_DOCUMENT_FAILED("loading document failed"), LIST_OF_LISTS("list of lists"), INVALID_INDEX_VALUE(
                "invalid @index value"), CONFLICTING_INDEXES("conflicting indexes"), INVALID_ID_VALUE(
                        "invalid @id value"), INVALID_LOCAL_CONTEXT("invalid local context"), MULTIPLE_CONTEXT_LINK_HEADERS(
                                "multiple context link headers"), LOADING_REMOTE_CONTEXT_FAILED(
                                        "loading remote context failed"), INVALID_REMOTE_CONTEXT("invalid remote context"), RECURSIVE_CONTEXT_INCLUSION(
                                                "recursive context inclusion"), INVALID_BASE_IRI("invalid base IRI"), INVALID_VOCAB_MAPPING(
                                                        "invalid vocab mapping"), INVALID_DEFAULT_LANGUAGE("invalid default language"), KEYWORD_REDEFINITION(
                                                                "keyword redefinition"), INVALID_TERM_DEFINITION("invalid term definition"), INVALID_REVERSE_PROPERTY(
                                                                        "invalid reverse property"), INVALID_IRI_MAPPING("invalid IRI mapping"), CYCLIC_IRI_MAPPING(
                                                                                "cyclic IRI mapping"), INVALID_KEYWORD_ALIAS("invalid keyword alias"), INVALID_TYPE_MAPPING(
                                                                                        "invalid type mapping"), INVALID_LANGUAGE_MAPPING("invalid language mapping"), COLLIDING_KEYWORDS(
                                                                                                "colliding keywords"), INVALID_CONTAINER_MAPPING("invalid container mapping"), INVALID_TYPE_VALUE(
                                                                                                        "invalid type value"), INVALID_VALUE_OBJECT("invalid value object"), INVALID_VALUE_OBJECT_VALUE(
                                                                                                                "invalid value object value"), INVALID_LANGUAGE_TAGGED_STRING(
                                                                                                                        "invalid language-tagged string"), INVALID_LANGUAGE_TAGGED_VALUE(
                                                                                                                                "invalid language-tagged value"), INVALID_TYPED_VALUE("invalid typed value"), INVALID_SET_OR_LIST_OBJECT(
                                                                                                                                        "invalid set or list object"), INVALID_LANGUAGE_MAP_VALUE(
                                                                                                                                                "invalid language map value"), COMPACTION_TO_LIST_OF_LISTS(
                                                                                                                                                        "compaction to list of lists"), INVALID_REVERSE_PROPERTY_MAP(
                                                                                                                                                                "invalid reverse property map"), INVALID_REVERSE_VALUE("invalid @reverse value"), INVALID_REVERSE_PROPERTY_VALUE(
                                                                                                                                                                        "invalid reverse property value"),

                                                                                                                                                                        // non spec related errors
                                                                                                                                                                        SYNTAX_ERROR("syntax error"), NOT_IMPLEMENTED("not implemnted"), UNKNOWN_FORMAT(
                                                                                                                                                                                "unknown format"), INVALID_INPUT("invalid input"), PARSE_ERROR("parse error"), UNKNOWN_ERROR(
                                                                                                                                                                                        "unknown error");

        private final String error;

        private Error(String error) {
            this.error = error;
        }

        @Override
        public String toString() {
            return error;
        }
    }

    public JsonLdError setType(Error error) {
        this.type = error;
        return this;
    };

    public Error getType() {
        return type;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    @Override
    public String getMessage() {
        final String msg = super.getMessage();
        if (msg != null && !"".equals(msg)) {
            return type.toString() + ": " + msg;
        }
        return type.toString();
    }
}
