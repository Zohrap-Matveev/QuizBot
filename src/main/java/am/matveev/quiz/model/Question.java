package am.matveev.quiz.model;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class Question{

    private String questionText;
    private List<String> answers;
    private String correctAnswer;
}
