package org.n52.wps.project.riesgos;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.n52.wps.io.data.GenericFileData;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.project.riesgos.util.JavaProcessStreamReader;
import org.n52.wps.server.AbstractAlgorithm;
import org.n52.wps.server.ExceptionReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class InterTxt extends AbstractAlgorithm {

    private static Logger LOGGER = LoggerFactory.getLogger(InterTxt.class);

    private List<String> errors = new ArrayList<>();

    private final String lineSeparator = System.getProperty("line.separator");

    private String workspacePath = "/usr/share/riesgos/";

	private Object inputdataID = "input-csv";

    public InterTxt() {}

    @Override
    public List<String> getErrors() {
        return errors;
    }

    public String runScript(String argumentString) throws ExceptionReport {
        LOGGER.info("Executing python script.");

        try {

            Runtime rt = Runtime.getRuntime();

            String command = getCommand(argumentString);

            Process proc = rt.exec(command, new String[]{}, new File(workspacePath));

            PipedOutputStream pipedOut = new PipedOutputStream();

            PipedInputStream pipedIn = new PipedInputStream(pipedOut);

            // attach error stream reader
            JavaProcessStreamReader errorStreamReader =
                    new JavaProcessStreamReader(proc.getErrorStream(), "ERROR");

            // attach output stream reader
            JavaProcessStreamReader outputStreamReader = new JavaProcessStreamReader(proc.getInputStream(), "OUTPUT", pipedOut);

            // start them
            errorStreamReader.start();
            outputStreamReader.start();

            String output = "";
            try (BufferedReader outputReader = new BufferedReader(new InputStreamReader(pipedIn));) {
                String line = outputReader.readLine();

                while (line != null) {
                    output = output.concat(line + lineSeparator);
                    line = outputReader.readLine();
                }
            }

            try {
                proc.waitFor();
            } catch (InterruptedException e1) {
                LOGGER.error("Java process was interrupted.", e1);
            } finally {
                proc.destroy();
            }

            LOGGER.info(output);

            return output;
            
        } catch (Exception e) {
            LOGGER.error("Exception occurred while trying to execute python script.", e);
        }
        
        throw new ExceptionReport("Could not run script", ExceptionReport.NO_APPLICABLE_CODE);
    }

    private String getCommand(String argumentString) {

        String pythonScriptName = "inter-txt.py";

        return "python " + workspacePath + File.separatorChar + pythonScriptName + " " + argumentString;
    }

    @Override
    public Map<String, IData> run(Map<String, List<IData>> inputData) throws ExceptionReport {

    	List<IData> inputDataList = inputData.get(inputdataID);
    	
    	GenericFileData inputDataFile = ((GenericFileDataBinding)inputDataList.get(0)).getPayload();
    	
        String argumentString = inputDataFile.getBaseFile(false).getAbsolutePath();

        String output = runScript(argumentString);

        Map<String, IData> result = new HashMap<>();

        result.put("report", new LiteralStringBinding(output));

        return result;
    }

	@Override
    public Class<?> getInputDataType(String id) {
        return GenericFileDataBinding.class;
    }

    @Override
    public Class<?> getOutputDataType(String id) {
        return LiteralStringBinding.class;
    }

}
