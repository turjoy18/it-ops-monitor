package com.itopsmonitor.ticket;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MockJiraControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void mockJiraCreateIssueReturnsKey() throws Exception {
        String body = """
                {
                  "fields": {
                    "project": { "key": "OPS" },
                    "summary": "test",
                    "description": "desc",
                    "issuetype": { "name": "Bug" }
                  }
                }
                """;

        mockMvc.perform(post("/mocks/jira/rest/api/2/issue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.key").value(org.hamcrest.Matchers.startsWith("OPS-")))
                .andExpect(jsonPath("$.id").isNotEmpty());
    }
}
