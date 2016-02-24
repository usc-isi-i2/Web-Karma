package com.github.jsonldjava.core;

import java.util.regex.Pattern;

public class Regex {
    final public static Pattern TRICKY_UTF_CHARS = Pattern.compile(
            // ("1.7".equals(System.getProperty("java.specification.version")) ?
            // "[\\x{10000}-\\x{EFFFF}]" :
            "[\uD800\uDC00-\uDB7F\uDFFF]" // this seems to work with jdk1.6
            );
    // for ttl
    final public static Pattern PN_CHARS_BASE = Pattern
            .compile("[a-zA-Z]|[\\u00C0-\\u00D6]|[\\u00D8-\\u00F6]|[\\u00F8-\\u02FF]|[\\u0370-\\u037D]|[\\u037F-\\u1FFF]|"
                    + "[\\u200C-\\u200D]|[\\u2070-\\u218F]|[\\u2C00-\\u2FEF]|[\\u3001-\\uD7FF]|[\\uF900-\\uFDCF]|[\\uFDF0-\\uFFFD]|"
                    + TRICKY_UTF_CHARS);
    final public static Pattern PN_CHARS_U = Pattern.compile(PN_CHARS_BASE + "|[_]");
    final public static Pattern PN_CHARS = Pattern.compile(PN_CHARS_U
            + "|[-0-9]|[\\u00B7]|[\\u0300-\\u036F]|[\\u203F-\\u2040]");
    final public static Pattern PN_PREFIX = Pattern.compile("(?:(?:" + PN_CHARS_BASE + ")(?:(?:"
            + PN_CHARS + "|[\\.])*(?:" + PN_CHARS + "))?)");
    final public static Pattern HEX = Pattern.compile("[0-9A-Fa-f]");
    final public static Pattern PN_LOCAL_ESC = Pattern
            .compile("[\\\\][_~\\.\\-!$&'\\(\\)*+,;=/?#@%]");
    final public static Pattern PERCENT = Pattern.compile("%" + HEX + HEX);
    final public static Pattern PLX = Pattern.compile(PERCENT + "|" + PN_LOCAL_ESC);
    final public static Pattern PN_LOCAL = Pattern.compile("((?:" + PN_CHARS_U + "|[:]|[0-9]|"
            + PLX + ")(?:(?:" + PN_CHARS + "|[.]|[:]|" + PLX + ")*(?:" + PN_CHARS + "|[:]|" + PLX
            + "))?)");
    final public static Pattern PNAME_NS = Pattern.compile("((?:" + PN_PREFIX + ")?):");
    final public static Pattern PNAME_LN = Pattern.compile("" + PNAME_NS + PN_LOCAL);
    final public static Pattern UCHAR = Pattern.compile("\\u005Cu" + HEX + HEX + HEX + HEX
            + "|\\u005CU" + HEX + HEX + HEX + HEX + HEX + HEX + HEX + HEX);
    final public static Pattern ECHAR = Pattern.compile("\\u005C[tbnrf\\u005C\"']");
    final public static Pattern IRIREF = Pattern.compile("(?:<((?:[^\\x00-\\x20<>\"{}|\\^`\\\\]|"
            + UCHAR + ")*)>)");
    final public static Pattern BLANK_NODE_LABEL = Pattern.compile("(?:_:((?:" + PN_CHARS_U
            + "|[0-9])(?:(?:" + PN_CHARS + "|[\\.])*(?:" + PN_CHARS + "))?))");
    final public static Pattern WS = Pattern.compile("[ \t\r\n]");
    final public static Pattern WS_0_N = Pattern.compile(WS + "*");
    final public static Pattern WS_0_1 = Pattern.compile(WS + "?");
    final public static Pattern WS_1_N = Pattern.compile(WS + "+");
    final public static Pattern STRING_LITERAL_QUOTE = Pattern
            .compile("\"(?:[^\\u0022\\u005C\\u000A\\u000D]|(?:" + ECHAR + ")|(?:" + UCHAR + "))*\"");
    final public static Pattern STRING_LITERAL_SINGLE_QUOTE = Pattern
            .compile("'(?:[^\\u0027\\u005C\\u000A\\u000D]|(?:" + ECHAR + ")|(?:" + UCHAR + "))*'");
    final public static Pattern STRING_LITERAL_LONG_SINGLE_QUOTE = Pattern
            .compile("'''(?:(?:(?:'|'')?[^'\\\\])|" + ECHAR + "|" + UCHAR + ")*'''");
    final public static Pattern STRING_LITERAL_LONG_QUOTE = Pattern
            .compile("\"\"\"(?:(?:(?:\"|\"\")?[^\\\"\\\\])|" + ECHAR + "|" + UCHAR + ")*\"\"\"");
    final public static Pattern LANGTAG = Pattern.compile("(?:@([a-zA-Z]+(?:-[a-zA-Z0-9]+)*))");
    final public static Pattern INTEGER = Pattern.compile("[+-]?[0-9]+");
    final public static Pattern DECIMAL = Pattern.compile("[+-]?[0-9]*\\.[0-9]+");
    final public static Pattern EXPONENT = Pattern.compile("[eE][+-]?[0-9]+");
    final public static Pattern DOUBLE = Pattern.compile("[+-]?(?:(?:[0-9]+\\.[0-9]*" + EXPONENT
            + ")|(?:\\.[0-9]+" + EXPONENT + ")|(?:[0-9]+" + EXPONENT + "))");
}
