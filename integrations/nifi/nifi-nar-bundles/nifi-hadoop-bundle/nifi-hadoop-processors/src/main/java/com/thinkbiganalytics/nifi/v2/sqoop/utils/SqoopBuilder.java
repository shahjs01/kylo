package com.thinkbiganalytics.nifi.v2.sqoop.utils;

import com.thinkbiganalytics.nifi.v2.sqoop.PasswordMode;
import com.thinkbiganalytics.nifi.v2.sqoop.enums.CompressionAlgorithm;
import com.thinkbiganalytics.nifi.v2.sqoop.enums.ExtractDataFormat;
import com.thinkbiganalytics.nifi.v2.sqoop.enums.HiveDelimStrategy;
import com.thinkbiganalytics.nifi.v2.sqoop.enums.HiveNullEncodingStrategy;
import com.thinkbiganalytics.nifi.v2.sqoop.enums.SqoopLoadStrategy;
import com.thinkbiganalytics.nifi.v2.sqoop.security.DecryptPassword;

import org.apache.nifi.logging.ComponentLog;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * A class to build a sqoop command that can be run on the command line
 * @author jagrut sharma
 */

public class SqoopBuilder {
    private final static String SPACE_STRING = " ";
    private final static String START_SPACE_QUOTE = " \"";
    private final static String END_QUOTE_SPACE = "\" ";
    private final static String QUOTE = "\"";
    private final static String EQUAL_STRING = "=";
    private final static String STAR_STRING = "*";
    private final static String MASK_STRING = "*****";
    private final static String UNABLE_TO_DECRYPT_STRING = "UNABLE_TO_DECRYPT_ENCRYPTED_PASSWORD";
    private final Integer DEFAULT_CLUSTER_MAP_TASKS = 4;

    private final static String sourcePasswordLoaderClassLabel = "-Dorg.apache.sqoop.credentials.loader.class";
    private final static String sourcePasswordLoaderClassValue = "org.apache.sqoop.util.password.CryptoFileLoader";
    private final static String sourcePasswordPassphraseLabel = "-Dorg.apache.sqoop.credentials.loader.crypto.passphrase";
    private String sourcePasswordPassphrase;
    private final static String sourceDriverLabel = "--driver";
    private String sourceDriver;
    private final static String sourceConnectionStringLabel = "--connect";
    private String sourceConnectionString;
    private final static String sourceUserNameLabel = "--username";
    private String sourceUserName;
    private PasswordMode passwordMode;
    private final static String sourcePasswordHdfsFileLabel = "--password-file";
    private String sourcePasswordHdfsFile;
    private final static String sourcePasswordClearTextLabel = "--password";
    private String sourceEnteredPassword;
    private final static String sourceTableNameLabel = "--table";
    private String sourceTableName;
    private final static String sourceTableFieldsLabel = "--columns";
    private String sourceTableFields;
    private final static String sourceTableWhereClauseLabel = "--where";
    private String sourceTableWhereClause;
    private final static String sourceTableSplitFieldLabel = "--split-by";
    private String sourceTableSplitField;
    private final static String targetHdfsDirectoryLabel = "--target-dir";
    private String targetHdfsDirectory;

    private final static String extractDataFormatTextLabel = "--as-textfile";
    private final static String extractDataFormatAvroLabel = "--as-avrodatafile";
    private final static String extractDataFormatSequenceFileLabel = "--as-sequencefile";
    private final static String extractDataFormatParquetLabel = "--as-parquetfile";
    private ExtractDataFormat extractDataFormat;

    private final static String sourceAutoSetToOneMapperLabel = "--autoreset-to-one-mapper";
    private final static String clusterMapTasksLabel = "--num-mappers";
    private Integer clusterMapTasks = DEFAULT_CLUSTER_MAP_TASKS;
    private SqoopLoadStrategy sourceLoadStrategy;
    private final static String incrementalStrategyLabel = "--incremental";
    private final static String incrementalAppendStrategyLabel = "append";
    private final static String incrementalLastModifiedStrategyLabel = "lastmodified";
    private final static String sourceCheckColumnNameLabel = "--check-column";
    private String sourceCheckColumnName;
    private final static String sourceCheckColumnLastValueLabel = "--last-value";
    private String sourceCheckColumnLastValue;
    private final static String sourceBoundaryQueryLabel = "--boundary-query";
    private String sourceBoundaryQuery;
    private final static String clusterUIJobNameLabel = "--mapreduce-job-name";
    private String clusterUIJobName;
    private HiveDelimStrategy targetHiveDelimStrategy;
    private final static String targetHiveDropDelimLabel = "--hive-drop-import-delims";
    private final static String targetHiveReplaceDelimLabel = "--hive-delims-replacement";
    private String targetHiveReplaceDelim;
    private final HiveNullEncodingStrategy targetHiveNullEncodingStrategy = HiveNullEncodingStrategy.ENCODE_STRING_AND_NONSTRING;
    private final static String targetHiveNullEncodingStrategyNullStringLabel = "--null-string '\\\\N'";
    private final static String targetHiveNullEncodingStrategyNullNonStringLabel = "--null-non-string '\\\\N'";
    private final static String targetHdfsFileDelimiterLabel = "--fields-terminated-by";
    private String targetHdfsFileDelimiter;
    private final static String targetCompressLabel = "--compress";
    private final static String targetCompressionCodecLabel = "--compression-codec";
    private String targetCompressionCodec;
    private Boolean targetCompressFlag = false;

    private final static String operationName = "sqoop";
    private final static String operationType = "import";

    private ComponentLog logger = null;

    /**
     * Set logger
     * @param logger Logger
     * @return {@link SqoopBuilder}
     */
    public SqoopBuilder setLogger (ComponentLog logger) {
        this.logger = logger;
        logger.info("Logger set to {}", new Object[]{this.logger});
        return this;
    }

    /**
     * Set JDBC driver for source system
     * @param sourceDriver source driver
     * @return {@link SqoopBuilder}
     */
    public SqoopBuilder setSourceDriver (String sourceDriver) {
        this.sourceDriver = sourceDriver;
        logMessage("info", "Source Driver", this.sourceDriver);
        return this;
    }

    /**
     * Set connection string for source system
     * @param sourceConnectionString source connection string
     * @return {@link SqoopBuilder}
     */
    public SqoopBuilder setSourceConnectionString (String sourceConnectionString) {
        this.sourceConnectionString = sourceConnectionString;
        logMessage("info", "Source Connection String", this.sourceConnectionString);
        return this;
    }

    /**
     * Set user name for connecting to source system
     * @param sourceUserName user name
     * @return {@link SqoopBuilder}
     */
    public SqoopBuilder setSourceUserName (String sourceUserName) {
        this.sourceUserName = sourceUserName;
        logMessage("info", "Source User Name", this.sourceUserName);
        return this;
    }

    /**
     * Set password mode for providing password to connect to source system
     * @param passwordMode {@link PasswordMode}
     * @return {@link SqoopBuilder}
     */
    public SqoopBuilder setPasswordMode (PasswordMode passwordMode) {
        this.passwordMode = passwordMode;
        logMessage("info", "Source Password Mode", this.passwordMode);
        return this;
    }

    /**
     * Set location of password file on HDFS
     * @param sourcePasswordHdfsFile location on HDFS
     * @return {@link SqoopBuilder}
     */
    public SqoopBuilder setSourcePasswordHdfsFile (String sourcePasswordHdfsFile) {
        this.sourcePasswordHdfsFile = sourcePasswordHdfsFile;
        logMessage("info", "Source Password File (HDFS)", MASK_STRING);
        return this;
    }

    /**
     * Set passphrase used to generate encrypted password
     * @param sourcePasswordPassphrase passphrase
     * @return {@link SqoopBuilder}
     */
    public SqoopBuilder setSourcePasswordPassphrase (String sourcePasswordPassphrase) {
        this.sourcePasswordPassphrase = sourcePasswordPassphrase;
        logMessage("info", "Source Password Passphrase", MASK_STRING);
        return this;
    }

    /**
     * Set password entered (clear text / encrypted)
     * @param sourceEnteredPassword password string
     * @return {@link SqoopBuilder}
     */
    public SqoopBuilder setSourceEnteredPassword(String sourceEnteredPassword) {
        this.sourceEnteredPassword = sourceEnteredPassword;
        logMessage("info", "Source Entered Password", MASK_STRING);
        return this;
    }

    /**
     * Set source table name to extract from
     * @param sourceTableName table name
     * @return {@link SqoopBuilder}
     */
    public SqoopBuilder setSourceTableName (String sourceTableName) {
        this.sourceTableName = sourceTableName;
        logMessage("info", "Source Table Name", this.sourceTableName);
        return this;
    }

    /**
     * Set fields to get from source table (comma-separated). Use * to indicate all fields.
     * @param sourceTableFields source fields separated by comma / *
     * @return {@link SqoopBuilder}
     */
    public SqoopBuilder setSourceTableFields (String sourceTableFields) {
        if (sourceTableFields.trim().equals(STAR_STRING)) {
            // all fields
            this.sourceTableFields = sourceTableFields.trim();
        }
        else {
            // selected fields
            List<String> fieldList = Arrays.asList(sourceTableFields.split(","));
            this.sourceTableFields = getQueryFieldsForStatement(fieldList);
        }
        logMessage("info", "Source Table Fields", this.sourceTableFields);
        return this;
    }

    /**
     * Set WHERE clause to filter extract from source table
     * @param sourceTableWhereClause WHERE clause
     * @return {@link SqoopBuilder}
     */
    public SqoopBuilder setSourceTableWhereClause (String sourceTableWhereClause) {
        this.sourceTableWhereClause = sourceTableWhereClause;
        logMessage("info", "Source Table Where Clause", sourceTableWhereClause);
        return this;
    }

    /**
     * Set load strategy for source system (full/incremental)
     * @param sourceLoadStrategy {@link SqoopLoadStrategy}
     * @return {@link SqoopBuilder}
     */
    public SqoopBuilder setSourceLoadStrategy (SqoopLoadStrategy sourceLoadStrategy) {
        this.sourceLoadStrategy = sourceLoadStrategy;
        logMessage("info", "Source Load Strategy", this.sourceLoadStrategy);
        return this;
    }

    /**
     * Set column to check for last modified time / id for incremental load.<br>
     *     Not needed for full load.
     * @param sourceCheckColumnName column name to check
     * @return {@link SqoopBuilder}
     */
    public SqoopBuilder setSourceCheckColumnName (String sourceCheckColumnName) {
        this.sourceCheckColumnName = sourceCheckColumnName;
        logMessage("info", "Source Check Column Name", this.sourceCheckColumnName);
        return this;
    }

    /**
     * Last value extracted for incremental load mode. <br>
     *     Not needed for full load.
     * @param sourceCheckColumnLastValue last value extracted
     * @return {@link SqoopBuilder}
     */
    public SqoopBuilder setSourceCheckColumnLastValue (String sourceCheckColumnLastValue) {
        this.sourceCheckColumnLastValue = sourceCheckColumnLastValue;
        logMessage("info", "Source Check Column Last Value", this.sourceCheckColumnLastValue);
        return this;
    }

    /**
     * Set a field can be used for splitting units of work in parallel. By default, single-field primary key is used if available.
     * @param sourceSplitByField field name
     * @return {@link SqoopBuilder}
     */
    public SqoopBuilder setSourceSplitByField (String sourceSplitByField) {
        this.sourceTableSplitField = sourceSplitByField;
        logMessage("info", "Source Split By Field", this.sourceTableSplitField);
        return this;
    }

    /**
     * Set boundary query to get max and min values for split-by-field column
     * @param sourceBoundaryQuery boundary query
     * @return {@link SqoopBuilder}
     */
    public SqoopBuilder setSourceBoundaryQuery (String sourceBoundaryQuery) {
        this.sourceBoundaryQuery = sourceBoundaryQuery;
        logMessage("info", "Source Boundary Query", this.sourceBoundaryQuery);
        return this;
    }

    /**
     * Set number of mappers to use for extract
     * @param clusterMapTasks number of mappers
     * @return {@link SqoopBuilder}
     */
    public SqoopBuilder setClusterMapTasks (Integer clusterMapTasks) {
        if (clusterMapTasks > 0) {
            this.clusterMapTasks = clusterMapTasks;
            logMessage("info", "Number of Cluster Map Tasks", this.clusterMapTasks);
        }
        return this;
    }

    /**
     * Set job name to show in cluster UI
     * @param clusterUIJobName job name
     * @return {@link SqoopBuilder}
     */
    public SqoopBuilder setClusterUIJobName (String clusterUIJobName) {
        this.clusterUIJobName = clusterUIJobName;
        logMessage("info", "Cluster UI Job Name", this.clusterUIJobName);
        return this;
    }

    /**
     * Set target directory to land the extracted data in
     * @param targetHdfsDirectory HDFS directory
     * @return {@link SqoopBuilder}
     */
    public SqoopBuilder setTargetHdfsDirectory (String targetHdfsDirectory) {
        this.targetHdfsDirectory = targetHdfsDirectory;
        logMessage("info", "Target HDFS Directory", this.targetHdfsDirectory);
        return this;
    }

    /**
     * Set format to land the extracted data in on HDFS
     * @param extractDataFormat {@link ExtractDataFormat}
     * @return {@link SqoopBuilder}
     */
    public SqoopBuilder setTargetExtractDataFormat(ExtractDataFormat extractDataFormat) {
        this.extractDataFormat = extractDataFormat;
        logMessage("info", "Extract Data Format", this.extractDataFormat);
        return this;
    }

    /**
     * Set delimiter when landing data in HDFS
     * @param targetHdfsFileDelimiter delimiter
     * @return {@link SqoopBuilder}
     */
    public SqoopBuilder setTargetHdfsFileDelimiter (String targetHdfsFileDelimiter) {
        this.targetHdfsFileDelimiter = targetHdfsFileDelimiter;
        logMessage("info", "Target HDFS File Delimiter", this.targetHdfsFileDelimiter);
        return this;
    }

    /**
     * Set strategy to handle Hive-specific delimiters (\n, \r, \01)
     * @param targetHiveDelimStrategy delimiter strategy {@link HiveDelimStrategy}
     * @return {@link SqoopBuilder}
     */
    public SqoopBuilder setTargetHiveDelimStrategy (HiveDelimStrategy targetHiveDelimStrategy) {
        this.targetHiveDelimStrategy = targetHiveDelimStrategy;
        logMessage("info", "Target Hive Delimiter Strategy", this.targetHiveDelimStrategy);
        return this;
    }

    /**
     * Set replacement delimiter for Hive
     * @param targetHiveReplaceDelim replacement delimiter
     * @return {@link SqoopBuilder}
     */
    public SqoopBuilder setTargetHiveReplaceDelim (String targetHiveReplaceDelim) {
        this.targetHiveReplaceDelim = targetHiveReplaceDelim;
        logMessage("info", "Target Hive Replace Delim", this.targetHiveReplaceDelim);
        return this;
    }

    /**
     * Set compression algorithm for data landing in HDFS
     * @param targetCompressionAlgorithm compression algorithm
     * @return {@link SqoopBuilder}
     */
    public SqoopBuilder setTargetCompressionAlgorithm (CompressionAlgorithm targetCompressionAlgorithm) {
        targetCompressionCodec = getCompressionCodecClass (targetCompressionAlgorithm);
        logMessage("info", "Target Compression Algorithm", targetCompressionAlgorithm);
        return this;
    }

    /*
     * Get the compression codec class
     */
    private String getCompressionCodecClass (CompressionAlgorithm compressionAlgorithm) {
        String compressionCodecClass = null;

        if (compressionAlgorithm != null) {
            switch (compressionAlgorithm) {
                case NONE:
                    compressionCodecClass = null;
                    break;

                case GZIP:
                    compressionCodecClass = "org.apache.hadoop.io.compress.GzipCodec";
                    break;

                case SNAPPY:
                    compressionCodecClass = "org.apache.hadoop.io.compress.SnappyCodec";
                    break;

                case BZIP2:
                    compressionCodecClass = "org.apache.hadoop.io.compress.BZip2Codec";
                    break;

                case LZO:
                    compressionCodecClass = "com.hadoop.compression.lzo.LzoCodec";
                    break;
            }
        }

        if (compressionCodecClass != null) {
            targetCompressFlag = true;
        }

        logMessage("info", "Compression Codec", this.targetCompressionCodec);
        logMessage("info", "Compress output", this.targetCompressFlag);

        return compressionCodecClass;
    }

    /*
     * Log message as per level
     */
    private void logMessage(@Nonnull String level, @Nonnull String property, @Nonnull Object value) {
        switch (level) {
            case "debug":
                if (logger != null) {
                    logger.debug("{} set to: {}", new Object[] { property, value});
                }
                break;

            case "info":
                if (logger != null) {
                    logger.info("{} set to: {}", new Object[] { property, value});
                }
                break;

            case "trace":
                if (logger != null) {
                    logger.trace("{} set to: {}", new Object[] { property, value});
                }
                break;

            case "warn":
                if (logger != null) {
                    logger.warn("{} set to: {}", new Object[] { property, value});
                }
                break;

            case "error":
                if (logger != null) {
                    logger.error("{} set to: {}", new Object[] { property, value});
                }
                break;

            default:
                if (logger != null) {
                    logger.info("{} set to: {}", new Object[] { property, value});
                }
        }
    }

    /**
     * Build a sqoop command
     * @return sqoop command
     */
    public String build() {
        return buildSqoopCommand();
    }

    /*
     * Build the sqoop command
     */
    private String buildSqoopCommand() {
        StringBuffer commandStringBuffer = new StringBuffer();
        SqoopUtils sqoopUtils = new SqoopUtils();

        /* Identify operation */
        commandStringBuffer.append(operationName)                                               //sqoop
            .append(SPACE_STRING)
            .append(operationType)                                                              //import
            .append(SPACE_STRING);

        /* Handle encrypted password file */
        if (passwordMode == PasswordMode.ENCRYPTED_ON_HDFS_FILE) {
            commandStringBuffer.append(sourcePasswordLoaderClassLabel)                          //-Dorg.apache.sqoop.credentials.loader.class
                .append(EQUAL_STRING)
                .append(QUOTE)
                .append(sourcePasswordLoaderClassValue)                                         //org.apache.sqoop.util.password.CryptoFileLoader
                .append(END_QUOTE_SPACE)
                .append(sourcePasswordPassphraseLabel)                                          //-Dorg.apache.sqoop.credentials.loader.crypto.passphrase
                .append(EQUAL_STRING)
                .append(QUOTE)
                .append(sourcePasswordPassphrase)                                               //"user provided"
                .append(END_QUOTE_SPACE);
        }

        /* Handle Oracle case */
        if (!sqoopUtils.isOracleDatabase(sourceDriver)) {
            commandStringBuffer.append(sourceDriverLabel)                                       //--driver
                .append(START_SPACE_QUOTE)
                .append(sourceDriver)                                                           //"user provided"
                .append(END_QUOTE_SPACE);
        }
        else {
            logger.info("Skipping provided --driver parameter for Oracle database.");
        }

        /* Handle authentication */
        commandStringBuffer
            .append(sourceConnectionStringLabel)                                                //--connect
            .append(START_SPACE_QUOTE)
            .append(sourceConnectionString)                                                     //"user provided"
            .append(END_QUOTE_SPACE)
            .append(sourceUserNameLabel)                                                        //--username
            .append(START_SPACE_QUOTE)
            .append(sourceUserName)                                                             //"user provided"
            .append(END_QUOTE_SPACE);


        /* Handle password modes */
        if (passwordMode == PasswordMode.ENCRYPTED_ON_HDFS_FILE) {
            commandStringBuffer.append(sourcePasswordHdfsFileLabel)                             //--password-file
                .append(START_SPACE_QUOTE)
                .append(sourcePasswordHdfsFile)                                                 //"user provided"
                .append(END_QUOTE_SPACE);
        }
        else if (passwordMode == PasswordMode.CLEAR_TEXT_ENTRY || passwordMode == PasswordMode.ENCRYPTED_TEXT_ENTRY) {

            if (passwordMode == PasswordMode.ENCRYPTED_TEXT_ENTRY) {
                try {
                    sourceEnteredPassword = DecryptPassword.decryptPassword(sourceEnteredPassword, sourcePasswordPassphrase);
                    logger.info("Entered encrypted password was decrypted successfully.");
                }
                catch (Exception e) {
                    sourceEnteredPassword = UNABLE_TO_DECRYPT_STRING;
                    logger.warn("Unable to decrypt entered password (encrypted, Base 64). [{}]", new Object[] { e.getMessage() });
                }
            }

            commandStringBuffer.append(sourcePasswordClearTextLabel)                            //--password
                .append(START_SPACE_QUOTE)
                .append(sourceEnteredPassword)                                                  //"user provided"
                .append(END_QUOTE_SPACE);
        }

        /* Handle table details */
        commandStringBuffer.append(sourceTableNameLabel)                                        //--table
            .append(START_SPACE_QUOTE)
            .append(sourceTableName)                                                            //"user provided"
            .append(END_QUOTE_SPACE);

        if (!sourceTableFields.trim().equals(STAR_STRING)) {
            commandStringBuffer
                .append(sourceTableFieldsLabel)                                                 //--columns
                .append(START_SPACE_QUOTE)
                .append(sourceTableFields)                                                      //"generated from user provided value"
                .append(END_QUOTE_SPACE);
        }

        if (sourceTableWhereClause != null) {
            commandStringBuffer
                .append(sourceTableWhereClauseLabel)                                            //--where
                .append(START_SPACE_QUOTE)
                .append(sourceTableWhereClause)                                                 //"user provided"
                .append(END_QUOTE_SPACE);
        }

        /* Handle splits */
        if (sourceTableSplitField != null) {
            commandStringBuffer
                .append(sourceTableSplitFieldLabel)                                             //--split-by
                .append(START_SPACE_QUOTE)
                .append(sourceTableSplitField)                                                  //"user provided"
                .append(END_QUOTE_SPACE);
        }
        else {
            commandStringBuffer
                .append(sourceAutoSetToOneMapperLabel)                                          //--autoreset-to-one-mapper
                .append(SPACE_STRING);
        }

        /* Handle HDFS landing data parameters */
        commandStringBuffer
            .append(targetHdfsDirectoryLabel)                                                   //--target-dir
            .append(START_SPACE_QUOTE)
            .append(targetHdfsDirectory)                                                        //"user provided"
            .append(END_QUOTE_SPACE);


        switch (extractDataFormat) {
            case TEXT:
                commandStringBuffer.append(extractDataFormatTextLabel);                         //--as-textfile
                break;

            case AVRO:
                commandStringBuffer.append(extractDataFormatAvroLabel);                         //--as-avrodatafile
                break;

            case SEQUENCE_FILE:
                commandStringBuffer.append(extractDataFormatSequenceFileLabel);                 //--as-sequencefile
                break;

            case PARQUET:
                commandStringBuffer.append(extractDataFormatParquetLabel);                      //--as-parquetfile
                break;

            default:
                commandStringBuffer.append(extractDataFormatTextLabel);                         //--as-textfile
                break;
        }
        commandStringBuffer.append(SPACE_STRING);

        commandStringBuffer
            .append(targetHdfsFileDelimiterLabel)                                               //--fields-terminated-by
            .append(START_SPACE_QUOTE)
            .append(targetHdfsFileDelimiter)                                                    //"user provided"
            .append(END_QUOTE_SPACE);

        /* Handle incremental load parameters */
        if (sourceLoadStrategy != SqoopLoadStrategy.FULL_LOAD) {
            commandStringBuffer
                .append(incrementalStrategyLabel)                                               //--incremental
                .append(SPACE_STRING);

            if (sourceLoadStrategy == SqoopLoadStrategy.INCREMENTAL_APPEND) {
                commandStringBuffer.append(incrementalAppendStrategyLabel);                     //append
            }
            else if (sourceLoadStrategy == SqoopLoadStrategy.INCREMENTAL_LASTMODIFIED) {
                commandStringBuffer.append(incrementalLastModifiedStrategyLabel);               //lastmodified
            }

            commandStringBuffer
                .append(SPACE_STRING)
                .append(sourceCheckColumnNameLabel)                                             //--check-column
                .append(START_SPACE_QUOTE)
                .append(sourceCheckColumnName)                                                  //"user provided"
                .append(END_QUOTE_SPACE)
                .append(sourceCheckColumnLastValueLabel)                                        //--last-value
                .append(START_SPACE_QUOTE)
                .append(sourceCheckColumnLastValue)                                             //"user provided/watermark service"
                .append(END_QUOTE_SPACE);
        }

        /* Handle Hive related parameters */
        if (targetHiveDelimStrategy == HiveDelimStrategy.DROP) {
            commandStringBuffer
                .append(targetHiveDropDelimLabel)                                               //--hive-drop-import-delims
                .append(SPACE_STRING);
        }
        /*
        else if (targetHiveDelimStrategy == HiveDelimStrategy.KEEP) {
            // Do nothing. Keeping for readability.
        }*/
        else if (targetHiveDelimStrategy == HiveDelimStrategy.REPLACE) {
            commandStringBuffer
                .append(targetHiveReplaceDelimLabel)                                            //--hive-delims-replacement
                .append(START_SPACE_QUOTE)
                .append(targetHiveReplaceDelim)                                                 //"user provided"
                .append(END_QUOTE_SPACE);
        }

        if (targetHiveNullEncodingStrategy
            == HiveNullEncodingStrategy.ENCODE_STRING_AND_NONSTRING) {
            commandStringBuffer
                .append(targetHiveNullEncodingStrategyNullStringLabel)                          //--null-string '\\\\N'
                .append(SPACE_STRING)
                .append(targetHiveNullEncodingStrategyNullNonStringLabel)                       //--null-non-string '\\\\N'
                .append(SPACE_STRING);
        }
        /*
        else if (targetHiveNullEncodingStrategy
                 == HiveNullEncodingStrategy.DO_NOT_ENCODE) {
            // Do nothing. Keeping for readability.
        }*/
        else if (targetHiveNullEncodingStrategy
                 == HiveNullEncodingStrategy.ENCODE_ONLY_STRING) {
            commandStringBuffer
                .append(targetHiveNullEncodingStrategyNullStringLabel)                          //--null-string '\\\\N'
                .append(SPACE_STRING);
        }
        else if (targetHiveNullEncodingStrategy
                 == HiveNullEncodingStrategy.ENCODE_ONLY_NONSTRING) {
            commandStringBuffer
                .append(targetHiveNullEncodingStrategyNullNonStringLabel)                       //--null-non-string '\\\\N'
                .append(SPACE_STRING);
        }

        /* Handle other job parameters */
        if (sourceBoundaryQuery != null) {
            commandStringBuffer
                .append(sourceBoundaryQueryLabel)                                               //--boundary-query
                .append(START_SPACE_QUOTE)
                .append(sourceBoundaryQuery)                                                    //"user provided"
                .append(END_QUOTE_SPACE);
        }

        commandStringBuffer.append(clusterMapTasksLabel)                                        //--num-mappers
            .append(START_SPACE_QUOTE)
            .append(clusterMapTasks)                                                            //"user-provided-value"
            .append(END_QUOTE_SPACE);

        if (targetCompressFlag) {
            commandStringBuffer
                .append(targetCompressLabel)                                                    //--compress
                .append(SPACE_STRING)
                .append(targetCompressionCodecLabel)                                            //--compression-codec
                .append(START_SPACE_QUOTE)
                .append(targetCompressionCodec)                                                 //"user provided"
                .append(END_QUOTE_SPACE);
        }

        if (clusterUIJobName != null) {
            commandStringBuffer
                .append(clusterUIJobNameLabel)                                                  //--mapreduce-job-name
                .append(START_SPACE_QUOTE)
                .append(clusterUIJobName)                                                       //"user-provided-value"
                .append(QUOTE);
        }

        return commandStringBuffer.toString();
    }

    /*
     * Get the list of fields as a string separated by commas
     */
    private String getQueryFieldsForStatement(List<String> fields) {
        int totalFields = fields.size();
        StringBuilder queryFieldsBuilder = new StringBuilder();

        for (int i = 0; i < totalFields; i++) {
            queryFieldsBuilder.append(fields.get(i).trim());
            if (i != (totalFields - 1)) {
                queryFieldsBuilder.append(",");
            }
        }
        return queryFieldsBuilder.toString();
    }
}
