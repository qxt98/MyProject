package com.smartpharma.service.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** OpenAI 兼容的 chat completions 响应 DTO */
@JsonIgnoreProperties(ignoreUnknown = true)
class ChatResponseDto {
    private List<Choice> choices;

    String getContent() {
        if (choices != null && !choices.isEmpty() && choices.get(0).message != null) {
            return choices.get(0).message.content;
        }
        return null;
    }

    void setChoices(List<Choice> choices) {
        this.choices = choices;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Choice {
        private Message message;

        Message getMessage() {
            return message;
        }

        void setMessage(Message message) {
            this.message = message;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Message {
        private String content;

        String getContent() {
            return content;
        }

        @JsonProperty("content")
        void setContent(String content) {
            this.content = content;
        }
    }
}
