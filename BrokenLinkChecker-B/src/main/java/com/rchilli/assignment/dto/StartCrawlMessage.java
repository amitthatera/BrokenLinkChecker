package com.rchilli.assignment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StartCrawlMessage {

	private String url;
    private String email;
    private boolean sendEmail;
}
