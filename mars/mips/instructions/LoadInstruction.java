package mars.mips.instructions;

/*
 * This file is a part of BUAA CO extension.
 * 
 */

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

import mars.Globals;

/**
 * This class provides a static method to load an additional basic instruction
 * during the runtime.
 * 
 * @author Toby Shi
 * @version November 2022
 * @see InstructionSet
 * @see BasicInstruction
 * @see AdditionalInstruction
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
            AdditionalInstruction newInstance = (AdditionalInstruction) clazz.getDeclaredConstructor().newInstance();
            BasicInstruction instr = new BasicInstruction(
                    newInstance.getExample(),
                    newInstance.getDescription(),
                    newInstance.getInstructionFormat(),
                    newInstance.getOperationMask(),
                    (SimulationCode) newInstance);
            instr.createExampleTokenList();
            Globals.instructionSet.getInstructionList().add(instr);
            Globals.instructionSet.initialize(); // reinitialize because a new basic instruction is added.
            urlClassLoader.close();
        } catch (Exception e) {
            throw new ClassNotFoundException("Load " + filename + " failed!");
        }
    }
}
