package ru.spbau.mit.aush.execute.process;


import ru.spbau.mit.aush.execute.cmd.CmdExecutor;
import ru.spbau.mit.aush.log.Logging;

import java.util.logging.Logger;

/**
 * Container main class for builtin AUSH commands;
 * This class is used to run a external process, but for builtin
 * command.
 */
class BuiltinCmdContainer {
    private static final Logger logger = Logging.getLogger("BuiltinCmdContainer");

    public static void main(String[] args) {
        if (args.length != 2) {
            logger.severe("Bad container arguments!");
            throw new IllegalArgumentException("not enough arguments");
        }

        String executorClassName = args[0];
        String argsStr = args[1];

        Class executorClass = null;
        try {
            executorClass = Class.forName(executorClassName);
        } catch (ClassNotFoundException e) {
            logger.severe("No such executor class found " + executorClassName);
            System.exit(1);
        }

        CmdExecutor cmdExecutor = null;
        try {
            cmdExecutor = (CmdExecutor) executorClass.newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            logger.severe("Can't instantiate executor: " + e.getMessage());
            System.exit(1);
        }

        cmdExecutor.exec(argsStr, System.in, System.out);
    }
}
