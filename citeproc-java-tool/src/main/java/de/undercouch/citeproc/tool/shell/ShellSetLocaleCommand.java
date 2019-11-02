package de.undercouch.citeproc.tool.shell;

import de.undercouch.citeproc.CSL;
import de.undercouch.citeproc.helper.tool.ToolUtils;
import de.undercouch.citeproc.tool.AbstractCSLToolCommand;
import de.undercouch.underline.InputReader;
import de.undercouch.underline.OptionParserException;
import de.undercouch.underline.UnknownAttributes;
import jline.console.completer.Completer;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;

/**
 * Set the current citation locale
 * @author Michel Kraemer
 */
public class ShellSetLocaleCommand extends AbstractCSLToolCommand implements Completer {
    /**
     * The current locales
     */
    private List<String> locales;

    @Override
    public String getUsageName() {
        return "set locale";
    }

    @Override
    public String getUsageDescription() {
        return "Set the current citation locale";
    }

    /**
     * Sets the current locales
     * @param locales the locales
     */
    @UnknownAttributes("LOCALE")
    public void setLocales(List<String> locales) {
        this.locales = locales;
    }

    @Override
    public boolean checkArguments() {
        if (locales == null || locales.isEmpty()) {
            error("no locale specified");
            return false;
        }
        if (locales.size() > 1) {
            error("you can only specify one locale");
            return false;
        }

        String l = locales.get(0);
        try {
            Set<String> supportedLocales = CSL.getSupportedLocales();
            if (!supportedLocales.contains(l)) {
                String message = "unsupported locale `" + l + "'";
                String dyms = ToolUtils.getDidYouMeanString(supportedLocales, l);
                if (dyms != null && !dyms.isEmpty()) {
                    message += "\n\n" + dyms;
                }
                error(message);
                return false;
            }
        } catch (IOException e) {
            // could not check supported locales. ignore
        }

        return true;
    }

    @Override
    public int doRun(String[] remainingArgs, InputReader in, PrintWriter out)
            throws OptionParserException, IOException {
        ShellContext.current().setLocale(locales.get(0));
        return 0;
    }

    @Override
    public int complete(String buffer, int cursor, List<CharSequence> candidates) {
        Set<String> sf;
        try {
            sf = CSL.getSupportedLocales();
        } catch (IOException e) {
            // could not get list of supported locales. ignore.
            return 0;
        }

        if (buffer.trim().isEmpty()) {
            candidates.addAll(sf);
        } else {
            String[] args = buffer.split("\\s+");
            String last = args[args.length - 1];
            for (String f : sf) {
                if (f.startsWith(last)) {
                    candidates.add(f);
                }
            }
        }
        return 0;
    }
}
