package com.rchilli.assignment.service;

import java.util.UUID;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;

@Service
public class LoggerService {

	@Value("${logs.file.path}")
	private String filePath;
	

	public String getLogs() {
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

	    // create file name
	    String uuid = UUID.randomUUID().toString().substring(0, 8);
	    String fileName = filePath + "application_" + uuid + ".csv";

	    // create the header FileAppender
	    FileAppender<ILoggingEvent> headerAppender = new FileAppender<>();
	    headerAppender.setContext(loggerContext);
	    headerAppender.setFile(fileName);
	    headerAppender.setAppend(false);  // set to false so it overwrites any existing file

	    PatternLayoutEncoder headerEncoder = new PatternLayoutEncoder();
	    headerEncoder.setContext(loggerContext);
	    headerEncoder.setPattern("Date, Thread, Level, Logger, Message%n");  // the header line
	    headerEncoder.start();

	    headerAppender.setEncoder(headerEncoder);
	    headerAppender.start();

	    // create a dummy event to trigger the header
	    Logger rootLogger = loggerContext.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
	    rootLogger.addAppender(headerAppender);
	    rootLogger.info("init");  // this line will not actually appear in the file because we'll stop the appender immediately
	    headerAppender.stop();

	    // now create the main FileAppender for log entries
	    FileAppender<ILoggingEvent> fileAppender = new FileAppender<>();
	    fileAppender.setContext(loggerContext);
	    fileAppender.setFile(fileName);
	    fileAppender.setAppend(true);  // set to true so it appends to the existing file

	    PatternLayoutEncoder layoutEncoder = new PatternLayoutEncoder();
	    layoutEncoder.setContext(loggerContext);
	    // Only logging Date, Thread, Level, Logger, and Message. Encapsulating Message in quotes to handle any commas in the message.
	    layoutEncoder.setPattern("%d{yyyy-MM-dd'T'HH:mm:ss.SSS}, [%thread], %-5level, %logger{36}, \"%msg\"%n");
	    layoutEncoder.start();

	    fileAppender.setEncoder(layoutEncoder);
	    fileAppender.start();

	    rootLogger.setLevel(Level.INFO);
	    rootLogger.addAppender(fileAppender);

	    return fileName;

	}
}
