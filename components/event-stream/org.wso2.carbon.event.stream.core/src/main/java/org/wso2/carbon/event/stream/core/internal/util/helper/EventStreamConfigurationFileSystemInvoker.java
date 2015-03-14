package org.wso2.carbon.event.stream.core.internal.util.helper;

import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.stream.core.EventStreamDeployer;
import org.wso2.carbon.event.stream.core.exception.EventStreamConfigurationException;
import org.wso2.carbon.event.stream.core.internal.util.EventStreamConstants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class EventStreamConfigurationFileSystemInvoker {
    private static final Log log = LogFactory.getLog(EventStreamConfigurationFileSystemInvoker.class);

    public static void save(StreamDefinition streamDefinition, String filePath, AxisConfiguration axisConfig) throws EventStreamConfigurationException{
        EventStreamDeployer eventStreamDeployer = (EventStreamDeployer)((DeploymentEngine) axisConfig.getConfigurator()).
                getDeployer(EventStreamConstants.EVENT_STREAMS, EventStreamConstants.EVENT_STREAMS_FILE_EXTENSION_TYPE);
        try {
            OutputStreamWriter writer = null;
            try {
                /* save contents to .xml file */
                File file = new File(filePath);

                writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");

                eventStreamDeployer.getDeployedEventStreamFilePaths().add(filePath);
                writer.write(streamDefinition.toString());
                log.info("Stream definition configuration for " + streamDefinition.getStreamId() + " saved in the filesystem");
            } finally {
                if (writer != null) {
                    writer.flush();
                    writer.close();
                }
            }
            eventStreamDeployer.executeManualDeployment(filePath);
        } catch (IOException e) {
            eventStreamDeployer.getDeployedEventStreamFilePaths().remove(filePath);
            log.error("Error while saving stream definition " + streamDefinition.getStreamId(), e);
            throw new EventStreamConfigurationException("Error while saving stream definition " + streamDefinition.getStreamId(), e);
        }
    }

    public static void delete(String fileName, AxisConfiguration axisConfig) throws EventStreamConfigurationException {

        try {
            String directoryPath =  new File(axisConfig.getRepository().getPath())
                    .getAbsolutePath() + File.separator + EventStreamConstants.EVENT_STREAMS;
            String filePath =  directoryPath + File.separator + fileName;
            File file = new File(filePath);
            if (file.exists()) {
                EventStreamDeployer deployer = (EventStreamDeployer)((DeploymentEngine) axisConfig.getConfigurator()).
                        getDeployer(EventStreamConstants.EVENT_STREAMS, EventStreamConstants.EVENT_STREAMS_FILE_EXTENSION_TYPE);
                deployer.getUnDeployedEventStreamFilePaths().add(filePath);
                boolean fileDeleted = file.delete();
                if (!fileDeleted) {
                    log.error("Could not delete " + fileName);
                    deployer.getUnDeployedEventStreamFilePaths().remove(filePath);
                } else {
                    log.info(fileName + " is deleted from the file system");
                    deployer.executeManualUndeployment(filePath);
                }
            }
        } catch (Exception e) {
            throw new EventStreamConfigurationException("Error while deleting the stream definition file ", e);
        }
    }
}