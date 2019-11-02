package de.undercouch.citeproc.tool.shell;

import de.undercouch.citeproc.CSLTool;
import de.undercouch.citeproc.tool.AbstractCSLToolCommand;
import de.undercouch.citeproc.tool.CSLToolCommand;
import de.undercouch.citeproc.tool.ShellCommand;
import de.undercouch.underline.Command;
import de.undercouch.underline.InputReader;
import de.undercouch.underline.Option;
import de.undercouch.underline.OptionGroup;
import de.undercouch.underline.OptionIntrospector;
import de.undercouch.underline.OptionIntrospector.ID;
import de.undercouch.underline.OptionParser;
import de.undercouch.underline.OptionParserException;
import de.undercouch.underline.UnknownAttributes;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Displays help about a command in the interactive shell
 * @author Michel Kraemer
 */
public class ShellHelpCommand extends AbstractCSLToolCommand {
    private List<String> commands = new ArrayList<>();

    /**
     * Sets the commands to display the help for
     * @param commands the commands
     */
    @UnknownAttributes("COMMAND")
    public void setCommands(List<String> commands) {
        this.commands = commands;
    }

    @Override
    public String getUsageName() {
        return "help";
    }

    @Override
    public String getUsageDescription() {
        return "Display help for a given command";
    }

    @Override
    public int doRun(String[] remainingArgs, InputReader in, PrintWriter out)
            throws OptionParserException, IOException {
        Class<? extends Command> cmdClass;

        String[] args = commands.toArray(new String[0]);

        OptionGroup<ID> options;
        try {
            ShellCommandParser.Result pr = ShellCommandParser.parse(
                    args, ShellCommand.EXCLUDED_COMMANDS);
            String[] ra = pr.getRemainingArgs();
            if (ra.length > 0) {
                error("unknown command `" + ra[0] + "'");
                return 1;
            }

            cmdClass = pr.getLastCommand();
            if (cmdClass == null) {
                options = OptionIntrospector.introspect(CSLTool.class,
                        AdditionalShellCommands.class);
            } else {
                options = OptionIntrospector.introspect(cmdClass);
            }
        } catch (IntrospectionException e) {
            // should never happen
            throw new RuntimeException(e);
        }

        OptionGroup<ID> filtered = new OptionGroup<>();
        for (Option<ID> cmd : options.getCommands()) {
            Class<? extends Command> cc =
                    OptionIntrospector.getCommand(cmd.getId());
            if (!ShellCommand.EXCLUDED_COMMANDS.contains(cc)) {
                filtered.addCommand(cmd);
            }
        }

        CSLToolCommand cmd = null;
        if (cmdClass != null) {
            try {
                cmd = (CSLToolCommand)cmdClass.newInstance();
            } catch (Exception e) {
                // should never happen
                throw new RuntimeException(e);
            }
        }

        if (cmdClass == null) {
            OptionParser.usage(null, null, filtered, null, out);
        } else {
            String unknownArguments = OptionIntrospector.getUnknownArgumentName(
                    cmdClass);
            OptionParser.usage(cmd.getUsageName(), cmd.getUsageDescription(),
                    filtered, unknownArguments, null, out);
        }

        return 0;
    }
}
