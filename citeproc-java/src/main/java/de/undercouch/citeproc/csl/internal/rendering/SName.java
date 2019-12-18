package de.undercouch.citeproc.csl.internal.rendering;

import de.undercouch.citeproc.csl.CSLName;
import de.undercouch.citeproc.csl.internal.RenderContext;
import de.undercouch.citeproc.csl.internal.SElement;
import de.undercouch.citeproc.helper.NodeHelper;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Node;

/**
 * A name element from a style file
 * @author Michel Kraemer
 */
public class SName implements SElement {
    private final String variable;
    private final String and;
    private final String delimiter;
    private final String delimiterPrecedesLast;
    private final String initializeWith;
    private final String nameAsSortOrder;
    private final String sortSeparator;

    /**
     * Create the name element from an XML node
     * @param node the XML node (may be {@code null})
     * @param variable the variable that holds the name (may be {@code null})
     */
    public SName(Node node, String variable) {
        this.variable = variable;

        String delimiter;
        String delimiterPrecedesLast;
        String sortSeparator;
        if (node != null) {
            and = NodeHelper.getAttrValue(node, "and");
            initializeWith = NodeHelper.getAttrValue(node, "initialize-with");
            nameAsSortOrder = NodeHelper.getAttrValue(node, "name-as-sort-order");
            delimiter = NodeHelper.getAttrValue(node, "delimiter");
            delimiterPrecedesLast = NodeHelper.getAttrValue(node,
                    "delimiter-precedes-last");
            sortSeparator = NodeHelper.getAttrValue(node, "sort-separator");
        } else {
            and = null;
            initializeWith = null;
            nameAsSortOrder = null;
            delimiter = null;
            delimiterPrecedesLast = null;
            sortSeparator = null;
        }

        if (delimiter == null) {
            delimiter = ", ";
        }
        this.delimiter = delimiter;

        if (delimiterPrecedesLast == null) {
            delimiterPrecedesLast = "contextual";
        }
        this.delimiterPrecedesLast = delimiterPrecedesLast;

        if (sortSeparator == null) {
            sortSeparator = ", ";
        }
        this.sortSeparator = sortSeparator;
    }

    @Override
    public void render(RenderContext ctx) {
        CSLName[] names = null;
        if (variable != null) {
            names = ctx.getNameVariable(variable);
        }
        if (names == null) {
            return;
        }

        String and;
        if ("text".equals(this.and)) {
            and = " " + ctx.getTerm("and") + " ";
        } else if ("symbol".equals(this.and)) {
            and = " & ";
        } else {
            and = delimiter;
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < names.length; ++i) {
            boolean nameAsSort = "all".equals(nameAsSortOrder) ||
                    (i == 0 && "first".equals(nameAsSortOrder));
            builder.append(render(names[i], nameAsSort));

            if (i < names.length - 1) {
                if (i == names.length - 2) {
                    boolean delimiterAppended = false;
                    switch (delimiterPrecedesLast) {
                        case "contextual":
                            if (names.length > 2) {
                                builder.append(delimiter);
                                delimiterAppended = true;
                            }
                            break;

                        case "always":
                            builder.append(delimiter);
                            delimiterAppended = true;
                            break;

                        case "after-inverted-name":
                            // IMHO, according to the standard, we should
                            // check for nameAsSort == true here, but
                            // citeproc.js seems to behave differently
                            if (i == 0) {
                                builder.append(delimiter);
                                delimiterAppended = true;
                            }
                            break;

                        default:
                            break;
                    }
                    if (!delimiterAppended || !and.equals(delimiter)) {
                        appendAnd(builder, and);
                    }
                } else {
                    builder.append(delimiter);
                }
            }
        }
        ctx.emit(builder.toString());
    }

    private void appendAnd(StringBuilder builder, String and) {
        if (Character.isWhitespace(builder.charAt(builder.length() - 1)) &&
                Character.isWhitespace(and.charAt(0))) {
            builder.append(and, 1, and.length());
        } else {
            builder.append(and);
        }
    }

    private String render(CSLName name, boolean nameAsSort) {
        StringBuilder result = new StringBuilder();

        String given = name.getGiven();
        StringBuilder givenBuffer = new StringBuilder();
        if (initializeWith != null) {
            // produce initials for each given name and append
            // 'initializeWith' to each of them
            boolean found = true;
            for (int i = 0; i < given.length(); ++i) {
                char c = given.charAt(i);
                if (Character.isWhitespace(c) || c == '.') {
                    found = true;
                } else if (found) {
                    givenBuffer.append(c).append(initializeWith);
                    found = false;
                }
            }
        } else {
            givenBuffer.append(given);
        }

        given = givenBuffer.toString();
        given = StringUtils.stripEnd(given, null);

        if (nameAsSort) {
            result.append(name.getFamily()).append(sortSeparator).append(given);
        } else {
            result.append(given).append(" ").append(name.getFamily());
        }

        return result.toString();
    }
}
