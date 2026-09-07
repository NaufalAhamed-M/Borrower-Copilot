package com.borrowercopilot.controller;

import com.borrowercopilot.dto.AssessmentRequest;
import com.borrowercopilot.dto.AssessmentResponse;
import com.borrowercopilot.dto.OfferRequest;
import com.borrowercopilot.dto.OfferResponse;
import com.borrowercopilot.service.AssessmentService;
import com.borrowercopilot.service.OfferService;
import com.borrowercopilot.service.QuestionService;
import com.borrowercopilot.model.Enums.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class AssessmentController {
    private final AssessmentService assessmentService;
    private final QuestionService questionService;
    private final OfferService offerService;

    public AssessmentController(AssessmentService assessmentService, QuestionService questionService, OfferService offerService) {
        this.assessmentService = assessmentService;
        this.questionService = questionService;
        this.offerService = offerService;
    }

    @PostMapping("/assessment")
    public ResponseEntity<AssessmentResponse> assess(@Valid @RequestBody AssessmentRequest request) {
        return ResponseEntity.ok(assessmentService.assess(request));
    }

    @PostMapping("/offer-check")
    public ResponseEntity<OfferResponse> offerCheck(@Valid @RequestBody OfferRequest request) {
        return ResponseEntity.ok(offerService.check(request));
    }

    @GetMapping("/questions")
    public ResponseEntity<List<QuestionService.Question>> questions(
            @RequestParam(required = false) LoanType loanType,
            @RequestParam(required = false) IncomeType incomeType) {

        return ResponseEntity.ok(questionService.questions(
                loanType == null ? LoanType.PERSONAL : loanType,
                incomeType == null ? IncomeType.SALARIED : incomeType));
    }
}
