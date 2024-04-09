package am.matveev.quiz.services;

import am.matveev.quiz.config.BotConfig;
import am.matveev.quiz.model.Question;
import am.matveev.quiz.model.Quiz;
import am.matveev.quiz.model.Quiz2;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramBot extends TelegramLongPollingBot{

    private final BotConfig config;
    private final Quiz quiz;
    private final Quiz2 quiz2;
    private final UserService userService;
    private boolean greetingSent = false;
    private int currentQuestionIndex = 0;
    private boolean isFirstQuizCompleted = false;
    private int maxFirstQuizScore;
    private boolean continueButtonShown = false;

    @Override
    public String getBotUsername(){
        return config.getName();
    }

    @Override
    public String getBotToken(){
        return config.getToken();
    }

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            long chatId = update.getMessage().getChatId();
            String messageText = update.getMessage().getText();

            if (messageText.equals("/start")) {
                sendRegistrationMessage(chatId);
            } else if (messageText.equals("/continue")) {
                if (isFirstQuizCompleted) {
                    startSecondQuiz(chatId);
                } else {
                    SendMessage message = new SendMessage();
                    message.setChatId(String.valueOf(chatId));
                    message.setText("Вы не можете продолжить викторину, пока не завершите первую. Продолжить первую викторину, отправьте ответ на текущий вопрос или дождитесь завершения.");
                    try {
                        execute(message);
                    } catch (TelegramApiException e) {
                        log.error("Failed to send message", e);
                    }
                }
            } else {
                userService.registerUser(chatId, messageText);
                sendNextQuestion(chatId);
            }
        } else if (update.hasCallbackQuery()) {
            String userAnswer = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            checkAnswer(chatId, userAnswer);
        }
    }


    private void sendRegistrationMessage(long chatId){
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Для прохождения викторины, пожалуйста, зарегистрируйтесь. Введите ваше имя:");
        try{
            execute(message);
        }catch(TelegramApiException e){
            log.error("Failed to send registration message", e);
        }
    }

    private void sendGreeting(long chatId) throws TelegramApiException{
        InputStream inputStream = getClass().getResourceAsStream("/static/1200x630wa.png");
        InputFile inputFile = new InputFile(inputStream, "1200x630wa.png");

        SendPhoto photo = new SendPhoto();
        photo.setChatId(String.valueOf(chatId));
        photo.setPhoto(inputFile);
        execute(photo);

        String greetingMessage = "Привет! Это Quiz Bot. Попробуй пройти нашу футбольную викторину.";
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(greetingMessage);
        execute(message);
    }

        private void sendNextQuestion(long chatId) throws TelegramApiException {
        if (!greetingSent) {
            sendGreeting(chatId);
            greetingSent = true;
        }

        List<Question> questions;
        if (!isFirstQuizCompleted) {
            questions = quiz.getQuestions();
        } else {
            questions = quiz2.getQuestions();
        }

        Question question = questions.get(currentQuestionIndex);

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(question.getQuestionText());

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        for (String answer : question.getAnswers()) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(answer);
            button.setCallbackData(answer);
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(button);
            keyboard.add(row);
        }

        if (currentQuestionIndex == questions.size() - 1 && continueButtonShown) {
            InlineKeyboardButton continueButton = new InlineKeyboardButton();
            continueButton.setText("Продолжить");
            continueButton.setCallbackData("continue");

            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(continueButton);
            keyboard.add(row);
        }

        keyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(keyboardMarkup);

        execute(message);
    }


    private void startSecondQuiz(long chatId) throws TelegramApiException{
        resetQuiz();
        sendNextQuestion(chatId);
    }

    private int score = 0;


        private void checkAnswer(long chatId, String userAnswer) throws TelegramApiException{
        List<Question> questions;
        if(! isFirstQuizCompleted){
            questions = quiz.getQuestions();
        }else{
            questions = quiz2.getQuestions();
        }

        Question currentQuestion = questions.get(currentQuestionIndex);
        String correctAnswer = currentQuestion.getCorrectAnswer();
        String response;
        if(userAnswer.equalsIgnoreCase(correctAnswer)){
            response = "Верно!";
            score++;
            if(! isFirstQuizCompleted){
                maxFirstQuizScore++; // Увеличиваем количество правильных ответов на вопросы первой викторины
            }
        }else{
            response = "Неверно. Правильный ответ: " + correctAnswer;
        }
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(response);
        execute(message);

        if(currentQuestionIndex + 1 < questions.size()){
            currentQuestionIndex++;
            sendNextQuestion(chatId);
        }else{
            if(! isFirstQuizCompleted){
                isFirstQuizCompleted = true;
                currentQuestionIndex = 0;
                if(maxFirstQuizScore == quiz.getQuestions().size() && maxFirstQuizScore >= 10){ // Проверяем, набрано ли максимальное количество баллов и не менее 10
                    startSecondQuiz(chatId);
                }else{
                    SendMessage endMessage = new SendMessage();
                    endMessage.setChatId(String.valueOf(chatId));
                    endMessage.setText("Поздравляем! Вы завершили первую викторину. Но для доступа ко второй викторине, наберите максимальное количество баллов (не менее 10): " + quiz.getQuestions().size());
                    execute(endMessage);
                    resetQuiz();
                }
            }else{
                SendMessage endMessage = new SendMessage();
                endMessage.setChatId(String.valueOf(chatId));
                endMessage.setText("Вопросы закончились. Спасибо за участие! Вы набрали : " + score + " балла(ов). Чтобы начать заново, отправьте команду /start");
                execute(endMessage);
                resetQuiz();
            }
        }
    }

    private void resetQuiz(){
        score = 0;
        currentQuestionIndex = 0;
    }
}

