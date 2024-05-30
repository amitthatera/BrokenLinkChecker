package com.rchilli.assignment.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.rchilli.assignment.dto.StartCrawlMessage;
import com.rchilli.assignment.service.BrokenLinkDetectionService;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@Controller
public class WebSocketController {

	@Autowired
	private BrokenLinkDetectionService linkCheckerService;

	@MessageMapping("/startCrawl")
	public void startCrawl(StartCrawlMessage message) {
		linkCheckerService.detectBrokenLinks(message.getUrl(), message.getEmail(), message.isSendEmail());
	}
}