package org.meridor.perspective.shell.misc;

import org.meridor.perspective.shell.repository.ApiProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

//TODO: use it (find the way to intercept command calls)
@Component
public class ErrorHandler {
    
    @Autowired
    private ApiProvider apiProvider;
    
    @Autowired
    private ShellInformationProvider shellInformationProvider;
    
    @Autowired
    private Logger logger;
    
    public void handleErrors(Throwable e) throws Throwable {
        if (
                e instanceof IOException ||
                e.getCause() != null && e.getCause() instanceof IOException
        ) {
            String apiUrl = apiProvider.getBaseUri();
            logger.error(String.format("Failed to connect to the API. Is your connection url = \"%s\" correct?", apiUrl));
        } else {
            printErrorMessage(e);
        }
    }
    
    private void printErrorMessage(Throwable throwable) {
        logger.error(String.format("An error [%s] occurred during command execution: %s", throwable.getClass().getCanonicalName(), throwable.getMessage()));
        printDiagnosticOutput(throwable);
    }
    
    //TODO: we may want to implement automatic error sending here, i.e. just POST stacktrace and product version information to some fixed URL  
    private void printDiagnosticOutput(Throwable throwable) {
        logger.error("The following output will help developers to diagnose the problem:");
        logger.error("---");
        logger.error(String.format("Product: perspective-shell %s", shellInformationProvider.getVersion()));
        
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(byteArrayOutputStream);
        throwable.printStackTrace(printStream);
        String stackTrace = byteArrayOutputStream.toString();
        logger.error(String.format("Exception: %s", stackTrace));
    }
    
}
