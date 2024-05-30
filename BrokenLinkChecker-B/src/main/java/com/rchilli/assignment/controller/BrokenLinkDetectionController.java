package com.rchilli.assignment.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rchilli.assignment.dto.CrawlReport;
import com.rchilli.assignment.service.BrokenLinkDetectionService;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class BrokenLinkDetectionController {

	@Autowired
    private BrokenLinkDetectionService linkCheckerService;
	
	@GetMapping("/broken-links")
    public ResponseEntity<CrawlReport> getBrokenLinks(@RequestParam("url") String url,
    		                                @RequestParam("email") String email,
    		                                @RequestParam(required = false) boolean sendEmail) {
        CrawlReport crawlReport = linkCheckerService.detectBrokenLinks(url, email, sendEmail);
        return new ResponseEntity<CrawlReport>(crawlReport, HttpStatus.OK);
    }
	
}
