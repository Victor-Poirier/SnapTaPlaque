package com.example.snaptaplaque.models.api.predictions;

import java.util.List;

public class HistoryResponse {
    private List<HistoryResult> history;

    public HistoryResponse(List<HistoryResult> history) {
        this.history = history;
    }

    public List<HistoryResult> getHistory() {
        return history;
    }
}
