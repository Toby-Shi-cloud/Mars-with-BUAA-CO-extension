package mars.mips.instructions;

/*
 * This file is a part of BUAA CO extension.
 * 
 */

import java.io.File;
import java.io.InvalidClassException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Objects;

import mars.Globals;

/**
 * This class provides a static method to load an additional basic instruction
 * during the runtime.
 * 
 * @author Toby Shi
 * @version November 2022
 * @see InstructionSet
 * @see BasicInstruction
 * @see InstructionLoad
 */
@SuppressWarnings("unchecked")
public class LoadInstruction {

    /**
     * Load an additional basic instruction during the runtime.
     * @param filename the name of .class file to be loaded
     * @throws ClassNotFoundException if failed to load the .class file
     */
    public static void loadClass(String filename) throws ClassNotFoundException {
        try {
            File classFile = new File(filename);
            String className = classFile.getAbsolutePath();
            className = className.substring(className.lastIndexOf(File.separatorChar) + 1, className.lastIndexOf('.'));
            URL url = classFile.toURI().toURL();
            URLClassLoader urlClassLoader = new URLClassLoader(new URL[] { url });
            Class<?> clazz = urlClassLoader.loadClass(className);
            InstructionLoad newInstance = (InstructionLoad) clazz.getDeclaredConstructor().newInstance();
            BasicInstruction instr = getBasicInstruction(newInstance);
            Globals.instructionSet.getInstructionList().add(instr);
            Globals.instructionSet.initialize(); // reinitialize because a new basic instruction is added.
            urlClassLoader.close();
        } catch (Exception e) {
            throw new ClassNotFoundException("Load " + filename + " failed!");
        }
    }
    
    private static BasicInstruction getBasicInstruction(InstructionLoad newInstance) throws InvalidClassException {
        BasicInstructionFormat basicInstructionFormat;

        if (Objects.equals(newInstance.getFormatStr(), "R"))
            basicInstructionFormat = BasicInstructionFormat.R_FORMAT;
        else if (Objects.equals(newInstance.getFormatStr(), "B"))
            basicInstructionFormat = BasicInstructionFormat.I_BRANCH_FORMAT;
        else if (Objects.equals(newInstance.getFormatStr(), "J"))
            basicInstructionFormat = BasicInstructionFormat.J_FORMAT;
        else if (Objects.equals(newInstance.getFormatStr(), "I"))
            basicInstructionFormat = BasicInstructionFormat.I_FORMAT;
        else
            throw new InvalidClassException("Invalid format string: " + newInstance.getFormatStr());

        BasicInstruction instr = new BasicInstruction(
                newInstance.getTemplate(),
                newInstance.getDescription(),
                basicInstructionFormat,
                newInstance.getEncoding(),
                (SimulationCode) newInstance);
        instr.createExampleTokenList();
        return instr;
    }
}
