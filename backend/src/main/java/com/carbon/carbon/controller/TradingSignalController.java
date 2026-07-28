package com.carbon.carbon.controller;

import com.carbon.carbon.entity.TradingSignal;
import com.carbon.carbon.service.TradingSignalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/signal")
@CrossOrigin(origins = "*")
public class TradingSignalController {

    @Autowired
    private TradingSignalService tradingSignalService;

    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateSignal() {
        return ResponseEntity.ok(tradingSignalService.generateSignal());
    }

    @GetMapping("/latest")
    public ResponseEntity<TradingSignal> getLatestSignal() {
        TradingSignal signal = tradingSignalService.getLatestSignal();
        if (signal != null) {
            return ResponseEntity.ok(signal);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/history")
    public ResponseEntity<List<TradingSignal>> getSignalHistory() {
        return ResponseEntity.ok(tradingSignalService.getSignalHistory());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TradingSignal> getSignalById(@PathVariable Long id) {
        TradingSignal signal = tradingSignalService.getSignalById(id);
        if (signal != null) {
            return ResponseEntity.ok(signal);
        }
        return ResponseEntity.notFound().build();
    }
}
