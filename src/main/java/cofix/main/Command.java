/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */

package cofix.main;

import cofix.common.util.Pair;
import org.apache.commons.cli.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author: Jiajun
 * @date: 2019-06-13
 */
public class Command {

    private static Options options;
    private String projName;
    private String projHome;
    private List<Integer> ids;
    private boolean parsed = false;


    static {
        options = new Options();

        Option option = new Option("home", "projectHome", true, "Project home.");
        option.setRequired(true);
        options.addOption(option);

        option = new Option("proj", "projectName", true, "Project name in Defects4j.");
        option.setRequired(true);
        options.addOption(option);

        option = new Option("id", "projectID", true, "Bug id.");
        option.setRequired(true);
        options.addOption(option);
    }

    public Command(String[] args, Map<String, Pair<Integer, Set<Integer>>> projInfo) {
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine commandLine = null;
        try {
            commandLine = parser.parse(options, args);
        } catch (ParseException e) {
            String command = "$CMD -home /home/path/to/project -proj math/chart -id 1/1-5/1,5,7/single/multi/all";
            formatter.printHelp(command, options);
            return;
        }

        projHome = commandLine.getOptionValue("home");
        projName = commandLine.getOptionValue("proj");
        ids = new LinkedList<>();
        String idseq = commandLine.getOptionValue("id");
        if(idseq.equalsIgnoreCase("single")){
            ids.addAll(projInfo.get(projName).getSecond());
        } else if(idseq.equalsIgnoreCase("multi")){
            for(int id = 1; id <= projInfo.get(projName).getFirst(); id++){
                if(projInfo.get(projName).getSecond().contains(id)){
                    continue;
                }
                ids.add(id);
            }
        } else if(idseq.equalsIgnoreCase("all")){
            for(int id = 1; id <= projInfo.get(projName).getFirst(); id++){
                ids.add(id);
            }
        } else if(idseq.contains("-")){
            int start = Integer.parseInt(idseq.substring(0, idseq.indexOf("-")));
            int end = Integer.parseInt(idseq.substring(idseq.indexOf("-") + 1, idseq.length()));
            for(int id = start; id <= end; id++){
                ids.add(id);
            }
        } else {
            String[] split = idseq.split(",");
            for(String string : split){
                int id = Integer.parseInt(string);
                ids.add(id);
            }
        }
        parsed = true;
    }

    public boolean valid() {
        return parsed;
    }

    public String getProjHome() {
        return projHome;
    }

    public String getProjName() {
        return projName;
    }

    public List<Integer> getBugIds() {
        return ids;
    }
}
