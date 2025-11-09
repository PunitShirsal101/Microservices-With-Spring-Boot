package com.bankingsystemapi.controller;

import com.bankingsystemapi.entity.Transaction;
import com.bankingsystemapi.service.ComplianceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/compliance")
public class ComplianceController {

    @Autowired
    private ComplianceService complianceService;

    @GetMapping("/report")
    public ResponseEntity<List<Transaction>> getComplianceReport() {
        List<Transaction> report = complianceService.generateComplianceReport();
        return ResponseEntity.ok(report);
    }
}
