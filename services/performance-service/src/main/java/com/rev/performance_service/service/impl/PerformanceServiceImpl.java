package com.rev.performance_service.service.impl;

import com.rev.performance_service.entity.*;
import com.rev.performance_service.repository.GoalRepository;
import com.rev.performance_service.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PerformanceServiceImpl {

    private final ReviewRepository reviewRepository;
    private final GoalRepository goalRepository;

    public PerformanceReview createReview(Long userId, int year, String deliverables, String accomplishments, String improvements, int selfRating) {
        PerformanceReview review = new PerformanceReview();
        review.setUserId(userId);
        review.setYear(year);
        review.setDeliverables(deliverables);
        review.setAccomplishments(accomplishments);
        review.setImprovements(improvements);
        review.setSelfRating(selfRating);
        review.setStatus(ReviewStatus.DRAFT);
        return reviewRepository.save(review);
    }

    public PerformanceReview submitReview(Long reviewId) {
        PerformanceReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        review.setStatus(ReviewStatus.SUBMITTED);
        return reviewRepository.save(review);
    }

    public PerformanceReview provideFeedback(Long reviewId, String feedback, int managerRating) {
        PerformanceReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        review.setManagerFeedback(feedback);
        review.setManagerRating(managerRating);
        review.setStatus(ReviewStatus.REVIEWED);
        return reviewRepository.save(review);
    }

    public List<PerformanceReview> getMyReviews(Long userId) {
        return reviewRepository.findByUserId(userId);
    }

    public List<PerformanceReview> getTeamReviews(Long managerId) {
        return reviewRepository.findAll().stream()
                .filter(r -> r.getUserId() != null)
                .toList();
    }

    public Goal createGoal(Long userId, String title, String description) {
        Goal goal = new Goal();
        goal.setUserId(userId);
        goal.setTitle(title);
        goal.setDescription(description);
        goal.setProgress(0);
        goal.setStatus(GoalStatus.NOT_STARTED);
        return goalRepository.save(goal);
    }

    public Goal updateGoalProgress(Long goalId, int progress, GoalStatus status) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new RuntimeException("Goal not found"));
        goal.setProgress(progress);
        goal.setStatus(status);
        return goalRepository.save(goal);
    }

    public Goal addGoalComment(Long goalId, String comment) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new RuntimeException("Goal not found"));
        goal.setManagerComment(comment);
        return goalRepository.save(goal);
    }

    public List<Goal> getMyGoals(Long userId) {
        return goalRepository.findByUserId(userId);
    }

    public void deleteGoal(Long goalId) {
        goalRepository.deleteById(goalId);
    }
}
