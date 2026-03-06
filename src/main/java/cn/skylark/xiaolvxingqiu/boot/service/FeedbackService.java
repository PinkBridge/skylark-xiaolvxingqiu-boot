package cn.skylark.xiaolvxingqiu.boot.service;

import cn.skylark.xiaolvxingqiu.boot.mapper.FeedbackMapper;
import cn.skylark.xiaolvxingqiu.boot.model.Feedback;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeedbackService {

    private final FeedbackMapper feedbackMapper;

    public FeedbackService(FeedbackMapper feedbackMapper) {
        this.feedbackMapper = feedbackMapper;
    }

    public Feedback submit(Long userId, Feedback request) {
        Feedback feedback = new Feedback();
        feedback.setUserId(userId);
        feedback.setContent(request.getContent() == null ? "" : request.getContent().trim());
        feedback.setContact(request.getContact() == null ? "" : request.getContact().trim());
        feedbackMapper.insert(feedback);
        return feedback;
    }

    public List<Feedback> listByUserId(Long userId) {
        return feedbackMapper.selectByUserId(userId);
    }
}
