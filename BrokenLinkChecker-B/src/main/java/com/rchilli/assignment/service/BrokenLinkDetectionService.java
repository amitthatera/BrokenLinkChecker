package com.rchilli.assignment.service;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.rchilli.assignment.dto.CrawlReport;

import jakarta.annotation.PreDestroy;

@Service
public class BrokenLinkDetectionService {

	@Value("${connection.timeout}")
	private Integer connectionTimeout;

	@Value("${socket.timeout}")
	private Integer socketTimeout;

	@Value("${request.timeout}")
	private Integer requestTimeout;

	@Value("${crawl.max-depth}")
	private Integer maxDepth;

	@Autowired
	private SimpMessagingTemplate simpMessagingTemplate;

	@Autowired
	private EmailSenderService emailSender;

	@Autowired
	private CrawlReport crawlReport;

	@Autowired
	private LoggerService log;

	private final org.slf4j.Logger logger;

	private final ExecutorService executorService;

	private final Set<String> visitedLinks;

	private final Set<String> redirectedLinks;

	private static final Pattern ILLEGAL_CHARACTERS_PATTERN = Pattern.compile("[{}]");

	public BrokenLinkDetectionService() {
		this.logger = LoggerFactory.getLogger(BrokenLinkDetectionService.class);
		int availableProcessors = Runtime.getRuntime().availableProcessors();
		this.executorService = Executors.newWorkStealingPool(availableProcessors * 3);
		this.visitedLinks = new HashSet<>();
		this.redirectedLinks = new HashSet<>();
	}

	@PreDestroy
	public void cleanUp() {
		// Properly shut down the ExecutorService
		executorService.shutdown();
		try {
			executorService.awaitTermination(8, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			logger.warn("ExecutorService shutdown interrupted.", e);
		}
	}

	private void logAndSendToWebSocket(String message, int statusCode) {
		String messageWithStatus = message + " Status code: " + statusCode;
		logger.info(messageWithStatus);
		simpMessagingTemplate.convertAndSend("/topic/logs", messageWithStatus);
	}

	public CrawlReport detectBrokenLinks(String url, String email, boolean sendEmail) {
		String file = log.getLogs();
		crawlAsync(url, 0);
		if (sendEmail == true) {
			emailSender.sendEmail(email, file);
		}
		logger.info("Completed");
		return crawlReport;
	}

	private void crawlAsync(String url, int depth) {
		if (depth > maxDepth || visitedLinks.contains(url)) {
			return;
		}

		if (ILLEGAL_CHARACTERS_PATTERN.matcher(url).find()) {
			logger.warn("Skipping URL with illegal characters in the query: {}", url);
			return;
		}
		crawlReport.incrementTotalVisited();
		visitedLinks.add(url);
		int maxRetries = 3;
		for (int retry = 1; retry <= maxRetries; retry++) {
			try (CloseableHttpClient httpClient = HttpClientBuilder.create().setMaxConnTotal(100)
					.setMaxConnPerRoute(100).disableCookieManagement().build()) {
				String encodedUrl = url.replace(" ", "%20");
				RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(connectionTimeout)
						.setConnectionRequestTimeout(requestTimeout).setSocketTimeout(socketTimeout).build();
				HttpGet request = new HttpGet(encodedUrl);
				request.setConfig(requestConfig);

				HttpResponse response = httpClient.execute(request);
				int statusCode = response.getStatusLine().getStatusCode();
				if (statusCode == HttpStatus.SC_OK) {
					 logAndSendToWebSocket("Valid link: " + url, statusCode);
				}
				if (statusCode >= HttpStatus.SC_BAD_REQUEST) {
					logAndSendToWebSocket("Broken link detected: " + url, statusCode);
					crawlReport.addBrokenLink(url, statusCode);
				}

				if (statusCode >= HttpStatus.SC_MULTIPLE_CHOICES && statusCode <= HttpStatus.SC_MOVED_PERMANENTLY) {
					// Handle redirects
					String redirectLocation = response.getFirstHeader("Location").getValue();
					if (!redirectedLinks.contains(redirectLocation)) {
						redirectedLinks.add(redirectLocation);
						logger.info("Redirect link detected: {} (Redirected to: {})", url, redirectLocation);
						crawlAsync(redirectLocation, depth);
					}
				}

				if (depth < maxDepth) {
					Document doc = Jsoup.parse(response.getEntity().getContent(), StandardCharsets.UTF_8.name(), url);
					List<Element> links = new ArrayList<>(doc.select("a[href]"));
					crawlReport.incrementTotalAnchors();
					crawlLinks(links, depth + 1);
				}
				break;
			} catch (ConnectException | SocketTimeoutException e) {
				logAndSendToWebSocket("Retry {} - Connection timeout or error while connecting to URL: {}"+ url, retry);
				if (retry < maxRetries) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException ex) {
						logger.error("Thread interrupted while sleeping for retry delay.");
						Thread.currentThread().interrupt();
					}
				}
			} catch (CompletionException e) {
				logger.warn("Skipping URL due to exception: {}", url, e);
			} catch (HttpStatusException e) {
				logAndSendToWebSocket("Error while connecting url "+url, e.getStatusCode());
				break;
			} catch (IOException e) {
				logger.error("Error connecting to URL: " + url);
				crawlReport.incrementTotalErrors();
				break;
			}
		}
	}

	private void crawlLinks(List<Element> links, int depth) {
		List<CompletableFuture<Void>> futures = new ArrayList<>();
		for (Element link : links) {
			String nextUrl = link.absUrl("href");
			if (shouldSkipLink(nextUrl)) {
				continue;
			}
			CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
				// Check if the URL has already been visited to avoid redundant crawling
				if (!visitedLinks.contains(nextUrl)) {
					crawlAsync(nextUrl, depth);
				}
			}, executorService);
			futures.add(future);
		}
		CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
		allFutures.join();
		try {
			allFutures.get(3000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			// Handle timeout or other exceptions if needed.
			// In this example, we just log the exception and continue.
			logger.warn("Exception occurred during crawling.", e);
		}
	}

	private boolean shouldSkipLink(String href) {
		if (href.startsWith("mailto:") || href.startsWith("tel:")) {
			return true; // Skip mailto and tel links
		}

		if (href.startsWith("#")) {
			return true; // Skip fragment links
		}

		if (href.endsWith(".onion")) {
			return true;
		}

		if (href.contains("page") || href.contains("next") || href.contains("prev")) {
			return true; // Skip pagination links
		}

		String extension = href.substring(href.lastIndexOf(".") + 1);
		if (extension.matches("(pdf|doc|docx|jpg|jpeg|png|gif)")) {
			return true; // Skip file links (add more extensions as needed)
		}

		if (href.contains("login") || href.contains("logout")) {
			return true; // Skip login/logout links
		}

		return false; // Crawl all other links
	}

}
